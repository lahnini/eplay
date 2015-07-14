package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.movie.ThumbnailLoader.ThumbnailLoaderListener;
import com.konka.eplay.modules.photo.QuickToast;

/**
 * 视频详情页
 * 
 * @author situ hui
 * 
 */
public class MovieInfoActivity extends Activity {
	private View mNullLayout;// 辅助弹窗去焦

	private View mBack;
	private TextView mMovieName;
	private TextView mMovieSize;
	private TextView mMovieResolution;
	private TextView mMovieDuration;
	private TextView mMovieDate;
	private TextView mMoviePath;
	private ImageView mMovieImg;
	private ImageView mBackground;

	private Button mBtnOpen;
	private Button mBtnDelete;

	private int mIndex;
	private ArrayList<String> mPaths;
	private String mPath;
	private CommonFileInfo mMovieFileInfo;

	private ThumbnailLoader mThumbnailLoader;

	private MediaMetadataRetriever mRetriever;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_info);
		init();
		getSourceFromIntent();
		setInfo();
	}

	@Override
	protected void onStart() {
		mBtnOpen.requestFocus();
		super.onStart();
	}

	// 初始化activity
	private void init() {
		mNullLayout = findViewById(R.id.null_layout);
		mBack = findViewById(R.id.back);

		mMovieName = (TextView) findViewById(R.id.movie_info_name);
		mMovieSize = (TextView) findViewById(R.id.movie_info_size);
		mMovieResolution = (TextView) findViewById(R.id.movie_info_resolution);
		mMovieDuration = (TextView) findViewById(R.id.movie_info_duration);
		mMovieDate = (TextView) findViewById(R.id.movie_info_date);
		mMoviePath = (TextView) findViewById(R.id.movie_info_path);
		mMovieImg = (ImageView) findViewById(R.id.movie_info_img);
		mBackground = (ImageView) findViewById(R.id.movie_info_background_img);
		mBtnOpen = (Button) findViewById(R.id.movie_info_open);
		mBtnDelete = (Button) findViewById(R.id.movie_info_delete);

		mBtnOpen.setOnClickListener(mOnClickListener);
		mBtnDelete.setOnClickListener(mOnClickListener);

		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	// 从intent获取数据
	private void getSourceFromIntent() {
		Intent intent = getIntent();
		mIndex = intent.getIntExtra(Constant.PLAY_INDEX, 0);
		mPaths = intent.getStringArrayListExtra(Constant.PLAY_PATHS);
		if (mPaths == null) {
			finish();
			return;
		}
	}

	// 显示详情页上面信息
	private void setInfo() {
		if (mIndex >= mPaths.size()) {
			finish();
			return;
		}
		mPath = mPaths.get(mIndex);
		File file = new File(mPath);
		if (!file.exists()) {
			Trace.Debug("file dosen't exist:" + mPath);
			finish();
			return;
		}
		mMovieFileInfo = new CommonFileInfo(file);
		if (mMovieFileInfo == null) {
			finish();
			return;
		}
		mRetriever = new MediaMetadataRetriever();
		try {
			mRetriever.setDataSource(mPath);
		} catch (IllegalArgumentException e) {
			Trace.Info("movie info activity mediametadataretriever runtime exception,video path=" + mPath);
			mRetriever = null;
		}
		setImage();
		setName();
		setSize();
		setDuration();
		setDate();
		setPath();
		setResolution();
		if (mRetriever != null) {
			mRetriever.release();
		}

	}

	private void setName() {
		String name = mMovieFileInfo.getName();
		mMovieName.setText(name);
	}

	// 显示大小
	private void setSize() {
		long size = mMovieFileInfo.getSize();
		String sizeString = Tools.formatSize(size);
		mMovieSize.setText("" + sizeString);
	}

	// 显示分辩率
	private void setResolution() {
		if (mPath == null) {
			return;
		}
		// if (mRetriever != null) {
		// // 获取视频原宽高
		// String width =
		// mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
		// String height =
		// mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
		// if (width == null || height == null) {
		// return;
		// }
		// int videoW = Integer.parseInt(width);
		// int videoH = Integer.parseInt(height);
		// mMovieResolution.setText(videoW + "*" + videoH);
		// }
		// else {
		// mMovieResolution.setText(this.getString(R.string.unknown));
		// }

		final MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
			
			@Override
			public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
				int videoW = mediaPlayer.getVideoWidth();
				int videoH = mediaPlayer.getVideoHeight();
				mMovieResolution.setText(videoW + "*" + videoH);
			}
		});
		try {
			mediaPlayer.setDataSource(mPath);
			mediaPlayer.prepareAsync();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 显示时长
	private void setDuration() {
		if (mPath == null) {
			return;
		}
		if (mRetriever != null) {
			String duration = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			if (duration == null) {
				return;
			}
			int timeInmillisec = Integer.parseInt(duration);
			String time = Tools.formatMsec(timeInmillisec);
			mMovieDuration.setText(time);
		}
		else {
			mMovieDuration.setText(this.getString(R.string.unknown));
		}

	}

	// 显示日期
	private void setDate() {
		Date date = mMovieFileInfo.getCreatedTime();
		SimpleDateFormat formater = new SimpleDateFormat("yyyy.MM.dd");
		String dateString = formater.format(date);
		mMovieDate.setText(dateString);
	}

	// 显示路径
	private void setPath() {
		String path = mMovieFileInfo.getParentPath();
		mMoviePath.setText(Utils.getWrapperPath(path));
	}

	// 设置图片
	private void setImage() {
		if (mPath == null) {
			return;
		}
		mThumbnailLoader = new ThumbnailLoader(this);
		mThumbnailLoader.loadThumbnail(mPath, new ThumbnailLoaderListener() {

			@Override
			public void onThumbnailLoadStart() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onThumbnailLoadEnd(Bitmap result) {
				mMovieImg.setImageBitmap(result);
				mBackground.setImageBitmap(result);
			}
		});
	}

	// 显示删除对话框
	private void showDeleteDialog() {
		// 去焦
		mNullLayout.setFocusable(true);
		mNullLayout.requestFocus();

		final Dialog dialog = new Dialog(this, R.style.delete_dialog);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		dialog.setContentView(view);

		TextView message = (TextView) view.findViewById(R.id.info_message);
		message.setText(getResources().getString(R.string.movie_delete_hint));
		view.findViewById(R.id.cancelButton).requestFocus();
		Window window = dialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		view.findViewById(R.id.decideButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				// 删除操作
				Trace.Debug("####showDeleteDialog delete confirm");
				File file = new File(mPath);
				if (file != null && file.exists()) {
					if (file.delete()) {
						// 此处在外层还要进行刷新一下显示
						QuickToast.showToast(MovieInfoActivity.this,
								getResources().getString(R.string.movie_delete_success));
						Intent intent = new Intent();
						intent.putExtra("delete_index", mIndex);
						setResult(RESULT_OK, intent);
					}
				}

				// 退出详情页Activity
				MovieInfoActivity.this.finish();
			}
		});

		view.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				mNullLayout.setFocusable(false);
				mBtnDelete.requestFocus();
			}
		});

		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				mNullLayout.setFocusable(false);
				mBtnDelete.requestFocus();
			}
		});

		dialog.show();
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.movie_info_open:
				Trace.Debug("movie open");
				Intent intent = new Intent();
				intent.setAction(Constant.PLAY_VIDEO_ACTION);
				intent.putExtra(Constant.PLAY_INDEX, mIndex);
				intent.putStringArrayListExtra(Constant.PLAY_PATHS, mPaths);
				startActivity(intent);
				finish();
				break;
			case R.id.movie_info_delete:
				Trace.Debug("movie delete");
				showDeleteDialog();
				break;
			default:
				break;
			}
		}

	};
}
