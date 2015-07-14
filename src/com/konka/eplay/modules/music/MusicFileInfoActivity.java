/**
 * @Title: MusicFileInfoActivity.java
 * @Package com.konka.eplay.modules.music
 * @Description: TODO(用一句话描述该文件做什么)
 * @author xuyunyu
 * @date 2015年4月20日 下午6:44:39
 * @version
 */
package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;
import iapp.eric.utils.custom.model.APIC;
import iapp.eric.utils.metadata.Mp3;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.konka.eplay.Configuration;
import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.AlwaysMarqueeTextView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.photo.PhotoActivity;
import com.konka.eplay.modules.photo.PictureInfoActivity;
import com.konka.eplay.modules.photo.QuickToast;

/**
 * @ClassName: MusicFileInfoActivity
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年4月20日 下午6:44:39
 */
public class MusicFileInfoActivity extends Activity {

	private List<CommonFileInfo> mMusicFiles;
	private int mListPosition = 0;

	private TextView mOpenView;
	private TextView mDeleteView;
	private AlwaysMarqueeTextView mSongName;
	private TextView mSinger;
	private TextView mAlbum;
	private TextView mFileSize;
	private TextView mFileFormat;
	private TextView mMusicDuration;
	private TextView mBitRate;
	private TextView mFileCreatedTime;
	private AlwaysMarqueeTextView mFilePath;

	private LinearLayout mBackButton;
	// 一次来判断列表是从哪个列表传来的。true则是来则文件夹二级列表页面，false则来自所有歌曲的列表页面
	private boolean mIsFromSecondList;

	private ImageView mThumnailImage;
	private Bitmap mThumnailImageBitmap;
	private Bitmap mActivityBitmap;
	private LinearLayout mMusicInfoLayout;
	private ButtonClickListener mButtonClickListener;

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			CommonFileInfo info = mMusicFiles.get(mListPosition);
			if (null != mThumnailImageBitmap) {
				mThumnailImage.setImageBitmap(mThumnailImageBitmap);
			}
			if (null != mActivityBitmap) {
				Drawable drawable = new BitmapDrawable(mActivityBitmap);
				mMusicInfoLayout.setBackgroundDrawable(drawable);
			}else {
				mMusicInfoLayout.setBackgroundResource(R.drawable.music_bg);
			}

			mSinger.setText(info.getSinger());
			mAlbum.setText(info.getSpecial());
			mMusicDuration.setText(info.getTime());
			if (0 != info.getBitrate()) {
				mBitRate.setText(info.getBitrate() / 1000 + "kbps");
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (!parseIntent(getIntent())) {
			return;
		}
		setContentView(R.layout.musicinfo_layout);

		initViews();

		showAllInfo();
		// printinfo();

	}

	/**
	 * @Title: showAllInfo
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return void
	 */
	private void showAllInfo() {
		final CommonFileInfo info = mMusicFiles.get(mListPosition);
		String name = info.getName();
		mSongName.setText(name.subSequence(0, name.length() - 4));

		DecimalFormat df = new DecimalFormat("#.##");
		String size = df.format(info.getSize() / 1024.0 / 1024.0);
		mFileSize.setText(size + "M");

		int index = info.getPath().lastIndexOf(".");
		mFileFormat.setText(info.getPath().substring(index + 1));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		String sDateTime = sdf.format(info.getCreatedTime());
		mFileCreatedTime.setText(sDateTime);

		mFilePath.setText(info.getParentPath());

		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] result = MusicUtils.getSingleSongInfo(info.getPath());
				info.setSinger(result[1]);
				info.setTime(result[3]);
				info.setTitle(result[2]);
				info.setSpecial(result[0]);
				info.setBitrate(Integer.valueOf(result[4].trim()));
				getCover(mMusicFiles, mListPosition, false);
				mHandler.sendEmptyMessage(0);
			}
		}).start();

	}

	private void getCover(List<CommonFileInfo> mList, int position, boolean isPause) {
		mThumnailImageBitmap = null;

		String url = "";
		if (mList.isEmpty()) {
			return;
		}
		url = mList.get(position).getPath();
		Trace.Info("###lhq 传入MP3类的本地路径为 -->" + mList.get(position).getPath());
		Mp3 m = null;
		try {
			m = new Mp3(url, false, true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null == m) {
				// mAlbumBitmap = getBigerBitmap(null, isPause);
				Bitmap default_bg = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_music_player_bg);
				mActivityBitmap = getBigerBitmap(default_bg, isPause, Utils.getScreenW(this), Utils.getScreenH(this));
				// mHandler.sendEmptyMessage(SHOW_COVER);
				return;
			}
		}

		if (null != m.getTagID3V2()) {
			Trace.Debug("####ID3V2不为空");
			if (m.getTagID3V2().getTagHeader().equals("ID3")) {
				Trace.Info("获取到了DI3V2标签中的数据");
				if (null != m.getTagID3V2().getTagFrame().get("APIC")) {
					APIC apic = (APIC) (m.getTagID3V2().getTagFrame().get("APIC").getContent());
					if (null != apic) {
						if (apic.pictureData != null) {
							Trace.Info("music cover byte[] is not null length-->" + apic.pictureData.length);
							byte[] buffer = apic.pictureData;

							Bitmap bm = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
							if (null != bm) {
								Trace.Info("###lhq 音乐专辑bitmap不为 空");
								bm.setDensity(Utils.getDensityDpi(this));
								mThumnailImageBitmap = getBigerBitmap(bm, isPause, 442, 442);
								mActivityBitmap = MusicUtils.blurBackgroundImage(MusicFileInfoActivity.this, bm,18);
								return;
							}
						} else if (apic.url != null) {
							Trace.Info("music cover url -->" + apic.url);
						}
					}
				}
			}
		}

		Bitmap default_bg = BitmapFactory.decodeResource(this.getResources(), R.drawable.default_music_player_bg);
		mActivityBitmap = MusicUtils.blurBackgroundImage(MusicFileInfoActivity.this, default_bg,18);

	}

	private Bitmap getBigerBitmap(Bitmap zhuanji, boolean isPause, int newWidth, int newHeight) {
		// TODO 不要用魔法书，转成dp
		// int newWidth = 442;
		// int newHeight = 442;

		Bitmap newBitmap = null;

		if (null != zhuanji) {
			float width = zhuanji.getWidth();
			float height = zhuanji.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;

			float msx_scale = Math.max(scaleWidth, scaleHeight);
			matrix.postScale(msx_scale, msx_scale);
			newBitmap = Bitmap.createBitmap(zhuanji, 0, 0, (int) width, (int) height, matrix, true);
		}

		Bitmap bitmap = null;
		bitmap = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);
		bitmap.setDensity(Utils.getDensityDpi(this));
		Canvas canvas = new Canvas(bitmap);

		if (null != newBitmap) {
			Trace.Info("专辑图片bitmap不为空");
			canvas.drawBitmap(newBitmap, 0, 0, null);
		}
		return bitmap;
	}

	/**
	 * @Title: printinf
	 * @Description: 测试用
	 * @return void
	 */
	private void printinfo() {
		CommonFileInfo info = mMusicFiles.get(mListPosition);
		Trace.Info("name==" + info.getName());
		Trace.Info("path==" + info.getPath());
		Trace.Info("parent path==" + info.getParentPath());
		Trace.Info("singer==" + info.getSinger());
		Trace.Info("special==" + info.getSpecial());
		int index = info.getPath().lastIndexOf(".");
		Trace.Info("special==" + info.getPath().substring(index + 1));

		DecimalFormat df = new DecimalFormat("#.##");
		String size = df.format(info.getSize() / 1024.0 / 1024.0);

		Trace.Info("size==" + size);
		Trace.Info("time==" + info.getTime());
		Trace.Info("title==" + info.getTitle());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		String sDateTime = sdf.format(info.getCreatedTime());
		Trace.Info("create time==" + sDateTime);
		// Calendar calendar = new ca

	}

	/**
	 * @Title: initViews
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return void
	 * @throws
	 */
	private void initViews() {

		mOpenView = (TextView) findViewById(R.id.music_info_open);
		mDeleteView = (TextView) findViewById(R.id.music_info_delete);

		mSongName = (AlwaysMarqueeTextView) findViewById(R.id.musicinfo_name);
		mSinger = (TextView) findViewById(R.id.musicinfo_singer);
		mAlbum = (TextView) findViewById(R.id.musicinfo_album);
		mFileSize = (TextView) findViewById(R.id.musicinfo_size);
		mFileFormat = (TextView) findViewById(R.id.musicinfo_format);
		mMusicDuration = (TextView) findViewById(R.id.musicinfo_duration);
		mBitRate = (TextView) findViewById(R.id.musicinfo_bitrate);
		mFileCreatedTime = (TextView) findViewById(R.id.musicinfo_date);
		mFilePath = (AlwaysMarqueeTextView) findViewById(R.id.musicinfo_path);
		mBackButton = (LinearLayout) findViewById(R.id.musicinfo_back_btn);

		mThumnailImage = (ImageView) findViewById(R.id.musicinfo_thumnail);

		mMusicInfoLayout = (LinearLayout) findViewById(R.id.musicinfo_root);

		mButtonClickListener = new ButtonClickListener();
		mOpenView.setOnClickListener(mButtonClickListener);
		mDeleteView.setOnClickListener(mButtonClickListener);
		mBackButton.setOnClickListener(mButtonClickListener);

		mOpenView.requestFocus();
		mBackButton.setNextFocusDownId(R.id.music_info_open);

	}

	/**
	 * @Title: parseIntent
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param intent
	 */
	private boolean parseIntent(Intent intent) {
		if (intent == null) {
			finish();
			return false;
		}
		String action = intent.getAction();
		if (action.equals(Constant.MUSIC_INFO)) {

			if (intent.hasExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE)) {
				String where = intent.getStringExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE);
				if (where.equals(MusicPlayerActivity.SONGLIST_FROM_MUSICFILELISTACTIVITY)) {
					mIsFromSecondList = false;
					mMusicFiles = MusicActivity.getAllMusicList();
				} else if (where.equals(MusicPlayerActivity.SONGLIST_FROM_MUSICSECONDLISTACTIVITY)) {
					mIsFromSecondList = true;
					mMusicFiles = MusicSecondListActivity.getAllSecondMusicList();
				}else if(where.equals(MusicPlayerActivity.SONGLIST_FROM_MUSICLIKELISTACTIVITY)){
					mIsFromSecondList=false;
					mMusicFiles=MusicActivity.getLikeList();
				}
			}

			if (intent.hasExtra(Constant.PLAY_INDEX)) {
				int index = intent.getIntExtra(Constant.PLAY_INDEX, 0);
				mListPosition = index;
				Trace.Info("index value -->" + index);
			} else {
				mListPosition = 0;
			}

			return true;
		}

		return false;

	}

	// 显示删除对话框
	private void showDeleteDialog() {
		final Dialog dialog = new Dialog(MusicFileInfoActivity.this, R.style.delete_dialog);
		// 去掉标题栏
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.delete_dialog, null);

		TextView message = (TextView) view.findViewById(R.id.info_message);
		message.setText(MusicUtils.getResourceString(MusicFileInfoActivity.this, R.string.music_info_if_delete));

		view.findViewById(R.id.decideButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				// 删除操作
				Trace.Debug("####showDeleteDialog delete confirm");
				String filePath = mMusicFiles.get(mListPosition).getPath();
				File file = new File(filePath);
				if (file != null && file.exists()) {
					String where = getIntent().getStringExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE);
					if(!where.equals(MusicPlayerActivity.SONGLIST_FROM_MUSICLIKELISTACTIVITY))
					mMusicFiles.remove(mListPosition);
					PhotoActivity.deleteFile(MusicActivity.getAllMusicFile(), filePath);
					 file.delete();

					// 此处在外层还要进行刷新一下显示
					 Configuration.ISMUSICDELETED=true;
				}
				QuickToast.showToast(MusicFileInfoActivity.this, "删除歌曲成功！");
				// 退出详情页Activity
//				setResult(RESULT_OK, getIntent());
				MusicFileInfoActivity.this.finish();
			}
		});

		view.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		view.findViewById(R.id.cancelButton).requestFocus();

		dialog.setContentView(view);
		Window window = dialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dialog.show();
	}

	class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.music_info_open:
				if (mIsFromSecondList) {
					MusicSecondListActivity.hideLikeButton();
					Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
					i.putExtra("MUSICPATH", "");
					i.putExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE,
									MusicPlayerActivity.SONGLIST_FROM_MUSICSECONDLISTACTIVITY);
					i.putExtra(Constant.PLAY_INDEX, mListPosition);
					startActivity(i);
				}else {
					AllSongListFragment.hideLikeButton();
					Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
					i.putExtra("MUSICPATH", "");
					i.putExtra(MusicPlayerActivity.SONGLIST_FROM_WHERE,
									MusicPlayerActivity.SONGLIST_FROM_MUSICFILELISTACTIVITY);
					i.putExtra(Constant.PLAY_INDEX, mListPosition);
					startActivity(i);
				}

				finish();
				break;
			case R.id.music_info_delete:
				showDeleteDialog();
				break;

			case R.id.musicinfo_back_btn:
				finish();
				break;

			default:
				break;
			}

		}
	}

}
