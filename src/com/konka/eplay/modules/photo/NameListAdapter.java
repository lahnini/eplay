package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.model.FileComparator.sortByName;
import com.konka.eplay.modules.CommonFileInfo;

public class NameListAdapter extends BaseAdapter {
	private Context mContext;
	private List<CommonFileInfo> mList;
	private LayoutInflater inflater;
	private List<String> mNameList = new ArrayList<String>();
	static final int GB_SP_DIFF = 160;
	// 存放国标一级汉字不同读音的起始区位码
	static final int[] secPosValueList = { 1601, 1637, 1833, 2078, 2274, 2302,
					2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858,
					4027, 4086, 4390, 4558, 4684, 4925, 5249, 5600 };
	// 存放国标一级汉字不同读音的起始区位码对应读音
	static final char[] firstLetter = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
					'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w',
					'x', 'y', 'z' };

	public NameListAdapter(Context context, List<CommonFileInfo> list) {
		mContext = context;
		mList = list;
//		Collections.sort(mList, new FileComparator.sortByName());
		if (mList.size() > 0) {
			int length = mList.size();
			int i = 0;
			for (i = 0; i < length; i++) {
				String spells = mList.get(i).getFirstLetter();
				// nameString=nameString.substring(0, 1);
				// String spells = getSpells(nameString).toUpperCase();
				if (!mNameList.contains(spells)) {
					mNameList.add(spells);
				}
			}
		}
		Trace.Info("TimlistAdapter" + "timelist" + mNameList.size()
						+ "namelist" + mNameList.size());
	}


	@Override
	public Object getItem(int position) {
		return mNameList.get(position);
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
			convertView = inflater.inflate(R.layout.line_list_item, null);
			viewHolder = new NameViewHolder();

			viewHolder.name = (TextView) convertView
							.findViewById(R.id.txt_date_time);
			viewHolder.endView = (View) convertView.findViewById(R.id.v_line);
			Trace.Info("position##" + position + "##mlist.size##"
							+ mNameList.size());
		

			// Log.i("TimeAdapter", position+" : " + mTimeList.get(position));
			viewHolder.name.setText(mNameList.get(position));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (NameViewHolder) convertView.getTag();
			viewHolder.name.setText(mNameList.get(position));
			Trace.Info("position##" + position + "##mlist.size##"
							+ mNameList.size());
//			if (position == mNameList.size() - 1) {
//
//				viewHolder.endView.setVisibility(View.GONE);
//			}
		}
		if (position == mNameList.size() - 1) {

			viewHolder.endView.setVisibility(View.GONE);
		}else{
			viewHolder.endView.setVisibility(View.VISIBLE);
		}
		// String titleStr = list.get(position).get("title").toString();

		// viewHolder.title.setText(titleStr);

		return convertView;
	}

	static class NameViewHolder {
		public TextView name;
		public View endView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		// return mList.size();
		return mNameList.size();
	}

	public void switchList(List<CommonFileInfo> list) {
		// TODO Auto-generated method stub
		mList = list;
	}

	public static String getSpells(String characters) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < characters.length(); i++) {

			char ch = characters.charAt(i);
			if ((ch >> 7) == 0) {
				buffer.append(characters);
				// 判断是否为汉字，如果左移7为为0就不是汉字，否则是汉字
			} else {
				char spell = getFirstLetter(ch);
				buffer.append(String.valueOf(spell));
			}
		}
		return buffer.toString();
	}

	// 获取一个汉字的首字母
	public static Character getFirstLetter(char ch) {

		byte[] uniCode = null;
		try {
			uniCode = String.valueOf(ch).getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		if (uniCode[0] < 128 && uniCode[0] > 0) { // 非汉字
			return null;
		} else {
			return convert(uniCode);
		}
	}

	/**
	 * 获取一个汉字的拼音首字母。 GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码
	 * 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
	 * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
	 */
	static char convert(byte[] bytes) {
		char result = '-';
		int secPosValue = 0;
		int i;
		for (i = 0; i < bytes.length; i++) {
			bytes[i] -= GB_SP_DIFF;
		}
		secPosValue = bytes[0] * 100 + bytes[1];
		for (i = 0; i < 23; i++) {
			if (secPosValue >= secPosValueList[i]
							&& secPosValue < secPosValueList[i + 1]) {
				result = firstLetter[i];
				break;
			}
		}
		return result;
	}

	public List<String> getNameList() {
		return mNameList;
	}
}
