package com.konka.eplay.modules;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.modules.photo.ImageLoader;
import com.konka.eplay.modules.photo.ImageLoader.ImageLoaderListener;

public class AsyncImageView extends ImageView {

	public static interface OnImageViewLoadListener {
		void onLoadingStarted(AsyncImageView imageView);

		void onLoadingEnded(AsyncImageView imageView, Bitmap image);

		void onLoadingFailed(AsyncImageView imageView, Throwable throwable);
	}

	private String mPath;
	// private ImageRequest mRequest;
	private boolean mPaused;
	private boolean mIsTrue;// 判断当前是否是图片模块，如果是文件夹只显示图片的缩略图，如果不是，显示所有的
	private Bitmap mBitmap;
	//private OnImageViewLoadListener mOnImageViewLoadListener;
	private MultimediaType mMediaType = MultimediaType.MMT_NONE;
	private Bitmap tmpbmp = null;

	public AsyncImageView(Context context) {
		this(context, null);
	}

	public AsyncImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mPaused = false;
	}

	/**
	 * 设置路径，开始异步获取缩略图，兼容音视频缩略图的获取
	 * 
	 * @param path
	 */
	public void setPath(String path, boolean isTrue) {
		// stopLoading();
		mPath = path;
		mIsTrue = isTrue;

		mMediaType = Utils.getMmt(mPath);
		if (new File(mPath).isDirectory()) {
			Trace.Debug("isDirectory" + true);
			mMediaType = MultimediaType.MMT_FOLDER;
			mBitmap = getFolderPic();
		}

		if (!mPaused) {
			reload();
		} else {
			if (mIsTrue) {
				Trace.Debug("###getImageThumbnail");
				mBitmap = getImageThumbnail(mPath, 350, 350);
			} else if (mMediaType == MultimediaType.MMT_MUSIC) {
//				mBitmap = AudioThumbnail.getThumb(mPath);
			} else if (mMediaType == MultimediaType.MMT_MOVIE) {
				mBitmap = getVideoThumbnail(mPath, 350, 350,
								Thumbnails.MICRO_KIND);
			} else if (mMediaType == MultimediaType.MMT_FOLDER) {
				mBitmap = getFolderPic();
			} else {
				mBitmap = ImageCache.getInstance().getBitmap(mPath);
			}
			Trace.Debug("mbitmap" + mBitmap);
			if (mBitmap != null) {
				setImageBitmap(mBitmap);
				return;
			}
			setDefaultImage();
		}
	}

	public void setImageForLabel(String path,int position) {

		Bitmap bitmap = null;

		Point point = new Point(350, 350);
		
		ImageLoader.getInstance(getContext()).setFileCacheMark(true);
		
		bitmap = ImageLoader.getInstance(getContext()).loadLocalImage(path,
						point, position, R.drawable.photo_open_failed,
						new ImageLoaderListener() {

							@Override
							public void onImageLoader(String path, Bitmap bitmap) {
								if(path.equals(AsyncImageView.this.getTag())) {
									AsyncImageView.this.setImageBitmap(bitmap);
									Trace.Debug("####bitmap " + bitmap.getWidth()
													+ "," + bitmap.getHeight());
								}	
							}
						});

		if (bitmap == null) {
			setImageResource(R.drawable.p);
		} else {
			AsyncImageView.this.setImageBitmap(bitmap);
		}
	}

	public void setImageForPic(String path,final int position) {

		Bitmap bitmap = null;

		Point point = new Point(318, 237);
		
		ImageLoader.getInstance(getContext()).setFileCacheMark(true);
		
		bitmap = ImageLoader.getInstance(getContext()).loadLocalImage(path,
						point, position, R.drawable.photo_open_failed,
						new ImageLoaderListener() {

							@Override
							public void onImageLoader(String path, Bitmap bitmap) {
								Trace.Debug(path);
								if(path.equals(AsyncImageView.this.getTag())) {
									AsyncImageView.this.setImageBitmap(bitmap);
									Trace.Debug("####bitmap " + position);
									Trace.Debug("####bitmap " + bitmap.getWidth()
													+ "," + bitmap.getHeight());
								}						
								
							}
						});

		if (bitmap == null) {
			setImageResource(R.drawable.p);
		} else {
//			Trace.Debug("####setImageForPic " + position);
			AsyncImageView.this.setImageBitmap(bitmap);
		}
	
	}

	public void setImageForPicFolder(String path) {

		Bitmap bitmap = null;

		Point point = new Point(318, 237);
		
		ImageLoader.getInstance(getContext()).setFileCacheMark(true);
		
		bitmap = ImageLoader.getInstance(getContext()).loadLocalImage(path,
						point, 0, R.drawable.photo_open_failed,
						new ImageLoaderListener() {

							@Override
							public void onImageLoader(String path, Bitmap bitmap) {
								if(path.equals(AsyncImageView.this.getTag())) {
									AsyncImageView.this.setImageBitmap(bitmap);
									Trace.Debug("####bitmap " + bitmap.getWidth()
													+ "," + bitmap.getHeight());
								}	                                                    
							}
						});

		if (bitmap == null) {
			// bitmap=getFolderPic();
			setImageResource(R.drawable.p);
		} else {
			AsyncImageView.this.setImageBitmap(bitmap);
		}
	}

	public Bitmap getFolderPic() {
		Trace.Debug("getFolderPic");
		Bitmap bmp = null;
//		File file = new File(mPath);
//		File[] subFiles = file.listFiles();
//		List<File> fileList = new ArrayList<File>();
//		if (subFiles!=null&&subFiles.length>0) {
//			int count=subFiles.length;
//		for (int i = 0; i < count; i++) {
//			fileList.add(subFiles[i]);
//		}
//		}
//
//		if (Configuration.sortType == SortType.ST_BY_TIME
//						&& Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
//			Collections.sort(fileList,
//							new FileComparator.sortListByModifyDate());
//			Collections.reverse(fileList);
//		} else {
//			Collections.sort(fileList, new FileComparator.sortListByName());
//		}
//		
//		int count = fileList.size();
//		List<Bitmap> list = new ArrayList<Bitmap>();
		if (Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
//			for (int i = 0; i < count; i++) {
//				String picpath = fileList.get(i).getAbsolutePath();
//				if (Utils.getMmt(picpath) != MultimediaType.MMT_PHOTO) {
//					continue;
//				}
		
				Point point = new Point(318, 237);
//				
				ImageLoader.getInstance(getContext()).setFileCacheMark(true);
//				
				bmp = ImageLoader.getInstance(getContext()).loadLocalImage(
								mPath, point, 0, R.drawable.photo_open_failed,
								new ImageLoaderListener() {

									@Override
									public void onImageLoader(String path,
													Bitmap bitmap) {
										Trace.Debug(path);
										 AsyncImageView.this.setImageBitmap(bitmap);
										Trace.Debug("####bitmap "
														+ bitmap.getWidth()
														+ ","
														+ bitmap.getHeight());
									}
								});

				 if(bmp == null) {
					 setImageResource(R.drawable.p);
//					 break;
				 } else {
					setImageBitmap(bmp);
//					break;
		} 
//				 else if (Configuration.curMediaType == MultimediaType.MMT_MOVIE) {
//			for (int i = 0; i < count; i++) {
//				String picpath = fileList.get(i).getAbsolutePath();
//
//				if (Utils.getMmt(picpath) != MultimediaType.MMT_MOVIE) {
//					continue;
//				}
//				bmp = getVideoThumbnail(picpath, 350, 350, 96 * 96);
//				if (bmp != null) {
//					list.add(bmp);
//					i = count;
//				}
//				if (list.size() == 1) {
//					break;
//				}
//			}
//			if (list.size() > 0) {
//
//				tmpbmp = list.get(0);
//			} else {
//
//				BitmapDrawable bitmap = (BitmapDrawable) getContext()
//								.getResources().getDrawable(R.drawable.v);
//				bmp = bitmap.getBitmap();
//				tmpbmp = bmp;
			}
//	 else if (Configuration.curMediaType == MultimediaType.MMT_MUSIC) {
//			for (int i = 0; i < count; i++) {
//				String picpath = fileList.get(i).getAbsolutePath();
//
//				if (Utils.getMmt(picpath) != MultimediaType.MMT_MUSIC) {
//					continue;
//				}
//				bmp=AudioThumbnail.getThumb(picpath);
//				if (bmp == null) {
//					
//					BitmapDrawable bitmap = (BitmapDrawable) getContext()
//									.getResources().getDrawable(R.drawable.m);
//					bmp = bitmap.getBitmap();
//					tmpbmp=bmp;
//					setImageBitmap(bmp);
//					
//				}
//				list.add(bmp);
//				i = subFiles.length;
//				setImageBitmap(bmp);
//			}
//		}

		return tmpbmp;
	}

	/**
	 * 停止解码显示图片
	 * 
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		if (mPaused != paused) {
			mPaused = paused;
			if (!paused) {
				reload();
			}
		}
	}

	private void reload() {
		Trace.Debug("reload");
		if (mPath != null) {
			if (mIsTrue) {
				mBitmap = getImageThumbnail(mPath, 350, 350);
			} else if (mMediaType == MultimediaType.MMT_MUSIC) {
				mBitmap = AudioThumbnail.getThumb(mPath);
			} else if (mMediaType == MultimediaType.MMT_MOVIE) {
				mBitmap = getVideoThumbnail(mPath, 350, 350, 96 * 96);
			} else if (mMediaType == MultimediaType.MMT_FOLDER) {
				mBitmap = getFolderPic();
			} else {
				mBitmap = ImageCache.getInstance().getBitmap(mPath);
			}

			if (mBitmap != null) {
				setImageBitmap(mBitmap);
				return;
			}
			setDefaultImage();
		}
	}

	private void setDefaultImage() {
		if (mBitmap == null) {
			switch (mMediaType) {
			case MMT_PHOTO:
				setImageResource(R.drawable.p);
				break;
			case MMT_MOVIE:
				// 默认图不用添加
				setImageResource(R.drawable.v);
				break;
			case MMT_MUSIC:
				// 默认图不用添加
				setImageResource(R.drawable.m);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	private Bitmap getImageThumbnail(String imagePath, int width, int height) {

		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / 212;
		int beHeight = h / 158;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, 212, 158,
						ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
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
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
					int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
						ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

}
