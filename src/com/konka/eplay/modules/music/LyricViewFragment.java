package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Audio;
import iapp.eric.utils.base.Trace;
import iapp.eric.utils.metadata.Lyric;
import iapp.eric.utils.metadata.LyricInfo;
import iapp.eric.utils.metadata.LyricSentence;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.music.MusicControlPanelFragment.onLrcChangeListener;
import com.konka.eplay.modules.music.SearchLyricFragment.onSearchBackListaenner;
import com.konka.eplay.modules.music.lyric.LrcRow;
import com.konka.eplay.modules.music.lyric.LrcView;

/**
 * @ClassName: LyricViewFragment
 * @Description: 歌词展示的主界面
 * @author xuyunyu
 * @date 2015年3月24日 下午2:19:43
 * @version 1.0
 *
 */
public class LyricViewFragment extends Fragment {

	public final static int LYRICS_SHOWING = 1;
	public final static int LYRICS_NO_SHOW_TIPS = 2;
	public final static int LYRICS_DOWNLOADING = 4;
	public final static int LYRICS_SEARCH_NETWORK_DISCONNECTED = 5;

	public final static String ARGUMENT_IS_FROMBACKSTACK = "backstack";
	public final static String ARGUMENT_IS_CLICKED = "click";
	public final static String ARGUMENT_IS_SEARCH_BY_USER = "user_search";

	private boolean mButton_is_click = false;

	private OnLyricViewListener mCardFlipSwitchCallBack;
	private Button mSearchButton;
	private View root;
	private ViewGroup mLyricShowLayout;
	private ViewGroup mLyricNoLayout;
	private ViewGroup mLyricDownloadingLayout;

	private ImageView mDownloadingImageView;
	private TextView mDownloadingTextView;
	private TextView mDownloadingTips;

	private TextView mNoLyricTitle;
	private RelativeLayout mNoLyricContent;
	private TextView mNoNetworkContent;
	private FrameLayout mNoLyricButton;

	private LrcView mLrcView;
	private MusicPlayerService mMuiscPlayerService;

	private List<CommonFileInfo> mMuiscList;
	private int mCurrentPosition;

	private Lyric mLyricFromUser;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (mLrcView.getTag() == null) {
				return;
			}

			if (msg.what == LYRICS_NO_SHOW_TIPS) {
				LyricResult result = (LyricResult) msg.obj;
				if (mLrcView.getTag().equals(result.LRCtag)) {
					mLrcView.reset();

					if (null != mDownloadingImageView && mDownloadingImageView.getBackground() != null
							&& mDownloadingImageView.getBackground() instanceof AnimationDrawable) {
						AnimationDrawable anim = (AnimationDrawable) mDownloadingImageView.getBackground();
						if (anim != null && anim.isRunning()) { // 如果正在运行,就停止
							anim.stop();
						}
					}
					mLyricShowLayout.setVisibility(View.GONE);
					mSearchButton.setVisibility(View.GONE);
					mLyricNoLayout.setVisibility(View.VISIBLE);
					mNoLyricButton.setFocusable(true);
					mNoLyricButton.setClickable(true);
					// mNoLyricButton.requestFocus();
					mNoLyricTitle.setText(MusicUtils.getResourceString(getActivity(), R.string.music_no_lyric_tips));
					mNoLyricContent.setVisibility(View.VISIBLE);
					mNoNetworkContent.setVisibility(View.INVISIBLE);
					mLyricDownloadingLayout.setVisibility(View.GONE);

				}
				Trace.Info("歌词下载失败!");
			} else if (msg.what == LYRICS_DOWNLOADING) {

				Trace.Info("downloading");
				LyricResult result = (LyricResult) msg.obj;
				if (mLrcView.getTag().equals(result.LRCtag)) {
					mLrcView.reset();
					mLyricShowLayout.setVisibility(View.GONE);
					mSearchButton.setVisibility(View.GONE);
					mLyricNoLayout.setVisibility(View.GONE);
					mLyricDownloadingLayout.setVisibility(View.VISIBLE);

					AnimationDrawable anim = (AnimationDrawable) mDownloadingImageView.getBackground();
					anim.start();
				}
			} else if (msg.what == LYRICS_SHOWING) {
				LyricResult result = (LyricResult) msg.obj;
				if (mLrcView.getTag().equals(result.LRCtag)) {

					if (null != mDownloadingImageView && mDownloadingImageView.getBackground() != null
							&& mDownloadingImageView.getBackground() instanceof AnimationDrawable) {
						AnimationDrawable anim = (AnimationDrawable) mDownloadingImageView.getBackground();
						if (anim != null && anim.isRunning()) { // 如果正在运行,就停止
							anim.stop();
						}
					}
					mLyricShowLayout.setVisibility(View.VISIBLE);
					mSearchButton.setVisibility(View.VISIBLE);
					mLyricNoLayout.setVisibility(View.GONE);
					mLyricDownloadingLayout.setVisibility(View.GONE);
					mLrcView.setLrcRows(getLrcRows(result.filepath));
					mLrcView.seekTo(mMuiscPlayerService.getCurrentPosition(), false, false);
					Trace.Info("歌词下载完成!");
				}
			} else if (msg.what == LYRICS_SEARCH_NETWORK_DISCONNECTED) {

				LyricResult result = (LyricResult) msg.obj;
				if (mLrcView.getTag().equals(result.LRCtag)) {
					mLrcView.reset();

					if (null != mDownloadingImageView && mDownloadingImageView.getBackground() != null
							&& mDownloadingImageView.getBackground() instanceof AnimationDrawable) {
						AnimationDrawable anim = (AnimationDrawable) mDownloadingImageView.getBackground();
						if (anim != null && anim.isRunning()) { // 如果正在运行,就停止
							anim.stop();
						}
					}
					mLyricShowLayout.setVisibility(View.GONE);
					mSearchButton.setVisibility(View.GONE);
					mLyricNoLayout.setVisibility(View.VISIBLE);
					mNoLyricButton.setFocusable(false);
					mNoLyricButton.setClickable(false);
					mNoLyricTitle.setText(MusicUtils.getResourceString(getActivity(),
							R.string.music_tips_network_unavaliable));
					mNoLyricContent.setVisibility(View.INVISIBLE);
					mNoNetworkContent.setVisibility(View.VISIBLE);
					// mLyricNoNetwork.setVisibility(View.VISIBLE);
					mLyricDownloadingLayout.setVisibility(View.GONE);

				}
				Trace.Info("歌词下载失败!");

			}
		};
	};

	// Container Activity must implement this interface
	public interface OnLyricViewListener {
		public void onCardFlip();

		public void onFocusSearchButton();
	}

	public LyricViewFragment() {
		setArguments(new Bundle());

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Trace.Info("lyricview oncreate");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Trace.Info("lyricview onCreateView");
		root = inflater.inflate(R.layout.music_lrcview, container, false);
		// root.setBackgroundColor(Color.CYAN);

		mLrcView = (LrcView) root.findViewById(R.id.music_lyricview);
		mLyricShowLayout = (RelativeLayout) root.findViewById(R.id.lyric_show_layout);
		mLyricNoLayout = (RelativeLayout) root.findViewById(R.id.no_lyric_layout);
		mLyricDownloadingLayout = (RelativeLayout) root.findViewById(R.id.lyric_downloading);
		mDownloadingImageView = (ImageView) root.findViewById(R.id.loading_image);
		mDownloadingTextView = (TextView) root.findViewById(R.id.loading_text);
		mDownloadingTips = (TextView) root.findViewById(R.id.lyricview_text);

		mNoLyricTitle = (TextView) root.findViewById(R.id.no_lyric_tip_title);
		mNoLyricContent = (RelativeLayout) root.findViewById(R.id.no_lyric_tip_content);
		mNoNetworkContent = (TextView) root.findViewById(R.id.no_network_tip_content);
		mNoLyricButton = (FrameLayout) root.findViewById(R.id.download_no_lyric);

		mDownloadingImageView.setBackgroundResource(R.drawable.downloading_anim);

		mSearchButton = (Button) root.findViewById(R.id.music_search_button);
		mSearchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				mButton_is_click = true;
				mCardFlipSwitchCallBack.onCardFlip();
			}
		});

		mNoLyricButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mButton_is_click = true;
				mCardFlipSwitchCallBack.onCardFlip();

			}
		});

		mSearchButton.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mDownloadingTips.setTextColor(getActivity().getResources().getColor(
							R.color.songlist_unselected_title));
				} else {
					mDownloadingTips.setTextColor(getActivity().getResources().getColor(R.color.lyricview_tips_text));
				}

			}
		});

		Bundle bundle = getArguments();
		if (bundle.containsKey(ARGUMENT_IS_FROMBACKSTACK)) {
			if (bundle.getBoolean(ARGUMENT_IS_FROMBACKSTACK)) {
				if (!bundle.containsKey(ARGUMENT_IS_SEARCH_BY_USER) || !bundle.getBoolean(ARGUMENT_IS_SEARCH_BY_USER)) {
					Trace.Info("重新加载歌词====");
					refreshUI();
					bundle.remove(ARGUMENT_IS_FROMBACKSTACK);
					bundle.remove(ARGUMENT_IS_SEARCH_BY_USER);
				} else {

					if (null != mLyricFromUser) {
						downLoadByUser(mLyricFromUser);
					}

				}

			}
		}
		if (bundle.containsKey(ARGUMENT_IS_SEARCH_BY_USER)) {
			bundle.remove(ARGUMENT_IS_SEARCH_BY_USER);
		}

		if (bundle.containsKey(ARGUMENT_IS_CLICKED)) {
			if (bundle.getBoolean(ARGUMENT_IS_CLICKED)) {
				if (mSearchButton.isShown()) {
					mSearchButton.requestFocus();
				} else {
					mCardFlipSwitchCallBack.onFocusSearchButton();
				}

				bundle.remove(ARGUMENT_IS_CLICKED);
			} else {
				mCardFlipSwitchCallBack.onFocusSearchButton();
			}
		}

		// mButton.requestFocus();

		return root;
	}

	@Override
	public void onPause() {
		super.onPause();
		Trace.Info("lyricview onPause");

		getArguments().putBoolean(ARGUMENT_IS_FROMBACKSTACK, true);
		getArguments().putBoolean(ARGUMENT_IS_CLICKED, mButton_is_click);
	}

	@Override
	public void onStop() {
		super.onStop();
		Trace.Info("lyricview onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Trace.Info("lyricview onDestroy");

	}

	public boolean setFocusDrawable(int rsId) {
		if (mSearchButton.isFocused()) {
			mSearchButton.setBackgroundResource(rsId);
			return true;
		}
		return false;
	}

	private List<LrcRow> getLrcRows(String filepath) {
		Trace.Info("getLrcRows");
		List<LrcRow> rows = null;
		ArrayList<LyricSentence> lyricSentences;

		Audio audio = new Audio();
		LyricInfo lyricInfo = audio.parseLyric(filepath);
		if (null != lyricInfo) {
			lyricSentences = lyricInfo.lyric;
		} else {
			return null;
		}

		if (null != lyricSentences) {
			rows = new ArrayList<LrcRow>();
			LrcRow lrcRow;
			LyricSentence sentence;
			for (int i = 0; i < lyricSentences.size(); i++) {

				lrcRow = new LrcRow();
				sentence = lyricSentences.get(i);

				Trace.Debug("startTime=" + sentence.startTime + "  endtime=" + sentence.endTime + " holdtime="
						+ sentence.holdTime + "  content=" + sentence.content);
				lrcRow.setTime((int) (sentence.startTime));
				lrcRow.setContent(sentence.content);
				lrcRow.setTotalTime((int) (sentence.holdTime));
				rows.add(lrcRow);
			}

			Trace.Info("" + rows.size());
			return rows;
		} else {
			return null;
		}

		// // InputStream is = getResources().openRawResource(R.raw.hs);
		// InputStream is = null;
		// try {
		// is = new FileInputStream(filepath);
		// } catch (FileNotFoundException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		//
		// // TODO 判断 is 是否为null
		// BufferedReader br = new BufferedReader(new InputStreamReader(is));
		// String line;
		// StringBuffer sb = new StringBuffer();
		// try {
		// while ((line = br.readLine()) != null) {
		//
		// // Trace.Info("row======" + sb.toString());
		// sb.append(line + "\n");
		// }
		// // System.out.println(sb.toString());
		// // Trace.Info("row======" + sb.toString());
		// rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
		// } catch (IOException e) {
		// e.printStackTrace();
		// } finally {
		// try {
		// br.close();
		// is.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// /return rows;
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

	public void setmMuiscPlayerService(MusicPlayerService mMuiscPlayerService) {
		this.mMuiscPlayerService = mMuiscPlayerService;
	}

	public void setSearchFragment(SearchLyricFragment searchLyricFragment) {

		searchLyricFragment.setSearchListenner(new onSearchBackListaenner() {

			@Override
			public void onSearchLyric(final Lyric lyric) {

				// downLoadByUser(lyric);
				mLyricFromUser = lyric;

			}

		});
	}

	/**
	 * @Title: downLoadByUser
	 * @Description: 用户选择的下载方式
	 * @param lyric
	 */
	private void downLoadByUser(final Lyric lyric) {
		Trace.Info("search url===" + lyric.getLrcUrl());

		new Thread(new Runnable() {

			@Override
			public void run() {

				mMuiscList = mMuiscPlayerService.getmMusicList();
				mCurrentPosition = mMuiscPlayerService.getmCurrentListPosition();
				CommonFileInfo fileInfo = mMuiscList.get(mCurrentPosition);
				String tag = fileInfo.getPath();
				mLrcView.setTag(tag);

				Message msg = Message.obtain();
				msg.what = LYRICS_DOWNLOADING;
				LyricResult re = new LyricResult();
				re.LRCtag = tag;
				msg.obj = re;
				mHandler.sendMessage(msg);

				MusicUtils.downloadLyricByUser(getActivity(), mHandler, lyric, fileInfo.getTitle(), tag);

			}
		}).start();
	}

	public void setContronlPanel(MusicControlPanelFragment musicControlPanelFragment) {
		musicControlPanelFragment.setOnLrcChangeListener(new onLrcChangeListener() {

			@Override
			public void onSeekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser) {
				mLrcView.seekTo(progress, fromSeekBar, fromSeekBarByUser);

			}
		});
	}

	public void refreshUI() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				mMuiscList = mMuiscPlayerService.getmMusicList();
				if (mMuiscList == null) {
					return;
				}
				mCurrentPosition = mMuiscPlayerService.getmCurrentListPosition();
				CommonFileInfo fileInfo = mMuiscList.get(mCurrentPosition);
				String url = fileInfo.getPath();
				// 给lrcview设置tag 很重要
				mLrcView.setTag(url);

				Message msg = Message.obtain();
				msg.what = LYRICS_DOWNLOADING;
				LyricResult re = new LyricResult();
				re.LRCtag = url;
				msg.obj = re;
				mHandler.sendMessage(msg);

				if (fileInfo.getTitle().trim().isEmpty() || fileInfo.getSinger().trim().isEmpty()) {
					String[] result = MusicUtils.getSingleSongInfo(url);

					if (result[2].equals(MusicUtils.getResourceString(getActivity(), R.string.music_unknown_songname))
							|| result[1].equals(MusicUtils.getResourceString(getActivity(),
									R.string.music_unknown_artist))) {

						Message message = Message.obtain();
						message.what = LYRICS_NO_SHOW_TIPS;
						LyricResult result_no = new LyricResult();
						result_no.LRCtag = url;
						message.obj = result_no;
						mHandler.sendMessage(message);
						return;
					}
					Trace.Info("歌手歌名为空 重新搜索");
					MusicUtils.downloadLyric(getActivity(), result[2], result[1], mHandler, url);

				} else {
					if (fileInfo.getTitle().trim()
							.equals(MusicUtils.getResourceString(getActivity(), R.string.music_unknown_songname))
							|| fileInfo.getSinger().trim()
									.equals(MusicUtils.getResourceString(getActivity(), R.string.music_unknown_artist))) {

						Message message = Message.obtain();
						message.what = LYRICS_NO_SHOW_TIPS;
						LyricResult result = new LyricResult();
						result.LRCtag = url;
						message.obj = result;
						mHandler.sendMessage(message);
						return;
					} else {
						MusicUtils.downloadLyric(getActivity(), fileInfo.getTitle(), fileInfo.getSinger(), mHandler,
								url);
					}

				}

			}
		}).start();
	}

}
