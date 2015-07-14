package com.konka.eplay.modules;

import iapp.eric.utils.base.Trace;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.R;
import com.konka.eplay.model.LocalProvider;
import com.konka.eplay.modules.movie.MovieActivity;
import com.konka.eplay.modules.photo.PhotoActivity;

/**
 * @ClassName: LoadingDialog
 * @Description: 文件加载中的提示框
 * @author xuyunyu
 * @date 2015年5月20日 上午9:21:51
 * @version 1.0
 *
 */
public class LoadingDialog extends Dialog {
	private Activity mActivity;
	private Context mContext;
	private ImageView mLoadingImageView;
	private TextView mLoadingTextView;
	private View mContentView;

	public LoadingDialog(Context context) {
		super(context);
		mContext = context;
	}

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
		mActivity=(Activity)context;
		mContext = context;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mContentView = inflater.inflate(R.layout.progress_dialog, null);

		mLoadingImageView = (ImageView) mContentView
						.findViewById(R.id.music_dialog_loading_image);
		mLoadingTextView = (TextView) mContentView
						.findViewById(R.id.music_dialog_loading_text);
		mLoadingImageView.setBackgroundResource(R.drawable.downloading_anim);
	}

	public LoadingDialog(Context context, boolean cancelable,
					OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		mContext = context;

		LayoutInflater inflater = LayoutInflater.from(mContext);
		mContentView = inflater.inflate(R.layout.progress_dialog, null);

		mLoadingImageView = (ImageView) mContentView
						.findViewById(R.id.music_dialog_loading_image);
		mLoadingTextView = (TextView) mContentView
						.findViewById(R.id.music_dialog_loading_text);
		mLoadingImageView.setBackgroundResource(R.drawable.downloading_anim);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(mContentView);
		setCancelable(true);
		setCanceledOnTouchOutside(true);

	}

	@Override
	public void show() {
		super.show();
		if (null!=mLoadingImageView) {
			AnimationDrawable anim = (AnimationDrawable) mLoadingImageView
					.getBackground();
	        anim.start();
		}

	}

	@Override
	public void hide() {
		if (null != mLoadingImageView
						&& mLoadingImageView.getBackground() != null
						&& mLoadingImageView.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mLoadingImageView
							.getBackground();
			if (anim != null && anim.isRunning()) { // 如果正在运行,就停止
				anim.stop();
			}
		}
		super.hide();
	}

	public void setMessageText(String message) {
		if (null != mLoadingTextView) {
			mLoadingTextView.setText(message);
		}

	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
			Trace.Info("######### KEYCODE_BACK");
			this.dismiss();
//			switch (Configuration.curMediaType) {
//			case MMT_MOVIE:
//				break;
//			case MMT_PHOTO:
			LocalProvider.stopScan();
			mActivity.finish();
			
//				break;
//			case MMT_MUSIC:
//				break;
//			default:
//				break;
//			}

		}
		return false;
	}
}
