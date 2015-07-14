package com.konka.eplay.modules.photo;

import java.lang.reflect.Field;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;

/**
 * 
 * Created on: 2015-6-15
 * 
 * @brief 可设置滚动切换时间的ViewPager
 * @author mcsheng
 * @date Latest modified on: 2015-6-15
 * @version V1.0.00
 * 
 */
public class CustomScrollViewPager extends ViewPager {

	private int mScrollDuration = 500;
	private Boolean mIsCanScroll = true;

	public CustomScrollViewPager(Context context) {
		super(context);
		init();

	}

	public CustomScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setScroll();
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

	public void setScrollDuration(int duration) {
		mScrollDuration = duration;
		setScroll();
	}

}