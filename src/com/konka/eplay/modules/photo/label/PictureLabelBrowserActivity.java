
package com.konka.eplay.modules.photo.label;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.List;

import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.modules.AlwaysMarqueeTextView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.ScrollGridView;
import com.konka.eplay.modules.music.MusicPlayerService;
import com.konka.eplay.modules.photo.ImageLoader;
import com.konka.eplay.modules.photo.ImageViewPagerActivity;
import com.konka.eplay.modules.photo.ImageViewPagerAdapter;
import com.konka.eplay.modules.photo.PhotoActivity;
import com.konka.eplay.modules.photo.PhotoDetailsPopupWindow;
import com.konka.eplay.modules.photo.QuickToast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PictureLabelBrowserActivity extends Activity {

	private static PictureLabelBrowserActivity sActivity;
	private ScrollGridView mGridView;
	private TextView mNameTextView;
	private PictureLabelAdapter mPictureLabelAdapter;
	private List<CommonFileInfo> mList;
	private FrameLayout mTopBtnFrameLayout;
	private ImageView mTopBtnImageView;
	private TextView mClearTextView;
	private TextView mColorTextView;
	private RelativeLayout mHeadRelativeLayout;

	//音乐提示相关控件  add by xuyunyu
	FrameLayout mMusicTipLayout;
	ImageView mMusicWave;
	AlwaysMarqueeTextView mSongName;
	private MusicPlayerService mPlayerService;
	private Context mcontext;

	private int mColorIndex = -1;
	private static final int REQUEST_CODE = 1;
	private Boolean mScrollEnd = false;

	private int mFirstVisibleItem = -1;
	private int mVisibleItemCount = -1;
	private int mTotalItemCount = -1;

	public void onEventMainThread(IEvent event) {

		if (event instanceof EventMusicStateChange) {

			switch (((EventMusicStateChange) event).musicStateType) {
			case Constant.MUSIC_SERVICE_FLAG_CHANGE_SONG:
				updateMuisicInfo();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PLAY:
				playMuisicAnimation();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_PAUSE:
				stopMuisicAnimation();
				break;
			case Constant.MUSIC_SERVICE_FLAG_SONG_STOP:
				stopMuisicAnimation();
				mMusicTipLayout.setVisibility(View.INVISIBLE);
				break;
			}

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();

		//add by xuyunyu
		mPlayerService = MusicPlayerService.getInstance();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (null != mPlayerService && !mPlayerService.getPlayState()) {
			mMusicTipLayout.setVisibility(View.INVISIBLE);
			return;
		}
		playMuisicAnimation();
		// 注册接收event事件
		EventDispatchCenter.getInstance().register(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopMuisicAnimation();
		// 不再接收event事件
		EventDispatchCenter.getInstance().unregister(this);
	}

	/*
	* @Description: 开启音乐提示音乐跳动
	 */
	private void playMuisicAnimation() {

		Trace.Info("play animation");
		if (null == mPlayerService) {
			mMusicTipLayout.setVisibility(View.INVISIBLE);
		} else if (null != mPlayerService.getmMediaPlayer()) {
			if (mPlayerService.getmMediaPlayer().isPlaying()) {
				mMusicTipLayout.setVisibility(View.VISIBLE);
				AnimationDrawable anim = (AnimationDrawable) mMusicWave.getBackground();
				anim.start();
				mSongName.setText(mPlayerService.getMusicName());
			} else {
				mMusicTipLayout.setVisibility(View.VISIBLE);
				mSongName.setText(mPlayerService.getMusicName());
			}
		}
	}

	/*
	* @Description: 关闭音乐提示音乐跳动
	 */
	private void stopMuisicAnimation() {

		mMusicTipLayout.setVisibility(View.VISIBLE);
		if (null != mMusicWave && mMusicWave.getBackground() != null
				&& mMusicWave.getBackground() instanceof AnimationDrawable) {
			AnimationDrawable anim = (AnimationDrawable) mMusicWave.getBackground();
			if (anim != null && anim.isRunning()) { // 如果正在运行,就停止
				anim.stop();
			}
		}
	}

	private void updateMuisicInfo() {
		if (null != mPlayerService) {
			mSongName.setText(mPlayerService.getMusicName());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picture_label);
		sActivity=PictureLabelBrowserActivity.this;
		mGridView = (ScrollGridView) findViewById(R.id.label_gridview);
		mNameTextView = (TextView) findViewById(R.id.local_text_view);
		mTopBtnFrameLayout = (FrameLayout) findViewById(R.id.local_top_btn_frameLayout);
		mTopBtnImageView = (ImageView) findViewById(R.id.local_top_btn);
		mClearTextView = (TextView) findViewById(R.id.label_clear_all);
		mColorTextView = (TextView) findViewById(R.id.label_color_textView);
		mHeadRelativeLayout = (RelativeLayout) findViewById(R.id.label_head_layout);

		//add by xuyunyu
		mMusicTipLayout = (FrameLayout) findViewById(R.id.photo_lable_music_tip_layout);
		mMusicWave = (ImageView) findViewById(R.id.photo_lable_music_wave);
		mSongName = (AlwaysMarqueeTextView) findViewById(R.id.photo_lable_music_songname);
		mMusicWave.setBackgroundResource(R.drawable.music_wave_anim);
		mMusicTipLayout.setOnClickListener(mOnClickListener);
	}

	private void initData() {
		Intent intent = getIntent();
		mColorIndex = intent.getIntExtra(Constant.LABEL, -1);
		switch (mColorIndex) {
		case PhotoActivity.RED:
			mColorTextView.setText(this.getString(R.string.label_red));
			break;
		case PhotoActivity.BLUE:
			mColorTextView.setText(this.getString(R.string.label_blue));
			break;
		case PhotoActivity.YELLOW:
			mColorTextView.setText(this.getString(R.string.label_yellow));
			break;
		default:
			break;
		}
		mList = PhotoActivity.getLikePhotoList(mColorIndex);
		Trace.Debug("mList.size="+mList.size());
		if (mList.size() == 0) {
			QuickToast.showToast(this, getString(R.string.no_to_tag));
			finish();
		}

		mPictureLabelAdapter = new PictureLabelAdapter(this, mList);
		//设置BorderView
		mGridView.setBorderView((ImageView) this.findViewById(R.id.border_view_in_gridView));

		mGridView.setAdapter(mPictureLabelAdapter);
		mGridView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> paramAdapterView,
							View paramView, int paramInt, long paramLong) {
				mNameTextView.setVisibility(View.VISIBLE);
				mNameTextView.setText(Utils.getWrapperPath(mList.get(paramInt).getPath())) ;
				Trace.Debug("####onItemSelected "
								+ mGridView.getFirstVisiblePosition());
				// 用于响应topButton到第一个Item时的处理
				if (paramInt == 0) {
					mTopBtnFrameLayout.setVisibility(View.GONE);
					// 先让清空不能获取到焦点，为的是让GridView优先获取到焦点
					mClearTextView.setFocusable(false);
					mHeadRelativeLayout.setFocusable(false);
					paramView.requestFocus();
					mClearTextView.setFocusable(true);
					mHeadRelativeLayout.setFocusable(true);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> paramAdapterView) {
			}
		});

		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> paramAdapterView,
							View paramView, int paramInt, long paramLong) {
				Intent intent = new Intent();
				intent.setAction(Constant.PLAY_IMAGE_ACTION);
				intent.putExtra(Constant.PLAY_INDEX, paramInt);
				intent.putExtra(Constant.COLOR_INDEX, mColorIndex);
				// startActivity(intent);
				startActivityForResult(intent, REQUEST_CODE);
			}
		});

		mGridView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View paramView, boolean paramBoolean) {
				if (!paramBoolean) {
					mNameTextView.setVisibility(View.GONE);
				} else {
					GridView parent = (GridView) paramView;
					int selPos = parent.getSelectedItemPosition();
					mNameTextView.setVisibility(View.VISIBLE);
					mNameTextView.setText(Utils.getWrapperPath(mList.get(selPos).getPath()));
				}
			}
		});

		mGridView.setOnKeyListener(mOnKeyListener);
		mClearTextView.setOnClickListener(mOnClickListener);
		mTopBtnImageView.setOnClickListener(mOnClickListener);
		mHeadRelativeLayout.setOnClickListener(mOnClickListener);

		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView paramAbsListView,
							int paramInt) {
				Trace.Debug("###onScrollStateChanged");
				switch (paramInt) {
				// 滚动结束
				case OnScrollListener.SCROLL_STATE_IDLE:
					Trace.Debug("###onScrollStateChanged stop");

					if(mGridView.getFirstVisiblePosition() > 3) {
						mTopBtnFrameLayout.setVisibility(View.VISIBLE);
					} else {
						mTopBtnFrameLayout.setVisibility(View.GONE);
					}

					if(mGridView.isScrollToTop()) {
						mPictureLabelAdapter.setScrollTopMark(false);
						//mPictureLabelAdapter.notifyDataSetChanged();
						break;
					}

					//滚动结束才让所需线程运行
					ImageLoader.getInstance(PictureLabelBrowserActivity.this).unLockLoad();

					if(mGridView.getScrollDirection() == Constant.SCROLL_UP) {
						ImageLoader.getInstance(PictureLabelBrowserActivity.this).
						cancelTaskInList(mFirstVisibleItem, mFirstVisibleItem + 7, Constant.SCROLL_UP);
					} else if(mGridView.getScrollDirection() == Constant.SCROLL_DOWN) {
						ImageLoader.getInstance(PictureLabelBrowserActivity.this).
						cancelTaskInList(mFirstVisibleItem, mFirstVisibleItem + 7, Constant.SCROLL_DOWN);
					}

					break;
				// 正在滚动
				case OnScrollListener.SCROLL_STATE_FLING:
					Trace.Debug("###onScrollStateChanged fling");
					if(mGridView.isScrollToTop()) {
						mPictureLabelAdapter.setScrollTopMark(true);
						break;
					}
					//滚动时锁住线程创建运行，避免滚动卡顿
					ImageLoader.getInstance(PictureLabelBrowserActivity.this).lockLoad();
					break;
				default:
					break;
				}

			}

			@Override
			public void onScroll(AbsListView paramAbsListView,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
				Trace.Debug("###onScroll");
				mFirstVisibleItem = firstVisibleItem;
				mVisibleItemCount = visibleItemCount;
				mTotalItemCount = totalItemCount;
			}
		});

	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View paramView) {
			switch (paramView.getId()) {
			case R.id.local_top_btn:
				Trace.Debug("####onClick top");
				//mGridView.setSelection(0);
				mGridView.smoothScrollToTop();
				break;
			case R.id.label_clear_all:
//				mList.clear();
//				mPictureLabelAdapter.notifyDataSetChanged();
//				QuickToast.showToast(getBaseContext(), "还没有标记任何图片");
//				finish();
//				Trace.Debug("####onClick clear all");
				showClearTagDialog();
				
			

				break;
			case R.id.label_head_layout:
				Trace.Debug("####onClick back finish");
				PictureLabelBrowserActivity.this.finish();
				break;
			case R.id.photo_lable_music_tip_layout:
				Intent i = new Intent(Constant.PLAY_MUSIC_ACTION);
				i.putExtra("isBackGround", true);
				startActivity(i);
				break;
			default:
				break;
			}
		}
	};

	private OnKeyListener mOnKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View paramView, int paramInt,
						KeyEvent paramKeyEvent) {

			GridView parent = (GridView) paramView;

			if (paramKeyEvent.getAction() == KeyEvent.ACTION_DOWN
							&& paramKeyEvent.getKeyCode() == KeyEvent.KEYCODE_MENU) {

				int selPos = parent.getSelectedItemPosition();
				int pos = selPos - parent.getFirstVisiblePosition();
				View view = parent.getChildAt(pos);
				if (view != null) {
					showDetailsMenu(view, mGridView.getSelectedItemPosition());
					return true;
				}
			}
			return false;
		}
	};

	private void showDetailsMenu(View view, int position) {

		CommonFileInfo fileInfo = mList.get(position);
		PhotoDetailsPopupWindow popupWindow = new PhotoDetailsPopupWindow(
						PictureLabelBrowserActivity.this, fileInfo, position,
						mColorIndex);
		popupWindow.setAnimationStyle(R.style.popwin_anim_style);
		popupWindow.showAsDropDown(view,
						Utils.dip2px(PictureLabelBrowserActivity.this, 63),
						-Utils.dip2px(PictureLabelBrowserActivity.this, 191));
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				mPictureLabelAdapter.notifyDataSetChanged();
				if (mList.size() == 0) {
					QuickToast.showToast(getBaseContext(), "还没有标记任何图片");
					finish();
				}
				if (PhotoActivity.getLikePhotoList(PhotoActivity.BLUE) != null) {
					ContentManager.writeData2DB(
									getApplicationContext(),
									PhotoActivity.getLikePhotoList(PhotoActivity.BLUE),
									MultimediaType.MMT_BLUEPHOTO);
				}
				// 标为黄的照片
				if (PhotoActivity.getLikePhotoList(PhotoActivity.YELLOW) != null) {
					Trace.Debug("###writeYellowPhoto");
					ContentManager.writeData2DB(
									getApplicationContext(),
									PhotoActivity.getLikePhotoList(PhotoActivity.YELLOW),
									MultimediaType.MMT_YELLOWPHOTO);
				}

				// 标为红的照片
				if (PhotoActivity.getLikePhotoList(PhotoActivity.RED) != null) {
					Trace.Debug("###writeRedPhoto");
					Trace.Debug("##redPhotoSize##" + PhotoActivity.getLikePhotoList(PhotoActivity.RED).size());
					ContentManager.writeData2DB(
									getApplicationContext(),
									PhotoActivity.getLikePhotoList(PhotoActivity.RED),
									MultimediaType.MMT_REDPHOTO);
				}

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 更新下数据
		Trace.Debug("###onResumePictureLabel");
		mPictureLabelAdapter.notifyDataSetChanged();
	}

	private void showClearTagDialog() {
		final Dialog dialog = new Dialog(PictureLabelBrowserActivity.this,R.style.delete_dialog);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.delete_dialog, null);

		((TextView)view.findViewById(R.id.info_message)).
			setText(getString(R.string.Whether_to_empty_tag));

		view.findViewById(R.id.decideButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						//清空操作
						Trace.Debug("####showClearTagDialog clear confirm");
						mList.clear();
						mPictureLabelAdapter.notifyDataSetChanged();
						if (PhotoActivity.getLikePhotoList(PhotoActivity.BLUE) != null) {
							ContentManager.writeData2DB(
											getApplicationContext(),
											PhotoActivity.getLikePhotoList(PhotoActivity.BLUE),
											MultimediaType.MMT_BLUEPHOTO);
						}
						// 标为黄的照片
						if (PhotoActivity.getLikePhotoList(PhotoActivity.YELLOW) != null) {
							Trace.Debug("###writeYellowPhoto");
							ContentManager.writeData2DB(
											getApplicationContext(),
											PhotoActivity.getLikePhotoList(PhotoActivity.YELLOW),
											MultimediaType.MMT_YELLOWPHOTO);
						}

						// 标为红的照片
						if (PhotoActivity.getLikePhotoList(PhotoActivity.RED) != null) {
							Trace.Debug("###writeRedPhoto");
							Trace.Debug("##redPhotoSize##" + PhotoActivity.getLikePhotoList(PhotoActivity.RED).size());
							ContentManager.writeData2DB(
											getApplicationContext(),
											PhotoActivity.getLikePhotoList(PhotoActivity.RED),
											MultimediaType.MMT_REDPHOTO);
						}
						QuickToast.showToast(PictureLabelBrowserActivity.this,
								getString(R.string.tag_clear_success));
						finish();
						
					}
				});

		view.findViewById(R.id.cancelButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

		view.findViewById(R.id.cancelButton).requestFocus();

		dialog.setContentView(view);
		Window window = dialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		dialog.show();
	}
	public static PictureLabelBrowserActivity getInstance() {
		return sActivity;
	}
	public static void finishActivity() {
		if (sActivity!=null) {
			
		sActivity.finish();
		}
	}

}