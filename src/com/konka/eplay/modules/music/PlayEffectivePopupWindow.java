/**
 * @Title: PlayEffectivePopupWindow.java
 * @Package com.konka.eplay.modules.music
 * @Description: TODO(用一句话描述该文件做什么)
 * @author xuyunyu
 * @date 2015年4月13日 下午3:34:01
 * @version
 */
package com.konka.eplay.modules.music;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.Utils;

/**
 * @ClassName: PlayEffectivePopupWindow
 * @Description: 音乐音效选择的弹出框
 * @author xuyunyu
 * @date 2015年4月13日 下午3:34:01
 * @version 1.0
 *
 */
public class PlayEffectivePopupWindow extends PopupWindow {

	private View mContentView;
	private Context mContext;
	private OnEffectiveChangeListenner mChangeListenner;
	private ButtonEffectiveClickListener mButtonEffectiveClickListener;
	private ButtonEffectiveOnkeyListener mButtonEffectiveOnkeyListener;
	private Button mViewNormal;
	private Button mViewFolk;
	private Button mViewDance;
	private Button mViewMetal;
	private Button mViewJazz;
	private Button mViewRock;

	public interface OnEffectiveChangeListenner {

		public void onEffectiveChange(String effective, short preset);

		public void onChangeFocus(boolean isRight);

	}

	public void setmChangeListenner(OnEffectiveChangeListenner mChangeListenner) {
		this.mChangeListenner = mChangeListenner;
	}

	public PlayEffectivePopupWindow(Context context) {
		super(context);

		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = inflater.inflate(R.layout.music_effective_layout, null);

		this.setContentView(mContentView);
		this.setWidth(Utils.dip2px(mContext, 120));
		this.setHeight(Utils.dip2px(mContext, 270));
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.setClippingEnabled(false);

		this.update();
		Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
		this.setBackgroundDrawable(drawable);

		this.setAnimationStyle(R.style.MusicSettingPopupAnimation);
		mButtonEffectiveClickListener = new ButtonEffectiveClickListener();
		mButtonEffectiveOnkeyListener = new ButtonEffectiveOnkeyListener();

		mViewNormal = (Button) mContentView.findViewById(R.id.normal);
		mViewFolk = (Button) mContentView.findViewById(R.id.folk);
		mViewDance = (Button) mContentView.findViewById(R.id.dance);
		mViewMetal = (Button) mContentView.findViewById(R.id.mental);
		mViewJazz = (Button) mContentView.findViewById(R.id.jazz);
		mViewRock = (Button) mContentView.findViewById(R.id.rock);

		mViewNormal.setOnClickListener(mButtonEffectiveClickListener);
		mViewFolk.setOnClickListener(mButtonEffectiveClickListener);
		mViewDance.setOnClickListener(mButtonEffectiveClickListener);
		mViewMetal.setOnClickListener(mButtonEffectiveClickListener);
		mViewJazz.setOnClickListener(mButtonEffectiveClickListener);
		mViewRock.setOnClickListener(mButtonEffectiveClickListener);

		mViewNormal.setOnKeyListener(mButtonEffectiveOnkeyListener);
		mViewFolk.setOnKeyListener(mButtonEffectiveOnkeyListener);
		mViewDance.setOnKeyListener(mButtonEffectiveOnkeyListener);
		mViewMetal.setOnKeyListener(mButtonEffectiveOnkeyListener);
		mViewJazz.setOnKeyListener(mButtonEffectiveOnkeyListener);
		mViewRock.setOnKeyListener(mButtonEffectiveOnkeyListener);

	}

	class ButtonEffectiveClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.normal:
				mChangeListenner.onEffectiveChange(MusicUtils.getResourceString(mContext,R.string.music_effective_nornal), (short) 0);
				dismiss();
				break;
			case R.id.folk:
				mChangeListenner.onEffectiveChange(MusicUtils.getResourceString(mContext,R.string.music_effective_folk), (short) 4);
				dismiss();
				break;
			case R.id.dance:
				mChangeListenner.onEffectiveChange(MusicUtils.getResourceString(mContext,R.string.music_effective_dance), (short) 2);
				dismiss();
				break;
			case R.id.mental:
				mChangeListenner.onEffectiveChange(MusicUtils.getResourceString(mContext,R.string.music_effective_metal), (short) 5);
				dismiss();
				break;
			case R.id.jazz:
				mChangeListenner.onEffectiveChange(MusicUtils.getResourceString(mContext,R.string.music_effective_jazz), (short) 8);
				dismiss();
				break;
			case R.id.rock:
				mChangeListenner.onEffectiveChange(MusicUtils.getResourceString(mContext,R.string.music_effective_rock), (short) 9);
				dismiss();
				break;

			default:
				break;
			}

		}

	}

	class ButtonEffectiveOnkeyListener implements OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean handled = false;
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (isShowing()) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_RIGHT:
						mChangeListenner.onChangeFocus(true);
						dismiss();
						handled = true;
						break;
					case KeyEvent.KEYCODE_DPAD_LEFT:
						mChangeListenner.onChangeFocus(false);
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

}
