package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.R;
import com.konka.eplay.modules.ScrollGridView;

public class MovieDirFragment extends Fragment {
	private View mView;
	private ScrollGridView mGridView;
	private MovieActivity mMovieActivity;
	private int mPosition;// 记录焦点位置
	private TextView mEmptyView; //GridView为空时显示的内容

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_movie_dir_browser, container, false);
		mGridView = (ScrollGridView) mView.findViewById(R.id.movie_dir_gridview);
		mGridView.setBorderView((ImageView) mView.findViewById(R.id.border_view_in_mygridView));
		mEmptyView = (TextView) mView.findViewById(R.id.movie_empty_view);
		return mView;
	}

	@Override
	public void onStart() {
		Trace.Debug("### MovieDirFragment onStart");
		super.onStart();
		
		mMovieActivity = (MovieActivity) getActivity();
		mMovieActivity.setShowedGridView(mGridView);
		// 刷新列表
		mGridView.setAdapter(mMovieActivity.getDirAdapter());
		mMovieActivity.getDirAdapter().notifyDataSetChanged();
		
		mGridView.setOnItemClickListener(mMovieActivity.new ItemClickListener());
		mGridView.setOnScrollListener(mMovieActivity.new OnGridViewScroll());
		mGridView.setOnKeyListener(mMovieActivity.new ItemOnKey());
		mGridView.setOnFocusChangeListener(mMovieActivity.mOnFocusChangeListener);
		mGridView.setOnItemSelectedListener(mMovieActivity.new ItemSeleteListener());
		
		mMovieActivity.setBackText(mMovieActivity.getString(R.string.moviebtn));
		
		if (mPosition == GridView.INVALID_POSITION) {
			mPosition = 0;
		}
		if (mGridView.getCount() > 0 && mPosition >= 0 && mPosition < mGridView.getCount()) {
			mGridView.setSelection(mPosition);
			mGridView.requestFocus();
		}
	}
	

	@Override
	public void onStop() {
		super.onStop();
		mPosition = mGridView.getSelectedItemPosition();
		Trace.Debug("### MovieDirFragment onStop, position = " + mPosition);
	}

	public View getView() {
		return mView;
	}

	public void setGridViewEmptyView() {
		if (mGridView != null) {
			mGridView.setEmptyView(mEmptyView);
		}
	}

	public void setGridViewEmptyViewGone() {
		mEmptyView.setVisibility(View.GONE);
	}
}
