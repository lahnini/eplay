package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import com.konka.eplay.R;
import com.konka.eplay.Utils;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * Created on: 2015-3-30
 * 
 * @brief 图片缩放Activity
 * @author mcsheng
 * @date Latest modified on: 2015-4-14
 * @version V1.0.00
 * 
 */
public class ImageScaleActivity extends Activity {
	
	//中间显示的缩放ImageView
	private KeyScaleImageView mKeyScaleImageView = null;
	public static final String TYPE = "scale_activity";
	private LinearLayout mScaleLinearLayout;
	
	private GuideBorderView mGuideBorderView;
	private TextView mScaleTextView;
	private TextView mScaleSmallTextView;
	private FrameLayout mScaleFrameLayoutInFragment;
	//右下角显示的ImageView
	private ImageView mReferImageView;
	//右下角显示的ImageView上面的黑色蒙罩
	private ImageView mMaskImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}
	
	//先Activity的onWindowFocusChanged方法执行，然后再到各个View的onWindowFocusChanged方法执行
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Trace.Debug("onWindowFocusChanged");
		if(hasFocus) {			
			showScaleLinearLayout();
			setScaleTextAndReferImage();			
		}		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageViewPagerActivity.sCurrentBitmap = null;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if(mGuideBorderView.getVisibility() == View.VISIBLE) {
					Trace.Debug("left");
					mGuideBorderView.move(GuideBorderView.MOVE_LEFT);
					return true;
				} else {
					return false;
				}
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if(mGuideBorderView.getVisibility() == View.VISIBLE) {
					Trace.Debug("right");
					mGuideBorderView.move(GuideBorderView.MOVE_RIGHT);
					return true;
				} else {
					return false;
				}				
			case KeyEvent.KEYCODE_DPAD_UP:
				if(mGuideBorderView.getVisibility() == View.VISIBLE) {
					Trace.Debug("up");
					mGuideBorderView.move(GuideBorderView.MOVE_UP);
					return true;
				} else {
					return false;
				}	
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if(mGuideBorderView.getVisibility() == View.VISIBLE) {
					Trace.Debug("down");
					mGuideBorderView.move(GuideBorderView.MOVE_DOWN);
					return true;
				} else {
					return false;
				}
			case KeyEvent.KEYCODE_ENTER:
				Trace.Debug("ok");
				mKeyScaleImageView.zoom();
				
				//判断是否显示缩放LinearLayout
				showScaleLinearLayout();
				
				mGuideBorderView.zoom();
								
				int scale = (int) getScale();
				mScaleTextView.setText(scale + "");		
				return true;
			default:
				break;					
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_imagescale);
		
		mScaleLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_scale_activity);
		mKeyScaleImageView = (KeyScaleImageView) findViewById(R.id.imageView_scale_activity);
		if(KeyScaleImageView.sHasRotate) {
			if(KeyScaleImageView.sRotateBitmap == null) {
				Trace.Fatal("####initView sRotateBitmap is null");
			}
			//当高度大于限制值时关闭ImageView的硬件加速，以使可以显示
			if(KeyScaleImageView.sRotateBitmap.getWidth() > KeyScaleImageView.LIMIT_LENGTH || 
					KeyScaleImageView.sRotateBitmap.getHeight() > KeyScaleImageView.LIMIT_LENGTH) {
				mKeyScaleImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
			mKeyScaleImageView.setImageBitmap(KeyScaleImageView.sRotateBitmap);
		} else {
			if(ImageViewPagerActivity.sCurrentBitmap == null) {
				Trace.Fatal("####initView sCurrentBitmap is null");
			}
			//当高度大于限制值时关闭ImageView的硬件加速，以使可以显示
			if(ImageViewPagerActivity.sCurrentBitmap.getWidth() > KeyScaleImageView.LIMIT_LENGTH || 
					ImageViewPagerActivity.sCurrentBitmap.getHeight() > KeyScaleImageView.LIMIT_LENGTH) {
				mKeyScaleImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
			mKeyScaleImageView.setImageBitmap(ImageViewPagerActivity.sCurrentBitmap);
		}
		
		mGuideBorderView = (GuideBorderView) findViewById(R.id.guide_border_view);
		mScaleTextView = (TextView) findViewById(R.id.guide_textView);
		mScaleSmallTextView = (TextView) findViewById(R.id.guide_small_textView);
	
		mReferImageView = (ImageView) findViewById(R.id.image_guide_view);
		mScaleFrameLayoutInFragment = (FrameLayout) findViewById(R.id.scale_framelayout);
		mMaskImageView = (ImageView) findViewById(R.id.scale_mask_imageview);
		//设置BorderView
		mKeyScaleImageView.setGuideBorderView(mGuideBorderView);
		QuickToast.showToast(this, this.getString(R.string.ok_can_scale));
	}
	
	public float getScale() {
		return mKeyScaleImageView.getScale();
	}
	
	public KeyScaleImageView getKeyScaleImageView() {
		return mKeyScaleImageView;
	}
	
	/**
	 * 根据显示图片的大小来判断，是否显示缩放LinearLayout
	 */
	public void showScaleLinearLayout() {
		
		int screenWidth = Utils.getScreenW(this);
		int screenHeight = Utils.getScreenH(this);
		Point point = null;
		Trace.Debug("####showScaleLinearLayout " + screenWidth + "," + screenHeight);
		//当ImageView的ScaleType类型不是Matrix时，不能用getActualImageMeasure()获取宽与高
		if(mKeyScaleImageView.getScaleType() == ScaleType.CENTER) {
			point = new Point();
			point.x = mKeyScaleImageView.getImageWidth();
			point.y = mKeyScaleImageView.getImageHeight();
		} else if(mKeyScaleImageView.getScaleType() == ScaleType.MATRIX) {
			point = mKeyScaleImageView.getActualImageMeasure();
		}
		
		Trace.Debug("####showScaleLinearLayout point " + point.x + "," + point.y);
		
		if(point.x > screenWidth || point.y > screenHeight) {
			mScaleLinearLayout.setVisibility(View.VISIBLE);
			mGuideBorderView.setVisibility(View.VISIBLE);
		} else {
			//设置View的属性为INVISIBLE，才会在开始时设置LayoutParam成功，若在GONE的状态下
			//设置layout定位就会出现不能定位的情况
			mScaleLinearLayout.setVisibility(View.INVISIBLE);
			mGuideBorderView.setVisibility(View.INVISIBLE);
		}
		
		
	}
	
	private Point getLocation(View view) {
		int[] location2 = new int[2];
		view.getLocationInWindow(location2);
		Point point = new Point();
		point.x = location2[0];
		point.y = location2[1];
		return point;
	}
	
	private void setLocation(View view , int left ,int top) {
		FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
		lp.setMargins(left, top, 0, 0);
		view.setLayoutParams(lp);
	}
	
	//设置缩放Text和蒙罩的位置显示
	private void setScaleTextAndReferImage() {
		Point point = mGuideBorderView.getActualImageMeasure();
		Point mReferPoint = getLocation(mReferImageView);
		Point mScaleFramePoint = getLocation(mScaleFrameLayoutInFragment);	

		int guideLeft = mReferPoint.x + 
				(mReferImageView.getWidth() - point.x);
		int guideTop = mReferPoint.y + 
				(mReferImageView.getHeight() - point.y);
		//bitmap相对于父View的相对坐标
		int centerX = (guideLeft - mScaleFramePoint.x) + point.x / 2;
		int centerY = (guideTop - mScaleFramePoint.y) + point.y / 2;
		
		Trace.Debug("####onWindowFocusChanged centerX,centerY " + centerX + "," + centerY);
		Trace.Debug("####onWindowFocusChanged mReferImageView " + mReferImageView.getWidth()+ 
				"," + mReferImageView.getHeight());
		
		int x = point.x / 2 - Utils.dip2px(this, 30);
		int y = point.y / 2 - Utils.dip2px(this, 40);
		int left = centerX + x;
		int top = centerY + y;
		
		int left_small = left - Utils.dip2px(this, 10);
		int top_small = top + Utils.dip2px(this, 10);
		//对应显示倍数的TextView进行定位
		setLocation(mScaleSmallTextView, left_small, top_small);
		setLocation(mScaleTextView, left, top);
		
		//对蒙罩进行定位
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mMaskImageView.getLayoutParams();
		//罩的宽度与高度都分别加一个像素，使蒙罩能完全罩住Bitmap，避免计算时所带来的误差
		lp.width = point.x + 1;
		lp.height = point.y + 1;
		lp.setMargins((int)(centerX - point.x / 2.0f), (int)(centerY - point.y / 2.0f), 
				(int)(centerX + point.x / 2.0f), (int)(centerY + point.y / 2.0f));
		
		mMaskImageView.setLayoutParams(lp);
	}
	
	//以监听触摸来实现监听鼠标操作
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		Trace.Debug("####dispatchTouchEvent");
//		int what = ev.getAction();
//		
//		Trace.Debug("####dispatchTouchEvent what is " + what);
//		switch(what) {
//		case MotionEvent.ACTION_DOWN:
//			break;
//		case MotionEvent.ACTION_UP:
//			mIsMouseDownInBorder = false;
//			mClickCount++;
//			if(mClickCount == 1) {
//				mFirstClickTime = System.currentTimeMillis();
//			} else if(mClickCount == 2) {
//				mSecondClickTime = System.currentTimeMillis();
//				Trace.Debug("####dispatchTouchEvent double click first time is " + mFirstClickTime);
//				Trace.Debug("####dispatchTouchEvent double click second time is " + mSecondClickTime);
//				Trace.Debug("####dispatchTouchEvent double click time is " + (mSecondClickTime - mFirstClickTime));
//				if(mSecondClickTime - mFirstClickTime < DOUBLE_CLICK_TIME) {
//					//鼠标左键双击实现缩放
//					Trace.Info("####dispatchTouchEvent Mouse double click");
//					mKeyScaleImageView.zoom();					
//					//判断是否显示缩放LinearLayout
//					showScaleLinearLayout();					
//					mGuideBorderView.zoom();									
//					int scale = (int) getScale();
//					mScaleTextView.setText(scale + "");		
//				}
//				//清零
//				mClickCount = 0;
//				mFirstClickTime = 0;
//				mSecondClickTime = 0;
//			}
//			break;
//		case MotionEvent.ACTION_MOVE:
//			break;
//		default:
//			break;
//		}
//		return super.dispatchTouchEvent(ev);
//	}	
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {		
		Trace.Debug("####onGenericMotionEvent"); 
		
		//输入源为可显示的指针设备，如：mouse pointing device(鼠标指针),stylus pointing device(尖笔设备)  
		if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) { 
			
			switch (event.getAction()) {  
			//处理滚轮事件  
			case MotionEvent.ACTION_SCROLL:  
				Trace.Debug("####onGenericMotionEvent getAxisValue is " + event.getAxisValue(MotionEvent.AXIS_VSCROLL));
				//获得垂直坐标上的滚动方向,也就是滚轮向下滚  
				if( event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {      
				    Trace.Debug("####onGenericMotionEvent wheel down");  
//				    mKeyScaleImageView.reverseZoom();														
				} else { 
				//获得垂直坐标上的滚动方向,也就是滚轮向上滚  
				   Trace.Debug("####onGenericMotionEvent wheel up"); 
				   mKeyScaleImageView.zoom();																																	
				}
				//判断是否显示缩放LinearLayout
				showScaleLinearLayout();
				mGuideBorderView.zoom();	
				int scale = (int) getScale();
				mScaleTextView.setText(scale + "");	
			    return true;  
			}  
		}  
		return super.onGenericMotionEvent(event);
	}

}