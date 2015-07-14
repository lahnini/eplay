/**
 * @Title: LikeButton.java
 * @Package com.konka.eplay.modules.music
 * @Description: TODO(用一句话描述该文件做什么)
 * @author A18ccms A18ccms_gmail_com
 * @date 2015年5月5日 下午3:22:34
 * @version
 */
package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import com.adobe.xmp.impl.Utils;
import com.konka.eplay.R;
import com.konka.eplay.modules.music.AllSongListFragment.ViewHolderForAudio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * @ClassName: LikeButton
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 */
public class LikeButton extends RelativeLayout {

	private int mLeft, mTop;
	private int BORDER_SIZE = 0;

	private int mNextLeft, mNextTop;
	private boolean mNextLike;
	private boolean mHasNext = false;
	private boolean mAnimationOut = true;

	// boolean can = true;

	public LikeButton(Context context) {
		super(context);
	}

	public LikeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LikeButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isAnimationOut() {
		return mAnimationOut;
	}

	public void setAnimationOutState(boolean isout) {
		mAnimationOut = isout;
	}

	/**
	 * 设置view的位置
	 */
	public void setLocation(View view, View root, View selectionView) {
		// can = true;
		ViewLocation location = findLocationWithView(view);
		ViewLocation root_location = findLocationWithView(root);
		if (view==null) {
			return;
		}
		if (view.getTag() == null) {
			int distance = (185 - selectionView.getHeight()) / 2;
			mLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
			mTop = location.y - (int) BORDER_SIZE - root_location.y - distance;
		} else {
			if (view.getTag() instanceof com.konka.eplay.modules.music.AllSongListFragment.ViewHolderForAudio) {
				Trace.Info("ViewHolderForAudio");
				mLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
				mTop = location.y - (int) BORDER_SIZE - root_location.y - 52;
			} else if (view.getTag() instanceof com.konka.eplay.modules.music.MusicSecondListActivity.ViewHolderForAudio) {
				mLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
				mTop = location.y - (int) BORDER_SIZE - root_location.y - 52;
			} else {
				int distance = (185 - selectionView.getHeight()) / 2;
				mLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
				mTop = location.y - (int) BORDER_SIZE - root_location.y - distance;
			}
		}

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(185, 185);
		lp.leftMargin = mLeft;
		lp.topMargin = mTop;
		this.setLayoutParams(lp);
		if (mHasNext) {
			mHasNext = false;
		}

		// this.onLayout(true,mLeft, mTop, mRight, mBottom);
		// can = false;
		// this.clearAnimation();
		// BorderView.this.setVisibility(View.VISIBLE);
	}

	public void refreshLocation(View view, View root, int height) {
		// can = true;
		ViewLocation location = findLocationWithView(view);
		ViewLocation root_location = findLocationWithView(root);
		if (view.getTag() instanceof com.konka.eplay.modules.music.AllSongListFragment.ViewHolderForAudio) {
			Trace.Info("ViewHolderForAudio");
			mLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
			mTop = location.y - (int) BORDER_SIZE - root_location.y - 52;
		} else if (view.getTag() instanceof com.konka.eplay.modules.music.MusicSecondListActivity.ViewHolderForAudio) {
			mLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
			mTop = location.y - (int) BORDER_SIZE - root_location.y - 52;
		} else {
			int distance = (185 - height) / 2;
			mLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
			mTop = location.y - (int) BORDER_SIZE - root_location.y - distance;
		}

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(185, 185);
		lp.leftMargin = mLeft;
		lp.topMargin = mTop;
		this.setLayoutParams(lp);
		if (mHasNext) {
			mHasNext = false;
		}

		// this.onLayout(true,mLeft, mTop, mRight, mBottom);
		// can = false;
		// this.clearAnimation();
		// BorderView.this.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置view的位置
	 */
	public void setNextLocation(View view, View root, View selectionView, boolean islike) {

		ViewLocation location = findLocationWithView(view);
		ViewLocation root_location = findLocationWithView(root);
		if (view.getTag() instanceof ViewHolderForAudio) {
			Trace.Info("ViewHolderForAudio");
			mNextLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
			mNextTop = location.y - (int) BORDER_SIZE - root_location.y - 52;
		} else {
			int distance = (185 - selectionView.getHeight()) / 2;
			mNextLeft = location.x - (int) BORDER_SIZE - root_location.x + view.getWidth() - 185 + 12;
			mNextTop = location.y - (int) BORDER_SIZE - root_location.y - distance;
		}

		mNextLike = islike;
		mHasNext = true;
	}

	public void getNextLocation(ImageView imageView) {
		if (mHasNext) {
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(185, 185);
			lp.leftMargin = mNextLeft;
			lp.topMargin = mNextTop;
			this.setLayoutParams(lp);
			if (mNextLike) {
				imageView.setImageResource(R.drawable.musicplayer_islike_yes);
			} else {
				imageView.setImageResource(R.drawable.musicplayer_islike_no);
			}
			mHasNext = false;
		}
	}

	public ViewLocation findLocationWithView(View view) {

		int[] location = new int[2];
		if (view == null) {
			return new ViewLocation(location[0], location[1]);
		}
		// 这里坐标的定位要选择好，发现dialog不是全屏显示的时候getLocationOnScreen定位移动是会有偏差的
		// Dialog全屏显示的时候，getLocationOnScreen方法和getLocationInWindow方法获取到的坐标值是一致的
		// 若不全屏显示的时候，坐标值不一致的，所以对dialog类的窗体，最好使用getLocationInWindow方法来定位坐标
		// view.getLocationOnScreen(location);
		view.getLocationInWindow(location);
		return new ViewLocation(location[0], location[1]);
	}

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
