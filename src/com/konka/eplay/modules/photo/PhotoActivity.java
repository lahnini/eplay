package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.Constant.DataSourceType;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.Constant.TAB_TYPE;
import com.konka.eplay.GlobalData;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.event.EventCommResult;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.EventTimeout;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.model.CommonResult;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.model.LocalDiskInfo;
import com.konka.eplay.model.FileComparator.sortByName;
import com.konka.eplay.model.LocalProvider;
import com.konka.eplay.modules.AlwaysMarqueeTextView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.LoadingDialog;
import com.konka.eplay.modules.MainActivity;
import com.konka.eplay.modules.MyGridView;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.PhotoAdapter;
import com.konka.eplay.modules.ScrollGridView;
import com.konka.eplay.modules.music.MusicActivity;
import com.konka.eplay.modules.music.MusicPlayerService;
import com.konka.eplay.modules.photo.label.PictureLabelBrowserActivity;

@SuppressLint({ "NewApi", "ShowToast" })
public class PhotoActivity extends Activity implements OnScrollListener,
				View.OnClickListener {

	public static final String TAG = "LocalThumbFragment";
	/***
	 * 存储根目录文件链表
	 */
	public static List<CommonFileInfo> sFileList = null;
	/***
	 * 要传递给下一个Activity当前文件链表
	 */
	public static List<CommonFileInfo> mSendList = null;
	static {
		sFileList = new ArrayList<CommonFileInfo>();
		mSendList = new ArrayList<CommonFileInfo>();
	}
	public static Map<String, List<CommonFileInfo>> mPhotoListMap;
	static {
		mPhotoListMap = new HashMap<String, List<CommonFileInfo>>();
	}
	private GlobalData mApp;
	/***
	 * 当前USB链表
	 */
	private List<LocalDiskInfo> mUsbList;
	private FrameLayout mLineListView;
	private ListView mTimeLineListView;
	private ListView mNameLineListView;
	private ScrollGridView mGridView;
	private TextView emptyView;
	private PopupWindow popupWindow;
	private Operation mOperation;
	private PhotoAdapter mAdapter;
	private TimeListAdapter mTimeListAdapter;
	private NameListAdapter mNameListAdapter;
	// 记录上一次时间轴的textview
	private TextView last2time;
	private TextView iv;
	private ImageView hLine;
	public static String curNameString = ".";
	public static String curTimeString = "0";
	// 时间轴更多
	private TextView moreTime;
	private CommonFileInfo mFile;
	private LoadingDialog mProDialog;
	private static List<CommonFileInfo> redLike = null;
	private static List<CommonFileInfo> yellowLike = null;
	private static List<CommonFileInfo> blueLike = null;
	public static List<File> tmpRedLike = null;
	public static List<File> tmpBlueLike = null;
	public static List<File> tmpYellowLike = null;

	private ImageButton mTopBtn;
	private LinearLayout mBackBtn;
	private TextView mBackBtnTxt;
	private TextView mPhotoPathTxt;
	// 收藏分类红绿蓝
	private LinearLayout mLabelLayout;
	private ImageView mRedButton;
	private ImageView mBlueButton;
	private ImageView mYellowButton;
	// 按菜单键textview
	private TextView menuTextView;

	// 音乐提示相关控件 add by xuyunyu
	FrameLayout mMusicTipLayout;
	ImageView mMusicWave;
	AlwaysMarqueeTextView mSongName;
	private MusicPlayerService mPlayerService;
	// 是否删除图片
	private static boolean isDelete = false;
	// 是否写图片列表
	protected boolean hasWrite = false;
	protected boolean flag;
	// 是否进入编辑模式
	public boolean mIsEditMode;
	//是否正在滚动
	private boolean isScroll=false;
	// 是否已进入文件夹
	private boolean mEnterFolder = false;
	// 文件夹路径
	public String mkdirPath = null;
	// 记录进入当前目录时，光标所在的位置。返回上一层时使用
	private int mLocation = 0;
	// 选中的待编辑操作的文件列表
	public List<CommonFileInfo> mSelectedFileList = new ArrayList<CommonFileInfo>();
	List<CommonFileInfo> puzzleList;
	private DataSourceType mCurDataSourceType;
	private Thread writeMap;

	// 当前选中项
	private int mSelected;
	private String mCurrPath;
	private PhotoUsbReceiver mUsbReceiver;
	private SortType mSortType;
	private View mPreView;
	private int mHostPos = 0;
	private Toast mToast = null;
	public static final int RED = 3;
	public static final int BLUE = 2;
	public static final int YELLOW = 1;
	protected static final int GET_PHOTO = 0;
	protected static final int REFRESH_DATA = 1;
	protected static final int NO_DATA = 2;

	private IntentFilter usbFilter;
	protected int mTotalItemCount = -1;
	protected int mFirstVisibleItem = -1;
	protected int mVisibleItemCount = -1;
	private static long startTime = 0;
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_PHOTO:
				emptyView.setVisibility(View.GONE);
				PictureLabelBrowserActivity.finishActivity();
				ImageViewPagerActivity.finishActivity();
				new Thread(new FindImgThreadFromDisk()).start();
				break;
			case REFRESH_DATA:
				emptyView.setVisibility(View.GONE);

				mAdapter.switchList(sFileList);
				// // TODO Auto-generated method stub

				// mOperation.sort(sFileList, mSortType);
				mAdapter.notifyDataSetChanged();
				mProDialog.dismiss();
				mGridView.setSelection(0);
				mGridView.requestFocus();
				mLineListView.setVisibility(View.GONE);
				mBackBtnTxt.setText(R.string.photobtn);
				menuTextView.setVisibility(View.GONE);
				mLabelLayout.setVisibility(View.VISIBLE);
				if (mToast != null)
					mToast.cancel();
				break;
			case NO_DATA:
				if (mProDialog.isShowing())
					mProDialog.dismiss();
				ImageViewPagerActivity.finishActivity();
				mAdapter.switchList(sFileList);
				mTimeLineListView.setVisibility(View.GONE);
				mNameLineListView.setVisibility(View.GONE);
				mAdapter.notifyDataSetChanged();
				// mCurrentFilePath.setVisibility(View.GONE);
				mEnterFolder = false;
				mBackBtnTxt.setText(R.string.photobtn);
				// add by lhq 无盘时设置一下emptyview
				mGridView.setEmptyView(emptyView);
				if (mProDialog.isShowing())
					mProDialog.dismiss();
			default:
				break;
			}

		}
	};

	private static void initList() {
		redLike = new ArrayList<CommonFileInfo>();
		yellowLike = new ArrayList<CommonFileInfo>();
		blueLike = new ArrayList<CommonFileInfo>();
		tmpRedLike = new ArrayList<File>();
		tmpBlueLike = new ArrayList<File>();
		tmpYellowLike = new ArrayList<File>();
	}

	public void onEventMainThread(IEvent event) {
		if (event instanceof EventCommResult) {
			EventCommResult commEvent = (EventCommResult) event;
			if (commEvent.type != Constant.MSG_LIST_SPECIFIC_MEDIATYPE)
				return;
			CommonResult result = commEvent.result;
			MultimediaType eMediaType = (MultimediaType) result.data2;

			if (result.code == CommonResult.OK && result.time >= startTime) {
				startTime = result.time;
				if (eMediaType != MultimediaType.MMT_PHOTO)
					return;

				sFileList.clear();
				@SuppressWarnings("unchecked")
				List<CommonFileInfo> list = (List<CommonFileInfo>) (result.data);
				sFileList.addAll(list);
				if (sFileList.size()==0) {
					mProDialog.dismiss();
				}
				// mOperation.sort(sFileList, mSortType);

				writeMap = new Thread(new Runnable() {

					@Override
					public void run() {
						hasWrite = true;
						List<CommonFileInfo> tmpNamePhotoList = new ArrayList<CommonFileInfo>();
						List<CommonFileInfo> tmpTimePhotoList = new ArrayList<CommonFileInfo>();
						tmpNamePhotoList.addAll(sFileList);
						tmpTimePhotoList.addAll(sFileList);
						if (mSortType == SortType.ST_BY_NAME) {
							mPhotoListMap.put("nameList", tmpNamePhotoList);
							mOperation.sort(tmpTimePhotoList,
											SortType.ST_BY_TIME);
							mPhotoListMap.put("timeList", tmpTimePhotoList);
						} else {
							mPhotoListMap.put("timeList", tmpTimePhotoList);
							mOperation.sort(tmpNamePhotoList,
											SortType.ST_BY_NAME);
							mPhotoListMap.put("nameList", tmpNamePhotoList);

						}
						// TODO Auto-generated method stub
						int count = sFileList.size();
						for (int i = 0; i < count; i++) {
							Trace.Debug("###putMap=" + i);
							if (i < sFileList.size()) {

								String tmpPath = sFileList.get(i).getPath();
								List<CommonFileInfo> tmpPhotoList = Operation
												.getInstance()
												.getSpecificFiles(
																tmpPath,
																MultimediaType.MMT_PHOTO);
								Collections.sort(tmpPhotoList,
												new FileComparator.sortByName());
								mPhotoListMap.put(tmpPath, tmpPhotoList);
							}
						}

					}
				});
				writeMap.start();
				list.clear();
				if (sFileList.size() == 0) {
					mEnterFolder = false;
					mGridView.setEmptyView(emptyView);
					return;
				}
				mHandler.sendEmptyMessage(REFRESH_DATA);
			} else {
				// mProDialog.dismiss();
				// mGridView.setEmptyView(emptyView);
				mHandler.sendEmptyMessage(NO_DATA);
			}
		} else if (event instanceof EventTimeout) {
			// "扫描文件还需要一会儿时间，请耐心等候..."
			mProDialog.setMessageText(mApp.getResources().getString(
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Trace.Debug("###onCreate()");
		super.onCreate(savedInstanceState);
		Configuration.isHome=false;
		if (redLike == null) {
			initList();
		}

		mSortType = Configuration.sortType;
		Trace.Debug("msortType" + mSortType);
		setContentView(R.layout.activity_photo);
		mApp = (GlobalData) getApplication();
		mApp.notifySwitchFragment(TAB_TYPE.TAB_IMG);
		mOperation = Operation.getInstance();
		mCurDataSourceType = DataSourceType.DST_LOCAL;
		usbFilter = new IntentFilter();
		usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		usbFilter.addDataScheme("file");
		mUsbReceiver = new PhotoUsbReceiver();
		registerReceiver(mUsbReceiver, usbFilter);
		initView();

		mGridView.setSelection(0);
		// mGridView.setFocusable(true);
		mGridView.requestFocus();
		// add by xuyunyu
		mPlayerService = MusicPlayerService.getInstance();
	}

	private void initView() {

		mGridView = (ScrollGridView) findViewById(R.id.local_ablum_gridview);
		mLineListView = (FrameLayout) findViewById(R.id.listlayout);
		mTimeLineListView = (ListView) findViewById(R.id.time_list);
		mNameLineListView = (ListView) findViewById(R.id.name_list);
		mBackBtn = (LinearLayout) findViewById(R.id.backphoto);
		mBackBtn.setOnClickListener(this);
		mBackBtnTxt = (TextView) findViewById(R.id.backtxt);
		mPhotoPathTxt = (TextView) findViewById(R.id.photo_path);
		mLabelLayout = (LinearLayout) findViewById(R.id.labellayout);
		mRedButton = (ImageView) findViewById(R.id.label_red_border);
		mBlueButton = (ImageView) findViewById(R.id.label_blue_border);
		mYellowButton = (ImageView) findViewById(R.id.label_yellow_border);
		menuTextView = (TextView) findViewById(R.id.menutext);
		moreTime = (TextView) findViewById(R.id.moretime);
		emptyView = (TextView) findViewById(R.id.local_empty_view);
		// add by xuyunyu
		mMusicTipLayout = (FrameLayout) findViewById(R.id.photo_music_tip_layout);
		mMusicWave = (ImageView) findViewById(R.id.photo_music_wave);
		mSongName = (AlwaysMarqueeTextView) findViewById(R.id.photo_music_songname);
		mMusicWave.setBackgroundResource(R.drawable.music_wave_anim);
		mMusicTipLayout.setOnClickListener(this);

		mRedButton.setOnClickListener(this);
		mBlueButton.setOnClickListener(this);
		mYellowButton.setOnClickListener(this);
		Trace.Info("sfilelist" + sFileList.size());
		mGridView.setOverScrollMode(View.OVER_SCROLL_NEVER);

		mProDialog = new LoadingDialog(this, R.style.progressDialog_holo);// TODO
		mProDialog.setMessageText(mApp.getResources().getString(
						R.string.progress_dialog_loading));
		mUsbList = Utils.getExternalStorage(mApp.getApplicationContext());
		List<CommonFileInfo> tmpnameList = mPhotoListMap.get("nameList");
		List<CommonFileInfo> tmptimeList = mPhotoListMap.get("timeList");
		if (sFileList.size() == 0) {
			if (mUsbList != null && mUsbList.size() > 0) {
				Trace.Info("###find data");
				mProDialog.setMessageText(mApp.getResources().getString(
								R.string.progress_dialog_loading));
				mProDialog.show();
				emptyView.setVisibility(View.GONE);
				new Thread(new FindImgThreadFromCache()).start();
			} else {
				mProDialog.dismiss();
				emptyView.setVisibility(View.VISIBLE);
				mGridView.setEmptyView(emptyView);
				mEnterFolder = false;
			}
		} else {

			if (tmpnameList != null && mSortType == SortType.ST_BY_NAME) {
				Trace.Debug("readNamlist");
				sFileList.clear();
				sFileList.addAll(tmpnameList);
			} else if (tmptimeList != null && mSortType == SortType.ST_BY_TIME) {
				Trace.Debug("readtimlist");
				sFileList.clear();
				sFileList.addAll(tmptimeList);
			} else {
				Trace.Debug("sortList");
				// mOperation.sort(sFileList, mSortType);
			}

		}

		mAdapter = new PhotoAdapter(getApplication(), sFileList,
						mSelectedFileList);
		mGridView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		mGridView.setOnFocusChangeListener(new FocusChangeListener());
		mGridView.setOnItemSelectedListener(new ItemSelectedListener());
		mGridView.setOnKeyListener(new KeyListener());
		mNameLineListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
				// TODO Auto-generated method stub
				String firstLetterString = mNameListAdapter.getNameList().get(
								position);
				int count = mSendList.size();
				for (int i = 0; i < count; i++) {
					if (mSendList.get(i).getFirstLetter()
									.equals(firstLetterString)) {
						Trace.Debug("###Equals");
						Trace.Debug("####scroll to position NameLineListView click");
						mGridView.smoothScrollToPosition(i);
						break;
					}
				}
				Trace.Debug("###GridViewSelection="
								+ mGridView.getSelectedItemPosition());
			}

		});

		mNameLineListView
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent,
											View view, int position, long id) {
								// TODO Auto-generated method stub
								Trace.Debug("###getlastposition="
												+ mNameLineListView
																.getLastVisiblePosition());
								// 用于控制字母轴下方更多提示的出现与消失
								if (mNameLineListView.getLastVisiblePosition() == mNameListAdapter
												.getNameList().size() - 1) {
									moreTime.setVisibility(View.GONE);
								} else {
									moreTime.setVisibility(View.VISIBLE);
								}
								// 修改字母轴的字体颜色及下划线的出现与消失
								if (iv != null)
									iv.setTextColor(getResources().getColor(
													R.color.text_nofocus));
								iv = (TextView) view
												.findViewById(R.id.txt_date_time);
								iv.setTextColor(getResources().getColor(
												R.color.white));
								if (hLine != null)
									hLine.setVisibility(View.GONE);
								hLine = (ImageView) view
												.findViewById(R.id.h_name_line);
								hLine.setVisibility(View.VISIBLE);

								// } else {
								// hLine.setBackgroundResource(R.color.transparent);
								// }
								String firstLetterString = mNameListAdapter
												.getNameList().get(position);
								if (mNameLineListView.hasFocus()) {
									hLine.setBackgroundResource(R.drawable.time_selected);
									curNameString = firstLetterString;
									//mAdapter.notifyDataSetChanged();
								}else {
									hLine.setBackgroundResource(R.color.transparent);
								}
								// 用于跳转至符合字母轴当前选中字母包含的第一个文件处
								int count = mSendList.size();
								for (int i = 0; i < count; i++) {
									if (mSendList.get(i).getFirstLetter()
													.equals(firstLetterString)) {
										Trace.Debug("###Equals");
										if (mNameLineListView.hasFocus()) {
											
											Trace.Debug("gridViewPOsition="
															+ mGridView.getFirstVisiblePosition()
															+ "i=" + i);
											Trace.Debug("####scroll to position NameLineListView");
											// 执行跳转

											if(!mGridView.ScrollToPosition(i)){
												Trace.Debug("###not scroll");
												mAdapter.notifyDataSetChanged();
											};

										}
										break;
									}
								}
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
								// TODO Auto-generated method stub

							}
						});
		mNameLineListView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus/* &&mNameLineListView.getVisibility()==View.VISIBLE */) {
					hLine.setBackground(getResources().getDrawable(
									R.drawable.time_selected));
					curNameString = mNameListAdapter.getNameList()
									.get(mNameLineListView
													.getSelectedItemPosition());
					mAdapter.notifyDataSetChanged();
				} else {
					curNameString = ".";
					hLine.setBackgroundResource(R.color.transparent);
					mAdapter.notifyDataSetChanged();
				}
			}
		});

		mTimeLineListView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus/* &&mNameLineListView.getVisibility()==View.VISIBLE */) {
					hLine.setBackgroundResource(R.drawable.time_selected);
					curTimeString = mTimeListAdapter.getTimeList()
									.get(mTimeLineListView
													.getSelectedItemPosition());
					mAdapter.notifyDataSetChanged();
				} else {
					curTimeString = "0";
					hLine.setBackgroundResource(R.color.transparent);
					if (mEnterFolder){
					
						Trace.Debug("refreshData");
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		});
		mTimeLineListView.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == KeyEvent.ACTION_DOWN) {

					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT&&isScroll) {
						Trace.Debug("isScrollTrue");
						return true;
					}
					}
				return false;
			}
		});
		mNameLineListView.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT&&isScroll) {
						Trace.Debug("isScrollTrue");
						return true;
					}
				}
				return false;
			}
		});

		mTimeLineListView
						.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent,
											View view, int position, long id) {
								// TODO Auto-generated method stub
								// 控制显示时间轴的更多提示
								if (mTimeLineListView.getLastVisiblePosition() == mTimeListAdapter
												.getTimeList().size() - 1) {
									moreTime.setVisibility(View.GONE);
								} else {
									moreTime.setVisibility(View.VISIBLE);
								}
								// 控制显示字体颜色及下划线
								if (iv != null)
									iv.setTextColor(getResources().getColor(
													R.color.text_nofocus));
								iv = (TextView) view
												.findViewById(R.id.txt_date_time);
								iv.setTextColor(getResources().getColor(
												R.color.white));
								if (last2time != null)
									last2time.setTextColor(getResources()
													.getColor(R.color.text_nofocus));
								last2time = (TextView) view
												.findViewById(R.id.last_date_time);
								last2time.setTextColor(getResources().getColor(
												R.color.white));
								if (hLine != null)
									hLine.setVisibility(View.GONE);
								hLine = (ImageView) view
												.findViewById(R.id.h_line);
								hLine.setVisibility(View.VISIBLE);
								String modifyTime = mTimeListAdapter
												.getTimeList().get(position);
								if (mTimeLineListView.hasFocus()) {
									curTimeString = modifyTime;
									hLine.setBackgroundResource(R.drawable.time_selected);
								} else {
									hLine.setBackgroundResource(R.color.transparent);
								}

								// 跳转至时间轴当前选中时间包含第一个文件
								int count = mSendList.size();
								for (int i = 0; i < count; i++) {
									if (Utils.dateToStr(
													mSendList.get(i)
																	.getModifiedTime())
													.toString()
													.equals(modifyTime)) {
										Trace.Debug("###Equals");
										// mGridView.setFocusable(true);
										// mGridView.requestFocus();
										if (mTimeLineListView.hasFocus()) {

											// mGridView.smoothScrollTo(
											// mGridView.getFirstVisiblePosition(),
											// i);
											Trace.Debug("####scroll to position TimeLineListView");
											// 执行跳转
											if(!mGridView.ScrollToPosition(i)){
												Trace.Debug("###not scroll");
												mAdapter.notifyDataSetChanged();
											};
										}
										break;
									}
								}
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
								// TODO Auto-generated method stub

							}
						});
		mGridView.setOnItemClickListener(new ItemClickListener());

		// if (mCurDataSourceType == DataSourceType.DST_LAN) {
		// ((RelativeLayout) getActivity().findViewById(R.id.collect_button))
		// .setVisibility(View.INVISIBLE);
		// } else {
		// ((RelativeLayout) getActivity().findViewById(R.id.collect_button))
		// .setVisibility(View.VISIBLE);
		// }
		
		// if (mCurDataSourceType == DataSourceType.DST_CLOUD)
		// emptyView.setText(getString(R.string.cannotfind_cloud_picture));
		// else if (mCurDataSourceType == DataSourceType.DST_LAN)
		// emptyView.setText(getString(R.string.cannotfind_lan_picture));

		mTopBtn = (ImageButton) findViewById(R.id.local_album_top_btn);
		mTopBtn.setOnClickListener(this);

		mTopBtn.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						// Trace.Debug("####onkey isbig" + isBig);
						mGridView.requestFocus();
						return true;
					}
				}
				return false;
			}
		});

		// mOperation.sort(sFileList, mSortType);
		// mAdapter.notifyDataSetChanged();

		// mGridView.setFocusableInTouchMode(true);
		mGridView.setOnScrollListener(this);

		mGridView.setBorderView((ImageView) this
						.findViewById(R.id.border_view_in_mygridView));
		if (!hasWrite && mPhotoListMap.isEmpty()) {

			writeMap = new Thread(new Runnable() {

				@Override
				public void run() {
					hasWrite = true;
					List<CommonFileInfo> tmpNamePhotoList = new ArrayList<CommonFileInfo>();
					List<CommonFileInfo> tmpTimePhotoList = new ArrayList<CommonFileInfo>();
					tmpNamePhotoList.addAll(sFileList);
					tmpTimePhotoList.addAll(sFileList);
					if (mSortType == SortType.ST_BY_NAME) {
						mPhotoListMap.put("nameList", tmpNamePhotoList);
						mOperation.sort(tmpTimePhotoList, SortType.ST_BY_TIME);
						mPhotoListMap.put("timeList", tmpTimePhotoList);
					} else {
						mPhotoListMap.put("timeList", tmpTimePhotoList);
						mOperation.sort(tmpNamePhotoList, SortType.ST_BY_NAME);
						mPhotoListMap.put("nameList", tmpNamePhotoList);

					}
					// TODO Auto-generated method stub
					int count = sFileList.size();
					for (int i = 0; i < count; i++) {
						Trace.Debug("###putMap=" + i);
						if (i < sFileList.size()) {

							String tmpPath = sFileList.get(i).getPath();
							List<CommonFileInfo> tmpPhotoList = Operation
											.getInstance()
											.getSpecificFiles(
															tmpPath,
															MultimediaType.MMT_PHOTO);
							Collections.sort(tmpPhotoList,
											new FileComparator.sortByName());
							mPhotoListMap.put(tmpPath, tmpPhotoList);
						}
					}

				}
			});
			writeMap.start();

		}
	}

	private class FocusChangeListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View arg0, boolean hasFocus) {
			if (hasFocus) {
				int position = mGridView.getSelectedItemPosition();
				mPhotoPathTxt.setText(getResources().getString(R.string.path)
								+ Utils.getWrapperPath(mAdapter
												.getAdapterList().get(position)
												.getPath()));
				mPhotoPathTxt.setVisibility(View.VISIBLE);
			} else {
				mPhotoPathTxt.setVisibility(View.GONE);
			}
		}
	}

	private class ItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
			mPhotoPathTxt.setVisibility(View.VISIBLE);
			Trace.Info("###mGridView.hasFocus() = " + mGridView.hasFocus());
			Trace.Info("###position=" + position);
			mSelected = position;
			if (position < mAdapter.getCount() && position >= 0
							&& mGridView.hasFocus()) {
				CommonFileInfo curFile = mAdapter.getAdapterList()
								.get(position);

				mPhotoPathTxt.setText(getResources().getString(R.string.path)
								+ Utils.getWrapperPath(curFile.getPath()));
				if (mEnterFolder
								&& mNameLineListView.getVisibility() == View.VISIBLE) {

					String firstLetterString = curFile.getFirstLetter();
					Trace.Debug("###FrstLetter" + firstLetterString);
					List<String> nameList = new ArrayList<String>();
					nameList = mNameListAdapter.getNameList();
					int count = nameList.size();
					if (count > 0) {
						for (int i = 0; i < count; i++) {
							if (nameList.get(i).equals(firstLetterString)) {
								mNameLineListView.setSelection(i);
								break;
							}
						}
					}
				}
				if (mEnterFolder
								&& mTimeLineListView.getVisibility() == View.VISIBLE) {

					String data = Utils.dateToStr(curFile.getModifiedTime())
									.toString();
					Trace.Debug("###modifedtme=" + data);
					List<String> timeList = new ArrayList<String>();
					timeList = mTimeListAdapter.getTimeList();
					int count = timeList.size();
					if (count > 0) {
						for (int i = 0; i < count; i++) {
							Trace.Debug("###modifiedtime(i)=" + timeList.get(i));
							if (timeList.get(i).equals(data)) {
								Trace.Debug("###local=" + i);
								mTimeLineListView.setSelection(i);
								break;
							}
						}
					}
				}

				// if (parent.hasFocus()) {
				// if (mPreView != null) {
				// mPreView.clearAnimation();
				// }
				// mPreView = view;
				// } else {
				// mGridView.setSelection(-1);
				// }

			} else {

				Trace.Debug("###selection#-1");
				mPhotoPathTxt.setText(null);
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// mPhotoPathTxt.setVisibility(View.INVISIBLE);
			Trace.Debug("###onNothingSelected()");
			// mCurrentFilePath.setText(getString(R.string.my_photo) + "("
			// + mAdapter.getAdapterList().size() + ")");
			if (mPreView != null) {
				// Animation animation1 = AnimationUtils.loadAnimation(
				// mApp, R.anim.shrink);
				// mPreView.startAnimation(animation1);
				mPreView.clearAnimation();
			}
		}

	}

	private class KeyListener implements OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_MENU
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
				GridView parent = (GridView) v;
				int selPos = parent.getSelectedItemPosition();
				int pos = selPos - parent.getFirstVisiblePosition();
				View view = parent.getChildAt(pos);
				if (view != null) {
					// showMenu(view, mGridView.getSelectedItemPosition());
					Trace.Debug("####show Menu");
					if (mEnterFolder) {

						showDetailsMenu(view,
										mGridView.getSelectedItemPosition());
					}
					return true;
				}
			} else if (event.getAction() == KeyEvent.ACTION_DOWN) {

				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					if (mSelected % 4 == 0
									&& mTimeLineListView.getVisibility() == View.VISIBLE) {
						if (mEnterFolder) {

							mTimeLineListView.setFocusable(true);
							mTimeLineListView.requestFocus();
						}
						return true;
					} else if (mSelected % 4 == 0
									&& mNameLineListView.getVisibility() == View.VISIBLE) {
						if (mEnterFolder) {

							mNameLineListView.setFocusable(true);
							mNameLineListView.requestFocus();
						}
						return true;
					}

				} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					if ((mSelected % 4 == 3 || (mAdapter.getAdapterList()
									.size() - 1 == mSelected))
									&& mTopBtn.getVisibility() == View.INVISIBLE) {
						return true;
					}
				}/*
				 * else if(keyCode==KeyEvent.KEYCODE_DPAD_UP){ if(mSelected>7){
				 * mGridView.scrollBy(0, 358); // return true; } }else
				 * if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN){ if(mSelected>3){
				 * mGridView.scrollBy(0,358); // return true; } }
				 */
			} else if (keyCode == KeyEvent.KEYCODE_BACK
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
				Trace.Debug("##########keyback");
			}

			return false;
		}
	}

	@SuppressLint("DefaultLocale")
	private class ItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
						long id) {
			Trace.Info("###onItemClick position = " + position);
			mFile = mAdapter.getAdapterList().get(position);
			if (mFile.isDir()) {
				mBackBtnTxt.setText(mFile.getName());
				mLabelLayout.setVisibility(View.GONE);
				menuTextView.setVisibility(View.VISIBLE);
				mCurrPath = mFile.getPath();

				List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
				mLocation = position;
				mEnterFolder = true;
				Trace.Info("###mSortType = " + mSortType.ordinal());

				mLineListView.setVisibility(View.VISIBLE);

				if (Configuration.sortType == SortType.ST_BY_TIME) {
					list = Operation.getInstance().getSpecificFiles(mCurrPath,
									MultimediaType.MMT_PHOTO);
					mTimeListAdapter = new TimeListAdapter(getApplication(),
									list);
					mTimeLineListView.setAdapter(mTimeListAdapter);
					Trace.Debug("###lastVisiblePosition="
									+ mTimeLineListView
													.getLastVisiblePosition());
					Trace.Debug("###firstVisiblePosition="
									+ mTimeLineListView
													.getFirstVisiblePosition());

					Trace.Debug("###timeListAdapter="
									+ mTimeListAdapter.getTimeList().size());
					if (5 < mTimeListAdapter.getTimeList().size()) {
						Trace.Debug("###setgone");
						moreTime.setVisibility(View.VISIBLE);
					} else {
						moreTime.setVisibility(View.GONE);
					}
					mTimeLineListView.setVisibility(View.VISIBLE);
					mNameLineListView.setVisibility(View.GONE);
					mTimeListAdapter.notifyDataSetChanged();
				} else if (Configuration.sortType == SortType.ST_BY_NAME) {
					List<CommonFileInfo> tmplist = mPhotoListMap.get(mCurrPath);

					if (tmplist != null && tmplist.size() > 0) {
						Trace.Debug("readcache--mcurpath=" + mCurrPath);
						Trace.Debug("readcache--tmplist.size=" + tmplist.size());
						list.addAll(tmplist);
					} else {
						Trace.Debug("readcachefailed");
						list = Operation.getInstance().getSpecificFiles(
										mCurrPath, MultimediaType.MMT_PHOTO);
						Collections.sort(list, new FileComparator.sortByName());
					}
					mNameListAdapter = new NameListAdapter(getApplication(),
									list);
					mNameLineListView.setAdapter(mNameListAdapter);
					if (5 < mNameListAdapter.getNameList().size()) {
						Trace.Debug("###setgone");
						moreTime.setVisibility(View.VISIBLE);
					}
					mTimeLineListView.setVisibility(View.GONE);
					mNameLineListView.setVisibility(View.VISIBLE);
					mNameListAdapter.notifyDataSetChanged();
				}

				// mOperation.sort(list, mSortType);
				if (list == null || list.isEmpty()) {
					// showToast(getString(R.string.file_open_fail), 0);
					return;
				}
				int count = list.size();
				for (int i = 0; i < count; i++) {
					if (redLike.size() > 0) {
						int countRed = redLike.size();
						for (int j = 0; j < countRed; j++) {
							if (list.get(i).getPath()
											.equals(redLike.get(j).getPath())) {
								list.get(i).setIsRed(true);
								list.get(i).setIsBlue(false);
								list.get(i).setIsYellow(false);
								j = countRed;
								Trace.Debug("%%isRED");
							}
						}

					}
					if (blueLike.size() > 0) {
						int countBlue = blueLike.size();
						for (int j = 0; j < countBlue; j++) {
							if (list.get(i).getPath()
											.equals(blueLike.get(j).getPath())) {
								Trace.Debug("%%isBlue");
								list.get(i).setIsRed(false);
								list.get(i).setIsBlue(true);
								list.get(i).setIsYellow(false);
								j = countBlue;
							}
						}

					}
					if (yellowLike.size() > 0) {
						int countYellow = yellowLike.size();
						for (int j = 0; j < countYellow; j++) {
							if (list.get(i)
											.getPath()
											.equals(yellowLike.get(j).getPath())) {
								Trace.Debug("%%isYellow");
								list.get(i).setIsRed(false);
								list.get(i).setIsBlue(false);
								list.get(i).setIsYellow(true);
								j = countYellow;
							}
						}

					}
				}
				mAdapter.switchList(list);
				mAdapter.notifyDataSetChanged();
				// mGridView.requestFocus();
				mGridView.setSelection(0);
				if (!list.isEmpty()) {
					// mCurrentFilePath.setText(Utils.getWrapperPath(mAdapter
					// .getAdapterList().get(0).getPath()));
					Trace.Info("###mGridView is focusing = "
									+ mGridView.hasFocus());
					mSendList.clear();
					mSendList = mAdapter.getAdapterList();
				}
			} else {
				Intent intent = new Intent();
				intent.setAction(Constant.PLAY_IMAGE_ACTION);
				if (!mEnterFolder) {
					int size = mSendList.size();
					List<CommonFileInfo> temList = new ArrayList<CommonFileInfo>();
					int count = 0;
					for (int i = 0; i < size; i++) {
						CommonFileInfo cfi = mSendList.get(i);
						if (cfi.isDir()) {
							temList.add(cfi);
							count++;
						}
						if (mFile == cfi) {
							position = position - count;
							Trace.Info("###position = " + position);
						}
					}
					mSendList.removeAll(temList);
				}
				intent.putExtra(Constant.PLAY_INDEX, position);
				startActivityForResult(intent, 1);
			}
		}
	}

	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// super.onActivityCreated(savedInstanceState);
	// Trace.Debug("###onActivityCreated");
	// }

	@Override
	public void onStart() {
		Trace.Debug("###onStart()");
		super.onStart();
		if (null != mPlayerService && !mPlayerService.getPlayState()) {
			mMusicTipLayout.setVisibility(View.INVISIBLE);
			return;
		}
		playMuisicAnimation();
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

	private void updateMuisicInfo() {
		if (null != mPlayerService) {
			mSongName.setText(mPlayerService.getMusicName());
		}

	}

	@Override
	public void onResume() {
		Trace.Debug("###onResume()");
		super.onResume();
		Trace.Debug("###onResume+mSendList=" + mSendList.size());
		if (mSendList.size() == 0) {
			deleteFile(sFileList, mCurrPath);
			
			mAdapter.switchList(sFileList);
			mEnterFolder = false;
			mBackBtnTxt.setText(R.string.photobtn);
			mNameLineListView.setVisibility(View.GONE);
			mTimeLineListView.setVisibility(View.GONE);
			mLabelLayout.setVisibility(View.GONE);
			if (mPhotoListMap.get("nameList") != null) {

				deleteFile(mPhotoListMap.get("nameList"), mCurrPath);
			}
			if (mPhotoListMap.get("timeList") != null) {
				deleteFile(mPhotoListMap.get("timeList"), mCurrPath);
			}
		}
		mAdapter.notifyDataSetChanged();
		Trace.Debug("isdeleted="+isDelete);
		if (sFileList.size()==0&&(mNameListAdapter!=null||mTimeListAdapter!=null)) {
			Trace.Debug("resume emptyview");
			mGridView.setEmptyView(emptyView);
		}

		// 注册接收event事件
		EventDispatchCenter.getInstance().register(this);
	}

	@Override
	public void onPause() {
		Trace.Debug("###onPause()");
		super.onPause();
		// 不再接收event事件
		EventDispatchCenter.getInstance().unregister(this);
	}

	@Override
	public void onStop() {
		Trace.Debug("###onStop()");
		super.onStop();

		stopMuisicAnimation();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Configuration.isHome=true;
		new Thread() {
			public void run() {
				if (sFileList.size() > 0) {
					ContentManager.writeData2DB(getApplicationContext(),
									sFileList, MultimediaType.MMT_PHOTO);
					Trace.Info("write2db photo");
				}
			}
		}.start();
		Trace.Debug("###onDestroy()");
		Trace.Debug("##BlueLikeSize=" + blueLike.size());
		mIsEditMode = false;
		if (mUsbReceiver != null) {
			unregisterReceiver(mUsbReceiver);
			mUsbReceiver = null;
		}
		if (mProDialog != null)
			mProDialog.dismiss();
		mProDialog = null;
		// Trace.Info("###sFileList.size() = " + sFileList.size());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.backphoto:
			if (mEnterFolder) {

				if (isDelete) {
					new Thread(new FindImgThreadFromDisk()).start();
					isDelete = false;
				} else {
					mAdapter.switchList(sFileList);
					mLineListView.setVisibility(View.GONE);

					mAdapter.notifyDataSetChanged();
					mGridView.requestFocus();
					mBackBtnTxt.setText(R.string.photobtn);
					menuTextView.setVisibility(View.GONE);
					mLabelLayout.setVisibility(View.VISIBLE);
					Trace.Info("###mLocation = " + mLocation);
					mGridView.setSelection(mLocation);
					// mCurrentFilePath.setText(Utils.getWrapperPath(sFileList
					// .get(mLocation).getPath()));
					// Trace.Info("###mLocation = "
					// + mGridView.getSelectedItemPosition() + "");
				}
				mEnterFolder = false;
				Trace.Info("###mEnterFolder = " + mEnterFolder);
			} else if (false == mEnterFolder) {
				Configuration.ISQUICKENTER = true;
				// Configuration.curMediaType = MultimediaType.MMT_PHOTO;
				emptyView.setVisibility(View.GONE);
				PhotoActivity.this.finish();

			}
			break;
		case R.id.local_album_top_btn:
			// mGridView.setSelection(0);
			// mGridView.requestFocus();
			mGridView.smoothScrollToTop();

			// 友盟统计
			// MobclickAgent.onEvent(mApp, MMUmeng.EVENT_ID_MM_UTILS,
			// MMUmeng.MM_UTILS_EVENT_KEY_3);
			break;
		case R.id.label_yellow_border: {
			// mAdapter.switchList(yellowLike);
			// mAdapter.notifyDataSetChanged();
			Intent intent = new Intent(PhotoActivity.this,
							PictureLabelBrowserActivity.class);
			intent.putExtra(Constant.LABEL, YELLOW);
			startActivity(intent);
		}
			break;
		case R.id.label_blue_border: {
			// mAdapter.switchList(blueLike);
			// mAdapter.notifyDataSetChanged();

			Intent intent = new Intent(PhotoActivity.this,
							PictureLabelBrowserActivity.class);
			intent.putExtra(Constant.LABEL, BLUE);
			startActivity(intent);
		}
			break;
		case R.id.label_red_border: {
			Trace.Debug("##Click+redLikeSize##" + redLike.size());
			// mAdapter.switchList(redLike);
			// mAdapter.notifyDataSetChanged();
			Intent intent = new Intent(PhotoActivity.this,
							PictureLabelBrowserActivity.class);
			intent.putExtra(Constant.LABEL, RED);
			startActivity(intent);
		}
			break;
		case R.id.photo_music_tip_layout:
			Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
			i.putExtra("isBackGround", true);
			startActivity(i);
			break;
		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
		if (mTopBtn != null) {
			if (firstVisibleItem == 0)
				mTopBtn.setVisibility(View.INVISIBLE);
			else
				mTopBtn.setVisibility(View.VISIBLE);
		}
		mFirstVisibleItem = firstVisibleItem;
		mVisibleItemCount = visibleItemCount;
		mTotalItemCount = totalItemCount;

	}

	@Override
	public void onScrollStateChanged(AbsListView listView, int scrollState) {
		Trace.Debug("###onScrollStateChanged");
		
		switch (scrollState) {
		// 滚动结束
		case OnScrollListener.SCROLL_STATE_IDLE:
			isScroll=false;
			Trace.Debug("###onScrollStateChanged stop");
			if (mGridView.isScrollToTop() || mGridView.isScrollToPosition()) {
				mAdapter.setScrollTopMark(false);
				break;
			}
			// 滚动结束才让所需线程运行
			ImageLoader.getInstance(this).unLockLoad();
			if (mGridView.getScrollDirection() == Constant.SCROLL_UP) {
				ImageLoader.getInstance(this).cancelTaskInList(
								mFirstVisibleItem, mFirstVisibleItem + 7,
								Constant.SCROLL_UP);
			} else if (mGridView.getScrollDirection() == Constant.SCROLL_DOWN) {
				ImageLoader.getInstance(this).cancelTaskInList(
								mFirstVisibleItem, mFirstVisibleItem + 7,
								Constant.SCROLL_DOWN);
			}
			break;
		// 正在滚动
		case OnScrollListener.SCROLL_STATE_FLING:
			isScroll=true;
			Trace.Debug("###onScrollStateChanged fling");
			if (mGridView.isScrollToTop() || mGridView.isScrollToPosition()) {
				mAdapter.setScrollTopMark(true);
				break;
			}
			// 滚动时锁住线程创建运行，避免滚动卡顿
			ImageLoader.getInstance(this).lockLoad();
			break;
		default:
			break;
		}
	}

	/**
	 * Activity监听到USB状态变化后会调用此函数，子Fragment无需注册/解绑广播
	 * 
	 * @param intent
	 */
	public void notifyUsbChanged(Intent intent) {
		Trace.Debug("###notifyUsbChanged");
		if (mProDialog.isShowing())
			mProDialog.dismiss();
		if (mProDialog != null) {
			mProDialog.setMessageText(mApp.getResources().getString(
							R.string.progress_dialog_loading));
			mProDialog.show();
		}
		String action = intent.getAction();
		Trace.Debug("##### getExternalStorage()");
		mUsbList = Utils.getExternalStorage(mApp.getApplicationContext());
		Trace.Info("###mUsbList.size() = " + mUsbList.size());
		mEnterFolder = false;
		if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {// sdcard绑定
			if (emptyView != null)
				emptyView.setVisibility(View.GONE);
			// if (mToast == null)
			// showToast(R.string.usb_mount);f
			if (mUsbList != null && mUsbList.size() > 0) {
				mHandler.sendEmptyMessage(GET_PHOTO);
				// mCurrentFilePath.setVisibility(View.VISIBLE);
			}
		} else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {// sdcard移除
			// if (mToast == null)
			// showToast(R.string.usb_remove);
			if (writeMap != null) {
				Thread dummy = writeMap;
				writeMap = null;
				dummy.interrupt();
			}
			if (null == mUsbList || mUsbList.size() == 0) {
				mGridView.setOnItemSelectedListener(null);
				sFileList.clear();
				mHandler.sendEmptyMessage(NO_DATA);
			} else if (mUsbList != null && mUsbList.size() > 0) {
				mHandler.sendEmptyMessage(GET_PHOTO);
			}
		}

	}

	public void requestFocus(boolean flag) {
		this.flag = flag;
	}

	private void showDetailsMenu(View view, int position) {
		CommonFileInfo fileInfo = mSendList.get(position);
		final PhotoDetailsPopupWindow popupWindow = new PhotoDetailsPopupWindow(
						PhotoActivity.this, fileInfo, position);
		popupWindow.setAnimationStyle(R.style.popwin_anim_style);
		popupWindow.showAsDropDown(view, Utils.dip2px(PhotoActivity.this, 59),
						-Utils.dip2px(PhotoActivity.this, 189));
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub

				if (popupWindow.isChanged()) {
					mAdapter.notifyDataSetChanged();
					new Thread() {
						public void run() {
							// 标为蓝的照片
							if (blueLike != null) {
								ContentManager.writeData2DB(
												getApplicationContext(),
												blueLike,
												MultimediaType.MMT_BLUEPHOTO);
							}
							// 标为黄的照片
							if (yellowLike != null) {
								Trace.Debug("###writeYellowPhoto");
								ContentManager.writeData2DB(
												getApplicationContext(),
												yellowLike,
												MultimediaType.MMT_YELLOWPHOTO);
							}

							// 标为红的照片
							if (redLike != null) {
								Trace.Debug("###writeRedPhoto");
								Trace.Debug("##redPhotoSize##" + redLike.size());
								ContentManager.writeData2DB(
												getApplicationContext(),
												redLike,
												MultimediaType.MMT_REDPHOTO);
							}

						}
					}.start();
				}
			}
		});

	}

	class FindImgThreadFromCache implements Runnable {

		@Override
		public void run() {
			List<String> roots = new ArrayList<String>();
			int size = mUsbList.size();
			tmpBlueLike.clear();
			tmpRedLike.clear();
			tmpYellowLike.clear();
			Trace.Info("###USBsize = " + size);
			if (size > 0) {
				List<File> tmpRedList = new ArrayList<File>();
				List<File> tmpBlueList = new ArrayList<File>();
				List<File> tmpYellowList = new ArrayList<File>();
				for (int i = 0; i < size; i++) {
					Trace.Debug("i=" + i + "##usbsize=" + mUsbList.size());
					String pathString = mUsbList.get(i).getPath();
					roots.add(pathString);

					tmpRedList = ContentManager.readDataFromDB(mApp,
									pathString, MultimediaType.MMT_REDPHOTO);
					tmpBlueList = ContentManager.readDataFromDB(mApp,
									pathString, MultimediaType.MMT_BLUEPHOTO);
					tmpYellowList = ContentManager.readDataFromDB(mApp,
									pathString, MultimediaType.MMT_YELLOWPHOTO);
					if (tmpBlueList != null) {
						tmpBlueLike.addAll(tmpBlueList);
						tmpBlueList.clear();
					}
					if (tmpRedList != null) {
						tmpRedLike.addAll(tmpRedList);
						tmpRedList.clear();
					}
					if (tmpYellowList != null) {
						tmpYellowLike.addAll(tmpYellowList);
						tmpYellowList.clear();
					}
				}

				// Trace.Info("###start to find image from cache");
				mOperation.listWithSpecificMediaType(MultimediaType.MMT_PHOTO,
								roots, true);
			}
			Trace.Debug("##if>0##" + tmpRedLike.size());
			if (tmpRedLike.size() > 0) {
				int count = tmpRedLike.size();

				for (int j = 0; j < count; j++) {
					CommonFileInfo redCommonFileInfo = new CommonFileInfo(
									tmpRedLike.get(j));
					redCommonFileInfo.setIsRed(true);
					redLike.add(redCommonFileInfo);
				}
				Trace.Debug("likeredsize="+redLike.size());
				tmpRedLike.clear();
			}
			if (tmpBlueLike.size() > 0) {
				int countB = tmpBlueLike.size();
				Trace.Debug("##tmpBlueLikesize=" + tmpBlueLike.size());
				for (int j = 0; j < countB; j++) {
					CommonFileInfo blueCommonFileInfo = new CommonFileInfo(
									tmpBlueLike.get(j));
					blueCommonFileInfo.setIsBlue(true);
					blueLike.add(blueCommonFileInfo);
				}
				tmpBlueLike.clear();
			}
			if (tmpYellowLike.size() > 0) {
				int countY = tmpYellowLike.size();

				for (int j = 0; j < countY; j++) {
					CommonFileInfo yellowCommonFileInfo = new CommonFileInfo(
									tmpYellowLike.get(j));
					yellowCommonFileInfo.setIsYellow(true);
					yellowLike.add(yellowCommonFileInfo);
				}
				tmpYellowLike.clear();
			}
			Trace.Debug("##redLikeSize##" + redLike.size());
			Trace.Debug("##yellowLikeSize##" + yellowLike.size());
			Trace.Debug("##blueLikeSize##" + blueLike.size());
		}
	}

	class FindImgThreadFromDisk implements Runnable {
		@Override
		public void run() {
			Trace.Info("###read from disk");
			List<String> roots = new ArrayList<String>();
			int size = mUsbList.size();
			// String pathString="mnt/usb/sda1";
			// roots.add(pathString);
			Trace.Info("###size = " + size);
			for (int i = 0; i < size; i++) {
				roots.add(mUsbList.get(i).getPath());
			}
			mOperation.listWithSpecificMediaType(MultimediaType.MMT_PHOTO,
							roots, true, true);
		}
	}

	// class FindCloudImgThread implements Runnable {
	// @Override
	// public void run() {
	// mOperation.listWithSpecificMediaType(MultimediaType.MMT_PHOTO,
	// null, true);
	// }
	// }

	// class FindLanImgThread implements Runnable {
	// @Override
	// public void run() {
	// List<String> roots = new ArrayList<String>();
	//
	// roots.add("/mnt/samba");
	// mOperation.listWithSpecificMediaType(MultimediaType.MMT_PHOTO,
	// roots, true);
	// }
	// }

	/**
	 * 获取文件列表，再Activity退出时调用，clear
	 * 
	 * @return
	 */
	public static List<CommonFileInfo> getFileList() {
		return sFileList;
	}

	/** 设置是否删除 */
	public static void setIsDelete(boolean isdelete) {
		isDelete = isdelete;
	}

	/**
	 * 获取文件列表，再Activity退出时调用，clear
	 * 
	 * @return
	 */
	public static List<CommonFileInfo> getLikePhotoList(int colorIndex) {
		switch (colorIndex) {
		case YELLOW:
			return yellowLike;
		case BLUE:
			return blueLike;
		case RED:
			return redLike;
		default:
			return redLike;
		}
	}

	public static void copyTo(List<CommonFileInfo> list) {
		sFileList.clear();
		sFileList.addAll(list);
		list.clear();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Trace.Debug("####onActivityResult");
		// Trace.Debug("###requestCode = " + requestCode);
		// Trace.Debug("###resultCode = " + resultCode);
		if (requestCode == 1 && resultCode == -1) {
			if (mEnterFolder) {
				List<CommonFileInfo> list = Operation.getInstance()
								.getSpecificFiles(mCurrPath,
												MultimediaType.MMT_PHOTO);
				if (list != null) {
					mAdapter.switchList(list);
					if (Configuration.sortType == SortType.ST_BY_TIME) {
						mTimeLineListView.setVisibility(View.VISIBLE);
						mNameLineListView.setVisibility(View.GONE);
						mTimeListAdapter.switchList(list);
						mTimeListAdapter.notifyDataSetChanged();

					} else if (Configuration.sortType == SortType.ST_BY_NAME) {
						mTimeLineListView.setVisibility(View.GONE);
						mNameLineListView.setVisibility(View.VISIBLE);
						mNameListAdapter.switchList(list);
						mNameListAdapter.notifyDataSetChanged();
					}
					mAdapter.notifyDataSetChanged();
					mGridView.setSelection(0);
					// if (!list.isEmpty())
					// mCurrentFilePath.setText(Utils.getWrapperPath(mAdapter
					// .getAdapterList().get(0).getPath()));
				}
			} else {
				new Thread(new FindImgThreadFromDisk()).start();
			}
		} else if (requestCode == 1 && resultCode == 2) {
			// Trace.Info("###mFile.getPath() = " + mFile.getPath());
			String dir = Utils.getRootPath(mFile.getPath());
			// + getString(R.string.pic_beautify_dir);
			Trace.Info("###dir = " + dir);
			Trace.Info("###mFile.getPath = " + dir);
			if (mEnterFolder
							&& Utils.getParentPath(mFile.getPath()).equals(dir)) {
				Trace.Info("###start thread");
				List<CommonFileInfo> list = mOperation.getSpecificFiles(dir,
								MultimediaType.MMT_PHOTO);
				if (list != null && list.size() > 0) {
					mAdapter.switchList(list);
					mAdapter.notifyDataSetChanged();
				}
			} else {
				CommonFileInfo file = new CommonFileInfo(new File(dir));
				if (!Utils.contains(sFileList, file))
					sFileList.add(file);
				mAdapter.notifyDataSetChanged();
			}
		} else if (requestCode == 2 && resultCode == -1) {
			CommonFileInfo cfi = puzzleList.get(0);
			String dir = Utils.getRootPath(cfi.getPath());
			// + getString(R.string.pic_puzzle_dir);
			Trace.Info("###dir = " + dir);
			// Trace.Info("###cfi.getPath() = " + cfi.getPath());
			if (mEnterFolder && Utils.getParentPath(cfi.getPath()).equals(dir)) {
				List<CommonFileInfo> list = mOperation.getSpecificFiles(dir,
								MultimediaType.MMT_PHOTO);
				if (list != null && list.size() > 0) {
					mAdapter.switchList(list);
					mAdapter.notifyDataSetChanged();
				}
			} else {
				CommonFileInfo file = new CommonFileInfo(new File(dir));
				Trace.Info("###file.getPath() = " + file.getPath());
				if (!Utils.contains(sFileList, file)) {
					Trace.Info("###contains");
					sFileList.add(file);
				}
				mAdapter.notifyDataSetChanged();
			}
		} else if (requestCode == 1 && resultCode == 3) {
			if (mCurDataSourceType == DataSourceType.DST_LOCAL) {
				// int size = mUsbList.size();
				Trace.Debug("##### getExternalStorage()");
				mUsbList = Utils.getExternalStorage(mApp
								.getApplicationContext());
				// if (size < mUsbList.size())
				// showToast(getString(R.string.usb_mount), 0);
				// else
				// showToast(getString(R.string.usb_remove), 0);
				if (mUsbList.size() > 0) {
					Trace.Info("###here");
					new Thread(new FindImgThreadFromDisk()).start();
				} else {
					Trace.Info("###here22");
					sFileList.clear();
					mAdapter.switchList(sFileList);
					mAdapter.notifyDataSetChanged();
					mGridView.setEmptyView(emptyView);
					mEnterFolder = false;
				}
			}
		} else if (requestCode == 3 && resultCode == -1) {
			isDelete = true;
			// CommonFileInfo deleteFileInfo = mAdapter.getAdapterList().get(
			// mGridView.getPosition());
			// if (deleteFileInfo.getIsBlue())
			// deleteBlue(deleteFileInfo);
			// if (deleteFileInfo.getIsRed())
			// deleteRed(deleteFileInfo);
			// if (deleteFileInfo.getIsYellow())
			// deleteYellow(deleteFileInfo);
			// File deleteFile = new File(deleteFileInfo.getPath());
			// deleteFile.delete();
			// mAdapter.getAdapterList().remove(mGridView.getPosition());
			// mSendList.remove(mGridView.getPosition());
			deleteFile(mPhotoListMap.get(mCurrPath), mAdapter.getAdapterList()
							.get(mLocation).getPath());
			mAdapter.notifyDataSetChanged();
			Trace.Debug("###deletesize2=" + mAdapter.getAdapterList().size());
		}
	}

	public static void deleteFile(List<CommonFileInfo> fileList, String path) {
		// TODO Auto-generated method stub
		if (fileList.size() > 0) {

			int i = 0;
			for (i = 0; i < fileList.size(); i++) {
				if (fileList.get(i).getPath().equals(path)) {
					fileList.remove(i);
					break;

				}
			}
		}
	}

	public int getHostPos() {
		return mHostPos;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
			Trace.Info("######### KEYCODE_BACK");
			
			if(isScroll){
				return true;
			}else if (mEnterFolder) {
				curNameString = ".";
				curTimeString = "0";
				Configuration.MARKISENTER = false;
				mEnterFolder = false;
				mGridView.requestFocus();
				mBackBtnTxt.setText(R.string.photobtn);
				mLabelLayout.setVisibility(View.VISIBLE);
				if (isDelete) {
					ImageLoader.clearCache();
					mFile.setChildrenPhotoCount(mSendList.size());
					mAdapter.switchList(sFileList);
					mAdapter.notifyDataSetChanged();
					// new Thread(new FindImgThreadFromDisk()).start();
					isDelete = false;
				} else {
					mAdapter.switchList(sFileList);
					mAdapter.notifyDataSetChanged();
					mLineListView.setVisibility(View.GONE);
					mBackBtnTxt.setText(R.string.photobtn);
					menuTextView.setVisibility(View.GONE);
					mLabelLayout.setVisibility(View.VISIBLE);
					mGridView.setSelection(mLocation);
					// Trace.Info("sFileList.size() = " + sFileList.size() +
					// "");

					// mGridView.requestFocus();
					Trace.Info("###mLocation = " + mLocation);
					// mCurrentFilePath.setText(Utils.getWrapperPath(sFileList
					// .get(mLocation).getPath()));
					// Trace.Info("###mLocation = "
					// + mGridView.getSelectedItemPosition() + "");
				}
				
				
				Trace.Info("###mEnterFolder = " + mEnterFolder);
				return true;
			} else if (false == mEnterFolder) {
				emptyView.setVisibility(View.GONE);
				PhotoActivity.this.finish();
				return true;
			} else if (popupWindow != null && popupWindow.isShowing()) {
				popupWindow.dismiss();
				return true;
			}
		}
		// 交给所属Activity处理按键事件
		return false;
	}

	/**
	 * 监听USB状态
	 */
	public class PhotoUsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Trace.Debug("###new intent" + intent.getAction());
			// if (Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
			Trace.Info("PhotoActivity###UsbChanged");
			notifyUsbChanged(intent);
			// } else if (intent.getAction().equals(Constant.DELETEPHOTO)) {
			// Trace.Debug("###delete Photo");
			// Trace.Debug("###deletesize1="
			// + mAdapter.getAdapterList().size());
			// CommonFileInfo deleteFileInfo = mAdapter.getAdapterList()
			// .get(mGridView.getPosition());
			// File deleteFile = new File(deleteFileInfo.getPath());
			// deleteFile.delete();
			// mAdapter.getAdapterList().remove(mGridView.getPosition());
			// mAdapter.notifyDataSetChanged();
			// Trace.Debug("###deletesize2="
			// + mAdapter.getAdapterList().size());
			// }
			// }
		}
	}

	private void showToast(int res) {
		if (mToast == null)
			mToast = Toast.makeText(getApplicationContext(), null,
							Toast.LENGTH_SHORT);
		mToast.setText(res);
		mToast.show();
	}

	public static void addLikePhoto(CommonFileInfo file, int listCode) {
		switch (listCode) {
		case YELLOW:
			yellowLike.add(file);
			break;
		case BLUE:
			blueLike.add(file);
			break;
		case RED:
			redLike.add(file);
			break;
		default:
			break;
		}
	}

	public static void deleteLikePhoto(CommonFileInfo file, int listCode) {
		List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
		switch (listCode) {
		case YELLOW:
			list = yellowLike;
			break;

		case BLUE:
			list = blueLike;
			break;

		case RED:
			list = redLike;
			break;
		default:
			break;
		}
		if (list.size() > 0) {

			int i = 0;
			for (i = 0; i < list.size(); i++) {
				if (list.get(i).getPath().equals(file.getPath())) {
					list.remove(i);
					i = list.size();

				}
			}
		}
	}

}
