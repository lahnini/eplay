package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;
import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 适用于ViewPager的滚动Scroller，可以通过它来定制滚动时间和滚动监听</br> <b>创建时间：</b>2015-3-10
 * 
 * @author mcsheng
 */
public class ViewPagerScroller extends Scroller {

	private static final String TAG = "ViewPagerScroller";
	private int mSrollDuration = 500;
	private ScrollListener mScrollListener;

	public ViewPagerScroller(Context context) {
		super(context);
	}

	public ViewPagerScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
	}

	public ViewPagerScroller(Context context, Interpolator interpolator,
					boolean flywheel) {
		super(context, interpolator, flywheel);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy) {
		super.startScroll(startX, startY, dx, dy, mSrollDuration);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		super.startScroll(startX, startY, dx, dy, mSrollDuration);
	}

	/**
	 * 滚动的时候，computeScrollOffset方法就会被调用
	 */
	@Override
	public boolean computeScrollOffset() {
		boolean hr = super.computeScrollOffset();
		Trace.Debug("####computeScrollOffset isFinished = " + isFinished()
						+ ", mScrollListener = " + mScrollListener);
		if (mScrollListener != null) {
			// 正在滚动回调
			mScrollListener.scrolling();
		}

		if (isFinished()) {
			if (mScrollListener != null) {
				// 滚动结束回调
				mScrollListener.endScroll();
			}
		}
		return hr;
	}

	/**
	 * 获取滚动时间
	 */
	public int getSrollDuration() {
		return mSrollDuration;
	}

	/**
	 * 设置滚动时间
	 */
	public void setScrollDuration(int duration) {
		mSrollDuration = duration;
	}

	/**
	 * 设置滚动监听器
	 */
	public void setScrollListener(ScrollListener l) {
		mScrollListener = l;
	}

	/**
	 * 自定义的滚动监听接口
	 */
	public interface ScrollListener {
		public void startScroll();

		public void scrolling();

		public void endScroll();
	}
}