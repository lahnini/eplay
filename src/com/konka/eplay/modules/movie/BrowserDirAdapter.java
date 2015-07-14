package com.konka.eplay.modules.movie;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.movie.ThumbnailLoader.ThumbnailLoaderListener;

public class BrowserDirAdapter extends BaseAdapter {

	private final Context mMovieActivity;
	private List<CommonFileInfo> mFileList;
	private ThumbnailLoader mThumbnailLoader;

	BrowserDirAdapter(Context movieActivity, List<CommonFileInfo> list) {
		this.mMovieActivity = movieActivity;
		mFileList = list;
		mThumbnailLoader = new ThumbnailLoader(mMovieActivity);
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
			convertView = LayoutInflater.from(mMovieActivity).inflate(R.layout.gridview_movie_dir_item, null);
			file.ficon = (ImageView) convertView.findViewById(R.id.video_icon);
			file.fcount = (TextView) convertView.findViewById(R.id.video_count);
			file.fname = (TextView) convertView.findViewById(R.id.video_name);
			convertView.setTag(file);
		}
		else {
			file = (FileView) convertView.getTag();
		}
		if (file != null && data != null) {
			file.fname.setText(mFileList.get(position).getName());
			if (mFileList.get(position).isDir()) {
				String countText = new String("" + mFileList.get(position).getChildrenMovieCount()
						+ this.mMovieActivity.getString(R.string.moviebtn));
				file.fcount.setText(countText);

				// 这种方法获取会有排序操作
				// Operation operation = Operation.getInstance();
				// List<CommonFileInfo> childList =
				// operation.getSpecificFiles(data.getPath(),
				// MultimediaType.MMT_MOVIE);

				File f = new File(data.getPath());
				File[] childList = f.listFiles();
				List<File> movieList = new ArrayList<File>();
				if(childList != null) {
					for (File item : childList) {
						if (Utils.getMmt(item.getAbsolutePath()) == MultimediaType.MMT_MOVIE) {
							movieList.add(item);
							break;
						}
					}
				}
				if (movieList != null && movieList.size() > 0) {
					final String tempPath = movieList.get(0).getPath();
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
			}
		}
		return convertView;
	}

	private class FileView {
		private ImageView ficon;
		private TextView fcount;
		private TextView fname;
	}
}
