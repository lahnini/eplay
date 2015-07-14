package com.konka.eplay.services;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;

/**
 * 
 * Created on: 2014年8月29日
 * 
 * @brief 基本的计时服务，方便快速开发 不同的计时任务，添加一套startXXXXTimer、stopXXXXTimer、class
 *        XXXXTask，然后在TerminateTimer中添加
 * @author Eric Fung
 * @date Latest modified on: 2014年8月29日
 * @version V1.0.00
 * 
 */
public class MainTimerService {
	private Handler mMainHandler = null; // 主线程的handler，方便计时到了做某些处理
	private Timer mTimer = null;
	private Context mContext = null;

	private XXXXTask mXXXXTask = null;
	private final static int POLL_INTERVAL = 10 * 60 * 1000;// 10分钟，单位毫秒

	public MainTimerService(Context context, Handler handler) {
		mMainHandler = handler;
		mTimer = new Timer(true);
		mContext = context;
	}

	/**
	 * @brief 销毁计时服务，包括所有任务
	 */
	public void TerminateTimer() {
		if (null != mTimer) {
			mTimer.cancel();
			mXXXXTask = null;
		}
	}

	/**
	 * @brief 启动XXXX计时任务
	 */
	public void startXXXXTimer() {
		if (null != mTimer) {
			if (null != mXXXXTask) {
				mXXXXTask.cancel();
				mXXXXTask = null;
			}
		} else {
			mTimer = new Timer(true);
		}

		// 创建计时器任务
		mXXXXTask = new XXXXTask();
		mTimer.schedule(mXXXXTask, 0, POLL_INTERVAL);
	}

	/**
	 * @brief 停止XXXX计时任务
	 */
	public void stopXXXXTimer() {
		if (null != mTimer) {
			if (null != mXXXXTask) {
				mXXXXTask.cancel();
				mXXXXTask = null;
			}
		}
	}

	/**
	 * 
	 * Created on: 2014年8月29日
	 * 
	 * @brief XXXX计时任务要处理的事情
	 * @author Eric Fung
	 * @date Latest modified on: 2014年8月29日
	 * @version V1.0.00
	 * 
	 */
	class XXXXTask extends TimerTask {

		@Override
		public void run() {
			// mMainHandler.sendEmptyMessage(XXXX);
			// do something
		}

	}
}
