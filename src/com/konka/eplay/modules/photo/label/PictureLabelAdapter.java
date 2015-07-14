package com.konka.eplay.modules.photo.label;

import iapp.eric.utils.base.Trace;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.modules.AsyncImageView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.photo.ImageLoader;

public class PictureLabelAdapter extends BaseAdapter {
	private Context mContext;
	private List<CommonFileInfo> mList;
	private Boolean mIsScrollTop = false;

	public PictureLabelAdapter(Context context, List<CommonFileInfo> list) {
		mContext = context;
		mList = list;
	}

	public void switchList(List<CommonFileInfo> list) {
		mList = list;
	}

	public List<CommonFileInfo> getAdapterList() {
		return mList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			convertView = mInflater.inflate(R.layout.picture_label_gridview_item, null);

			holder.labelView=(ImageView)convertView.findViewById(R.id.picture_label);
			holder.imageView = (AsyncImageView) convertView
							.findViewById(R.id.local_gridview_item_img);
			holder.nameView = (TextView) convertView
							.findViewById(R.id.pic_name);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CommonFileInfo file = mList.get(position);
		final String filePath = file.getPath();
		holder.nameView.setText(file.getName());

		Trace.Debug("####getView filePath is " + filePath);
		holder.imageView.setTag(filePath);
		//当不滑向顶部时再进行开线程加载
		if(!mIsScrollTop) {
			holder.imageView.setImageForLabel(filePath, position);
		}
		return convertView;
	}

	private class ViewHolder {
		AsyncImageView imageView;
		TextView nameView;
		ImageView labelView;
	}
	
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		ImageLoader.getInstance(mContext).clearAllTaskList();
	}
	
	public void setScrollTopMark(Boolean mark) {
		mIsScrollTop = mark;
	}
}
