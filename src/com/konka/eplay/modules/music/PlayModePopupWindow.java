package com.konka.eplay.modules.music;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.konka.eplay.R;
import com.konka.eplay.Utils;

/**
 * @ClassName: PlayModePopupWindow
 * @Description: 音乐播放器播放模式选择弹出框
 * @author xuyunyu
 * @date 2015年4月13日 上午10:13:25
 * @version 1.0
 *
 */
public class PlayModePopupWindow extends PopupWindow {

	private View mContentView;
	private Context mContext;
	private OnPlayModeContentChangeListenner mModeContentChangeListenner;
	private RelativeLayout mLayout_single;
	private RelativeLayout mLayout_circle;
	private RelativeLayout mLayout_random;
	private ButtonClickListener mButtonClickListener;
	private ButtonModeOnkeyListener mModeOnkeyListener;

	public interface OnPlayModeContentChangeListenner {

		public void onChangeContent(int rsID, int playmode);

		public void onChangeFocus(boolean isRight);

	}

	public void setModeContentChangeListenner(OnPlayModeContentChangeListenner modeContentChangeListenner) {
		this.mModeContentChangeListenner = modeContentChangeListenner;
	}

	public PlayModePopupWindow(Context context) {
		super(context);

		mButtonClickListener = new ButtonClickListener();
		mModeOnkeyListener = new ButtonModeOnkeyListener();
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = inflater.inflate(R.layout.music_playmode_layout, null);

		mLayout_circle = (RelativeLayout) mContentView.findViewById(R.id.playmode_circle);
		mLayout_single = (RelativeLayout) mContentView.findViewById(R.id.playmode_single);
		mLayout_random = (RelativeLayout) mContentView.findViewById(R.id.playmode_random);

		mLayout_circle.setOnClickListener(mButtonClickListener);
		mLayout_random.setOnClickListener(mButtonClickListener);
		mLayout_single.setOnClickListener(mButtonClickListener);

		mLayout_circle.setOnKeyListener(mModeOnkeyListener);
		mLayout_random.setOnKeyListener(mModeOnkeyListener);
		mLayout_single.setOnKeyListener(mModeOnkeyListener);

		this.setContentView(mContentView);
		this.setWidth(Utils.dip2px(mContext, 150));
		this.setHeight(Utils.dip2px(mContext, 160));
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.update();

		// 实例化一个ColorDrawable颜色为透明
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
		this.setBackgroundDrawable(drawable);
		this.setAnimationStyle(R.style.MusicSettingPopupAnimation);

	}

	class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.playmode_circle:
				mModeContentChangeListenner.onChangeContent(R.drawable.music_mode_circle,
						MusicPlayerService.MUSIC_SERVICE_PLAYMODE_CIRCLE);
				dismiss();
				break;
			case R.id.playmode_single:
				mModeContentChangeListenner.onChangeContent(R.drawable.music_mode_single,
						MusicPlayerService.MUSIC_SERVICE_PLAYMODE_SINGLE);
				dismiss();
				break;
			case R.id.playmode_random:
				mModeContentChangeListenner.onChangeContent(R.drawable.music_mode_random,
						MusicPlayerService.MUSIC_SERVICE_PLAYMODE_RANDOM);
				dismiss();
				break;

			default:
				break;
			}

		}

	}

	class ButtonModeOnkeyListener implements OnKeyListener {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean handled = false;
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (isShowing()) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_RIGHT:
						mModeContentChangeListenner.onChangeFocus(true);
						dismiss();
						handled = true;
						break;
					case KeyEvent.KEYCODE_DPAD_LEFT:
						mModeContentChangeListenner.onChangeFocus(false);
						dismiss();
						handled = true;
						break;

					default:
						break;
					}
				}
			}
			return handled;
		}

	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		mLayout_random.requestFocus();
	}

}
