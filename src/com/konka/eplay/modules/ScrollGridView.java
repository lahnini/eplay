package com.konka.eplay.modules;

import iapp.eric.utils.base.Trace;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.konka.eplay.Constant;
import com.konka.eplay.R;


/**
 *
 * Created on: 2015-5-8
 *
 * @brief 实现滚动的GridView，并且可以监听滚动的状态改变(原生的GridView是不能在电视上监听的，在手机上可以)；
 * 为了实现滚动时，焦点不闪烁，使用了一个虚的边框，然后透明掉真实的selector
 * @author mcsheng
 * @date Latest modified on: 2015-5-13
 * @version V1.0.00
 *
 */
public class ScrollGridView extends GridView {

	//滚动距离
	private int mScrollDistance = -1;
	private int mPreScrollDistance = -1;
	
	private Boolean mFisrtTime = true;
	//滚动持续时间
	private int mScrollDuration = 300;

	private Boolean mIsScrolling = false;

	private Object lock = new Object();
	private OnScrollListener mOuterScrollListener = null;
	private OnItemSelectedListener mOuterItemSelectedListener = null;
	private OnItemClickListener mOuterItemClickListener = null;
	private ImageView mBorderView = null;
	private int mDirection = 0;

	private Boolean mIsGainFocus = false;

	private Boolean mIsMouseTouching = false;

	//用于是否在滚动到顶部
	private Boolean mIsScrollToTop = false;

	private Boolean mIsScrollToPositon = false;
	//add by xuyunyu  用于避免onMeasure多次调用而使多次调用getView浪费资源的情况的标志量
	public Boolean mIsOnMeasure;

	private int mScrollToPosition = -1;
	
	private AlphaAnimation mClickAlphaAnimation = null;
	
	//用于指示是否开始向下滚动到最后一行的标志，true表示开始滚动到最后一行，false表示不是
	private Boolean mIsDownScrollToLastLine = false;
	
	public ScrollGridView(Context context) {
		super(context);
		init();
	}

	public ScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ScrollGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		//这里是对super设置监听，因为ScrollGridView重写了setOnScrollListener
		super.setOnScrollListener(mOnScrollListener);
		super.setOnItemSelectedListener(mOnItemSelectedListener);
		super.setOnItemClickListener(mOnItemClickListener);
		
		mClickAlphaAnimation = new AlphaAnimation(1.0f, 0.5f);
		mClickAlphaAnimation.setDuration(100);
		
		//取消滚动到边缘的效果（默认是开启的）
		this.setOverScrollMode(View.OVER_SCROLL_NEVER);
	}


	//add by xuyunyu
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mIsOnMeasure = true;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	//add by xuyunyu
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mIsOnMeasure = false;
		super.onLayout(changed, l, t, r, b);
	}
	
	public Boolean isOnMeasure() {
		return mIsOnMeasure;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		Trace.Debug("####onFocusChanged");
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		mIsGainFocus = gainFocus;
//		//只处理第一次
//		if(gainFocus && mFisrtTime && this.getSelectedView() != null) {
//			mFisrtTime = false;
//			//算出需要滚动的距离
//			mFixScrollDistance = this.getSelectedView().getHeight() + this.getVerticalSpacing();
//			Trace.Debug("####onFocusChanged mFixScrollDistance is " + mFixScrollDistance);
//			return;
//		}

		//失去焦点或者获取到焦点，都先将虚边框设置不可见，selector设置为不透明，以避免GridView获取到焦点时的闪烁
		if(mBorderView != null) {
			mBorderView.setVisibility(View.GONE);
		}

		this.getSelector().setAlpha(255);
	}

	/*View的按键处理优先级：dispatchKeyEvent > OnKeyListener > onKeyDown，所以在
	 *dispatchKeyEvent方法最先处理按键，以避免外部方法OnKeyListener的按键监听干扰而导致显示出问题*/
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		//正在滚动禁止遥控器操作，以避免持续操作滚动出错
		if(isScrolling()) {
			return true;
		}

		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			//有按键被按下时就先复位标志
			mIsDownScrollToLastLine = false;
			
			switch(event.getKeyCode()) {

			case KeyEvent.KEYCODE_DPAD_DOWN: {
				//显示滑块
				ScrollGridView.this.setVerticalScrollBarEnabled(true);
			
				int firstVisiblePosition = this.getFirstVisiblePosition();
				int lastVisiblePosition = this.getLastVisiblePosition();
				int selPos = this.getSelectedItemPosition();
				//到最后一行时不滚动，避免回弹出错
				//当向下键被按下时，并且被选中的Item在第二行时才进行向上滚动特定的距离
				if(lastVisiblePosition != this.getAdapter().getCount() - 1 && selPos > firstVisiblePosition + 3) {
					//处理最后一行的情况
					if(selPos + this.getNumColumns() > ScrollGridView.this.getAdapter().getCount() - 1) {
						if(mBorderView != null) {
							Trace.Debug("####dispatchKeyEvent key down borderView gone");
							//将标志位置为true，以避免长按键滚动到最后一行出现虚边框在空缺位置闪跳的情况
							mIsDownScrollToLastLine = true;
							mBorderView.setVisibility(View.GONE);
						}
					}
					mDirection = Constant.SCROLL_DOWN;
					//算出需要滚动的距离
					mScrollDistance = this.getSelectedView().getHeight() + this.getVerticalSpacing();
					this.smoothScrollBy(mScrollDistance, mScrollDuration);
					return true;
				}
			}
				break;
			case KeyEvent.KEYCODE_DPAD_UP: {

				ScrollGridView.this.setVerticalScrollBarEnabled(true);

				int firstVisiblePosition = this.getFirstVisiblePosition();
				int selPos = this.getSelectedItemPosition();
				//firstVisiblePosition为0时不滚动，避免回弹出错
				//当向上键被按下时，并且被选中的Item在第一行时才进行向下滚动特定的距离
				if(firstVisiblePosition != 0 && selPos < firstVisiblePosition + 4) {
					mDirection = Constant.SCROLL_UP;
					//算出需要滚动的距离
					mScrollDistance = this.getSelectedView().getHeight() + this.getVerticalSpacing();
					this.smoothScrollBy(-mScrollDistance, mScrollDuration);
					return true;
				}
			}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				//不显示滑块
				ScrollGridView.this.setVerticalScrollBarEnabled(false);
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				ScrollGridView.this.setVerticalScrollBarEnabled(false);
				break;
			default: 
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		//正在滚动禁止遥控器操作，以避免持续操作滚动出错
//		if(isScrolling()) {
//			return true;
//		}
//
//		return super.dispatchTouchEvent(ev);
//	}

	//将鼠标滚轮操作去掉
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
					return true;
				} else {
				//获得垂直坐标上的滚动方向,也就是滚轮向上滚
					return true;
				}
			}
		}
		return super.onGenericMotionEvent(event);
	}

	/**
	 * 设置滚动持续时间
	 */
	public void setScrollDuration(int duration) {
		mScrollDuration = duration;
	}

	public int getScrollDirection() {
		return mDirection;
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


	private OnScrollListener mOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView paramAbsListView, int paramInt) {		

			if(mOuterScrollListener != null) {
				mOuterScrollListener.onScrollStateChanged(paramAbsListView, paramInt);
			}
			
			switch(paramInt) {
			//滚动结束
			case OnScrollListener.SCROLL_STATE_IDLE: {

				if(mIsMouseTouching) {
					ScrollGridView.this.setScrolling(false);
					mIsMouseTouching = false;
					return;
				} else if(mIsScrollToTop) {
					ScrollGridView.this.setScrolling(false);
					mIsScrollToTop = false;
					//选中第一个
					ScrollGridView.this.setSelection(0);
					((BaseAdapter)ScrollGridView.this.getAdapter()).notifyDataSetChanged();
					return;
				} else if(mIsScrollToPositon) {
					mIsScrollToPositon = false;			
					ScrollGridView.this.setScrolling(false);
					ScrollGridView.this.setSelection(mScrollToPosition);
					((BaseAdapter)ScrollGridView.this.getAdapter()).notifyDataSetChanged();
					return;
				}

				int firstVisiblePosition = ScrollGridView.this.getFirstVisiblePosition();
				int selPos = ScrollGridView.this.getSelectedItemPosition();
				int numColumns = ScrollGridView.this.getNumColumns();
				if(selPos > firstVisiblePosition + numColumns - 1) {
					ScrollGridView.this.setSelection(selPos - numColumns);
				} else {
					//当小于总的Item数目时才进行焦点跳转，以避免滚到最后一页时Item排布未满而
					//出现选中越界问题
					if(selPos + numColumns < ScrollGridView.this.getAdapter().getCount()) {
						ScrollGridView.this.setSelection(selPos + numColumns);
					} else {
						ScrollGridView.this.setSelection(ScrollGridView.this.getAdapter().getCount() - 1);
					}
				}
				ScrollGridView.this.setScrolling(false);

			}
				break;
			//正在滚动
			case OnScrollListener.SCROLL_STATE_FLING:
				ScrollGridView.this.setScrolling(true);
				if(mIsScrollToTop) {
					//申请焦点，避免被其他控件抢焦点，出现焦点闪烁现象
					ScrollGridView.this.requestFocus();
					mBorderView.setVisibility(View.GONE);
					ScrollGridView.this.getSelector().setAlpha(0);
					break;
				}
				break;
			//手指贴着滚动
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				ScrollGridView.this.setScrolling(true);
				mIsMouseTouching = true;
				if(mBorderView != null) {
					mBorderView.setVisibility(View.GONE);
				}

				break;
			default:
				break;
			}
			
		}

		@Override
		public void onScroll(AbsListView paramAbsListView, int paramInt1,
				int paramInt2, int paramInt3) {
			if(mOuterScrollListener != null) {
				mOuterScrollListener.onScroll(paramAbsListView, paramInt1, paramInt2, paramInt3);
			}
		}
	};

	private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> paramAdapterView,
				View paramView, int paramInt, long paramLong) {

			if(mBorderView != null && !mIsDownScrollToLastLine) {
				ScrollGridView.this.setBorderLocation(paramView);
			} else if(mBorderView != null && mIsDownScrollToLastLine) {
				//当滚动到最后一行时，若对应的位置时没有数据就只显示真的焦点框，而不进行虚焦点框定位，避免闪跳
				ScrollGridView.this.getSelector().setAlpha(255);
			}
			
			if(mOuterItemSelectedListener != null) {
				mOuterItemSelectedListener.onItemSelected(paramAdapterView, paramView, paramInt, paramLong);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> paramAdapterView) {
			if(mOuterItemSelectedListener != null) {
				mOuterItemSelectedListener.onNothingSelected(paramAdapterView);
			}
		}
	};

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2,
				final long arg3) {


			mClickAlphaAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation paramAnimation) {					
					ScrollGridView.this.getSelector().setAlpha(0);
				}
				
				@Override
				public void onAnimationRepeat(Animation paramAnimation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation paramAnimation) {
					if (mOuterItemClickListener != null) {
						mOuterItemClickListener.onItemClick(arg0, arg1, arg2, arg3);
					}
				}
			});
			
			if (mBorderView != null) {
				mBorderView.setVisibility(View.VISIBLE);
				ScrollGridView.this.setBorderLocation(ScrollGridView.this.getSelectedView());
				mBorderView.clearAnimation();
				mBorderView.startAnimation(mClickAlphaAnimation);
			}
		}
	};

	/**
	 * 设置滚动监听
	 */
	public void setOnScrollListener(OnScrollListener listener) {
		this.mOuterScrollListener = listener;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		this.mOuterItemSelectedListener = listener;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mOuterItemClickListener = listener;
	}

	public void setBorderView(ImageView borderView) {
		
		//borderView = null;
		
		if(borderView != null) {
			mBorderView = borderView;
			//设置selector为透明
			ScrollGridView.this.getSelector().setAlpha(0);
			borderView.setBackgroundResource(R.drawable.picture_brower_item_focus);
		}
	}

	private void setBorderLocation(View toView) {

		/*
		 * 当获取到的selector的宽度和高度为0时，不显示虚焦点框，显示真实的selector的焦点框，这样的情况一
		 * 般会出现在GridView首次获取到焦点的时候,不是必现的，是偶然出现的
		 */
        if(0 == this.getSelector().getBounds().width() ||
        		0 == this.getSelector().getBounds().height()) {
        	ScrollGridView.this.getSelector().setAlpha(255);
        	mBorderView.setVisibility(View.GONE);
        	return;
        }

		if(toView != null && mBorderView != null) {

			//设置selector透明
			ScrollGridView.this.getSelector().setAlpha(0);

			int [] location = new int[2];
			toView.getLocationInWindow(location);

			Rect rect = new Rect();
			//内边距
			this.getSelector().getPadding(rect);

			int left = location[0] - rect.left;
	        int top = location[1] - rect.top;
	        int right = left + toView.getWidth() + rect.right;
	        int bottom = top + toView.getHeight() + rect.bottom;

	        MarginLayoutParams lp = (MarginLayoutParams) mBorderView.getLayoutParams();
        	lp.setMargins(left, top, right, bottom);
        	//根据selector的大小进行设置
	        lp.width = this.getSelector().getBounds().width();
	        lp.height = this.getSelector().getBounds().height();

//	        Trace.Debug("####onItemSelected location widht,height " + lp.width + "," + lp.height);
//	        Trace.Debug("####onItemSelected location rect is" + rect.left + "," + rect.top + "," + rect.right + "," + rect.bottom);
 	        mBorderView.setLayoutParams(lp);
 	        //当GridView获取到焦点的时候才使虚边框可见
 	        if(mIsGainFocus) {
 	        	mBorderView.setVisibility(View.VISIBLE); 	        	
 	        }
		}
	}


	/**
	 * 平滑滚动到顶部
	 */
	public void smoothScrollToTop() {
		int firstPosition = ScrollGridView.this.getFirstVisiblePosition();
		int numColums = ScrollGridView.this.getNumColumns();
		mScrollDistance = this.getSelectedView().getHeight() + this.getVerticalSpacing();
		mIsScrollToTop = true;
		//当大于10行时，设置只滚动10行距离，避免滚动距离过大以使smoothScrollBy出错的情况
		if(firstPosition / numColums > 10) {
			ScrollGridView.this.smoothScrollBy(-(10 * mScrollDistance), 1000);
		} else {
			ScrollGridView.this.smoothScrollBy(-((firstPosition / numColums) * mScrollDistance), 1000);
		}

	}

	/**
	 * 滚动到position指定的位置，由于原生的smoothScrollToPosition滚动有问题，所以现重
	 *新重写
	 */
	
	public void smoothScrollToPosition(int position) {
		ScrollToPosition(position);
	}
	
	public Boolean ScrollToPosition(int position) {
		
		//滚动时使selector不可见
		this.getSelector().setAlpha(0);
		
		int numColums = ScrollGridView.this.getNumColumns();
		int firstPosition = ScrollGridView.this.getFirstVisiblePosition();
		
		/*这里用于处理在上一次smoothScrollToPosition还没有结束时，就调用smoothScrollToPosition的情况.
		 *即此时this.getSelectedView()会为null，于是使用先前的滚动距离作为现在的滚动距离*/
		if(this.getSelectedView() != null) {
			mScrollDistance = this.getSelectedView().getHeight() + this.getVerticalSpacing();
			mPreScrollDistance = mScrollDistance;
		} else {
			mScrollDistance = mPreScrollDistance;
		}
		
		//指示正在滚动到特定的位置
		mIsScrollToPositon = true;
		//滚动到特定的位置时，是否会滚动的标志位
		Boolean isScroll = false;
	
		int diferNumColums = 0;
		
		if(position > firstPosition) {
			diferNumColums = position/numColums - firstPosition / numColums;
			//当滚动的行数过多时，只滚动10行，以避免滚动效果不好的情况
			if(diferNumColums > 10) {
				diferNumColums = 10;
			}
			ScrollGridView.this.smoothScrollBy( (diferNumColums * mScrollDistance), 1000);
			mScrollToPosition = position;
		} else if(position < firstPosition) {
			diferNumColums = firstPosition/numColums - position / numColums;
			if(diferNumColums > 10) {
				diferNumColums = 10;
			}
			ScrollGridView.this.smoothScrollBy( -(diferNumColums * mScrollDistance) , 1000);
			mScrollToPosition = position;
		} else {
			return false;
		}
		
		if (diferNumColums == 0) {
			isScroll = false;
		} else {
			isScroll = true;
		}
		
		return isScroll;
	}

	public Boolean isScrollToTop() {
		return mIsScrollToTop;
	}

	public Boolean isScrollToPosition() {
		return mIsScrollToPositon;
	}
	
	
	public  void setBordViewVisibility(int visibility) {
		if (mBorderView != null) {
			mBorderView.setVisibility(visibility);
		}
	}

}