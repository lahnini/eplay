package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.spec.MGF1ParameterSpec;

import com.konka.eplay.R;
import com.konka.eplay.Utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 为图片浏览而处理左右、上下遥控器按键的ViewPager</br> <b>创建时间：</b>2015-3-17
 * 
 * @author mcsheng
 */
public class KeyViewPager extends ViewPager {

	/**显示图片的张数*/
	public static int sSize = -1;
	
	private int mScrollDuration = 500;
	
	private Boolean mIsCanScroll = true;
	
	private OnPageChangeListener mOutOnPageChangeListener = null;
	
	private Object lock = new Object();
	private Boolean mIsScrolling = false;
	
	public KeyViewPager(Context context) {
		super(context);
		init();
	}

	public KeyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	@Override
	public void scrollTo(int x, int y) {
		//重载，根据标志位来实现是否可以滑动
		if (mIsCanScroll) {  
            super.scrollTo(x, y);  
        }  
	}
	
	public void setCanScroll(Boolean mark) {
		mIsCanScroll = mark;
	}
	
	public Boolean isCanScroll() {
		return mIsCanScroll;
	}
	
	private void setScrolling(boolean mark) {
		synchronized (this.lock) {
			this.mIsScrolling = mark;
		}
	}

	private boolean isScrolling() {
		synchronized (this.lock) {
			return this.mIsScrolling;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		//正在滑动时自行处理掉所有按键,避免连续滑动操作过快，出现黑屏现象。
		if (isScrolling()) {
			return true;
		} 
		
		//当折叠动画还在执行时屏蔽所有按键
//		FoldingAndOpenLayout foldingLayout = (FoldingAndOpenLayout) this.findViewById(
//				this.getCurrentItem()).getParent().getParent();		
//		if (foldingLayout.isFoldAnimationRunning()) {
//			return true;
//		}
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_RIGHT: {
			setCanScroll(true);
			// 到最后一张，此时再按右键的处理
			if (this.getCurrentItem() + 1 == sSize) {
				QuickToast.showToast(getContext(), getContext().getString(R.string.last_picture));
				return false;
			}
			// 通过给每个子View设置一个id来查找对应的子View，并根据其加载的Tag来判断是否加载完成
			if (((Boolean)true).equals(this.findViewById(this.getCurrentItem() + 1)
					.getTag(R.id.tag_load))) {
				revertRotate();
				return false;
			} else {
				//QuickToast.showToast(getContext(), getContext().getString(R.string.wait_picture_load));
				//return true; 	
				//未加载完成时，加载默认图，提示正在加载
				revertRotate();
				View viewGroup = (View) (this.findViewById(this.getCurrentItem() + 1)).getParent();
				AnimationDrawable anim = (AnimationDrawable) viewGroup.findViewById(R.id.loading_imageview).getBackground();
				if(anim != null) {
					anim.start();
				}
				viewGroup.findViewById(R.id.progressBar_viewpager).setVisibility(View.VISIBLE);
				return false;
			}
		}
		case KeyEvent.KEYCODE_DPAD_LEFT: {
			setCanScroll(true);
			// 到第一张，此时再按左键的处理
			if (this.getCurrentItem() - 1 == -1) {
				QuickToast.showToast(getContext(), getContext().getString(R.string.first_picture));
				return false;
			}

			if (((Boolean)true).equals(this.findViewById(this.getCurrentItem() - 1)
					.getTag(R.id.tag_load))) {
				revertRotate();
				return false;//当为true时返回false进行切换
			} else {
				revertRotate();
				View viewGroup = (View) (this.findViewById(this.getCurrentItem() - 1)).getParent();
				AnimationDrawable anim = (AnimationDrawable) viewGroup.findViewById(R.id.loading_imageview).getBackground();
				if(anim != null) {
					anim.start();
				}
				viewGroup.findViewById(R.id.progressBar_viewpager).setVisibility(View.VISIBLE);
				return false;
			}
		}
		case KeyEvent.KEYCODE_DPAD_UP: {
			if (((Boolean)true).equals(this.findViewById(this.getCurrentItem())
					.getTag(R.id.tag_load))) {
				// 顺时针旋转
				this.rotate(true);
			}
			return true;
		}
		case KeyEvent.KEYCODE_DPAD_DOWN: {
			if (((Boolean)true).equals(this.findViewById(this.getCurrentItem())
					.getTag(R.id.tag_load))) {
				// 逆时针旋转
				this.rotate(false);
			}
			return true;
		}
		case KeyEvent.KEYCODE_MENU: {
			// 当菜单栏获取到焦点时，ViewPager中的onKeyDown将不会响应，因为焦点已在菜单栏上
			FrameLayout frameLayout = (FrameLayout) this.getParent();
			if (frameLayout.getChildAt(1).getVisibility() == View.VISIBLE) {
				frameLayout.getChildAt(1).setVisibility(View.GONE);
				frameLayout.findViewById(R.id.left_arrow_viewpager).setVisibility(View.GONE);
				frameLayout.findViewById(R.id.right_arrow_viewpager).setVisibility(View.GONE);
				return true;

			} else {
				RelativeLayout relativeLayout = (RelativeLayout) frameLayout
						.getChildAt(1);
				relativeLayout.setVisibility(View.VISIBLE);
				LinearLayout linearLayout = (LinearLayout) relativeLayout
						.getChildAt(2);
				linearLayout.setVisibility(View.VISIBLE);
				linearLayout.getChildAt(0).requestFocus();
				((ImageViewPagerActivity)getContext()).enterAnimation();
				//重置时间， 用于没有任何操作时菜单栏自动5秒后消失
				((ImageViewPagerActivity)getContext()).resetTime();
				return true;
			}
		}
		case KeyEvent.KEYCODE_ENTER: {
			View view = this.findViewById(this.getCurrentItem());
			String name = (String) view.getTag(R.id.tag_file_name);
			QuickToast.showToastForName(getContext(), name);
			return false;
		}
		default:
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 旋转操作方法
	 * 
	 * @param mark
	 *            true 顺时针旋转 false 逆时针旋转
	 */
	public void rotate(Boolean mark) {
		ImageView imageView = (ImageView) this
				.findViewById(this.getCurrentItem());
		if(!imageView.getTag(R.id.tag_type).equals(PictureInfo.GIF)) {
			((KeyScaleImageView)imageView).rotate(mark, (Bitmap) imageView.getTag(R.id.tag_bitmap));
		} else {
			((GifView)imageView).rotate(mark);
			//QuickToast.showToast(getContext(), getContext().getString(R.string.gif_no_support));
		}
		
	}

	/**
	 * 获取当前的ImageView中的Bitmap，ViewPag自带的getCurrentView方法获取到的View是重复利用的View，
	 * 不能获取正确的
	 */
	public Bitmap getBitmapInCurrentView() {
		// id是在适配器中设置的
		ImageView imageView = (ImageView) this.findViewById(this
				.getCurrentItem());
		return (Bitmap) imageView.getTag(R.id.tag_bitmap);
	}

	
	/**
	 * 设置壁纸
	 */
	public Boolean setWallPager() {	
		
		ImageView imageView = (ImageView) KeyViewPager.this
				.findViewById(KeyViewPager.this.getCurrentItem());
		WallpaperManager wallpaperManager = WallpaperManager
				.getInstance(getContext());
		
		Trace.Debug("####setWallPager DesiredMinimumWidth,DesiredMinimumHeight is " + 
				wallpaperManager.getDesiredMinimumWidth() + "," + wallpaperManager.getDesiredMinimumHeight());
		try {
			
			Bitmap bitmap = null;
			
			if(!imageView.getTag(R.id.tag_type).equals(PictureInfo.GIF)) {
				bitmap = (Bitmap) imageView
						.getTag(R.id.tag_bitmap);
			} else {
				//gif处理
				GifView gifView = (GifView) imageView;
				ImageViewPagerAdapter adapter = (ImageViewPagerAdapter) KeyViewPager.this.getAdapter();
				String path = adapter.getPathList().get(gifView.getId());
				PictureInfo pictureInfo = new PictureInfo(getContext(), path);
				Trace.Debug("####setWallPager gifView width,height " + gifView.getWidth() + "," + gifView.getHeight());
				bitmap = pictureInfo.getThumbnail(gifView.getWidth(), gifView.getHeight());				
			}
			
			Matrix localMatrix = new Matrix();
			int screenWidth = Utils.getScreenW(getContext());
			int screenHeight = Utils.getScreenH(getContext());
			float scaleH = -1, scaleW = -1;
			float scale = -1;
			//当Bitmap有宽或高大于屏幕时
			if(bitmap.getWidth() > screenWidth || bitmap.getHeight() > screenHeight) {
				scaleW = Utils.getScreenW(getContext()) / (bitmap.getWidth() * 1.0f);
				scaleH = Utils.getScreenH(getContext()) / (bitmap.getHeight() * 1.0f);
				scale = (scaleW > scaleH) ? scaleH : scaleW;
				localMatrix.postScale(scale, scale);
				Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), 
						bitmap.getHeight(), localMatrix, true);
				Trace.Debug("####setWallPager tmpBitmap is " + tmpBitmap.getWidth() + ","
						+ tmpBitmap.getHeight());
				//经过处理后的Bitmap的宽与高若等于屏幕的宽与高，则还需要设置成壁纸，否则还要进行处理
				if(tmpBitmap.getWidth() == screenWidth && tmpBitmap.getHeight() == screenHeight) {
					wallpaperManager.setBitmap(tmpBitmap);
				} else {
					dealBitmapForWallPaper(tmpBitmap, wallpaperManager);
				}
				
			} else {
				//Bitmap的宽与高小于屏幕的处理
				dealBitmapForWallPaper(bitmap, wallpaperManager);
			}
													
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}	

		return true;
	}
	
	private void dealBitmapForWallPaper(Bitmap bitmap, WallpaperManager wallpaperManager) throws IOException {
		
		int screenWidth = Utils.getScreenW(getContext());
		int screenHeight = Utils.getScreenH(getContext());
		
		int[] piexls = new int[screenHeight * screenWidth];
		int[] piexlBitmap = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(piexlBitmap, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
				bitmap.getHeight());
		
		int widthDifer = (int) ((screenWidth - bitmap.getWidth()) / 2.0f);
		int heightDifer = (int) ((screenHeight - bitmap.getHeight()) / 2.0f);
		
		for(int y = 0; y < screenHeight; y++) {
			for(int x = 0; x < screenWidth; x++) {
				int index = y * screenWidth + x;
				if(y >= heightDifer && y < heightDifer + bitmap.getHeight() && x >= widthDifer 
						&& x < widthDifer + bitmap.getWidth()) {
					int indexDifer = (y - heightDifer) * bitmap.getWidth() + (x - widthDifer);
					piexls[index] = piexlBitmap[indexDifer];
				} else {
					//其他部分设置成黑色
					piexls[index] = 0xff000000;
				}
			}
		}
		Bitmap bitmapScreen = Bitmap.createBitmap(piexls, screenWidth, screenHeight, Config.ARGB_8888);
		wallpaperManager.setBitmap(bitmapScreen);
	}
	
	private void init() {
		setScroll();
		//先内部处理滑动监听，然后再引出
		super.setOnPageChangeListener(mOnPageChangeListener);
	}
	
	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int paramInt) {
			if(mOutOnPageChangeListener != null) {
				mOutOnPageChangeListener.onPageSelected(paramInt);
			}
		}
		
		@Override
		public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
			if(mOutOnPageChangeListener != null) {
				mOutOnPageChangeListener.onPageScrolled(paramInt1, paramFloat, paramInt2);
			}
		}
		
		@Override
		public void onPageScrollStateChanged(int paramInt) {
			
			switch(paramInt) {
			//滑动结束
			case ViewPager.SCROLL_STATE_IDLE:
				//设置滑动停止
				KeyViewPager.this.setScrolling(false);
				break;
			//用手指拖拉
			case ViewPager.SCROLL_STATE_DRAGGING:
				break;
			//正在滑动
			case ViewPager.SCROLL_STATE_SETTLING:
				//设置正在滑动
				KeyViewPager.this.setScrolling(true);
				break;
			default:
				break;
			}
			
			if(mOutOnPageChangeListener != null) {
				mOutOnPageChangeListener.onPageScrollStateChanged(paramInt);
			}
		}
	};

	// 设置ViewPager滚动速度
	private void setScroll() {
		ViewPagerScroller viewPagerScroller = null;
		// 利用java的发射机制来设置ViewPager中的mScroller从而定制ViewPager的滚动速度
		try {
			// 使用KeyViewPager，需要获取到父类来映射才行
			Field field = this.getClass().getSuperclass()
					.getDeclaredField("mScroller");
			field.setAccessible(true);
			viewPagerScroller = new ViewPagerScroller(getContext(),
					new AccelerateInterpolator());
			viewPagerScroller.setScrollDuration(mScrollDuration);
			field.set(this, viewPagerScroller);
		} catch (NoSuchFieldException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		}
	}
	
	//旋转操作恢复
	public void revertRotate() {
		//进行复位操作，避免旋转后进行切换
		ImageView imageView = (ImageView) this
				.findViewById(this.getCurrentItem());
		if(!imageView.getTag(R.id.tag_type).equals(PictureInfo.GIF)) {
			Bitmap bitmap = (Bitmap) imageView.getTag(R.id.tag_bitmap);
			imageView.setImageBitmap(bitmap);
		} else {
			GifView gifView = (GifView) imageView;
			gifView.revertRotate();
		}
	}

	/**
	 * 设置ViewPager滚动时间
	 */
	public void setScrollDuration(int duration) {
		mScrollDuration = duration;
		setScroll();
	}
	
	interface PictureLoadEndListener {
		public void loadEnd();

	}
	
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mOutOnPageChangeListener = listener;
	}
	
	
	
}
