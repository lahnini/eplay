package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

import com.konka.android.media.KKMediaPlayer;
import com.konka.android.tv.KKPictureManager;
import com.konka.eplay.interfaces.IMoviePlayer;
import com.konka.eplay.interfaces.IPlayer;
import com.konka.eplay.modules.movie.player.EMediaPlayer;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnCompletionListener;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnErrorListener;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnInfoListener;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnPreparedListener;

/*
 * 用于控制video播放，封装了MediaPlayer对象，并关联MovieView(继承SurfaceView)控件用于显示画面 <br>
 * <br>
 * MediaPlayer object有多个state，some MediaPlayer method being called in an invalid
 * state transfers the object(mediaplayer) to the Error state. <br>
 * <br>
 * 使用MoviePlayer时，若在不合适的内嵌mediaplayer状态下使用了不合适的方法
 * ，会导致mediaplayer进入error状态
 * 使用movieplayer的setOnErrorListener方法设置监听，以进入error状态后作进一步操作
 * 
 * @author situhui
 */
public class MoviePlayer extends IPlayer implements IMoviePlayer {

	// 快进快退的时间 单位msec
	private static final int FAST_FORWARD_BACKWARD = 15000;

	// 用于同步的状态常量，表内嵌的mediaplayer的状态
	private static final int STATUS_IDLE = 0;
	private static final int STATUS_INITIALIZED = 1;
	private static final int STATUS_PREPARING = 2;
	private static final int STATUS_PREPARED = 3;
	private static final int STATUS_STARTED = 4;
	private static final int STATUS_PAUSED = 5;
	private static final int STATUS_STOPPED = 6;
	private static final int STATUS_PLAYBACKCOMPLETED = 7;
	private static final int STATUS_END = 8;
	private static final int STATUS_ERROR = 9;
	// 记录内嵌mediaplayer的状态
	private int mStatus;

	// 屏显模式
	public static final int SCREEN_FULL = 1; // 等比
	public static final int SCREEN_FULL_RESIZE = 2; // 拉伸
	public static final int SCREEN_DEFAULT = 3; // 原始

	// 循环模式
	public static final int LOOP_SINGLE = 1;// 单个循环
	public static final int LOOP_ALL = 2;// 顺序循环
	public static final int LOOP_SHUFFLE = 3;// 随机
	private int mLoopMode;// 记录当前循环状态

	private Context mContext = null;
	// 此movieplayer显示画面的surfaceview
	private MovieView mMovieView = null;
	// 内嵌的实际起播放操作的mediaplayer
	private EMediaPlayer mEMediaPlayer = null;
	private KKMediaPlayer mKKMediaPlayer = null;

	// 标记prepared后是否紧接着start
	private boolean mStartWhenPrepared;
	// 标记prepared后是否紧接着seek
	private int mSeekWhenPrepared;

	// 当前播放资源uri
	private Uri mDataSourceUri = null;
	// 播放资源集合
	private List<String> mDataSourceList;
	// 资源集合当前播放位置
	private int mPosition;

	private int mVideoWidth = 0;
	private int mVideoHeight = 0;

	public MoviePlayer(Context context, MovieView mv) {
		mContext = context;
		mMovieView = mv;
		if (mMovieView != null) {
			mMovieView.getHolder().addCallback(new Callback() {
				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					// TODO Auto-generated method stub
				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					constructMediaPlayer();
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					// TODO Auto-generated method stub
				}
			});
		}
		// 默认循环模式为顺序循环
		mLoopMode = LOOP_ALL;
	}

	/**
	 * 生成并初始化内嵌的mediaplayer，与movieview相关联
	 */
	private void constructMediaPlayer() {
		mEMediaPlayer = new EMediaPlayer(mContext);
		mKKMediaPlayer = mEMediaPlayer.getKKMediaPlayer();

		mEMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		mEMediaPlayer.setOnPreparedListener(mOnPreparedListener);
		mEMediaPlayer.setOnCompletionListener(mOnCompletionListener);
		mEMediaPlayer.setOnErrorListener(mOnErrorListener);
		mEMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
		mEMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
		mEMediaPlayer.setOnInfoListener(mOnInfoListener);

		if (mKKMediaPlayer != null) {
			mKKMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
		}

		mStatus = STATUS_IDLE;

		// mKkMediaPlayer = new KKMediaPlayer();
		// setMediaPlayer(getKkMediaPlayer().getMediaPlayer());
		// mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
		// mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
		// mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
		// mMediaPlayer.setOnErrorListener(mOnErrorListener);
		// mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
		// mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateCallback);
		// mKkMediaPlayer.setOnInfoListener(mOnInfoCallback);
		// mKkMediaPlayer.setOnTimedTextListener(mOnTimedTextCallback);

		// if(mKkMediaPlayer.getMediaPlayerVersion() !=
		// KKMediaPlayer.VERSION_REALTEK){
		// mMediaPlayer.setDisplay(mMovieView.getHolder());
		// }
		// mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// mMediaPlayer.setScreenOnWhilePlaying(true);
	}

	public KKMediaPlayer getKKMediaPlayer() {
		return mKKMediaPlayer;
	}

	public int getVideoWidth() {
		return mVideoWidth;
	}

	public int getVideoHeight() {
		return mVideoHeight;
	}

	// 暂时没用到 相关代码先不要动
	// 用于控制上一个快进/快退操作完成后才进行下一个快进/快退操作，解决上一个快进/快退操作没完成，又执行下一个上一个快进/快退操作时造成步长不一致问题
	private boolean lastFastCompleted = true;

	/** 快进 */
	@Override
	public void ff() {
		if (mStatus == STATUS_STARTED || mStatus == STATUS_PAUSED) {
			// if (lastFastCompleted == true) {
			lastFastCompleted = false;
			int duration = mEMediaPlayer.getDuration();
			int current = mEMediaPlayer.getCurrentPosition();
			Trace.Debug("当前：" + current);
			int goalMsec = current + FAST_FORWARD_BACKWARD;
			goalMsec = goalMsec <= duration ? goalMsec : duration;

			mEMediaPlayer.seekTo(goalMsec);
			Trace.Debug("目标：" + goalMsec);
			// }
		}
	}

	/** 快退 */
	@Override
	public void fb() {
		if (mStatus == STATUS_STARTED || mStatus == STATUS_PAUSED) {
			// if (lastFastCompleted == true) {
			lastFastCompleted = false;
			int current = mEMediaPlayer.getCurrentPosition();
			Trace.Debug("当前：" + current);
			int goalMsec = current - FAST_FORWARD_BACKWARD;
			goalMsec = goalMsec >= 0 ? goalMsec : 0;

			mEMediaPlayer.seekTo(goalMsec);
			Trace.Debug("目标：" + goalMsec);
			// }
		}
	}

	public void setSubtitleDisplay(SurfaceHolder holder) {
		if (mKKMediaPlayer != null) {
			mKKMediaPlayer.setSubtitleDisplay(holder);
		}
	}

	@Override
	public void switchSubtitle(int which) {
		if (which >= 0 && which < mKKMediaPlayer.getSubtitleTrackNumber()) {
			mKKMediaPlayer.setSubtitleTrack(which);
		}
	}

	@Override
	public void turnOnSubtitle() {
		if (mKKMediaPlayer != null) {
			mKKMediaPlayer.turnOnSubtitleTrack();
		}
	}

	@Override
	public void turnOffSubtitle() {
		if (mKKMediaPlayer != null) {
			mKKMediaPlayer.turnOffSubtitleTrack();
		}
	}

	@Override
	public void setSubtitleDataSource(String uri) {
		if (mKKMediaPlayer != null) {
			mKKMediaPlayer.addTimedTextSource(uri, null);
		}
	}

	/**
	 * 获取字幕总数
	 * 
	 * @return
	 */
	public int getSubtitleTrackNumber() {
		Trace.Info("getSubtitleTrackNumber");
		if (mKKMediaPlayer == null) {
			return 0;
		}
		return mKKMediaPlayer.getSubtitleTrackNumber();
	}

	// 暂时没用到
	@Override
	public int getSubtitle() {
		return 0;
	}

	@Override
	public int getTrack() {
		return mAudioTrackIndex;
	}

	// 当前音轨索引
	private int mAudioTrackIndex;

	@Override
	public void switchTrack(int which) {
		if (which >= 0 && which < mKKMediaPlayer.getAudioTrackNumber()) {
			mKKMediaPlayer.setAudioTrack(which);
			mAudioTrackIndex = which;
		}
	}

	/**
	 * 获取音轨总数
	 * 
	 * @return
	 */
	public int getAudioTrackNumber() {
		if (mKKMediaPlayer == null) {
			return 0;
		}
		return mKKMediaPlayer.getAudioTrackNumber();
	}

	// 空实现，暂时没有用到
	@Override
	public void switchScreenDisplayMode(int playerType, int mode) {
		// switch (playerType) {
		// case KKMediaPlayer.VERSION_REALTEK: {
		// KKMediaPlayer.setAspectRatio(mode);
		// }
		// break;
		// default:
		// // 除了RTK平台，其他只要调整了view即可，原因未明，待研究
		// break;
		// }
	}

	// 空实现，暂时没有用到
	@Override
	public void switchBrightness() {

	}

	/**
	 * 设置图像模式
	 * 
	 * @param mode
	 */
	public void setPictureMode(KKPictureManager.EN_KK_PICTURE_MODE mode) {
		switch (mode) {
		case DYNAMIC:
			break;
		case NORMAL:
			break;
		case SOFT:
			break;
		case VIVID:
			break;
		default:
			return;
		}
		KKPictureManager.getInstance(mContext).setPictureMode(mode);
	}

	/**
	 * 获取当前图像模式
	 * 
	 * @return
	 */
	public KKPictureManager.EN_KK_PICTURE_MODE getPictureMode() {
		return KKPictureManager.getInstance(mContext).getPictureMode();
	}

	/**
	 * 获取当前循环模式
	 * 
	 * @return
	 */
	public int getLoopMode() {
		return mLoopMode;
	}

	/**
	 * 设置循环模式
	 * 
	 * @param loopMode
	 */
	public void setLoopMode(int loopMode) {
		if (loopMode == LOOP_ALL || loopMode == LOOP_SHUFFLE || loopMode == LOOP_SINGLE) {
			mLoopMode = loopMode;
		}
	}

	/**
	 * 释放播放器资源
	 */
	public void release() {
		mEMediaPlayer.release();
		mStatus = STATUS_END;

		mDataSourceUri = null;
		mDataSourceList = null;
		mPosition = 0;
		mEMediaPlayer = null;
		mKKMediaPlayer = null;
		mSeekWhenPrepared = 0;
		mStartWhenPrepared = false;
		mAudioTrackIndex = 0;
		lastFastCompleted = true;
	}

	/** 重置movieplayer */
	@Override
	public void reset() {
		if (!mEMediaPlayer.isHWDecoder()) {
			mEMediaPlayer.release();
			mStatus = STATUS_END;
			constructMediaPlayer();
		}
		else {
			mEMediaPlayer.reset();
			mStatus = STATUS_IDLE;
		}
		mDataSourceUri = null;
		mSeekWhenPrepared = 0;
		mStartWhenPrepared = false;
		mAudioTrackIndex = 0;
		lastFastCompleted = true;// 在这加这句解决按住快进进入下一视频时可能出现快进快退不可用
	}

	/** 开始播放 */
	@Override
	public void start() {
		if (mStatus == STATUS_PREPARED || mStatus == STATUS_PAUSED || mStatus == STATUS_PLAYBACKCOMPLETED) {
			mEMediaPlayer.start();
			mStatus = STATUS_STARTED;
		}
		else if (mStatus == STATUS_PREPARING) {
			mStartWhenPrepared = true;
		}
	}

	/** 跳转到 */
	@Override
	public void seek(int second) {
		if (mStatus == STATUS_PREPARED || mStatus == STATUS_STARTED || mStatus == STATUS_PAUSED
				|| mStatus == STATUS_PLAYBACKCOMPLETED) {
			mEMediaPlayer.seekTo(second);
		}
		else if (mStatus == STATUS_PREPARING) {
			mSeekWhenPrepared = second;
		}
	}

	@Override
	public boolean isPlaying() {
		return mEMediaPlayer.isPlaying();
	}

	/** 返回true时表示movieplayer的内置mediaplayer的状为prepared */
	@Override
	public boolean isPrepared() {
		if (mStatus == STATUS_PREPARED) {
			return true;
		}
		return false;
	}

	/** 初始化播放器，读取播放资源uri，内置mediaplay进入prepared状态 */
	@Override
	public void initialize() {
		if (mDataSourceUri == null) {
			Trace.Fatal("play failed");
			return;
		}

		try {
			if (mStatus == STATUS_IDLE) {
				mEMediaPlayer.setDataSource(mContext, mDataSourceUri);
				mStatus = STATUS_INITIALIZED;

				// 软解添加
				if (!mEMediaPlayer.isHWDecoder()) {
					mMovieView.getHolder().setFormat(PixelFormat.RGB_565);
					mEMediaPlayer.getSoftMediaPlayer().setVideoChroma(io.vov.vitamio.MediaPlayer.VIDEOCHROMA_RGB565);
				}
				// setDisplay放这里适配软解
				mEMediaPlayer.setDisplay(mMovieView.getHolder());

				mEMediaPlayer.prepareAsync();
				mStatus = STATUS_PREPARING;
			}
			else if (mStatus == STATUS_STOPPED) {
				// stop()后再调用initialize()会进入到这里
				mEMediaPlayer.prepareAsync();
				mStatus = STATUS_PREPARING;
			}
		} catch (Exception e) {
			Trace.Debug("mediaplayer setdatasource fail,path=" + mDataSourceUri.getPath());
			mSetDataSourceFailListener.onSetDataSourceFail(mEMediaPlayer, mDataSourceUri.getPath(), e);
			e.printStackTrace();
			// 出现异常，进行reset并重置原数据
			reset();
			setDataSourceList(mDataSourceList, mPosition);
		}
	}

	/** 继续播放 */
	@Override
	public void resume() {
		this.start();
		mStatus = STATUS_STARTED;
	}

	/** 暂停播放 */
	@Override
	public void pause() {
		if (mStatus == STATUS_STARTED) {
			mEMediaPlayer.pause();
			mStatus = STATUS_PAUSED;
		}
	}

	/** 停止播放，stop()后需要再调initialize()后才能使用此movieplayer播放 */
	@Override
	public void stop(boolean force) {
		if (mStatus == STATUS_PAUSED || mStatus == STATUS_STARTED || mStatus == STATUS_PREPARED
				|| mStatus == STATUS_PLAYBACKCOMPLETED) {
			mEMediaPlayer.stop();
			mStatus = STATUS_STOPPED;
		}
	}

	/** 重播 */
	@Override
	public void replay() {
		this.seek(0);
		this.start();
	}

	/** 上一个 */
	@Override
	public void prev() {
		changeMovie(false);
	}

	/** 下一个 */
	@Override
	public void next() {
		changeMovie(true);
	}

	/**
	 * @param next
	 *            true时表示下一个，false表示上一个
	 */
	private void changeMovie(boolean next) {
		if (mDataSourceList == null || mDataSourceList.isEmpty()) {
			return;
		}
		if (next == true) {
			mPosition++;
			if (mPosition == mDataSourceList.size()) {
				mPosition = 0;
			}
		}
		else if (next == false) {
			mPosition--;
			if (mPosition == -1) {
				mPosition = mDataSourceList.size() - 1;
			}
		}
		reset();
		setDataSourceList(mDataSourceList, mPosition);
		initialize();
		start();
	}

	/**
	 * 随机播放下一个
	 */
	public void playShuffle() {
		if (mDataSourceList == null || mDataSourceList.isEmpty()) {
			return;
		}
		int size = mDataSourceList.size();
		mPosition = (int) (Math.random() * size);
		reset();
		setDataSourceList(mDataSourceList, mPosition);
		initialize();
		start();
	}

	@Override
	public int getDuration() {
		if (mStatus == STATUS_PREPARED || mStatus == STATUS_PAUSED || mStatus == STATUS_PLAYBACKCOMPLETED
				|| mStatus == STATUS_STARTED || mStatus == STATUS_STOPPED) {
			return mEMediaPlayer.getDuration();
		}
		return 0;
	}

	@Override
	public int getCurrent() {
		if (mStatus != STATUS_ERROR && mStatus != STATUS_IDLE && mStatus != STATUS_END && mStatus != STATUS_PREPARING) {
			return mEMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	// 专门为进度条获取播放进度的方法，解决快进快退进度跳动，返回-1时不刷新进度
	public int getCurrentForProgress() {
		if (mStatus != STATUS_ERROR && mStatus != STATUS_IDLE && mStatus != STATUS_END && mStatus != STATUS_PREPARING) {
			if (lastFastCompleted == true) {
				return mEMediaPlayer.getCurrentPosition();
			}
			else {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * @return 返回当前播放资源的uri
	 */
	public Uri getDataSourceUri() {
		return mDataSourceUri;
	}

	// 设置播放资源
	private void setDataSourcePath(String path) {
		if (path == null) {
			return;
		}
		Uri uri = Uri.parse(path);
		mDataSourceUri = uri;
	}

	/**
	 * @param dataSourceList
	 *            播放资源路径集合
	 * @param startPosition
	 *            从哪个开发播放
	 */
	public void setDataSourceList(List<String> dataSourceList, int startPosition) {
		if (mStatus == STATUS_IDLE) {
			mDataSourceList = dataSourceList;
			if (mDataSourceList != null && !mDataSourceList.isEmpty()) {
				if (startPosition < 0 || startPosition >= mDataSourceList.size()) {
					startPosition = 0;
				}
				mPosition = startPosition;
				String path = dataSourceList.get(mPosition);
				setDataSourcePath(path);
			}
			else {
				mDataSourceList = null;
			}
		}
	}

	/**
	 * 获取当前播放资源在集合中的索引
	 * 
	 * @return
	 */
	public int getPosition() {
		return mPosition;
	}

	/**
	 * 设置MediaPlayer.OnErrorListener
	 * 
	 * @param errorListener
	 */
	public void setOnErrorListener(OnErrorListener errorListener) {
		mErrorListener = errorListener;
	}

	/**
	 * 设置MediaPlayer.OnPreparedListener
	 * 
	 * @param preparedListener
	 */
	public void setOnPreparedListener(OnPreparedListener preparedListener) {
		mPreparedListener = preparedListener;
	}

	/**
	 * 设置OnTimedTextListener
	 * 
	 * @param timedTextListener
	 */
	public void setOnTimedTextListener(KKMediaPlayer.OnTimedTextListener timedTextListener) {
		mTimedTextListener = timedTextListener;
	}

	/**
	 * 设置MediaPlayer.OnCompletionListener
	 * 
	 * @param completionListener
	 */
	public void setOnCompletionListener(OnCompletionListener completionListener) {
		mComPletionListener = completionListener;
	}

	/**
	 * 设置MediaPlayer.OnOnInfoListener
	 * 
	 * @param infoListener
	 */
	public void setOnInfoListener(OnInfoListener infoListener) {
		mInfoListener = infoListener;
	}

	private EMediaPlayer.OnCompletionListener mComPletionListener;
	private EMediaPlayer.OnCompletionListener mOnCompletionListener = new EMediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(EMediaPlayer mp) {
			Trace.Debug("##############oncompletion");
			if (mComPletionListener != null) {
				// 更换资源前运行
				mComPletionListener.onCompletion(mp);
			}
			if (mStatus == STATUS_STARTED) {
				mStatus = STATUS_PLAYBACKCOMPLETED;

				if (mLoopMode == LOOP_ALL) {
					next();
				}
				else if (mLoopMode == LOOP_SHUFFLE) {
					playShuffle();
				}
				else if (mLoopMode == LOOP_SINGLE) {

					// 不reset会导致快进快退一些问题
					// // 解决按住快进进入重播视频时可能出现快进快退不可用,解决解决按住快进可能出现不进入重播:
					// //
					// completed后重新开始播放，如果直接seek到视频末尾会停住，应该没有进入mediaplayer的started状态
					// //
					// lastFastCompleted==true或false时在STATUS_PLAYBACKCOMPLETED也快进不了的
					// MoviePlayer.this.seek(0);// 这句要在start之前，让ff从0开始
					// MoviePlayer.this.start();

					reset();
					setDataSourceList(mDataSourceList, mPosition);
					initialize();
					start();
				}

			}
		}
	};

	// MediaPlayer用户在activity层设置的OnPreparedListener
	private EMediaPlayer.OnPreparedListener mPreparedListener;
	private EMediaPlayer.OnPreparedListener mOnPreparedListener = new EMediaPlayer.OnPreparedListener() {

		@Override
		public void onPrepared(EMediaPlayer mp) {
			if (mStatus == STATUS_PREPARING) {
				mStatus = STATUS_PREPARED;

				mVideoWidth = mp.getVideoWidth();
				mVideoHeight = mp.getVideoHeight();

				if (mVideoHeight != 0 && mVideoWidth != 0) {
					mMovieView.getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				}

				if (mPreparedListener != null) {
					mPreparedListener.onPrepared(mp);
				}

				if (mSeekWhenPrepared != 0) {
					MoviePlayer.this.seek(mSeekWhenPrepared);
				}
				if (mStartWhenPrepared) {
					MoviePlayer.this.start();
				}
			}
		}
	};

	// MediaPlayer用户在activity层设置的OnErrorListener
	private EMediaPlayer.OnErrorListener mErrorListener;
	private EMediaPlayer.OnErrorListener mOnErrorListener = new EMediaPlayer.OnErrorListener() {

		@Override
		public boolean onError(EMediaPlayer mp, int what, int extra) {
			Trace.Info("player error:" + what + ":" + extra);

			mStatus = STATUS_ERROR;

			if (mErrorListener != null) {
				mErrorListener.onError(mp, what, extra);
			}

			return true;
		}
	};

	private KKMediaPlayer.OnTimedTextListener mTimedTextListener;
	private KKMediaPlayer.OnTimedTextListener mOnTimedTextListener = new KKMediaPlayer.OnTimedTextListener() {

		@Override
		public void onTimedText(MediaPlayer arg0, String arg1) {
			if (mTimedTextListener != null) {
				mTimedTextListener.onTimedText(arg0, arg1);
			}
		}
	};

	private EMediaPlayer.OnInfoListener mInfoListener;
	private EMediaPlayer.OnInfoListener mOnInfoListener = new EMediaPlayer.OnInfoListener() {

		@Override
		public boolean onInfo(EMediaPlayer mp, int what, int extra) {
			Trace.Debug("#### onInfo()");
			Trace.Info("#### onInfo: what = " + what + ", extra = " + extra);
			if (mInfoListener != null) {
				mInfoListener.onInfo(mp, what, extra);
			}
			return true;
		}
	};

	private EMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new EMediaPlayer.OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(EMediaPlayer mp) {
			Trace.Debug("onSeekComplete当前：" + mp.getCurrentPosition());
			lastFastCompleted = true;// 注意seekcomplete没成功情况，造成lastFastCompleted一直为false
		}
	};

	private EMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new EMediaPlayer.OnVideoSizeChangedListener() {

		@Override
		public void onVideoSizeChanged(EMediaPlayer mp, int width, int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				mMovieView.getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
		}
	};

	private OnSetDataSourceFailListener mSetDataSourceFailListener;

	public void setOnSetDataSourceFailListener(OnSetDataSourceFailListener listener) {
		mSetDataSourceFailListener = listener;
	}

	/**
	 * 监听mediaplayer setDataSource,抛异常时回调
	 */
	static interface OnSetDataSourceFailListener {
		void onSetDataSourceFail(EMediaPlayer mp, String sourcePath, Exception e);
	}

}
