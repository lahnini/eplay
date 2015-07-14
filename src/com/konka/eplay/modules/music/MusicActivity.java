package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.Constant.TAB_TYPE;
import com.konka.eplay.GlobalData;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.event.EventCommResult;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventTimeout;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.event.MusicFilesSortationEvent;
import com.konka.eplay.model.CommonResult;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.model.LocalDiskInfo;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.LoadingDialog;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.VersionToast;
import com.konka.eplay.modules.music.AllSongListFragment.OnAllSongListListener;
import com.konka.eplay.modules.music.SongFolderFragment.OnSongFolderListener;

public class MusicActivity extends Activity implements OnSongFolderListener, OnAllSongListListener {

	public static final int REFRESH_DATA = 2;

	private View mFolderTabSelected;
	private View mAllSongTabSelected;
	private View mTabFocusLeft;
	private View mTabFocusRight;
	private TextView mFolderTab;
	private TextView mAllSongTab;
	private TextView mEmptyText;
	private RelativeLayout mBackButton;
	private MusicListMainOnClickListener mClickListener;

	private int mTabID;
	private int mTabIDForBackButton;
	private SongFolderFragment mSongFolderFragment;
	private AllSongListFragment mAllSongListFragment;
	private FragmentManager mFragmentManager;
	private LoadingDialog mLoadingDialog;

	private GlobalData mApp;
	private Context mContext;
	private List<LocalDiskInfo> mUsbList;
	private static Operation mOperation;

	private static List<CommonFileInfo> sList = null;
	public static List<CommonFileInfo> sAllList = null;
	private static List<CommonFileInfo> sListForMusicPlayer = null;
    private static long startTime=0;
	private long mTagTime;

	// 每个歌手对应的歌曲信息
	public static Map<String, List<CommonFileInfo>> mSingerCollections;
	// 每张专辑对应的歌曲信息
	public static Map<String, List<CommonFileInfo>> mAlbumCollections;
	// 歌手tab的数据
	public static List<CommonFileInfo> mSonglistBySinger = null;
	// 专辑tab的数据
	public static List<CommonFileInfo> mSonglistByAlbum = null;
	// “我喜欢”tab的数据
	public static List<CommonFileInfo> mSonglistByCollect = null;

	private Map<String, Fragment> mFragments;

	private MusicListFocusChangeListener mFocusChangeListener;
	private MusicListOnkeyListener mOnkeyListener;
	private MusicUsbReceiver mUsbReceiver;

	static {
		sList = new ArrayList<CommonFileInfo>();
		sAllList = new ArrayList<CommonFileInfo>();
		sListForMusicPlayer = new ArrayList<CommonFileInfo>();
		sFileList = new ArrayList<CommonFileInfo>();
		mSingerCollections = new HashMap<String, List<CommonFileInfo>>();
		mAlbumCollections = new HashMap<String, List<CommonFileInfo>>();
		mSonglistBySinger = new ArrayList<CommonFileInfo>();
		mSonglistByAlbum = new ArrayList<CommonFileInfo>();
		mSonglistByCollect = new ArrayList<CommonFileInfo>();
	}

	private Handler mHandlerFromSongFolderFragment;
	private Handler mHandlerFromAllSongListFragment;

	/***
	 * 存储根目录文件链表
	 */
	public static List<CommonFileInfo> sFileList = null;

	// Event事件接收，发送在LocalProvider中
	@SuppressWarnings("unchecked")
	public void onEventMainThread(IEvent event) {
		if (event instanceof EventCommResult) {
			EventCommResult commEvent = (EventCommResult) event;
			if (commEvent.type != Constant.MSG_LIST_SPECIFIC_MEDIATYPE)
				return;
			Trace.Info("get media list");
			CommonResult result = commEvent.result;
			MultimediaType eMediaType = (MultimediaType) result.data2;

			if (result.code == CommonResult.OK&&result.time>=startTime) {
				startTime=result.time;
				Trace.Info("get music list sucess");
				if (eMediaType != MultimediaType.MMT_MUSIC)
					return;

//				sList.clear();
//				sAllList.clear();
				List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
				List<CommonFileInfo> all_list = new ArrayList<CommonFileInfo>();
				list.addAll((List<CommonFileInfo>) result.data);
				all_list.addAll((List<CommonFileInfo>) result.data3);

				if (all_list.size() == 0) {
					showResult(null);
					return;
				}
				Trace.Info("####musicchange  EventCommResult");
				Trace.Info("####musicchange  EventCommResult"+all_list.size());
				long time = System.currentTimeMillis();
				mTagTime = time;
				sortMusicFiles(list, all_list, time);

			}
		} else if (event instanceof MusicFilesSortationEvent) {
			Trace.Info(mTagTime + "time1");
			Trace.Info(((MusicFilesSortationEvent) event).dataHolder.tagTime + "time2");

			if (((MusicFilesSortationEvent) event).eventType == MusicFilesSortationEvent.SORTATION_FINISH) {
				// 接收到此事件表明在后台对列表排序完成，显示结果
				if (((MusicFilesSortationEvent) event).dataHolder != null
						&& ((MusicFilesSortationEvent) event).dataHolder.tagTime == mTagTime) {
					showResult(((MusicFilesSortationEvent) event).dataHolder);
				}
			} else if (((MusicFilesSortationEvent) event).eventType == MusicFilesSortationEvent.REFRESH_MESSAGE) {

				if (((MusicFilesSortationEvent) event).dataHolder != null
						&& ((MusicFilesSortationEvent) event).dataHolder.tagTime == mTagTime) {
					mLoadingDialog.setMessageText(((MusicFilesSortationEvent) event).message);
				}
			}

		} else if (event instanceof EventTimeout) {
			// "扫描文件还需要一会儿时间，请耐心等待.."
			mLoadingDialog.setMessageText(this.getResources().getString(R.string.progress_dialog_loading_more));
		}
	}

	/*
	 * @Title: showResult
	 *
	 * @Description:显示扫描结果
	 */
	private synchronized void showResult(DataHolder dataHolder) {
		Trace.Info("####musicchange  showResult");

		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
		if (dataHolder == null) {
			mEmptyText.setVisibility(View.INVISIBLE);
			mHandlerFromAllSongListFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			if (null != mHandlerFromSongFolderFragment) {
				mHandlerFromSongFolderFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			}
			return;
		}
		clearAllData();
		sList.addAll(dataHolder.folderList);
		sAllList.addAll(dataHolder.allMusicList);
		mSonglistBySinger.addAll(dataHolder.songlistBySinger);
		mSonglistByAlbum.addAll(dataHolder.songlistByAlbum);
		//TODO 调试完去掉toast
		//Toast.makeText(MusicActivity.this, "清空"+mSonglistByCollect.size(), Toast.LENGTH_SHORT).show();
		Trace.Info("mSonglistByCollect"+mSonglistByCollect.size());
		mSonglistByCollect.addAll(dataHolder.songlistByCollect);
		//Toast.makeText(MusicActivity.this, "显示的我喜欢文件数"+mSonglistByCollect.size(), Toast.LENGTH_SHORT).show();
		Trace.Info("mSonglistByCollect"+mSonglistByCollect.size());
		mSingerCollections.putAll(dataHolder.singerCollections);
		mAlbumCollections.putAll(dataHolder.albumCollections);
		if (sAllList.size() == 0) {
			mEmptyText.setVisibility(View.VISIBLE);
		} else {
			mEmptyText.setVisibility(View.INVISIBLE);
		}
		Trace.Info("####musicchange  showResult"+sAllList.size());
		Trace.Info("slist.size -->" + sList.size());
		Trace.Info("sAllList.size -->" + sAllList.size());
		mHandlerFromAllSongListFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
		if (null != mHandlerFromSongFolderFragment) {
			mHandlerFromSongFolderFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
		}
	}

	/*
	 * @Title: sortMusicFiles
	 *
	 * @Description: 对文件进行排序
	 */
	private  void sortMusicFiles(final List<CommonFileInfo> folder_list, final List<CommonFileInfo> all_list,
			final long tagTime) {

		Trace.Info("####musicchange  sortMusicFiles");

		// 先启动对话框
		if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
			mLoadingDialog.setMessageText("");
			mLoadingDialog.show();
		}
		mEmptyText.setVisibility(View.INVISIBLE);
		Thread sortThread = new Thread(new Runnable() {

			@Override
			public void run() {

				DataHolder dataHolder = new DataHolder();
				dataHolder.tagTime = tagTime;
				dataHolder.folderList.addAll(folder_list);
				dataHolder.allMusicList.addAll(all_list);

				// 先获取“我喜欢”的数据
				MusicFilesSortationEvent message_event = new MusicFilesSortationEvent();
				message_event.eventType = MusicFilesSortationEvent.REFRESH_MESSAGE;
				message_event.dataHolder = dataHolder;
				message_event.message = MusicUtils.getResourceString(MusicActivity.this, R.string.music_warn_not_eject);
				EventDispatchCenter.getInstance().post(message_event);
				final ArrayList<LocalDiskInfo> usblist = Utils.getExternalStorage(mContext);
				List<File> tmpLikeList = new ArrayList<File>();
				CommonFileInfo commonFileInfo;
				Trace.Debug("usblistsize="+usblist.size());
				for (int i = 0; i < usblist.size(); i++) {
				//	tmpLikeList.clear();
					tmpLikeList = ContentManager.readDataFromDB(mApp, mUsbList.get(i).getPath(),
							MultimediaType.MMT_LIKEMUSIC);
					if (tmpLikeList != null) {
						int count = tmpLikeList.size();
						for (int j = 0; j < count; j++) {
							commonFileInfo = new CommonFileInfo(tmpLikeList.get(j));
							// 设置为喜欢
							commonFileInfo.setIsLike(true);
							// 添加进去
							if (!dataHolder.songlistByCollect.contains(commonFileInfo)) {
								dataHolder.songlistByCollect.add(commonFileInfo);
							}

						}
					}
					Trace.Debug("songlistByCollect="+dataHolder.songlistByCollect.size());
				}
				Trace.Info("mSonglistByCollect" + dataHolder.songlistByCollect.size());

//				final int size = dataHolder.songlistByCollect.size();
//
//				Handler handler = new Handler(getMainLooper());
//				handler.post(new Runnable() {
//
//					@Override
//					public void run() {
//						//TODO 调试完去掉toast
//						Toast.makeText(MusicActivity.this, "扫描到我喜欢的歌曲"+size, Toast.LENGTH_SHORT).show();
//
//					}
//				});


				// 获取歌曲信息
				MusicUtils.getAllSongInfo(dataHolder.songlistByCollect, null);
				MusicUtils.getAllSongInfo(dataHolder.allMusicList, null);

				// 歌手和专辑分类
				sortMusicBySingerAndAlbum(dataHolder);

				//歌手和专辑排序
				Collections.sort(dataHolder.songlistBySinger, new FileComparator.sortBySinger());
				Collections.sort(dataHolder.songlistByAlbum, new FileComparator.sortBySpecial());
				// “我喜欢”排序
				Operation.getInstance().sort(dataHolder.songlistByCollect, SortType.ST_BY_NAME);

				//操作完成，显示list
				MusicFilesSortationEvent event = new MusicFilesSortationEvent();
				event.eventType = MusicFilesSortationEvent.SORTATION_FINISH;
				event.dataHolder = dataHolder;
				EventDispatchCenter.getInstance().post(event);

			}
		});
		sortThread.setPriority(Thread.MAX_PRIORITY);
		sortThread.start();

	}

	/*
	 * @Description: 对全部歌曲按照歌手和专辑进行分类
	 */
	private void sortMusicBySingerAndAlbum(DataHolder dataHolder) {
		Trace.Info("sortMusicFiles");
		dataHolder.songlistByAlbum.clear();
		dataHolder.songlistBySinger.clear();
		// mSonglistByCollect.clear();
		dataHolder.singerCollections.clear();
		dataHolder.albumCollections.clear();
		CommonFileInfo fileInfo;
		String tempSinger;
		String temAlbum;
		List<String> singers = new ArrayList<String>();
		List<String> albums = new ArrayList<String>();

		for (int i = 0; i < dataHolder.allMusicList.size(); i++) {

			// 歌曲总列表和我喜欢的列表对应
			dataHolder.allMusicList.get(i).setIsLike(false);
			for (int j = 0; j < dataHolder.songlistByCollect.size(); j++) {
				if (dataHolder.songlistByCollect.get(j).getPath().equals(dataHolder.allMusicList.get(i).getPath())) {
					Trace.Debug("##setIsLike=" + true);
					dataHolder.allMusicList.get(i).setIsLike(true);
					break;
				}
			}

			fileInfo = dataHolder.allMusicList.get(i);
			temAlbum = fileInfo.getSpecial();
			tempSinger = fileInfo.getSinger();
			if (!singers.contains(tempSinger)) {
				singers.add(tempSinger);
				CommonFileInfo temp = new CommonFileInfo();
				temp.setSinger(tempSinger);
				temp.setDir(true);
				dataHolder.songlistBySinger.add(temp);
				List<CommonFileInfo> temp_list = new ArrayList<CommonFileInfo>();
				temp_list.add(fileInfo);
				dataHolder.singerCollections.put(tempSinger, temp_list);
			} else if (null != tempSinger) {
				if (dataHolder.singerCollections.get(tempSinger) != null) {
					dataHolder.singerCollections.get(tempSinger).add(fileInfo);
				}
			}

			if (!albums.contains(temAlbum)) {
				albums.add(temAlbum);
				CommonFileInfo temp = new CommonFileInfo();
				temp.setSpecial(temAlbum);
				temp.setDir(true);
				temp.setPath(fileInfo.getPath());
				dataHolder.songlistByAlbum.add(temp);
				List<CommonFileInfo> temp_list = new ArrayList<CommonFileInfo>();
				temp_list.add(fileInfo);
				dataHolder.albumCollections.put(temAlbum, temp_list);
			} else if (null != temAlbum) {
				if (dataHolder.albumCollections.get(temAlbum) != null) {
					dataHolder.albumCollections.get(temAlbum).add(fileInfo);
				}
			}
		}

		for (int i = 0; i < dataHolder.songlistBySinger.size(); i++) {
			dataHolder.songlistBySinger.get(i).setChildrenCount(
					dataHolder.singerCollections.get(dataHolder.songlistBySinger.get(i).getSinger()).size());
		}

		for (int i = 0; i < dataHolder.songlistByAlbum.size(); i++) {
			dataHolder.songlistByAlbum.get(i).setChildrenCount(
					dataHolder.albumCollections.get(dataHolder.songlistByAlbum.get(i).getSpecial()).size());
		}

		Trace.Debug("singer size==" + singers.size());
		Trace.Debug("album size==" + albums.size());
		Trace.Debug("singercolllec size==" + dataHolder.singerCollections.size());
		Trace.Debug("albumcolllec size==" + dataHolder.albumCollections.size());

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_filelist_layout);
		Configuration.isHome=false;
		Trace.Info("fragments oncreate");
		mFragmentManager = getFragmentManager();

		int backStackCount = mFragmentManager.getBackStackEntryCount();
		for(int i = 0; i < backStackCount; i++) {
			mFragmentManager.popBackStack();
		}
		mSongFolderFragment = new SongFolderFragment();
		mAllSongListFragment = new AllSongListFragment();
		mFragments = new HashMap<String, Fragment>();

		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.musiclist_content, mAllSongListFragment);
		fragmentTransaction.commit();
		mFragments.put("mAllSongListFragment", mAllSongListFragment);

		initViews();
		initData();

	}

	private void initData() {
		Trace.Info("Music init");
		mApp = (GlobalData) getApplication();
		mContext = getApplicationContext();
		mUsbList = Utils.getExternalStorage(mApp.getApplicationContext());

		// mApp.setSubHandler(mHandler, TAB_TYPE.TAB_MUSIC);
		mApp.notifySwitchFragment(TAB_TYPE.TAB_MUSIC);
		mOperation = Operation.getInstance();
		Configuration.SCAN_BY_FOLDER = true;
		IntentFilter filter = new IntentFilter();
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addDataScheme("file");
		mUsbReceiver = new MusicUsbReceiver();
		registerReceiver(mUsbReceiver, filter);

	}

	private void initViews() {

		mLoadingDialog = new LoadingDialog(MusicActivity.this, R.style.progressDialog_holo);
		mEmptyText = (TextView) findViewById(R.id.music_empty_text);

		mFolderTab = (TextView) findViewById(R.id.music_file_mode);
		mAllSongTab = (TextView) findViewById(R.id.music_sort_mode);
		mFolderTabSelected = (View) findViewById(R.id.music_file_mode_selected);
		mAllSongTabSelected = (View) findViewById(R.id.music_sort_mode_selected);
		mTabFocusLeft = (View) findViewById(R.id.music_mode_focus_left);
		mTabFocusRight = (View) findViewById(R.id.music_mode_focus_right);
		mBackButton = (RelativeLayout) findViewById(R.id.musiclist_top);

		mClickListener = new MusicListMainOnClickListener();
		mBackButton.setOnClickListener(mClickListener);
		mFolderTab.setOnClickListener(mClickListener);
		mAllSongTab.setOnClickListener(mClickListener);

		mFocusChangeListener = new MusicListFocusChangeListener();
		mFolderTab.setOnFocusChangeListener(mFocusChangeListener);
		mAllSongTab.setOnFocusChangeListener(mFocusChangeListener);

		mOnkeyListener = new MusicListOnkeyListener();
		mFolderTab.setOnKeyListener(mOnkeyListener);
		mAllSongTab.setOnKeyListener(mOnkeyListener);
		mBackButton.setOnKeyListener(mOnkeyListener);
		mFolderTab.requestFocus();

		mTabID = R.id.music_file_mode;
		mTabIDForBackButton = -1;

	}

	private void SwitchFragment(Fragment fragmentToshow, Fragment fragmentToHide, String showTag) {

		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.hide(fragmentToHide);

		if (mFragments.containsKey(showTag)) {
			fragmentTransaction.show(mFragments.get(showTag));
			fragmentTransaction.commit();
		} else {
			mFragments.put(showTag, fragmentToshow);
			fragmentTransaction.add(R.id.musiclist_content, fragmentToshow);
			fragmentTransaction.commit();
		}

		Trace.Info("#######size" + mFragments.size());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && mAllSongListFragment.isVisible()) {
			mAllSongListFragment.dismissLikeButton(event);
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onResume() {
		super.onResume();
		// 注册接收event事件
		if (Configuration.ISQUICKENTER) {
			Configuration.ISQUICKENTER = false;
			MusicActivity.this.finish();
		}
		EventDispatchCenter.getInstance().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// 不再接收event事件
		EventDispatchCenter.getInstance().unregister(this);
	}


	//重写 onSaveInstanceState方法，但是不调用 super方法，暂时解决崩溃之后activityfragment重叠的问题
	//TODO  布置会不会有副作用
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Configuration.isHome=true;
		if (mUsbReceiver != null) {
			unregisterReceiver(mUsbReceiver);
			mUsbReceiver = null;
		}
	}

	private void getMusicList() {
		Trace.Info("####musicchange  getMusicList");
		// 先清除相关的数据
		mSonglistByCollect.clear();
		mSonglistBySinger.clear();
		mSonglistByAlbum.clear();
		mSingerCollections.clear();
		mAlbumCollections.clear();
		final List<String> rootpath = new ArrayList<String>();
		if (mApp == null)
			return;

		final ArrayList<LocalDiskInfo> usblist = Utils.getExternalStorage(mContext);
		Trace.Info("UsbChanged" + usblist.size());
		if (usblist.size() == 0) {
			// 显示没有文件
			mEmptyText.setVisibility(View.VISIBLE);
			mTagTime = System.currentTimeMillis();
			if (mLoadingDialog != null) {
				mLoadingDialog.dismiss();
			}
			mHandlerFromAllSongListFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			if (null != mHandlerFromSongFolderFragment) {
				mHandlerFromSongFolderFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			}
			return;
		}
		for (int i = 0; i < usblist.size(); i++) {
			rootpath.add(usblist.get(i).getPath());
		}

		mEmptyText.setVisibility(View.INVISIBLE);

		if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
			if (!mLoadingDialog.isShowing()) {
				Trace.Info("dialog_music  show");
				mLoadingDialog.setMessageText(MusicUtils.getResourceString(this, R.string.scanning_file));
				if (!mLoadingDialog.isShowing()) {
					mLoadingDialog.show();
				}
			}else {
				mLoadingDialog.setMessageText(MusicUtils.getResourceString(this, R.string.scanning_file));
			}


		}

		new Thread(new Runnable() {
			public void run() {
				// 扫描全盘数据
				Trace.Debug("开启全盘扫描");
				Operation.getInstance().listWithSpecificMediaType(MultimediaType.MMT_MUSIC, rootpath, true);
			}
		}).start();
	}

	public void setmHandlerFromSongFolderFragment(Handler mHandlerFromSongFolderFragment) {
		this.mHandlerFromSongFolderFragment = mHandlerFromSongFolderFragment;
	}

	public void setmHandlerFromAllSongListFragment(Handler mHandlerFromAllSongListFragment) {
		this.mHandlerFromAllSongListFragment = mHandlerFromAllSongListFragment;
	}

	/**
	 * Activity监听到USB状态变化后会调用此函数，子Fragment无需注册/解绑广播
	 *
	 * @param intent
	 */
	public void notifyUsbChanged(Intent intent) {
//		ScrollGridView/.setBordViewVisibility(View.GONE);
		Trace.Info("-->usb change");
		clearAllData();
		mHandlerFromAllSongListFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
		if (null != mHandlerFromSongFolderFragment) {
			mHandlerFromSongFolderFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
		}
		getMusicList();
	}

	public static List<CommonFileInfo> getFileList() {
		return sList;
	}

	public static void clearAllData() {

		sAllList.clear();
		sList.clear();
		mSonglistBySinger.clear();
		mSonglistByAlbum.clear();
		mSonglistByCollect.clear();
		mSingerCollections.clear();
		mAlbumCollections.clear();


	}

	public static void copyTo(List<CommonFileInfo> list, List<CommonFileInfo> alist) {
		Trace.Debug("MusicCopyto");
		sList.clear();
		sList.addAll(list);
		sAllList.clear();
		sAllList.addAll(alist);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				for (int i = 0; i < sAllList.size(); i++) {
					// 歌曲总列表和我喜欢的列表对应
					sAllList.get(i).setIsLike(false);
					for (int j = 0; j < mSonglistByCollect.size(); j++) {
						if (mSonglistByCollect.get(j).getPath().equals(sAllList.get(i).getPath())) {
							Trace.Debug("##setIsLike=" + true);
							sAllList.get(i).setIsLike(true);
							break;
						}
					}
				}
				MusicUtils.getAllSongInfo(sAllList, null);

			}
		});
		// thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();

	}

	/**
	 * 监听USB状态
	 */
	public class MusicUsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Trace.Info("####musicchange  usbchange");
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
				notifyUsbChanged(intent);
			}
		}
	}

	class MusicListFocusChangeListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			switch (v.getId()) {
			case R.id.music_sort_mode:
				if (hasFocus) {
					mTabID = R.id.music_sort_mode;
					mAllSongTabSelected.setVisibility(View.INVISIBLE);
					mTabFocusLeft.setVisibility(View.INVISIBLE);
					mTabFocusRight.setVisibility(View.VISIBLE);
					((TextView) v).setTextColor(getResources().getColor(R.color.allsonglist_tab_top_selected));
				} else {
					mTabFocusLeft.setVisibility(View.INVISIBLE);
					mTabFocusRight.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.music_file_mode:
				if (hasFocus) {
					mTabID = R.id.music_file_mode;
					mFolderTabSelected.setVisibility(View.INVISIBLE);
					mTabFocusLeft.setVisibility(View.VISIBLE);
					mTabFocusRight.setVisibility(View.INVISIBLE);
					((TextView) v).setTextColor(getResources().getColor(R.color.allsonglist_tab_top_selected));
				} else {
					mTabFocusLeft.setVisibility(View.INVISIBLE);
					mTabFocusRight.setVisibility(View.INVISIBLE);
				}
				break;

			default:
				break;
			}

		}

	}

	class MusicListMainOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.musiclist_top:
				finish();
				break;
			case R.id.music_file_mode:
				mFolderTabSelected.setVisibility(View.INVISIBLE);
				mAllSongTabSelected.setVisibility(View.INVISIBLE);
				mTabFocusLeft.setVisibility(View.VISIBLE);
				mTabFocusRight.setVisibility(View.INVISIBLE);
				mFolderTab.setTextColor(getResources().getColor(R.color.allsonglist_tab_top_selected));
				mAllSongTab.setTextColor(getResources().getColor(R.color.allsonglist_tab_unselected));

				SwitchFragment(mAllSongListFragment, mSongFolderFragment, "mAllSongListFragment");
				break;
			case R.id.music_sort_mode:
				mAllSongTabSelected.setVisibility(View.INVISIBLE);
				mFolderTabSelected.setVisibility(View.INVISIBLE);
				mTabFocusLeft.setVisibility(View.INVISIBLE);
				mTabFocusRight.setVisibility(View.VISIBLE);
				mAllSongTab.setTextColor(getResources().getColor(R.color.allsonglist_tab_top_selected));
				mFolderTab.setTextColor(getResources().getColor(R.color.allsonglist_tab_unselected));
				SwitchFragment(mSongFolderFragment, mAllSongListFragment, "mSongFolderFragment");
				break;
			default:
				break;
			}

		}

	}

	class MusicListOnkeyListener implements OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {

				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (v.getId() == R.id.music_file_mode || v.getId() == R.id.music_sort_mode) {
						mTabID = v.getId();
					}
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
					if (v.getId() == R.id.music_file_mode || v.getId() == R.id.music_sort_mode) {
						mTabIDForBackButton = v.getId();
					}
				}
				if (v.getId() == R.id.music_file_mode) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						((TextView) v).setTextColor(getResources().getColor(R.color.allsonglist_tab_unselected));
						SwitchFragment(mSongFolderFragment, mAllSongListFragment, "mSongFolderFragment");
						mAllSongListFragment.setVisualizerView(false);

					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						mFolderTabSelected.setVisibility(View.VISIBLE);
						mTabFocusLeft.setVisibility(View.INVISIBLE);
						mTabFocusRight.setVisibility(View.INVISIBLE);
						((TextView) v).setTextColor(getResources()
								.getColor(R.color.allsonglist_tab_top_second_selected));
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						mFolderTabSelected.setVisibility(View.VISIBLE);
						mTabFocusLeft.setVisibility(View.INVISIBLE);
						mTabFocusRight.setVisibility(View.INVISIBLE);
						((TextView) v).setTextColor(getResources()
								.getColor(R.color.allsonglist_tab_top_second_selected));
						mHandlerFromAllSongListFragment.sendEmptyMessage(AllSongListFragment.SELECTE_FOUCUS_TAB);
						return true;

					}
				} else if (v.getId() == R.id.music_sort_mode) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						((TextView) v).setTextColor(getResources().getColor(R.color.allsonglist_tab_unselected));
						SwitchFragment(mAllSongListFragment, mSongFolderFragment, "mAllSongListFragment");
						// TODO 在这里刷新，合理吗？应找个更好的方法。
						mAllSongListFragment.refreshLikeList();

					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {

						if (mSongFolderFragment != null && mSongFolderFragment.isAvailableToHaveFocus()) {
							mAllSongTabSelected.setVisibility(View.VISIBLE);
							mTabFocusLeft.setVisibility(View.INVISIBLE);
							mTabFocusRight.setVisibility(View.INVISIBLE);
							((TextView) v).setTextColor(getResources().getColor(
									R.color.allsonglist_tab_top_second_selected));

						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						mAllSongTabSelected.setVisibility(View.VISIBLE);
						mTabFocusLeft.setVisibility(View.INVISIBLE);
						mTabFocusRight.setVisibility(View.INVISIBLE);
						((TextView) v).setTextColor(getResources()
								.getColor(R.color.allsonglist_tab_top_second_selected));
					}
				} else if (v.getId() == R.id.musiclist_top) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (mTabIDForBackButton != -1) {
							findViewById(mTabIDForBackButton).requestFocus();
							return true;
						}
					}
				}

			}
			return false;
		}
	}

	@Override
	public void onRefreshData() {

		Trace.Info("onRefreshData");
//		sList.clear();
//		sAllList.clear();
		clearAllData();
		mHandlerFromAllSongListFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
		if (null != mHandlerFromSongFolderFragment) {
			mHandlerFromSongFolderFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
		}
		getMusicList();

	}

	@Override
	public List<CommonFileInfo> getMusicFolderList() {
		Trace.Info("getMusicFolderList");
		return sList;
	}


	@Override
	protected void onStart() {

		Trace.Info("fragment size "+mFragments.size());
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		for (int i = 0; i < mFragments.size(); i++) {
			//fragmentTransaction.hide(fragment)
		}
		super.onStart();
	}

	/**
	 * @Description: 本身已经获取了焦点的返回false，表明不需要获取 返回true，表示从没有焦点到获取到焦点
	 */
	public boolean onRequestFocus(boolean isgetFocus) {

		if (isgetFocus) {
			findViewById(mTabID).requestFocus();
			return true;
		} else {
			if (findViewById(mTabID).isFocused()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static List<CommonFileInfo> getAllMusicList() {
		Trace.Info("sListForMusicPlayer" + sListForMusicPlayer.size());
		return sListForMusicPlayer;
	}

	public static List<CommonFileInfo> getAllMusicFile() {
		return sAllList;
	}

	@Override
	public void setSongList(List<CommonFileInfo> list) {
		sListForMusicPlayer = list;

	}

	public static List<CommonFileInfo> getLikeList() {
		return mSonglistByCollect;
	}

	public void deleteMusicFile(List<CommonFileInfo> list, CommonFileInfo file) {
		if (list != null) {

			int count = list.size();
			for (int i = 0; i < count; i++) {
				if (file.getPath().equals(list.get(i).getPath())) {
					list.remove(i);
				}
			}
		}

	}

	@Override
	public void onRefreshData(boolean needScan) {
		if (needScan) {
//			sList.clear();
//			sAllList.clear();
			clearAllData();
			mHandlerFromAllSongListFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			if (null != mHandlerFromSongFolderFragment) {
				mHandlerFromSongFolderFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			}
			getMusicList();
		} else {
			List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
			List<CommonFileInfo> all_list = new ArrayList<CommonFileInfo>();
			list.addAll(sList);
			all_list.addAll(sAllList);
			sList.clear();
			sAllList.clear();
			mHandlerFromAllSongListFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			if (null != mHandlerFromSongFolderFragment) {
				mHandlerFromSongFolderFragment.sendEmptyMessage(MusicActivity.REFRESH_DATA);
			}
			long time = System.currentTimeMillis();
			mTagTime = time;
			sortMusicFiles(list, all_list, time);
		}
	}

}
