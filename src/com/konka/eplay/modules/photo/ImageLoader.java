package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.MD5;
import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.Utils;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.modules.AsyncImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.wifi.WifiConfiguration.Status;
import android.os.AsyncTask;
import android.os.WorkSource;
import android.widget.ImageView;

/**
 * 实现图片加载的类</br> <b>创建时间：</b>2015-3-17
 * 
 * @author mcsheng
 */
public class ImageLoader {

	private static final String TAG = "ImageLoader";
	private BitmapMemoryCache mBitmapMemoryCache;
	private static ImageLoader sImageLoader = null;
	private static Context sContext;
	private List<BitmapWorkerTask> mTaskList;
	private List<BitmapWorkerTask> mAllTaskList;
	private ExecutorService mExecutorService;
	private Boolean mAllowLoad = true;
	//文件缓存
	private DiskCache mDiskCache;
	
	//是否使用文件缓存，在获取缩略图的时候是需要开启文件缓存的，而图片播放时是不需要的
	private Boolean mIsOpenFileCache = false;

	private ImageLoader() {
		mTaskList = new ArrayList<BitmapWorkerTask>();
		mAllTaskList = new ArrayList<BitmapWorkerTask>();
		//取10M作为缓存
		mBitmapMemoryCache = new BitmapMemoryCache(10*1024);
		 
		// 获取当前系统的CPU数目
        int cpuNums = Runtime.getRuntime().availableProcessors();
        //根据系统资源情况灵活定义线程池大小 （可固定的线程池，无界队列）
        mExecutorService = Executors.newFixedThreadPool(cpuNums + 1,new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable paramRunnable) {
				Thread thread = new Thread(paramRunnable);  
				//设置创建的线程具有最高优先级
				thread.setPriority(Thread.MAX_PRIORITY); 
			    return thread;
			}
		});
        
        //设置文件缓存，最大缓存为40MB
        mDiskCache = new DiskCache(sContext,40 * 1024 * 1024);
	}

	public static synchronized ImageLoader getInstance(Context context) {
		sContext = context;
		if (sImageLoader == null) {
			Trace.Debug("####getInstance is null to new");
			sImageLoader = new ImageLoader();			
			return sImageLoader;
		} else {
			Trace.Debug("####getInstance not null");
			return sImageLoader;
		}
	}

	public static synchronized void releaseInstance() {
		if (sImageLoader != null) {
			sImageLoader = null;
			System.gc();
		}
	}
	
	public static synchronized void clearCache() {
		if (sImageLoader!=null) {
			
		sImageLoader.clear();
		}
	}
	
	
	public synchronized void lockLoad() {
		sImageLoader.mAllowLoad = false;
	}
	
	public synchronized void unLockLoad() {
		sImageLoader.mAllowLoad = true;
		doTask();
	}
	
	//清空缓存
	private void clear() {
		mBitmapMemoryCache.clearAll();
	}


	public Bitmap loadLocalImage(String path, Point point, int positionId,
					int resId, ImageLoaderListener listener ) {
		String pathExtension = null;
		return loadLocalImage(path, pathExtension,point,positionId,resId,listener);
	}
	
	public Bitmap loadLocalImage(String path, String pathExtension, Point point, int positionId,
			int resId, ImageLoaderListener listener) {
		Bitmap bitmap = null;
		String cachePath = null;
		if(pathExtension == null) {
			cachePath = path;
		} else {
			cachePath = path + pathExtension;
		}
		bitmap = mBitmapMemoryCache.get(cachePath);
		if (bitmap == null) {
			//确保mAllTaskList中只有一个task的id为path，避免添加多个
			//只对浏览加载过滤，播放时不进行过滤
			if(pathExtension == null) {
				for(BitmapWorkerTask tmpTask : mAllTaskList) {
					if(tmpTask != null && tmpTask.getPath().equals(path)) {
						Trace.Debug("####loadLocalImage mAllTaskList has a same task");
						return bitmap;
					}
				}
			}
			
			
			File file=new File(path);
			File[] subFiles = file.listFiles();
			List<File> fileList = new ArrayList<File>();
			if (subFiles!=null&&subFiles.length>0) {
				int count=subFiles.length;
			for (int i = 0; i < count; i++) {
				fileList.add(subFiles[i]);
			}
			}

//			if (Configuration.sortType == SortType.ST_BY_TIME
//							&& Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
//				Trace.Debug("###sortTime");
//				Collections.sort(fileList,
//								new FileComparator.sortListByModifyDate());
//				Collections.reverse(fileList);
//			} else {
//				Trace.Debug("###sortName");
//				Collections.sort(fileList, new FileComparator.sortListByName());
//			}
			
			int count = fileList.size();
			if (Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
				for (int i = 0; i < count; i++) {
					String picpath = fileList.get(i).getAbsolutePath();
					if (Utils.getMmt(picpath)==MultimediaType.MMT_PHOTO) {
						path=picpath;
						break;
					}
		}
		}
			
			
			
			BitmapWorkerTask task = new BitmapWorkerTask(path,cachePath,point, resId,
							listener);
			// 为每个Task设置id
			if (positionId != -1) {
				task.setId(positionId);
				mTaskList.add(task);
				mAllTaskList.add(task);
				Trace.Debug("####loadLocalImage mAllTaskList " + mAllTaskList.size());
			}
			//创建的task基于新的线程池，没有限制，以提高图片加载显示速度
			//task.executeOnExecutor(Executors.newCachedThreadPool());// 用线程池
			//task.executeOnExecutor(mExecutorService);
			//task.execute(path);
			if(mAllowLoad) {
				doTask();
			}
		} else {
			Trace.Debug("####loadLocalImage BitmapMemoryCache get is not null");
		}
		// 返回的bitmap有可能是空的，因为有可能线程还在运行，bitmap还没有加载成功，此时就为null，所以在外处理时需要注意
		return bitmap;
	}
	
	
	public Bitmap getBitmapFromCache(String filePath) {
		Bitmap bitmap = null;
		bitmap = mBitmapMemoryCache.get(filePath);
		if(bitmap == null) {
			bitmap = mDiskCache.getImage(filePath);
		}
		
		return bitmap;		
	}
	
	/**
	 * 设置是否开启文件缓存，true 开启，false 关闭
	 */
	public void setFileCacheMark(Boolean mark) {
		mIsOpenFileCache = mark;
	}
	
	private void doTask() {
		synchronized (mTaskList) {            
            for (BitmapWorkerTask task : mTaskList) {
            	//当getStatus为peding时才去执行，否则不执行（以避免running或finish时去执行报错）
                if (task != null && task.getStatus() == AsyncTask.Status.PENDING) {
                	task.executeOnExecutor(mExecutorService);
                }
            }
            mTaskList.clear();
        }
	}

	public void cancelTaskInList(int positionId) {
		Trace.Debug("####cancelTaskInList mTaskList " + mAllTaskList.size());
		for (BitmapWorkerTask task : mAllTaskList) {
			if (task != null && task.getId() == positionId) {
				task.cancel(true);
				mAllTaskList.remove(task);
				Trace.Debug("####cancelTaskInList task cancel");
				break;
			}
		}
	}
	
	/**
	 * 根据滚动方向来杀死前面四个task，还是后面四个task，以保证正在运行的线程是正在显示的View加载的
	 */
	public void cancelTaskInList(int firstVisibleItemPosition, int lastVisibleItemPosition, int direction) {
		
		List<BitmapWorkerTask> deleteList = new ArrayList<BitmapWorkerTask>();
		
		if(direction == Constant.SCROLL_UP) {
			for(int i = lastVisibleItemPosition + 1; i <= lastVisibleItemPosition + 3; i++) {
				for (BitmapWorkerTask task : mAllTaskList) {
					if (task != null && task.getId() == i) {
						task.cancel(true);
						deleteList.add(task);
						Trace.Debug("####cancelTaskInList task cancel");
						Trace.Debug("####cancelTaskInList mTaskList " + mAllTaskList.size());
					}
				}
			}
			
			mAllTaskList.removeAll(deleteList);
			deleteList.clear();
			
		} else if(direction == Constant.SCROLL_DOWN) {
			
			for(int i = firstVisibleItemPosition - 1; i >= firstVisibleItemPosition - 3; i--) {
				for (BitmapWorkerTask task : mAllTaskList) {
					if (task != null && task.getId() == i) {
						task.cancel(true);
						deleteList.add(task);
						Trace.Debug("####cancelTaskInList task cancel");
						Trace.Debug("cancelTaskInList mTaskList mTaskList " + mAllTaskList.size());
					}
				}
			}

			mAllTaskList.removeAll(deleteList);
			deleteList.clear();
		}
	}
	
	/**
	 * 清除掉所有正在运行的加载线程
	 */
	public void clearAllTaskList() {	
		Trace.Debug("cancelTaskInList mTaskList mTaskList " + mAllTaskList.size());
		for (BitmapWorkerTask task : mAllTaskList) {
				task.cancel(true);
		}
		mAllTaskList.clear();
	}

	public interface ImageLoaderListener {
		public void onImageLoader(String path, Bitmap bitmap);
	}

	private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

		private int taskId = -1;
		private Point mPointInTask = null;
		private String mPathInTask = null;
		private int mResIdInTask = -1;
		private ImageLoaderListener mListenerInTask = null;
		private String mCachePathInTask = null;
		
		public BitmapWorkerTask(String path, String cachePath, Point point, int resId,
				ImageLoaderListener listener ) {
			mPathInTask = path;
			mCachePathInTask = cachePath;
			mPointInTask = point;
			mResIdInTask = resId;
			mListenerInTask = listener;
		}

		public void setId(int id) {
			taskId = id;
		}

		public int getId() {
			return taskId;
		}
		
		public String getPath() {
			return mPathInTask;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			try {
				
				if(mIsOpenFileCache) {
					bitmap = mDiskCache.getImage(mCachePathInTask);
					if(bitmap != null) {
						mBitmapMemoryCache.put(mCachePathInTask, bitmap);
						return bitmap;
					}
				}
				
				bitmap = BitmapUtils.bitmapFromFile(mPathInTask,
								mPointInTask.x, mPointInTask.y);
				
			} catch (OutOfMemoryError e) {
				bitmap = null;// 内存溢出处理,置null显示默认的图片
				//发生内存溢出，让系统gc一下
				System.gc();
				System.runFinalization();
				Trace.Fatal("####doInBackground OutOfMemoryError");
			}

			if (bitmap != null) {
				Trace.Debug("####doInBackground BitmapMemoryCache put");
				//根据路径存入
				mBitmapMemoryCache.put(mCachePathInTask, bitmap);	
				if(mIsOpenFileCache) {
					mDiskCache.putImage(mCachePathInTask, bitmap);
				}
			} else {
				// 设置默认的显示图片,此时bitmap为null，有两种情况：一种oom发生，另一种是图片decode解不出来（即decode为null，文件可能不是图片）
				bitmap = BitmapUtils.bitmapFromResource(
								sContext.getResources(), mResIdInTask,
								mPointInTask.x, mPointInTask.y);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			
			if (mListenerInTask != null) {
				mListenerInTask.onImageLoader(mPathInTask, bitmap);
			}
			//执行完之后也从队列中移除
			mAllTaskList.remove(BitmapWorkerTask.this);
			Trace.Debug("####onPostExecute mAllTaskList size is " + mAllTaskList.size());
		}
	}

}