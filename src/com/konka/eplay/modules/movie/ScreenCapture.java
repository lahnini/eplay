package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;

import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.KKCommonManager.EN_KK_CAPTURE_MODE;
import com.konka.android.tv.common.KKTVCamera.TakePictureCallback;
import com.konka.eplay.Constant;
import com.konka.eplay.Utils;

public class ScreenCapture {
	private String mPath = null;

	private Context mContxt = null;
	// 默认存储路径
	private String mSaveDir = "/mnt/usb/sda1" + "/" + Constant.APP_DIR + "/" + ThumbnailLoader.DIR;

	public ScreenCapture(Context context, String path) {
		mPath = path;
		mContxt = context;
		// 更新存储路径
		mSaveDir = Utils.getRootPath(mPath) + "/" + Constant.APP_DIR + "/" + ThumbnailLoader.DIR;
	}

	/**
	 * @brief 判断视频对应的截图是否已经存储过
	 * @return 存在-true，不存在-false
	 */
	private boolean isFileExist() {
		File dir = new File(mSaveDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String file_name = Utils.Md5(Utils.getSubPath(mPath));
		File file = new File(mSaveDir + "/" + file_name);
		if (file.exists()) {
			return true;
		}

		return false;
	}

	/**
	 * 拍照
	 * 
	 * @param width
	 *            照片的宽度
	 * @param height
	 *            照片的高度
	 * @return true 执行成功；false 执行失败
	 * @hide
	 */
	public void takePicture(int width, int height) {
		Trace.Info("###lhq 开始屏幕截图");

		if (isFileExist()) {
			Trace.Info("###lhq 截图已存储，不进行截图");
			return;
		}
		if (KKCommonManager.getInstance(mContxt).getPlatform().equals("rtd2995d")) {
			width = 3840;
			height = 2160;
			KKCommonManager.getInstance(mContxt).takePictureofTV(width, height, new TakePictureCallback() {
				@Override
				public void onPictureTaken(Bitmap arg0) {
					Trace.Info("###lhq 收到截图回调");
					if (arg0 != null) {
						Trace.Info("###lhq 截图成功返回截图结果");
						saveScreenshot(arg0);
					}
				}
			});
		}
		else {
			KKCommonManager.getInstance(mContxt).takePictureofTV(width, height, new TakePictureCallback() {
				@Override
				public void onPictureTaken(Bitmap arg0) {
					Trace.Info("###lhq 收到截图回调");
					if (arg0 != null) {
						Trace.Info("###lhq 截图成功返回截图结果");
						saveScreenshot(arg0);
					}
				}
			}, EN_KK_CAPTURE_MODE.CURRENT_VIDEO);
		}
	}

	public int saveScreenshot(Bitmap bitmap) {
		if (isFileExist()) {
			Trace.Info("###lhq 截图已存储，不进行保存");
			return 0;
		}

		try {
			String file_name = Utils.Md5(Utils.getSubPath(mPath));
			String path = mSaveDir + "/" + file_name;
			Trace.Info("###lhq video pic save path" + path);
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

}
