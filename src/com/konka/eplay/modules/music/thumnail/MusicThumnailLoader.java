package com.konka.eplay.modules.music.thumnail;

import iapp.eric.utils.base.Trace;
import iapp.eric.utils.custom.model.APIC;
import iapp.eric.utils.metadata.Mp3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.konka.eplay.Configuration;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.modules.AudioThumbnail;
import com.konka.eplay.modules.music.MusicUtils;
import com.konka.eplay.modules.music.thumnail.ImageSizeUtil.ImageSize;

/**
 * 图片加载类
 *
 */
public class MusicThumnailLoader {
	private static MusicThumnailLoader mInstance;

	/**
	 * 图片缓存的核心对象
	 */
	private LruCache<String, Bitmap> mLruCache;
	/**
	 * 线程池
	 */
	private ExecutorService mThreadPool;
	private static final int DEAFULT_THREAD_COUNT = 1;
	/**
	 * 队列的调度方式
	 */
	private Type mType = Type.LIFO;
	/**
	 * 任务队列
	 */
	private LinkedList<Runnable> mTaskQueue;
	/**
	 * 后台轮询线程
	 */
	private Thread mPoolThread;
	private Handler mPoolThreadHandler;
	/**
	 * UI线程中的Handler
	 */
	private Handler mUIHandler;

	private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
	private Semaphore mSemaphoreThreadPool;
	// 是否开启硬盘缓存
	private boolean isDiskCacheEnable = true;

	String mCachePathDir = "";
	String mCacheAbsolutePath = "";

	private static final String TAG = "Eplay";
	private static final String DEFAULT_IMAGE = "defaultimage";

	/**
	 * @Description: 队列类型，FIFO--先进先出，LIFO--后进先出
	 */
	public enum Type {
		FIFO, LIFO;
	}

	// 单例模式
	private MusicThumnailLoader(int threadCount, Type type) {
		init(threadCount, type);
	}

	/**
	 *
	 * @Title: getInstance
	 * @Description: 获得实例
	 */
	public static MusicThumnailLoader getInstance() {
		if (mInstance == null) {
			synchronized (MusicThumnailLoader.class) {
				if (mInstance == null) {
					mInstance = new MusicThumnailLoader(DEAFULT_THREAD_COUNT, Type.LIFO);
				}
			}
		}
		return mInstance;
	}

	/**
	 * @Title: getInstance
	 * @Description: 获得实例
	 */
	public static MusicThumnailLoader getInstance(int threadCount, Type type) {
		if (mInstance == null) {
			synchronized (MusicThumnailLoader.class) {
				if (mInstance == null) {
					mInstance = new MusicThumnailLoader(threadCount, type);
				}
			}
		}
		return mInstance;
	}

	/**
	 * @Title: init
	 * @Description: 初始化操作，包括后台轮询，内存初始化和创建线程池
	 */
	private void init(int threadCount, Type type) {
		initBackThread();

		// 获取我们应用的最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheMemory = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}

		};

		// 创建线程池
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mTaskQueue = new LinkedList<Runnable>();
		mType = type;
		mSemaphoreThreadPool = new Semaphore(threadCount);
	}

	/**
	 * 初始化后台轮询线程
	 */
	private void initBackThread() {
		// 后台轮询线程
		mPoolThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				mPoolThreadHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// 线程池去取出一个任务进行执行
						mThreadPool.execute(getTask());
						try {
							mSemaphoreThreadPool.acquire();
						} catch (InterruptedException e) {
						}
					}
				};
				// 释放一个信号量
				mSemaphorePoolThreadHandler.release();
				Looper.loop();
			};
		};
		// 开启线程
		mPoolThread.start();
	}

	/**
	 *
	 * @Title: loadImage
	 * @Description: 根据path为imageview设置图片
	 * @param path
	 *            网络图片url
	 * @param imageView
	 *            图片容器
	 * @param isFromNet
	 *            是否从网络中下载
	 */
	public void loadImage(final String path, final ImageView imageView) {

		Trace.Debug("loadImage  MusicPath=="+path);

		imageView.setTag(path);
		String rootPath = Utils.getRootPath(path);
		mCachePathDir = rootPath + "/" + MusicUtils.APP_DIR + "/musicThumnail/";
		if (!new File(mCachePathDir).exists()) {
			new File(mCachePathDir).mkdirs();
		}
		String md5Name = Utils.Md5(path.substring(rootPath.length()));
		String cacheAbsolutePath = rootPath + "/" + MusicUtils.APP_DIR + "/musicThumnail/" + md5Name + ".png";

		if (mUIHandler == null) {
			mUIHandler = new Handler() {
				public void handleMessage(Message msg) {
					// 获取得到图片，为imageview回调设置图片
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					Bitmap bm = holder.bitmap;
					ImageView imageview = holder.imageView;
					String path = holder.path;
					// 将path与getTag存储路径进行比较
					if (imageview.getTag().toString().equals(path)) {
						if (null != bm) {
							imageview.setImageBitmap(bm);
						} else {
							imageview.setImageResource(R.drawable.m);
						}

					}
				};
			};
		}
		// 根据path在内存缓存中获取bitmap
		Bitmap bm = getBitmapFromLruCache(cacheAbsolutePath);

		if (bm != null) {
			refreashBitmap(path, imageView, bm);
		} else {
			addTask(buildTask(path, imageView, cacheAbsolutePath));
		}

	}

	/**
	 * 根据传入的参数，新建一个任务
	 *
	 * @param path
	 * @param imageView
	 * @param isFromNet
	 * @return
	 */
	private Runnable buildTask(final String path, final ImageView imageView, final String cachepath) {
		return new Runnable() {
			@Override
			public void run() {
				Bitmap bm = null;
				File file = new File(cachepath);
				if (file.exists())// 如果在缓存文件中发现
				{
					bm = loadImageFromLocal(file.getAbsolutePath(), imageView);
				} else {
					Trace.Info(file.getAbsolutePath());
					boolean getThumbState = getThumb(path, file);
					if (getThumbState) {
						bm = loadImageFromLocal(file.getAbsolutePath(), imageView);
					}
				}
				// 3、把图片加入到缓存
				addBitmapToLruCache(cachepath, bm);
				refreashBitmap(path, imageView, bm);
				mSemaphoreThreadPool.release();
			}

		};
	}

	private boolean getThumb(String path, File file) {
		Mp3 m = new Mp3(path, false, true);
		if (null != m.getTagID3V2()) {
			// System.out.println("####ID3V2不为空");
			if (m.getTagID3V2().getTagHeader().equals("ID3")) {
				// Trace.Info("获取到了DI3V2标签中的数据");
				if (null != m.getTagID3V2().getTagFrame().get("APIC")) {
					APIC apic = (APIC) (m.getTagID3V2().getTagFrame().get("APIC").getContent());
					if (null != apic) {
						if (apic.pictureData != null) {
							// Trace.Info("music cover byte[] is not null length-->"
							// + apic.pictureData.length);
							byte[] buffer = apic.pictureData;
							Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

							if (isDiskCacheEnable)// 检测是否开启硬盘缓存
							{
								if (null != bm) {
									FileOutputStream out;
									try {
										out = new FileOutputStream(file);
										//TODO 将图片压缩
										bm.compress(Bitmap.CompressFormat.WEBP, 80, out);
										out.flush();
										out.close();
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
							return true;
						} else if (apic.url != null) {
							// Trace.Info("music cover url -->" + apic.url);
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * @Title: loadImageFromLocal
	 * @Description: 从本地获取图片文件
	 * @param path
	 * @param imageView
	 * @return Bitmap
	 */
	private Bitmap loadImageFromLocal(final String path, final ImageView imageView) {
		Bitmap bm;
		// 加载图片
		// 图片的压缩
		// 1、获得图片需要显示的大小
		ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
		// 2、压缩图片
		bm = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height);
		return bm;
	}

//	private Bitmap loadImageFromResource(int  rsID, final ImageView imageView) {
//		Bitmap bm;
//		// 加载图片
//		// 图片的压缩
//		// 1、获得图片需要显示的大小
//		ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
//
//		//bm = BitmapFactory.decodeResource(get, R.drawable.music_bg);
//		// 2、压缩图片
//		//bm = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height);
//		//return bm;
//	}

	/**
	 * @Title: getTask
	 * @Description: 从任务队列取出一个方法
	 */
	private Runnable getTask() {
		if (mType == Type.FIFO) {
			return mTaskQueue.removeFirst();
		} else if (mType == Type.LIFO) {
			return mTaskQueue.removeLast();
		}
		return null;
	}

	/**
	 * @Title: md5
	 * @Description:利用签名辅助类，将字符串字节数组
	 */
	public String md5(String str) {
		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			digest = md.digest(str.getBytes());
			return bytes2hex02(digest);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * @Title: bytes2hex02
	 * @Description: 将字节数组转换成16进制
	 * @param bytes
	 */
	public String bytes2hex02(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		for (byte b : bytes) {
			// 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
			tmp = Integer.toHexString(0xFF & b);
			if (tmp.length() == 1) {// 每个字节8位，转为16进制标志，2个16进制位
				tmp = "0" + tmp;
			}
			sb.append(tmp);
		}
		return sb.toString();

	}

	/**
	 *
	 * @Title: refreashBitmap
	 * @Description: 封装image相关信息，传递到handler中取刷新ui
	 * @param path
	 * @param imageView
	 * @param bm
	 */
	private void refreashBitmap(final String path, final ImageView imageView, Bitmap bm) {
		Message message = Message.obtain();
		ImgBeanHolder holder = new ImgBeanHolder();
		holder.bitmap = bm;
		holder.path = path;
		holder.imageView = imageView;
		message.obj = holder;
		mUIHandler.sendMessage(message);
	}

	/**
	 *
	 * @Title: addBitmapToLruCache
	 * @Description: 将图片加入LruCache
	 * @param path
	 *            根据path来标示
	 * @param bm
	 */
	protected void addBitmapToLruCache(String path, Bitmap bm) {
		if (getBitmapFromLruCache(path) == null) {
			if (bm != null)
				mLruCache.put(path, bm);
		}
	}

	/**
	 *
	 * @Title: decodeSampledBitmapFromPath
	 * @Description: 根据图片需要显示的宽和高对图片进行压缩
	 * @param path
	 *            本地文件路径
	 * @param width
	 *            图片需要显示的高度
	 * @param height
	 *            图片需要显示的高度
	 */
	protected Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
		// 获得图片的宽和高，并不把图片加载到内存中
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options, width, height);

		// 使用获得到的InSampleSize再次解析图片
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}

	/**
	 *
	 * @Title: addTask
	 * @Description: 添加一个下载任务
	 */
	private synchronized void addTask(Runnable runnable) {
		mTaskQueue.add(runnable);
		// if(mPoolThreadHandler==null)wait();
		try {
			if (mPoolThreadHandler == null)
				mSemaphorePoolThreadHandler.acquire();
		} catch (InterruptedException e) {
		}
		mPoolThreadHandler.sendEmptyMessage(0x110);
	}

	/**
	 *
	 * @Title: getDiskCacheDir
	 * @Description: 获得缓存图片的地址，封装成File对象
	 * @param context
	 * @param uniqueName
	 *            唯一的url对应的唯一MD5值
	 * @return File
	 */
	public File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * @Title: getBitmapFromLruCache
	 * @Description: 根据path在缓存中获取bitmap
	 */
	private Bitmap getBitmapFromLruCache(String key) {
		return mLruCache.get(key);
	}

	/**
	 *
	 * @ClassName: ImgBeanHolder
	 * @Description: 封装显示imageview的相关信息的bean
	 * @author xuyunyu
	 * @date 2015年1月16日 上午10:41:32
	 */
	private class ImgBeanHolder {
		Bitmap bitmap;
		ImageView imageView;
		String path;
	}
}
