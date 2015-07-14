package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.movie.ThumbnailLoader.ThumbnailLoaderListener;

public class BrowserAdapter extends BaseAdapter {

	private final Context mMovieActivity;
	private List<CommonFileInfo> mFileList;
	private ThumbnailLoader mThumbnailLoader;
	private PlayRecordHelper mPlayRecordHelper;

	BrowserAdapter(Context movieActivity, List<CommonFileInfo> list) {
		this.mMovieActivity = movieActivity;
		mFileList = list;
		mThumbnailLoader = new ThumbnailLoader(mMovieActivity);
	}

	public List<CommonFileInfo> getList() {
		return mFileList;
	}

	@Override
	public int getCount() {
		return mFileList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final FileView file;
		CommonFileInfo data = null;
		if (position >= 0 && position < mFileList.size()) {
			data = mFileList.get(position);
		}
		else {
			return null;
		}
		if (convertView == null) {
			file = new FileView();
			convertView = LayoutInflater.from(mMovieActivity).inflate(R.layout.gridview_movie_item, null);
			file.ficon = (ImageView) convertView.findViewById(R.id.video_icon);
			file.fname = (TextView) convertView.findViewById(R.id.video_name);
			file.fPlayRecord = (TextView) convertView.findViewById(R.id.play_record);
			convertView.setTag(file);
		}
		else {
			file = (FileView) convertView.getTag();
		}
		if (file != null && data != null) {
			file.fname.setText(data.getName());
			final String tempPath = data.getPath();
			showPlayRecord(file.fPlayRecord, tempPath);
			file.ficon.setTag(tempPath);
			mThumbnailLoader.loadThumbnailLocal(tempPath, new ThumbnailLoaderListener() {

				@Override
				public void onThumbnailLoadStart() {
					file.ficon.setImageResource(R.drawable.v);
				}

				@Override
				public void onThumbnailLoadEnd(Bitmap result) {
					String path = (String) file.ficon.getTag();

					if (path != null && path.equals(tempPath)) {
						if (result != null) {
							file.ficon.setImageBitmap(result);
						}
					}

				}
			});
		}
		return convertView;
	}

	// 设置要给item显示的播放记录
	private void showPlayRecord(TextView textView, String path) {
		Trace.Debug("showPlayRecord");
		textView.setTag(path);// 防错位
		PlayRecordTask task = new PlayRecordTask(textView, path);
		task.execute();
	}

	private class FileView {
		private ImageView ficon;
		private TextView fname;
		private TextView fPlayRecord;
	}

	private class PlayRecordTask extends AsyncTask<Void, Void, String> {

		private TextView textView;// 用于显示记录的textview
		private String path;// 要查询的视频路径

		public PlayRecordTask(TextView textView, String path) {
			this.textView = textView;
			this.path = path;
		}

		@Override
		protected void onPreExecute() {
			textView.setVisibility(View.GONE);
		}

		@Override
		protected String doInBackground(Void... params) {
			if (mPlayRecordHelper == null) {
				mPlayRecordHelper = new PlayRecordHelper(mMovieActivity);
			}
			if (mPlayRecordHelper.isPlayCompleted(path)) {
				return mMovieActivity.getString(R.string.play_completed);
			}
			else {
				int timeInMillis = mPlayRecordHelper.getPlayedOffset(path);
				if (timeInMillis == 0) {
					return null;
				}
				else {
					String time = Tools.formatMsec(timeInMillis);
					return mMovieActivity.getString(R.string.played_to) + time;
				}
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				return;
			}
			else {
				String tag = (String) textView.getTag();
				if (tag != null && tag.equals(path)) {// 防错位
					textView.setVisibility(View.VISIBLE);
					textView.setText(result);
				}
			}
		}

	}

}
