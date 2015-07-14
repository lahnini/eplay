package com.konka.eplay.modules.photo.music;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.music.MusicActivity;
import com.konka.eplay.modules.music.MusicBaseAdapter;
import com.konka.eplay.modules.music.MusicListView;
import com.konka.eplay.modules.music.MusicPlayerActivity;
import com.konka.eplay.modules.music.MusicPlayerService;
import com.konka.eplay.modules.music.VisualizerView;
import com.konka.facerecognition.IFaceRecognitionService;

public class MusicEntryInPhotoDialog extends Dialog {

	private static final int LISTVIEW_SELECTION = 0;
	private MusicListView mListView;
	private Context mContext;
	private View mContentView;
	private List<CommonFileInfo> mMusicFiles;
	private MusicPlayerService mPlayerService;
	private MusicDialogAdapter mDialogAdapter;
	private boolean mIsClicked = false;

	public MusicEntryInPhotoDialog(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public MusicEntryInPhotoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		mContext = context;
		init();
	}

	public MusicEntryInPhotoDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		init();
	}

	public void onEventMainThread(IEvent event) {
		if (event instanceof EventMusicStateChange) {

			switch (((EventMusicStateChange) event).musicStateType) {
			case LISTVIEW_SELECTION:
				if (null!=mListView) {
					mListView.setSelection(mPosition);
				}
				break;
			case Constant.MUSIC_SERVICE_FLAG_CHANGE_SONG:
				refreshMusicData();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PLAY:
				refreshMusicData();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PAUSE:
				refreshMusicData();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_STOP:
				dismiss();
				break;
			}

//			if (((EventMusicStateChange) event).musicStateType==0) {
//				if (null!=mListView) {
//					mListView.setSelection(mPosition);
//				}
//			}else {
//				refreshMusicData();
//			}

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = LayoutInflater.from(mContext);
		mContentView = inflater.inflate(R.layout.dialog_music_entry_in_photo, null);
		mListView = (MusicListView) mContentView.findViewById(R.id.listView_in_music_entry_photo);
		setContentView(mContentView);

		Window dialogWindow = this.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
		lp.x = 215;
		lp.y = 143;
		lp.height = 716;
		lp.width = 1478;
		dialogWindow.setAttributes(lp);

		// 初始化列表
		mDialogAdapter = new MusicDialogAdapter(mMusicFiles);
		mListView.setAdapter(mDialogAdapter);

		mIsClicked = false;
		getRightPosition();
		if (mPlayerService != null) {
			mListView.setSelectionFromTop(mPlayerService.getmCurrentListPosition(), mListView.getDividerHeight());
		}
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

				// add by mcsheng
				mMusicFiles.get(position).recentPlayTime = System.currentTimeMillis();
				Trace.Debug("####onItemClick recentPlayTime is " + mMusicFiles.get(position).recentPlayTime);


				if (null != mPlayerService) {
					if (null == mPlayerService.getMusciPath()) {
						mPlayerService.playSongList(mMusicFiles, position);
						mIsClicked = true;
					} else if (mPlayerService.getMusciPath().equals(mMusicFiles.get(position).getPath())
							&& null != mPlayerService.getmMediaPlayer()
							&& !mPlayerService.getmMediaPlayer().isPlaying()) {
						mPlayerService.play();
					} else if (mPlayerService.getMusciPath().equals(mMusicFiles.get(position).getPath())
							&& null != mPlayerService.getmMediaPlayer() && mPlayerService.getmMediaPlayer().isPlaying()) {
						mPlayerService.pause();
					} else if (!mPlayerService.getMusciPath().equals(mMusicFiles.get(position).getPath())) {
						mPlayerService.playSongList(mMusicFiles, position);
						mIsClicked = true;
					}
				}

				mDialogAdapter.notifyDataSetChanged();
				final Handler mHandler = new Handler();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mListView.getLastVisiblePosition() == mListView.getSelectedItemPosition()
								&& MusicDialogAdapter.TYPE_AUDIO == mListView.getAdapter().getItemViewType(
										mListView.getSelectedItemPosition())) {
							mListView.setSelectionFromTop(mListView.getSelectedItemPosition(), mListView.getHeight()
									- mListView.getSelectedView().getHeight() - mListView.getDividerHeight() * 2);

						}
					}
				}, 20);

			}
		});
	}

	@Override
	public void show() {

		EventDispatchCenter.getInstance().register(this);

//		if (null!=mPlayerService&&null != mPlayerService.getmMusicList()
//				&& (mMusicFiles.size() == mPlayerService.getmMusicList().size())) {
//			if (mPlayerService != null && mListView != null) {
//				mListView.setSelection(mPlayerService.getmCurrentListPosition());
//			}
//		}else {
			getRightPosition();
//		}
		super.show();
	}

	private void init() {
		if (mMusicFiles == null) {
			mMusicFiles = new ArrayList<CommonFileInfo>();
		}
		mMusicFiles.addAll(MusicActivity.sAllList);

		mPlayerService = MusicPlayerService.getInstance();

		// add by mcsheng
		sortList();
	}

	/*
	 * @Description: 后台切换歌曲的时候，当music dialog在显示的时候及时刷新
	 */
	public void refreshMusicData() {

		if (null != mDialogAdapter) {
			mDialogAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void dismiss() {

		EventDispatchCenter.getInstance().unregister(this);
		// add by mcsheng
		sortList();

		super.dismiss();
	}

	class MusicDialogAdapter extends MusicBaseAdapter {

		public static final int TYPE_NORMAL = 0;
		public static final int TYPE_AUDIO = 1;
		private LayoutInflater mInflater;

		public MusicDialogAdapter(List<CommonFileInfo> fileList) {
			super(fileList);
			mInflater = LayoutInflater.from(mContext);
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
						mPlayerService.setVisualizerView(holderForAudio.visualizerView);
						// mVisualizerView = holderForAudio.visualizerView;
					}
				}
				break;

			default:
				break;
			}

			return convertView;
		}

	}

	private final class ViewHolderForAudio {
		public TextView title;
		public TextView singer;
		public TextView time;
		public TextView state;
		public VisualizerView visualizerView;
		public ImageView islike;
		public boolean needInflate;
	}

	private final class ViewHolder {

		public TextView title;
		public TextView singer;
		public TextView time;
		public ImageView islike;
		public boolean needInflate;
	}

	// 从大到小排序 add by mcsheng
	private class RecentPlayTimeComparator implements Comparator<Object> {

		@Override
		public int compare(Object paramT1, Object paramT2) {

			CommonFileInfo fileInfo1 = ((CommonFileInfo) paramT1);
			CommonFileInfo fileInfo2 = ((CommonFileInfo) paramT2);

			long a = fileInfo1.recentPlayTime;

			long b = fileInfo2.recentPlayTime;

			if (a - b > 0) {
				return -1;
			} else if (a - b < 0) {
				return 1;
			} else {
				return 0;
			}
		}

	}
    private int mPosition;
	private void getRightPosition() {
		Trace.Info("getRightPosition");
		mPosition = 0;
		if (mMusicFiles.size() == 0||mPlayerService==null||mPlayerService.getmMusicList()==null||mListView==null) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (mMusicFiles.size()<=0) {
					mPosition = 0;
					return;
				}
				int index = mMusicFiles.indexOf(mPlayerService.getmMusicList().get(mPlayerService.getmCurrentListPosition()));
				if (-1!=index) {
					mPosition = index;
					EventMusicStateChange event = new EventMusicStateChange();
					event.musicStateType = LISTVIEW_SELECTION;
					EventDispatchCenter.getInstance().post(event);
				}

			}
		}).start();
	}

	// 列表排序 add by mcsheng
	private void sortList() {

		// add by xuyunyu
		if (mMusicFiles.size() == 0) {
			return;
		}
		List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();

		CommonFileInfo preCurrentFileInfo = null;

		// 判断服务是否为空，以避免在init()中使用时出错
		if (mPlayerService != null) {
			preCurrentFileInfo = mMusicFiles.get(mPlayerService.getmCurrentListPosition());
		}

		for (CommonFileInfo fileInfo : mMusicFiles) {
			if (fileInfo.getIsLike()) {
				list.add(fileInfo);
			}
		}

		mMusicFiles.removeAll(list);

		RecentPlayTimeComparator comp = new RecentPlayTimeComparator();
		// Collections.sort(list, comp);
		Collections.sort(mMusicFiles, comp);

		mMusicFiles.addAll(0, list);

		// 定位新的位置
		if (preCurrentFileInfo != null && null != mPlayerService.getmMusicList()
				&& (mMusicFiles.size() == mPlayerService.getmMusicList().size())&&mIsClicked) {
			if (preCurrentFileInfo.getIsLike()) {
				mPlayerService.setCurrentPosition(0);
			} else {
				mPlayerService.setCurrentPosition(list.size());
			}
		}

	}

}