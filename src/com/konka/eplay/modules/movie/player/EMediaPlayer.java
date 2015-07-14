package com.konka.eplay.modules.movie.player;

import iapp.eric.utils.base.Trace;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.konka.android.media.KKMediaPlayer;
import com.konka.eplay.Configuration;

/**
 * 
 * @Created on 2015年5月19日
 * @brief 兼容软硬解播放器
 * @author
 * @date Latest modified on: 2015年5月19日
 * @version V1.0.00
 * 
 */
public class EMediaPlayer {

	private final boolean mIsHWDecoder;

	private io.vov.vitamio.MediaPlayer mSoftMediaPlayer;
	private KKMediaPlayer mKKMediaPlayer;

	private Context mContext;

	public EMediaPlayer(Context context) {
		this(context, Configuration.IS_HWDECODER);
	}

	public EMediaPlayer(Context context, boolean isHWDecoder) {
		mIsHWDecoder = isHWDecoder;
		mContext = context;
		Trace.Info("########视频播放器    " + (mIsHWDecoder == true ? "硬解" : "软解"));
		initPlayer();
	}

	
	private void initPlayer() {
		if (mIsHWDecoder == true) {
			initHWPlayer();
		}
		else {
			initSFPlayer();
		}
	}

	private void initHWPlayer() {
		if (mIsHWDecoder == false) {
			return;
		}
		mKKMediaPlayer = new KKMediaPlayer();
		mKKMediaPlayer.getMediaPlayer().setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(android.media.MediaPlayer mp) {
				if (mOnPreparedListener != null) {
					mOnPreparedListener.onPrepared(EMediaPlayer.this);
				}
			}
		});
		mKKMediaPlayer.getMediaPlayer().setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(android.media.MediaPlayer mp) {
				if (mOnCompletionListener != null) {
					mOnCompletionListener.onCompletion(EMediaPlayer.this);
				}
			}
		});
		mKKMediaPlayer.getMediaPlayer().setOnErrorListener(new android.media.MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
				if (mOnErroListener != null) {
					return mOnErroListener.onError(EMediaPlayer.this, what, extra);
				}
				return false;
			}
		});
		mKKMediaPlayer.getMediaPlayer().setOnSeekCompleteListener(
				new android.media.MediaPlayer.OnSeekCompleteListener() {

					@Override
					public void onSeekComplete(android.media.MediaPlayer mp) {
						if (mOnSeekCompleteListener != null) {
							mOnSeekCompleteListener.onSeekComplete(EMediaPlayer.this);
						}
					}
				});
		mKKMediaPlayer.getMediaPlayer().setOnVideoSizeChangedListener(
				new android.media.MediaPlayer.OnVideoSizeChangedListener() {

					@Override
					public void onVideoSizeChanged(android.media.MediaPlayer mp, int width, int height) {
						if (mOnVideoSizeChangedListener != null) {
							mOnVideoSizeChangedListener.onVideoSizeChanged(EMediaPlayer.this, width, height);
						}
					}
				});
		mKKMediaPlayer.setOnInfoListener(new android.media.MediaPlayer.OnInfoListener() {

			@Override
			public boolean onInfo(android.media.MediaPlayer mp, int what, int extra) {
				if (mOnInfoListener != null) {
					mOnInfoListener.onInfo(EMediaPlayer.this, what, extra);
				}
				return false;
			}
		});
	}

	private void initSFPlayer() {
		if (mIsHWDecoder == true) {
			return;
		}
		mSoftMediaPlayer = new io.vov.vitamio.MediaPlayer(mContext);
		mSoftMediaPlayer.setOnPreparedListener(new io.vov.vitamio.MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(io.vov.vitamio.MediaPlayer mp) {
				if (mOnPreparedListener != null) {
					mOnPreparedListener.onPrepared(EMediaPlayer.this);
				}
			}
		});
		mSoftMediaPlayer.setOnCompletionListener(new io.vov.vitamio.MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(io.vov.vitamio.MediaPlayer mp) {
				if (mOnCompletionListener != null) {
					mOnCompletionListener.onCompletion(EMediaPlayer.this);
				}
			}
		});
		mSoftMediaPlayer.setOnErrorListener(new io.vov.vitamio.MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(io.vov.vitamio.MediaPlayer mp, int what, int extra) {
				if (mOnErroListener != null) {
					return mOnErroListener.onError(EMediaPlayer.this, what, extra);
				}
				return false;
			}
		});
		mSoftMediaPlayer.setOnSeekCompleteListener(new io.vov.vitamio.MediaPlayer.OnSeekCompleteListener() {

			@Override
			public void onSeekComplete(io.vov.vitamio.MediaPlayer mp) {
				if (mOnSeekCompleteListener != null) {
					mOnSeekCompleteListener.onSeekComplete(EMediaPlayer.this);
				}
			}
		});
		mSoftMediaPlayer.setOnVideoSizeChangedListener(new io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener() {

			@Override
			public void onVideoSizeChanged(io.vov.vitamio.MediaPlayer mp, int width, int height) {
				if (mOnVideoSizeChangedListener != null) {
					mOnVideoSizeChangedListener.onVideoSizeChanged(EMediaPlayer.this, width, height);
				}
			}
		});
		mSoftMediaPlayer.setOnInfoListener(new io.vov.vitamio.MediaPlayer.OnInfoListener() {

			@Override
			public boolean onInfo(io.vov.vitamio.MediaPlayer mp, int what, int extra) {
				if (mOnInfoListener != null) {
					mOnInfoListener.onInfo(EMediaPlayer.this, what, extra);
				}
				return false;
			}
		});
	}

	public KKMediaPlayer getKKMediaPlayer() {
		if (mIsHWDecoder == false) {
			return null;
		}
		else {
			return mKKMediaPlayer;
		}
	}

	public io.vov.vitamio.MediaPlayer getSoftMediaPlayer() {
		if (mIsHWDecoder == true) {
			return null;
		}
		else {
			return mSoftMediaPlayer;
		}
	}

	public boolean isHWDecoder() {
		return mIsHWDecoder;
	}

	public void setDisplay(SurfaceHolder sh) {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().setDisplay(sh);
		}
		else {
			mSoftMediaPlayer.setDisplay(sh);
		}
	}

	public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException,
			SecurityException, IllegalStateException {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().setDataSource(context, uri);
		}
		else {
			mSoftMediaPlayer.setDataSource(context, uri);
		}
	}

	public void setAudioStreamType(int streamMusic) {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().setAudioStreamType(streamMusic);
		}
		else {
			((Activity) mContext).setVolumeControlStream(streamMusic);
		}

	}

	public int getDuration() {
		if (mIsHWDecoder) {
			return mKKMediaPlayer.getMediaPlayer().getDuration();
		}
		else {
			return (int) mSoftMediaPlayer.getDuration();
		}
	}

	public int getCurrentPosition() {
		if (mIsHWDecoder) {
			return mKKMediaPlayer.getMediaPlayer().getCurrentPosition();
		}
		else {
			return (int) mSoftMediaPlayer.getCurrentPosition();
		}
	}

	public void seekTo(int goalMsec) {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().seekTo(goalMsec);
		}
		else {
			mSoftMediaPlayer.seekTo(goalMsec);
		}
	}

	public void release() {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().release();
		}
		else {
			mSoftMediaPlayer.release();
		}
	}

	public void reset() {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().reset();
		}
		else {
			mSoftMediaPlayer.reset();
		}
	}

	public void start() {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().start();
		}
		else {
			mSoftMediaPlayer.start();
		}
	}

	public boolean isPlaying() {
		if (mIsHWDecoder) {
			return mKKMediaPlayer.getMediaPlayer().isPlaying();
		}
		else {
			return mSoftMediaPlayer.isPlaying();
		}
	}

	public void prepareAsync() {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().prepareAsync();
		}
		else {
			mSoftMediaPlayer.prepareAsync();
		}

	}

	public void pause() {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().pause();
		}
		else {
			mSoftMediaPlayer.pause();
		}
	}

	public void stop() {
		if (mIsHWDecoder) {
			mKKMediaPlayer.getMediaPlayer().stop();
		}
		else {
			mSoftMediaPlayer.stop();
		}
	}

	public int getVideoWidth() {
		if (mIsHWDecoder) {
			return mKKMediaPlayer.getMediaPlayer().getVideoWidth();
		}
		else {
			return mSoftMediaPlayer.getVideoWidth();
		}
	}

	public int getVideoHeight() {
		if (mIsHWDecoder) {
			return mKKMediaPlayer.getMediaPlayer().getVideoHeight();
		}
		else {
			return mSoftMediaPlayer.getVideoHeight();
		}
	}

	private OnPreparedListener mOnPreparedListener;
	private OnCompletionListener mOnCompletionListener;
	private OnErrorListener mOnErroListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
	private OnInfoListener mOnInfoListener;

	public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
		mOnPreparedListener = onPreparedListener;
	}

	public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
		mOnCompletionListener = onCompletionListener;
	}

	public void setOnErrorListener(OnErrorListener onErrorListener) {
		mOnErroListener = onErrorListener;
	}

	public void setOnSeekCompleteListener(OnSeekCompleteListener onSeekCompleteListener) {
		mOnSeekCompleteListener = onSeekCompleteListener;
	}

	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
		mOnVideoSizeChangedListener = onVideoSizeChangedListener;
	}

	public void setOnInfoListener(OnInfoListener onInfoListener) {
		mOnInfoListener = onInfoListener;
	}

	public interface OnPreparedListener {
		public void onPrepared(EMediaPlayer mp);
	}

	public interface OnCompletionListener {
		public void onCompletion(EMediaPlayer mp);
	}

	public interface OnErrorListener {
		public boolean onError(EMediaPlayer mp, int what, int extra);
	}

	public interface OnSeekCompleteListener {
		public void onSeekComplete(EMediaPlayer mp);
	}

	public interface OnVideoSizeChangedListener {
		public void onVideoSizeChanged(EMediaPlayer mp, int width, int height);
	}

	public interface OnInfoListener {
		public boolean onInfo(EMediaPlayer mp, int what, int extra);
	}

}
