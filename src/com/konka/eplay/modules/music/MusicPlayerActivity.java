package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.MainActivity;
import com.konka.eplay.modules.music.LyricViewFragment.OnLyricViewListener;
import com.konka.eplay.modules.music.MusicControlPanelFragment.OnControlPanelListener;
import com.konka.eplay.modules.music.SonglistPopupWindow.OnChangeSongListenner;
import com.konka.eplay.modules.photo.QuickToast;

/**
 * @ClassName: MusicPanelActivity
 * @Description: 音乐播放器的主界面，控制fragment的跳转和播放逻辑
 * @author xuyunyu
 * @date 2015年3月24日 下午2:10:57
 * @version V1.0
 *
 */
public class MusicPlayerActivity extends Activity implements OnLyricViewListener,
		FragmentManager.OnBackStackChangedListener, OnControlPanelListener, OnChangeSongListenner {

	private static final int RUNNABLE_DELAY_TIME = 1000;
	// 启动MusicPlayerService的Action
	public static final String ACTION_MUSIC_SERVICE_INTENT = "com.konka.EPlay.MusicPlayerService";
	public static final String ACTION_MUSIC_BROADCAST = "com.konka.EPlay.broadcast.MusicPlayerService";

	public static final String SONGLIST_FROM_MUSICFILELISTACTIVITY = "songlist_from_musicfilelist_activity";
	public static final String SONGLIST_FROM_MUSICSECONDLISTACTIVITY = "songlist_from_musicsecondlist_activity";
	public static final String SONGLIST_FROM_MUSICLIKELISTACTIVITY = "songlist_from_musicLlike_tab";
	public static final String SONGLIST_FROM_WHERE = "songlist_from_where";
	private FragmentManager mFragmentManager;
	private FrameLayout mControlLayout;
	private FrameLayout mLyricLayout;
	private SonglistPopupWindow mSonglistPopupWindow;
	private SwitchAndJumpPopupWindow mSwitchPopupWindow;
	private ServiceConnection mServiceConnection = null;
	private MusicPlayerService mPlayerService;

	private MusicHomeReceiver mHomeReceiver;
	private MusicPlayerUsbReceiver mUsbReceiver;
	private boolean mIsBackground = false;

	private MusicControlPanelFragment mMusicControlPanelFragment;
	private LyricViewFragment mLyricViewFragment;

	// 标记是否在显示歌词搜索界面
	private boolean mShowingBack;
	private List<CommonFileInfo> mMusicFiles;
	private int mListPosition = 0;

	private Handler mHandler = new Handler();
	private Runnable runnable = new Runnable() {
		public void run() {
			mSonglistPopupWindow.setFocusImageVisible(View.VISIBLE);
		}

	};

	public void onEventMainThread(IEvent event) {
		if (event instanceof EventMusicStateChange) {
			if (((EventMusicStateChange) event).musicStateType == Constant.MUSIC_SERVICE_FLAG_CHANGE_SONG) {
				mMusicControlPanelFragment.refreshUI();
				mLyricViewFragment.refreshUI();

				if (mSonglistPopupWindow != null && mSonglistPopupWindow.isShowing()) {
					mSonglistPopupWindow.refreshList();
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!parseIntent(getIntent())) {
			return;
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.music_player);

		mFragmentManager = getFragmentManager();
		if (savedInstanceState == null) {
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			// transaction.setCustomAnimations(android.R.animator.fade_in,
			// android.R.animator.fade_out);
			mMusicControlPanelFragment = new MusicControlPanelFragment();
			mLyricViewFragment = new LyricViewFragment();
			transaction.add(R.id.music_control, mMusicControlPanelFragment, "MusicControlPanelFragment");
			transaction.add(R.id.music_lyric, mLyricViewFragment, "LyricViewFragment");
			transaction.commit();
		} else {
			mShowingBack = (mFragmentManager.getBackStackEntryCount() > 0);
		}
		//添加入栈监听
		mFragmentManager.addOnBackStackChangedListener(this);

		initViews();
		// startService(new Intent(ACTION_MUSIC_SERVICE_INTENT));
		bindService();
		registerReciever();

		EventDispatchCenter.getInstance().register(this);

	}

	/*
	 * @Description:注册广播
	 */
	private void registerReciever() {
		//监听首页luancher键
		mHomeReceiver = new MusicHomeReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Constant.QUICK_STANDBY_ACTION);
		intentFilter.addAction(Constant.EPG_ACTION_1);
		intentFilter.addAction(Constant.EPG_ACTION_2);
		intentFilter.addAction(Constant.START_APP);
		registerReceiver(mHomeReceiver, intentFilter);

		//监听外部存储拔插
		mUsbReceiver = new MusicPlayerUsbReceiver();
		IntentFilter usbFilter = new IntentFilter();
		usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		usbFilter.addDataScheme("file");
		registerReceiver(mUsbReceiver, usbFilter);

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

				mMusicControlPanelFragment.setmMuiscPlayerService(mPlayerService);

				mLyricViewFragment.setmMuiscPlayerService(mPlayerService);

				mLyricViewFragment.setContronlPanel(mMusicControlPanelFragment);

				if (!mIsBackground && mMusicFiles != null && mMusicFiles.size() > 0 && mListPosition >= 0
						&& mListPosition < mMusicFiles.size()) {
					mPlayerService.playSongList(mMusicFiles, mListPosition);
				} else {
					mMusicControlPanelFragment.refreshUI();
					mLyricViewFragment.refreshUI();
				}

			}
		};

		Intent intent = new Intent();
		intent.setAction(ACTION_MUSIC_SERVICE_INTENT);
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	/*
	 * @Description: 初始化view
	 */
	private void initViews() {
		mControlLayout = (FrameLayout) findViewById(R.id.music_control);
		mLyricLayout = (FrameLayout) findViewById(R.id.music_lyric);
	}

	/*
	 * @Description: 解析传递过来的intent，获取相关的数据，如果是无效的数据，则直接返回
	 */
	private boolean parseIntent(Intent intent) {

		if (intent == null) {
			finish();
			return false;
		}

		String action = intent.getAction();

		if (action.equals(Constant.PLAY_MUSIC_ACTION)) {
			mIsBackground = intent.getBooleanExtra("isBackGround", false);
			if (mIsBackground) {
				return true;
			}

			if (intent.hasExtra("MUSICPATH")) {
				// 有这个extra其实是代表从MusicFragment传入
				if (!mIsBackground) {

					if (intent.hasExtra(SONGLIST_FROM_WHERE)) {
						String where = intent.getStringExtra(SONGLIST_FROM_WHERE);
						mMusicFiles = new ArrayList<CommonFileInfo>();
						List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
						if (where.equals(SONGLIST_FROM_MUSICFILELISTACTIVITY)) {
							list = MusicActivity.getAllMusicList();
							mMusicFiles.addAll(list);
						} else if (where.equals(SONGLIST_FROM_MUSICSECONDLISTACTIVITY)) {
							list = MusicSecondListActivity.getAllSecondMusicList();
							mMusicFiles.addAll(list);
						}
					}

				}
			}
			if (intent.hasExtra(Constant.PLAY_INDEX)) {
				int index = intent.getIntExtra(Constant.PLAY_INDEX, 0);
				mListPosition = index;
				Trace.Info("index value -->" + index);
			} else {
				mListPosition = 0;
			}
			return true;
		}
		return false;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean handled = false;

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Fragment fragment = mFragmentManager.findFragmentByTag("SearchLyricFragment");
			if (fragment != null && fragment.isVisible()) {
				mFragmentManager.popBackStack();
				handled = true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {

			showSwitchAndJumpWindow();
			handled = true;
		}
		return handled || super.onKeyDown(keyCode, event);
	}

	/*
	 * @Description: 显示跳转到图片和视频的弹窗
	 */
	private void showSwitchAndJumpWindow() {

		if (mSwitchPopupWindow == null) {
			mSwitchPopupWindow = new SwitchAndJumpPopupWindow(this);
			mSwitchPopupWindow.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					mLyricViewFragment.setFocusDrawable(R.drawable.music_imagebutton_selector);
					mMusicControlPanelFragment.setFocusDrawable(R.drawable.music_imagebutton_selector, true);
				}
			});
		}
		if (!mSwitchPopupWindow.isShowing()) {
			mSwitchPopupWindow.showAtLocation(findViewById(R.id.music_player_root), Gravity.NO_GRAVITY,
					Utils.getScreenW(this) - 300, (Utils.getScreenH(this) - mSwitchPopupWindow.getHeight()) / 2);
			if (!mLyricViewFragment.setFocusDrawable(R.drawable.listview_selector)) {
				mMusicControlPanelFragment.setFocusDrawable(R.drawable.listview_selector, false);
			}

		}

	}

	@Override
	public void onCardFlip() {

		if (mShowingBack) {
			mFragmentManager.popBackStack();
			return;
		}
		// 翻到背面.
		mShowingBack = true;

		SearchLyricFragment mSearchLyricFragment = new SearchLyricFragment();

		mLyricViewFragment.setSearchFragment(mSearchLyricFragment);
		mSearchLyricFragment.setSearchTitle(mPlayerService.getmMusicList()
				.get(mPlayerService.getmCurrentListPosition()));
		mFragmentManager
				.beginTransaction()
				.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
						R.animator.card_flip_left_in, R.animator.card_flip_left_out)
				.replace(R.id.music_lyric, mSearchLyricFragment, "SearchLyricFragment").addToBackStack(null).commit();

	}

	@Override
	public void onBackStackChanged() {
		mShowingBack = (mFragmentManager.getBackStackEntryCount() > 0);
	}

	/*
	 * @Description: 显示歌曲列表的弹窗
	 */
	@Override
	public void onShowSonglistWindow() {

		// if (mSonglistPopupWindow == null) {
		// 初始化window，并监听dismiss动作
		// mSonglistPopupWindow = null;
		mSonglistPopupWindow = new SonglistPopupWindow(this, mPlayerService);
		mSonglistPopupWindow.setChangeSongListenner(this);

		mSonglistPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {

				mHandler.removeCallbacks(runnable);
				mControlLayout.clearAnimation();
				mLyricLayout.clearAnimation();
				mSonglistPopupWindow.setFocusImageVisible(View.INVISIBLE);

				findViewById(R.id.music_control_songlist).setBackgroundResource(R.drawable.music_imagebutton_selector);
				mFragmentManager.beginTransaction().remove(mFragmentManager.findFragmentByTag("AlphaFragment"))
						.commit();
				Animation animation = AnimationUtils.loadAnimation(MusicPlayerActivity.this, R.anim.fragment_right);
				animation.setFillAfter(true);

				mControlLayout.startAnimation(animation);
				mLyricLayout.startAnimation(animation);

			}
		});
		// }
		// 判断是否显示popupwindow
		if (!mSonglistPopupWindow.isShowing()) {

			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			transaction.setCustomAnimations(R.animator.alpha_in, R.animator.alpha_out);
			transaction.add(R.id.music_player_root, new AlphaFragment(), "AlphaFragment").commit();
			mControlLayout.clearAnimation();
			mLyricLayout.clearAnimation();
			mSonglistPopupWindow.showAtLocation(findViewById(R.id.music_player_root), Gravity.NO_GRAVITY,
					Utils.getScreenW(this) - Utils.dip2px(this, 343.33f) + Utils.dip2px(this, 8), 0);//12像素是右边间隔

			Animation animation = AnimationUtils.loadAnimation(this, R.anim.fragment_left);
			animation.setFillAfter(true);

			mControlLayout.startAnimation(animation);
			mLyricLayout.startAnimation(animation);

			mHandler.postDelayed(runnable, RUNNABLE_DELAY_TIME); // 安排一个Runnable对象到主线程队列中

		}

	}

	@Override
	protected void onDestroy() {

		// 解绑 music service
		if (mServiceConnection != null) {
			unbindService(mServiceConnection);
		}

		if (null != mHomeReceiver) {
			unregisterReceiver(mHomeReceiver);
		}
		if (null != mUsbReceiver) {
			unregisterReceiver(mUsbReceiver);
		}

		EventDispatchCenter.getInstance().unregister(this);

		super.onDestroy();
	}

	@Override
	protected void onResume() {

		if (Configuration.ISQUICKENTER) {
			MusicPlayerActivity.this.finish();
		}
		super.onResume();
	}

	/*
	 * 响应音乐播放列表被点击的动作，比方相应歌曲
	 */
	@Override
	public void onChangeSong(int position) {
		mPlayerService.playSelection(position);
	}

	@Override
	public void onSearchLyric() {
		onCardFlip();
	}

	/**
	* @ClassName: MusicHomeReceiver
	* @Description: 监听home键，用户按主页键回到首页，则停止播放音乐。
	 */
	class MusicHomeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Trace.Debug("MusicReceiver triggered!");
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
			//	mPlayerService.clearMusicFiles();
				mPlayerService.stop();
				MusicPlayerActivity.this.finish();
			}
		}
	}

	/**
	* @ClassName: MusicPlayerUsbReceiver
	* @Description: 监听usb的插拔
	 */
	class MusicPlayerUsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				//do nothing
			} else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
				stopLocalMusic();
			}
		}
	}

	/*
	* @Description: 如果被拔掉的U盘里面包含当前播放的这首歌，则立即退出activity
	 */
	private void stopLocalMusic() {
		Trace.Info("stopLocalMusic triggered");
		if (mPlayerService != null && mPlayerService.getMusciPath() !=null ) {
			String path = mPlayerService.getMusciPath();
			String rootPath = Utils.getRootPath(path);
			Trace.Debug("####checkUsbEject");
			boolean isEject = Utils.checkUsbEject(rootPath, getApplicationContext());
			if (isEject) {
				// 当前播放盘被拔掉，而且播放的歌曲就在此U盘
				QuickToast.showToast(MusicPlayerActivity.this, MusicUtils.getResourceString(this, R.string.music_quit_player));
				if (null!=mPlayerService) {
					mPlayerService.clearMusicFiles();
					mPlayerService.stop();
					MusicPlayerActivity.this.finish();
				}

			}
		}
	}

	//更换播放器背景
	@Override
	public void onChangeBackround(Bitmap bitmap) {
		if (null!=bitmap) {
			((RelativeLayout) findViewById(R.id.music_player_root)).setBackgroundDrawable(new BitmapDrawable(bitmap));
		}else {
			((RelativeLayout) findViewById(R.id.music_player_root)).setBackgroundResource(R.drawable.music_bg);
		}

	}

	@Override
	public void onFocusSearchButton() {
		findViewById(R.id.music_control_search).setBackgroundResource(R.drawable.music_imagebutton_selector);
		findViewById(R.id.music_control_search).requestFocus();

	}

}
