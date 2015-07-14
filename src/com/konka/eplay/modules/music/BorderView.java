package com.konka.eplay.modules.music;

import com.konka.eplay.R;

import iapp.eric.utils.base.Trace;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

/**
 * 焦点跳转动画框</br> <b>创建时间:</b> 2014-12-10
 *
 * @author mcsheng
 */
public class BorderView extends ImageView {
	protected static final String TAG = "BorderView";

	public static final String TOP = "top";
	public static final String CENTER = "center";
	public static final String BOTTOM = "bottom";

	private int BORDER_SIZE = 13; // 命名方式不对，全大写应该用于不允许改变的常亮命名，而这个BORDER_SIZE在后面却可以设置。
									// xuyunyu
	private int RECENT_RUN_BORDER_SIZE = 0;

	private final int RECENT_RUN_INIT_X = 33;
	private final int RECENT_RUN_INIT_Y = 547;
	private final int RECENT_RUN_WIDTH = 242;
	private final int RECENT_RUN_HEIGHT = 173;

	// private static int BORDER_SIZE = 20;
	private int TRAN_DUR_ANIM = 100;// xuyunyu
	private int mAlphaLocationDuration = 100;
	private Context mContext;
	private int mLeft, mTop, mRight, mBottom;
	// 相对于偏离的View
	private View mDeviateView;

	// 点击效果结束后的回调监听器
	private OnClickEffectListener mOnClickEffectListener;
	// 移动结束后的监听
	private EndRunListener mEndRunListener;

	private boolean mCanMove = false;

	// 记录上一次的焦点组件，用于判断是否未移动控件的焦点，相同则不重新加载动画
	private View mLastFocusView;

	private String mLocationMark = "null";

	public interface EndRunListener {

		void onEndRunListener();
	}

	public interface OnClickEffectListener {
		// 点击效果结束后回调
		public void onClickEffectEnd();
	}

	public BorderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setBorderStyle(R.drawable.button_selected);
	}

	public BorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setBorderStyle(R.drawable.button_selected);
	}

	public BorderView(Context context) {
		super(context);
		mContext = context;
		setBorderStyle(R.drawable.button_selected);
	}

	public void setAvailable(boolean canmove) {

		mCanMove = canmove;
	}

	public boolean isAvailable() {

		return mCanMove;
	}

	@Override
	public void layout(int l, int t, int r, int b) {
		if (mCanMove) {
			super.layout(l, t, r, b);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		setBorderStyle(R.drawable.focus_clicked);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		setBorderStyle(R.drawable.button_selected);
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * 设置边界框的外框大小
	 *
	 * @param padding
	 */
	public void setBorderSize(int size) {
		BORDER_SIZE = size;
	}

	/**
	 * 设置位移动画时间
	 *
	 * @param dur
	 */
	public void setTranslateAnimtionDuration(int dur) {
		TRAN_DUR_ANIM = dur;
	}

	/**
	 * 设置透明定位动画的时间
	 */
	public void setAlphaLocationDuration(int dur) {
		mAlphaLocationDuration = dur;
	}

	/**
	 * 设置view的位置
	 */
	public void setLocation(View view) {

		ViewLocation location = findLocationWithView(view);
		if (null==location) {
			return;
		}
		mCanMove = true;
		mLeft = location.x - (int) BORDER_SIZE;
		mTop = location.y - (int) BORDER_SIZE;
		// TODO 像素
		mRight = location.x + (int) BORDER_SIZE + view.getWidth() - 105;
		mBottom = location.y + (int) BORDER_SIZE + view.getHeight();
		this.layout(mLeft, mTop, mRight, mBottom);
		mCanMove = false;
		// this.clearAnimation();
		// BorderView.this.setVisibility(View.VISIBLE);
	}

	public void setToIdle() {
		mCanMove = true;
		this.layout(0, 0, 0, 0);
		mCanMove = false;
		// this.clearAnimation();
		// BorderView.this.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置view的位置
	 */
	public void setLocation4Main(View view) {

		ViewLocation location = findLocationWithView(view);
		if (null==location) {
			return;
		}
		mCanMove = false;
		mLeft = location.x - (int) BORDER_SIZE;
		mTop = location.y - (int) BORDER_SIZE;
		// TODO 像素
		mRight = location.x + (int) BORDER_SIZE + view.getWidth();
		mBottom = location.y + (int) BORDER_SIZE + view.getHeight();
		this.layout(mLeft, mTop, mRight, mBottom);
		mCanMove = false;
		// this.clearAnimation();
		// BorderView.this.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置view的位置
	 */
	public void setLocation4Search(View view, View root) {


//		ViewLocation location = findLocationWithView(view);
//		ViewLocation root_location = findLocationWithView(root);
//		if (null==location||null==root_location) {
//			return;
//		}
//		mCanMove = false;
//		mLeft = location.x - (int) BORDER_SIZE - root_location.x;
//		mTop = location.y - (int) BORDER_SIZE - root_location.y;
//		// TODO 像素
//		mRight = location.x + (int) BORDER_SIZE + view.getWidth() - root_location.x;
//		mBottom = location.y + (int) BORDER_SIZE + view.getHeight() - root_location.y;
//
//		Trace.Info(mTop+"  "+mLeft);
//
//		// FrameLayout.LayoutParams lp = (LayoutParams)
//		// BorderView.this.getLayoutParams();
//		// lp.setMargins(mLeft, mTop, mRight, mBottom);
//		// BorderView.this.setLayoutParams(lp);
//		this.layout(mLeft, mTop, mRight, mBottom);
//		mCanMove = false;
//		// this.clearAnimation();
//		// BorderView.this.setVisibility(View.VISIBLE);
	}

	public void setLocation4MusicList(View view, View root) {
		ViewLocation location = findLocationWithView(view);
		ViewLocation root_location = findLocationWithView(root);
		if (null==location||null==root_location) {
			return;
		}
		mLeft = location.x - (int) BORDER_SIZE - root_location.x;
		mTop = location.y - (int) BORDER_SIZE - root_location.y;
		// TODO 像素
		mRight = location.x + (int) BORDER_SIZE + view.getWidth() - root_location.x;
		mBottom = location.y + (int) BORDER_SIZE + view.getHeight() - root_location.y;

		mCanMove = false;
		this.layout(mLeft, mTop, mRight, mBottom);
	}

	public void setLocation4RecentRunInit() {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		final int x = (int) (RECENT_RUN_INIT_X * scale + 0.5f);
		final int y = (int) (RECENT_RUN_INIT_Y * scale + 0.5f);
		final int w = (int) (RECENT_RUN_WIDTH * scale + 0.5f);
		final int h = (int) (RECENT_RUN_HEIGHT * scale + 0.5f);
		final int s = (int) (RECENT_RUN_BORDER_SIZE * scale + 0.5f);
		mLeft = x - s;
		mRight = x + s + w;
		mTop = y - s;
		mBottom = y + s + h;
		Trace.Debug("#####setLocation4RecentRunInit X: " + x + "  Y: " + y);
		Trace.Debug("#####setLocation4RecentRunInit W: " + w + "  H: " + h);
		this.layout(mLeft, mTop, mRight, mBottom);
		BorderView.this.setVisibility(View.VISIBLE);
	}

	public void setLocationInCoordinate(int x, int y, View view) {// xuyunyu
		Log.v(TAG, "setLocation X:" + x + " Y:" + y);

		mLeft = x - (int) BORDER_SIZE;
		mTop = y - (int) BORDER_SIZE;
		// TODO 像素
		mRight = x + (int) BORDER_SIZE + view.getWidth() - 105;
		mBottom = y + (int) BORDER_SIZE + view.getHeight();
		this.layout(mLeft, mTop, mRight, mBottom);
		this.clearAnimation();
		BorderView.this.setVisibility(View.VISIBLE);
	}

	public void setLocationInCoordinate(int x, int y, int width, int height) {
		Log.v(TAG, "setLocation X:" + x + " Y:" + y); // 建议只用一种打印log的方式 xuyunyu

		mLeft = x - (int) BORDER_SIZE;
		mTop = y - (int) BORDER_SIZE;
		mRight = x + (int) BORDER_SIZE + width;
		mBottom = y + (int) BORDER_SIZE + height;
		this.layout(mLeft, mTop, mRight, mBottom);
		this.clearAnimation();
		BorderView.this.setVisibility(View.VISIBLE);
	}

	// 重新布局回调
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (this.mLeft != left || mTop != top || mRight != right || mBottom != bottom) {
			this.layout(this.mLeft, this.mTop, this.mRight, this.mBottom);
		}
	}

	/**
	 * 获取View的位置
	 *
	 * @param view
	 *            获取的控件
	 * @return 位置
	 */
	public ViewLocation findLocationWithView(View view) {

		int[] location = new int[2];
		if (view == null) {
			return null;
		}
		// 这里坐标的定位要选择好，发现dialog不是全屏显示的时候getLocationOnScreen定位移动是会有偏差的
		// Dialog全屏显示的时候，getLocationOnScreen方法和getLocationInWindow方法获取到的坐标值是一致的
		// 若不全屏显示的时候，坐标值不一致的，所以对dialog类的窗体，最好使用getLocationInWindow方法来定位坐标
		// view.getLocationOnScreen(location);
		view.getLocationInWindow(location);
		return new ViewLocation(location[0], location[1]);
	}

	/**
	 * 设置焦点框的显示风格
	 */
	public void setBorderStyle(int id) {
		this.setBackgroundResource(id);
	}

	/**
	 * 启动焦点框位移和闪烁动画
	 *
	 * @param toView
	 *            焦点跳到的View
	 */
	public void runTranslateAndAlphaAnimation(View toView) { // 三个重载方法，存在大量重复代码，建议将重复代码抽取出来单独作为一个方法
																// xuyunyu

		if (toView == null || mLastFocusView == toView) {
			return;
		}

		// 设置透明、闪烁动画
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.6f, 1f);
		// 记录位置信息，以为启动动画前box已经设置到目标位置了
		ViewLocation fromLocation = findLocationWithView(this);
		ViewLocation toLocation = findLocationWithView(toView);
		Trace.Debug("from X: " + fromLocation.x);
		Trace.Debug("from X: " + fromLocation.y);
		Trace.Debug("to X: " + toLocation.x);
		Trace.Debug("to X: " + toLocation.y);
		// 设置平移动画
		TranslateAnimation tran = new TranslateAnimation(-toLocation.x + (float) BORDER_SIZE + fromLocation.x, 0,
				-toLocation.y + (float) BORDER_SIZE + fromLocation.y, 0);
		// TranslateAnimation tran = new
		// TranslateAnimation(-toLocation.x+(float)BORDER_SIZE+fromLocation.x,0,0,
		// 0);

		AnimationSet boxAnimaSet = new AnimationSet(true);
		boxAnimaSet.addAnimation(tran);
		boxAnimaSet.addAnimation(alphaAnimation);
		boxAnimaSet.setDuration(TRAN_DUR_ANIM);
		// BorderView.this.setVisibility(View.INVISIBLE);
		// setLocationDeviate(toView, mDeviateView);//先位移到目标位置再启动动画
		setLocation(toView);
		// boxAnimaSet.setAnimationListener(mTranslateAndAlphaAnimationListener);
		BorderView.this.startAnimation(boxAnimaSet);
		mLastFocusView = toView;
	}

	public void runTranslateAndAlphaAnimation(View toView, EndRunListener listener) {
		// 设置焦点框的显示风格
		// setBorderStyle(R.drawable.control_center_selected);
		if (toView == null) {
			return;
		}

		if (listener != null) {
			mEndRunListener = listener;
		}

		// 设置透明、闪烁动画
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.6f, 1f);
		// 记录位置信息，以为启动动画前box已经设置到目标位置了
		ViewLocation fromLocation = findLocationWithView(this);
		ViewLocation toLocation = findLocationWithView(toView);
		Trace.Debug("#####from X: " + fromLocation.x);
		Trace.Debug("#####from Y: " + fromLocation.y);
		Trace.Debug("#####to X: " + toLocation.x);
		Trace.Debug("#####to Y: " + toLocation.y);
		// 设置平移动画
		TranslateAnimation tran = new TranslateAnimation(-toLocation.x + (float) BORDER_SIZE + fromLocation.x, 0,
				-toLocation.y + (float) BORDER_SIZE + fromLocation.y, 0);
		;

		AnimationSet boxAnimaSet = new AnimationSet(true);
		boxAnimaSet.addAnimation(tran);
		boxAnimaSet.addAnimation(alphaAnimation);
		boxAnimaSet.setDuration(TRAN_DUR_ANIM);
		setLocation(toView);
		boxAnimaSet.setAnimationListener(mTranslateAndAlphaAnimationListener);
		BorderView.this.startAnimation(boxAnimaSet);
		mLastFocusView = toView;
	}

	public void runTranslateAndAlphaAnimation(View toView, int id, EndRunListener listener) {
		// 设置焦点框的显示风格
		setBorderStyle(id);
		if (toView == null || mLastFocusView == toView) {
			return;
		}

		if (listener != null) {
			mEndRunListener = listener;
		}

		// 设置透明、闪烁动画
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.6f, 1f);
		// 记录位置信息，以为启动动画前box已经设置到目标位置了
		ViewLocation fromLocation = findLocationWithView(this);
		ViewLocation toLocation = findLocationWithView(toView);

		Trace.Debug("#####from X: " + fromLocation.x);
		Trace.Debug("#####from Y: " + fromLocation.y);
		Trace.Debug("#####to X: " + toLocation.x);
		Trace.Debug("#####to Y: " + toLocation.y);
		// 设置平移动画
		TranslateAnimation tran = new TranslateAnimation(-toLocation.x + (float) BORDER_SIZE + fromLocation.x, 0,
				-toLocation.y + (float) BORDER_SIZE + fromLocation.y, 0);
		// TranslateAnimation tran = new
		// TranslateAnimation(-toLocation.x+(float)BORDER_SIZE+fromLocation.x,0,0,
		// 0);

		AnimationSet boxAnimaSet = new AnimationSet(true);
		boxAnimaSet.addAnimation(tran);
		boxAnimaSet.addAnimation(alphaAnimation);
		boxAnimaSet.setDuration(TRAN_DUR_ANIM);
		// BorderView.this.setVisibility(View.INVISIBLE);
		// setLocationDeviate(toView, mDeviateView);//先位移到目标位置再启动动画
		setLocation(toView);
		boxAnimaSet.setAnimationListener(mTranslateAndAlphaAnimationListener);
		BorderView.this.startAnimation(boxAnimaSet);
		mLastFocusView = toView;
	}

	/**
	 * 透明定位方法
	 */
	public void runAlphaLocateAnimation(View toView, EndRunListener listener) {

		if (listener != null) {
			mEndRunListener = listener;
		}

		if (toView == null || mLastFocusView == toView) {
			return;
		}

		// 设置透明、闪烁动画
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1f);

		AnimationSet boxAnimaSet = new AnimationSet(true);
		boxAnimaSet.addAnimation(alphaAnimation);
		boxAnimaSet.setDuration(mAlphaLocationDuration);
		// setLocation(toView);
		boxAnimaSet.setAnimationListener(mAlphaAnimationListener);
		BorderView.this.startAnimation(boxAnimaSet);
		mLastFocusView = toView;
	}

	/**
	 * 实现点击闪烁动画
	 *
	 * @param onClickEffectListener
	 *            点击闪烁动画结束的回调监听器
	 */
	public void runAlPhaAnimation(OnClickEffectListener onClickEffectListener) {
		mOnClickEffectListener = onClickEffectListener;
		// 设置焦点框的显示风格
		// setBorderStyle(R.drawable.control_center_selected);
		// 设置透明、闪烁动画
		AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.6f);
		alphaAnimation.setDuration(50);
		alphaAnimation.setAnimationListener(mClickAnimationListner);
		this.startAnimation(alphaAnimation);
	}

	public void setBorderLocationMark(String mark) {
		mLocationMark = mark;
	}

	public String getBorderLocationMark() {
		return mLocationMark;
	}

	private AnimationListener mClickAnimationListner = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			// 点击动画结束后就回调
			mOnClickEffectListener.onClickEffectEnd();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	};

	private AnimationListener mTranslateAndAlphaAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			// setLocation(mLastFocusView);
			mEndRunListener.onEndRunListener();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	};

	private AnimationListener mAlphaAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			mEndRunListener.onEndRunListener();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	};

	// 坐标位置类
	private class ViewLocation {
		public int x;
		public int y;

		public ViewLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}