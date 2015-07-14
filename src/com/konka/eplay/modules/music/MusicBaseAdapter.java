/**
 * @Title: MusicBaseAdapter.java
 * @Package com.konka.eplay.modules.music
 * @Description: TODO(用一句话描述该文件做什么)
 * @date 2015年5月4日 下午4:39:57
 */
package com.konka.eplay.modules.music;

import java.util.List;

import com.konka.eplay.modules.CommonFileInfo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @ClassName: MusicBaseAdapter
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年5月4日 下午4:39:57
 *
 */
public class MusicBaseAdapter extends BaseAdapter {

	private List<CommonFileInfo> mFileList = null;

	public MusicBaseAdapter(List<CommonFileInfo> fileList) {
		mFileList = fileList;
	}

	public void setlist(List<CommonFileInfo> fileList){
		mFileList = fileList;
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

	public List<CommonFileInfo> getList() {
		return mFileList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}


}
