package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.GlobalData;
import com.konka.eplay.R;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.music.roundedview.RoundedImageView;
import com.konka.eplay.modules.music.thumnail.LocalImageLoader;
import com.konka.eplay.modules.music.thumnail.MusicThumnailLoader;

/**
 * @ClassName: AllSongListFragment
 * @Description: 音乐浏览界面的列表模式下的主界面
 * @author xuyunyu
 * @date 2015年4月30日 下午3:50:23
 */
public class AllSongListFragment extends Fragment implements OnScrollListener {

	// 传给音乐播放器activity的key
	public static final String MUSICPLAYER_MUSICPATH = "MUSICPATH";
	// 显示音乐波形的list item的高度
	private static final int AUDIO_ITEM_HEIGHT = 160;
	public static final int SELECTE_FOUCUS_TAB = 6;
	public static final int REFRESH_SONGLIST_FOR_OTHERS = 7;

	private static final int MUSIC_TAB_ALL = 100;
	private static final int MUSIC_TAB_SINGER = 101;
	private static final int MUSIC_TAB_ALBUM = 102;
	private static final int MUSIC_TAB_FAVORITE = 103;
	private final int ANIMATION_DURATION = 300;
	// 默认光标在歌曲列表
	private int mCurrentTab = MUSIC_TAB_ALL;

	// 界面元素
	private View root;
	private Button mAllSongTab;
	private TextView mAllSongTabText;
	private Button mSingerTab;
	private TextView mSingerTabText;
	private Button mAlbumTab;
	private TextView mAlbumTabText;
	private Button mFavoriteTab;
	private TextView mFavoriteTabText;
	private RelativeLayout mRootLayout;
	private MusicListView mSongListView;
	private static LikeButton mLikeButton;
	private RelativeLayout mTopButtonLyout;
	private FrameLayout mTopButton;
	private ImageView mLikeImageView;
	private FrameLayout mLikeClickButton;
	private FrameLayout mDetailClickButton;
	private TextView mMenuTip;
	private VisualizerView mVisualizerView;
	private MusicActivity mActivity;

	// 监听器
	private AllSongListFocusChangeListener mFocusChangeListener;
	private OnAllSongListListener mAllSongListListener;
	private AllSongListOnkeyListener mAllSongListOnkeyListener;
	private AllSongListClickListener mAllSongListClickListener;
	// listview的适配器
	private AllSongListAllAdapter mAdapter;
	private AllSongListAllAdapter mAdapterforCollect;
	private AllSongListSingerAdapter mAdapterForSinger;
	private AllSongListAlbumAdapter mAdapterForAlbum;
	// 后台音乐播放服务
	private MusicPlayerService mPlayerService;

	// 记录用户长按的list item的位置
	private int mLongClickPosition = -1;
	// 记忆住用户是从哪个tab往下移动到listview的，实现从哪来回哪去的效果，在此保存一份id
	private int mTabId = -1;
	// 记忆住用户是从哪个tab往上移动到顶部两个选项“所有歌曲”和“文件夹”的，实现从哪来回哪去的效果，在此保存一份id
	private int mTabIdFromTop = -1;

	// 所有歌曲tab的数据
	private List<CommonFileInfo> mSonglist = null;
	// 歌手tab的数据
	private List<CommonFileInfo> mSonglistBySinger = null;
	// 专辑tab的数据
	private List<CommonFileInfo> mSonglistByAlbum = null;
	// “我喜欢”tab的数据
	private List<CommonFileInfo> mSonglistByCollect = null;
	// 每个歌手对应的歌曲信息
	private Map<String, List<CommonFileInfo>> mSingerCollections;
	// 每张专辑对应的歌曲信息
	private Map<String, List<CommonFileInfo>> mAlbumCollections;
	private Thread writeThread;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case MusicActivity.REFRESH_DATA:
				mSonglist = MusicActivity.sAllList;
				mSonglistBySinger = MusicActivity.mSonglistBySinger;
				mSonglistByAlbum = MusicActivity.mSonglistByAlbum;
				mSonglistByCollect = MusicActivity.mSonglistByCollect;
				mSingerCollections = MusicActivity.mSingerCollections;
				mAlbumCollections = MusicActivity.mAlbumCollections;
				if (mSonglist.size() == 0 && isVisible()) {
					if (mLikeButton != null) {
						mLikeButton.setVisibility(View.INVISIBLE);
					}
					if (!mActivity.onRequestFocus(false)) {
						switch (mCurrentTab) {
						case MUSIC_TAB_ALL:
							mAllSongTab.requestFocus();
							break;
						case MUSIC_TAB_SINGER:
							mSingerTab.requestFocus();
							break;
						case MUSIC_TAB_ALBUM:
							mAlbumTab.requestFocus();
							break;
						}
					}
				}

					if (null != mAdapterForAlbum) {
						mAdapterForAlbum.notifyDataSetChanged();
					}
					if (null != mAdapterForSinger) {
						mAdapterForSinger.notifyDataSetChanged();
					}
					if (null != mAdapterforCollect) {
						mAdapterforCollect.notifyDataSetChanged();
					}


				if (mSonglistByCollect.size() == 0 && isVisible()) {
					if (mLikeButton != null) {
						mLikeButton.setVisibility(View.INVISIBLE);
					}
					if (mCurrentTab == MUSIC_TAB_FAVORITE) {
						if (!mActivity.onRequestFocus(false)) {
							mFavoriteTab.requestFocus();
						}
					}
					if (null != mAdapterforCollect) {
						mAdapterforCollect.notifyDataSetChanged();
					}
				}

				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}

				break;
			case SELECTE_FOUCUS_TAB:
				if (-1 != mTabIdFromTop) {
					root.findViewById(mTabIdFromTop).requestFocus();
				} else {
					root.findViewById(R.id.musiclist_all).requestFocus();
				}
				break;
			case REFRESH_SONGLIST_FOR_OTHERS:
				mAdapter.notifyDataSetChanged();
				if (null != mAdapterForAlbum) {
					mAdapterForAlbum.notifyDataSetChanged();
				}
				if (null != mAdapterForSinger) {
					mAdapterForSinger.notifyDataSetChanged();
				}
				if (null != mAdapterforCollect) {
					mAdapterforCollect.notifyDataSetChanged();
				}
				break;

			default:
				break;
			}
		};

	};

	interface OnAllSongListListener {

		public void setSongList(List<CommonFileInfo> list);

		public void onRefreshData(boolean needScan);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = (MusicActivity) activity;
		mActivity.setmHandlerFromAllSongListFragment(mHandler);

		try {
			mAllSongListListener = (OnAllSongListListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnAllSongListListener");
		}
	}

	public void onEventMainThread(IEvent event) {
		if (event instanceof EventMusicStateChange) {
			refreshAdapter();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initData();
		// 获取音乐列表，如果为空则启动全盘扫描
		mSonglist = MusicActivity.sAllList;
		mSonglistBySinger = MusicActivity.mSonglistBySinger;
		mSonglistByAlbum = MusicActivity.mSonglistByAlbum;
		mSonglistByCollect = MusicActivity.mSonglistByCollect;
		mSingerCollections = MusicActivity.mSingerCollections;
		mAlbumCollections = MusicActivity.mAlbumCollections;

		if (mSonglist.size() > 0) {
			if (mSonglistBySinger.size() <= 0||mSonglistByAlbum.size() <= 0) {
				mAllSongListListener.onRefreshData(false);
			}
		}else {
			mAllSongListListener.onRefreshData(true);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (((GlobalData) getActivity().getApplication()).isSortTypeChange) {
					// 如果用户在首页对排序做了更改，则对音乐文件夹数据重新排序

					((GlobalData) getActivity().getApplication()).isSortTypeChange = false;
					Operation.getInstance().sort(((MusicActivity) getActivity()).getMusicFolderList(),
							Configuration.sortType);

					//TODO 得实时刷新
				}

			}
		}).start();

		// 注册接收event事件
		EventDispatchCenter.getInstance().register(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		root = inflater.inflate(R.layout.allsonglist_layout, container, false);
		initViews();

		return root;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mPlayerService == null) {
			mPlayerService = MusicPlayerService.getInstance();
		}

		// 固定刷新，当从二级列表或者音乐播放器菜单返回时，需要对数据进行更新，当我喜欢菜单被隐藏的时候才刷新列表。
		if (mLikeButton.getVisibility() == View.INVISIBLE) {
			switch (mCurrentTab) {
			case MUSIC_TAB_ALL:
				mAdapter.notifyDataSetChanged();
				break;
			case MUSIC_TAB_SINGER:
				mAdapterForSinger.notifyDataSetChanged();
				break;
			case MUSIC_TAB_ALBUM:
				mAdapterForAlbum.notifyDataSetChanged();
				break;
			case MUSIC_TAB_FAVORITE:
				mAdapterforCollect.notifyDataSetChanged();
				break;
			}
		}

		if (this.isVisible()) {
			if (null != mSongListView.getSelectedView()) {
				if (mLikeButton.getVisibility() == View.INVISIBLE) {
					if (mSongListView.getLastVisiblePosition() == mSongListView.getSelectedItemPosition()
							&& AllSongListAllAdapter.TYPE_AUDIO == mSongListView.getAdapter().getItemViewType(
									mSongListView.getSelectedItemPosition())) {
						mSongListView.setSelectionFromTop(
								mSongListView.getSelectedItemPosition(),
								mSongListView.getHeight() - mSongListView.getSelectedView().getHeight()
										- mSongListView.getDividerHeight() * 2);
						if (mSongListView.getSelectedView().getHeight() < AUDIO_ITEM_HEIGHT) {

							mHandler.postDelayed(new Runnable() {

								@Override
								public void run() {
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition(),
											mSongListView.getHeight() - mSongListView.getSelectedView().getHeight()
													- mSongListView.getDividerHeight() * 2);

								}
							}, 15);
							relocateBorderView(30);
							return;
						}
					}
				}
			}
			relocateBorderView(20);
		}

	}

	/*
	 * @Description: 重新定位焦点框
	 */
	private void relocateBorderView(int delayTime) {
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (null != mSongListView.getSelectedView()) {
					if (mLikeButton.getVisibility() == View.INVISIBLE) {
						mSongListView.requestFocus();
					}
					mLikeButton.setLocation(mSongListView.getSelectedView(), mRootLayout,
							mSongListView.getSelectedView());
					if (mCurrentTab == MUSIC_TAB_ALL) {
						if (mSonglist.get(mSongListView.getSelectedItemPosition()).getIsLike()) {
							mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
						} else {
							mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
						}
					}
				}

			}
		}, delayTime);
	}

	/*
	 * @Title: initData
	 *
	 * @Description: 初始化数据
	 */
	private void initData() {
		mSonglistByAlbum = new ArrayList<CommonFileInfo>();
		mSonglistBySinger = new ArrayList<CommonFileInfo>();

		mSingerCollections = new HashMap<String, List<CommonFileInfo>>();
		mAlbumCollections = new HashMap<String, List<CommonFileInfo>>();
		mPlayerService = MusicPlayerService.getInstance();
		if (mPlayerService == null) {
			Trace.Fatal("play service null");
		}
	}

	public static void hideLikeButton() {
		if (null != mLikeButton) {
			mLikeButton.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//mSonglist = null;
		//mLikeButton = null;
		// 不再接收event事件
		EventDispatchCenter.getInstance().unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		Trace.Info("mCurrentTab=" + mCurrentTab + Configuration.ISMUSICDELETED);
		if (Configuration.ISMUSICDELETED) {
			int position = mSongListView.getSelectedItemPosition();

			if (mCurrentTab == MUSIC_TAB_ALBUM) {
				if (mAlbumCollections.get(mSonglistByAlbum.get(position).getSpecial()).size() > 0) {

					mSonglistByAlbum.get(position).setChildrenCount(
							mAlbumCollections.get(mSonglistByAlbum.get(position).getSpecial()).size());
				} else {
					mSonglistByAlbum.remove(position);
				}
				mAdapterForAlbum.notifyDataSetChanged();
			} else if (mCurrentTab == MUSIC_TAB_SINGER) {
				if (mSingerCollections.get(mSonglistBySinger.get(position).getSinger()).size() > 0) {

					mSonglistBySinger.get(position).setChildrenCount(
							mSingerCollections.get(mSonglistBySinger.get(position).getSinger()).size());
				} else {
					mSonglistBySinger.remove(position);
				}
				mAdapterForSinger.notifyDataSetChanged();
			} else if (mCurrentTab == MUSIC_TAB_FAVORITE) {
				if (mSonglistByCollect.size() == 0) {
					mFavoriteTab.requestFocus();
					mLikeButton.setVisibility(View.INVISIBLE);
				} else {
					mAdapterforCollect.setlist(mSonglistByCollect);
					mAdapterforCollect.notifyDataSetChanged();
				}
				mAdapterforCollect.notifyDataSetChanged();
			} else {
				mAdapter.notifyDataSetChanged();
			}
			mLikeButton.setVisibility(View.INVISIBLE);

			mSongListView.requestFocus();
			Configuration.ISMUSICDELETED = false;
			mHandler.sendEmptyMessage(REFRESH_SONGLIST_FOR_OTHERS);
		}

	}

//	public static List<CommonFileInfo> getmSongList() {
//		return mSonglist;
//	}

	/*
	 * @Description: 刷新like的列表
	 */
	public void refreshLikeList() {
		for (int i = 0; i < mSonglist.size(); i++) {
			mSonglist.get(i).setIsLike(false);
			for (int j = 0; j < mSonglistByCollect.size(); j++) {
				if (mSonglistByCollect.get(j).getPath().equals(mSonglist.get(i).getPath())) {
					Trace.Debug("##setIsLike=" + true);
					mSonglist.get(i).setIsLike(true);
					break;
				}
			}
		}
		mHandler.sendEmptyMessage(REFRESH_SONGLIST_FOR_OTHERS);
	}

	private void initViews() {
		mAllSongTab = (Button) root.findViewById(R.id.musiclist_all);
		mSingerTab = (Button) root.findViewById(R.id.musiclist_sort_singer);
		mAlbumTab = (Button) root.findViewById(R.id.musiclist_sort_album);
		mFavoriteTab = (Button) root.findViewById(R.id.musiclist_favorite);
		mAllSongTabText = (TextView) root.findViewById(R.id.musiclist_all_text);
		mSingerTabText = (TextView) root.findViewById(R.id.musiclist_sort_singer_text);
		mAlbumTabText = (TextView) root.findViewById(R.id.musiclist_sort_album_text);
		mFavoriteTabText = (TextView) root.findViewById(R.id.musiclist_favorite_text);
		mMenuTip = (TextView) root.findViewById(R.id.musiclist_mune_tip);

		mRootLayout = (RelativeLayout) root.findViewById(R.id.allsong_tab_root);
		mSongListView = (MusicListView) root.findViewById(R.id.allsong_listview);
		mLikeButton = (LikeButton) root.findViewById(R.id.allsong_like);
		mTopButton = (FrameLayout) root.findViewById(R.id.allsong_top_btn);
		mTopButtonLyout = (RelativeLayout) root.findViewById(R.id.allsong_top_btn_layout);
		mLikeImageView = (ImageView) root.findViewById(R.id.allsong_like_image);
		mLikeClickButton = (FrameLayout) root.findViewById(R.id.allsong_like_clickButton);
		mDetailClickButton = (FrameLayout) root.findViewById(R.id.allsong_detail_clickButton);
		mDetailClickButton.setNextFocusLeftId(R.id.allsong_like_clickButton);

		mAdapter = new AllSongListAllAdapter(mSonglist);
		mSongListView.setAdapter(mAdapter);
		mSongListView.setOnScrollListener(this);
		// try it
		mSongListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mSongListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

				if (null==mLikeButton) {
					return;
				}
				mLikeButton.setVisibility(View.INVISIBLE);
				if (mSonglist.size()==0) {
					return;
				}
				if (mCurrentTab == MUSIC_TAB_ALL) {

					if (mLikeButton.getAnimation() != null && !mLikeButton.getAnimation().hasEnded()) {
						mLikeButton.setNextLocation(paramView, mRootLayout, mSongListView.getSelectedView(), mSonglist
								.get(paramInt).getIsLike());
					} else {
						mLikeButton.setLocation(paramView, mRootLayout, mSongListView.getSelectedView());
						if (mSonglist.get(paramInt).getIsLike()) {
							mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
						} else {
							mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
						}
					}

				} else if (mCurrentTab == MUSIC_TAB_FAVORITE) {
					mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
					if (mLikeButton.getAnimation() != null && !mLikeButton.getAnimation().hasEnded()) {
						mLikeButton.setNextLocation(paramView, mRootLayout, mSongListView.getSelectedView(), true);
					} else {
						mLikeButton.setLocation(paramView, mRootLayout, mSongListView.getSelectedView());
					}
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {
				if (null==mLikeButton) {
					return;
				}
				mLikeButton.setVisibility(View.INVISIBLE);
			}
		});

		mSongListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				if (mCurrentTab == MUSIC_TAB_ALBUM || mCurrentTab == MUSIC_TAB_SINGER) {
					return false;
				}

				if (mCurrentTab == MUSIC_TAB_ALL) {
					if (mSonglist.get(position).getIsLike()) {
						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
					} else {
						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
					}
				} else if (mCurrentTab == MUSIC_TAB_FAVORITE) {
					mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
				}

				mLongClickPosition = position;
				mLikeButton.setLocation(view, mRootLayout, view);
				if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

					if (view.getTag() instanceof ViewHolderForAudio) {
						((ViewHolderForAudio) view.getTag()).islike.setVisibility(View.INVISIBLE);
					} else {
						((ViewHolder) view.getTag()).islike.setVisibility(View.INVISIBLE);
					}

					mLikeButton.getNextLocation(mLikeImageView);

					Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.music_item_in);
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

		mSongListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {

				switch (mCurrentTab) {
				case MUSIC_TAB_ALL:
					if (paramView.getTag() instanceof ViewHolderForAudio) {
						Intent intent_all = new Intent(Constant.PLAY_MUSIC_ACTION);
						intent_all.putExtra("isBackGround", true);
						startActivity(intent_all);
						break;
					}
					mAllSongListListener.setSongList(mSonglist);
					Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
					i.putExtra(MUSICPLAYER_MUSICPATH, "");
					i.putExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE,
							MusicPlayerActivity.SONGLIST_FROM_MUSICFILELISTACTIVITY);
					i.putExtra(Constant.PLAY_INDEX, paramInt);
					startActivity(i);
					break;
				case MUSIC_TAB_SINGER:
					Trace.Info("singer size "
							+ mSingerCollections.get(mSonglistBySinger.get(paramInt).getSinger()).size());
					mAllSongListListener.setSongList(mSingerCollections
							.get(mSonglistBySinger.get(paramInt).getSinger()));
					Intent intent = new Intent();
					intent.putExtra(MusicSecondListActivity.SECONDLIST_FROM_ALLSONG,
							MusicSecondListActivity.SECONDLIST_FROM_ALLSONG_SHOWSINGER);
					intent.setAction("com.konka.eplay.action.MUSIC_SECOND_LIST");
					startActivity(intent);
					break;
				case MUSIC_TAB_ALBUM:
					mAllSongListListener.setSongList(mAlbumCollections.get(mSonglistByAlbum.get(paramInt).getSpecial()));
					Intent intent_album = new Intent();
					intent_album.putExtra(MusicSecondListActivity.SECONDLIST_FROM_ALLSONG,
							MusicSecondListActivity.SECONDLIST_FROM_ALLSONG_SHOWALBUM);
					intent_album.setAction("com.konka.eplay.action.MUSIC_SECOND_LIST");
					startActivity(intent_album);
					break;
				case MUSIC_TAB_FAVORITE:
					if (paramView.getTag() instanceof ViewHolderForAudio) {
						Intent intent_fav = new Intent(Constant.PLAY_MUSIC_ACTION);
						intent_fav.putExtra("isBackGround", true);
						startActivity(intent_fav);
						break;
					}
					mAllSongListListener.setSongList(mSonglistByCollect);
					Intent intent_like = new Intent(Constant.PLAY_MUSIC_ACTION);
					intent_like.putExtra(MUSICPLAYER_MUSICPATH, "");
					intent_like.putExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE,
							MusicPlayerActivity.SONGLIST_FROM_MUSICFILELISTACTIVITY);
					intent_like.putExtra(Constant.PLAY_INDEX, paramInt);
					startActivity(intent_like);
					break;

				default:
					break;
				}

			}
		});

		mFocusChangeListener = new AllSongListFocusChangeListener();
		mAllSongListOnkeyListener = new AllSongListOnkeyListener();
		mAllSongListClickListener = new AllSongListClickListener();

		mDetailClickButton.setOnClickListener(mAllSongListClickListener);
		mLikeClickButton.setOnClickListener(mAllSongListClickListener);
		mTopButton.setOnClickListener(mAllSongListClickListener);
		mAllSongTab.setOnClickListener(mAllSongListClickListener);
		mSingerTab.setOnClickListener(mAllSongListClickListener);
		mAlbumTab.setOnClickListener(mAllSongListClickListener);
		mFavoriteTab.setOnClickListener(mAllSongListClickListener);

		mAllSongTab.setOnFocusChangeListener(mFocusChangeListener);
		mSingerTab.setOnFocusChangeListener(mFocusChangeListener);
		mAlbumTab.setOnFocusChangeListener(mFocusChangeListener);
		mFavoriteTab.setOnFocusChangeListener(mFocusChangeListener);

		mAllSongTab.setOnKeyListener(mAllSongListOnkeyListener);
		mSingerTab.setOnKeyListener(mAllSongListOnkeyListener);
		mAlbumTab.setOnKeyListener(mAllSongListOnkeyListener);
		mFavoriteTab.setOnKeyListener(mAllSongListOnkeyListener);
		mSongListView.setOnKeyListener(mAllSongListOnkeyListener);
		mLikeClickButton.setOnKeyListener(mAllSongListOnkeyListener);
		mDetailClickButton.setOnKeyListener(mAllSongListOnkeyListener);
		mTopButton.setOnKeyListener(mAllSongListOnkeyListener);
	}

	public void refreshAdapter() {

		BaseAdapter adapter = (BaseAdapter) mSongListView.getAdapter();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mSongListView == null) {
						return;
					}
					if (null != mSongListView.getSelectedView()) {
						mLikeButton.setLocation(mSongListView.getSelectedView(), mRootLayout,
								mSongListView.getSelectedView());
					}

				}
			}, 50);
		}
	}

	private void deleteCell(final View v, final int index) {

		final AnimationListener al = new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {

				// 测试用，后期再次做相应的数据库操作，删除此选项
				Trace.Info("mSonglistByCollect" + mSonglistByCollect.size());
				mSonglistByCollect.get(index).setIsLike(false);
				mSonglistByCollect.remove(index);
				if (writeThread != null && writeThread.isAlive() /* isScanning */) {
					Trace.Warning("###mScanThread is still alive. it doesn't perform this scannable action.");
					// nothing to do.
					Configuration.isStopWrite=true;
					
				}
				//实时写入数据库缓存
				writeThread=new Thread(new Runnable() {
					public void run() {
						Configuration.isStopWrite=false;
						ContentManager.writeData2DB(getActivity(), mSonglistByCollect, MultimediaType.MMT_LIKEMUSIC);
					}
				});
				writeThread.start();


				Trace.Info("msonglist" + mSonglist.size());
				if (v.getTag() instanceof ViewHolder) {
					ViewHolder vh = (ViewHolder) v.getTag();
					vh.needInflate = true;
				} else if (v.getTag() instanceof ViewHolderForAudio) {
					ViewHolderForAudio vh = (ViewHolderForAudio) v.getTag();
					vh.needInflate = true;
				}
				mAdapterforCollect.notifyDataSetChanged();

				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						Trace.Info("last " + mSonglistByCollect.size() + "  " + mSonglistByCollect.size());
						if ((index - 1) == mSonglistByCollect.size() - 1 && mSonglistByCollect.size() > 0) {
							if (mSongListView.getSelectedView() == null) {
								View selected_view = mSongListView.getChildAt(mSongListView.getLastVisiblePosition()
										- mSongListView.getFirstVisiblePosition());
								mSongListView.setSelectionFromTop(
										mSonglistByCollect.size() - 1,
										mSongListView.getHeight() - selected_view.getHeight()
												- mSongListView.getDividerHeight() * 2);
							} else {
								mSongListView.setSelectionFromTop(mSonglistByCollect.size() - 1,
										mSongListView.getHeight() - mSongListView.getSelectedView().getHeight()
												- mSongListView.getDividerHeight() * 2);
							}

						}

					}
				}, 50);
				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (mSonglistByCollect.size() <= 0) {
							return;
						}
						if (null != mSongListView.getSelectedView()) {
							mLikeButton.setLocation(mSongListView.getSelectedView(), mRootLayout,
									mSongListView.getSelectedView());
						} else {
							View selected_view = mSongListView.getChildAt(mSongListView.getLastVisiblePosition()
									- mSongListView.getFirstVisiblePosition());
							mLikeButton.setLocation(selected_view, mRootLayout, selected_view);
						}
					}
				}, 100);

				if (mSonglistByCollect.size() == 0) {
					// mFavoriteTab.setFocusable(true);
					mFavoriteTab.requestFocus();
					// mLikeButton.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {

			}
		};

		mSongListView.requestFocus();
		Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.music_item_out);
		scaleAnimation.setDuration(150);
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
				if (mCurrentTab == MUSIC_TAB_ALL) {
					mAdapter.notifyDataSetChanged();
				} else {
					mAdapterforCollect.notifyDataSetChanged();
				}
				collapse(v, al);
			}
		});
		mLikeButton.startAnimation(scaleAnimation);
	}

	private void collapse(final View v, AnimationListener al) {
		final int initialHeight = v.getMeasuredHeight();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 1) {
					v.setVisibility(View.GONE);
				} else {
					v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		if (al != null) {
			anim.setAnimationListener(al);
		}
		anim.setDuration(ANIMATION_DURATION);
		v.startAnimation(anim);
	}

	public void changeTabBackground(View v, boolean hasfocus) {

		switch (v.getId()) {
		case R.id.musiclist_all:
			if (hasfocus) {
				mAllSongTabText.setBackgroundResource(R.drawable.musiclist_btn_bg);
			} else {
				mAllSongTabText.setBackgroundResource(R.drawable.secondtab_selected);
			}

			break;
		case R.id.musiclist_sort_singer:
			if (hasfocus) {
				mSingerTabText.setBackgroundResource(R.drawable.musiclist_btn_bg);
			} else {
				mSingerTabText.setBackgroundResource(R.drawable.secondtab_selected);
			}
			break;
		case R.id.musiclist_sort_album:
			if (hasfocus) {
				mAlbumTabText.setBackgroundResource(R.drawable.musiclist_btn_bg);
			} else {
				mAlbumTabText.setBackgroundResource(R.drawable.secondtab_selected);
			}
			break;
		case R.id.musiclist_favorite:
			if (hasfocus) {
				mFavoriteTabText.setBackgroundResource(R.drawable.musiclist_btn_bg);
			} else {
				mFavoriteTabText.setBackgroundResource(R.drawable.secondtab_selected);
			}
			break;

		default:
			break;
		}
	}

	private void freshButtonFocus(View v) {

		mAllSongTab.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
		mSingerTab.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
		mAlbumTab.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
		mFavoriteTab.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
		((Button) v).setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_top_second_selected));
	}

	public MusicListView getListView() {
		return mSongListView;
	}

	class AllSongListClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.allsong_detail_clickButton:

				if (mCurrentTab == MUSIC_TAB_ALL) {
					mAllSongListListener.setSongList(mSonglist);
				} else if (mCurrentTab == MUSIC_TAB_FAVORITE) {
					mAllSongListListener.setSongList(mSonglistByCollect);
					Trace.Debug("###mSonglistyCollectSize" + mSonglistByCollect.size());
				}
				if (mCurrentTab == MUSIC_TAB_ALL || mCurrentTab == MUSIC_TAB_FAVORITE) {
					Intent i = new Intent(Constant.MUSIC_INFO);
					i.putExtra("PLAY_PATHS", "");
					i.putExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE,
							MusicPlayerActivity.SONGLIST_FROM_MUSICFILELISTACTIVITY);
					// 兼容鼠标长按操作
					if (mSongListView.getSelectedItemPosition() < 0) {
						i.putExtra(Constant.PLAY_INDEX, mLongClickPosition);
					} else {
						i.putExtra(Constant.PLAY_INDEX, mSongListView.getSelectedItemPosition());
					}
					startActivity(i);
				}

				break;
			case R.id.allsong_like_clickButton:

				CommonFileInfo curCommonFileInfo;
				// 兼容鼠标长按操作

				if (mCurrentTab == MUSIC_TAB_FAVORITE) {
					// curCommonFileInfo.setIsLike(false);

					if (mSongListView.getSelectedItemPosition() < 0) {
						deleteCell(
								mSongListView.getChildAt(mLongClickPosition - mSongListView.getFirstVisiblePosition()),
								mLongClickPosition);
					} else {
						deleteCell(mSongListView.getSelectedView(), mSongListView.getSelectedItemPosition());
					}

					mAdapterforCollect.notifyDataSetChanged();

				} else {

					if (mSongListView.getSelectedItemPosition() < 0) {
						curCommonFileInfo = mSonglist.get(mLongClickPosition);
					} else {
						curCommonFileInfo = mSonglist.get(mSongListView.getSelectedItemPosition());
					}

					if (!curCommonFileInfo.getIsLike()) {
						mSonglistByCollect.add(curCommonFileInfo);
						curCommonFileInfo.setIsLike(true);

						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_yes);
					} else {
						mSonglistByCollect.remove(curCommonFileInfo);
						curCommonFileInfo.setIsLike(false);

						mLikeImageView.setImageResource(R.drawable.musicplayer_islike_no);
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
							ContentManager.writeData2DB(getActivity(), mSonglistByCollect, MultimediaType.MMT_LIKEMUSIC);
						}
					});
					writeThread.start();
				}

				break;
			case R.id.allsong_top_btn:

				mSongListView.smoothScrollBy(-mSongListView.getLastVisiblePosition() * 98, 5000);// 98是一个list
																									// item的高度+dividerHeight

				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {

						if (!mSongListView.isStackFromBottom()) {
							mSongListView.setStackFromBottom(true);
						}
						mSongListView.setStackFromBottom(false);
						mSongListView.requestFocus();
					}
				}, 500);
				break;

			case R.id.musiclist_all:
				if (mCurrentTab == MUSIC_TAB_ALL) {
					return;
				}

				mMenuTip.setVisibility(View.VISIBLE);
				freshButtonFocus(v);
				mTabId = v.getId();
				mAllSongTab.requestFocus();
				if (null == mAdapter) {
					mAdapter = new AllSongListAllAdapter(mSonglist);
				}
				mSongListView.setAdapter(mAdapter);
				mCurrentTab = MUSIC_TAB_ALL;
				break;
			case R.id.musiclist_sort_album:
				if (mCurrentTab == MUSIC_TAB_ALBUM) {
					return;
				}
				mMenuTip.setVisibility(View.INVISIBLE);
				freshButtonFocus(v);
				mTabId = v.getId();
				mSingerTab.requestFocus();
				if (null == mAdapterForAlbum) {
					mAdapterForAlbum = new AllSongListAlbumAdapter(mSonglistByAlbum);
				}
				mSongListView.setAdapter(mAdapterForAlbum);
				mCurrentTab = MUSIC_TAB_ALBUM;
				break;
			case R.id.musiclist_sort_singer:
				if (mCurrentTab == MUSIC_TAB_SINGER) {
					return;
				}
				mMenuTip.setVisibility(View.INVISIBLE);
				freshButtonFocus(v);
				mTabId = v.getId();
				mAlbumTab.requestFocus();
				if (null == mAdapterForSinger) {
					mAdapterForSinger = new AllSongListSingerAdapter(mSonglistBySinger);
				}
				mSongListView.setAdapter(mAdapterForSinger);
				mCurrentTab = MUSIC_TAB_SINGER;
				break;
			case R.id.musiclist_favorite:
				if (mCurrentTab == MUSIC_TAB_FAVORITE) {
					return;
				}
				mMenuTip.setVisibility(View.VISIBLE);
				freshButtonFocus(v);
				mTabId = v.getId();
				mFavoriteTab.requestFocus();
				if (null == mAdapterforCollect) {
					mAdapterforCollect = new AllSongListAllAdapter(mSonglistByCollect);
				}
				mSongListView.setAdapter(mAdapterforCollect);
				mCurrentTab = MUSIC_TAB_FAVORITE;
				break;

			default:
				break;
			}

		}

	}

	class AllSongListFocusChangeListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {

			if (isButton(v.getId())) {
				if (hasFocus) {
					mAllSongTab.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
					mSingerTab.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
					mAlbumTab.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
					mFavoriteTab
							.setTextColor(getActivity().getResources().getColor(R.color.allsonglist_tab_unselected));
					changeTabBackground(v, true);
					((Button) v).setTextColor(getActivity().getResources().getColor(
							R.color.allsonglist_tab_top_selected));
				}

			}
			switch (v.getId()) {
			case R.id.musiclist_all:
				mMenuTip.setVisibility(View.VISIBLE);
				break;
			case R.id.musiclist_sort_singer:
				mMenuTip.setVisibility(View.INVISIBLE);
				break;
			case R.id.musiclist_sort_album:
				mMenuTip.setVisibility(View.INVISIBLE);
				break;
			case R.id.musiclist_favorite:
				mMenuTip.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	}

	class AllSongListOnkeyListener implements OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (v.getId()) {
				case R.id.musiclist_all:
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						if (null == mAdapterForSinger) {
							mAdapterForSinger = new AllSongListSingerAdapter(mSonglistBySinger);
						}
						mSongListView.setAdapter(mAdapterForSinger);
						mCurrentTab = MUSIC_TAB_SINGER;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

					}
					break;
				case R.id.musiclist_sort_singer:
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						if (null == mAdapterForAlbum) {
							mAdapterForAlbum = new AllSongListAlbumAdapter(mSonglistByAlbum);

						}
						mSongListView.setAdapter(mAdapterForAlbum);
						mCurrentTab = MUSIC_TAB_ALBUM;

					} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						if (null == mAdapter)
							mAdapter = new AllSongListAllAdapter(mSonglist);
						mSongListView.setAdapter(mAdapter);
						mAdapter.notifyDataSetChanged();
						mCurrentTab = MUSIC_TAB_ALL;
					}
					break;
				case R.id.musiclist_sort_album:
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						mAdapterforCollect = new AllSongListAllAdapter(mSonglistByCollect);
						mSongListView.setAdapter(mAdapterforCollect);
						mAdapterforCollect.notifyDataSetChanged();
						mCurrentTab = MUSIC_TAB_FAVORITE;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						mSongListView.setAdapter(mAdapterForSinger);
						mCurrentTab = MUSIC_TAB_SINGER;
					}
					break;
				case R.id.musiclist_favorite:
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						// do nothing
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						mSongListView.setAdapter(mAdapterForAlbum);
						mCurrentTab = MUSIC_TAB_ALBUM;
						// 切换到其他tab的时候，立刻刷新其他列表的“我喜欢”属性
						new Thread(new Runnable() {

							@Override
							public void run() {
								refreshLikeList();
							}
						}).start();
					}
					break;
				default:
					break;
				}

				if (isButton(v.getId())) {
					freshVisualizerView();

					if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						mTabIdFromTop = v.getId();
						mActivity.onRequestFocus(true);
						changeTabBackground(v, false);
						((Button) v).setTextColor(getActivity().getResources().getColor(
								R.color.allsonglist_tab_top_second_selected));
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						mSongListView.setSelection(0);
						mTabId = v.getId();
						boolean change = false;
						switch (mCurrentTab) {
						case MUSIC_TAB_ALL:
							if (!mSonglist.isEmpty()) {
								change = true;
							}
							break;
						case MUSIC_TAB_SINGER:

							if (!mSonglistBySinger.isEmpty()) {
								change = true;
							}
							break;
						case MUSIC_TAB_ALBUM:

							if (!mSonglistByAlbum.isEmpty()) {
								change = true;
							}
							break;
						case MUSIC_TAB_FAVORITE:

							if (!mSonglistByCollect.isEmpty()) {
								change = true;
							}
							break;
						}
						if (change) {
							changeTabBackground(v, false);
							((Button) v).setTextColor(getActivity().getResources().getColor(
									R.color.allsonglist_tab_top_second_selected));
						}

					}
				} else if (v.getId() == R.id.allsong_listview) {

					if (!mLikeButton.isAnimationOut()) {
						if (mLikeButton.getAnimation() != null && !mLikeButton.getAnimation().hasEnded()) {
							return true;
						}
					}
					if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						if (0 == mSongListView.getSelectedItemPosition() && -1 != mTabId) {
							root.findViewById(mTabId).requestFocus();
							return true;
						} else if (!isReadyToGo) {
							return false;
						} else if (mSongListView.getSelectedItemPosition() <= mSongListView.getFirstVisiblePosition() + 1) {
							if (mSongListView.getSelectedItemPosition() <= 1) {
								return false;
							}
							scrollListViewUp();
							return false;
						}
					} else if (keyCode == KeyEvent.KEYCODE_MENU) {

						if (mCurrentTab == MUSIC_TAB_ALBUM || mCurrentTab == MUSIC_TAB_SINGER) {
							return false;
						}
						if (mSongListView.getSelectedView() == null) {
							return false;
						}
						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							if (mSongListView.getSelectedView().getTag() instanceof ViewHolderForAudio) {
								((ViewHolderForAudio) mSongListView.getSelectedView().getTag()).islike
										.setVisibility(View.INVISIBLE);
							} else {
								((ViewHolder) mSongListView.getSelectedView().getTag()).islike
										.setVisibility(View.INVISIBLE);
							}
							mLikeButton.getNextLocation(mLikeImageView);

							Animation scaleAnimation = AnimationUtils
									.loadAnimation(getActivity(), R.anim.music_item_in);
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

					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (!isReadyToGo) {
							return false;
						}
						if (mSongListView.getSelectedItemPosition() >= mSongListView.getLastVisiblePosition() - 1) {
							if (mSongListView.getSelectedItemPosition() >= mSongListView.getAdapter().getCount() - 2) {
								return false;
							}
							scrollListViewDown();
							return false;
						}
					}
				} else if (v.getId() == R.id.allsong_like_clickButton || v.getId() == R.id.allsong_detail_clickButton) {
					if ((v.getId() == R.id.allsong_like_clickButton && keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
							|| (v.getId() == R.id.allsong_detail_clickButton && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
							|| keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
						mSongListView.requestFocus();
						mLikeButton.setVisibility(View.INVISIBLE);

						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(),
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
									if (mCurrentTab == MUSIC_TAB_ALL) {
										mAdapter.notifyDataSetChanged();
									} else {
										mAdapterforCollect.notifyDataSetChanged();
									}
								}
							});
							mLikeButton.startAnimation(scaleAnimation);

						}
						return true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						mSongListView.requestFocus();
						int[] location = new int[2];
						int[] location_root = new int[2];
						int[] location_listview = new int[2];
						mRootLayout.getLocationInWindow(location_root);
						mSongListView.getLocationInWindow(location_listview);
						mSongListView.getSelectedView().getLocationInWindow(location);

						int list_y = location_listview[1] - location_root[1];
						int button_y = location[1] - location_root[1];
						int y = (int) (button_y - list_y + mSongListView.getSelectedView().getHeight() * 2 + mSongListView
								.getDividerHeight());
						if (y < mSongListView.getHeight()) {
							int z = (int) (button_y - list_y + mSongListView.getSelectedView().getHeight());
							mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() + 1, z);
						} else {
							if (AllSongListAllAdapter.TYPE_NORMAL == mSongListView.getAdapter().getItemViewType(
									mSongListView.getSelectedItemPosition() + 1)) {
								if (AllSongListAllAdapter.TYPE_NORMAL == mSongListView.getAdapter().getItemViewType(
										mSongListView.getSelectedItemPosition())) {
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() + 1,
											mSongListView.getHeight() - mSongListView.getSelectedView().getHeight()
													- mSongListView.getDividerHeight() * 2);
								} else {
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() + 1,
											mSongListView.getHeight() - mSongListView.getSelectedView().getHeight() / 2
													- mSongListView.getDividerHeight() * 2);
								}

							} else if (AllSongListAllAdapter.TYPE_AUDIO == mSongListView.getAdapter().getItemViewType(
									mSongListView.getSelectedItemPosition() + 1)) {
								if (mSongListView.getSelectedItemPosition() >= mSongListView.getLastVisiblePosition() - 1) {
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() + 1,
											mSongListView.getHeight() - mSongListView.getSelectedView().getHeight() * 2
													- mSongListView.getDividerHeight() * 2);
								} else {
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() + 1,
											mSongListView.getHeight() - mSongListView.getSelectedView().getHeight() * 2
													- mSongListView.getDividerHeight());
								}

							}

						}

						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(),
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
									if (mCurrentTab == MUSIC_TAB_ALL) {
										mAdapter.notifyDataSetChanged();
									} else {
										mAdapterforCollect.notifyDataSetChanged();
									}
								}
							});
							mLikeButton.startAnimation(scaleAnimation);
						}
						return true;

					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {

						mSongListView.requestFocus();
						int[] location = new int[2];
						int[] location_root = new int[2];
						int[] location_listview = new int[2];
						mRootLayout.getLocationInWindow(location_root);
						mSongListView.getLocationInWindow(location_listview);
						mSongListView.getSelectedView().getLocationInWindow(location);

						int list_y = location_listview[1] - location_root[1];
						int button_y = location[1] - location_root[1];
						int y = (int) (button_y - list_y - mSongListView.getSelectedView().getHeight() * 1 - mSongListView
								.getDividerHeight());

						if (y < 0) {
							mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() - 1, 0);
						} else if (y < mSongListView.getHeight()) {
							if (AllSongListAllAdapter.TYPE_NORMAL == mSongListView.getAdapter().getItemViewType(
									mSongListView.getSelectedItemPosition() - 1)) {
								if (AllSongListAllAdapter.TYPE_NORMAL == mSongListView.getAdapter().getItemViewType(
										mSongListView.getSelectedItemPosition())) {
									int z = (int) (button_y - list_y - mSongListView.getSelectedView().getHeight() - mSongListView
											.getDividerHeight() * 2);
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() - 1, z);
								} else {
									int z = (int) (button_y - list_y - mSongListView.getSelectedView().getHeight() / 2 - mSongListView
											.getDividerHeight() * 2);
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() - 1, z);
								}

							} else {
								if (mSongListView.getSelectedItemPosition() <= mSongListView.getFirstVisiblePosition() + 1) {
									int z = (int) (button_y - list_y - mSongListView.getSelectedView().getHeight() * 2);
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() - 1, z);
								} else {
									int z = (int) (button_y - list_y - mSongListView.getSelectedView().getHeight() * 2 - mSongListView
											.getDividerHeight() * 2);
									mSongListView.setSelectionFromTop(mSongListView.getSelectedItemPosition() - 1, z);
								}

							}

						}

						if (mLikeButton.getAnimation() == null || mLikeButton.getAnimation().hasEnded()) {

							Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(),
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
									if (mCurrentTab == MUSIC_TAB_ALL) {
										mAdapter.notifyDataSetChanged();
									} else {
										mAdapterforCollect.notifyDataSetChanged();
									}
								}
							});
							mLikeButton.startAnimation(scaleAnimation);
						}
						return true;
					}

				} else if (v.getId() == R.id.allsong_top_btn) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						mSongListView.requestFocus();
						return true;
					}
				}
			}
			return false;
		}

	}

	private boolean isButton(int id) {
		return id == R.id.musiclist_all || id == R.id.musiclist_sort_singer || id == R.id.musiclist_sort_album
				|| id == R.id.musiclist_favorite;
	}

	public void freshVisualizerView() {

		if (mCurrentTab == MUSIC_TAB_ALL | mCurrentTab == MUSIC_TAB_FAVORITE) {
			if (null != mVisualizerView) {
				mVisualizerView.setmStopDraw(true);
			}

		} else {
			if (null != mVisualizerView) {
				mVisualizerView.setmStopDraw(false);
			}
		}

	}

	public void setVisualizerView(boolean state) {

		if (null != mVisualizerView) {
			mVisualizerView.setmStopDraw(state);
		}
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

	class AllSongListAllAdapter extends MusicBaseAdapter {

		public static final int TYPE_NORMAL = 0;
		public static final int TYPE_AUDIO = 1;
		private LayoutInflater mInflater;

		public AllSongListAllAdapter(List<CommonFileInfo> fileList) {
			super(fileList);
			mInflater = LayoutInflater.from(getActivity());
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
					holder = new ViewHolder();

					convertView = mInflater.inflate(R.layout.music_alllist_item_all, null);
					setViewHolder(convertView, holder);
					break;

				case TYPE_AUDIO:
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

			if (mFileList.size()==0) {
				//or return null
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
					holderForAudio.state.setText(MusicUtils.getResourceString(getActivity(), R.string.music_playing));
				} else {
					holderForAudio.state.setText(MusicUtils.getResourceString(getActivity(), R.string.music_pause));
				}

				if (parent instanceof MusicListView) {
					if (((MusicListView) parent).isOnMeasure) {

					} else {
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

	class AllSongListSingerAdapter extends MusicBaseAdapter {

		public AllSongListSingerAdapter(List<CommonFileInfo> fileList) {
			super(fileList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolderForSinger holder = null;
			if (convertView == null) {

				holder = new ViewHolderForSinger();
				LayoutInflater mInflater = LayoutInflater.from(getActivity());
				convertView = mInflater.inflate(R.layout.music_alllist_item_singer, null);

				holder.profile = (RoundedImageView) convertView.findViewById(R.id.allsong_singer_profile);
				holder.artist = (TextView) convertView.findViewById(R.id.allsong_singer_artist);
				holder.count = (TextView) convertView.findViewById(R.id.allsong_singer_count);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolderForSinger) convertView.getTag();
			}

			if (parent instanceof MusicListView) {
				if (((MusicListView) parent).isOnMeasure) {
					return convertView;
				}
			}

			List<CommonFileInfo> mFileList = getList();
			if (mFileList.size()==0) {
				//or return null
				return convertView;
			}

			// holder.profile.setText(name.substring(0, name.length() - 4));
			holder.artist.setText(mFileList.get(position).getSinger());
			if (mFileList.get(position).getChildrenCount() > 1) {
				holder.count.setText(mFileList.get(position).getChildrenCount()
						+ MusicUtils.getResourceString(getActivity(), R.string.music_file_count_more));
			} else {
				holder.count.setText(mFileList.get(position).getChildrenCount()
						+ MusicUtils.getResourceString(getActivity(), R.string.music_file_count));
			}

			LocalImageLoader.getInstance().loadImage(getActivity(), mFileList.get(position).getSinger().toLowerCase(),
					holder.profile);

			return convertView;
		}

	}

	class AllSongListAlbumAdapter extends MusicBaseAdapter {

		public AllSongListAlbumAdapter(List<CommonFileInfo> fileList) {
			super(fileList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolderForAlbum holder = null;
			if (convertView == null) {

				holder = new ViewHolderForAlbum();
				LayoutInflater mInflater = LayoutInflater.from(getActivity());
				convertView = mInflater.inflate(R.layout.music_alllist_item_album, null);

				holder.profile = (ImageView) convertView.findViewById(R.id.allsong_album_profile);
				holder.special = (TextView) convertView.findViewById(R.id.allsong_album_artist);
				holder.count = (TextView) convertView.findViewById(R.id.allsong_album_count);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolderForAlbum) convertView.getTag();
			}

			if (parent instanceof MusicListView) {
				if (((MusicListView) parent).isOnMeasure) {
					return convertView;
				}
			}

			List<CommonFileInfo> mFileList = getList();
			if (mFileList.size()==0) {
				//or return null
				return convertView;
			}

			MusicThumnailLoader.getInstance().loadImage(mFileList.get(position).getPath(), holder.profile);
			holder.special.setText(mFileList.get(position).getSpecial());
			if (mFileList.get(position).getChildrenCount() > 1) {
				holder.count.setText(mFileList.get(position).getChildrenCount()
						+ MusicUtils.getResourceString(getActivity(), R.string.music_file_count_more));
			} else {
				holder.count.setText(mFileList.get(position).getChildrenCount()
						+ MusicUtils.getResourceString(getActivity(), R.string.music_file_count));
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

	public final class ViewHolderForSinger {
		public RoundedImageView profile;
		public TextView artist;
		public TextView count;
	}

	public final class ViewHolderForAlbum {
		public ImageView profile;
		public TextView special;
		public TextView count;
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

	/*
	 * @Title: scrollListViewDown
	 *
	 * @Description: 向上滑动列表
	 */
	private void scrollListViewUp() {
		int[] location = new int[2];
		int[] location_root = new int[2];
		int[] location_listview = new int[2];
		mRootLayout.getLocationInWindow(location_root);
		mSongListView.getLocationInWindow(location_listview);
		mSongListView.getSelectedView().getLocationInWindow(location);
		int list_y = location_listview[1] - location_root[1];
		int button_y = location[1] - location_root[1];
		int distance = mSongListView.getHeight() - mSongListView.getSelectedView().getHeight()
				- mSongListView.getDividerHeight() - (button_y - list_y);
		int temp = button_y - list_y - mSongListView.getDividerHeight();

		mSongListView.smoothScrollBy(-distance - temp, 500);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				((BaseAdapter) mSongListView.getAdapter()).notifyDataSetChanged();
				mLikeButton.setLocation(mSongListView.getSelectedView(), mRootLayout, mSongListView.getSelectedView());
			}
		}, 500);
	}

	/*
	 * @Title: scrollListViewDown
	 *
	 * @Description: 向下滑动列表
	 */
	private void scrollListViewDown() {
		int[] location = new int[2];
		int[] location_root = new int[2];
		int[] location_listview = new int[2];
		mRootLayout.getLocationInWindow(location_root);
		mSongListView.getLocationInWindow(location_listview);
		mSongListView.getSelectedView().getLocationInWindow(location);
		int list_y = location_listview[1] - location_root[1];
		int button_y = location[1] - location_root[1];
		int temp;
		if (mCurrentTab == MUSIC_TAB_ALL || mCurrentTab == MUSIC_TAB_FAVORITE) {
			if (AllSongListAllAdapter.TYPE_NORMAL == mSongListView.getAdapter().getItemViewType(
					mSongListView.getSelectedItemPosition() + 1)) {
				if (AllSongListAllAdapter.TYPE_NORMAL == mSongListView.getAdapter().getItemViewType(
						mSongListView.getSelectedItemPosition())) {
					temp = (mSongListView.getSelectedView().getHeight() + mSongListView.getDividerHeight())
							- (mSongListView.getHeight() - (button_y - list_y + mSongListView.getSelectedView()
									.getHeight()));

				} else {
					temp = (mSongListView.getSelectedView().getHeight() * 2 + mSongListView.getDividerHeight())
							- (mSongListView.getHeight() - (button_y - list_y + mSongListView.getSelectedView()
									.getHeight()));
				}
			} else {
				if (AllSongListAllAdapter.TYPE_NORMAL == mSongListView.getAdapter().getItemViewType(
						mSongListView.getSelectedItemPosition())) {
					temp = (mSongListView.getSelectedView().getHeight() * 2 + mSongListView.getDividerHeight())
							- (mSongListView.getHeight() - (button_y - list_y + mSongListView.getSelectedView()
									.getHeight()));
				} else {
					temp = (mSongListView.getSelectedView().getHeight() * 2 + mSongListView.getDividerHeight())
							- (mSongListView.getHeight() - (button_y - list_y + mSongListView.getSelectedView()
									.getHeight()));
				}
			}
		} else {
			temp = (mSongListView.getSelectedView().getHeight() + mSongListView.getDividerHeight())
					- (mSongListView.getHeight() - (button_y - list_y + mSongListView.getSelectedView().getHeight()));
		}
		mSongListView.smoothScrollBy(button_y - list_y + mSongListView.getSelectedView().getHeight() - temp
				- mSongListView.getDividerHeight(), 500);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				((BaseAdapter) mSongListView.getAdapter()).notifyDataSetChanged();
				mLikeButton.setLocation(mSongListView.getSelectedView(), mRootLayout, mSongListView.getSelectedView());
			}
		}, 500);
	}

	public void dismissLikeButton(MotionEvent event) {
		Trace.Info("touch listener");
		if (mLikeButton != null && mLikeButton.getVisibility() == View.VISIBLE) {
			Rect bounds = mLikeButton.getBackground().getBounds();
			if (!bounds.contains((int) event.getX(), (int) event.getY())) {
				mLikeButton.setVisibility(View.INVISIBLE);
			}
		}
	}

}
