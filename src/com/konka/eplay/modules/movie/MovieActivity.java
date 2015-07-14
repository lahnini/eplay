package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.List;

import android.R.attr;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.event.EventCommResult;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.EventTimeout;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.model.CommonResult;
import com.konka.eplay.model.LocalDiskInfo;
import com.konka.eplay.modules.AlwaysMarqueeTextView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.LoadingDialog;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.ScrollGridView;
import com.konka.eplay.modules.music.MusicPlayerService;

/**
 * @brief 视频文件浏览
 * @author situhui
 */
public class MovieActivity extends Activity {
	// 目录文件
	private static List<CommonFileInfo> sFirstList = new ArrayList<CommonFileInfo>();
	// 记录视频文件
	private static List<CommonFileInfo> sSecondList = new ArrayList<CommonFileInfo>();

	// handler message
	private static final int GET_VIDEO_LIST = 0;
	private static final int LIST_SHOW = 2;
	private static final int USB_GET_OUT = 100;

	private static final int REQUEST_CODE_DELETE = 1;

	private ScrollGridView mGridView;

	private BrowserAdapter mAdapter;
	private BrowserDirAdapter mDirAdapter;

	private View mBack;
	private TextView mBackText;
	// 回到顶部按钮
	private View mTop;
	private TextView mPathText;

	private LoadingDialog mLoadingDialog;

	// 音乐提示相关控件 add by xuyunyu
	FrameLayout mMusicTipLayout;
	ImageView mMusicWave;
	AlwaysMarqueeTextView mSongName;
	private MusicPlayerService mPlayerService;

	private MovieUsbReceiver mUsbReceiver;

	// 排序方式
	private SortType mSortType;
	private Operation mOperation;
	// 保存临时列表
	private static List<CommonFileInfo> tmpTimeList;
	private static List<CommonFileInfo> tmpNameList;
	static {
		tmpTimeList = new ArrayList<CommonFileInfo>();
		tmpNameList = new ArrayList<CommonFileInfo>();
	}

	// 最近一次进入的文件夹
	private CommonFileInfo mFolderFileInfo;

	// 显示视频文件夹的fragment
	private MovieDirFragment mDirFragment;
	// 显示视频的fragment
	private MovieFragment mMovieFragment;
	private static long startTime=0;

	// 设置当前显示给用户看到的gridView
	public void setShowedGridView(ScrollGridView gridView) {
		Trace.Debug("setShowedGridView");
		if (gridView != null) {
			mGridView = gridView;
		}
	}

	public BrowserAdapter getAdapter() {
		if (mAdapter == null) {
			mAdapter = new BrowserAdapter(this, sSecondList);
		}
		return mAdapter;
	}

	public BrowserDirAdapter getDirAdapter() {
		Trace.Debug("getDirAdapter");
		if (mDirAdapter == null) {
			Trace.Debug("new->getDirAdapter");
			mDirAdapter = new BrowserDirAdapter(this, sFirstList);
		}
		return mDirAdapter;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Trace.Debug("#####MovieActivity onCreate()");
		setContentView(R.layout.activity_movie);
		Configuration.isHome=false;
		mOperation = Operation.getInstance();
		mSortType = Configuration.sortType;

		registerUsbReceiver();
		EventDispatchCenter.getInstance().register(this);

		initView();
		// add by xuyunyu
		mPlayerService = MusicPlayerService.getInstance();
	}

	@Override
	protected void onStart() {
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
	}

	/*
	 * @Description: 开启音乐提示音乐跳动
	 */
	private void playMuisicAnimation() {

		Trace.Info("play animation");
		if (null == mPlayerService) {
			mMusicTipLayout.setVisibility(View.INVISIBLE);
		} else if (null != mPlayerService.getmMediaPlayer()) {
			if (mPlayerService.getmMediaPlayer().isPlaying()) {
				mMusicTipLayout.setVisibility(View.VISIBLE);
				AnimationDrawable anim = (AnimationDrawable) mMusicWave
								.getBackground();
				anim.start();
				mSongName.setText(mPlayerService.getMusicName());
			} else {
				mMusicTipLayout.setVisibility(View.VISIBLE);
				mSongName.setText(mPlayerService.getMusicName());
			}

		}

	}

	/*
	 * @Description: 关闭音乐提示音乐跳动
	 */
	private void stopMuisicAnimation() {

		mMusicTipLayout.setVisibility(View.VISIBLE);
		if (null != mMusicWave
						&& mMusicWave.getBackground() != null
						&& mMusicWave.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mMusicWave
							.getBackground();
			if (anim != null && anim.isRunning()) { // 如果正在运行,就停止
				anim.stop();
			}
		}
	}

	/*
	 * @Description: 更新音乐提示歌曲信息
	 */
	private void updateMuisicInfo() {
		if (null != mPlayerService) {
			mSongName.setText(mPlayerService.getMusicName());
		}

	}

	// 注册usb监听器
	private void registerUsbReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addDataScheme("file");
		mUsbReceiver = new MovieUsbReceiver();
		registerReceiver(mUsbReceiver, filter);
	}

	private void initView() {
		mBack = findViewById(R.id.back);
		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (backToDir()) {
					return;
				} else {
					Configuration.ISQUICKENTER = true;
					finish();
				}
			}
		});
		mBackText = (TextView) findViewById(R.id.back_text);
		mTop = findViewById(R.id.movie_top_btn);
		mTop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// mGridView.setSelection(0);
				// mGridView.requestFocus();

				mGridView.smoothScrollToTop();

			}
		});

		mPathText = (TextView) findViewById(R.id.file_path);
		mLoadingDialog = new LoadingDialog(this, R.style.progressDialog_holo);
		mLoadingDialog.setMessageText(getResources().getString(
						R.string.progress_dialog_file_loading));

		// add by xuyunyu
		mMusicTipLayout = (FrameLayout) findViewById(R.id.movie_music_tip_layout);
		mMusicWave = (ImageView) findViewById(R.id.movie_music_wave);
		mSongName = (AlwaysMarqueeTextView) findViewById(R.id.movie_music_songname);
		mMusicWave.setBackgroundResource(R.drawable.music_wave_anim);
		mMusicTipLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
				i.putExtra("isBackGround", true);
				startActivity(i);
			}
		});

		// 添加目录fragment
		mDirFragment = new MovieDirFragment();
		getFragmentManager().beginTransaction().add(R.id.browser, mDirFragment)
						.commit();

		if (sFirstList.size() == 0) {
			mHandler.sendEmptyMessage(GET_VIDEO_LIST);
		} else {
			mHandler.sendEmptyMessage(LIST_SHOW);
		}
	}

	// Event事件接收，发送在LocalProvider中
	public void onEventMainThread(IEvent event) {
		Trace.Debug("MovieActivity  onEventMainThread");
		if (event instanceof EventCommResult) {
			EventCommResult commEvent = (EventCommResult) event;
			if (commEvent.type != Constant.MSG_LIST_SPECIFIC_MEDIATYPE)
				return;
			CommonResult result = commEvent.result;
			MultimediaType eMediaType = (MultimediaType) result.data2;

			if (result.code == CommonResult.OK&&result.time>=startTime) {
				startTime=result.time;
				if (eMediaType != MultimediaType.MMT_MOVIE)
					return;
				sFirstList.clear();
				@SuppressWarnings("unchecked")
				List<CommonFileInfo> list = (List<CommonFileInfo>) result.data;
				Trace.Debug("list size " + list.size());
				sFirstList.addAll(list);
				if (Configuration.sortType == SortType.ST_BY_NAME) {
					tmpNameList.clear();
					tmpNameList.addAll(sFirstList);
				} else {
					tmpTimeList.clear();
					tmpTimeList.addAll(sFirstList);
				}
				list.clear();
				mHandler.sendEmptyMessage(LIST_SHOW);
			} else {
				// 错误处理
				// showToast(getString(R.string.toast_data_error));
			}
		} else if (event instanceof EventTimeout) {
			// "扫描文件还需要一会儿时间，请耐心等待.."
			mLoadingDialog.setMessageText(getResources().getString(
							R.string.progress_dialog_loading_more));
		} else if (event instanceof EventMusicStateChange) {

			switch (((EventMusicStateChange) event).musicStateType) {
			case Constant.MUSIC_SERVICE_FLAG_CHANGE_SONG:
				updateMuisicInfo();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PLAY:
				playMuisicAnimation();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PAUSE:
				stopMuisicAnimation();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_STOP:
				stopMuisicAnimation();
				mMusicTipLayout.setVisibility(View.INVISIBLE);
				break;
			}

		}
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			final List<String> rootPath = new ArrayList<String>();
			switch (msg.what) {
			case GET_VIDEO_LIST:
				backToDir();
				mDirFragment.setGridViewEmptyViewGone();
				showProDialog(true);
				ArrayList<LocalDiskInfo> usblist = Utils
								.getExternalStorage(getBaseContext());
				Trace.Debug("##### getExternalStorage()");
				usblist = Utils.getExternalStorage(MovieActivity.this);
				if (usblist.size() <= 0) {
					mHandler.sendEmptyMessage(USB_GET_OUT);
				} else {
					for (int i = 0; i < usblist.size(); i++) {
						rootPath.add(usblist.get(i).getPath());
					}
					new Thread() {
						public void run() {
							Looper.prepare();
							Operation.getInstance().listWithSpecificMediaType(
											MultimediaType.MMT_MOVIE, rootPath,
											true);
							// 扫描结果在onEventMainThread()回调中返回
						}
					}.start();
				}
				break;
			case LIST_SHOW:
				showProDialog(false);
//				backToDir();
				Trace.Debug("tmpNmaeListSize="+tmpNameList.size()+"tmpTimeListSize="+tmpTimeList.size());
				if (MovieActivity.this != null) {
					if (sFirstList.size() > 0) {
						if (Configuration.sortType == SortType.ST_BY_NAME
										&& tmpNameList.size() > 0) {
							sFirstList.clear();
							sFirstList .addAll(tmpNameList);
//						} else if (Configuration.sortType == SortType.ST_BY_TIME
//										&& tmpTimeList.size() > 0) {
//							sFirstList = tmpTimeList;
						} else {
							Operation.getInstance().sort(sFirstList, mSortType);
							if (mSortType == SortType.ST_BY_NAME
											&& tmpNameList.size()==0) {
								tmpNameList .clear();
								tmpNameList .addAll(sFirstList);
							} else if(mSortType == SortType.ST_BY_TIME
											&& tmpTimeList.size()==0) {
//								tmpTimeList = sFirstList;
							}
							Trace.Debug(" sort->tmpNmaeListSize="+tmpNameList.size()+"tmpTimeListSize="+tmpTimeList.size());

						}
						if (mDirAdapter != null) {
							Trace.Debug("显示视频列表 list size=" + sFirstList.size());
							mDirAdapter.notifyDataSetChanged();
						}
						mGridView.setSelection(0);
						mGridView.requestFocus();
						mDirFragment.setGridViewEmptyView();
						Trace.Debug("设置 dir fragment焦点 list size="
										+ sFirstList.size());
					} else {
						mHandler.sendEmptyMessage(USB_GET_OUT);
					}
				}
				break;
			case USB_GET_OUT:
				Trace.Debug("显示“无法找到视频”");
				showProDialog(false);
				// 回到第一级fragment
//				backToDir();// 放在这才能正确显示“无法找到视频”
				sFirstList.clear();
				sSecondList.clear();
				if (mDirAdapter != null) {
					mDirAdapter.notifyDataSetChanged();
				}
				mDirFragment.setGridViewEmptyView();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLoadingDialog.dismiss();
		Configuration.isHome=true;
		// add by liangyangyang
		new Thread() {
			public void run() {
				if (sFirstList.size() > 0) {
					ContentManager.writeData2DB(getApplicationContext(),
									sFirstList, MultimediaType.MMT_MOVIE);
					Trace.Info("write2db photo");
				}
			}
		}.start();

		if (mUsbReceiver != null) {
			unregisterReceiver(mUsbReceiver);
			mUsbReceiver = null;
		}
		EventDispatchCenter.getInstance().unregister(this);
	}

	class ItemOnKey implements View.OnKeyListener {
		@Override
		public boolean onKey(View view, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_MENU
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
				if (mGridView.getId() == R.id.movie_gridview) {
					int position = mGridView.getSelectedItemPosition();
					if (position < 0) {
						return false;
					}
					ArrayList<String> list = new ArrayList<String>();
					for (int i = 0; i < sSecondList.size(); i++) {
						list.add(sSecondList.get(i).getPath());
					}
					// 进入详情页
					Intent intent = new Intent();
					intent.setAction(Constant.MOVIE_INFO);
					intent.putStringArrayListExtra(Constant.PLAY_PATHS, list);
					intent.putExtra(Constant.PLAY_INDEX, position);
					startActivityForResult(intent, REQUEST_CODE_DELETE);
					return true;
				}
			}
			return false;
		}
	}

	class ItemSeleteListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View arg1,
						int position, long arg3) {
			Trace.Debug("onItemSelected " + "is dir ? "
							+ (parent.getId() == R.id.movie_dir_gridview));
			if (position >= 0 && position < parent.getAdapter().getCount()
							&& mGridView.hasFocus()) {

				String path = ((CommonFileInfo) parent.getAdapter().getItem(
								position)).getPath();
				String wrapperPath = Utils.getWrapperPath(path);
				mPathText.setText(getString(R.string.info_path) + wrapperPath);
				mPathText.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			Trace.Debug("onNothingSelected " + "is dir ? "
							+ (parent.getId() == R.id.movie_dir_gridview));
			mPathText.setText("");
		}

	}

	private void playVideo(final int position) {
		// 点击播放视频前，停止音乐播放
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.requestAudioFocus(new OnAudioFocusChangeListener() {

			@Override
			public void onAudioFocusChange(int focusChange) {
				Trace.Warning("###onAudioFocusChange==" + focusChange);
				if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				}
			}
		}, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		// 添加播放器处理
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < sSecondList.size(); i++) {
			list.add(sSecondList.get(i).getPath());
		}
		Intent intent = new Intent();
		intent.setAction(Constant.PLAY_VIDEO_ACTION);
		intent.putExtra(Constant.PLAY_INDEX, position);
		intent.putStringArrayListExtra(Constant.PLAY_PATHS, list);
		startActivity(intent);
	}

	private void showProDialog(boolean isshow) {
		if (isshow) {
			mLoadingDialog.setMessageText(getResources().getString(
							R.string.progress_dialog_file_loading));
			mLoadingDialog.show();
		} else {
			mLoadingDialog.dismiss();
		}
	}

	/**
	 * Activity监听到USB状态变化后会调用此函数，子Fragment无需注册/解绑广播
	 *
	 * @param intent
	 */
	public void notifyUsbChanged(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
			mHandler.sendEmptyMessage(GET_VIDEO_LIST);
		} else if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
			mHandler.sendEmptyMessage(GET_VIDEO_LIST);
		}
	}

	public static List<CommonFileInfo> getFileList() {
		return sFirstList;
	}

	public static void copyTo(List<CommonFileInfo> list) {
		sFirstList.clear();
		sFirstList.addAll(list);
		list.clear();
		if (Configuration.sortType == SortType.ST_BY_NAME) {
			tmpNameList.addAll(sFirstList);
		} else {
//			tmpTimeList = sFirstList;
		}
	}

	class OnGridViewScroll implements OnScrollListener {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
			if (mTop != null) {
				if (firstVisibleItem == 0) {
					mTop.setVisibility(View.INVISIBLE);
				} else {
					mTop.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Trace.Info("###lhq videoFragment onActivityResult resultcode"
						+ resultCode + "requestcode" + requestCode);
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_DELETE) {
			int deleteIndex = data.getIntExtra("delete_index", -1);
			if (deleteIndex >= 0 && deleteIndex <= sSecondList.size() - 1) {
				sSecondList.remove(deleteIndex);
				mFolderFileInfo.updateFileCount();
				Trace.Debug("文件夹中视频个数"
								+ mFolderFileInfo.getChildrenMovieCount());
				if (mFolderFileInfo.getChildrenMovieCount() == 0) {
					sFirstList.remove(mFolderFileInfo);
					backToDir();
				}
			}
		}
	}

	/**
	 * 监听USB状态
	 */
	public class MovieUsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Configuration.curMediaType == MultimediaType.MMT_MOVIE) {
				Trace.Info("MovieActivity###UsbChanged");
				notifyUsbChanged(intent);
			}

		}
	}

	class ItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> view, View arg1, int position,
						long arg3) {

			if (view.getId() == R.id.movie_dir_gridview) {
				CommonFileInfo file = (CommonFileInfo) mDirAdapter
								.getItem(position);
				enterFolder(file);
			} else {
				// 调用播放器播放视屏
				playVideo(position);
			}
		}

	};

	OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (v.getId() == R.id.movie_dir_gridview
							|| v.getId() == R.id.movie_gridview) {
				Trace.Debug("onFocusChange " + hasFocus + "####is dir ? "
								+ (v.getId() == R.id.movie_dir_gridview));
				if (hasFocus) {
					CommonFileInfo file = (CommonFileInfo) ((GridView) v)
									.getSelectedItem();
					if (file != null) {
						String wrapperPath = Utils.getWrapperPath(file
										.getPath());
						mPathText.setText(getString(R.string.info_path)
										+ wrapperPath);
						mPathText.setVisibility(View.VISIBLE);
					}
				} else {
					mPathText.setVisibility(View.GONE);
				}
			}
		}
	};

	// 进入视频文件夹
	private void enterFolder(CommonFileInfo file) {

		mFolderFileInfo = file;
		List<CommonFileInfo> list = mOperation.getSpecificFiles(file.getPath(),
						MultimediaType.MMT_MOVIE);

		if (list == null || list.isEmpty()) {
			return;
		}

		Trace.Info("###mSortType = " + mSortType.ordinal());
		sSecondList.clear();
		sSecondList.addAll(list);

		// if (mSortType == SortType.ST_BY_TIME) {
		// 视频文件按名称排序
		mOperation.sort(sSecondList, SortType.ST_BY_NAME);
		// }
		// else {
		// mOperation.sort(sSecondList, mSortType);
		// }

		if (mMovieFragment == null) {
			mMovieFragment = new MovieFragment();
		}
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.browser, mMovieFragment);
		transaction.addToBackStack(null);
		transaction.commit();

		setBackText(file.getName());
	}

	/**
	 * 设置视频浏览页左上角返回按钮的文字
	 *
	 * @param text
	 */
	void setBackText(String text) {
		if (mBackText == null) {
			mBackText = (TextView) findViewById(R.id.back_text);
		}
		mBackText.setText(text);
	}

	// 界面返回到目录层fragment，若当前已在目录返回false
	private boolean backToDir() {
		FragmentManager fm = getFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			fm.popBackStack();
			return true;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// *不去调用super保存状态，不能去掉这个方法*
	}
}
