package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.konka.eplay.Configuration;
import com.konka.eplay.Utils;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.R;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.model.FileComparator.sortByName;
import com.konka.eplay.modules.CommonFileInfo;

public class MusicListViewAdapter extends BaseAdapter {
	private Context mContext;
	private SortType mSortType;
	private List<CommonFileInfo>mList;
	private LayoutInflater inflater;
	private List<CommonFileInfo>mSingerList = new ArrayList<CommonFileInfo>();
	public MusicListViewAdapter(Context context, List<CommonFileInfo> list) {
//		MusicUtils.getAllSongInfo(list);
		mContext = context;
		mList=list;
		switch (Configuration.musicSortType) {
		case ST_BY_NAME:
//			Collections.sort(list, new FileComparator.sortByName());
			mSortType=SortType.ST_BY_NAME;
			break;
		case ST_BY_SINGER:
			Trace.Debug("##ST_BY_SINGER##");
			mSortType=SortType.ST_BY_SINGER;
//			if(mList.size()>0){
//				int length=mList.size();
//				int i=0;
//				for(i=0;i<length;i++){
//					CommonFileInfo rootCommonFileInfo = new CommonFileInfo(new File(mList.get(i).getParentPath()));
//					rootCommonFileInfo.setName(rootCommonFileInfo.getSinger());
//					rootCommonFileInfo.setChildrenCount(1);
//					rootCommonFileInfo.setDir(true);
//					if(!mSingerList.contains(rootCommonFileInfo)){
//						mSingerList.add(rootCommonFileInfo);
//					}else {
//						rootCommonFileInfo.setChildrenCount(rootCommonFileInfo.getChildrenCount()+1);
//					}
//				}
//			}
			Trace.Debug("##mSingerListSize##+"+mSingerList.size());
//			Collections.sort(list, new FileComparator.sortBySinger());
			break;
		case ST_BY_SPECIAL:
			mSortType=SortType.ST_BY_SPECIAL;
//			Collections.sort(list, new FileComparator.sortBySpecial());
			break;
		case ST_BY_LIKE:
			mSortType=SortType.ST_BY_LIKE;
//			Collections.sort(list, new FileComparator.sortByName());
			break;

		default:
			break;
		}
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
		
		NameViewHolder viewHolder;
		if (convertView == null) {
			inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.music_list_item, null);
			viewHolder = new NameViewHolder();
				viewHolder.musicname = (TextView) convertView
								.findViewById(R.id.musicname);
				viewHolder.musicsinger = (TextView) convertView
								.findViewById(R.id.musicsinger);
				viewHolder.musictimelength = (TextView) convertView
								.findViewById(R.id.musiclength);
				Trace.Info("position##"+position+"##mlist.size##"+mList.size());

				//Log.i("TimeAdapter",  position+" : " + mTimeList.get(position));
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (NameViewHolder) convertView.getTag();
		}
		// String titleStr = list.get(position).get("title").toString();

		// viewHolder.title.setText(titleStr);
		if(Configuration.musicSortType==SortType.ST_BY_SINGER||Configuration.musicSortType==SortType.ST_BY_SPECIAL){
			Trace.Info("####ST_SORT_BY_SINGER");
			viewHolder.musicsinger.setText(null);
			viewHolder.musictimelength.setText(""+mList.get(position).getChildrenCount()+"首" );
		} else{
		viewHolder.musicsinger.setText(mList.get(position).getSinger());
		viewHolder.musictimelength.setText(mList.get(position).getTime());
		}
		Trace.Debug("$$$setSingerName+"+position);
		viewHolder.musicname.setText(mList.get(position).getName());
		return convertView;
	}

	static class NameViewHolder {
		public TextView musicname;
		public TextView musicsinger;
		public TextView musictimelength;
		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		//return mList.size();
		return mList.size();
	}

	public void switchList(List<CommonFileInfo> list) {
		// TODO Auto-generated method stub
		mList=list;
	}
	
}
