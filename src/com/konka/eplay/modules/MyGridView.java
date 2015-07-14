package com.konka.eplay.modules;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class MyGridView extends GridView {
	private int position = 0;

	public boolean isOnMeasure;

	public MyGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChildrenDrawingOrderEnabled(true);
	}

	@Override
	protected void setChildrenDrawingOrderEnabled(boolean enabled) {
		super.setChildrenDrawingOrderEnabled(enabled);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		position = getSelectedItemPosition() - getFirstVisiblePosition();
		if (position < 0) {
			return i;
		} else {
			if (i == childCount - 1) {
				// 这是�?后一个需要刷新的item
				if (position > i) {
					position = i;
				}
				return position;
			}
			if (i == position) {
				// 这是原本要在�?后一个刷新的item
				return childCount - 1;
			}
		}
		return i;
	}



	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		isOnMeasure = true;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		isOnMeasure = false;
		super.onLayout(changed, l, t, r, b);
	}
//	public int getPosition(){
//		return position;
//	}
}
