package com.konka.eplay.modules;

import iapp.eric.utils.base.Trace;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.photo.ImageLoader;
import com.konka.eplay.modules.photo.PhotoActivity;

public class PhotoAdapter extends BaseAdapter {
	private Context mContext;
	private List<CommonFileInfo> mList;
	
	private Boolean mIsScrollTop = false;

	public PhotoAdapter(Context context, List<CommonFileInfo> list,
					List<CommonFileInfo> selList) {
		mContext = context;
		mList = list;
	}

	public void switchList(List<CommonFileInfo> list) {
		mList = list;
	}

	public void setSelectMode(boolean editMode) {
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

	@SuppressLint({ "InflateParams", "ResourceAsColor" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (position >= mList.size())
			return convertView;
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			convertView = mInflater.inflate(R.layout.gridview_item, null);
			holder.enlargeView = (ImageView) convertView
							.findViewById(R.id.enlargeview);
			holder.isMark=(ImageView)convertView.findViewById(R.id.ismark);
			holder.iv = (AsyncImageView) convertView
							.findViewById(R.id.local_gridview_item_img);
			holder.fphotomsg=(FrameLayout)convertView.findViewById(R.id.fphotomsg);
			holder.pic_name = (TextView) convertView
							.findViewById(R.id.pic_name);
			holder.pic_count = (TextView) convertView
							.findViewById(R.id.pic_count);
//			holder.sel_icon = (ImageView) convertView
//							.findViewById(R.id.local_gridview_item_sel);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (parent instanceof ScrollGridView) {
			if (((ScrollGridView) parent).isOnMeasure()) {
				return convertView;
			}
		}
		
		CommonFileInfo file = mList.get(position);
		final String filePath = file.getPath();
		holder.pic_name.setText(file.getName());
		holder.iv.setTag(filePath);
		if (file.isDir()) {
			holder.fphotomsg.setVisibility(View.VISIBLE);
			String picCount = new String("" + file.getChildrenPhotoCount()
							+ "张");
			holder.pic_count.setText(picCount);
			holder.enlargeView.setVisibility(View.VISIBLE);
			holder.isMark.setVisibility(View.INVISIBLE);
			if(!mIsScrollTop) {
				holder.iv.setPath(filePath, true);			
			}
			convertView.setAlpha(1);
		} else {
			if(file.getIsBlue()&&!Configuration.MARKISENTER){
				Trace.Debug("!!isBlue");
				holder.isMark.setBackgroundResource(R.drawable.label_blue);
				holder.isMark.setVisibility(View.VISIBLE);
			}else if(file.getIsRed()&&!Configuration.MARKISENTER){
				Trace.Debug("!!isRed");
				holder.isMark.setBackgroundResource(R.drawable.label_red);
				holder.isMark.setVisibility(View.VISIBLE);
			}else if (file.getIsYellow()&&!Configuration.MARKISENTER) {
				Trace.Debug("!!isYellow");
				holder.isMark.setBackgroundResource(R.drawable.label_yellow);
				holder.isMark.setVisibility(View.VISIBLE);
			}else {
					holder.isMark.setVisibility(View.INVISIBLE);
			}
			holder.enlargeView.setVisibility(View.GONE);
			Trace.Debug("##curNameString="+PhotoActivity.curNameString);
			if (Configuration.sortType==SortType.ST_BY_NAME&&!PhotoActivity.curNameString.equals(".")&&!file.getFirstLetter().equals(PhotoActivity.curNameString)) {
				Trace.Debug("##equals");
				convertView.setAlpha((float) 0.4);
			}else if (Configuration.sortType==SortType.ST_BY_TIME&&	!PhotoActivity.curTimeString.equals("0")&&!Utils.dateToStr(file.getModifiedTime()).toString().equals(PhotoActivity.curTimeString)) {
				Trace.Debug("##equals");
				convertView.setAlpha((float) 0.4);
			}else {
				convertView.setAlpha(1);
				
			}
			holder.fphotomsg.setVisibility(View.INVISIBLE);
			if(!mIsScrollTop) {
				holder.iv.setImageForPic(filePath,position);
				Trace.Debug("###test position is " + position);
			}
		}
		return convertView;
	}
	class ViewHolder {
		AsyncImageView iv;
		ImageView sel_icon;
		ImageView pic_img;
		FrameLayout fphotomsg;
		TextView pic_count;
		TextView pic_name;
		ImageView enlargeView;
		ImageView isMark;
	}
	
	@Override
	public void notifyDataSetChanged() {
		ImageLoader.getInstance(mContext).clearAllTaskList();
		Trace.Debug("curNamenotifyDataSetChanged()");
		super.notifyDataSetChanged();
	}
	
	public void setScrollTopMark(Boolean mark) {
		mIsScrollTop = mark;
	}
}
