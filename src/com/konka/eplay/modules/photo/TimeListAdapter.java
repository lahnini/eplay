package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javazoom.jl.converter.Converter;
import android.R.string;
import android.content.Context;
import android.util.Log;
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
import com.konka.eplay.modules.CommonFileInfo;

public class TimeListAdapter extends BaseAdapter {
	private Context mContext;
	private List<CommonFileInfo>mList;
	private LayoutInflater inflater;
	private List<String>mTimeList = new ArrayList<String>() ;
	public TimeListAdapter(Context context, List<CommonFileInfo> list) {
		mContext = context;
		mList=list;
		Collections.sort(mList, new FileComparator.sortByModifyDate());
		Collections.reverse(mList);
		if(mList.size()>0){
			int length=mList.size();
			int i=0;
			for(i=0;i<length;i++){
				Date time=(Date) mList.get(i).getModifiedTime();
				String dataString=Utils.dateToStr(time).toString();
			if (!mTimeList.contains(dataString)) {
				mTimeList.add(dataString);
			}
			}
//			for(int j=0; j<mTimeList.size(); j++){
//				Log.i("TimeAdapter",  j + " : " + mTimeList.get(j));
//			}
		}
	}

	// @Override
	// public int getCount() {
	// if (Configuration.sortType == SortType.ST_BY_NAME) {
	// return mNameList.size();
	// } else {
	// return mTimeList.size();
	// }
	// }

	@Override
	public Object getItem(int position) {
		return mTimeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder;
		if (convertView == null) {
			inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.line_list_item, null);
			viewHolder = new ViewHolder();

				viewHolder.time = (TextView) convertView
								.findViewById(R.id.txt_date_time);
				viewHolder.lastTime=(TextView)convertView.findViewById(R.id.last_date_time);
				viewHolder.endView=(View)convertView.findViewById(R.id.v_line);
		

				//Log.i("TimeAdapter",  position+" : " + mTimeList.get(position));
				viewHolder.time.setText(getFirst2Letter(mTimeList.get(position)));
				viewHolder.lastTime.setVisibility(View.VISIBLE);
				viewHolder.lastTime.setText(getLastLetter(mTimeList.get(position)));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.time.setText(getFirst2Letter(mTimeList.get(position)));
			viewHolder.lastTime.setVisibility(View.VISIBLE);
			viewHolder.lastTime.setText(getLastLetter(mTimeList.get(position)));
		}
		if(position==mTimeList.size()-1){
			viewHolder.endView.setVisibility(View.GONE);
		}

		// String titleStr = list.get(position).get("title").toString();

		// viewHolder.title.setText(titleStr);

		return convertView;
	}

	static class ViewHolder {
		public TextView time;
		public TextView lastTime;
		public TextView title;
		public View endView;
	}
	
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		//return mList.size();
		return mTimeList.size();
	}

	public void switchList(List<CommonFileInfo> list) {
		// TODO Auto-generated method stub
		mList=list;
	}
	public List<String> getTimeList() {
		return mTimeList;
	}
	public String getFirst2Letter(String data){
		
		String f2=data.substring(6);
		return f2;
		
	}
	public String getLastLetter(String data){
		String lL=data.substring(0,4)+"/"+data.substring(4,6);
		return lL;
		
	}
	
}
