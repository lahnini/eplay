package com.konka.eplay.modules;

import iapp.eric.utils.base.Trace;

import java.util.HashMap;
import java.util.Map;

import com.konka.eplay.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * 
 * Created on: 2015-6-1
 * 
 * @brief 集成焦点点击效果、焦点平移效果的RelativeLayout
 * @author mcsheng
 * @date Latest modified on: 2015-6-5
 * @version V1.0.00
 * 
 */
public class IntegratedRelativeLayout extends RelativeLayout {
 
	private static final String TAG = "IntegratedRelativeLayout";
	private View mSelectedView = null;
	private View mPreSelectedView = null;
	private OnItemSelectedListener mOnItemSelectedListener = null;
	private OnItemClickedListener mOnItemClickedListener = null;
	
	private Map<View, FocusNodeInfo> mPositionMap = new HashMap<View,FocusNodeInfo>();
	
	private Boolean mFirstTime = true;
	
	private int mIndex = -1;
	
	//点击透明效果帧率调整
	private int mCurrentAlphaFrame = 1;
	private int mAlphaFrameRate = 6;
	
	//焦点平移帧率调整
	private int mCurrentTransFrame = 1;
	private int mTransFrameRate = 8;
	
	//放大帧率调整
	private int mCurrentEnlargeFrame = 1;
	private int mEnlargeFrameRate = 6;
	
	private Drawable mSelectorDrawable = null;
	private Rect mSelectorPaddingRect = null;
	private Rect mManualPaddingRect = null;
	
	private float mScaleX = 1.0f, mScaleY = 1.0f;
	
	private Boolean mIsPressed = false;
	private Boolean mIsTranslated = false;
	private Boolean mIsScaled = false;
	
	private Boolean mIsOpenClickEffect = false;
	private Boolean mIsOpenScaleEffect = false;
	private Boolean mIsOpenTransEffect = false;
	
	private Rect mTransRectBefore = null;
	private Rect mTransRectAfter = null;
	
		
	
	public IntegratedRelativeLayout(Context context) {
		super(context);
		init();
	}
	
	public IntegratedRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public IntegratedRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	

	private void init() {
		this.setChildrenDrawingOrderEnabled(true);
	}
	
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		//Log.d(TAG,"getChildDrawingOrder i is " + i);
		//若不重写此方法，默认的子View绘制顺序是0~(childCount - 1)，i逐渐增大
		int position = this.mIndex;
		
		//position小于0时，正常
		if (position < 0) {
			return i;
		}
		
		//position小于i时，正常
		if (i < position) {
			return i;
		}
		
		//position大于或等于i时，逆序
		if (i >= position) {
			return childCount - 1 - i + position;
		}
		
		return i;
	}
	
	@SuppressLint("UseValueOf")
	private void initChildPosition() {
		Log.d(TAG,"initChildPosition " + this.getChildCount());
		for (int i = 0; i < this.getChildCount(); i++) {
			View view = this.getChildAt(i);
			if(!mPositionMap.containsKey(view)) {
				FocusNodeInfo nodeInfo = new FocusNodeInfo();
				nodeInfo.index = i;
				nodeInfo.fromLeftView = view.focusSearch(FOCUS_LEFT);
				nodeInfo.fromRightView = view.focusSearch(FOCUS_RIGHT);
				nodeInfo.fromDownView = view.focusSearch(FOCUS_DOWN);
				nodeInfo.fromUpView = view.focusSearch(FOCUS_UP);
				mPositionMap.put(view, nodeInfo);
			}
		}
	}
	
	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			Log.d(TAG, "onFocusChanged gain");
			
			if (mSelectorDrawable != null) {
				mSelectorDrawable.setAlpha(255);
			}
			
			for (int i = 0; i < this.getChildCount(); i++) {
				this.getChildAt(i).setFocusable(true);
			}
			
			if (mSelectedView != null) {
				mSelectedView.requestFocus();

			} else if (this.getChildAt(0) != null) {
				
				this.getChildAt(0).requestFocus();
				this.setSelectedView(this.getChildAt(0));
				mIndex = 0;
			}
			
			if (mIsOpenScaleEffect) {
				mSelectedView.setScaleX(mScaleX);
				mSelectedView.setScaleY(mScaleY);
			}
			
			this.invalidate();
			
		} else {
			Trace.Debug("####onFocusChanged lose");
		}
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(mFirstTime) {
			mFirstTime = false;
			initChildPosition();
		}
		
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		Log.d(TAG,"dispatchDraw");
		
		if(View.VISIBLE != this.getVisibility()) {
			return;
		}
		
		if (mIsOpenClickEffect && mIsPressed) {
			drawPressedFocus(canvas);
			if(mCurrentAlphaFrame == mAlphaFrameRate + 1) {
				mCurrentAlphaFrame = 1;
				mIsPressed = false;
				View view = this.getSelectedView();
				//执行点击
				executeItemClicked(view, mPositionMap.get(view).index);
				view.performClick();
				
			} else {
				this.invalidate();
			}
		} else if (mIsOpenScaleEffect && mIsOpenTransEffect && mIsTranslated && mIsScaled) {
			//drawSynScaleAndTrans(canvas);	
			drawScaleAndScaleTrans(canvas);
		} else if (mIsOpenTransEffect && mIsTranslated) {
			drawTransFocus(canvas);
		} else if (mIsOpenScaleEffect && mIsScaled) {
			drawFrame();
			drawFocus(canvas);
		} else {
			drawFocus(canvas);
		}
		
	}
	
	//同步绘制缩放和平移焦点框，先缩放后平移焦点框（焦点不按缩放平移）
	private void drawScaleAndTrans(Canvas canvas) {
		if (this.mCurrentEnlargeFrame < this.mEnlargeFrameRate) {
			drawScale();
			drawPreScale();
			this.mCurrentEnlargeFrame++;
			this.invalidate();
		} else if (this.mCurrentEnlargeFrame == this.mEnlargeFrameRate) {
			drawScale();
			drawPreScale();			
			drawTransFocus(canvas);
			if(this.mCurrentTransFrame == 1) {
				this.mCurrentEnlargeFrame = 1;
				mIsScaled = false;
			}
		}
	}
	
	//同步绘制缩放和平移焦点框，先缩放后平移焦点框（焦点按缩放平移）
	private void drawScaleAndScaleTrans(Canvas canvas) {
		if (this.mCurrentEnlargeFrame <= this.mEnlargeFrameRate) {
			drawScale();
			drawPreScale();
			this.mCurrentEnlargeFrame++;			
			this.invalidate();
		} else if (this.mCurrentEnlargeFrame > this.mEnlargeFrameRate) {			
			
			if (mCurrentTransFrame == 1) {
				mTransRectBefore = this.getRectBeforeScale();
				mTransRectAfter = this.getRectAfterScale();
			}
			
			View view = this.getSelectedView();
			
			float scaleWidthBefore = mTransRectBefore.width();
			float scaleHeightBefore = mTransRectBefore.height();
			
			float scaleWidthAfter = scaleWidthBefore + ((mScaleX - 1.0f) * view.getWidth()  / mTransFrameRate * mCurrentTransFrame);
			float scaleHeightAfter = scaleHeightBefore + ((mScaleY - 1.0f) * view.getHeight() / mTransFrameRate * mCurrentTransFrame);
			
			int differWidthAbs = (int) Math.abs((scaleWidthBefore - scaleWidthAfter) / 2.0f);
			int differHeightAbs = (int) Math.abs((scaleHeightBefore - scaleHeightAfter) / 2.0f);				
					
			float totalX = mTransRectAfter.centerX() - mTransRectBefore.centerX();
			float totalY = mTransRectAfter.centerY() - mTransRectBefore.centerY();
			
			int dx = (int) (totalX / mTransFrameRate * mCurrentTransFrame);
			int dy = (int) (totalY / mTransFrameRate * mCurrentTransFrame);
			
			Rect moveRect = new Rect(mTransRectBefore);
			
			moveRect.left = moveRect.left - differWidthAbs;
			moveRect.top = moveRect.top - differHeightAbs;
			moveRect.right = moveRect.right + differWidthAbs;
			moveRect.bottom = moveRect.bottom + differHeightAbs;
			
			moveRect.offset(dx, dy);
			
			mSelectorDrawable.setBounds(moveRect);
			mSelectorDrawable.draw(canvas);
			mSelectorDrawable.setVisible(true, true);
			
			mCurrentTransFrame++;
			
			if (mCurrentTransFrame == mTransFrameRate + 1) {
				mIsScaled = false;
				mIsTranslated = false;
				mCurrentEnlargeFrame = 1;
				mCurrentTransFrame = 1;
				Trace.Debug("####drawAsynScaleAndTrans end");
			} else {
				this.invalidate();
			}
			
		}
	}
	
	private void drawFrame() {
		if (this.mCurrentEnlargeFrame < this.mEnlargeFrameRate) {
			drawScale();
			drawPreScale();
			this.mCurrentEnlargeFrame++;
			this.invalidate();
		} else if (this.mCurrentEnlargeFrame == this.mEnlargeFrameRate) {
			drawScale();
			drawPreScale();
			this.mCurrentEnlargeFrame = 1;
		}	
	}
	
	private void drawScale() {
		View view = getSelectedView();
		if (view != null) {
			float f1 = this.mScaleX - 1.0F;
			float f2 = this.mScaleY - 1.0F;
			int i = this.mEnlargeFrameRate;
			int j = this.mCurrentEnlargeFrame;
			float f3 = 1.0F + f1 * j / i;
			float f4 = 1.0F + f2 * j / i;
			view.setScaleX(f3);
			view.setScaleY(f4);
						
		}
	}
	
	private void drawPreScale() {
		
		if (mPreSelectedView != null) {
			Log.d(TAG,mPreSelectedView.getWidth() + "," + mPreSelectedView.getHeight());
			float f1 = this.mScaleX - 1.0F;
			float f2 = this.mScaleY - 1.0F;
			int i = this.mEnlargeFrameRate;
			int j = this.mCurrentEnlargeFrame;
			int k = i - j;
			float f3 = 1.0F + f1 * k / i;
			float f4 = 1.0F + f2 * k / i;
			mPreSelectedView.setScaleX(f3);
			mPreSelectedView.setScaleY(f4);
		}
	}
	
	
	private void drawFocus(Canvas canvas) {
		drawSelector(canvas);
	}
	
	//绘制点击效果
	private void drawPressedFocus(Canvas canvas) {
		
		if (mSelectorDrawable != null) {
			int alpha = 0;
			//当mCurrentAlphaFrame低于一半时，alpha逐渐变大
			if(mCurrentAlphaFrame <= mAlphaFrameRate) {
				alpha = (int) (255 / this.mAlphaFrameRate * this.mCurrentAlphaFrame);
			}
			
			mSelectorDrawable.setAlpha(alpha);
				
			drawSelector(canvas);
			
			mCurrentAlphaFrame++;
		}
	}
	
	//绘制平移焦点效果
	private void drawTransFocus(Canvas canvas) {
		if (mSelectorDrawable != null) {
			
			if (mCurrentTransFrame == 1) {
				mTransRectBefore = this.getRectBeforeScale();
				mTransRectAfter = this.getRectAfterScale();
				
				View view = this.getSelectedView();
				//当前后两个Rect的大小不同时，计算改变前框。
				float scaleWidthBefore = mTransRectBefore.width();
				float scaleHeightBefore = mTransRectBefore.height();
				
				float scaleWidthAfter = mTransRectAfter.width();
				float scaleHeightAfter = mTransRectAfter.height();
				
				int beforeArea = (int) (scaleWidthBefore * scaleHeightBefore);
				int afterArea = (int) (scaleWidthAfter * scaleHeightAfter);
				
				int differWidthAbs = (int) Math.abs((scaleWidthBefore - scaleWidthAfter) / 2.0f);
				int differHeightAbs = (int) Math.abs((scaleHeightBefore - scaleHeightAfter) / 2.0f);
				
				if (beforeArea > afterArea) {
					mTransRectBefore.left = mTransRectBefore.left + differWidthAbs;
					mTransRectBefore.top = mTransRectBefore.top + differHeightAbs;
					mTransRectBefore.right = mTransRectBefore.right - differWidthAbs;
					mTransRectBefore.bottom = mTransRectBefore.bottom - differHeightAbs;
				} else if (beforeArea < afterArea) {
					mTransRectBefore.left = mTransRectBefore.left - differWidthAbs;
					mTransRectBefore.top = mTransRectBefore.top - differHeightAbs;
					mTransRectBefore.right = mTransRectBefore.right + differWidthAbs;
					mTransRectBefore.bottom = mTransRectBefore.bottom + differHeightAbs;
					
				}
			}
			
			float totalX = mTransRectAfter.centerX() - mTransRectBefore.centerX();
			float totalY = mTransRectAfter.centerY() - mTransRectBefore.centerY();
			
			
			int dx = (int) (totalX / mTransFrameRate * mCurrentTransFrame);
			int dy = (int) (totalY / mTransFrameRate * mCurrentTransFrame);
			
			Rect moveRect = new Rect(mTransRectBefore);
			moveRect.offset(dx, dy);
			 
			mSelectorDrawable.setBounds(moveRect);
			mSelectorDrawable.draw(canvas);
			mSelectorDrawable.setVisible(true, true);
			
			mCurrentTransFrame++;
			if(mCurrentTransFrame > mTransFrameRate) {
				mIsTranslated = false;
				mCurrentTransFrame = 1;
			} else {
				this.invalidate();
			}
		}
	}
	
	private void drawSelector(Canvas canvas) {
		
		if (mSelectorDrawable != null) {
			Log.d(TAG,"drawSelector");
			Rect rect = this.getRectAfterScale();
			if (rect != null) {
				mSelectorDrawable.setBounds(rect);
				mSelectorDrawable.draw(canvas);
				mSelectorDrawable.setVisible(true, true);
			}			
		}
	}
	
	public void setSelector(int resId) {
		mSelectorDrawable = this.getContext().getResources().getDrawable(resId);
		mSelectorPaddingRect = new Rect();
		mSelectorDrawable.getPadding(mSelectorPaddingRect);
		//回调刷新，当drawable中的内容发生改变时就会回调，使this调用invalidate
		mSelectorDrawable.setCallback(this);
		mSelectorDrawable.setState(this.getDrawableState());		
	}
	
	public Drawable getSelector() {
		return mSelectorDrawable;
	}
	
	public void setTransFrameRate(int transFrameRate) {
		mTransFrameRate = transFrameRate;
	}
	
	private Rect getRectAfterScale() {
		View view = this.getSelectedView();
		if (view == null) {
			return null;
		}		
		return getRectForFocus(view);		
	}
	
	private Rect getRectBeforeScale() {
		View view = mPreSelectedView;
		if (view == null) {
			return null;
		}
		
		return getRectForFocus(view);	
	}
	
	//获取焦点框位置
	private Rect getRectForFocus(View view) {
		int[] locationView = new int[2];
		int[] locationLayout = new int[2];
		
		view.getLocationInWindow(locationView);
		this.getLocationInWindow(locationLayout);
		
		int scaleWidth = (int) (view.getWidth() * view.getScaleX());
		int scaleHeight = (int) (view.getHeight() * view.getScaleY());
		
		Rect rect1 = new Rect(locationView[0], locationView[1], 
				locationView[0] + scaleWidth, locationView[1] + scaleHeight);
		
		Rect rect2 = new Rect(locationLayout[0], locationLayout[1],
				locationLayout[0] + this.getWidth(), locationLayout[1] + this.getHeight());
		
		//使用Scroller进行滚动之后，坐标也进行相应地偏移，否则会使焦点框定位不准
		rect2.left = rect2.left - this.getScrollX();
		rect2.top = rect2.top - this.getScrollY();
		rect2.right = rect2.right - this.getScrollX();
		rect2.bottom = rect2.bottom - this.getScrollY();

		rect1.left -= rect2.left;
		rect1.right -= rect2.left;
		rect1.top -= rect2.top;
		rect1.bottom -= rect2.top;
		
		
		//根据Drawable的建议尺寸进行修补
		rect1.left -= mSelectorPaddingRect.left;
		rect1.top -= mSelectorPaddingRect.top;
		rect1.right += mSelectorPaddingRect.right;
		rect1.bottom += mSelectorPaddingRect.bottom;
		
		
		
		if (mManualPaddingRect != null) {
			rect1.left += mManualPaddingRect.left;
			rect1.right += mManualPaddingRect.right;
			rect1.top += mManualPaddingRect.top;
			rect1.bottom += mManualPaddingRect.bottom;
		}
						
		Log.i(TAG,"getRectForFocus this.getScrollX is " + this.getScrollX());
		Log.i(TAG,"getRectForFocus this.getScrollY is " + this.getScrollY());
		Log.i(TAG,"getRectForFocus rect1(left,top,right,bottom) is " + 
				rect1.left + "," + rect1.top + "," + rect1.right + "," + rect1.bottom);
		Log.i(TAG,"getRectForFocus scaleWidth,scaleHeight is " + scaleWidth + "," + scaleHeight);
		return rect1;

	}
	

	
	public void setManualPaddingRect(int left , int top, int right, int bottom) {
		mManualPaddingRect = new Rect();
		mManualPaddingRect.left = left;
		mManualPaddingRect.top = top;
		mManualPaddingRect.right = right;
		mManualPaddingRect.bottom = bottom;
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		//焦点还在平移时屏蔽所有按键
		if (mIsOpenTransEffect && mIsTranslated) {
			return true;
		}
		
		int what = event.getKeyCode();
		View nextFocusView = null;
		
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			
			switch (what) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				nextFocusView = this.getSelectedView().focusSearch(FOCUS_DOWN);
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				nextFocusView = this.getSelectedView().focusSearch(FOCUS_UP);	
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				nextFocusView = this.getSelectedView().focusSearch(FOCUS_RIGHT);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				nextFocusView = this.getSelectedView().focusSearch(FOCUS_LEFT);
				break;
			case KeyEvent.KEYCODE_ENTER:
				break;
			default:
				break;
			}
			
			if (nextFocusView != null) {
				//判断焦点是否已经移出RelativeLayout
				if (!nextFocusView.getParent().equals(this)) {
					Log.d(TAG, "false");
					if (mSelectorDrawable != null) {
						Log.d(TAG, "false to alpha");
						mSelectorDrawable.setAlpha(0);
						for (int i = 0; i < this.getChildCount(); i++) {
							this.getChildAt(i).setFocusable(false);
						}
						this.getSelectedView().setScaleX(1.0f);
						this.getSelectedView().setScaleY(1.0f);
						this.invalidate();
					}					
					return super.dispatchKeyEvent(event);
				}
				Log.d(TAG, nextFocusView.toString());
				
				mPreSelectedView = this.getSelectedView();
				this.setSelectedView(nextFocusView);
				
//				Log.d(TAG, mPositionMap.get(nextFocusView).toString());
				if (mPositionMap.get(nextFocusView) != null) {
					
					int postion = mPositionMap.get(nextFocusView).index;
					mIndex = postion;
					mIsTranslated = true;
					mIsScaled = true;
					//刷新
					this.invalidate();
					executeItemSelected(nextFocusView, postion);					
				}
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (what) {
			case KeyEvent.KEYCODE_ENTER:
				if(event.getRepeatCount() == 0 && this.getSelectedView() != null) {
					View currentView = this.getSelectedView();
					if(mPositionMap.get(currentView) != null) {						
						int postion = mPositionMap.get(currentView).index;
						if(mSelectorDrawable != null) {
							mIsPressed = true;
							this.invalidate();
						}
						return true;
					}					
				}
				break;
			default:
				break;
			}
		}
				
		return super.dispatchKeyEvent(event);
	}
	
	public void setScale(float scaleX, float scaleY) {
		mScaleX = scaleX;
		mScaleY = scaleY;
	}
	
	private void setSelectedView(View view) {
		mSelectedView = view;
	}
	
	public View getSelectedView() {
		return mSelectedView;
	}
	
	/**
	 * 是否开启焦点点击效果
	 */
	public void openClickEffect(Boolean isOpenClickEffect) {
		mIsOpenClickEffect = isOpenClickEffect;
	}
	
	/**
	 * 是否开启焦点平移效果
	 */
	public void openTransEffect(Boolean isOpenTransEffect) {
		mIsOpenTransEffect = isOpenTransEffect;
	}
	
	/**
	 * 是否开启缩放效果
	 */
	public void openEnlargeEffect(Boolean isOpenEnlargeEffect) {
		mIsOpenScaleEffect = isOpenEnlargeEffect;
	}
	
	
	private void executeItemSelected(View childView, int position) {
		if (mOnItemSelectedListener != null) {
			mOnItemSelectedListener.onItemSelected(IntegratedRelativeLayout.this, childView, position);
		}
	}
	
	private void executeItemClicked(View childView, int position) {
		if (mOnItemClickedListener != null) {
			mOnItemClickedListener.onItemClicked(IntegratedRelativeLayout.this, childView, position);
		}
	}

	
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
	}
	
	public void setOnItemClickedListener(OnItemClickedListener listener) {
        mOnItemClickedListener = listener;
	}
	
	public interface OnItemSelectedListener {
        void onItemSelected(View parent, View childView, int position);
    }
	
	public interface OnItemClickedListener {
        void onItemClicked(View parent, View childView, int position);
    }
	
	public interface OnScrollListener {
		public static final int SCROLL_STATE_IDLE = 0;
		public static final int SCROLL_STATE_TOUCH_SCROLL = 1;
		public static final int SCROLL_STATE_FLING = 2;		

		public void onScrollStateChanged(ViewGroup viewGroup, int state);
		
	}
	
	//记录一个view的左右上下可获取到焦点的view信息类
	private class FocusNodeInfo {
		public int index;
		public View fromLeftView;
		public View fromRightView;
		public View fromUpView;
		public View fromDownView;
	}
	
}