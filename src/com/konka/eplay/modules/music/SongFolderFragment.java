/**
 * @Title: SongFolderFragment.java
 * @Package com.konka.eplay.modules.music
 * @date 2015年4月30日 下午3:49:45
 * @version 1.0
 */
package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.modules.AlwaysMarqueeTextView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.ScrollGridView;
import com.konka.eplay.modules.music.thumnail.MusicThumnailLoader;

/**
 * @ClassName: SongFolderFragment
 * @Description: 音乐浏览的文件夹模式的主界面
 * @author xuyunyu
 * @date 2015年4月30日 下午3:49:45
 *
 */
public class SongFolderFragment extends Fragment implements OnScrollListener {

	public static final int START_GET_THUMNAIL = 12;
	private View root;
	private ScrollGridView mGridView;
	private AlwaysMarqueeTextView mPathTextView;
	private TextView mPathTitleTextView;

	private FrameLayout mTopButton;
	private RelativeLayout mTopButtonLyout;

	private MusicActivity mActivity;
	private List<CommonFileInfo> mFolderList = null;
	private MultiMediaListAdapter mAdapter;
	private OnSongFolderListener mFolderListener;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			case MusicActivity.REFRESH_DATA:
				Trace.Info("REFRESH_DATA");
				mFolderList = mFolderListener.getMusicFolderList();
				if (mFolderList.size()==0&&isVisible()) {
						getActivity().findViewById(R.id.music_sort_mode).requestFocus();
				}

				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case START_GET_THUMNAIL:
				Holder holder = (Holder) msg.obj;
				if (null != holder && holder.thumnail.getTag().equals(holder.folderPath)) {
					MusicThumnailLoader.getInstance().loadImage(holder.path, holder.thumnail);
				}
				break;

			default:
				break;
			}
		};
	};

	interface OnSongFolderListener {

		public void onRefreshData();
		public List<CommonFileInfo> getMusicFolderList();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MusicActivity) activity;
		mActivity.setmHandlerFromSongFolderFragment(mHandler);
		try {
			mFolderListener = (OnSongFolderListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnSongFolderListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFolderList = mFolderListener.getMusicFolderList();

		if (mFolderList == null || mFolderList.size() <= 0) {
			mFolderListener.onRefreshData();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mFolderList = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		// if(Configuration.ISMUSICDELETED){
		if (mFolderList.size() > 0 && mGridView.getSelectedItemPosition() >= 0) {
			mFolderList.get(mGridView.getSelectedItemPosition()).updateFileCount();
			if (mFolderList.get(mGridView.getSelectedItemPosition()).getChildrenMusicCount() > 0) {
				Trace.Debug("###ssongListsize=" + MusicSecondListActivity.getAllSecondMusicList().size());
				// mFolderList.get(mGridView.getSelectedItemPosition()).setChildrenMusicCount(MusicSecondListActivity.getAllSecondMusicList().size());
			} else {
				Trace.Debug("###ssongListsize=0" + MusicSecondListActivity.getAllSecondMusicList().size());
				mFolderList.remove(mGridView.getSelectedItemPosition());
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	// }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.songfolder_layout, container, false);

		initViews();

		return root;
	}

	private void initViews() {
		mGridView = (ScrollGridView) root.findViewById(R.id.musiclist_folder_gridview);
		mGridView.setBorderView((ImageView)getActivity().findViewById(R.id.musiclist_border));
		mAdapter = new MultiMediaListAdapter();
		mGridView.setAdapter(mAdapter);

		mPathTextView = (AlwaysMarqueeTextView) root.findViewById(R.id.musiclist_path);
		mPathTitleTextView = (TextView) root.findViewById(R.id.musiclist_path_title);

		mTopButton = (FrameLayout) root.findViewById(R.id.musiclist_top_btn);
		mTopButtonLyout = (RelativeLayout) root.findViewById(R.id.musicfolder_top_btn_layout);
		mGridView.setOnScrollListener(this);

		mTopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {

				mGridView.smoothScrollToTop();

			}
		});

		mGridView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_UP && mGridView.getSelectedItemPosition() < 4) {
						mActivity.onRequestFocus(true);
						return true;
					}
				}
				return false;
			}
		});

		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				CommonFileInfo fileInfo = (CommonFileInfo) mAdapter.getItem(arg2);
				// List<CommonFileInfo> list;
				if (fileInfo.isDir()) {
					Intent intent = new Intent();
					intent.putExtra(MusicSecondListActivity.SECONDLIST_FILE_LIST, fileInfo);
					intent.setAction("com.konka.eplay.action.MUSIC_SECOND_LIST");
					startActivity(intent);
				}

			}
		});

		mGridView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				if (mFolderList.size() > 0) {
					mPathTextView.setText(mFolderList.get(arg2).getPath());
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mPathTextView.setText("");
				mPathTitleTextView.setVisibility(View.INVISIBLE);

			}
		});

		mGridView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mPathTextView.setVisibility(View.VISIBLE);
					mPathTitleTextView.setVisibility(View.VISIBLE);
				}else {
					mPathTextView.setVisibility(View.INVISIBLE);
					mPathTitleTextView.setVisibility(View.INVISIBLE);
				}

			}
		});
	}

	public class MultiMediaListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mFolderList == null ? 0 : mFolderList.size();
		}

		@Override
		public Object getItem(int position) {
			if (mFolderList == null) {
				return null;
			} else {
				return mFolderList.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mFolderList == null || mFolderList.size() == 0)
				return null;
			final FileView file;
			final CommonFileInfo data = mFolderList.get(position);
			if (convertView == null) {
				file = new FileView();
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.music_grid_item, null);
				file.thumnail = (ImageView) convertView.findViewById(R.id.music_grid_thumnail);
				file.fcount = (TextView) convertView.findViewById(R.id.music_grid_count);
				file.fname = (TextView) convertView.findViewById(R.id.music_grid_foldername);
				convertView.setTag(file);
			} else {
				file = (FileView) convertView.getTag();
			}

			if (parent instanceof ScrollGridView) {
				if (((ScrollGridView) parent).isOnMeasure()) {
					return convertView;
				}
			}
			if (file != null) {
				if (mFolderList.get(position).isDir()) {
					String counttext = new String("" + mFolderList.get(position).getChildrenMusicCount());
					if (mFolderList.get(position).getChildrenMusicCount()>1) {
						file.fcount.setText(counttext + MusicUtils.getResourceString(getActivity(), R.string.music_file_count_more));
					}else {
						file.fcount.setText(counttext + MusicUtils.getResourceString(getActivity(), R.string.music_file_count));
					}

				}
				file.fname.setText(mFolderList.get(position).getName());

				file.thumnail.setTag(data.getPath());
				new Thread(new Runnable() {

					@Override
					public void run() {
						File folder_file = new File(data.getPath());
						File[] subFiles = folder_file.listFiles();
						List<File> fileList = new ArrayList<File>();
						if (subFiles == null) {
							Message message = Message.obtain();
							Holder holder = new Holder();
							holder.thumnail = file.thumnail;
							holder.folderPath = data.getPath();
							holder.path = data.getPath();
							message.what = START_GET_THUMNAIL;
							message.obj = holder;
							mHandler.sendMessage(message);
							return;
						}
						for (int i = 0; i < subFiles.length; i++) {
							fileList.add(subFiles[i]);
						}
						if (Configuration.sortType == SortType.ST_BY_TIME
								&& Configuration.curMediaType == MultimediaType.MMT_PHOTO) {
							Collections.sort(fileList, new FileComparator.sortListByModifyDate());
							Collections.reverse(fileList);
						} else {
							Collections.sort(fileList, new FileComparator.sortListByName());
						}

						for (int i = 0; i < fileList.size(); i++) {
							String picpath = fileList.get(i).getAbsolutePath();
							if (Utils.getMmt(picpath) == MultimediaType.MMT_MUSIC) {
								Message message = Message.obtain();
								Holder holder = new Holder();
								holder.thumnail = file.thumnail;
								holder.folderPath = data.getPath();
								holder.path = picpath;
								message.what = START_GET_THUMNAIL;
								message.obj = holder;
								mHandler.sendMessage(message);
								break;
							}
						}

					}
				}).start();

			}
			return convertView;
		}

		public class FileView {
			private ImageView thumnail;
			private TextView fcount;
			private TextView fname;
		}
	}

	public class Holder {
		private ImageView thumnail;
		private String path;
		private String folderPath;
	}

	public boolean isAvailableToHaveFocus() {
		if (mGridView == null || mFolderList == null || mFolderList.size() <= 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView paramAbsListView, int paramInt) {

	}

	@Override
	public void onScroll(AbsListView paramAbsListView, int paramInt1, int paramInt2, int paramInt3) {
		if (mTopButtonLyout != null) {
			if (paramInt1 == 0)
				mTopButtonLyout.setVisibility(View.INVISIBLE);
			else
				mTopButtonLyout.setVisibility(View.VISIBLE);
		}

	}
}
