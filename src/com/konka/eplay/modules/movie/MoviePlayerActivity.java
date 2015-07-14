package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.konka.android.media.KKMediaPlayer;
import com.konka.android.media.KKMediaPlayer.OnTimedTextListener;
import com.konka.android.tv.KK3DManager;
import com.konka.android.tv.KK3DManager.EN_KK_3D_DISPLAY_FORMAT;
import com.konka.android.tv.KK3DManager.EN_KK_3D_SELF_ADAPTIVE_DETECT_MODE;
import com.konka.android.tv.KKPictureManager;
import com.konka.android.tv.KKPictureManager.EN_KK_PICTURE_MODE;
import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.GlobalData;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.LoadingDialog;
import com.konka.eplay.modules.movie.MoviePlayer.OnSetDataSourceFailListener;
import com.konka.eplay.modules.movie.player.EMediaPlayer;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnCompletionListener;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnErrorListener;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnInfoListener;
import com.konka.eplay.modules.movie.player.EMediaPlayer.OnPreparedListener;

/**
 * @author situhui 视频播放activity，具有播放控制，设置，视频列表功能
 */
public class MoviePlayerActivity extends Activity {
	// 进度条上限
	private static final int PLAY_PROGRESS_MAX = 10000;
	// 播放过程中刷新进度条与时间的时间间隔
	private static final int PROGRESS_UPDATE_INTERVAL = 100;
	// 静止多少时间后隐藏
	static final int PAUSE_TIME_BEFORE_HIDE = 8000;

	// ui handler command
	private static final int CMD_UPDATE_PROGRESS = 0;// 更新播放进度
	private static final int CMD_HIDE_FLOATCONTROLLER = 1;// 隐藏悬浮进度条
	private static final int CMD_HIDE_CONTROLLER = 2;// 隐藏控制栏
	static final int CMD_HIDE_LIST = 3;// 隐藏视频列表
	private static final int CMD_CAPTURE_PICTURE = 4;// 屏幕截图
	private static final int CMD_HIDE_BLACK_MASK = 5;

	private CaptureHandlerThread mCaptureHandlerThread;
	private Handler mCaptureHandler;

	// ui线程handler
	Handler mHandler;

	private LoadingDialog mLoadingDialog;

	// 屏幕宽高
	private int mScreenWidth;
	private int mScreenHeight;

	private View mBtnReplay;
	private View mBtnPre;
	private View mBtnNext;
	private View mBtnList;
	private View mBtnSetting;
	private SeekBar mPlayProgressBar;
	private TextView mDuration;
	private TextView mCurrentTime;

	private LinearLayout mSettingSubtitle;
	private LinearLayout mSettingAudioTrack;
	private LinearLayout mSettingScaleMode;
	private LinearLayout mSettingPicMode;
	private LinearLayout mSettingLoopMode;
	private SeekBar mBacklightSeekBar;
	private TextView mBacklightValue;
	// 总字幕数，内置加外置
	private int mSubtitleNum;
	// 内置字幕数
	private int mEmbedSubtitleNum;
	// 当前字幕index
	private int mSubtitleIndex;// 注意这index
	// 外挂字幕路径列表
	private List<String> mExternalSubtitle;
	// 当前显示模式，值为MoviePlayer.SCREEN_FULL,MoviePlayer.SCREEN_FULL_RESIZE,MoviePlayer.SCREEN_DEFAULT
	private int mScaleMode;
	private KKPictureManager.EN_KK_PICTURE_MODE mPicMode;

	private MovieView mMovieView;
	private MoviePlayer mMoviePlayer;

	private PopupWindow mPopController;
	private PopupWindow mPopSetting;
	private PopupWindow mPopList;

	// 浮动的控制栏，就包含一个进度条与播放时间
	private View mFloatController;
	private SeekBar mPlayProgressBarFloat;
	private TextView mDurationFloat;
	private TextView mCurrentTimeFloat;

	// 用于显示字幕
	private TextView mSubtitleView;
	// 用于显示当前视频名称
	private TextView mMovieName;

	private Dialog mHintdialog;

	private int mIndex;
	private List<String> mPaths;
	RecyclerView mRecyclerView;
	private MovieListAdapter mListAdapter;
	private List<MovieInfo4Player> mMovieInfoList;

	// 进入播放器亮度被修改前的值，退出播放器恢复这值
	private int mOldBacklight;
	// 用于处理播放记录
	private PlayRecordHelper mPlayRecordHelper;

	private UsbReceiver mUsbReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*m3DManager = KK3DManager.getInstance(this);
		bTVHotKeyVersion3 = isTVHotKeyVersion3("com.konka.hotkey", this);
		mPictureManager = KKPictureManager.getInstance(this);
		init3DReceiver();*/

		initUsbReceiver();

		// 专门处理截图的handler
		mCaptureHandlerThread = new CaptureHandlerThread("CaptureHandlerThread", this);
		mCaptureHandlerThread.start();
		mCaptureHandler = new Handler(mCaptureHandlerThread.getLooper(), mCaptureHandlerThread);

		setContentView(R.layout.activity_movieplayer);
		mMovieName = (TextView) findViewById(R.id.movieplayer_name);

		// 获取屏幕宽高
		mScreenWidth = Utils.getScreenW(this);
		mScreenHeight = Utils.getScreenH(this);

		initHandler();

		initMovieView();

		initFloatintController();
		// 初始化控制栏
		initController();
		// 初始化设置窗
		initSettingWindow();
		// 初始化播放器的各种设置
		initPlayerSetting();

		showProDialog(true);
	}

	// 计算Activity获取焦点次数
	private int mWindowFocusTimes;

	// @Override
	// protected void onStart() {
	// super.onStart();
	// if (mWindowFocusTimes > 0) {
	// // 播放器显示时，显示控制栏
	// hidePopList();
	// hidePopSetting();
	// showControllerMillis(PAUSE_TIME_BEFORE_HIDE);
	// }
	// }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Trace.Debug("########MoviePlayerActivity onWindowFocusChanged");
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus == true) {
			mWindowFocusTimes++;
			// activity 首次显示
			if (mWindowFocusTimes == 1) {
				showControllerMillis(PAUSE_TIME_BEFORE_HIDE);
			}
		}

	}

	@Override
	protected void onResume() {
		Trace.Debug("########################MoviePlayerActivity onResume");
		overridePendingTransition(0, 0);
		mHandler.sendEmptyMessage(CMD_UPDATE_PROGRESS);
		super.onResume();
	}

	@Override
	protected void onPause() {
		Trace.Debug("###MoviePlayerActivity onPause");
		overridePendingTransition(0, 0);
		mMoviePlayer.pause();
		mHandler.removeMessages(CMD_CAPTURE_PICTURE);
		mHandler.removeMessages(CMD_UPDATE_PROGRESS);// 在这里removeMessages解决退出player后，还播放一会的问题
		savePlayedOffset(mMoviePlayer.getCurrent());
		super.onPause();
	}

	@Override
	protected void onStop() {
		Trace.Debug("###MoviePlayerActivity onStop");
		mMoviePlayer.pause();
		hideController();
		hidePopList();
		hidePopSetting();
		if (mHintdialog != null) {
			mHintdialog.dismiss();
		}
		mLoadingDialog.dismiss();
		savePlayerSetting();
		finish();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Trace.Debug("####MoviePlayerActivity onDestroy");
		mMoviePlayer.release();
		mMoviePlayer = null;
		mCaptureHandlerThread.quit();

		/*unregister3DReceiver();*/

		unregisterUsbReceiver();

		super.onDestroy();
	}

	String getCurrPlayingPath() {
		if (mMovieInfoList != null && !mMovieInfoList.isEmpty()) {
			return mMovieInfoList.get(mIndex).getPath();
		}
		return null;
	}

	// 初始化ui线程handler
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CMD_UPDATE_PROGRESS:
					updateProgress();
					sendEmptyMessageDelayed(CMD_UPDATE_PROGRESS, PROGRESS_UPDATE_INTERVAL);
					break;
				case CMD_HIDE_FLOATCONTROLLER:
					mFloatController.setVisibility(View.GONE);
					break;
				case CMD_HIDE_CONTROLLER:
					hideController();
					break;
				case CMD_HIDE_LIST:
					hidePopList();
					break;
				case CMD_CAPTURE_PICTURE:
					if (Configuration.IS_HWDECODER) {
						captureScreen();
					}
					break;
				case CMD_HIDE_BLACK_MASK:
					findViewById(R.id.mask).setVisibility(View.GONE);
					break;
				default:
					break;
				}
			}

		};

	}

	// 初始化浮动控制栏
	private void initFloatintController() {
		mFloatController = findViewById(R.id.floating_controller);
		mPlayProgressBarFloat = (SeekBar) mFloatController.findViewById(R.id.play_progress);
		mPlayProgressBarFloat.setMax(PLAY_PROGRESS_MAX);
		// mPlayProgressBarFloat.setOnSeekBarChangeListener(mPlayProgressChangeListener);
		mPlayProgressBarFloat.setEnabled(false);
		mDurationFloat = (TextView) mFloatController.findViewById(R.id.time_duration);
		mCurrentTimeFloat = (TextView) mFloatController.findViewById(R.id.time_progress);
	}

	// 初始化播放器的控制器
	private void initController() {

		LayoutInflater inflater = LayoutInflater.from(this);
		View contentView = inflater.inflate(R.layout.movieplayer_controller, null);
		contentView.setOnKeyListener(mControllerOnKeyListener);
		mPopController = new PopupWindow(contentView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		mPopController.setAnimationStyle(R.style.MovieContorllerAnim);
		// mPopController.setBackgroundDrawable(new BitmapDrawable());
		Tools.setPopupWindowTouchModal(mPopController, false);
		mDuration = (TextView) contentView.findViewById(R.id.time_duration);
		mCurrentTime = (TextView) contentView.findViewById(R.id.time_progress);
		mPlayProgressBar = (SeekBar) contentView.findViewById(R.id.play_progress);
		mPlayProgressBar.setMax(PLAY_PROGRESS_MAX);
		mPlayProgressBar.setOnSeekBarChangeListener(mPlayProgressChangeListener);
		mPlayProgressBar.setOnKeyListener(mControllerOnKeyListener);

		mBtnReplay = contentView.findViewById(R.id.btn_replay);
		mBtnReplay.setOnClickListener(mControllerOnClickListener);
		mBtnReplay.setOnKeyListener(mControllerOnKeyListener);
		mBtnPre = contentView.findViewById(R.id.btn_pre);
		mBtnPre.setOnKeyListener(mControllerOnKeyListener);
		mBtnPre.setOnClickListener(mControllerOnClickListener);
		mBtnNext = contentView.findViewById(R.id.btn_next);
		mBtnNext.setOnKeyListener(mControllerOnKeyListener);
		mBtnNext.setOnClickListener(mControllerOnClickListener);
		mBtnList = contentView.findViewById(R.id.btn_list);
		mBtnList.setOnKeyListener(mControllerOnKeyListener);
		mBtnList.setOnClickListener(mControllerOnClickListener);
		mBtnSetting = contentView.findViewById(R.id.btn_setting);
		mBtnSetting.setOnClickListener(mControllerOnClickListener);
		mBtnSetting.setOnKeyListener(mControllerOnKeyListener);

		// 设置热区，鼠标进入热区显示控制栏，鼠标到非热区，控制栏消失
		View hotspot = findViewById(R.id.pointer_hotspot);
		View notHotspot = findViewById(R.id.not_pointer_hotspot);
		hotspot.setOnHoverListener(mHotspotOnHoverListener);
		notHotspot.setOnHoverListener(mHotspotOnHoverListener);

	}

	private void initSettingWindow() {

		LayoutInflater inflater = LayoutInflater.from(this);
		View contentView = inflater.inflate(R.layout.movieplayer_setting, null);
		mPopSetting = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopSetting.setAnimationStyle(R.style.MovieSettingAnim);
		mPopSetting.setBackgroundDrawable(new BitmapDrawable());
		Tools.setPopupWindowTouchModal(mPopSetting, false);
		mSettingSubtitle = (LinearLayout) contentView.findViewById(R.id.setting_subtitle);
		mSettingAudioTrack = (LinearLayout) contentView.findViewById(R.id.setting_audio_track);
		mSettingScaleMode = (LinearLayout) contentView.findViewById(R.id.setting_scale_mode);
		mSettingPicMode = (LinearLayout) contentView.findViewById(R.id.setting_picture_mode);
		mSettingLoopMode = (LinearLayout) contentView.findViewById(R.id.setting_loop_mode);
		mBacklightSeekBar = (SeekBar) contentView.findViewById(R.id.setting_backlight_seekbar);
		mBacklightValue = (TextView) contentView.findViewById(R.id.setting_backlight_value);

		mBacklightSeekBar.setMax(100);
		mBacklightSeekBar.setOnSeekBarChangeListener(mBacklightSeekBarChangeListener);
		mBacklightSeekBar.setKeyProgressIncrement(1);

		mSettingSubtitle.setOnKeyListener(mSettingOnKeyListener);
		mSettingAudioTrack.setOnKeyListener(mSettingOnKeyListener);
		mSettingScaleMode.setOnKeyListener(mSettingOnKeyListener);
		mSettingPicMode.setOnKeyListener(mSettingOnKeyListener);
		mSettingLoopMode.setOnKeyListener(mSettingOnKeyListener);
		mBacklightSeekBar.setOnKeyListener(mSettingOnKeyListener);

		mSettingSubtitle.setOnClickListener(mSettingOnClickListener);
		mSettingAudioTrack.setOnClickListener(mSettingOnClickListener);
		mSettingScaleMode.setOnClickListener(mSettingOnClickListener);
		mSettingPicMode.setOnClickListener(mSettingOnClickListener);
		mSettingLoopMode.setOnClickListener(mSettingOnClickListener);

	}

	private void initListWindow() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View contentView = inflater.inflate(R.layout.movieplayer_list, null);
		mPopList = new PopupWindow(contentView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		mPopList.setAnimationStyle(R.style.MovieListAnim);
		mPopList.setBackgroundDrawable(new BitmapDrawable());
		Tools.setPopupWindowTouchModal(mPopList, false);
		mRecyclerView = (RecyclerView) contentView.findViewById(R.id.movieplayer_recycler_view);
		mRecyclerView.setOnTouchListener(mListOnTouchListener);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		mMovieInfoList = getMovieInfoList(mPaths);
		mListAdapter = new MovieListAdapter(mMovieInfoList, this);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setAdapter(mListAdapter);
	}

	// 初始化movieview
	private void initMovieView() {

		mMovieView = (MovieView) findViewById(R.id.movieview);
		mMovieView.getHolder().addCallback(mCallback);
		mMoviePlayer = mMovieView.getMoviePlayer();

		// 设置显示文字幕的SurfaceView
		SurfaceView subtitleSv = (SurfaceView) findViewById(R.id.subtitle_surface);
		SurfaceHolder holder = subtitleSv.getHolder();
		holder.setFormat(PixelFormat.RGBA_8888);// 缺这句Mediaplayer 会抛错
		holder.addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// 设置显示文字幕的SurfaceView
				mMoviePlayer.setSubtitleDisplay(holder);
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				// TODO Auto-generated method stub
			}
		});

		// 设置监听器
		mMoviePlayer.setOnPreparedListener(mPreparedListener);
		mMoviePlayer.setOnTimedTextListener(mOnTimedTextListener);
		mMoviePlayer.setOnCompletionListener(mOnCompletionListener);
		mMoviePlayer.setOnErrorListener(mErrorListener);
		mMoviePlayer.setOnSetDataSourceFailListener(mOnSetDataSourceFailListener);
		mMoviePlayer.setOnInfoListener(mInfoListener);

	}

	// 从intent中获取播放数据
	private void setSourceFromIntent(Intent intent) {
		Trace.Debug("#### setSourceFromIntent");
		mIndex = intent.getIntExtra(Constant.PLAY_INDEX, 0);
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = intent.getData();
			if (uri == null) {
				finish();
				return;
			}
			mPaths = new ArrayList<String>();
			mPaths.add(uri.getPath());
		}
		else {
			mPaths = intent.getStringArrayListExtra(Constant.PLAY_PATHS);
			if (mPaths == null) {
				finish();
				return;
			}
		}
		mMoviePlayer.setDataSourceList(mPaths, mIndex);
	}

	// 根据路径获取视频信息
	private List<MovieInfo4Player> getMovieInfoList(List<String> paths) {
		List<MovieInfo4Player> movieInfoList = null;
		if (paths == null) {
			return null;
		}
		for (String path : paths) {
			if (movieInfoList == null) {
				movieInfoList = new ArrayList<MovieInfo4Player>();
			}
			movieInfoList.add(new MovieInfo4Player(path));
		}
		return movieInfoList;
	}

	// 更新播放进度与播放时间
	private void updateProgress() {
		if(mMoviePlayer == null) {
			return;
		}
		// 转化为long是为是避免下面计算相乘时数字过大而越界
		long msec = (long) mMoviePlayer.getCurrent();
		// 设置播放时间
		String currTime = Tools.formatCurrMsec((int) msec);
		mCurrentTime.setText(currTime);
		mCurrentTimeFloat.setText(currTime);

		// 设置进度条进度
		int duration = mMoviePlayer.getDuration();
		if (duration != 0) {
			long offset = (long) (PLAY_PROGRESS_MAX * msec / duration);
			mPlayProgressBar.setProgress((int) offset);
			mPlayProgressBarFloat.setProgress((int) offset);
		}

	}

	// 根据进度条进度设置播放时间
	private void setProgressByDrag(SeekBar seekBar) {
		// 转化为long是为是避免下面计算相乘时数字过大而越界
		long offset = (long) seekBar.getProgress();
		mPlayProgressBar.setProgress((int) offset);
		mPlayProgressBarFloat.setProgress((int) offset);
		Trace.Debug("offset:" + offset);
		long duration = (long) mMoviePlayer.getDuration();
		Trace.Debug("duration:" + duration);
		long currTimeInMsec = duration * offset / PLAY_PROGRESS_MAX;
		Trace.Debug("目标时间：" + currTimeInMsec);
		mMoviePlayer.seek((int) currTimeInMsec);
		mCurrentTime.setText(Tools.formatCurrMsec((int) currTimeInMsec));
		mCurrentTimeFloat.setText(Tools.formatCurrMsec((int) currTimeInMsec));
	}

	// 显示控制栏，隐藏控制栏 转换
	private void toggleController() {
		if (mPopController == null) {
			return;
		}
		if (mPopController.isShowing()) {
			hideController();
		}
		else {
			showControllerMillis(PAUSE_TIME_BEFORE_HIDE);
		}
	}

	private View controllerFocusedView;// 记录上次焦点位置

	// 显示控制栏不自动隐藏
	private void showController() {
		if (mHintdialog != null && mHintdialog.isShowing()) {
			return;
		}
		if (mPopController != null) {
			mHandler.removeMessages(CMD_HIDE_CONTROLLER);
			mPopController.showAtLocation(mMovieView, Gravity.BOTTOM, 0, 0);
			if (controllerFocusedView == null) {
				mBtnSetting.requestFocus();
			}
			else {
				controllerFocusedView.requestFocus();
			}
			mFloatController.setVisibility(View.GONE);
		}
	}

	// 显示控制栏一段时间（几秒后隐藏）
	void showControllerMillis(long time) {
		if (mPopController != null && !mPopController.isShowing()) {
			showController();
		}
		mHandler.removeMessages(CMD_HIDE_CONTROLLER);
		mHandler.sendEmptyMessageDelayed(CMD_HIDE_CONTROLLER, time);
	}

	// 隐藏控制栏
	private void hideController() {
		if (mPopController != null) {
			mHandler.removeMessages(CMD_HIDE_CONTROLLER);
			if (mPopController.isShowing()) {
				controllerFocusedView = mPopController.getContentView().findFocus();
				Trace.Debug("focused :" + controllerFocusedView);
			}
			mPopController.dismiss();
		}
	}

	// 显示悬浮进度条一段时间（几秒后隐藏悬浮进度条）
	private void showFloatControllerMillis(long time) {
		mFloatController.setVisibility(View.VISIBLE);
		mFloatController.requestFocus();
		mHandler.removeMessages(CMD_HIDE_FLOATCONTROLLER);
		mHandler.sendEmptyMessageDelayed(CMD_HIDE_FLOATCONTROLLER, time);
	}

	// 显示设置窗，隐藏设置窗 转换
	private void togglePopSetting() {
		if (mPopSetting == null) {
			return;
		}
		if (mPopSetting.isShowing()) {
			hidePopSetting();
		}
		else {
			showPopSetting();
		}
	}

	private int POP_SETTING_Y;

	// 显示设置窗
	private void showPopSetting() {
		if (mPopSetting != null) {
			if (POP_SETTING_Y == 0) {
				POP_SETTING_Y = Tools.dip2px(this, 140);
			}
			mPopSetting.showAtLocation(mMovieView, Gravity.TOP, 0, POP_SETTING_Y);
		}
	}

	// 隐藏设置窗
	private void hidePopSetting() {
		if (mPopSetting != null) {
			mPopSetting.dismiss();
		}
	}

	// 显示列表窗，隐藏列表窗 转换
	private void togglePopList() {
		if (mPopList == null) {
			return;
		}
		if (mPopList.isShowing()) {
			hidePopList();
		}
		else {
			showPopListMillis(PAUSE_TIME_BEFORE_HIDE);
		}
	}

	// 显示列表窗
	private void showPopList() {
		if (mPopList != null) {
			mPopList.showAtLocation(mMovieView, Gravity.BOTTOM, 0, 0);
			mFloatController.setVisibility(View.GONE);
			// 设置当前播放视频条目获取焦点
			mListAdapter.setFocus(mIndex);
		}
	}

	// 显示列表窗一段时间（几秒后隐藏）
	void showPopListMillis(long time) {
		if (mPopList != null && !mPopList.isShowing()) {
			showPopList();
		}
		mHandler.removeMessages(CMD_HIDE_LIST);
		mHandler.sendEmptyMessageDelayed(CMD_HIDE_LIST, time);
	}

	// 隐藏列表窗
	void hidePopList() {
		if (mPopList != null) {
			mHandler.removeMessages(CMD_HIDE_LIST);
			mPopList.dismiss();
		}
	}

	// 隐藏暂停标志
	private void hidePauseTag() {
		Trace.Debug("hide pause tag");
		findViewById(R.id.movieplayer_pause_tag).setVisibility(View.GONE);
		mMovieName.setVisibility(View.GONE);
		mPlayProgressBar.setThumb(getResources().getDrawable(R.drawable.player_seekbar_thumb_play));
		mPlayProgressBarFloat.setThumb(getResources().getDrawable(R.drawable.player_seekbar_thumb_play));
	}

	// 显示暂停标志
	private void showPauseTag() {
		Trace.Debug("show  pause tag");
		findViewById(R.id.movieplayer_pause_tag).setVisibility(View.VISIBLE);
		mMovieName.setVisibility(View.VISIBLE);
		mPlayProgressBar.setThumb(getResources().getDrawable(R.drawable.player_seekbar_thumb_pause));
		mPlayProgressBarFloat.setThumb(getResources().getDrawable(R.drawable.player_seekbar_thumb_pause));
	}

	// 出错时调用，显示提示窗口
	private void showHintDialog(String text) {
		hideController();
		hidePopList();
		hidePopSetting();
		if (mHintdialog == null) {
			mHintdialog = new Dialog(this, R.style.delete_dialog);
			Window window = mHintdialog.getWindow();
			window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		}
		LayoutInflater inflater = LayoutInflater.from(this);
		View contentView = inflater.inflate(R.layout.movie_error_dialog, null);
		mHintdialog.setContentView(contentView);
		mHintdialog.setCancelable(false);
		contentView.findViewById(R.id.decideButton).requestFocus();
		contentView.findViewById(R.id.decideButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mHintdialog.dismiss();
				// 退出MoviePlayerActivity
				MoviePlayerActivity.this.finish();
			}
		});

		TextView message = (TextView) contentView.findViewById(R.id.info_message);
		message.setText(text + "，" + getResources().getString(R.string.quit_movie_player));

		mHintdialog.show();
	}

	// 显示或隐藏progress dialog， true时显示
	private void showProDialog(boolean toShow) {
		// 续播功能需要在start之前seek，而这样会引起scale画面跳闪，通过蒙上黑色蒙板R.id.mask来遮档跳闪
		if (toShow) {
			findViewById(R.id.mask).setVisibility(View.VISIBLE);
			if (mLoadingDialog == null) {
				mLoadingDialog = new LoadingDialog(this, R.style.progressDialog_holo);
				mLoadingDialog.setMessageText(getResources().getString(R.string.progress_dialog_movie_loading));
			}
			mLoadingDialog.show();
		}
		else {
			mLoadingDialog.dismiss();
			mHandler.sendEmptyMessageDelayed(CMD_HIDE_BLACK_MASK, 800);
		}
	}

	// 播放暂停间切换
	private void togglePlay() {
		if (mMoviePlayer.isPlaying()) {
			mMoviePlayer.pause();
			showPauseTag();
		}
		else {
			mMoviePlayer.start();
			hidePauseTag();
		}
	}

	// 上一个按钮操作
	private void prev() {
		if (mMoviePlayer == null) {
			return;
		}
		savePlayedOffset(mMoviePlayer.getCurrent());
		int loopMode = mMoviePlayer.getLoopMode();
		showProDialog(true);
		if (loopMode == MoviePlayer.LOOP_ALL || loopMode == MoviePlayer.LOOP_SINGLE) {
			mMoviePlayer.prev();
		}
		else if (loopMode == MoviePlayer.LOOP_SHUFFLE) {
			mMoviePlayer.playShuffle();
		}
		mIndex = mMoviePlayer.getPosition();// prepared失败时索引也要变化
		hidePauseTag();
	}

	// 下一个按钮操作
	private void next() {
		if (mMoviePlayer == null) {
			return;
		}
		savePlayedOffset(mMoviePlayer.getCurrent());
		int loopMode = mMoviePlayer.getLoopMode();
		showProDialog(true);
		if (loopMode == MoviePlayer.LOOP_ALL || loopMode == MoviePlayer.LOOP_SINGLE) {
			mMoviePlayer.next();
		}
		else if (loopMode == MoviePlayer.LOOP_SHUFFLE) {
			mMoviePlayer.playShuffle();
		}
		mIndex = mMoviePlayer.getPosition();// prepared失败时索引也要变化
		hidePauseTag();
	}

	/**
	 * @param position
	 */
	void play(int position) {
		savePlayedOffset(mMoviePlayer.getCurrent());
		showProDialog(true);
		mMoviePlayer.reset();
		mMoviePlayer.setDataSourceList(mPaths, position);
		mIndex = position;
		mMoviePlayer.initialize();
		mMoviePlayer.start();
		hidePauseTag();
	}

	private void initScaleMode() {
		SettingLoader settingLoader = new SettingLoader(this);
		mScaleMode = settingLoader.getSMFromPreferences();
	}

	/*
	 * 改变屏显模式
	 * 
	 * @param next false向前改变，true向后改变
	 */
	private void switchScaleMode(boolean next) {
		if (mScaleMode == MoviePlayer.SCREEN_FULL) {
			if (next == true) {
				mScaleMode = MoviePlayer.SCREEN_DEFAULT;
			}
			else {
				mScaleMode = MoviePlayer.SCREEN_FULL_RESIZE;
			}

		}
		else if (mScaleMode == MoviePlayer.SCREEN_DEFAULT) {
			if (next == true) {
				mScaleMode = MoviePlayer.SCREEN_FULL_RESIZE;
			}
			else {
				mScaleMode = MoviePlayer.SCREEN_FULL;
			}

		}
		else if (mScaleMode == MoviePlayer.SCREEN_FULL_RESIZE) {
			if (next == true) {
				mScaleMode = MoviePlayer.SCREEN_FULL;
			}
			else {
				mScaleMode = MoviePlayer.SCREEN_DEFAULT;
			}

		}
		setScaleMode(mScaleMode);
	}

	// 设置屏显模式
	private void setScaleMode(int scaleMode) {
		String mode = null;
		if (mScaleMode == MoviePlayer.SCREEN_DEFAULT) {
			mode = getResources().getString(R.string.screen_default);
		}
		else if (mScaleMode == MoviePlayer.SCREEN_FULL) {
			mode = getResources().getString(R.string.screen_full);
		}
		else if (mScaleMode == MoviePlayer.SCREEN_FULL_RESIZE) {
			mode = getResources().getString(R.string.screen_full_resize);
		}

		TextView textView = (TextView) mSettingScaleMode.findViewById(R.id.setting_value);
		textView.setText(mode);
		mMovieView.switchScreenDisplayMode(0, scaleMode, 0, 0, mScreenWidth, mScreenHeight);
	}

	// 显示字幕
	private void showSubtitle(String text) {
		if (mSubtitleView == null) {
			mSubtitleView = (TextView) findViewById(R.id.subtitle);
		}
		mSubtitleView.setText(text);
	}

	// 初始化播放器设置，这部分设置在播放器更换播放资源时不会改变
	private void initPlayerSetting() {
		// 初始屏显模式
		initScaleMode();
		// 设置图像模式
		initPictureMode();
		// 设置循环模式
		initLoopMode();
		// 设置背光
		initBacklight();
	}

	// 初始化视频设置，更换视频播放资源要调用此方法来重新设定
	private void initMovieSetting() {
		mIndex = mMoviePlayer.getPosition();
		// 设置字幕
		initSubtitle();
		// 设置音轨
		initAudioTrack();
		// 根据播放器当前屏显模式显示
		setScaleMode(mScaleMode);
		// 设置暂停时显示的名称
		mMovieName.setText(mMovieInfoList.get(mIndex).getName());
		// 续播
		mMoviePlayer.seek(getRecordedPlayedOffset());
		// 刷新播放列表
		mListAdapter.setPlayingPosition(mIndex);

	}

	// 设置字幕
	private void initSubtitle() {
		mSubtitleIndex = 0;
		mSubtitleNum = 0;
		SubtitleTool subtitleTool = new SubtitleTool(mPaths.get(mIndex));
		mExternalSubtitle = subtitleTool.getSubtitlePaths();
		mEmbedSubtitleNum = mMoviePlayer.getSubtitleTrackNumber();
		mSubtitleNum = mEmbedSubtitleNum;
		if (mExternalSubtitle != null && !mExternalSubtitle.isEmpty()) {
			mSubtitleNum = mEmbedSubtitleNum + mExternalSubtitle.size();
			mMoviePlayer.setSubtitleDataSource(mExternalSubtitle.get(0));
		}
		Trace.Info("内置字幕：" + mEmbedSubtitleNum + "   总字幕：" + mSubtitleNum);

		View valueArea = mSettingSubtitle.findViewById(R.id.value_area);// 带箭头
		View noneArea = mSettingSubtitle.findViewById(R.id.none);// 显示“无” 不带箭头
		if (mSubtitleNum == 0) {
			noneArea.setVisibility(View.VISIBLE);
			valueArea.setVisibility(View.GONE);
			return;
		}
		else {
			// 若有字幕，默认设置第一个
			mMoviePlayer.switchSubtitle(0);
			mSubtitleIndex = 0;
			mMoviePlayer.turnOnSubtitle();

			noneArea.setVisibility(View.GONE);
			valueArea.setVisibility(View.VISIBLE);
			TextView textView = (TextView) mSettingSubtitle.findViewById(R.id.setting_value);
			String text = getResources().getString(R.string.subtitle);
			textView.setText(text + (mSubtitleIndex + 1));
		}
	}

	/*
	 * 切换字幕
	 * 
	 * @param next true 向后切换，false 向前切换
	 */
	private void switchSubtitle(boolean next) {
		if (mSubtitleNum == 0) {
			return;
		}
		mMoviePlayer.turnOnSubtitle();
		if (next == true) {
			mSubtitleIndex++;
			if (mSubtitleIndex > mSubtitleNum) {
				mSubtitleIndex = 0;
			}
		}
		else {
			mSubtitleIndex--;
			if (mSubtitleIndex < 0) {
				mSubtitleIndex = mSubtitleNum;
			}
		}

		if (mSubtitleIndex >= 0 && mSubtitleIndex <= mEmbedSubtitleNum - 1) {
			// 此区间是内置字幕
			mMoviePlayer.switchSubtitle(mSubtitleIndex);
		}
		else if (mSubtitleIndex >= mEmbedSubtitleNum && mSubtitleIndex < mSubtitleNum) {
			// 更换外挂字幕
			int externalIntex = mSubtitleIndex - mEmbedSubtitleNum;
			mMoviePlayer.setSubtitleDataSource(mExternalSubtitle.get(externalIntex));
			mMoviePlayer.switchSubtitle(mEmbedSubtitleNum);
		}
		else if (mSubtitleIndex == mSubtitleNum) {
			// “字幕关”状态，关字幕
			mMoviePlayer.turnOffSubtitle();
		}

		TextView textView = (TextView) mSettingSubtitle.findViewById(R.id.setting_value);
		String text = getResources().getString(R.string.subtitle);

		if (mSubtitleIndex >= 0 && mSubtitleIndex <= mSubtitleNum - 1) {
			textView.setText(text + (mSubtitleIndex + 1));
		}
		else if (mSubtitleIndex == mSubtitleNum) {
			text = getResources().getString(R.string.subtitle_off);
			textView.setText(text);
		}

	}

	// // 打开或关闭字幕
	// private void toggleSubtitle() {
	// if (mSubtitleNum == 0) {
	// return;
	// }
	// TextView textView = (TextView)
	// mSettingSubtitle.findViewById(R.id.setting_value);
	// String off = getResources().getString(R.string.subtitle_off);
	// boolean status = textView.getText().equals(off);
	// if (status) {
	// mMoviePlayer.turnOnSubtitle();
	// String text = getResources().getString(R.string.subtitle);
	// textView.setText(text + (mSubtitleIndex + 1));
	// }
	// else {
	// mMoviePlayer.turnOffSubtitle();
	// textView.setText(off);
	// }
	//
	// }

	// 设置音轨
	private void initAudioTrack() {
		int audioTrackNum = mMoviePlayer.getAudioTrackNumber();
		Trace.Info("音轨个数：" + audioTrackNum);
		View valueArea = mSettingAudioTrack.findViewById(R.id.value_area);// 带箭头
		View noneArea = mSettingAudioTrack.findViewById(R.id.none);// 显示“无” 不带箭头
		if (audioTrackNum == 0) {
			valueArea.setVisibility(View.GONE);
			noneArea.setVisibility(View.VISIBLE);
			return;
		}

		valueArea.setVisibility(View.VISIBLE);
		noneArea.setVisibility(View.GONE);
		// 默认第一个音轨
		mMoviePlayer.switchTrack(0);
		TextView textView = (TextView) mSettingAudioTrack.findViewById(R.id.setting_value);
		String text = getResources().getString(R.string.audio_track);
		textView.setText(text + 1);
	}

	/*
	 * 切换音轨
	 * 
	 * @param next true 向后切换，false 向前切换
	 */
	private void switchAudioTrack(boolean next) {
		Trace.Debug("####switchAudioTrack");
		int audioTrackNum = mMoviePlayer.getAudioTrackNumber();
		if (audioTrackNum == 0) {
			return;
		}
		int trackIndex = mMoviePlayer.getTrack();
		if (next == true) {
			trackIndex++;
		}
		else {
			trackIndex--;
		}
		if (trackIndex < 0) {
			trackIndex = audioTrackNum - 1;
		}
		else if (trackIndex > audioTrackNum - 1) {
			trackIndex = 0;
		}
		mMoviePlayer.switchTrack(trackIndex);
		TextView textView = (TextView) mSettingAudioTrack.findViewById(R.id.setting_value);
		String text = getResources().getString(R.string.audio_track);
		textView.setText(text + (trackIndex + 1));
	}

	// 初始化图像模式
	private void initPictureMode() {
		SettingLoader settingLoader = new SettingLoader(this);
		String picMode = settingLoader.getPMFromPreferences();
		mPicMode = EN_KK_PICTURE_MODE.NORMAL;
		if (picMode.equals(SettingLoader.PICTURE_MODE_DYNAMIC)) {
			mPicMode = EN_KK_PICTURE_MODE.DYNAMIC;
		}
		else if (picMode.equals(SettingLoader.PICTURE_MODE_VIVID)) {
			mPicMode = EN_KK_PICTURE_MODE.VIVID;
		}
		else if (picMode.equals(SettingLoader.PICTURE_MODE_SOFT)) {
			mPicMode = EN_KK_PICTURE_MODE.SOFT;
		}
		setPictureMode(mPicMode);
	}

	/*
	 * 切换图像模式
	 * 
	 * @param next
	 */
	private void switchPictureMode(boolean next) {
		mPicMode = mMoviePlayer.getPictureMode();
		switch (mPicMode) {
		case DYNAMIC:
			if (next == true) {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.VIVID;
			}
			else {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.SOFT;
			}
			break;
		case NORMAL:
			if (next == true) {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.SOFT;
			}
			else {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.VIVID;
			}
			break;
		case SOFT:
			if (next == true) {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.DYNAMIC;
			}
			else {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.NORMAL;
			}
			break;
		case VIVID:
			if (next == true) {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.NORMAL;
			}
			else {
				mPicMode = KKPictureManager.EN_KK_PICTURE_MODE.DYNAMIC;
			}
			break;
		}
		setPictureMode(mPicMode);
	}

	private void setPictureMode(KKPictureManager.EN_KK_PICTURE_MODE mode) {
		String text = "";
		switch (mode) {
		case DYNAMIC:
			text = getResources().getString(R.string.picture_dynamic);
			break;
		case NORMAL:
			text = getResources().getString(R.string.picture_normal);
			break;
		case SOFT:
			text = getResources().getString(R.string.picture_soft);
			break;
		case VIVID:
			text = getResources().getString(R.string.picture_vivid);
			break;
		}
		mMoviePlayer.setPictureMode(mode);
		TextView textView = (TextView) mSettingPicMode.findViewById(R.id.setting_value);
		textView.setText(text);
	}

	private void initLoopMode() {
		if (mMoviePlayer == null) {
			return;
		}
		SettingLoader settingLoader = new SettingLoader(this);
		int loopMode = settingLoader.getLMFromPreferences();
		setLoopMode(loopMode);
	}

	private void switchLoopMode(boolean next) {
		int mode = mMoviePlayer.getLoopMode();
		switch (mode) {
		case MoviePlayer.LOOP_ALL:
			if (next == true) {
				mode = MoviePlayer.LOOP_SHUFFLE;
			}
			else {
				mode = MoviePlayer.LOOP_SINGLE;
			}
			break;
		case MoviePlayer.LOOP_SINGLE:
			if (next == true) {
				mode = MoviePlayer.LOOP_ALL;
			}
			else {
				mode = MoviePlayer.LOOP_SHUFFLE;
			}
			break;
		case MoviePlayer.LOOP_SHUFFLE:
			if (next == true) {
				mode = MoviePlayer.LOOP_SINGLE;
			}
			else {
				mode = MoviePlayer.LOOP_ALL;
			}
			break;
		}
		setLoopMode(mode);
	}

	private void setLoopMode(int mode) {
		String text = "";
		switch (mode) {
		case MoviePlayer.LOOP_ALL:
			text = getResources().getString(R.string.loop_all);
			break;
		case MoviePlayer.LOOP_SINGLE:
			text = getResources().getString(R.string.loop_single);
			break;
		case MoviePlayer.LOOP_SHUFFLE:
			text = getResources().getString(R.string.loop_shuffle);
			break;
		}
		mMoviePlayer.setLoopMode(mode);
		TextView textView = (TextView) mSettingLoopMode.findViewById(R.id.setting_value);
		textView.setText(text);

	}

	// 初始播放器背光
	private void initBacklight() {
		SettingLoader backlightControl = new SettingLoader(this);
		mOldBacklight = backlightControl.getCurBacklight();
		Trace.Info("#######本机原亮度：" + mOldBacklight);
		int value = backlightControl.getBacklightFromPreferences();
		Trace.Info("#######播放器记录亮度：" + value);
		if (value == -1) {
			value = mOldBacklight;
		}
		backlightControl.setBacklight(value);
		mBacklightSeekBar.setProgress(value);
		mBacklightValue.setText(value + "");
	}

	private void savePlayerSetting() {
		SettingLoader settingLoader = new SettingLoader(this);
		// 保存播放器背光并恢复原背光
		int value = settingLoader.getCurBacklight();
		settingLoader.setBacklightToPreferences(value);
		settingLoader.setBacklight(mOldBacklight);

		settingLoader.setLMFromPreferences(mMoviePlayer.getLoopMode());
		settingLoader.setSMToPreferences(mScaleMode);
		String picMode = "";
		switch (mPicMode) {
		case DYNAMIC:
			picMode = SettingLoader.PICTURE_MODE_DYNAMIC;
			break;
		case NORMAL:
			picMode = SettingLoader.PICTURE_MODE_NORMAL;
			break;
		case SOFT:
			picMode = SettingLoader.PICTURE_MODE_SOFT;
			break;
		case VIVID:
			picMode = SettingLoader.PICTURE_MODE_VIVID;
			break;
		}
		settingLoader.setPMToPreferences(picMode);
	}

	// 保存当前视频播放进度
	private void savePlayedOffset(int offset) {
		if (mPlayRecordHelper == null) {
			mPlayRecordHelper = new PlayRecordHelper(this);
		}
		String path = mPaths.get(mIndex);
		mPlayRecordHelper.setPlayerOffset(path, offset);

	}

	// 从存储获取当索引视频的播放进度记录
	private int getRecordedPlayedOffset() {
		if (mPlayRecordHelper == null) {
			mPlayRecordHelper = new PlayRecordHelper(this);
		}
		String path = mPaths.get(mIndex);
		return mPlayRecordHelper.getPlayedOffset(path);
	}

	// 记录当前播放视频已播完
	private void setPlayCompleted() {
		if (mPlayRecordHelper == null) {
			mPlayRecordHelper = new PlayRecordHelper(this);
		}
		String path = mPaths.get(mIndex);
		mPlayRecordHelper.setPlayCompleted(path);
	}

	// 截屏
	private void captureScreen() {
		// 目前只处理截图一个事件，发个空消息即可
		mCaptureHandler.sendMessage(mCaptureHandler.obtainMessage());
	}

	private SurfaceHolder.Callback mCallback = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Trace.Info("###############surfaceDestroyed");
			if (mMoviePlayer != null) {
				mMoviePlayer.pause();
			}
			finish();//在这finish避免home键回主页然后快速返回应用时，因onStop没运行到MoviePlayerActivity没退出
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Trace.Debug("movieview surface created");
			if (mMoviePlayer != null) {
				showProDialog(true);
				// 设置播放资源
				setSourceFromIntent(getIntent());
				mMoviePlayer.initialize();
				mMoviePlayer.start();
				// 初始化视频列表
				initListWindow();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Trace.Info("###############surfaceChanged");
		}
	};

	private OnSetDataSourceFailListener mOnSetDataSourceFailListener = new OnSetDataSourceFailListener() {
		@Override
		public void onSetDataSourceFail(EMediaPlayer mp, String sourcePath, Exception e) {
			showProDialog(false);
			// 弹窗提示
			showHintDialog(getResources().getString(R.string.video_not_support));
		}
	};

	private OnTimedTextListener mOnTimedTextListener = new OnTimedTextListener() {

		@Override
		public void onTimedText(MediaPlayer mp, String text) {
			if (text != null) {
				showSubtitle(text);
			}
			else {
				showSubtitle("");
			}

		}
	};

	private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(EMediaPlayer mp) {
			MoviePlayerActivity.this.setPlayCompleted();
			MoviePlayerActivity.this.savePlayedOffset(0);
		}
	};

	// 播放进度条的OnSeekBarChangeListener
	private OnSeekBarChangeListener mPlayProgressChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Trace.Debug("onStopTrackingTouch");
			mHandler.sendEmptyMessage(CMD_UPDATE_PROGRESS);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Trace.Debug("onStartTrackingTouch");
			mHandler.removeMessages(CMD_UPDATE_PROGRESS);
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser == false) {
				return;
			}
			setProgressByDrag(seekBar);
		}
	};

	private OnSeekBarChangeListener mBacklightSeekBarChangeListener = new OnSeekBarChangeListener() {
		private KKPictureManager picManager;

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (picManager == null) {
				picManager = KKPictureManager.getInstance(MoviePlayerActivity.this);
			}
			if (fromUser) {
				picManager.setBacklight((short) progress);
				mBacklightValue.setText(progress + "");
				Trace.Debug("backlight value = "
						+ KKPictureManager.getInstance(MoviePlayerActivity.this).getBacklight());
			}
		}
	};

	private OnClickListener mControllerOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_replay:
				mMoviePlayer.replay();
				hidePauseTag();
				break;
			case R.id.btn_setting:
				showPopSetting();
				hideController();
				break;
			case R.id.btn_list:
				showPopListMillis(PAUSE_TIME_BEFORE_HIDE);
				hideController();
				break;
			case R.id.btn_pre:
				prev();
				break;
			case R.id.btn_next:
				next();
				break;
			default:
				break;
			}
		}
	};

	private OnClickListener mSettingOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.setting_subtitle:
				switchSubtitle(true);
				break;
			case R.id.setting_audio_track:
				switchAudioTrack(true);
				break;
			case R.id.setting_scale_mode:
				mSettingScaleMode.setEnabled(false);
				// scale过程用时较长，设置view对key的响应间隔，防止按键事件堆叠
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						mSettingScaleMode.setEnabled(true);
					}
				}, 500);
				switchScaleMode(true);
				break;
			case R.id.setting_picture_mode:
				switchPictureMode(true);
				break;
			case R.id.setting_loop_mode:
				switchLoopMode(true);
				break;
			default:
				break;
			}
		}
	};

	private OnKeyListener mControllerOnKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
						|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
						|| keyCode == KeyEvent.KEYCODE_ENTER) {
					showControllerMillis(PAUSE_TIME_BEFORE_HIDE);
				}
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
					hideController();
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
					hideController();
					return true;
				}

				switch (v.getId()) {
				case R.id.play_progress: {

					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						mMoviePlayer.fb();
						return true;
					}
					else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						mMoviePlayer.ff();
						return true;
					}
					else if (keyCode == KeyEvent.KEYCODE_ENTER && event.getRepeatCount() == 0) {
						togglePlay();
						return true;
					}
				}
					break;

				default:
				}
			}
			return false;
		}
	};

	private OnKeyListener mSettingOnKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {

				switch (v.getId()) {
				case R.id.setting_scale_mode:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getRepeatCount() == 0) {
						mSettingScaleMode.setEnabled(false);
						// scale过程用时较长，设置view对key的响应间隔，防止按键事件堆叠
						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								mSettingScaleMode.setEnabled(true);
							}
						}, 500);
						switchScaleMode(false);
						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getRepeatCount() == 0) {
						mSettingScaleMode.setEnabled(false);
						// scale过程用时较长，设置view对key的响应间隔，防止按键事件堆叠
						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								mSettingScaleMode.setEnabled(true);
							}
						}, 500);
						switchScaleMode(true);
						return true;
					}
					break;
				case R.id.setting_subtitle:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getRepeatCount() == 0) {
						switchSubtitle(false);
						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getRepeatCount() == 0) {
						switchSubtitle(true);
						return true;
					}
					break;
				case R.id.setting_audio_track:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getRepeatCount() == 0) {
						switchAudioTrack(false);
						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getRepeatCount() == 0) {
						switchAudioTrack(true);
						return true;
					}
					break;
				case R.id.setting_picture_mode:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getRepeatCount() == 0) {
						switchPictureMode(false);
						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getRepeatCount() == 0) {
						switchPictureMode(true);
						return true;
					}
					break;
				case R.id.setting_loop_mode:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getRepeatCount() == 0) {
						switchLoopMode(false);
						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getRepeatCount() == 0) {
						switchLoopMode(true);
						return true;
					}
					break;
				default:
					break;
				}
			}
			return false;
		}
	};

	// OnPreparedListener for mediaplayer
	private OnPreparedListener mPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(EMediaPlayer mp) {

			mHandler.removeMessages(CMD_CAPTURE_PICTURE);
			mHandler.sendEmptyMessageDelayed(CMD_CAPTURE_PICTURE, 5000);

			// 开始计算并显示控制栏上的时间
			int msec = mMoviePlayer.getDuration();
			mDuration.setText(Tools.formatMsec(msec));
			mDurationFloat.setText(Tools.formatMsec(msec));

			mHandler.removeMessages(CMD_UPDATE_PROGRESS);// 不加这句，上次会遗留message
			mHandler.sendEmptyMessageDelayed(CMD_UPDATE_PROGRESS, PROGRESS_UPDATE_INTERVAL);
			// 配置此视频的一些设置
			initMovieSetting();
			showProDialog(false);
			/******** play 3D ******/
			/*if (mMoviePlayer.getKKMediaPlayer() != null
					&& mMoviePlayer.getKKMediaPlayer().getMediaPlayerVersion() == KKMediaPlayer.VERSION_MSTAR) {
				Trace.Debug("bTVHotKeyVersion3 == " + bTVHotKeyVersion3);
				if (bTVHotKeyVersion3 == true) {
					autoDect3DVersion3();
				}
				else {
					autoDect3D();
				}
			}*/
		}
	};

	private OnErrorListener mErrorListener = new OnErrorListener() {

		@Override
		public boolean onError(EMediaPlayer mp, int what, int extra) {
			mHandler.removeMessages(CMD_CAPTURE_PICTURE);
			showProDialog(false);
			showHintDialog(getResources().getString(R.string.video_not_support));
			mIndex = mMoviePlayer.getPosition();
			mListAdapter.setPlayingPosition(mIndex);
			return true;
		}
	};

	private OnInfoListener mInfoListener = new OnInfoListener() {

		@Override
		public boolean onInfo(EMediaPlayer mp, int what, int extra) {
			if (mp.isHWDecoder() == true) {
				if (what == 1003 || what == 1002) {
					mHandler.removeMessages(CMD_CAPTURE_PICTURE);
					showProDialog(false);
					showHintDialog(getResources().getString(R.string.video_not_support));
					mMoviePlayer.reset();
					return true;
				}
			}
			return false;
		}
	};

	private OnTouchListener mListOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Trace.Debug("list ontouchdown");
				mHandler.removeMessages(CMD_HIDE_LIST);
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				Trace.Debug("list ontouchup");
				showPopListMillis(PAUSE_TIME_BEFORE_HIDE);
			}
			return false;
		}
	};

	private OnHoverListener mHotspotOnHoverListener = new OnHoverListener() {

		@Override
		public boolean onHover(View v, MotionEvent event) {
			switch (v.getId()) {
			case R.id.pointer_hotspot:
				if (event.getActionMasked() == MotionEvent.ACTION_HOVER_ENTER) {
					Trace.Debug("指针进入热区");
					showController();
					return true;
				}
				break;
			case R.id.not_pointer_hotspot:
				if (event.getActionMasked() == MotionEvent.ACTION_HOVER_ENTER) {
					Trace.Debug("指针进入非热区");
					hideController();
					return true;
				}
				break;
			default:

			}
			return false;
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_MENU: {
				if (event.getRepeatCount() == 0) {
					toggleController();
					return true;
				}
			}
				break;
			case KeyEvent.KEYCODE_ENTER: {
				if (event.getRepeatCount() == 0) {
					togglePlay();
					showFloatControllerMillis(PAUSE_TIME_BEFORE_HIDE);
					return true;
				}
			}
				break;
			case KeyEvent.KEYCODE_BACK: {
				if (event.getRepeatCount() == 0) {
					mMoviePlayer.pause();// 退出前暂停
					super.dispatchKeyEvent(event);
				}
			}
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				showPopListMillis(PAUSE_TIME_BEFORE_HIDE);
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				showPopListMillis(PAUSE_TIME_BEFORE_HIDE);
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				mMoviePlayer.fb();
				showFloatControllerMillis(PAUSE_TIME_BEFORE_HIDE);
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				mMoviePlayer.ff();
				showFloatControllerMillis(PAUSE_TIME_BEFORE_HIDE);
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			Trace.Debug("activity touch down");
			togglePlay();
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 监听USB状态
	 */
	class UsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
				Trace.Debug("#### ACTION_MEDIA_EJECT");
				Trace.Info("usb拔出");
				GlobalData gd = (GlobalData) getApplicationContext();

				String path = mMoviePlayer.getDataSourceUri().getPath();
				String rootPath = Utils.getRootPath(path);
				Trace.Debug("####path" + path);
				Trace.Debug("####rootPath" + rootPath);
				String unmountPath = intent.getData().getPath();
				Trace.Debug("#### intent.getData().getPath()=" + unmountPath);

				if (/* isEject */path.contains(unmountPath)) {
					// 当前播放盘被拔掉
					Trace.Info("#### 正在播放的文件不存在");
					mMoviePlayer.stop(true);

					hideController();
					hidePopList();
					hidePopSetting();
					if (mHintdialog != null) {
						mHintdialog.dismiss();
					}
					mLoadingDialog.dismiss();

					MoviePlayerActivity.this.finish();
				}
			}
		}
	}

	private void initUsbReceiver() {
		// 接收USB广播消息
		mUsbReceiver = new UsbReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addDataScheme("file");
		registerReceiver(mUsbReceiver, filter);
	}

	private void unregisterUsbReceiver() {
		if (mUsbReceiver != null) {
			unregisterReceiver(mUsbReceiver);
		}
	}

	/************ 3D播放 *************/
	private KK3DManager m3DManager;
	private KKPictureManager mPictureManager;
	private BroadCast3D mBroadCast3D;
	private boolean bTVHotKeyVersion3 = false;

	private boolean isTVHotKeyVersion3(String pkgName, Context context) {
		boolean bVersion3 = false;
		Trace.Debug("#### isTVHotKeyVersion3()");
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = getPackageManager().getPackageInfo(pkgName, 0);

			String packageName = pkgInfo.packageName; // 得到包名
			String version = pkgInfo.versionName; // 得到版本信息
			String pkgInfoStr = String.format("PackageName:%s, Vesion: %s", packageName, version);
			Trace.Debug("#### " + String.format("PkgInfo: %s", pkgInfoStr));

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pkgInfo != null && pkgInfo.versionName.startsWith("3")) {
			bVersion3 = true;
			Trace.Debug("#### startWith 3");
		}

		return bVersion3;
	}

	/**
	 * 接受3D广播
	 */
	class BroadCast3D extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Trace.Debug("====================== receive the broadcast====" + intent.getAction());
			if (intent.getAction().equals("com.konka.tv.hotkey.MM_3DENABLE")) {

				// 拉伸全屏
				// setFullScreen();

				// miCurScale = SCREEN_FULL_RESIZE;
				// Setting.saveVideoScaleMode(VideoPlayerActivity.this,
				// miCurScale);
				// setVideoScale(miCurScale);

			}
			else if (intent.getAction().equals("com.konka.tv.hotkey.MM_3DDISABLE")) {
				// 退出3D，不用恢复屏显模式
				// setOldScreen();
			}
		}
	}

	private void init3DReceiver() {
		// 接收3D广播的消息。
		IntentFilter filter3D = new IntentFilter();
		mBroadCast3D = new BroadCast3D();
		filter3D.addAction("com.konka.tv.hotkey.MM_3DENABLE");
		filter3D.addAction("com.konka.tv.hotkey.MM_3DDISABLE");
		registerReceiver(mBroadCast3D, filter3D);
	}

	private void unregister3DReceiver() {
		if (mBroadCast3D != null) {
			unregisterReceiver(mBroadCast3D);
		}
	}

	private void set3DOff() {
		if ((KK3DManager.getInstance(getApplicationContext()).getDisplayformat()) != EN_KK_3D_DISPLAY_FORMAT.NONE) {
			KK3DManager.getInstance(getApplicationContext()).setDisplayFormat(EN_KK_3D_DISPLAY_FORMAT.NONE);
		}

	}

	private void check3DScreen() {
		EN_KK_3D_DISPLAY_FORMAT eS3dMode = m3DManager.getDisplayformat();
		if (eS3dMode != EN_KK_3D_DISPLAY_FORMAT.NONE) {
			Trace.Debug("setOnKeyListener 3d 模式 !!!!!but not set\n");
		}
	}

	/**
	 * 将视频进行3D处理（用户选择3D模式后）
	 */
	void autoDect3D() {
		if (m3DManager == null || mPictureManager == null || mMoviePlayer == null
				|| mMoviePlayer.getKKMediaPlayer() == null)
			return;
		if (m3DManager.getSelfAdaptiveDectectMode() != EN_KK_3D_SELF_ADAPTIVE_DETECT_MODE.OFF
				&& !m3DManager.isCurrentMovieFramePacking() && !mPictureManager.is4K2KModeEnable()
		/* && !mVideoView.mKKMediaPlayer.isMVCSource() */) {
			new Thread(new Runnable() {// xuran add 3d auto
						@Override
						public void run() {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							int i = 0;
							while (i < 5) {
								m3DManager.setSelfAdaptiveDetectMode(EN_KK_3D_SELF_ADAPTIVE_DETECT_MODE.RIGHT_NOW);
								if (m3DManager.getDisplayformat() != EN_KK_3D_DISPLAY_FORMAT.NONE)
									break;
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								i++;
								Trace.Debug("xuran ..........selfdetect time:" + i);
							}
						}
					}).start();
		}
	}

	void autoDect3DVersion3() {
		if (m3DManager == null || mPictureManager == null || mMoviePlayer == null
				|| mMoviePlayer.getKKMediaPlayer() == null)
			return;

		new Thread(new Runnable() {// xuran add 3d auto
					@Override
					public void run() {
						int i = 0;
						while (i < 5) {
							m3DManager.setSelfAdaptiveDetectMode(EN_KK_3D_SELF_ADAPTIVE_DETECT_MODE.RIGHT_NOW);
							if (0 == i) {
								Intent intent_start = new Intent();
								intent_start.setAction("konka.action.3D_SELF_ADAPTIVE_DETECT_START");
								sendBroadcast(intent_start);
							}
							if (m3DManager.getDisplayformat() != EN_KK_3D_DISPLAY_FORMAT.NONE) {
								Intent intent_finish = new Intent();
								intent_finish.setAction("konka.action.3D_SELF_ADAPTIVE_DETECT_FINISH");
								sendBroadcast(intent_finish);
								break;
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							i++;
							Trace.Debug("xuran ..........selfdetect time:" + i);
						}
						if (5 == i) {
							Intent intent_finish = new Intent();
							intent_finish.setAction("konka.action.3D_SELF_ADAPTIVE_DETECT_FINISH");
							sendBroadcast(intent_finish);
						}
					}
				}).start();
	}

}
