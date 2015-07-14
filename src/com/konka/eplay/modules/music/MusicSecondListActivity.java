package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.R;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.modules.AlwaysMarqueeTextView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.LoadingDialog;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.music.AllSongListFragment.AllSongListAllAdapter;
import com.konka.eplay.modules.music.MusicActivity.MusicUsbReceiver;
import com.konka.eplay.modules.music.roundedview.RoundedImageView;
import com.konka.eplay.modules.music.thumnail.LocalImageLoader;
import com.konka.eplay.modules.music.thumnail.MusicThumnailLoader;
import com.konka.eplay.modules.photo.NameListAdapter;
import com.konka.eplay.modules.photo.PhotoActivity;

/**
 * @ClassName: MusicSecondListActivity
 * @Description: 从音乐文件夹进入的二级列表展示界面
 * @author xuyunyu
 * @date 2015年5月4日 上午9:49:57
 *
 */
public class MusicSecondListActivity extends Activity implements OnScrollListener {

	public static final String SECONDLIST_TITLE = "folder_title";
	public static final String SECONDLIST_SONG_COUNT = "song_count";
	public static final String SECONDLIST_FILE_LIST = "file_list";
	public static final String SECONDLIST_FROM_ALLSONG = "show_SingerOrAlbum";
	public static final String SECONDLIST_FROM_ALLSONG_SHOWSINGER = "Show_Singer";
	public static final String SECONDLIST_FROM_ALLSONG_SHOWALBUM = "Show_album";

	private MusicListView mListView;
	private RoundedImageView mImageView_top;
	private ImageView mImageView_top_asyn;
	private RelativeLayout mRoot;
	private AlwaysMarqueeTextView mFolderTitle;
	private String mCurPath;
	private TextView mSongCount;
	private static List<CommonFileInfo> sSonglist = null;
	private MusicSecondListAdapter mMusicSecondListAdapter;
	private MusicListOnkeyListenner mListOnkeyListenner;
	private MusicListOnClickListener mListOnClickListener;

	private static LikeButton mLikeButton;
	// 字母轴
	private ListView mNameLineListView;
	private NameListAdapter mNameListAdapter;
	private TextView iv;
	private ImageView hLine;
	private TextView moreName;

	private int mLongClickPosition = -1;

	private FrameLayout mTopButton;
	private RelativeLayout mTopButtonLyout;
	private ImageView mLikeImageView;
	private FrameLayout mLikeClickButton;
	private FrameLayout mDetailClickButton;
	private MusicPlayerService mPlayerService;
	private VisualizerView mVisualizerView;

	private RelativeLayout mBackButton;

	private AlwaysMarqueeTextView mPathtTextView;
	private TextView mPathtTitleTextView;
	private LoadingDialog mLoadingDialog;

	private Operation mOperation;
	private int mCouunt;
	private String mTitle="";
	private boolean mShowAsyn = false;
	private boolean mFromAlbum = false;
	private boolean mGetFolderMusic = false;
	private MusicSecondListUsbReceiver mUsbReceiver;

	static {
		sSonglist = new ArrayList<CommonFileInfo>();
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			MusicThumnailLoader.getInstance().loadImage(sSonglist.get(0).getPath(), mImageView_top_asyn);

			if (mLoadingDialog != null) {
				mLoadingDialog.dismiss();
			}
			mNameListAdapter = new NameListAdapter(getApplicationContext(), sSonglist);
			mNameLineListView.setAdapter(mNameListAdapter);

			// 这句必不可少
			mMusicSecondListAdapter.setlist(sSonglist);
			mMusicSecondListAdapter.notifyDataSetChanged();
			mListView.requestFocus();
		};
	};
	private Thread writeThread;

	public void onEventMainThread(IEvent event) {
		if (event instanceof EventMusicStateChange) {
			refreshAdapter();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.music_secondlist_layout);

		mOperation = Operation.getInstance();
		sSonglist = new ArrayList<CommonFileInfo>();
		parseIntent(getIntent());
		initViews();
		initData();
		mListView.requestFocus();
		if (mGetFolderMusic) {
			mLoadingDialog.show();
			getFolderMusics(mCurPath);
		}

		EventDispatchCenter.getInstance().register(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (null != mMusicSecondListAdapter && mLikeButton.getVisibility() == View.INVISIBLE) {
			mMusicSecondListAdapter.notifyDataSetChanged();
		}

		if (null != mListView.getSelectedView()) {
			if (mLikeButton.getVisibility() == View.INVISIBLE) {
				if (mListView.getLastVisiblePosition() == mListView.getSelectedItemPosition()
						&& MusicSecondListAdapter.TYPE_AUDIO == mListView.getAdapter().getItemViewType(
								mListView.getSelectedItemPosition())) {
					mListView.setSelectionFromTop(mListView.getSelectedItemPosition(), mListView.getHeight()
							- mListView.getSelectedView().getHeight() - mListView.getDividerHeight() * 2);
				}
			}
		}

		// 因为notifyDataSetChanged是异步的，目前无法监听它的完成时间点，所以只能以postDelayed的方法来实现假同步。延时时间初步设置为50ms。
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// mBorderView.setAvailable(true);
				// mBorderView.setLocation4MusicList(mListView.getSelectedView(),
				// mRoot);
				if (mLikeButton.getVisibility() == View.INVISIBLE) {
					mListView.requestFocus();
				}
				if (null != mListView.getSelectedView() && sSonglist.size() > 0) {
					mLikeButton.setLocation(mListView.getSelectedView(), mRoot, mListView.getSelectedView());
					if (sSonglist.get(mListView.getSelectedItemPosition()).getIsLike()) {
						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
					} else {
						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
					}
				}

			}
		}, 50);

	}

	/*
	 * @Title: parseIntent
	 *
	 * @Description: 解析intent
	 */
	private void parseIntent(Intent intent) {
		if (null == intent) {
			return;
		}

		if (intent.hasExtra(SECONDLIST_FILE_LIST)) {
			CommonFileInfo fileInfo = (CommonFileInfo) intent.getSerializableExtra(SECONDLIST_FILE_LIST);
			mTitle = fileInfo.getName();
			mCouunt = fileInfo.getChildrenMusicCount();
			mCurPath = fileInfo.getPath();
			mShowAsyn = true;
			mGetFolderMusic = true;

		} else if (intent.hasExtra(SECONDLIST_FROM_ALLSONG)) {
			// sSonglist.clear();
			if (intent.getStringExtra(SECONDLIST_FROM_ALLSONG).equals(SECONDLIST_FROM_ALLSONG_SHOWSINGER)) {
				mShowAsyn = false;
				sSonglist = MusicActivity.getAllMusicList();
				if (null != sSonglist && sSonglist.size() > 0) {
					mTitle = sSonglist.get(0).getSinger();
					mCouunt = sSonglist.size();
				}
			} else if (intent.getStringExtra(SECONDLIST_FROM_ALLSONG).equals(SECONDLIST_FROM_ALLSONG_SHOWALBUM)) {
				mShowAsyn = true;
				mFromAlbum = true;
				sSonglist = MusicActivity.getAllMusicList();
				if (null != sSonglist && sSonglist.size() > 0) {
					mTitle = sSonglist.get(0).getSpecial();
					mCouunt = sSonglist.size();
				}
			}
		}

	}

	private void getFolderMusics(final String path) {

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				sSonglist = mOperation.getSpecificFiles(path, MultimediaType.MMT_MUSIC);
				mOperation.sort(sSonglist, SortType.ST_BY_NAME);
				for (int i = 0; i < sSonglist.size(); i++) {
					for (int j = 0; j < MusicActivity.getLikeList().size(); j++) {
						if (MusicActivity.getLikeList().get(j).getPath().equals(sSonglist.get(i).getPath())) {
							sSonglist.get(i).setIsLike(true);
							break;
						}
					}
				}
				MusicUtils.getAllSongInfo(sSonglist, null);
				mHandler.sendEmptyMessage(0);
			}
		});
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	public static void hideLikeButton() {
		if (null != mLikeButton) {
			mLikeButton.setVisibility(View.INVISIBLE);
		}
	}

	public void refreshAdapter() {
		if (mMusicSecondListAdapter != null) {
			mMusicSecondListAdapter.notifyDataSetChanged();
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mListView == null) {
						return;
					}
					if (null != mListView.getSelectedView()) {
						mLikeButton.setLocation(mListView.getSelectedView(), mRoot, mListView.getSelectedView());
					}

				}
			}, 50);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		EventDispatchCenter.getInstance().unregister(this);
		mLikeButton = null;

		if (mUsbReceiver != null) {
			unregisterReceiver(mUsbReceiver);
			mUsbReceiver = null;
		}

	}

	/*
	 * @Description: 初始化相关数据
	 */
	private void initData() {

		mPlayerService = MusicPlayerService.getInstance();
		mMusicSecondListAdapter = new MusicSecondListAdapter(sSonglist);
		mListView.setAdapter(mMusicSecondListAdapter);

		if (mShowAsyn) {
			mImageView_top_asyn.setVisibility(View.VISIBLE);
			if (mFromAlbum) {
				MusicThumnailLoader.getInstance().loadImage(sSonglist.get(0).getPath(), mImageView_top_asyn);
			}

		} else {
			mImageView_top.setVisibility(View.VISIBLE);
			LocalImageLoader.getInstance()
					.loadImage(MusicSecondListActivity.this, mTitle.toLowerCase(), mImageView_top);
		}

		IntentFilter filter = new IntentFilter();
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addDataScheme("file");
		mUsbReceiver = new MusicSecondListUsbReceiver();
		registerReceiver(mUsbReceiver, filter);

	}

	/*
	 * @Description: 初始化界面
	 */
	private void initViews() {
		mListView = (MusicListView) findViewById(R.id.music_secondlist_listview);
		mImageView_top = (RoundedImageView) findViewById(R.id.music_secondlist_image_top);
		mImageView_top_asyn = (ImageView) findViewById(R.id.music_secondlist_image_top_asyn);
		mFolderTitle = (AlwaysMarqueeTextView) findViewById(R.id.music_secondlist_foldertitle);
		mSongCount = (TextView) findViewById(R.id.music_secondlist_songcount);
		mLikeButton = (LikeButton) findViewById(R.id.musiclist_second_like);
		mRoot = (RelativeLayout) findViewById(R.id.musiclist_second_root);
		mPathtTextView = (AlwaysMarqueeTextView) findViewById(R.id.musiclist_second_path);
		mPathtTitleTextView = (TextView) findViewById(R.id.musiclist_second_path_title);
		mTopButton = (FrameLayout) findViewById(R.id.allsong_second_top_btn);
		mTopButtonLyout = (RelativeLayout) findViewById(R.id.allsong_second_top_btn_layout);
		mLikeImageView = (ImageView) findViewById(R.id.musiclist_second_like_image);
		mLikeClickButton = (FrameLayout) findViewById(R.id.musiclist_second_like_clickButton);
		mDetailClickButton = (FrameLayout) findViewById(R.id.musiclist_second_detail_clickButton);
		mBackButton = (RelativeLayout) findViewById(R.id.music_secondlist_top);
		mBackButton.setNextFocusDownId(R.id.music_secondlist_listview);

		mLoadingDialog = new LoadingDialog(MusicSecondListActivity.this, R.style.progressDialog_holo);

		mNameLineListView = (ListView) findViewById(R.id.name_music_listview);
		moreName = (TextView) findViewById(R.id.morename_music);
		mNameListAdapter = new NameListAdapter(getApplicationContext(), sSonglist);
		mNameLineListView.setAdapter(mNameListAdapter);
		mNameLineListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Trace.Debug("###getlastposition=" + mNameLineListView.getLastVisiblePosition());
				if (mNameLineListView.getLastVisiblePosition() == mNameListAdapter.getNameList().size() - 1) {
					moreName.setVisibility(View.GONE);
				} else {
					moreName.setVisibility(View.VISIBLE);
				}
				if (iv != null)
					iv.setTextColor(getResources().getColor(R.color.text_nofocus));
				iv = (TextView) view.findViewById(R.id.txt_date_time);
				iv.setTextColor(getResources().getColor(R.color.white));
				if (hLine != null)
					hLine.setVisibility(View.GONE);
				hLine = (ImageView) view.findViewById(R.id.h_name_line);
				hLine.setVisibility(View.VISIBLE);
				if (mNameLineListView.hasFocus()) {
					hLine.setBackgroundResource(R.drawable.time_selected);
				} else {
					hLine.setBackgroundResource(R.color.transparent);
				}
				String firstLetterString = mNameListAdapter.getNameList().get(position);
				int count = sSonglist.size();
				for (int i = 0; i < count; i++) {
					if (sSonglist.get(i).getFirstLetter().equals(firstLetterString)) {
						Trace.Debug("###Equals");
						// mGridView.setFocusable(true);
						// mGridView.requestFocus();
						if (mNameLineListView.hasFocus()) {
							Trace.Debug("gridViewPOsition=" + mListView.getFirstVisiblePosition() + "i=" + i);
							// mListView.smoothScrollTo(
							// mListView.getFirstVisiblePosition(),
							// i);
							mListView.setSelection(i);
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
					if (null != hLine) {
						hLine.setBackground(getResources().getDrawable(R.drawable.time_selected));
					}

				} else {
					if (null != hLine) {
						hLine.setBackgroundResource(R.color.transparent);
					}

				}
			}
		});

		mListOnkeyListenner = new MusicListOnkeyListenner();
		mListOnClickListener = new MusicListOnClickListener();
		mLikeClickButton.setOnKeyListener(mListOnkeyListenner);
		mDetailClickButton.setOnKeyListener(mListOnkeyListenner);
		mListView.setOnKeyListener(mListOnkeyListenner);
		mTopButton.setOnKeyListener(mListOnkeyListenner);

		mLikeClickButton.setOnClickListener(mListOnClickListener);
		mDetailClickButton.setOnClickListener(mListOnClickListener);
		mTopButton.setOnClickListener(mListOnClickListener);
		mBackButton.setOnClickListener(mListOnClickListener);

		mListView.setOnScrollListener(this);
		mListView.setOverScrollMode(View.OVER_SCROLL_NEVER);

		mFolderTitle.setText(mTitle);
		if (mCouunt > 1) {
			mSongCount.setText(mCouunt + MusicUtils.getResourceString(this, R.string.music_file_count_more));
		} else {
			mSongCount.setText(mCouunt + MusicUtils.getResourceString(this, R.string.music_file_count));
		}

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, final View arg1, final int arg2, long arg3) {

				if (arg1.getTag() instanceof ViewHolderForAudio) {
					Intent intent_all = new Intent(Constant.PLAY_MUSIC_ACTION);
					intent_all.putExtra("isBackGround", true);
					startActivity(intent_all);
					return;
				}
				Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
				i.putExtra("MUSICPATH", "");
				i.putExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE,
						MusicPlayerActivity.SONGLIST_FROM_MUSICSECONDLISTACTIVITY);
				i.putExtra(Constant.PLAY_INDEX, arg2);
				startActivity(i);

			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				if (sSonglist.get(position).getIsLike()) {
					mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
				} else {
					mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
				}

				mLongClickPosition = position;
				mLikeButton.setLocation(view, mRoot, view);
				if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

					if (view.getTag() instanceof ViewHolderForAudio) {
						((ViewHolderForAudio) view.getTag()).islike.setVisibility(View.INVISIBLE);
					} else {
						((ViewHolder) view.getTag()).islike.setVisibility(View.INVISIBLE);
					}
					// mAdapter.notifyDataSetChanged();
					mLikeButton.getNextLocation(mLikeImageView);

					Animation scaleAnimation = AnimationUtils.loadAnimation(MusicSecondListActivity.this,
							R.anim.music_item_in);
					scaleAnimation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
							mLikeButton.setVisibility(View.VISIBLE);

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							mLikeClickButton.requestFocus();
							mLikeButton.setAnimationOutState(true);
						}
					});
					mLikeButton.startAnimation(scaleAnimation);
					mLikeButton.setAnimationOutState(false);
					return true;
				}
				return false;

			}
		});

		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

				mLikeButton.setVisibility(View.INVISIBLE);

				if (mLikeButton.getAnimation() != null && !mLikeButton.getAnimation().hasEnded()) {
					mLikeButton.setNextLocation(paramView, mRoot, mListView.getSelectedView(), sSonglist.get(paramInt)
							.getIsLike());
				} else {
					mLikeButton.setLocation(paramView, mRoot, mListView.getSelectedView());
					if (sSonglist.get(paramInt).getIsLike()) {
						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
					} else {
						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
					}
				}

				mPathtTextView.setText(sSonglist.get(paramInt).getPath());

				CommonFileInfo curFile = sSonglist.get(paramInt);
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

			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {

			}
		});

		mListView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mPathtTextView.setVisibility(View.VISIBLE);
					mPathtTitleTextView.setVisibility(View.VISIBLE);
				} else {
					mPathtTextView.setVisibility(View.INVISIBLE);
					mPathtTitleTextView.setVisibility(View.INVISIBLE);
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Configuration.ISMUSICDELETED) {

			if (sSonglist.size() == 0) {
				PhotoActivity.deleteFile(MusicActivity.getFileList(), mCurPath);
				MusicSecondListActivity.this.finish();
			} else {
				// mImageView_top_asyn.setPath(sSonglist.get(0).getPath(),
				// false);
				if (mShowAsyn) {
					mImageView_top_asyn.setVisibility(View.VISIBLE);
					MusicThumnailLoader.getInstance().loadImage(sSonglist.get(0).getPath(), mImageView_top_asyn);
				} else {
					mImageView_top.setVisibility(View.VISIBLE);
					LocalImageLoader.getInstance().loadImage(MusicSecondListActivity.this, mTitle.toLowerCase(),
							mImageView_top);
				}

				if (sSonglist.size() > 1) {
					mSongCount.setText(sSonglist.size()
							+ MusicUtils.getResourceString(this, R.string.music_file_count_more));
				} else {
					mSongCount
							.setText(sSonglist.size() + MusicUtils.getResourceString(this, R.string.music_file_count));
				}
				mLikeButton.setVisibility(View.INVISIBLE);
				mMusicSecondListAdapter.notifyDataSetChanged();
				mListView.requestFocus();
			}
			Configuration.ISMUSICDELETED = false;
		}
	}

	public static List<CommonFileInfo> getAllSecondMusicList() {
		return sSonglist;
	}

	public class MusicSecondListUsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Trace.Info("MusicSecongListActivity###UsbChanged");
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
				finish();
			}
		}
	}

	class MusicSecondListAdapter extends MusicBaseAdapter {

		public static final int TYPE_NORMAL = 0;
		public static final int TYPE_AUDIO = 1;
		private LayoutInflater mInflater;

		public MusicSecondListAdapter(List<CommonFileInfo> fileList) {
			super(fileList);
			mInflater = LayoutInflater.from(MusicSecondListActivity.this);
		}

		private void setViewHolder(View convertView, ViewHolder holder) {

			holder.title = (TextView) convertView.findViewById(R.id.allsong_songname);
			holder.singer = (TextView) convertView.findViewById(R.id.allsong_artist);
			holder.time = (TextView) convertView.findViewById(R.id.allsong_duration);
			holder.islike = (ImageView) convertView.findViewById(R.id.allsong_like_tag);
			holder.needInflate = false;

			convertView.setTag(holder);
		}

		private void setViewHolderAudio(View convertView, ViewHolderForAudio holder) {
			holder.title = (TextView) convertView.findViewById(R.id.allsong_songname_audio);
			holder.singer = (TextView) convertView.findViewById(R.id.allsong_artist_audio);
			holder.time = (TextView) convertView.findViewById(R.id.allsong_duration_audio);
			holder.state = (TextView) convertView.findViewById(R.id.allsong_playing_audio);
			holder.visualizerView = (VisualizerView) convertView.findViewById(R.id.allsong_audioview);
			holder.islike = (ImageView) convertView.findViewById(R.id.allsong_like_tag_audio);
			holder.needInflate = false;

			convertView.setTag(holder);

		}

		@Override
		public int getItemViewType(int position) {

			if (null == mPlayerService || null == mPlayerService.getMusciPath()) {
				return TYPE_NORMAL;
			}
			if (position >= getList().size()) {
				return TYPE_NORMAL;
			}
			if (getList().get(position).getPath().equals(mPlayerService.getMusciPath())) {
				return TYPE_AUDIO;
			} else {
				return TYPE_NORMAL;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			ViewHolderForAudio holderForAudio = null;
			int type = getItemViewType(position);
			List<CommonFileInfo> mFileList = getList();

			// if (parent instanceof MusicListView) {
			// if(((MusicListView) parent).isOnMeasure){
			// return convertView;
			// }
			// }
			if (convertView == null) {

				switch (type) {
				case TYPE_NORMAL:
					Trace.Info("null  normal");
					holder = new ViewHolder();

					convertView = mInflater.inflate(R.layout.music_alllist_item_all, null);
					setViewHolder(convertView, holder);
					break;

				case TYPE_AUDIO:
					Trace.Info("null");
					holderForAudio = new ViewHolderForAudio();
					convertView = mInflater.inflate(R.layout.music_alllist_all_audio_item, null);
					setViewHolderAudio(convertView, holderForAudio);
					break;

				default:
					break;
				}
			} else {
				switch (type) {
				case TYPE_NORMAL:
					if (convertView.getTag() instanceof ViewHolderForAudio
							|| ((ViewHolder) convertView.getTag()).needInflate) {
						holder = new ViewHolder();
						convertView = mInflater.inflate(R.layout.music_alllist_item_all, null);
						setViewHolder(convertView, holder);
					} else {
						holder = (ViewHolder) convertView.getTag();
					}

					break;

				case TYPE_AUDIO:

					if (convertView.getTag() instanceof ViewHolder
							|| ((ViewHolderForAudio) convertView.getTag()).needInflate) {
						holderForAudio = new ViewHolderForAudio();
						convertView = mInflater.inflate(R.layout.music_alllist_all_audio_item, null);
						setViewHolderAudio(convertView, holderForAudio);
					} else {
						holderForAudio = (ViewHolderForAudio) convertView.getTag();
					}

					break;

				default:
					break;
				}
			}

			if (parent instanceof MusicListView) {
				if (((MusicListView) parent).isOnMeasure) {
					return convertView;
				}
			}

			if (mFileList.size() == 0) {
				// or return null
				return convertView;
			}

			switch (type) {
			case TYPE_NORMAL:
				holder = (ViewHolder) convertView.getTag();
				String name = mFileList.get(position).getName();
				holder.title.setText(name.substring(0, name.length() - 4));
				holder.singer.setText(mFileList.get(position).getSinger());
				holder.time.setText(mFileList.get(position).getTime());

				if (mFileList.get(position).getIsLike()) {
					holder.islike.setVisibility(View.VISIBLE);
				} else {
					holder.islike.setVisibility(View.INVISIBLE);
				}
				break;

			case TYPE_AUDIO:
				holderForAudio = (ViewHolderForAudio) convertView.getTag();

				String name1 = mFileList.get(position).getName();
				holderForAudio.title.setText(name1.substring(0, name1.length() - 4));
				holderForAudio.singer.setText(mFileList.get(position).getSinger());
				holderForAudio.time.setText(mFileList.get(position).getTime());

				if (mFileList.get(position).getIsLike()) {
					holderForAudio.islike.setVisibility(View.VISIBLE);
				} else {
					holderForAudio.islike.setVisibility(View.INVISIBLE);
				}

				if (mPlayerService != null && mPlayerService.getmMediaPlayer() != null
						&& mPlayerService.getmMediaPlayer().isPlaying()) {
					holderForAudio.state.setText("正在播放");
				} else {
					holderForAudio.state.setText("播放暂停");
				}

				if (parent instanceof MusicListView) {
					if (((MusicListView) parent).isOnMeasure) {

					} else {
						Trace.Info("null set");
						mPlayerService.setVisualizerView(holderForAudio.visualizerView);
						mVisualizerView = holderForAudio.visualizerView;
					}
				}
				break;

			default:
				break;
			}

			return convertView;
		}

	}

	public final class ViewHolder {

		public TextView title;
		public TextView singer;
		public TextView time;
		public TextView state;
		public View view;
		public ImageView islike;
		public boolean needInflate;
	}

	public final class ViewHolderForAudio {
		public TextView title;
		public TextView singer;
		public TextView time;
		public TextView state;
		public VisualizerView visualizerView;
		public ImageView islike;
		public boolean needInflate;
	}

	/*
	 * @Description: 向上滑动列表
	 */
	private void scrollListViewUp() {
		int[] location = new int[2];
		int[] location_root = new int[2];
		int[] location_listview = new int[2];
		mListView.getLocationInWindow(location_root);
		mListView.getLocationInWindow(location_listview);
		mListView.getSelectedView().getLocationInWindow(location);
		int list_y = location_listview[1] - location_root[1];
		int button_y = location[1] - location_root[1];
		int distance = mListView.getHeight() - mListView.getSelectedView().getHeight() - mListView.getDividerHeight()
				- (button_y - list_y);
		int temp = button_y - list_y - mListView.getDividerHeight();

		mListView.smoothScrollBy(-distance - temp, 500);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
				mLikeButton.setLocation(mListView.getSelectedView(), mRoot, mListView.getSelectedView());
			}
		}, 500);
	}

	/*
	 * @Description: 向下滑动列表
	 */
	private void scrollListViewDown() {
		int[] location = new int[2];
		int[] location_root = new int[2];
		int[] location_listview = new int[2];
		mRoot.getLocationInWindow(location_root);
		mListView.getLocationInWindow(location_listview);
		mListView.getSelectedView().getLocationInWindow(location);
		int list_y = location_listview[1] - location_root[1];
		int button_y = location[1] - location_root[1];
		int temp;
		if (AllSongListAllAdapter.TYPE_NORMAL == mListView.getAdapter().getItemViewType(
				mListView.getSelectedItemPosition() + 1)) {
			if (AllSongListAllAdapter.TYPE_NORMAL == mListView.getAdapter().getItemViewType(
					mListView.getSelectedItemPosition())) {
				temp = (mListView.getSelectedView().getHeight() + mListView.getDividerHeight())
						- (mListView.getHeight() - (button_y - list_y + mListView.getSelectedView().getHeight()));

			} else {
				temp = (mListView.getSelectedView().getHeight() * 2 + mListView.getDividerHeight())
						- (mListView.getHeight() - (button_y - list_y + mListView.getSelectedView().getHeight()));
			}
		} else {
			if (AllSongListAllAdapter.TYPE_NORMAL == mListView.getAdapter().getItemViewType(
					mListView.getSelectedItemPosition())) {
				temp = (mListView.getSelectedView().getHeight() * 2 + mListView.getDividerHeight())
						- (mListView.getHeight() - (button_y - list_y + mListView.getSelectedView().getHeight()));
			} else {
				temp = (mListView.getSelectedView().getHeight() * 2 + mListView.getDividerHeight())
						- (mListView.getHeight() - (button_y - list_y + mListView.getSelectedView().getHeight()));
			}
		}
		mListView.smoothScrollBy(
				button_y - list_y + mListView.getSelectedView().getHeight() - temp - mListView.getDividerHeight(), 500);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
				mLikeButton.setLocation(mListView.getSelectedView(), mRoot, mListView.getSelectedView());
			}
		}, 500);
	}

	class MusicListOnkeyListenner implements OnKeyListener {

		@Override
		public boolean onKey(View paramView, int keycode, KeyEvent paramKeyEvent) {
			if (paramKeyEvent.getAction() == KeyEvent.ACTION_DOWN) {
				if (paramView.getId() == R.id.musiclist_second_like_clickButton
						|| paramView.getId() == R.id.musiclist_second_detail_clickButton) {

					if ((keycode == KeyEvent.KEYCODE_DPAD_LEFT && paramView.getId() == R.id.musiclist_second_like_clickButton)
							|| (keycode == KeyEvent.KEYCODE_DPAD_RIGHT && paramView.getId() == R.id.musiclist_second_detail_clickButton)
							|| keycode == KeyEvent.KEYCODE_BACK || keycode == KeyEvent.KEYCODE_MENU) {
						mListView.requestFocus();
						mLikeButton.setVisibility(View.INVISIBLE);
						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							Animation scaleAnimation = AnimationUtils.loadAnimation(MusicSecondListActivity.this,
									R.anim.music_item_out);
							scaleAnimation.setAnimationListener(new AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {
									mLikeButton.setVisibility(View.VISIBLE);

								}

								@Override
								public void onAnimationRepeat(Animation animation) {

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									mLikeButton.setVisibility(View.INVISIBLE);
									mMusicSecondListAdapter.notifyDataSetChanged();
								}
							});
							mLikeButton.startAnimation(scaleAnimation);

						}
						return true;
					} else if (keycode == KeyEvent.KEYCODE_DPAD_UP) {

						mListView.requestFocus();

						int[] location = new int[2];
						int[] location_root = new int[2];
						int[] location_listview = new int[2];
						mRoot.getLocationInWindow(location_root);
						mListView.getLocationInWindow(location_listview);
						mListView.getSelectedView().getLocationInWindow(location);

						int list_y = location_listview[1] - location_root[1];
						int button_y = location[1] - location_root[1];
						int y = (int) (button_y - list_y - mListView.getSelectedView().getHeight() * 1 - mListView
								.getDividerHeight());

						if (y < 0) {
							mListView.setSelectionFromTop(mListView.getSelectedItemPosition() - 1, 0);
						} else if (y < mListView.getHeight()) {
							if (AllSongListAllAdapter.TYPE_NORMAL == mListView.getAdapter().getItemViewType(
									mListView.getSelectedItemPosition() - 1)) {
								if (AllSongListAllAdapter.TYPE_NORMAL == mListView.getAdapter().getItemViewType(
										mListView.getSelectedItemPosition())) {
									int z = (int) (button_y - list_y - mListView.getSelectedView().getHeight() - mListView
											.getDividerHeight() * 2);
									mListView.setSelectionFromTop(mListView.getSelectedItemPosition() - 1, z);
								} else {
									int z = (int) (button_y - list_y - mListView.getSelectedView().getHeight() / 2 - mListView
											.getDividerHeight());
									mListView.setSelectionFromTop(mListView.getSelectedItemPosition() - 1, z);
								}

							} else {
								int z = (int) (button_y - list_y - mListView.getSelectedView().getHeight() * 2 - mListView
										.getDividerHeight());
								mListView.setSelectionFromTop(mListView.getSelectedItemPosition() - 1, z);
							}

						}

						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							Animation scaleAnimation = AnimationUtils.loadAnimation(MusicSecondListActivity.this,
									R.anim.music_item_out);
							scaleAnimation.setAnimationListener(new AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {
									mLikeButton.setVisibility(View.VISIBLE);

								}

								@Override
								public void onAnimationRepeat(Animation animation) {

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									mLikeButton.setVisibility(View.INVISIBLE);
									mMusicSecondListAdapter.notifyDataSetChanged();
								}
							});
							mLikeButton.startAnimation(scaleAnimation);
						}
						return true;

					} else if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {

						mListView.requestFocus();
						int[] location = new int[2];
						int[] location_root = new int[2];
						int[] location_listview = new int[2];
						mRoot.getLocationInWindow(location_root);
						mListView.getLocationInWindow(location_listview);
						mListView.getSelectedView().getLocationInWindow(location);

						int list_y = location_listview[1] - location_root[1];
						int button_y = location[1] - location_root[1];
						int y = (int) (button_y - list_y + mListView.getSelectedView().getHeight() * 2 + mListView
								.getDividerHeight());

						if (y < mListView.getHeight()) {
							int z = (int) (button_y - list_y + mListView.getSelectedView().getHeight());
							mListView.setSelectionFromTop(mListView.getSelectedItemPosition() + 1, z);
						} else {
							if (AllSongListAllAdapter.TYPE_NORMAL == mListView.getAdapter().getItemViewType(
									mListView.getSelectedItemPosition() + 1)) {
								if (AllSongListAllAdapter.TYPE_NORMAL == mListView.getAdapter().getItemViewType(
										mListView.getSelectedItemPosition())) {
									mListView.setSelectionFromTop(
											mListView.getSelectedItemPosition() + 1,
											mListView.getHeight() - mListView.getSelectedView().getHeight()
													- mListView.getDividerHeight() * 2);
								} else {
									mListView.setSelectionFromTop(mListView.getSelectedItemPosition() + 1,
											mListView.getHeight() - mListView.getSelectedView().getHeight() / 2);
								}

							} else if (AllSongListAllAdapter.TYPE_AUDIO == mListView.getAdapter().getItemViewType(
									mListView.getSelectedItemPosition() + 1)) {
								mListView.setSelectionFromTop(mListView.getSelectedItemPosition() + 1,
										mListView.getHeight() - mListView.getSelectedView().getHeight() * 2);
							}

						}

						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							Animation scaleAnimation = AnimationUtils.loadAnimation(MusicSecondListActivity.this,
									R.anim.music_item_out);
							scaleAnimation.setAnimationListener(new AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {
									mLikeButton.setVisibility(View.VISIBLE);

								}

								@Override
								public void onAnimationRepeat(Animation animation) {

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									mLikeButton.setVisibility(View.INVISIBLE);
									mMusicSecondListAdapter.notifyDataSetChanged();
								}
							});
							mLikeButton.startAnimation(scaleAnimation);
						}
						return true;

					}
				} else if (paramView.getId() == R.id.music_secondlist_listview) {

					if (mLikeButton!=null&&!mLikeButton.isAnimationOut()) {
						if (mLikeButton.getAnimation() != null && !mLikeButton.getAnimation().hasEnded()) {
							return true;
						}
					}

					if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
						if (!isReadyToGo) {
							return false;
						} else if (mListView.getSelectedItemPosition() <= mListView.getFirstVisiblePosition() + 1) {
							if (mListView.getSelectedItemPosition() <= 1) {
								return false;
							}
							scrollListViewUp();
							return false;
						}
					} else if (keycode == KeyEvent.KEYCODE_MENU) {

						if (mListView.getSelectedView() == null) {
							return false;
						}
						if (mListView.getSelectedView().getTag() instanceof ViewHolderForAudio) {
							((ViewHolderForAudio) mListView.getSelectedView().getTag()).islike
									.setVisibility(View.INVISIBLE);
						} else {
							((ViewHolder) mListView.getSelectedView().getTag()).islike.setVisibility(View.INVISIBLE);
						}

						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							mLikeButton.getNextLocation(mLikeImageView);

							Animation scaleAnimation = AnimationUtils.loadAnimation(MusicSecondListActivity.this,
									R.anim.music_item_in);
							scaleAnimation.setAnimationListener(new AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {
									mLikeButton.setVisibility(View.VISIBLE);

								}

								@Override
								public void onAnimationRepeat(Animation animation) {

								}

								@Override
								public void onAnimationEnd(Animation animation) {
									mLikeClickButton.requestFocus();
									mLikeButton.setAnimationOutState(true);
								}
							});
							mLikeButton.startAnimation(scaleAnimation);
							mLikeButton.setAnimationOutState(false);

						}

					} else if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (!isReadyToGo) {
							return false;
						}
						if (mListView.getSelectedItemPosition() >= mListView.getLastVisiblePosition() - 1) {
							if (mListView.getSelectedItemPosition() >= mListView.getAdapter().getCount() - 2) {
								return false;
							}
							scrollListViewDown();
							return false;
						}
					}
				} else if (paramView.getId() == R.id.allsong_second_top_btn) {
					if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
						mListView.requestFocus();
						return true;
					} else if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
					}
				}
			}

			return false;
		}

	}

	class MusicListOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.music_secondlist_top:
				finish();
				break;
			case R.id.allsong_second_top_btn:
				mListView.smoothScrollBy(-mListView.getLastVisiblePosition() * 98, 5000);

				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {

						if (!mListView.isStackFromBottom()) {
							mListView.setStackFromBottom(true);
						}
						mListView.setStackFromBottom(false);
						mListView.requestFocus();
					}
				}, 500);
				break;
			case R.id.musiclist_second_like_clickButton:

				CommonFileInfo curCommonFileInfo;
				if (mListView.getSelectedItemPosition() < 0) {
					curCommonFileInfo = sSonglist.get(mLongClickPosition);
				} else {
					curCommonFileInfo = sSonglist.get(mListView.getSelectedItemPosition());
				}

				if (!curCommonFileInfo.getIsLike()) {
					mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
					curCommonFileInfo.setIsLike(true);
					MusicActivity.getLikeList().add(curCommonFileInfo);
					Trace.Debug("##likelistSize=" + MusicActivity.getLikeList().size());

				} else {
					MusicActivity.getLikeList().remove(curCommonFileInfo);
					Trace.Debug("##deleteLikeList" + MusicActivity.getLikeList().size());
					mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
					curCommonFileInfo.setIsLike(false);
				}

				if (writeThread != null && writeThread.isAlive() /* isScanning */) {
					Trace.Warning("###mScanThread is still alive. it doesn't perform this scannable action.");
					// nothing to do.
					Configuration.isStopWrite=true;
					
				}
				//实时写入数据库缓存
				writeThread=new Thread(new Runnable() {
					public void run() {
						Configuration.isStopWrite=false;
						ContentManager.writeData2DB(getApplicationContext(), MusicActivity.getLikeList(), MultimediaType.MMT_LIKEMUSIC);
					}
				});
				writeThread.start();

				break;
			case R.id.musiclist_second_detail_clickButton:
				Intent i = new Intent(Constant.MUSIC_INFO);
				i.putExtra("PLAY_PATHS", "");
				i.putExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE,
						MusicPlayerActivity.SONGLIST_FROM_MUSICSECONDLISTACTIVITY);

				if (mListView.getSelectedItemPosition() < 0) {
					i.putExtra(Constant.PLAY_INDEX, mLongClickPosition);
				} else {
					i.putExtra(Constant.PLAY_INDEX, mListView.getSelectedItemPosition());
				}
				startActivity(i);
				break;

			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mTopButtonLyout != null) {
			if (firstVisibleItem == 0)
				mTopButtonLyout.setVisibility(View.INVISIBLE);
			else
				mTopButtonLyout.setVisibility(View.VISIBLE);
		}

	}

	public boolean isReadyToGo = true;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
			isReadyToGo = true;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:// 滚动状态
			isReadyToGo = false;
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动
			isReadyToGo = false;
			break;
		}

	}

}
