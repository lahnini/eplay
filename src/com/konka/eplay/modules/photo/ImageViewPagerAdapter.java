package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.util.List;

import com.konka.android.tv.KKCommonManager;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.photo.ImageLoader.ImageLoaderListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageViewPagerAdapter extends PagerAdapter {

	private List<String> mPathList;
	private Context mContext;
	private int mScreenWidth, mScreenHeight;
	private static final String PICTURE_PLAY = "play";
	//是否开启循环播放
	private Boolean mIsLoopPlay = false;
	
	public ImageViewPagerAdapter(Context context, List<String> list) {
		mContext = context;
		KeyViewPager.sSize = list.size();
		mPathList = list;
		
		Boolean sIs4K2KScreen = KKCommonManager.getInstance(context).isSupport4K2K();
		if(sIs4K2KScreen) {
			Trace.Debug("####ImageViewPagerAdapter is 4K Screen");
			mScreenWidth = ImageViewPagerActivity.SCREEN_4K_WIDTH;
			mScreenHeight = ImageViewPagerActivity.SCREEN_4K_HEIGHT;
		} else {
			Trace.Debug("####ImageViewPagerAdapter is not 4K Screen");
			mScreenWidth = Utils.getScreenW(mContext);
			mScreenHeight = Utils.getScreenH(mContext);
		}		
	}

	@Override
	public int getCount() {
		Trace.Debug("####getCount");
		//通过Integer.MAX_VALUE（足够大，播放张数不会超出这个值）和判断取余来实现循环播放，并不是无限循环播放
		if (mIsLoopPlay) {
			int count = mPathList.size();
			return count == 0 ? 0 : Integer.MAX_VALUE;
		} else {
			return mPathList.size();
		}
		
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {

		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		//循环播放的处理
		if (mIsLoopPlay) {
			//取余实现取真正的position
			position = position % mPathList.size();
		}
		
		Trace.Debug("####destroyItem " + position);
		//杀死未加载的线程，避免oom
		ImageLoader.getInstance(mContext).cancelTaskInList(position);
		container.removeView((View)object);
	}

	// 刚才加载时会预先加载前面2张，然后每次前进就会预先加载前一张，然后再释放（调用destrItem)最前面的一张（比如到第二张时就释放第一张，预先加载第三张）
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		//循环播放的处理
		if (mIsLoopPlay) {
			//取余实现取真正的position
			position = position % mPathList.size();
		}
		
		final int realPosition = position;
		
		Trace.Debug("####instantiateItem " + realPosition);
		Trace.Debug("####instantiateItem " + mPathList.get(realPosition));

		View view = View.inflate(mContext, R.layout.imageitem_viewpager, null);
		
		PictureInfo pictureInfo = new PictureInfo(mContext,
				mPathList.get(realPosition));	
		
		
		//加上空类型（即此文件可能不是图片类型）处理，避免程序崩溃
		if (pictureInfo.getType() == null) {
			KeyScaleImageView imageView = (KeyScaleImageView) view.findViewById(R.id.imageview_viewpager);
			//设置可见
			imageView.setVisibility(View.VISIBLE);
			imageView.setTag(R.id.tag_load, true);
			imageView.setId(realPosition);
			//imageView.setTag(R.id.tag_file_name, "photo_open_failed");
			imageView.setTag(R.id.tag_file_name, pictureInfo.getName());
			imageView.setTag(R.id.tag_type, "未知");
			
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.photo_open_failed);
			imageView.setTag(R.id.tag_bitmap, bitmap);
			
			imageView.setImageBitmap(bitmap);
			
			AnimationDrawable anim = (AnimationDrawable) view.findViewById(R.id.loading_imageview).getBackground();
			if(anim != null && anim.isRunning()) {
				anim.stop();
			}
			view.findViewById(R.id.progressBar_viewpager).setVisibility(View.GONE);
//			QuickToast.showToast(mContext,mContext.getString(R.string.picture_no_support));			
			container.addView(view);	
			
			if (mContext instanceof ImageViewPagerActivity) {
				((ImageViewPagerActivity)mContext).cancelProgressDialog();
			}
			
			return view;
		}	
		
		if(!PictureInfo.GIF.equals(pictureInfo.getType())) {
			Trace.Debug("####instantiateItem is not GIF");
			final KeyScaleImageView imageView = (KeyScaleImageView) view.findViewById(R.id.imageview_viewpager);
			//设置可见
			imageView.setVisibility(View.VISIBLE);
			imageView.setTag(R.id.tag_load, false);
			imageView.setId(realPosition);
			imageView.setTag(R.id.tag_file_name, pictureInfo.getName());
			imageView.setTag(R.id.tag_type, pictureInfo.getType());
			
			Point point = new Point(mScreenWidth,mScreenHeight);
//			Point point = new Point(3840,2160);
			Bitmap bitmap = null;
			String path = mPathList.get(realPosition);
			String cachePath = path + PICTURE_PLAY;
			
			ImageLoader.getInstance(mContext).setFileCacheMark(false);
			
			bitmap = ImageLoader.getInstance(mContext).loadLocalImage(
						path, cachePath, point, realPosition,
						R.drawable.photo_open_failed, new ImageLoaderListener() {

						@Override
						public void onImageLoader(String path, Bitmap bitmap) {
							setBitmap(bitmap, imageView, realPosition);					
						}
					});
			
			//缓存中已有
			if(bitmap != null) {
				setBitmap(bitmap, imageView, realPosition);
			}
		} else {
			GifView gifView = (GifView) view.findViewById(R.id.gifview_viewpager);
			gifView.setVisibility(View.VISIBLE);
			gifView.setMovieFile(mPathList.get(realPosition));
			gifView.setId(realPosition);
			gifView.setTag(R.id.tag_load, true);
			gifView.setTag(R.id.tag_file_name, pictureInfo.getName());
			gifView.setTag(R.id.tag_type, pictureInfo.getType());
			//加载成功后设置环形加载框不可见	
			AnimationDrawable anim = (AnimationDrawable) view.findViewById(R.id.loading_imageview).getBackground();
			if(anim != null && anim.isRunning()) {
				anim.stop();
			}
			view.findViewById(R.id.progressBar_viewpager).setVisibility(View.GONE);
			cancelProgressDialog(realPosition);
		}
		
		
		container.addView(view);	
		
		return view;
	}
	
	//重载这个方法，通过adapter.notifyDataSetChanged();可以刷新
	@Override  
	public int getItemPosition(Object object) {  
		Trace.Debug("####getItemPosition");
		//刷新个数
		KeyViewPager.sSize = mPathList.size();
	    return POSITION_NONE;  
	}  
	
	public List<String> getPathList() {
		return mPathList;
	}
	
	/**
	 * 是否开启循环播放
	 */
	public void setLoopPlay(Boolean loopPlay) {
		mIsLoopPlay = loopPlay;
	}
	
	private void cancelProgressDialog(int position) {
		String str = "com.konka.eplay.modules.photo.ImageViewPagerActivity";
		//判断后才进行取消  ImageViewPagerActivity调用才执行
		if(mContext.getClass().getName().equals(str) && 
				((ImageViewPagerActivity)mContext).getIndex() == position) {
			//取消加载等待框
			((ImageViewPagerActivity)mContext).cancelProgressDialog();
		}
	}
	
	private void setBitmap(Bitmap bitmap, ImageView imageView ,int position) {
		imageView.setImageBitmap(bitmap);
		Trace.Debug("####bitmap " + bitmap.getWidth() + "," + bitmap.getHeight());
		//当高度大于限制值时关闭ImageView的硬件加速，以使可以显示
		if(bitmap.getWidth() > KeyScaleImageView.LIMIT_LENGTH || bitmap.getHeight() > 
				KeyScaleImageView.LIMIT_LENGTH) {
			imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		imageView.setTag(R.id.tag_load, true);
		imageView.setTag(R.id.tag_bitmap, bitmap);
		
		//加载成功后设置环形加载框不可见
		View viewGroup = (View) imageView.getParent();
		AnimationDrawable anim = (AnimationDrawable) viewGroup.findViewById(R.id.loading_imageview).getBackground();
		if(anim != null && anim.isRunning()) {
			anim.stop();
		}
		viewGroup.findViewById(R.id.progressBar_viewpager).setVisibility(View.GONE);
		
		cancelProgressDialog(position);
	}

}