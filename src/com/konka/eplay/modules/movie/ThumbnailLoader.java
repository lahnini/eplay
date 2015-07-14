package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Video.Thumbnails;

import com.konka.eplay.Constant;
import com.konka.eplay.Utils;

/**
 * 用于异步加载视频列表thumbnail
 * 
 * @author situ hui
 * 
 */
public class ThumbnailLoader {

	private static int TIME_GET_FRAME = 10 * 1000000;

	public static String DIR = "movielistthumbnail";
	// 本地文件缓存文件个数
	private int FILE_MAX = 100;
	// 软引用缓存
	private Map<String, SoftReference<Bitmap>> mReferenceMap;
	// 异步任务列表
	private List<ThumbnailTask> mTaskList;

	private int mThumbnailW = 320;

	private int mThumbnailH = 180;

	private float mViewRatio;

	private boolean mFromVideoToggle;// 开关，标记是否去从视频文件中获取

	public ThumbnailLoader(Context context) {
		mTaskList = new ArrayList<ThumbnailTask>();
		mReferenceMap = new HashMap<String, SoftReference<Bitmap>>();
		mThumbnailW = Utils.dip2px(context, 213);
		mThumbnailH = Utils.dip2px(context, 120);
		mViewRatio = mThumbnailW / mThumbnailH;
	}

	/**
	 * 对外方法，异步获取视频thumbnail
	 * 
	 * @param videoPath
	 * @param listener
	 */
	public void loadThumbnail(String videoPath, ThumbnailLoaderListener listener) {
		if (videoPath == null) {
			return;
		}
		if (listener != null) {
			listener.onThumbnailLoadStart();
		}
		// 从软引用中取
		Bitmap bitmap = getFromReferenceMap(videoPath);
		if (bitmap != null) {
			Trace.Debug("从软引用中取得");
			if (listener != null) {
				listener.onThumbnailLoadEnd(bitmap);
			}
			return;
		}
		mFromVideoToggle = true;//可从视频文件中取
		ThumbnailTask task = new ThumbnailTask(videoPath, listener);
		mTaskList.add(task);
		task.execute();
	}

	/**
	 * 只从软引用和文件缓存去取，不从视频中取
	 * 
	 * @param videoPath
	 * @param listener
	 * @return
	 */
	public void loadThumbnailLocal(String videoPath, ThumbnailLoaderListener listener) {
		if (videoPath == null) {
			return;
		}
		if (listener != null) {
			listener.onThumbnailLoadStart();
		}
		// 从软引用中取
		Bitmap bitmap = getFromReferenceMap(videoPath);
		if (bitmap != null) {
			Trace.Debug("从软引用中取得");
			if (listener != null) {
				listener.onThumbnailLoadEnd(bitmap);
			}
			return;
		}
		mFromVideoToggle = false;//不从视频文件中取
		ThumbnailTask task = new ThumbnailTask(videoPath, listener);
		mTaskList.add(task);
		task.execute();
	}

	// 从软引用中取
	private Bitmap getFromReferenceMap(String path) {
		SoftReference<Bitmap> reference = mReferenceMap.get(path);
		if (reference != null) {
			return reference.get();
		}
		return null;
	}

	private Bitmap getFromFile(String path) {
		String usbPath = Utils.getRootPath(path);
		String dirName = Constant.APP_DIR + "/" + DIR;
		File dir = new File(usbPath, dirName);
		String subPath = Utils.getSubPath(path);
		String fileName = Utils.Md5(subPath);
		File file = new File(dir, fileName);
		if (!file.exists()) {
			return null;
		}
		else {

			// 获取原图宽高
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file.getPath(), opts);
			int imageWidth = opts.outWidth;
			int imageHeight = opts.outHeight;

			// 设置图片缩放比率
			int inSampleSize = 1;
			Trace.Debug("original image width:" + imageWidth);
			Trace.Debug("original image height:" + imageHeight);
			if (imageWidth > mThumbnailW || imageHeight > mThumbnailH) {
				if (((imageWidth + 0f) / imageHeight) > mViewRatio) {
					inSampleSize = Math.round((imageWidth + 0f) / mThumbnailW);
					Trace.Debug("width insamplesize:" + inSampleSize);
				}
				else {
					inSampleSize = Math.round((imageHeight + 0f) / mThumbnailH);
					Trace.Debug("height insamplesize" + inSampleSize);
				}
			}
			opts.inSampleSize = inSampleSize;
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inJustDecodeBounds = false;

			Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), opts);
			Trace.Debug("######bitmap" + bitmap);
			if (bitmap == null) {
				return null;
			}
			else {
				// 软引用缓存
				mReferenceMap.put(path, new SoftReference(bitmap));
				return bitmap;
			}
		}
	}

	// 把bitmap存入文件,path为视频绝对路径
	private boolean setToFile(Bitmap bitmap, String path) {
		String usbPath = Utils.getRootPath(path);
		String dirName = Constant.APP_DIR + "/" + DIR;
		File dir = new File(usbPath, dirName);
		// dir.setWritable(true,false);
		dir.mkdirs();

		// // 若超出文件缓存个数 ，就清理
		// int fileAmount = Tools.getFileAmount(dir.getPath());
		// if (fileAmount >= FILE_MAX) {
		// Tools.deleteFiles(dir.getPath());
		// }

		String subPath = Utils.getSubPath(path);
		Trace.Debug("thumbnail bitmap key : " + subPath);
		String fileName = Utils.Md5(subPath);
		File file = new File(dir, fileName);
		// 供第三方调用使用
		// file.setWritable(true,false);
		if (file.exists()) {
			Trace.Debug("不再写入文件,thumbnail exists : " + subPath);
			return false;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			try {
				if(fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		MediaMetadataRetriever retriever = null;
		try {
			if (retriever == null) {
				retriever = new MediaMetadataRetriever();
			}
			retriever.setDataSource(videoPath);
			// 获取第10秒处
			bitmap = retriever.getFrameAtTime(TIME_GET_FRAME, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		} catch (Exception e) {
			Trace.Info("getVideoThumbnail runtime exception,video path=" + videoPath);
			bitmap = null;
		} finally {
			if (retriever != null) {
				retriever.release();
			}
		}
		return bitmap;
	}

	private class ThumbnailTask extends AsyncTask<Void, Void, Bitmap> {

		private ThumbnailLoaderListener mThumbnailLoaderListener;
		private String mVideoPath;
		//mFromVideoToggle要不要存一个？

		public ThumbnailTask(String videoPath, ThumbnailLoaderListener listener) {
			mThumbnailLoaderListener = listener;
			mVideoPath = videoPath;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			if (mVideoPath != null) {
				// 从文件存储中取
				Bitmap bitmap = getFromFile(mVideoPath);
				if (bitmap != null) {
					Trace.Debug("从文件中取得");
					return bitmap;
				}
				else if (mFromVideoToggle == true) {
					// 从视频获取thumbnail
					Trace.Debug("从视频获取thumbnail");
					return getVideoThumbnail(mVideoPath, mThumbnailW, mThumbnailH, Thumbnails.MINI_KIND);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null) {
				return;
			}

			if (mThumbnailLoaderListener != null) {
				mThumbnailLoaderListener.onThumbnailLoadEnd(result);
			}
			// 存入软引用
			mReferenceMap.put(mVideoPath, new SoftReference(result));
			if (mFromVideoToggle == true) {
				// 存入文件, 去从视频文件中获取的开关打开才去存本地
				Trace.Debug("" + setToFile(result, mVideoPath));
			}
			mTaskList.remove(this);
		}

	}

	/**
	 * 异步获取视频 thumbnail的回调接口
	 * 
	 * @author situ hui
	 * 
	 */
	public static interface ThumbnailLoaderListener {

		/**
		 * 开始
		 */
		public void onThumbnailLoadStart();

		/**
		 * 结束
		 * 
		 * @param result
		 *            拿到的thumbnail bitmap
		 */
		public void onThumbnailLoadEnd(Bitmap result);
	}
}
