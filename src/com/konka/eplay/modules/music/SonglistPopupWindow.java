package com.konka.eplay.modules.music;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.music.BorderView.EndRunListener;

/**
 * @ClassName: SonglistPopupWindow
 * @Description: 显示歌曲列表
 * @author xuyunyu
 * @date 2015年3月24日 下午2:21:54
 *
 */
public class SonglistPopupWindow extends PopupWindow {

	private View mContentView;
	private Activity mContext;
	private ListView mListView;
	private List<CommonFileInfo> mMusicList;
	private SongListAdapter mListAdapter;
	private BorderView mSlectedView;

	private OnChangeSongListenner mChangeSongListenner;
	private MusicPlayerService mMusicPlayerService;

	public void setChangeSongListenner(OnChangeSongListenner mChangeSongListenner) {
		this.mChangeSongListenner = mChangeSongListenner;
	}

	public interface OnChangeSongListenner {

		public void onChangeSong(int position);


	}

	public SonglistPopupWindow(final Activity context, MusicPlayerService service) {
		super(context);
		mContext = context;

		mMusicPlayerService = service;
		mMusicList = mMusicPlayerService.getmMusicList();

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = inflater.inflate(R.layout.music_songlist, null);
		this.setContentView(mContentView);
		this.setWidth(Utils.dip2px(mContext, 394));
		this.setHeight(LayoutParams.MATCH_PARENT);

		this.setFocusable(true);
		this.setOutsideTouchable(true);
		this.setClippingEnabled(false);

		// 刷新状态
		this.update();

		// 实例化一个ColorDrawable颜色为透明
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
		this.setBackgroundDrawable(drawable);

		this.setAnimationStyle(R.style.PopupAnimation);

		mListView = (ListView) mContentView.findViewById(R.id.music_song_listview);

		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mSlectedView = (BorderView) mContentView.findViewById(R.id.list_view_selected);

		mListAdapter = new SongListAdapter();
		mListView.setAdapter(mListAdapter);

		mListView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				boolean handled = false;
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (isShowing()) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							shake();
							handled = true;
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT:
							dismiss();
							handled = true;
							break;

						default:
							break;
						}
					}
				}
				return handled;
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				mChangeSongListenner.onChangeSong(position);
				mSlectedView.runAlphaLocateAnimation(arg1, new EndRunListener() {

					@Override
					public void onEndRunListener() {
						// do nothing

					}
				});
			}
		});

		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mSlectedView.setLocation(arg1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

	}

	class SongListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mMusicList.size();
		}

		@Override
		public Object getItem(int position) {
			return mMusicList.get(position);
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
				convertView = mInflater.inflate(R.layout.music_songlist_item, null);

				holder.title = (TextView) convertView.findViewById(R.id.songlist_title);
				holder.singer = (TextView) convertView.findViewById(R.id.songlist_singer);
				holder.time = (TextView) convertView.findViewById(R.id.songlist_time);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String name = mMusicList.get(position).getName();
			holder.title.setText(name.substring(0, name.length() - 4));
			holder.singer.setText(mMusicList.get(position).getSinger());
			holder.time.setText(mMusicList.get(position).getTime());

			if (mMusicPlayerService.getmCurrentListPosition() == position) {

				holder.title.setTextColor(mContext.getResources().getColor(R.color.songlist_selected));
				holder.singer.setTextColor(mContext.getResources().getColor(R.color.songlist_selected));
				holder.time.setTextColor(mContext.getResources().getColor(R.color.songlist_selected));

			} else {
				holder.title.setTextColor(mContext.getResources().getColor(R.color.songlist_unselected_title));
				holder.singer.setTextColor(mContext.getResources().getColor(R.color.songlist_unselected_name));
				holder.time.setTextColor(mContext.getResources().getColor(R.color.songlist_unselected_name));
			}

			return convertView;
		}

	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		mListView.requestFocus();
		mListView.setSelection(mMusicPlayerService.getmCurrentListPosition());

	}

	public final class ViewHolder {

		public TextView title;
		public TextView singer;
		public TextView time;
	}

	/**
	* @Title: shake
	* @Description: 实现界面抖动的效果
	 */
	public void shake() {
		mContentView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake));
	}

	/**
	* @Title: refreshList
	* @Description: 熟悉列表
	 */
	public void refreshList() {
		mListAdapter.notifyDataSetChanged();
	}

	/**
	* @Title: setFocusImageVisible
	* @Description: 当界面出现动画完成之后此方法会被调用，使焦点框可见
	 */
	public void setFocusImageVisible(int visibility) {

		mSlectedView.setLocation(mListView.getSelectedView());
		mSlectedView.setVisibility(visibility);
	}

}
