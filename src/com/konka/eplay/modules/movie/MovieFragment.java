package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.ScrollGridView;
import com.konka.eplay.modules.photo.NameListAdapter;

public class MovieFragment extends Fragment {
	private MovieActivity mMovieActivity;
	private View mView;
	private int mPosition;// 记录焦点位置
	private ScrollGridView mGridView;

	private ListView mNameLineListView; // 字母轴
	private TextView moreTime;// 更多
	private NameListAdapter mNameListAdapter;
	private TextView iv;
	private ImageView hLine;
	// 当前选中项
	private int mSelected;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_movie_browser, container, false);
		mGridView = (ScrollGridView) mView.findViewById(R.id.movie_gridview);
		mGridView.setBorderView((ImageView) mView.findViewById(R.id.border_view_in_mygridView));

		mNameLineListView = (ListView) mView.findViewById(R.id.name_list);
		moreTime = (TextView) mView.findViewById(R.id.moretime);
		mNameLineListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				List<CommonFileInfo> list = mMovieActivity.getAdapter().getList();
				if (mNameLineListView.getLastVisiblePosition() == mNameListAdapter.getNameList().size() - 1) {
					moreTime.setVisibility(View.GONE);
				}
				else {
					moreTime.setVisibility(View.VISIBLE);
				}
				if (iv != null)
					iv.setTextColor(getResources().getColor(R.color.text_nofocus));
				iv = (TextView) view.findViewById(R.id.txt_date_time);
				iv.setTextColor(getResources().getColor(R.color.white));
				if (hLine != null)
					hLine.setVisibility(View.GONE);
				hLine = (ImageView) view.findViewById(R.id.h_name_line);
				hLine.setVisibility(View.VISIBLE);
				if (mNameLineListView.hasFocus()) {
					hLine.setBackgroundResource(R.drawable.time_selected);
				}else {
					hLine.setBackgroundResource(R.color.transparent);
				}
				String firstLetterString = mNameListAdapter.getNameList().get(position);
				int count = list.size();
				for (int i = 0; i < count; i++) {
					if (list.get(i).getFirstLetter().equals(firstLetterString)) {
						Trace.Debug("###Equals");
						// mGridView.setFocusable(true);
						// mGridView.requestFocus();
						if (mNameLineListView.hasFocus()) {
							Trace.Debug("gridViewPOsition=" + mGridView.getFirstVisiblePosition() + "i=" + i);
							Trace.Debug("####scroll to position NameLineListView");
							mGridView.smoothScrollToPosition(i);
						}
						break;
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
	mNameLineListView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus/*&&mNameLineListView.getVisibility()==View.VISIBLE*/){
					hLine.setBackground(getResources().getDrawable(R.drawable.time_selected));
				}else {
					hLine.setBackgroundResource(R.color.transparent);
				}
			}
		});
		
		

		return mView;
	}

	@Override
	public void onStart() {
		Trace.Debug("### MovieFragment onStart");
		super.onStart();

		mMovieActivity = (MovieActivity) getActivity();
		mMovieActivity.setShowedGridView(mGridView);
		// 刷新视频列表
		mGridView.setAdapter(mMovieActivity.getAdapter());
		mMovieActivity.getAdapter().notifyDataSetChanged();
		Trace.Debug("notifyDataSetChanged");

		mGridView.setOnItemClickListener(mMovieActivity.new ItemClickListener());
		mGridView.setOnScrollListener(mMovieActivity.new OnGridViewScroll());
		mGridView.setOnKeyListener(mKeyListener);
		mOnKeyListener = mMovieActivity.new ItemOnKey();
		mGridView.setOnFocusChangeListener(mMovieActivity.mOnFocusChangeListener);
		mGridView.setOnItemSelectedListener(this.mItemSeleteListener);
		mOnItemSelectedListener = mMovieActivity.new ItemSeleteListener();

		// 焦点设置
		if (mPosition == GridView.INVALID_POSITION) {
			mPosition = 0;
		}
		if (mGridView.getCount() > 0 && mPosition >= 0 && mPosition < mGridView.getCount()) {
			mGridView.setSelection(mPosition);
			mGridView.requestFocus();
		}

		mNameListAdapter = new NameListAdapter(mMovieActivity.getApplication(), mMovieActivity.getAdapter().getList());
		mNameLineListView.setAdapter(mNameListAdapter);

	}

	@Override
	public void onStop() {
		super.onStop();
		mPosition = mGridView.getSelectedItemPosition();
		Trace.Debug("### MovieFragment onStop, position = " + mPosition);
	}

	@Override
	public void onDestroyView() {
		Trace.Debug("### MovieFragment onDestroyView");
		super.onDestroyView();
		mPosition = 0;
	}

	public View getView() {
		return mView;
	}

	private OnItemSelectedListener mOnItemSelectedListener;
	// coustom OnItemSelectedListener for gridview
	private OnItemSelectedListener mItemSeleteListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (position < mMovieActivity.getAdapter().getCount() && position >= 0
					&& MovieFragment.this.mGridView.hasFocus()) {
				mSelected = position;
				CommonFileInfo curFile = (CommonFileInfo) mMovieActivity.getAdapter().getItem(position);
				String firstLetterString = curFile.getFirstLetter();
				Trace.Debug("###FrstLetter" + firstLetterString);
				List<String> nameList = new ArrayList<String>();
				nameList = mNameListAdapter.getNameList();
				int count = nameList.size();
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						if (nameList.get(i).equals(firstLetterString)) {
							mNameLineListView.setSelection(i);
							break;
						}
					}
				}
			}

			if (mOnItemSelectedListener != null) {
				mOnItemSelectedListener.onItemSelected(parent, view, position, id);
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			if (mOnItemSelectedListener != null) {
				mOnItemSelectedListener.onNothingSelected(parent);
			}
		}
	};

	private OnKeyListener mOnKeyListener;
	private OnKeyListener mKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				if(mSelected % 4 == 0) {
					mNameLineListView.requestFocus();
					return true;
				}
			}
			if (mOnKeyListener != null) {
				mOnKeyListener.onKey(v, keyCode, event);
			}
			return false;
		}
	};

}
