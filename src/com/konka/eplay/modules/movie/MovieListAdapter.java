package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.movie.ThumbnailLoader.ThumbnailLoaderListener;

/**
 * 播放器视频列表适配器
 * 
 * @author situ hui
 * 
 */
public class MovieListAdapter extends Adapter<MovieListAdapter.MyViewHolder> {

	private List<MovieInfo4Player> mMovieInfoList;
	private MoviePlayerActivity mPlayerActivity;
	private ThumbnailLoader mThumbnailLoader;
	private int mSelectedPosition;
	private int mPlayingPosition;
	private int mOldPlayingPosition = 0;

	private int mFleshFocusedTag = 0;// 标记是否刷新列表焦点，1表示刷新

	public MovieListAdapter(List<MovieInfo4Player> list, MoviePlayerActivity playerActivity) {
		mMovieInfoList = list;
		mPlayerActivity = playerActivity;
		mThumbnailLoader = new ThumbnailLoader(mPlayerActivity);
	}

	@Override
	public int getItemCount() {
		return mMovieInfoList.size();
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		holder.mVideoName.setTextColor(Color.WHITE);
		holder.mVideoName.setText(mMovieInfoList.get(position).getName());
		holder.mVideoImg.setTag(mMovieInfoList.get(position).getPath());
		mThumbnailLoader.loadThumbnailLocal(mMovieInfoList.get(position).getPath(), new ThumbnailLoaderListener() {

			@Override
			public void onThumbnailLoadStart() {
				holder.mVideoImg.setImageResource(R.drawable.v);
			}

			@Override
			public void onThumbnailLoadEnd(Bitmap result) {
				String path = (String) holder.mVideoImg.getTag();
				if (path != null && path.equals(mMovieInfoList.get(position).getPath())) {
					if (result != null) {
						holder.mVideoImg.setImageBitmap(result);
					}
				}

			}
		});
		if (position == mPlayingPosition) {
			holder.mVideoName.setText(mPlayerActivity.getString(R.string.playing_text));
			holder.mVideoName.setTextColor(mPlayerActivity.getResources().getColor(R.color.playing_text));
		}
		if (mFleshFocusedTag == 1 && position == mSelectedPosition) {
			holder.getItemView().requestFocus();
			mFleshFocusedTag = 0;
		}
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.movieplayer_list_item, null);
		MyViewHolder holder = new MyViewHolder(v);
		return holder;
	}

	/**
	 * 设置给定条目获取焦点
	 * 
	 * @param position
	 */
	public void setFocus(int position) {
		if (position < 0 || position >= getItemCount()) {
			position = 0;
		}
		((LinearLayoutManager) mPlayerActivity.mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position,
				Utils.dip2px(mPlayerActivity, 507));
		mSelectedPosition = position;
		mFleshFocusedTag = 1;
		notifyItemChanged(position);
	}

	/**
	 * 设置显示“正在播放”条目
	 * 
	 * @param position
	 */
	public void setPlayingPosition(int position) {
		if (position < 0 || position >= getItemCount()) {
			position = 0;
		}
		mPlayingPosition = position;
		notifyItemChanged(mOldPlayingPosition);
		notifyItemChanged(position);
		mOldPlayingPosition = mPlayingPosition;
	}

	class MyViewHolder extends RecyclerView.ViewHolder {
		private View mView;
		private TextView mVideoName;
		private ImageView mVideoImg;

		public MyViewHolder(View itemView) {
			super(itemView);
			if (itemView != null) {
				mView = itemView.findViewById(R.id.movieplayer_list_item);
				mVideoName = (TextView) itemView.findViewById(R.id.video_name);
				mVideoImg = (ImageView) itemView.findViewById(R.id.video_img);
				mView.setOnKeyListener(itemOnKeyListener);
				mView.setOnClickListener(itemOnClickListener);
				mView.setOnTouchListener(itemOnTouchListener);
				mView.setOnFocusChangeListener(onFocusChangeListener);
			}
		}

		public View getItemView() {
			return mView;
		}

		private OnKeyListener itemOnKeyListener = new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
						|| keyCode == KeyEvent.KEYCODE_ENTER) {
					mPlayerActivity.showPopListMillis(MoviePlayerActivity.PAUSE_TIME_BEFORE_HIDE);
				}
				return false;
			}
		};

		private OnClickListener itemOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = getPosition();
				mPlayerActivity.play(position);
				setFocus(position);
				setPlayingPosition(position);
			}
		};

		private OnTouchListener itemOnTouchListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Trace.Debug("item ontouchdown");
					mPlayerActivity.mHandler.removeMessages(MoviePlayerActivity.CMD_HIDE_LIST);
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Trace.Debug("item ontouchup");
					mPlayerActivity.showPopListMillis(MoviePlayerActivity.PAUSE_TIME_BEFORE_HIDE);
				}
				return false;
			}
		};

		private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == true) {
					Trace.Info("movie list focused position " + getPosition());
					if (mFleshFocusedTag != 1) {// mFleshFocusedTag==1时，是点击item或显示列表，点击item不需要scroll，显示列表时，焦点不是在当前播放item上所以要作判断，否则下面scroll把setFocus中的scroll覆盖
						((LinearLayoutManager) mPlayerActivity.mRecyclerView.getLayoutManager())
								.scrollToPositionWithOffset(getPosition(), Utils.dip2px(mPlayerActivity, 507));
					}
				}
			}
		};

	}
}
