/**
 * @Title: SettingPopupWindow.java
 * @Package com.konka.eplay.modules
 * @Description: TODO(用一句话描述该文件做什么)
 * @date 2015年4月28日 下午8:04:49
 * @version
 */
package com.konka.eplay.modules;

import iapp.eric.utils.base.FileOperations;
import iapp.eric.utils.base.Trace;
import iapp.eric.utils.metadata.Result;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.GlobalData;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.model.LocalDiskInfo;
import com.konka.eplay.modules.movie.PlayRecordHelper;
import com.konka.eplay.modules.photo.ImageLoader;

/**
 * @ClassName: SettingPopupWindow
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年4月28日 下午8:04:49
 * @version
 *
 */
public class SettingPopupWindow extends PopupWindow {

	View mContentView;
	Activity mContext;
	RelativeLayout mSortView;
	Button mCleanView;
	Button mVersionView;
	TextView mSortType;
	ImageView mArraow_left;
	ImageView mArraow_right;
	SortType mCurrentType;

	private Thread mCleanThread = null;

	private static final int CLEAN_CACHE_SUCESS = 1;
	private static final int CLEAN_CACHE_fail = 2;
	SettingClickListener mClickListener;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CLEAN_CACHE_SUCESS:
				VersionToast.showToast(mContext, "已清理完成");
				mCleanThread = null;
				break;
			case CLEAN_CACHE_fail:
				mCleanThread = null;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public SettingPopupWindow(final Activity context) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = inflater.inflate(R.layout.setting_dialog, null);
		mContext = context;

		this.setContentView(mContentView);
		// TODO 像素换成dp 原来基础上+15
		this.setWidth(300);
		this.setHeight(315);

		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.setClippingEnabled(false);
		this.update();
		Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
		this.setBackgroundDrawable(drawable);
		this.setAnimationStyle(R.style.SettingPopupAnimation);
		mSortView = (RelativeLayout) mContentView.findViewById(R.id.setting_sort);
		mCleanView = (Button) mContentView.findViewById(R.id.setting_clean);
		mVersionView = (Button) mContentView.findViewById(R.id.setting_version);
		mSortType = (TextView) mContentView.findViewById(R.id.setting_sorttype);
		mArraow_left = (ImageView) mContentView.findViewById(R.id.setting_arrow_left);
		mArraow_right = (ImageView) mContentView.findViewById(R.id.setting_arrow_right);

		if (Configuration.sortType == SortType.ST_BY_NAME) {
			mSortType.setText(mContext.getResources().getString(R.string.sortname));
		}else {
			mSortType.setText(mContext.getResources().getString(R.string.sortauto));
		}

		mCurrentType = Configuration.sortType;

		mClickListener = new SettingClickListener();
		mCleanView.setOnClickListener(mClickListener);
		mVersionView.setOnClickListener(mClickListener);
		mSortView.setOnClickListener(mClickListener);

		mSortView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				//		Trace.Info(mSortType.getText().toString().trim());
						ImageLoader.clearCache();
						if (mSortType.getText().toString().trim()
								.equals(mContext.getResources().getString(R.string.sortauto))) {
							mSortType.setText(mContext.getResources().getString(R.string.sortname));
							Configuration.sortType = SortType.ST_BY_NAME;
							Configuration.save(null, MainActivity.SORTATION_SETTING_KEY, SortType.ST_BY_NAME.toString());
						} else if (mSortType.getText().toString().trim()
								.equals(mContext.getResources().getString(R.string.sortname))) {
							mSortType.setText(mContext.getResources().getString(R.string.sortauto));
							Configuration.sortType = SortType.ST_BY_TIME;
							Configuration.save(null, MainActivity.SORTATION_SETTING_KEY, SortType.ST_BY_TIME.toString());
						}
					}
				}
				return false;
			}
		});

		mSortView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mArraow_left.setVisibility(View.VISIBLE);
					mArraow_right.setVisibility(View.VISIBLE);
				} else {
					mArraow_left.setVisibility(View.INVISIBLE);
					mArraow_right.setVisibility(View.INVISIBLE);
				}

			}
		});
	}


	@Override
	public void dismiss() {
		if (Configuration.sortType == mCurrentType) {
			((GlobalData)mContext.getApplication()).isSortTypeChange = false;
		}else {
			((GlobalData)mContext.getApplication()).isSortTypeChange = true;
		}
		super.dismiss();
	}

	@Override
	public void showAsDropDown(View anchor, int xoff, int yoff) {
		mCurrentType = Configuration.sortType;
		super.showAsDropDown(anchor, xoff, yoff);
	}

	class CleanCacheRun implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// delete cached files
			boolean ret1 = deleteCachedFiles(mContext.getApplicationContext());
			// delete /mnt/usb/sda1/kk.multimedia
			boolean ret2 = deleteUsbFiles(mContext.getApplicationContext());
			PlayRecordHelper playRecordHelper = new PlayRecordHelper(mContext);
			playRecordHelper.clearPlayRecord();
			if (ret1 || ret2) {
				mHandler.sendEmptyMessage(CLEAN_CACHE_SUCESS);
			} else {
				mHandler.sendEmptyMessage(CLEAN_CACHE_fail);
			}
		}

	}

	private boolean deleteCachedFiles(Context context) {
		File cachedDir = context.getCacheDir();
		Result subRet = deleteFiles(cachedDir);
		if (subRet == null) {
			return true;
		}
		if (subRet.type == Result.TYPE.SUCCESS)
			return true;
		else
			return false;
	}

	private boolean deleteUsbFiles(Context context) {
		Trace.Debug("##### getExternalStorage()");
		ArrayList<LocalDiskInfo> usbList = Utils.getExternalStorage(context);
		int size = usbList.size();
		for (int i = 0; i < size; i++) {
			String rootPath = usbList.get(i).getPath();
			String appDirPath = rootPath + "/" + Constant.APP_DIR;
			File appDir = new File(appDirPath);
			Result subRet = deleteFiles(appDir);
			if (null == subRet) {
				continue;
			}
		}
		return true;
	}

	// 递归删除文件
	private Result deleteFiles(File file) {
		FileOperations fo = new FileOperations(Runtime.getRuntime());
		try {
			return fo.getLinuxShell().doDelete(file.getAbsolutePath());
		} catch (Exception e) {

		}
		return null;
	}

	class SettingClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.setting_sort:
				dismiss();
				break;
			case R.id.setting_clean:
				if (mCleanThread == null) {
					mCleanThread = new Thread(new CleanCacheRun());
					mCleanThread.start();
				} else {
					VersionToast.showToast(mContext, "清理中，请耐心等待");
				}
				dismiss();
				break;
			case R.id.setting_version:
				VersionToast.showToast(mContext, Utils.getVersion(mContext));
				dismiss();
				break;

			default:
				break;
			}

		}

	}

}
