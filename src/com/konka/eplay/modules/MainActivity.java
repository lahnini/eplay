package com.konka.eplay.modules;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.zip.ZipException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.model.LocalProvider;
import com.konka.eplay.modules.files.FilesActivity;
import com.konka.eplay.modules.movie.MovieActivity;
import com.konka.eplay.modules.music.AllSongListFragment;
import com.konka.eplay.modules.music.MusicActivity;
import com.konka.eplay.modules.music.MusicDBManager;
import com.konka.eplay.modules.music.MusicPlayerActivity;
import com.konka.eplay.modules.music.MusicPlayerService;
import com.konka.eplay.modules.music.MusicUtils;
import com.konka.eplay.modules.photo.PhotoActivity;
import com.konka.eplay.modules.photo.QuickToast;

public class MainActivity extends Activity implements OnClickListener {
	private FrameLayout moreLayout;
	private FrameLayout mPhotoLayout;
	private FrameLayout mMoiveLayout;
	private FrameLayout mMusicLayout;
    private FrameLayout mFilesLayout;

	private IntegratedRelativeLayout mIntegratedRelativeLayout;

	private Toast mToast = null;
	private Handler mHandler = new MyHandler(this);
	private UsbReceiver mUsbReceiver;
	private IntentFilter filter;

	// add by xuyunyu
	private FrameLayout mMusicTipLayout;
	private ImageView mMusicWave;
	private ImageView mAppIcon;
	private AlwaysMarqueeTextView mSongName;
	private MusicPlayerService mPlayerService;
	private SettingPopupWindow mSettingPopupWindow;

	private ServiceConnection mServiceConnection = null;
	private HomeReciever mHomeReceiver;

	private RelativeLayout mLayot_top;

	/** 监听返回键是否间隔2秒 */
	private long exitTime = 0;
	private static final int USB_MSG = 101;
	private static final int DELAY = 1000;
	// 保存排序设置的key
	public static final String SORTATION_SETTING_KEY = "sort_setting";
	PopupWindow mPopupWindow;

	public static final String SYSTEM_REASON = "reason";
	public static final String SYSTEM_HOME_KEY = "homekey";// home key

	private static boolean mIsAnimated = true;//true

	static class MyHandler extends Handler {
		WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// MainActivity activity = mActivity.get();
			switch (msg.what) {
			case USB_MSG:
				Intent intent = (Intent) msg.obj;

				Trace.Info("###usb intent" + intent.getAction());
				if(Configuration.isHome){
					Trace.Debug("###UsbChangedCleanData");
				MovieActivity.getFileList().clear();
				MovieActivity.getFileList().clear();
				MusicActivity.getFileList().clear();
				MusicActivity.getAllMusicFile().clear();
				MusicActivity.clearAllData();
				//AllSongListFragment.getLikeList().clear();
				PhotoActivity.getFileList().clear();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	}

	public void onEventMainThread(IEvent event) {
		if (event instanceof EventMusicStateChange) {
			switch (((EventMusicStateChange) event).musicStateType) {
			case Constant.MUSIC_SERVICE_FLAG_CHANGE_SONG:
				updateMuisicInfo();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PLAY:
				playMuisicAnimation();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PAUSE:
				stopMuisicAnimation();
			case Constant.MUSIC_SERVICE_FLAG_SONG_STOP:
				stopMuisicAnimation();
				mMusicTipLayout.setVisibility(View.INVISIBLE);
				break;
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		moreLayout = (FrameLayout) findViewById(R.id.morelayout);
		mMoiveLayout = (FrameLayout) findViewById(R.id.movielayout);
		mMusicLayout = (FrameLayout) findViewById(R.id.musiclayout);
		mPhotoLayout = (FrameLayout) findViewById(R.id.photolayout);
        mFilesLayout = (FrameLayout) findViewById(R.id.fileslayout);

		//add by mcsheng
		mIntegratedRelativeLayout = (IntegratedRelativeLayout) findViewById(R.id.main_click_layout);
		mIntegratedRelativeLayout.setSelector(R.drawable.picture_brower_item_focus_small);
		mIntegratedRelativeLayout.setScale(1.2f, 1.2f);
		mIntegratedRelativeLayout.openClickEffect(true);
		mIntegratedRelativeLayout.openEnlargeEffect(true);
		mIntegratedRelativeLayout.openTransEffect(true);

		// add by xuyunyu

		mMusicTipLayout = (FrameLayout) findViewById(R.id.music_tip_layout);
		mLayot_top = (RelativeLayout) findViewById(R.id.main_top_layout);
		mMusicWave = (ImageView) findViewById(R.id.music_wave);
		mSongName = (AlwaysMarqueeTextView) findViewById(R.id.music_songname);
		mAppIcon = (ImageView) findViewById(R.id.appicon);
		mMusicWave.setBackgroundResource(R.drawable.music_wave_anim);
		mMusicTipLayout.setOnClickListener(this);

		moreLayout.setOnClickListener(this);
		mMoiveLayout.setOnClickListener(this);
		mMusicLayout.setOnClickListener(this);
		mPhotoLayout.setOnClickListener(this);
        mFilesLayout.setOnClickListener(this);

		// 初始化排序方式
		String sort = Configuration.getString(null, MainActivity.SORTATION_SETTING_KEY);
		if (null == sort) {
			Configuration.sortType = SortType.ST_BY_TIME;
		} else if (sort.equals(SortType.ST_BY_NAME.toString())) {
			Configuration.sortType = SortType.ST_BY_NAME;
		} else {
			Configuration.sortType = SortType.ST_BY_TIME;
		}

		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addDataScheme("file");
		mUsbReceiver = new UsbReceiver();
		registerReceiver(mUsbReceiver, filter);

//		if (Configuration.curMediaType == MultimediaType.MMT_MOVIE) {
//			mMoiveLayout.setFocusable(true);
//			mMoiveLayout.requestFocus();
//		} else if (Configuration.curMediaType == MultimediaType.MMT_MUSIC) {
//			mMusicLayout.setFocusable(true);
//			mMusicLayout.requestFocus();
//		} else if (Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
//			mPhotoLayout.setFocusable(true);
//			mPhotoLayout.requestFocus();
//		}

		if (null == mPlayerService) {
			bindService();
		}
		registerReciever();

		initSingerThumbnail();

		//mIntegratedRelativeLayout.requestFocus();
	}

	/*
	 * @Description:初始化歌手头像，开线程将头像和歌手数据库样本复制到本地
	 */
	private void initSingerThumbnail() {

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					String path = MainActivity.this.getFilesDir().getAbsolutePath() + "/"+ "portraitThumbnail";
				    String singer_path = MainActivity.this.getFilesDir().getAbsolutePath() + "/"+ MusicDBManager.DB_NAME;
				    //复制歌手头像
					if (!new File(path).exists()) {
						MusicUtils.copySingerPhotoToCache(MainActivity.this);
						try {
							MusicUtils.unZipFile(MainActivity.this.getFilesDir().getAbsolutePath() + "/"+"portraitThumbnail.zip", MainActivity.this.getFilesDir().getAbsolutePath());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (ZipException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else {
						Trace.Info("singer photo exists");
					}

					//复制歌手姓名数据库
					if (!new File(singer_path).exists()) {
						new MusicDBManager(MainActivity.this).copyDatabase();
					}else {
						Trace.Info("singer db exists");
					}

				}
			});
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
	}

	/*
	 * @Description: 绑定音乐播放服务
	 */
	private void bindService() {

		mServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Trace.Debug("MusicPlayerActivity---> 已断开连接service");
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
		mHomeReceiver = new HomeReciever();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Constant.QUICK_STANDBY_ACTION);
		intentFilter.addAction(Constant.EPG_ACTION_1);
		intentFilter.addAction(Constant.EPG_ACTION_2);
		intentFilter.addAction(Constant.START_APP);
		registerReceiver(mHomeReceiver, intentFilter);

	}

	class HomeReciever extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

				String flag = intent.getStringExtra(MainActivity.SYSTEM_REASON);
				if (flag != null && flag.equals(MainActivity.SYSTEM_HOME_KEY)) {
					Trace.Debug("###press home key");
					if (null != mPlayerService) {
						mPlayerService.pause();
					}
				}

			}else if (action.equals(Intent.ACTION_SCREEN_OFF)
					|| action.equalsIgnoreCase(Constant.QUICK_STANDBY_ACTION) || action.equals(Constant.EPG_ACTION_1)
					|| action.equals(Constant.EPG_ACTION_2) || action.equalsIgnoreCase(Constant.START_APP)) {
				Trace.Info("###lhq 收到待机广播 or EPG 预约!");
				if (null!=mPlayerService) {
					mPlayerService.stop();
					mPlayerService.clearMusicFiles();
				}

			}

		}

	}

	@Override
	protected void onStart() {
		Trace.Info("mian onstart");
		// 注册接收event事件
		EventDispatchCenter.getInstance().register(this);
		super.onStart();
		if (null != mPlayerService && !mPlayerService.getPlayState()) {
			mMusicTipLayout.setVisibility(View.INVISIBLE);
			return;
		}
		playMuisicAnimation();

	}

	@Override
	protected void onStop() {
		super.onStop();
		stopMuisicAnimation();
		// 不再接收event事件
		EventDispatchCenter.getInstance().unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mIsAnimated) {
			mAppIcon.setFocusable(true);
			mAppIcon.requestFocus();

			//add by mcsheng
			if (mIntegratedRelativeLayout.getSelector() != null) {
				mIntegratedRelativeLayout.getSelector().setAlpha(0);
			}
			startLayoutAnimation();
			mIsAnimated = false;
		}else {
			mAppIcon.setFocusable(false);
		}
//		if (Configuration.curMediaType == MultimediaType.MMT_MOVIE) {
//			mMoiveLayout.setFocusable(true);
//			mMoiveLayout.requestFocus();
//		} else if (Configuration.curMediaType == MultimediaType.MMT_MUSIC) {
//			mMusicLayout.setFocusable(true);
//			mMusicLayout.requestFocus();
//		} else if (Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
//			mPhotoLayout.setFocusable(true);
//			mPhotoLayout.requestFocus();
//		}
		Configuration.ISQUICKENTER = false;
	}

	/**
	 * @Title: startLayoutAnimation
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return void
	 * @throws
	 */
	private void startLayoutAnimation() {
		AnimationSet animationSet = new AnimationSet(false);
		Animation alphaAnimation = new AlphaAnimation(0f, 1.0f);
		Interpolator i = new LinearInterpolator();
		alphaAnimation.setInterpolator(i);
		alphaAnimation.setDuration(900);
		Animation transAnimation = new TranslateAnimation(0, 0, -30, 0);
		transAnimation.setDuration(1000);
		transAnimation.setInterpolator(i);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(transAnimation);

		PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 0.8f, 1f);
		PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f);
		PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f);
		ObjectAnimator a1 = ObjectAnimator.ofPropertyValuesHolder(mPhotoLayout, pvhX, pvhY, pvhZ);
		ObjectAnimator a2 = ObjectAnimator.ofPropertyValuesHolder(mMoiveLayout, pvhX, pvhY, pvhZ);
		ObjectAnimator a3 = ObjectAnimator.ofPropertyValuesHolder(mMusicLayout, pvhX, pvhY, pvhZ);
		// a2.setStartDelay(150);
		// a3.setStartDelay(300);

		// a1.setInterpolator(new AccelerateInterpolator());
		// a2.setInterpolator(new AccelerateInterpolator());
		// a3.setInterpolator(new AccelerateInterpolator());
		a1.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				super.onAnimationStart(animation);
				mPhotoLayout.setVisibility(View.VISIBLE);
			}
		});

		a2.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				super.onAnimationStart(animation);

				mMoiveLayout.setVisibility(View.VISIBLE);
			}
		});

		a3.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				super.onAnimationStart(animation);
				mMusicLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				mPhotoLayout.requestFocus();
				mAppIcon.setFocusable(false);
				//add by mcsheng
				mIntegratedRelativeLayout.requestFocus();
			}
		});

		mLayot_top.startAnimation(animationSet);

		a1.setStartDelay(500);
		a2.setStartDelay(500);
		a3.setStartDelay(500);
		a1.setDuration(500).start();
		a2.setDuration(500).start();
		a3.setDuration(500).start();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.morelayout:
			//mBorderView.setBorderStyle(R.drawable.button_press_focus);
			moreLayout.setBackgroundResource(R.drawable.focus_clicked_small);

			if (null == mSettingPopupWindow) {
				mSettingPopupWindow = new SettingPopupWindow(MainActivity.this);

				mSettingPopupWindow.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss() {
						moreLayout.setBackgroundResource(R.drawable.music_imagebutton_selector);
					}
				});
			}
			mSettingPopupWindow.showAsDropDown(v, -v.getWidth() / 2 - Utils.dip2px(this, 120), -10);

			break;
		case R.id.photolayout:
			Configuration.curMediaType = MultimediaType.MMT_PHOTO;
			Intent intentPhoto = new Intent(this, PhotoActivity.class);
			startActivity(intentPhoto);
			break;
		case R.id.movielayout:

			Configuration.curMediaType = MultimediaType.MMT_MOVIE;
			Intent intentMovie = new Intent(this, MovieActivity.class);
			startActivity(intentMovie);
			break;
		case R.id.musiclayout:
			Configuration.curMediaType = MultimediaType.MMT_MUSIC;
			Intent intentMusic = new Intent(this, MusicActivity.class);
			startActivity(intentMusic);
			break;
        case R.id.fileslayout:
 //          Configuration.curMediaType = MultimediaType.MMT_MUSIC;
            Intent intentFiles = new Intent(this, FilesActivity.class);
            startActivity(intentFiles);
            break;
		case R.id.clean:
			break;
		case R.id.version:
			break;
		case R.id.music_tip_layout:
			Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
			i.putExtra("isBackGround", true);
			startActivity(i);
			break;
		default:
			break;
		}
	}

	/**
	 * 监听USB状态
	 */
	class UsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				Trace.Debug("###USbMounted");
				QuickToast.cancelToast();
				QuickToast.showToast(MainActivity.this, getResources().getString(R.string.usb_mount));
			} else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
				Trace.Debug("###USbreject");
				QuickToast.cancelToast();
				QuickToast.showToast(MainActivity.this, getResources().getString(R.string.usb_remove));
				Trace.Info("####stopLocalMusic(dst)");

				 ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			     List<RunningTaskInfo> runningTasks = am.getRunningTasks(1);
			     RunningTaskInfo rti = runningTasks.get(0);
			     ComponentName component = rti.topActivity;
			     Trace.Info("stop"+component.getClassName());
			     if (!component.getClassName().equals("com.konka.eplay.modules.music.MusicPlayerActivity")) {
			    	 stopLocalMusic();
				}
			}
			mHandler.removeMessages(USB_MSG);
			Message msg = Message.obtain();
			msg.what = USB_MSG;
			msg.obj = intent;
			mHandler.sendMessageDelayed(msg, DELAY);
		}
	}

	private void stopLocalMusic() {
		if (mPlayerService != null && mPlayerService.getMusciPath() !=null ) {
			String path = mPlayerService.getMusciPath();
			String rootPath = Utils.getRootPath(path);
			Trace.Debug("####checkUsbEject");
			boolean isEject = Utils.checkUsbEject(rootPath, getApplicationContext());
			if (isEject) {
				// 当前播放盘被拔掉
				Trace.Info("####stop 正在播放的文件不存在");
				mPlayerService.stop();
				mPlayerService.clearMusicFiles();;
				stopMuisicAnimation();
				mMusicTipLayout.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void updateMuisicInfo() {
		if (null != mPlayerService) {
			mSongName.setText(mPlayerService.getMusicName());
		}
	}

	private void playMuisicAnimation() {

		Trace.Info("play animation");
		if (null == mPlayerService) {
			mMusicTipLayout.setVisibility(View.INVISIBLE);
		} else if (null != mPlayerService.getmMediaPlayer()) {
			if (mPlayerService.getmMediaPlayer().isPlaying()) {
				mMusicTipLayout.setVisibility(View.VISIBLE);
				AnimationDrawable anim = (AnimationDrawable) mMusicWave.getBackground();
				anim.start();
				mSongName.setText(mPlayerService.getMusicName());
			} else {
				mMusicTipLayout.setVisibility(View.VISIBLE);
				mSongName.setText(mPlayerService.getMusicName());
			}

		}

	}

	private void stopMuisicAnimation() {

		mMusicTipLayout.setVisibility(View.VISIBLE);
		if (null != mMusicWave && mMusicWave.getBackground() != null
				&& mMusicWave.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mMusicWave.getBackground();
			if (anim != null && anim.isRunning()) { // 如果正在运行,就停止
				anim.stop();
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUsbReceiver);
		Trace.Debug("##LikeListSize=" + MusicActivity.getLikeList());
		// AllSongListFragment.getLikeList().clear();
		Trace.Debug("##LikeListSize=" + MusicActivity.getLikeList());

		cachedSpecificMediaAsync();
		// 解绑 music service
		if (mServiceConnection != null) {
			unbindService(mServiceConnection);
		}
		if (null != mHomeReceiver) {
			unregisterReceiver(mHomeReceiver);
		}
		mIsAnimated = true;

		if (mPlayerService != null) {
			mPlayerService.stop();
			mPlayerService.clearMusicFiles();
			stopService(new Intent(MusicPlayerActivity.ACTION_MUSIC_SERVICE_INTENT));
			mPlayerService = null;
		}

	}

	/**
	 * 缓存特定文件到数据库
	 */
	private void cachedSpecificMediaAsync() {
		Trace.Debug("clearData");
		List<CommonFileInfo> albumList = PhotoActivity.getFileList();
		if(albumList!=null)
			albumList.clear();
			albumList=null;
			List<CommonFileInfo> albumRedList = PhotoActivity.getLikePhotoList(PhotoActivity.RED);
			if(albumRedList!=null)
				albumRedList.clear();
			albumRedList=null;
			List<CommonFileInfo> albumBlueList = PhotoActivity.getLikePhotoList(PhotoActivity.BLUE);
			if(albumBlueList!=null)
				albumBlueList.clear();
			albumBlueList=null;
			List<CommonFileInfo> albumYellowList = PhotoActivity.getLikePhotoList(PhotoActivity.YELLOW);
			if(albumYellowList!=null)
				albumYellowList.clear();
			albumYellowList=null;
		List<CommonFileInfo> musicAllList = MusicActivity.getAllMusicFile();
		if(musicAllList!=null)
			musicAllList.clear();
			musicAllList=null;
		List<CommonFileInfo> musicFolderList = MusicActivity.getFileList();
		if (musicFolderList != null)
			musicFolderList.clear();
		musicFolderList = null;
		List<CommonFileInfo> musicLikeList = MusicActivity.getLikeList();
		if (musicLikeList != null)
			musicLikeList.clear();
		musicLikeList = null;
		List<CommonFileInfo> musicList = MusicActivity.getFileList();
		if(musicList!=null)
			musicList.clear();
		musicList=null;

		List<CommonFileInfo> videoList = MovieActivity.getFileList();
		if (videoList!=null)
			videoList.clear();
				videoList=null;
//		new Thread() {
//			public void run() {
//				// 相册
//
//			}
//		}.start();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			Trace.Debug("###BACK##");
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Trace.Debug("###BACK>2000##");
				// Toast.makeText(getBaseContext(), ,
				// Toast.LENGTH_SHORT).show();
				VersionToast.showToast(getBaseContext(), "再按一次退出程序");
				exitTime = System.currentTimeMillis();
			} else {
				Trace.Debug("###BACK<2000##");
				finish();
				// System.exit(0);
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}