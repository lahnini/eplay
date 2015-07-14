package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.modules.CommonFileInfo;

/**
 * @ClassName: MusicPlayerService
 * @Description: 音乐播放服务
 * @author xuyunyu
 * @date 2015年3月24日 下午3:54:49
 */
public class MusicPlayerService extends Service {



	public final static int MUSIC_SERVICE_PLAYMODE_SINGLE = 11;
	public final static int MUSIC_SERVICE_PLAYMODE_CIRCLE = 12;
	public final static int MUSIC_SERVICE_PLAYMODE_RANDOM = 13;

	public final static int MUSIC_SERVICE_EFFECTIVE_ORIGINAL = 0;
	public final static int MUSIC_SERVICE_EFFECTIVE_COUNTRYSIDE = 4;
	public final static int MUSIC_SERVICE_EFFECTIVE_DANCE = 2;
	public final static int MUSIC_SERVICE_EFFECTIVE_MENTAL = 5;
	public final static int MUSIC_SERVICE_EFFECTIVE_JAZZ = 8;
	public final static int MUSIC_SERVICE_EFFECTIVE_ROC = 9;

	// 传递给activity的句柄
	private final MusicBinder mBinder = new MusicBinder();
	private MediaPlayer mMediaPlayer = null;
	private List<CommonFileInfo> mMusicList;
	private int mCurrentPosition;
	private static MusicPlayerService mInstance;

	private Visualizer mVisualizer;
	private VisualizerView mVisualizerView;

	private Equalizer mEqualizer;
	private short mPreset = -2;
	private boolean mIsStart = false;
	private AudioManager am = null;

	private int mCurAudionSessionId;

	private int mPlayMode = MUSIC_SERVICE_PLAYMODE_CIRCLE;// 默认顺序播放

	private OnAudioFocusChangeListener mAudioListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			Trace.Warning("###onAudioFocusChange==" + focusChange);
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				pause();
			}
		}
	};

	@Override
	public void onCreate() {

		super.onCreate();

		mPlayMode = Configuration.getInt(MusicControlPanelFragment.MUSIC_SP_FILE,
				MusicControlPanelFragment.MUSIC_SP_SONGMODE_KEY);
		if (mPlayMode == -1) {
			mPlayMode = MUSIC_SERVICE_PLAYMODE_CIRCLE;
		}
		mPreset = (short) Configuration.getInt(MusicControlPanelFragment.MUSIC_SP_FILE,
				MusicControlPanelFragment.MUSIC_SP_EFFECTIVE_KEY);
		if (mPreset == -1) {
			mPreset = MUSIC_SERVICE_EFFECTIVE_ORIGINAL;
		}

		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		Trace.Info("绑定  MusicPlayerService 成功");
		mInstance = this;
		return mBinder;
	}


	@Override
	public boolean onUnbind(Intent intent) {
		mInstance = null;
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Trace.Info("onStartCommand 初始化 servie");
		mInstance = this;
		return super.onStartCommand(intent, flags, startId);
	}

	public static MusicPlayerService getInstance(){
		return mInstance;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mMusicList!=null) {
			mMusicList.clear();
		}

		am.abandonAudioFocus(mAudioListener);
		mIsStart = false;
		if (null != mMediaPlayer) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			} else {
				mMediaPlayer.release();
			}
		}
		mInstance = null;
	}


	public boolean getPlayState() {
		return mIsStart;
	}

	public void playSongList(List<CommonFileInfo> musicFiles, int position) {

		if (null == musicFiles || 0 == musicFiles.size()) {
			return;
		}
		if (position >= musicFiles.size() || position < 0) {
			Trace.Info("position 超过数组，现将position置为0");
			position = 0;
		}

		mCurrentPosition = position;
		mMusicList = musicFiles;

		createPlayer(mCurrentPosition);
	}

	/**
	 * @Description: 初始化MediaPlayer，并且播放
	 */
	private void createPlayer(int position) {

		if (mMediaPlayer == null) {

			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					mMediaPlayer.stop();
					mMediaPlayer.reset();
					next(false);
				}
			});

			mMediaPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {

					mMediaPlayer.reset();
					next(false);
					Toast.makeText(getApplicationContext(), "播放出错，为您切换到下一首", Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		}

		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		mMediaPlayer.reset();
		if (mMusicList.size() <= 0) {
			return;
		}
		Trace.Info(mMusicList.get(position).getPath());
		try {
			mMediaPlayer.setDataSource(mMusicList.get(position).getPath());
			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Trace.Info("AudioSessionId  " + mMediaPlayer.getAudioSessionId());

		if (mEqualizer == null||mMediaPlayer.getAudioSessionId()!=mCurAudionSessionId) {

			Trace.Info("mEqualizer init");
			mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
			mEqualizer.setEnabled(true);
			mEqualizer.usePreset(mPreset);
		}

		if (mVisualizer == null) {
			mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
			mVisualizer.setCaptureSize(512);
			mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
				public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
					// Trace.Info("audio=====onWaveFormDataCapture");
					if (null != mVisualizerView&&mIsStart) {
						mVisualizerView.updateVisualizer(bytes, mMediaPlayer.getCurrentPosition(),
								mMediaPlayer.isPlaying());
					}
				}

				public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
					// Trace.Info("audio=====onFftDataCapture");
					if (null != mVisualizerView&&mIsStart) {
						mVisualizerView.updateVisualizer(fft, mMediaPlayer.getCurrentPosition(),
								mMediaPlayer.isPlaying());
					}

				}
			}, Visualizer.getMaxCaptureRate() / 10, false, true);
			mVisualizer.setEnabled(true);
		}

		mCurAudionSessionId = mMediaPlayer.getAudioSessionId();
		//抢占焦点
		am.requestAudioFocus(mAudioListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		mMediaPlayer.start();
		mIsStart = true;
		DispatchMusicEvent(Constant.MUSIC_SERVICE_FLAG_CHANGE_SONG);
	}

	/**
	 * @Title: snedMusicBroadcast
	 * @Description:
	 * @return void
	 * @throws
	 */
	private void DispatchMusicEvent(int type) {
		EventMusicStateChange event = new EventMusicStateChange();
		event.musicStateType = type;
		EventDispatchCenter.getInstance().post(event);
	}

	public void playSelection(int position) {

		mCurrentPosition = position;
		createPlayer(position);
	}

	public void play() {

		if (mMediaPlayer != null) {
			//抢占焦点
			am.requestAudioFocus(mAudioListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			mMediaPlayer.start();
			DispatchMusicEvent(Constant.MUSIC_SERVICE_FLAG_SONG_PLAY);
		}
	}

	public void pause() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				DispatchMusicEvent(Constant.MUSIC_SERVICE_FLAG_SONG_PAUSE);
			}

		}
	}

	public void stop() {

		if (!mIsStart) {
			return;
		}
		try {
			if (null != mMediaPlayer) {
				// 先控制状态避免异常
				mIsStart = false;
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
				}
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
				mEqualizer = null;
				mVisualizer = null;
				DispatchMusicEvent(Constant.MUSIC_SERVICE_FLAG_SONG_STOP);
			}
		} catch (IllegalStateException e) {
			Trace.Info("###music player stop IllegalStateException");
			e.printStackTrace();
		}
	}

	public void previous(boolean fromuser) {
		int position = getListPosition(-1,fromuser);
		Trace.Info("当前歌曲在列表中的position" + position);
		if (position != -1) {
			mCurrentPosition = position;
			createPlayer(mCurrentPosition);
		}
	}

	public void next(boolean fromuser) {
		int position = getListPosition(1,fromuser);
		Trace.Info("当前歌曲在列表中的position" + position);
		if (position != -1) {
			mCurrentPosition = position;
			createPlayer(mCurrentPosition);
		}

	}

	/*
	 * @Description: 获取播放列表的位置
	 */
	private int getListPosition(int increment,boolean fromuser) {

		switch (mPlayMode) {
		case MUSIC_SERVICE_PLAYMODE_CIRCLE:
			int temp = mCurrentPosition + increment;
			if (temp >= mMusicList.size()) {
				// Toast.makeText(getApplicationContext(), "已是最后一首",
				// Toast.LENGTH_SHORT).show();
				// 返回到第一首
				return 0;
			} else if (temp < 0) {
				// Toast.makeText(getApplicationContext(), "已是第一首",
				// Toast.LENGTH_SHORT).show();
				// 跳到最后一首
				return mMusicList.size() - 1;
			} else {
				return temp;
			}
		case MUSIC_SERVICE_PLAYMODE_RANDOM:
			Random rd = new Random();
			if (mMusicList.size() != 0) {
				return rd.nextInt(mMusicList.size());
			} else {
				mMediaPlayer.stop();
			}

		case MUSIC_SERVICE_PLAYMODE_SINGLE:
			if (fromuser) {
				int position = mCurrentPosition + increment;
				if (position >= mMusicList.size()) {
					// 返回到第一首
					return 0;
				} else if (position < 0) {
					// 跳到最后一首
					return mMusicList.size() - 1;
				} else {
					return position;
				}
			}else {
				return mCurrentPosition;
			}

		}
		return -1;
	}

	/*
	 * 设置播放模式
	 */
	public void setPlayMode(int playMode) {

		if (Configuration.INVALID_INT != playMode) {
			mPlayMode = playMode;
		}
	}

	/*
	 * 设置播放音效
	 */
	public void switchEffective(short preset) {

		mPreset = preset;
		if (mEqualizer == null||mMediaPlayer.getAudioSessionId()!=mCurAudionSessionId) {
			mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
			mEqualizer.setEnabled(true);
			mEqualizer.usePreset(mPreset);
		}else {
			mEqualizer.usePreset(mPreset);
		}


		// mEqualizer.getEnabled()
	}

	public void seekTo(int progress) {

		if (mMediaPlayer != null) {

			mMediaPlayer.seekTo(progress);
		}
	}

	public int getCurrentPosition() {
		if (null != mMediaPlayer&&mIsStart) {
			return mMediaPlayer.getCurrentPosition();
		} else {
			return 0;
		}

	}

	public int getmCurrentListPosition() {
		return mCurrentPosition;
	}

	public void setVisualizerView(VisualizerView view) {
		mVisualizerView = view;
		if (null!=mMediaPlayer) {
			mVisualizerView.setmMusicDuration(mMediaPlayer.getDuration());
		}


	}

	public MediaPlayer getmMediaPlayer() {
		return mMediaPlayer;
	}

	public String getMusciPath() {
		if (null != mMusicList && mMusicList.size() != 0) {
			return mMusicList.get(mCurrentPosition).getPath();
		} else {
			return null;
		}

	}

	public String getMusicName() {
		if (mMusicList != null && mMusicList.size() > 0) {
			String musicName = mMusicList.get(mCurrentPosition).getName();
			return musicName.substring(0, musicName.length() - 4);
		} else {
			return "";
		}

	}

	public void clearMusicFiles() {
		if (mMusicList!=null) {
			mMusicList.clear();
		}
		mCurrentPosition = 0;
	}

	public void setCurrentPosition(int position){
		mCurrentPosition = position;
	}

	public List<CommonFileInfo> getmMusicList() {
		return mMusicList;
	}

	public class MusicBinder extends Binder {

		public MusicPlayerService getService() {
			return MusicPlayerService.this;
		}
	}

}
