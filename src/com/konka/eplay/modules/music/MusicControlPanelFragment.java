package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;
import iapp.eric.utils.custom.model.APIC;
import iapp.eric.utils.metadata.Mp3;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.music.PlayEffectivePopupWindow.OnEffectiveChangeListenner;
import com.konka.eplay.modules.music.PlayModePopupWindow.OnPlayModeContentChangeListenner;

/**
 * @ClassName: MusicControlPanelFragment
 * @Description: 音乐播放器的控制主界面
 * @author xuyunyu
 * @date 2015年3月24日 下午2:19:09
 * @version 1.0
 *
 */
public class MusicControlPanelFragment extends Fragment {


	private static final int SEEKBAR_PROGRESS_INCREMENT = 10;
	private static final int REFRESH_TASK_PERIOD = 200;
	private static final int REFRESH_TASK_DELAY = 1000;
	public final static int SHOW_COVER = 1;
	public final static int REFRESH_SEEKBAR = 2;
	public final static int SHOW_ALBUM_SINGER = 3;

	public final static String MUSIC_SP_FILE = "musicSp";
	public final static String MUSIC_SP_SONGMODE_KEY = "songmode_key";
	public final static String MUSIC_SP_EFFECTIVE_KEY = "songeffective_key";

	private ImageButton mSonglistButton;
	private ImageButton mSerachLyricButton;
	private ImageButton mPreSongButton;
	private ImageButton mNextSongButton;
	private ImageButton mLikeButton;
	private ImageButton mSongMode;
	private Button mSongEffective;
	private OnControlPanelListener mControlPanelListener;
	private ControlButtonListener mButtonListener;
	private ControlFocusChangeListenner mControlFocusChangeListenner;
	private MusicPlayerService mMuiscPlayerService;
	private SeekBar mSeekBar;

	private TextView mSongname;
	private TextView mAlbum;
	private TextView mSinger;
	private TextView mCurrentMusicLength;
	private TextView mTotalMusicLength;
	private TextView mSongEffectiveText;
	private Bitmap mAlbumBitmap;
	private Bitmap mActivityBitmap;
	private String mAlbumName = "";
	private String mSingerName = "";

	private ImageView mAlbumImageView;
	private ImageView mAlbumImageViewPause;

	private PlayModePopupWindow mModePopupWindow;
	private PlayEffectivePopupWindow mEffectivePopupWindow;

	private onLrcChangeListener mLrcChangeListener;

	private List<CommonFileInfo> list;
	private int currentPosition;

	private View root;

	private Timer mTimer = new Timer();
	private TimerTask mTask;

	private int mLastFocusId = -1;
	private boolean mIsCanPause;
	private Rect mThumbRect;
	private Thread writeThread;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case SHOW_COVER:
				if (null != mAlbumBitmap) {
					mAlbumImageView.setImageBitmap(mAlbumBitmap);
				}

				mControlPanelListener.onChangeBackround(mActivityBitmap);

				break;
			case REFRESH_SEEKBAR:
				mSeekBar.setProgress(mMuiscPlayerService.getCurrentPosition());
				mCurrentMusicLength.setText(Utils.formatMusicDuration(mMuiscPlayerService.getCurrentPosition()));
				break;

			case SHOW_ALBUM_SINGER:
				mAlbum.setText(mAlbumName);
				mSinger.setText(mSingerName);
				break;

			default:
				break;
			}
		};
	};

	// Container Activity must implement this interface
	public interface OnControlPanelListener {
		public void onShowSonglistWindow();

		public void onSearchLyric();

		public void onChangeBackround(Bitmap bitmap);
	}

	public interface onLrcChangeListener {
		public void onSeekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mButtonListener = new ControlButtonListener();
		mControlFocusChangeListenner = new ControlFocusChangeListenner();

		//刷新进度条的任务
		mTask = new TimerTask() {

			@Override
			public void run() {
				mHandler.sendEmptyMessage(REFRESH_SEEKBAR);

			}
		};

		mTimer.schedule(mTask, REFRESH_TASK_DELAY, REFRESH_TASK_PERIOD);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		root = inflater.inflate(R.layout.music_controlpanel, container, false);

		initViews();

		return root;
	}

	@Override
	public void onStart() {
		super.onStart();
		//初始化进度条状态，主要是判断歌曲正在播放还是暂停
		if (null!=mMuiscPlayerService&&null != mMuiscPlayerService.getmMediaPlayer()) {
			if ( mMuiscPlayerService.getmMediaPlayer().isPlaying()) {
				mAlbumImageViewPause.setVisibility(View.INVISIBLE);
				mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_play));
			}else {
				mAlbumImageViewPause.setVisibility(View.VISIBLE);
				mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_pause));
			}
		}

	}


	/*
	 * @Description: 初始化view
	 */
	private void initViews() {
		mSonglistButton = (ImageButton) root.findViewById(R.id.music_control_songlist);
		mSerachLyricButton = (ImageButton) root.findViewById(R.id.music_control_search);
		mPreSongButton = (ImageButton) root.findViewById(R.id.music_control_pre);
		mNextSongButton = (ImageButton) root.findViewById(R.id.music_control_next);
		mSongMode = (ImageButton) root.findViewById(R.id.music_control_mode);
		mLikeButton = (ImageButton) root.findViewById(R.id.music_control_islike);
		mSongEffective = (Button) root.findViewById(R.id.music_control_effective);
		mSeekBar = (SeekBar) root.findViewById(R.id.music_control_seekbar);

		mSongname = (TextView) root.findViewById(R.id.music_control_songname);
		mAlbum = (TextView) root.findViewById(R.id.music_control_album);
		mSinger = (TextView) root.findViewById(R.id.music_control_singer);
		mCurrentMusicLength = (TextView) root.findViewById(R.id.current_music_length);
		mTotalMusicLength = (TextView) root.findViewById(R.id.total_music_length);
		mSongEffectiveText = (TextView) root.findViewById(R.id.music_control_effective_text);
		mAlbumImageView = (ImageView) root.findViewById(R.id.music_cover);
		mAlbumImageViewPause = (ImageView) root.findViewById(R.id.music_cover_pause);

		int songmode = Configuration.getInt(MUSIC_SP_FILE, MUSIC_SP_SONGMODE_KEY);
		switch (songmode) {
		case -1:
			mSongMode.setImageResource(R.drawable.music_mode_circle);
			break;
		case MusicPlayerService.MUSIC_SERVICE_PLAYMODE_CIRCLE:
			mSongMode.setImageResource(R.drawable.music_mode_circle);
			break;
		case MusicPlayerService.MUSIC_SERVICE_PLAYMODE_SINGLE:
			mSongMode.setImageResource(R.drawable.music_mode_single);
			break;
		case MusicPlayerService.MUSIC_SERVICE_PLAYMODE_RANDOM:
			mSongMode.setImageResource(R.drawable.music_mode_random);
			break;
		default:
			break;
		}

		int effectiveMode = Configuration.getInt(MUSIC_SP_FILE, MUSIC_SP_EFFECTIVE_KEY);
		switch (effectiveMode) {
		case -1:
			mSongEffectiveText.setText(MusicUtils.getResourceString(getActivity(),R.string.music_effective_nornal));
			break;
		case MusicPlayerService.MUSIC_SERVICE_EFFECTIVE_ORIGINAL:
			mSongEffectiveText.setText(MusicUtils.getResourceString(getActivity(),R.string.music_effective_nornal));
			break;
		case MusicPlayerService.MUSIC_SERVICE_EFFECTIVE_COUNTRYSIDE:
			mSongEffectiveText.setText(MusicUtils.getResourceString(getActivity(),R.string.music_effective_folk));
			break;
		case MusicPlayerService.MUSIC_SERVICE_EFFECTIVE_DANCE:
			mSongEffectiveText.setText(MusicUtils.getResourceString(getActivity(),R.string.music_effective_dance));
			break;
		case MusicPlayerService.MUSIC_SERVICE_EFFECTIVE_MENTAL:
			mSongEffectiveText.setText(MusicUtils.getResourceString(getActivity(),R.string.music_effective_metal));
			break;
		case MusicPlayerService.MUSIC_SERVICE_EFFECTIVE_JAZZ:
			mSongEffectiveText.setText(MusicUtils.getResourceString(getActivity(),R.string.music_effective_jazz));
			break;
		case MusicPlayerService.MUSIC_SERVICE_EFFECTIVE_ROC:
			mSongEffectiveText.setText(MusicUtils.getResourceString(getActivity(),R.string.music_effective_rock));
			break;
		default:
			break;
		}

		mSonglistButton.setOnClickListener(mButtonListener);
		mSerachLyricButton.setOnClickListener(mButtonListener);
		mPreSongButton.setOnClickListener(mButtonListener);
		mNextSongButton.setOnClickListener(mButtonListener);
		mSongMode.setOnClickListener(mButtonListener);
		mLikeButton.setOnClickListener(mButtonListener);
		mSongEffective.setOnClickListener(mButtonListener);
		mSeekBar.setOnClickListener(mButtonListener);
		mAlbumImageViewPause.setOnClickListener(mButtonListener);
		mAlbumImageView.setOnClickListener(mButtonListener);

		mSonglistButton.setOnFocusChangeListener(mControlFocusChangeListenner);
		mSerachLyricButton.setOnFocusChangeListener(mControlFocusChangeListenner);
		mPreSongButton.setOnFocusChangeListener(mControlFocusChangeListenner);
		mNextSongButton.setOnFocusChangeListener(mControlFocusChangeListenner);
		mSongMode.setOnFocusChangeListener(mControlFocusChangeListenner);
		mSongEffective.setOnFocusChangeListener(mControlFocusChangeListenner);

		mSeekBar.setKeyProgressIncrement(SEEKBAR_PROGRESS_INCREMENT);

		mSeekBar.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {

						if (-1 != mLastFocusId) {
							root.findViewById(mLastFocusId).requestFocus();
							return true;
						}
					}
				}
				return false;
			}
		});

		//实现鼠标点击seekbar的thumb的时候的暂停和拖动进度的操作
		mSeekBar.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (mThumbRect == null) {
						mThumbRect = mSeekBar.getThumb().getBounds();
						if (mThumbRect.contains((int) event.getX(), (int) event.getY())) {
							mIsCanPause = true;
						}
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					mIsCanPause = false;
					break;
				case MotionEvent.ACTION_MOVE:
					mIsCanPause = false;
					break;
				case MotionEvent.ACTION_UP:

					if (mIsCanPause) {
						if (mMuiscPlayerService.getmMediaPlayer().isPlaying()) {
							mMuiscPlayerService.pause();
							mAlbumImageViewPause.setVisibility(View.VISIBLE);
							mSeekBar.setThumb(getActivity().getResources().getDrawable(
									R.drawable.player_seekbar_thumb_pause));
						} else {
							mMuiscPlayerService.play();
							mAlbumImageViewPause.setVisibility(View.INVISIBLE);

							mSeekBar.setThumb(getActivity().getResources().getDrawable(
									R.drawable.player_seekbar_thumb_play));
						}
					}
					mThumbRect = null;
					mIsCanPause = false;
					break;
				default:
					mThumbRect = null;
					mIsCanPause = false;
					break;
				}

				return false;
			}
		});

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar paramSeekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar paramSeekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar paramSeekBar, int progress, boolean fromUser) {

				if (null != mLrcChangeListener) {
					mLrcChangeListener.onSeekTo(progress, true, fromUser);
				}
				if (fromUser) {
					if (progress > 0) {
						Trace.Debug("#####pro" + progress);
						mMuiscPlayerService.seekTo(progress);
						mSeekBar.setProgress(progress);
					}
				}

			}
		});

		mSonglistButton.requestFocus();
	}

	public void setFocusDrawable(int rsId,boolean isback){
		if (mSeekBar.isFocused()) {
			if (isback) {
				if (mMuiscPlayerService.getmMediaPlayer().isPlaying()) {
					mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_play));
				}else {
					mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_pause));
				}
			}else {
				if (mMuiscPlayerService.getmMediaPlayer().isPlaying()) {
					mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.seekbar_thumb_click_paly));
				}else {
					mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.seekbar_thumb_click_pause));
				}
			}

			return;
		}
		if (mLikeButton.isFocused()) {
			mLikeButton.setBackgroundResource(rsId);
		}else {
			root.findViewById(mLastFocusId).setBackgroundResource(rsId);
		}
	}

	// 按钮模拟心脏跳动
    private void playHeartbeatAnimation(final View v) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new ScaleAnimation(1.0f, 1.8f, 1.0f, 1.8f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f));
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.4f));

        animationSet.setDuration(200);
        animationSet.setInterpolator(new AccelerateInterpolator());

        animationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            	mLikeButton.setBackgroundResource(R.drawable.listview_selector);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(new ScaleAnimation(1.8f, 1.0f, 1.8f,
                        1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f));
                animationSet.addAnimation(new AlphaAnimation(0.4f, 1.0f));

                animationSet.setDuration(200);
                animationSet.setInterpolator(new DecelerateInterpolator());

                animationSet.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mLikeButton.setBackgroundResource(R.drawable.music_imagebutton_selector);
					}
				});
               // 实现心跳的View
                v.startAnimation(animationSet);

            }
        });
                // 实现心跳的View
        v.startAnimation(animationSet);
    }


	class ControlButtonListener implements OnClickListener {

		/**
		* @Fields MODE_POPUP_OFFEST_Y : TODO(用一句话描述这个变量表示什么)
		*/
		private static final int MODE_POPUP_OFFEST_Y = -13;

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.music_control_pre:
				mMuiscPlayerService.previous(true);
				break;
			case R.id.music_control_next:
				mMuiscPlayerService.next(true);
				Trace.Info("music next");
				break;
			case R.id.music_control_search:
				mSerachLyricButton.setBackgroundResource(R.drawable.focus_clicked_small);
				mControlPanelListener.onSearchLyric();
				break;
			case R.id.music_control_songlist:
				mSonglistButton.setBackgroundResource(R.drawable.focus_clicked_small);
				mControlPanelListener.onShowSonglistWindow();
				break;
			case R.id.music_control_islike:
				if (null != list && list.get(currentPosition).getIsLike()) {
					mLikeButton.setImageResource(R.drawable.musicplayer_islike_no);
					list.get(currentPosition).setIsLike(false);
					MusicActivity.getLikeList().remove(list.get(currentPosition));
				} else {
					playHeartbeatAnimation(mLikeButton);
					mLikeButton.setImageResource(R.drawable.musicplayer_islike_yes);
					list.get(currentPosition).setIsLike(true);
					MusicActivity.getLikeList().add(list.get(currentPosition));
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
						ContentManager.writeData2DB(getActivity(), MusicActivity.getLikeList(), MultimediaType.MMT_LIKEMUSIC);
					}
				});
				writeThread.start();
				break;
			case R.id.music_control_mode:
				mSongMode.setBackgroundResource(R.drawable.focus_clicked_small);
				if (null == mModePopupWindow) {
					mModePopupWindow = new PlayModePopupWindow(getActivity());

					mModePopupWindow.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss() {
							root.findViewById(R.id.music_control_mode).setBackgroundResource(
									R.drawable.music_imagebutton_selector);

						}
					});
					mModePopupWindow.setModeContentChangeListenner(new OnPlayModeContentChangeListenner() {

						@Override
						public void onChangeContent(int rsID, int playmode) {

							mSongMode.setImageResource(rsID);
							mMuiscPlayerService.setPlayMode(playmode);
							Configuration.save(MUSIC_SP_FILE, MUSIC_SP_SONGMODE_KEY, playmode);

						}

						@Override
						public void onChangeFocus(boolean isRight) {
							if (isRight) {
								mSerachLyricButton.requestFocus();
							} else {
								mSonglistButton.requestFocus();
							}

						}
					});

					mModePopupWindow.showAsDropDown(mSongMode, -mSongMode.getWidth() * 3 / 4, MODE_POPUP_OFFEST_Y);

				} else {
					mModePopupWindow.showAsDropDown(mSongMode, -mSongMode.getWidth() * 3 / 4, MODE_POPUP_OFFEST_Y);
				}

				break;
			case R.id.music_cover_pause:
			case R.id.music_cover:
			case R.id.music_control_seekbar:
				if (mMuiscPlayerService.getmMediaPlayer().isPlaying()) {
					mMuiscPlayerService.pause();
					// TODO 封面变化
					mAlbumImageViewPause.setVisibility(View.VISIBLE);

					mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_pause));
				} else {
					mMuiscPlayerService.play();
					mAlbumImageViewPause.setVisibility(View.INVISIBLE);

					mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_play));

				}

				break;

			case R.id.music_control_effective:
				mSongEffective.setBackgroundResource(R.drawable.button_press_focus);

				if (null == mEffectivePopupWindow) {
					mEffectivePopupWindow = new PlayEffectivePopupWindow(getActivity());

					mEffectivePopupWindow.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss() {
							root.findViewById(R.id.music_control_effective).setBackgroundResource(
									R.drawable.music_imagebutton_selector);

						}
					});

					mEffectivePopupWindow.setmChangeListenner(new OnEffectiveChangeListenner() {

						@Override
						public void onEffectiveChange(String effective, short preset) {
							mSongEffectiveText.setText(effective);
							mMuiscPlayerService.switchEffective(preset);
							Configuration.save(MUSIC_SP_FILE, MUSIC_SP_EFFECTIVE_KEY, preset);
						}

						@Override
						public void onChangeFocus(boolean isRight) {
							if (isRight) {
								View view = getActivity().findViewById(R.id.music_search_button);
								if (view.isShown()) {
									view.requestFocus();
								}
							} else {
								mSerachLyricButton.requestFocus();
							}

						}

					});

					mEffectivePopupWindow.showAsDropDown(mSongEffectiveText, -mSongEffectiveText.getWidth() * 1 / 2, 0);

				} else {
					mEffectivePopupWindow.showAsDropDown(mSongEffectiveText, -mSongEffectiveText.getWidth() * 1 / 2, 0);
				}
				break;

			default:
				break;
			}

		}

	}

	class ControlFocusChangeListenner implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {

			if (hasFocus) {
				mLastFocusId = v.getId();
			}

		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mControlPanelListener = (OnControlPanelListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement onFlyingLayoutListener");
		}
	}

	public void setmMuiscPlayerService(MusicPlayerService mMuiscPlayerService) {
		this.mMuiscPlayerService = mMuiscPlayerService;
	}

	public void setOnLrcChangeListener(onLrcChangeListener mChangeListener) {
		mLrcChangeListener = mChangeListener;
	}

	public void refreshUI() {
		list = mMuiscPlayerService.getmMusicList();
		// 空指针处理
		if (list == null&&list.size()<=0) {
			return;
		}
		currentPosition = mMuiscPlayerService.getmCurrentListPosition();
		String name = list.get(currentPosition).getName();
		if (list.get(currentPosition).getIsLike()) {
			mLikeButton.setImageResource(R.drawable.musicplayer_islike_yes);
		} else {
			mLikeButton.setImageResource(R.drawable.musicplayer_islike_no);
		}
		mSongname.setText(name.substring(0, name.length() - 4));
		// mSinger.setText(list.get(currentPosition).getSinger());
		if (mMuiscPlayerService.getmMediaPlayer().isPlaying()) {
			mAlbumImageViewPause.setVisibility(View.INVISIBLE);
			mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_play));

		} else {
			mAlbumImageViewPause.setVisibility(View.VISIBLE);

			mSeekBar.setThumb(getActivity().getResources().getDrawable(R.drawable.player_seekbar_thumb_pause));
		}

		mSeekBar.setProgress(0);
		mSeekBar.setMax(mMuiscPlayerService.getmMediaPlayer().getDuration());
		mCurrentMusicLength.setText("00:00");

		mTotalMusicLength.setText(Utils.formatMusicDuration(mMuiscPlayerService.getmMediaPlayer().getDuration()));

		new Thread(new Runnable() {

			@Override
			public void run() {
				getCover(list, currentPosition);

			}
		}).start();

		CommonFileInfo file = list.get(currentPosition);
		if (file.getSpecial().trim().isEmpty() || file.getSinger().trim().isEmpty()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					String[] result = MusicUtils.getSingleSongInfo(list.get(currentPosition).getPath());

					mAlbumName = result[0];
					mSingerName = result[1];
					mHandler.sendEmptyMessage(SHOW_ALBUM_SINGER);
				}
			}).start();
		} else {
			mAlbumName = file.getSpecial();
			mSingerName = file.getSinger();
			mHandler.sendEmptyMessage(SHOW_ALBUM_SINGER);
		}

	}

	//获取专辑封面图
	private void getCover(List<CommonFileInfo> mList, int position) {
		mAlbumBitmap = null;

		String url = "";
		if (mList.isEmpty()) {
			return;
		}
		url = mList.get(position).getPath();
		Trace.Info("###lhq 传入MP3类的本地路径为 -->" + mList.get(position).getPath());
		Mp3 m = null;
		try {
			m = new Mp3(url, false, true);
		} catch (Exception e) {
			e.printStackTrace();
			mAlbumBitmap = getBigerBitmap(null);
			Bitmap default_bg = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_music_player_bg);
			mActivityBitmap = MusicUtils.blurBackgroundImage(getActivity(), default_bg,24);
			mHandler.sendEmptyMessage(SHOW_COVER);
			return;
		} finally {
			if (null == m) {
				mAlbumBitmap = getBigerBitmap(null);
				Bitmap default_bg = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_music_player_bg);
				mActivityBitmap = MusicUtils.blurBackgroundImage(getActivity(), default_bg,24);
				mHandler.sendEmptyMessage(SHOW_COVER);
				return;
			}
		}

		if (null != m.getTagID3V2()) {
			Trace.Debug("####ID3V2不为空");
			if (m.getTagID3V2().getTagHeader().equals("ID3")) {
				Trace.Debug("获取到了DI3V2标签中的数据");
				if (null != m.getTagID3V2().getTagFrame().get("APIC")) {
					APIC apic = (APIC) (m.getTagID3V2().getTagFrame().get("APIC").getContent());
					if (null != apic) {
						if (apic.pictureData != null) {
							Trace.Debug("music cover byte[] is not null length-->" + apic.pictureData.length);
							byte[] buffer = apic.pictureData;

							Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
							if (null != bm) {
								Trace.Debug("### 音乐专辑bitmap不为 空");
								bm.setDensity(Utils.getDensityDpi(getActivity()));
								mAlbumBitmap = getBigerBitmap(bm);
								mActivityBitmap = MusicUtils.blurBackgroundImage(getActivity(), bm,24);
								mHandler.sendEmptyMessage(SHOW_COVER);
								return;
							}
						} else if (apic.url != null) {
							Trace.Debug("music cover url -->" + apic.url);
						}
					}
				}
			}
		}

		mAlbumBitmap = getBigerBitmap(null);
		Bitmap default_bg = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_music_player_bg);
		mActivityBitmap = MusicUtils.blurBackgroundImage(getActivity(), default_bg,24);
		mHandler.sendEmptyMessage(SHOW_COVER);

	}

	private Bitmap getBigerBitmap(Bitmap zhuanji) {
		int newWidth = Utils.dip2px(getActivity(), 353.33f);
		int newHeight =  Utils.dip2px(getActivity(), 353.33f);
		Bitmap newBitmap = null;

		if (null != zhuanji) {
			float width = zhuanji.getWidth();
			float height = zhuanji.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;
			matrix.postScale(scaleWidth, scaleHeight);
			newBitmap = Bitmap.createBitmap(zhuanji, 0, 0, (int) width, (int) height, matrix, true);
		}

		Bitmap bitmap = null;
		bitmap = Bitmap.createBitmap(Utils.dip2px(getActivity(), 354), Utils.dip2px(getActivity(), 354),
				Config.ARGB_8888);
		bitmap.setDensity(Utils.getDensityDpi(getActivity()));
		Canvas canvas = new Canvas(bitmap);

		if (null != newBitmap) {
			Trace.Debug("专辑图片bitmap不为空");
			canvas.drawBitmap(newBitmap, 0, 0, null);

		} else {
			Bitmap bg_default = BitmapFactory.decodeResource(getResources(), R.drawable.m);
			canvas.drawBitmap(bg_default, 0, 0, null);
		}
		return bitmap;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mTimer != null) {
			mTimer.cancel();
			mTask = null;
		}
	}

}
