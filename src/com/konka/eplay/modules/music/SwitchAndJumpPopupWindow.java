package com.konka.eplay.modules.music;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.R;
import com.konka.eplay.modules.movie.MovieActivity;
import com.konka.eplay.modules.photo.PhotoActivity;

/**
 * @ClassName: SwitchAndJumpPopupWindow
 * @Description: 控制跳转到视频或者图片浏览页面
 * @author xuyunyu
 * @date 2015年3月24日 下午2:32:42
 * @version V1.0
 * 
 */
public class SwitchAndJumpPopupWindow extends PopupWindow {

	View mContentView;
	Activity mContext;

	FrameLayout mSwitchPhoto;
	FrameLayout mSwitchPhotoBg;
	FrameLayout mSwitchVideo;
	FrameLayout mSwitchVideoBg;
	SwitchButtonKeyListener mButtonKeyListener;
	SwitchButtonClickListener mButtonClickListener;
	SwitchButtonFocusChangeListener mButtonFocusChangeListener;
	Animation mAnimationExtend;
	Animation mAnimationNarrow;

	public SwitchAndJumpPopupWindow(Activity context) {
		super(context);
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = inflater.inflate(R.layout.music_switch_jump, null);
		this.setContentView(mContentView);
		this.setWidth(400);
		this.setHeight(400);
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.setClippingEnabled(false);
		// 刷新状态
		this.update();
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		this.setBackgroundDrawable(dw);
		this.setAnimationStyle(R.style.SwitchPopupAnimation);

		mButtonKeyListener = new SwitchButtonKeyListener();
		mButtonClickListener = new SwitchButtonClickListener();
		mButtonFocusChangeListener = new SwitchButtonFocusChangeListener();

		mSwitchPhoto = (FrameLayout) mContentView
						.findViewById(R.id.music_switch_photo);
		mSwitchPhotoBg = (FrameLayout) mContentView
						.findViewById(R.id.music_switch_photo_bg);
		mSwitchVideo = (FrameLayout) mContentView
						.findViewById(R.id.music_switch_video);
		mSwitchVideoBg = (FrameLayout) mContentView
						.findViewById(R.id.music_switch_video_bg);
		mSwitchPhoto.setOnKeyListener(mButtonKeyListener);
		mSwitchVideo.setOnKeyListener(mButtonKeyListener);

		mSwitchPhoto.setOnClickListener(mButtonClickListener);
		mSwitchVideo.setOnClickListener(mButtonClickListener);

		mSwitchPhoto.setOnFocusChangeListener(mButtonFocusChangeListener);
		mSwitchVideo.setOnFocusChangeListener(mButtonFocusChangeListener);

	}

	class SwitchButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.music_switch_photo:
			
				Configuration.curMediaType = MultimediaType.MMT_PHOTO;
				Intent intent_photo = new Intent();
				intent_photo.setClass(mContext, PhotoActivity.class);
				mContext.startActivity(intent_photo);
				dismiss();
				break;
			case R.id.music_switch_video:
			
				Configuration.curMediaType = MultimediaType.MMT_MOVIE;
				Intent intent_video = new Intent();
				intent_video.setClass(mContext, MovieActivity.class);
				mContext.startActivity(intent_video);
				dismiss();
				break;

			default:
				break;
			}

		}

	}

	class SwitchButtonKeyListener implements OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean handled = false;
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_MENU
								|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					dismiss();
					handled = true;
				}
			}
			return handled;
		}

	}

	class SwitchButtonFocusChangeListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				if (null == mAnimationExtend) {
					mAnimationExtend = AnimationUtils.loadAnimation(mContext,
									R.anim.switch_extend);
				}
				switch (v.getId()) {
				case R.id.music_switch_photo:

					mSwitchPhoto.startAnimation(mAnimationExtend);
					mSwitchPhotoBg.startAnimation(mAnimationExtend);
					break;

				case R.id.music_switch_video:
					mSwitchVideo.startAnimation(mAnimationExtend);
					mSwitchVideoBg.startAnimation(mAnimationExtend);
					break;

				default:
					break;
				}
			} else {
				if (null == mAnimationNarrow) {
					mAnimationNarrow = AnimationUtils.loadAnimation(mContext,
									R.anim.switch_narrow);
				}
				switch (v.getId()) {
				case R.id.music_switch_photo:

					mSwitchPhoto.startAnimation(mAnimationNarrow);
					mSwitchPhotoBg.startAnimation(mAnimationNarrow);
					break;

				case R.id.music_switch_video:
					mSwitchVideo.startAnimation(mAnimationNarrow);
					mSwitchVideoBg.startAnimation(mAnimationNarrow);
					break;

				default:
					break;
				}
			}
		}

	}

}
