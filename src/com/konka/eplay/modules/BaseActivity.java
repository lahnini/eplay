/**
 * @Title: BaseActivity.java
 * @Package com.konka.eplay.modules
 * @Description: TODO(用一句话描述该文件做什么)
 * @author A18ccms A18ccms_gmail_com
 * @date 2015年4月23日 下午2:41:45
 * @version
 */
package com.konka.eplay.modules;

import iapp.eric.utils.base.Trace;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.konka.eplay.Constant;
import com.konka.eplay.modules.music.MusicPlayerActivity;
import com.konka.eplay.modules.music.MusicPlayerService;

/**
 * @ClassName: BaseActivity
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年4月23日 下午2:41:45
 * @version
 *
 */
public abstract class BaseActivity extends Activity {

	private MusicPlayerService mPlayerService;
	private ServiceConnection mServiceConnection = null;
	private MusicStateReciever mMusicStateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		startService(new Intent("com.konka.EPlay.MusicPlayerService"));
		bindService();
		registerReciever();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 解绑 music service
		if (mServiceConnection != null) {
			unbindService(mServiceConnection);
		}
		if (null != mMusicStateReceiver) {
			unregisterReceiver(mMusicStateReceiver);
		}
	}

	public MusicPlayerService getPlayerService() {
		return mPlayerService;
	}

	/*
	 * @Description: 绑定音乐播放服务
	 */
	private void bindService() {

		mServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Trace.Debug("MusicPlayerActivity---> 已断开连接service");
				playMuisicAnimation();
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Trace.Info("MusicPlayerActivity---> 已绑定到service");
				mPlayerService = ((MusicPlayerService.MusicBinder) service).getService();
				playMuisicAnimation();
			}
		};

		Intent intent = new Intent();
		intent.setAction(MusicPlayerActivity.ACTION_MUSIC_SERVICE_INTENT);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	/*
	 * @Description:注册广播
	 */
	private void registerReciever() {
		mMusicStateReceiver = new MusicStateReciever();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MusicPlayerActivity.ACTION_MUSIC_BROADCAST);
		registerReceiver(mMusicStateReceiver, intentFilter);

	}

	/**
	 * @Title: updateMuisicInfo
	 * @Description: 刷新歌曲信息
	 */
	public abstract void updateMuisicInfo();

	/**
	 * @Title: playMuisicAnimation
	 * @Description: 开始波形动画
	 */
	public abstract void playMuisicAnimation();

	/**
	 * @Title: stopMuisicAnimation
	 * @Description: 停止波形动画
	 */
	public abstract void stopMuisicAnimation();

	class MusicStateReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(MusicPlayerActivity.ACTION_MUSIC_BROADCAST)) {
				int flag = intent.getIntExtra("FLAG", -1);
				switch (flag) {
				case Constant.MUSIC_SERVICE_FLAG_CHANGE_SONG:
					updateMuisicInfo();
					break;
				case Constant.MUSIC_SERVICE_FLAG_SONG_PLAY:
					playMuisicAnimation();
					break;
				case Constant.MUSIC_SERVICE_FLAG_SONG_PAUSE:
					stopMuisicAnimation();
					break;
				}
			}

		}

	}

}
