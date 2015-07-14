package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;
import android.os.HandlerThread;
import android.os.Message;

/**
 * 
 * Created on: 2014-4-17
 * 
 * @brief 专门处理截图的消息线程
 * @author Eric Fung
 * @version V1.0.00
 * 
 */
class CaptureHandlerThread extends HandlerThread implements android.os.Handler.Callback {

	private MoviePlayerActivity mContext = null;

	public CaptureHandlerThread(String name, MoviePlayerActivity context) {
		super(name);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		Trace.Info("CaptureHandlerThread handleMessage");

		try {
			String playingPath = mContext.getCurrPlayingPath();
			if (mContext != null && playingPath != null) {
				ScreenCapture sc = new ScreenCapture(mContext, playingPath);
				sc.takePicture(320, 180);
			}
			else {
				return true;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}