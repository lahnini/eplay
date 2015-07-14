/**
 * @Title: SearchLyricFragment.java
 * @Package com.konka.eplay.modules.music
 * @Description: TODO(用一句话描述该文件做什么)
 * @author A18ccms A18ccms_gmail_com
 * @date 2015年3月24日 下午2:20:39
 * @version
 */
package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Audio;
import iapp.eric.utils.base.Audio.LyricEngineType;
import iapp.eric.utils.base.Trace;
import iapp.eric.utils.metadata.Lyric;
import iapp.eric.utils.metadata.SongInfo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.eplay.GlobalData;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.music.LyricViewFragment.OnLyricViewListener;

/**
 * @ClassName: SearchLyricFragment
 * @Description: 音乐播放器搜索主界面
 * @author xuyunyu
 * @date 2015年3月24日 下午2:20:39
 */
public class SearchLyricFragment extends Fragment {

	public final static int LYRIC_SEARCHING = 1;
	public final static int LYRIC_SEARCHING_NO_RESULT = 2;
	public final static int LYRIC_SEARCHING_SHOW_RESULT = 3;
	public final static int LYRIC_SEARCHING_SHOW_SINGER_OPTION = 4;
	public final static int LYRIC_SEARCHING_SINGER_MODE = 5;
	public final static int LYRIC_SEARCHING_NO_NETWORK = 6;

	public final static String LYRIC_SEARCH_LAST_KEYWORD = "LastKeword";
	public final static String LYRIC_SEARCH_LAST_LIST = "LastList";
	public final static String LYRIC_SEARCH_LAST_SONG_REALNAME = "LastSongRealName";
	private OnLyricViewListener mCardFlipSwitchCallBack;
	private ImageButton mSearchButton;
	private ListView mListView;
	private ListView mListViewSecond;
	private EditText mEditText;
	private ViewGroup mLyricSearchingLayout;
	private RelativeLayout mSearchingNoResultText;
	private ImageView mSearchingImageView;

	private FrameLayout mEditTextBg;
	private FrameLayout mSearchAgainLayout;
	private Button mSearchAgainButton;

	private Button mButtonNoMoreSearch;
	private FrameLayout mEditTextBgForSinger;
	private FrameLayout mEditTextBgForSingerSecond;
	private FrameLayout mSearchForSingerLayout;
	private FrameLayout mSearchForSingerLayoutSecond;
	private FrameLayout mSearchNoMoreLayout;
	private ImageButton mSearchButtonForSinger;
	private ImageButton mSearchButtonForSingerSecond;
	private EditText mEditTextForSinger;
	private EditText mEditTextForSingerSecond;
	private TextView mSongnameTextView;
	private FrameLayout mSearchButtonBg;

	private SearchOnKeyListenner mOnKeyListenner;
	private SearchOnClickListener mClickListener;
	private SearchOnFocusChangeListener mFocusChangeListener;
	private ArrayList<SearchResult> mSearchResults;

	private onSearchBackListaenner mSearchBackListaenner;

	private String mTitle;
	private String mSongSinger;
	private String mLastKeywords;
	private String mLastSongRealName;
	private SearchListAdapter mSearchListAdapter;
	private ArrayList<Lyric> lyricList;
	private boolean mBeginSingerSecondMode = false;
	private GlobalData mGlobalData;
	private MusicDBManager mDbManager;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case LYRIC_SEARCHING:
				mListView.setVisibility(View.GONE);
				mSearchingNoResultText.setVisibility(View.GONE);
				mSearchNoMoreLayout.setVisibility(View.GONE);
				mSearchAgainLayout.setVisibility(View.GONE);
				if (!mBeginSingerSecondMode) {
					mSearchForSingerLayout.setVisibility(View.GONE);
				} else {
					mSearchForSingerLayout.setVisibility(View.VISIBLE);
					Animation animation = new TranslateAnimation(0, 0, 0, -300f);
					animation.setDuration(200);
					// animation.setFillAfter(true);
					animation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation paramAnimation) {

						}

						@Override
						public void onAnimationRepeat(Animation paramAnimation) {
						}

						@Override
						public void onAnimationEnd(Animation paramAnimation) {

							mSearchForSingerLayoutSecond.setVisibility(View.VISIBLE);
							mEditTextForSingerSecond.setText(mEditTextForSinger.getText().toString().trim());
							mSearchForSingerLayout.setVisibility(View.GONE);
							mSearchButtonForSingerSecond.requestFocus();

						}
					});
					mSearchForSingerLayout.startAnimation(animation);
				}
				mLyricSearchingLayout.setVisibility(View.VISIBLE);
				mSearchButton.requestFocus();

				AnimationDrawable anim = (AnimationDrawable) mSearchingImageView.getBackground();
				anim.start();
				break;
			case LYRIC_SEARCHING_SINGER_MODE:
				mListViewSecond.setVisibility(View.GONE);
				mSearchingNoResultText.setVisibility(View.GONE);
				mSearchNoMoreLayout.setVisibility(View.GONE);
				mLyricSearchingLayout.setVisibility(View.VISIBLE);

				AnimationDrawable anim_second = (AnimationDrawable) mSearchingImageView.getBackground();
				anim_second.start();
				break;
			case LYRIC_SEARCHING_NO_RESULT:

				if (mListView.getTag().equals(msg.obj)) {
					if (null != mSearchingImageView && mSearchingImageView.getBackground() != null
							&& mSearchingImageView.getBackground() instanceof AnimationDrawable) {
						AnimationDrawable animDra = (AnimationDrawable) mSearchingImageView.getBackground();
						if (animDra != null && animDra.isRunning()) { // 如果正在运行,就停止
							animDra.stop();
						}
					}
					if (mBeginSingerSecondMode) {
						mEditText.setEnabled(true);
						mEditText.setFocusable(true);
						mSongnameTextView.setTextColor(getActivity().getResources().getColor(
								R.color.picture_info_text_bright));
						mEditText.setTextColor(getActivity().getResources().getColor(R.color.picture_info_text_bright));
					}
					mLyricSearchingLayout.setVisibility(View.GONE);
					mListView.setVisibility(View.GONE);
					mSearchingNoResultText.setVisibility(View.VISIBLE);
				}

				break;
			case LYRIC_SEARCHING_SHOW_RESULT:
				if (mListView.getTag().equals(msg.obj)) {

					if (null != mSearchingImageView && mSearchingImageView.getBackground() != null
							&& mSearchingImageView.getBackground() instanceof AnimationDrawable) {
						AnimationDrawable animtion = (AnimationDrawable) mSearchingImageView.getBackground();
						if (animtion != null && animtion.isRunning()) { // 如果正在运行,就停止
							animtion.stop();
						}
					}
					mLyricSearchingLayout.setVisibility(View.GONE);

					if (!mBeginSingerSecondMode) {
						mListView.setVisibility(View.VISIBLE);
						mSearchListAdapter.notifyDataSetChanged();
					} else {
						mListView.setVisibility(View.GONE);
						mEditText.setEnabled(true);
						mEditText.setFocusable(true);
						mSongnameTextView.setTextColor(getActivity().getResources().getColor(
								R.color.picture_info_text_bright));
						mEditText.setTextColor(getActivity().getResources().getColor(R.color.picture_info_text_bright));
						mListViewSecond.setVisibility(View.VISIBLE);
						mListViewSecond.setAdapter(mSearchListAdapter);
						mSearchListAdapter.notifyDataSetChanged();
					}
					mSearchingNoResultText.setVisibility(View.GONE);

				}

				break;
			case LYRIC_SEARCHING_SHOW_SINGER_OPTION:
				if (mListView.getTag().equals(msg.obj)) {
					if (null != mSearchingImageView && mSearchingImageView.getBackground() != null
							&& mSearchingImageView.getBackground() instanceof AnimationDrawable) {
						AnimationDrawable animDra = (AnimationDrawable) mSearchingImageView.getBackground();
						if (animDra != null && animDra.isRunning()) { // 如果正在运行,就停止
							animDra.stop();
						}
					}
					mSearchNoMoreLayout.setVisibility(View.VISIBLE);
					mSearchForSingerLayout.setVisibility(View.VISIBLE);
					mEditTextForSinger.requestFocus();
					mEditTextForSinger.setText(mSongSinger);
					mEditTextForSinger.setSelection(mSongSinger.length());
					mLyricSearchingLayout.setVisibility(View.GONE);
					mListView.setVisibility(View.GONE);
					mSearchButton.setVisibility(View.GONE);
					mSearchButtonBg.setVisibility(View.GONE);
					mSongnameTextView.setTextColor(getActivity().getResources().getColor(R.color.search_lyric_text));
					mEditText.setTextColor(getActivity().getResources().getColor(R.color.search_lyric_text));
					mEditText.setEnabled(false);
					mEditText.setFocusable(false);

				}

				break;
			case LYRIC_SEARCHING_NO_NETWORK:
				if (null != mSearchingImageView && mSearchingImageView.getBackground() != null
						&& mSearchingImageView.getBackground() instanceof AnimationDrawable) {
					AnimationDrawable animtion = (AnimationDrawable) mSearchingImageView.getBackground();
					if (animtion != null && animtion.isRunning()) { // 如果正在运行,就停止
						animtion.stop();
					}
				}
				mLyricSearchingLayout.setVisibility(View.GONE);
				mSearchAgainLayout.setVisibility(View.VISIBLE);
				mSearchAgainButton.requestFocus();

				break;

			default:
				break;
			}

		};
	};

	public interface onSearchBackListaenner {

		public void onSearchLyric(Lyric lyric);
	}

	public void setSearchListenner(onSearchBackListaenner searchBackListaenner) {
		this.mSearchBackListaenner = searchBackListaenner;
	}

	public void setSearchTitle(CommonFileInfo info) {
		if (info.getTitle().equalsIgnoreCase("未知歌名") || info.getTitle().equalsIgnoreCase("")) {//TODO
			mTitle = info.getName().substring(0, info.getName().length() - 4);
		} else {
			mTitle = info.getTitle();
		}

		if (info.getSinger().equalsIgnoreCase("未知歌手") || info.getSinger().equalsIgnoreCase("")) {
			mSongSinger = "";
		} else {
			mSongSinger = info.getSinger();
		}

		Trace.Info(mTitle + mSongSinger);

	}

	public SearchLyricFragment() {
		setArguments(new Bundle());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mOnKeyListenner = new SearchOnKeyListenner();
		mClickListener = new SearchOnClickListener();
		mFocusChangeListener = new SearchOnFocusChangeListener();
		mSearchResults = new ArrayList<SearchResult>();
		lyricList = new ArrayList<Lyric>();
		mLastKeywords = "";
		mLastSongRealName = "";

		//获取上一次搜索的结果
		mGlobalData = (GlobalData) getActivity().getApplication();
		mLastKeywords = mGlobalData.mLastKeywords;
		mLastSongRealName = mGlobalData.mLastSongRealName;
		if (mLastSongRealName != null && mTitle.equals(mLastSongRealName)) {
			mTitle = mLastKeywords;
			mSearchResults = mGlobalData.mSearchResults;
			lyricList = mGlobalData.lyricList;
		} else {
			mLastSongRealName = mTitle;
			mLastKeywords = mTitle;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.music_searchlyric, container, false);
		root.setBackgroundColor(Color.TRANSPARENT);
		mSearchButton = (ImageButton) root.findViewById(R.id.music_searhlyric_button);
		mEditText = (EditText) root.findViewById(R.id.search_enter_songname);
		mEditTextBg = (FrameLayout) root.findViewById(R.id.search_enter_songname_bg);

		mEditTextBgForSinger = (FrameLayout) root.findViewById(R.id.search_enter_songname_singer_bg);
		mSearchForSingerLayout = (FrameLayout) root.findViewById(R.id.search_edit_singer);
		mSearchNoMoreLayout = (FrameLayout) root.findViewById(R.id.search_singer);
		mSearchButtonForSinger = (ImageButton) root.findViewById(R.id.music_searhlyric_button_singer);
		mEditTextForSinger = (EditText) root.findViewById(R.id.search_enter_singername);
		mSongnameTextView = (TextView) root.findViewById(R.id.search_songname);
		mSearchButtonBg = (FrameLayout) root.findViewById(R.id.music_searhlyric_button_bg);

		mSearchAgainLayout = (FrameLayout) root.findViewById(R.id.search_again_layout);
		mSearchAgainButton = (Button) root.findViewById(R.id.search_again);

		mSearchingNoResultText = (RelativeLayout) root.findViewById(R.id.search_no_result);
		mLyricSearchingLayout = (RelativeLayout) root.findViewById(R.id.lyric_result_searching);
		mSearchingImageView = (ImageView) root.findViewById(R.id.searching_image);
		mButtonNoMoreSearch = (Button) root.findViewById(R.id.search_no_more);
		mSearchingImageView.setBackgroundResource(R.drawable.downloading_anim);

		mEditTextBgForSingerSecond = (FrameLayout) root.findViewById(R.id.search_enter_songname_singer_bg_second);
		mSearchForSingerLayoutSecond = (FrameLayout) root.findViewById(R.id.search_edit_singer_second);
		mSearchButtonForSingerSecond = (ImageButton) root.findViewById(R.id.music_searhlyric_button_singer_second);
		mEditTextForSingerSecond = (EditText) root.findViewById(R.id.search_enter_singername_second);

		mListView = (ListView) root.findViewById(R.id.search_result_list);
		mListViewSecond = (ListView) root.findViewById(R.id.search_result_list_second);
		// mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mSearchListAdapter = new SearchListAdapter();
		mListView.setAdapter(mSearchListAdapter);
		// mListView.setSelection(0);

		mEditText.setOnKeyListener(mOnKeyListenner);
		mSearchButton.setOnKeyListener(mOnKeyListenner);
		mListView.setOnKeyListener(mOnKeyListenner);
		mListViewSecond.setOnKeyListener(mOnKeyListenner);
		mEditTextForSinger.setOnKeyListener(mOnKeyListenner);
		mEditTextForSingerSecond.setOnKeyListener(mOnKeyListenner);
		mSearchButtonForSinger.setOnKeyListener(mOnKeyListenner);
		mSearchButtonForSingerSecond.setOnKeyListener(mOnKeyListenner);
		mButtonNoMoreSearch.setOnKeyListener(mOnKeyListenner);

		mSearchButton.setOnClickListener(mClickListener);
		mEditText.setOnClickListener(mClickListener);
		mEditTextForSinger.setOnClickListener(mClickListener);
		mEditTextForSingerSecond.setOnClickListener(mClickListener);
		mButtonNoMoreSearch.setOnClickListener(mClickListener);
		mSearchButtonForSinger.setOnClickListener(mClickListener);
		mSearchButtonForSingerSecond.setOnClickListener(mClickListener);
		mSearchAgainButton.setOnClickListener(mClickListener);

		mEditText.setOnFocusChangeListener(mFocusChangeListener);
		mEditTextForSinger.setOnFocusChangeListener(mFocusChangeListener);
		mEditTextForSingerSecond.setOnFocusChangeListener(mFocusChangeListener);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((LyricViewFragment) getFragmentManager().findFragmentByTag("LyricViewFragment")).getArguments()
						.putBoolean(LyricViewFragment.ARGUMENT_IS_SEARCH_BY_USER, true);
				mCardFlipSwitchCallBack.onCardFlip();

				if (null != lyricList) {
					String lrcUrl = lyricList.get(position).getLrcUrl();
					LyricEngineType let = lyricList.get(position).getLet();
					Lyric lyric = new Lyric(let, lrcUrl);
					mSearchBackListaenner.onSearchLyric(lyric);
				}

			}
		});

		mListViewSecond.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((LyricViewFragment) getFragmentManager().findFragmentByTag("LyricViewFragment")).getArguments()
						.putBoolean(LyricViewFragment.ARGUMENT_IS_SEARCH_BY_USER, true);
				mCardFlipSwitchCallBack.onCardFlip();

				if (null != lyricList) {
					String lrcUrl = lyricList.get(position).getLrcUrl();
					LyricEngineType let = lyricList.get(position).getLet();
					Lyric lyric = new Lyric(let, lrcUrl);
					mSearchBackListaenner.onSearchLyric(lyric);
				}

			}
		});

		return root;
	}

	@Override
	public void onPause() {
		super.onPause();
		Trace.Info(mLastKeywords + "  " + mLastSongRealName);

		//存储搜索的结果
		mGlobalData.mLastKeywords = mLastKeywords;
		mGlobalData.mLastSongRealName = mLastSongRealName;
		mGlobalData.mSearchResults = mSearchResults;
		mGlobalData.lyricList = lyricList;

	}

	//集中处理各组件key事件
	class SearchOnKeyListenner implements OnKeyListener {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			boolean handled = false;
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (v.getId()) {
				case R.id.music_searhlyric_button:
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && v.isFocused()) {
						shake(v);
						handled = true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && v.isFocused()) {
						if (mSearchAgainLayout.getVisibility()==View.VISIBLE) {
							handled = false;
						}else if (mSearchResults == null || mSearchResults.size() == 0) {
							shake(v);
							handled = true;
						}

					}
					break;
				case R.id.search_enter_songname:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && v.isFocused()) {

						if (0 == mEditText.getSelectionStart()) {
							getFragmentManager().popBackStack();
							handled = true;
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && v.isFocused()) {
						if (mSearchForSingerLayoutSecond.getVisibility() == View.VISIBLE) {
							handled = false;
						} else if (mSearchAgainLayout.getVisibility() == View.VISIBLE) {
							handled = false;
						}else if (mSearchResults == null || mSearchResults.size() == 0) {
							shake(v);
							shake(mEditTextBg);
							handled = true;
						}
					}
					break;
				case R.id.search_result_list:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && v.isFocused()) {
						getFragmentManager().popBackStack();
						handled = true;
					}
					break;

				case R.id.search_result_list_second:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && v.isFocused()) {
						getFragmentManager().popBackStack();
						handled = true;
					}
					break;
				case R.id.music_searhlyric_button_singer_second:
				case R.id.music_searhlyric_button_singer:
					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && v.isFocused()) {
						shake(v);
						handled = true;
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && v.isFocused()
							&& (mSearchResults == null || mSearchResults.size() == 0)) {
						shake(v);
						handled = true;
					}
					break;

				case R.id.search_enter_singername:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && v.isFocused()) {

						if (0 == mEditTextForSinger.getSelectionStart()) {
							getFragmentManager().popBackStack();
							handled = true;
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && v.isFocused()
							&& (mSearchResults == null || mSearchResults.size() == 0)) {
						shake(v);
						shake(mEditTextBgForSinger);
						handled = true;
					}
					break;

				case R.id.search_enter_singername_second:
					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && v.isFocused()) {

						if (0 == mEditTextForSingerSecond.getSelectionStart()) {
							getFragmentManager().popBackStack();
							handled = true;
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && v.isFocused()
							&& (mSearchResults == null || mSearchResults.size() == 0)) {
						shake(v);
						shake(mEditTextBgForSingerSecond);
						handled = true;
					}
					break;

				default:

					break;
				}
			}
			return handled;
		}

	}

	//集中处理各组件的点击事件
	class SearchOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.search_enter_songname:
				mEditTextBg.setBackgroundResource(R.drawable.edittext_enter);
				mEditText.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
				((InputMethodManager) getActivity().getSystemService(MusicPlayerActivity.INPUT_METHOD_SERVICE))
						.showSoftInput(v, InputMethodManager.SHOW_FORCED);

				break;
			case R.id.search_again:
			case R.id.music_searhlyric_button:
				mHandler.sendEmptyMessage(LYRIC_SEARCHING);

				if (!Utils.isConnected(getActivity())) {
					mHandler.sendEmptyMessage(LYRIC_SEARCHING_NO_NETWORK);
					return;
				}
				new Thread(new Runnable() {
					@Override
					public void run() {

						Audio a = new Audio();
						String searchWords = mEditText.getText().toString().trim();
						mLastKeywords = searchWords;
						Trace.Info(searchWords);
						mListView.setTag(searchWords + mSongSinger);

						// test
						// Message message1 = Message.obtain();
						// message1.obj = searchWords + mSongSinger;
						// message1.what = LYRIC_SEARCHING_SHOW_SINGER_OPTION;
						// mHandler.sendMessage(message1);
						// return;

						SongInfo songInfo = a.searchSongInfo(searchWords, null);
						if (null == songInfo) {
							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_NO_RESULT;
							mHandler.sendMessage(message);
							return;
						}
						lyricList = songInfo.getLyricList();
						mSearchResults = new ArrayList<SearchResult>();

						Trace.Info("list size " + lyricList.size());
						if (null != lyricList && lyricList.size() != 0) {
							SearchResult result;
							for (int i = 0; i < lyricList.size(); i++) {
								result = new SearchResult();
								result.url = lyricList.get(i).getLrcUrl();
								if (null == mDbManager) {
									mDbManager = new MusicDBManager(getActivity());
								}

								if (lyricList.get(i).getLet().equals(LyricEngineType.LET_GCM)) {
									result.singer = mDbManager.queryDatabase(lyricList.get(i).getArtist());
									if (result.singer==null) {
										result.singer = mSongSinger;
									}
								}else {
									result.singer = mSongSinger;
								}
								result.songname = lyricList.get(i).getSongName();
								mSearchResults.add(result);
							}

							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_SHOW_RESULT;
							mHandler.sendMessage(message);
						} else {
							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_SHOW_SINGER_OPTION;
							mHandler.sendMessage(message);
						}

					}
				}).start();

				break;
			case R.id.music_searhlyric_button_singer_second:
				mHandler.sendEmptyMessage(LYRIC_SEARCHING_SINGER_MODE);
				new Thread(new Runnable() {
					@Override
					public void run() {

						Audio a = new Audio();
						String searchWords = mEditText.getText().toString().trim();
						String searchWords_singer = mEditTextForSingerSecond.getText().toString().trim();
						Trace.Info(searchWords);
						mListView.setTag(searchWords + searchWords_singer);
						SongInfo songInfo = a.searchSongInfo(searchWords, searchWords_singer);
						if (null == songInfo) {
							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_NO_RESULT;
							mHandler.sendMessage(message);
							return;
						}
						lyricList = songInfo.getLyricList();
						mSearchResults = new ArrayList<SearchResult>();

						Trace.Info("list size " + lyricList.size());
						if (null != lyricList && lyricList.size() != 0) {
							SearchResult result;
							for (int i = 0; i < lyricList.size(); i++) {
								result = new SearchResult();
								result.url = lyricList.get(i).getLrcUrl();
								if (null == mDbManager) {
									mDbManager = new MusicDBManager(getActivity());
								}

								if (lyricList.get(i).getLet().equals(LyricEngineType.LET_GCM)) {
									result.singer = mDbManager.queryDatabase(lyricList.get(i).getArtist());
									if (result.singer==null) {
										result.singer = mSongSinger;
									}
								}else {
									result.singer = mSongSinger;
								}
								result.songname = lyricList.get(i).getSongName();
								mSearchResults.add(result);
							}

							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_SHOW_RESULT;
							mHandler.sendMessage(message);
						} else {
							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_NO_RESULT;
							mHandler.sendMessage(message);
						}

					}
				}).start();

				break;
			case R.id.music_searhlyric_button_singer:
				mBeginSingerSecondMode = true;
				mHandler.sendEmptyMessage(LYRIC_SEARCHING);

				new Thread(new Runnable() {
					@Override
					public void run() {

						Audio a = new Audio();
						String searchWords = mEditText.getText().toString().trim();
						String searchWords_singer = mEditTextForSinger.getText().toString().trim();
						Trace.Info(searchWords);
						mListView.setTag(searchWords + searchWords_singer);
						SongInfo songInfo = a.searchSongInfo(searchWords, searchWords_singer);
						if (null == songInfo) {
							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_NO_RESULT;
							mHandler.sendMessage(message);
							return;
						}
						lyricList = songInfo.getLyricList();
						mSearchResults = new ArrayList<SearchResult>();

						Trace.Info("list size " + lyricList.size());
						if (null != lyricList && lyricList.size() != 0) {
							SearchResult result;
							for (int i = 0; i < lyricList.size(); i++) {
								result = new SearchResult();
								result.url = lyricList.get(i).getLrcUrl();
								if (null == mDbManager) {
									mDbManager = new MusicDBManager(getActivity());
								}

								if (lyricList.get(i).getLet().equals(LyricEngineType.LET_GCM)) {
									result.singer = mDbManager.queryDatabase(lyricList.get(i).getArtist());
									if (result.singer==null) {
										result.singer = mSongSinger;
									}
								}else {
									result.singer = mSongSinger;
								}
								result.songname = lyricList.get(i).getSongName();
								mSearchResults.add(result);
							}

							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_SHOW_RESULT;
							mHandler.sendMessage(message);
						} else {
							Message message = Message.obtain();
							message.obj = searchWords + mSongSinger;
							message.what = LYRIC_SEARCHING_NO_RESULT;
							mHandler.sendMessage(message);
						}

					}
				}).start();
				break;
			case R.id.search_no_more:
				getFragmentManager().popBackStack();
				break;
			case R.id.search_enter_singername:
				mEditTextBgForSinger.setBackgroundResource(R.drawable.edittext_enter);
				mEditTextForSinger.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
				((InputMethodManager) getActivity().getSystemService(MusicPlayerActivity.INPUT_METHOD_SERVICE))
						.showSoftInput(v, InputMethodManager.SHOW_FORCED);
				break;
			case R.id.search_enter_singername_second:
				mEditTextBgForSingerSecond.setBackgroundResource(R.drawable.edittext_enter);
				mEditTextForSingerSecond.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
				((InputMethodManager) getActivity().getSystemService(MusicPlayerActivity.INPUT_METHOD_SERVICE))
						.showSoftInput(v, InputMethodManager.SHOW_FORCED);
				break;
			default:
				break;
			}

		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mEditText.setText(mTitle);
		mEditText.setSelection(mTitle.length());
		mEditText.requestFocus();
	}

	//集中处理各组件的焦点变化问题
	class SearchOnFocusChangeListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			switch (v.getId()) {
			case R.id.search_enter_songname:
				if (!hasFocus) {
					mEditTextBg.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
					mEditText.setBackgroundResource(R.drawable.music_imagebutton_selector);
				}
				break;
			case R.id.search_enter_singername:
				if (!hasFocus) {
					mEditTextBgForSinger.setBackgroundColor(getActivity().getResources().getColor(R.color.transparent));
					mEditTextForSinger.setBackgroundResource(R.drawable.music_imagebutton_selector);
				}
				break;
			case R.id.search_enter_singername_second:
				if (!hasFocus) {
					mEditTextBgForSingerSecond.setBackgroundColor(getActivity().getResources().getColor(
							R.color.transparent));
					mEditTextForSingerSecond.setBackgroundResource(R.drawable.music_imagebutton_selector);
				}
				break;
			default:
				break;
			}

		}

	}

	//处理歌曲列表数据的adapter
	class SearchListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mSearchResults.size();
		}

		@Override
		public Object getItem(int position) {
			return mSearchResults.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			SearchViewHolder holder = null;
			if (convertView == null) {

				holder = new SearchViewHolder();
				LayoutInflater mInflater = LayoutInflater.from(getActivity());
				convertView = mInflater.inflate(R.layout.music_searchlist_item, null);

				holder.title = (TextView) convertView.findViewById(R.id.search_result_songname);
				holder.singer = (TextView) convertView.findViewById(R.id.search_result_artist);

				convertView.setTag(holder);
			} else {
				holder = (SearchViewHolder) convertView.getTag();
			}

			holder.title.setText(mSearchResults.get(position).songname);
			holder.singer.setText(mSearchResults.get(position).singer);

			return convertView;
		}

	}

	public final class SearchViewHolder {

		public TextView title;
		public TextView singer;
	}

	//对传入的组件实现抖动的效果
	private void shake(View view) {
		Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
		animation.setFillEnabled(true);
		animation.setFillBefore(true);
		animation.setFillAfter(true);
		view.startAnimation(animation);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCardFlipSwitchCallBack = (OnLyricViewListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement onCardFlipSwitchListener");
		}
	}

}
