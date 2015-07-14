package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.konka.eplay.CurrentLauncher;
import com.konka.eplay.CurrentLauncher.LauncherInfoData;
import com.konka.eplay.R;
import com.konka.eplay.Constant;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.music.MusicPlayerService;
import com.konka.eplay.modules.music.MusicUtils;
import com.konka.eplay.modules.photo.FoldingAndOpenLayout.OnFoldListener;
import com.konka.eplay.modules.photo.music.MusicEntryInPhotoDialog;
import com.konka.eplay.modules.photo.music.Blur;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.graphics.BitmapFactory;

/**
 * 
 * Created on: 2015-3-30
 * 
 * @brief 图片浏览Activity
 * @author mcsheng
 * @date Latest modified on: 2015-4-1
 * @version V1.0.00
 * 
 */
public class ImageViewPagerActivity extends Activity {

	private KeyViewPager mViewPager;

	// 左右箭头
	private ImageView mLeftArrowBrower, mRightArrowBrower;

	private RelativeLayout mMenuLayoutPictureBrower;
	private LinearLayout mMenuBar;

	// 各个按钮
	private ImageView mScaleMenu;
	private ImageView mClockwiseRotationMenu;
	private ImageView mSlideShowMenu;
	private ImageView mWallPagerMenu;
	private ImageView mAddMenu;
	private ImageView mDeleteMenu;
	private ImageView mMusicMenu;

	private TextView mScaleMenuTextView;
	private TextView mClockwiseRotationMenuTextView;
	private TextView mSlideShowMenuTextView;
	private TextView mWallPagerMenuTextView;
	private TextView mAddMenuTextView;
	private TextView mDeleteMenuTextView;
	private TextView mMusicMenuTextView;

	private FrameLayout mPictureBrowerFrameLayout;

	private MusicEntryInPhotoDialog mMusicDialog;

	private ImageViewPagerAdapter mImageViewPagerAdapter;
	private List<String> mList = new ArrayList<String>();

	public static final int SCREEN_4K_WIDTH = 3840;
	public static final int SCREEN_4K_HEIGHT = 2160;

	// 当前保留的Bitmap，可供缩放页使用
	public static Bitmap sCurrentBitmap = null;

	private int mIndex = -1;

	private int mPosition = -1;
	// 标签页值
	private int mColorIndex = -1;

	private static ImageViewPagerActivity sActivity = null;

	// 自动消失
	private static final int AUTO_DISAPPEAR = 1;
	// 等待dialog消失
	private static final int PROGRESS_DIALOG_DISMISS = 2;
	// 设置壁纸返回的信息值
	private static final String WALL_PAGER_BOOLEAN = "WallPager_Boolean";
	// 控制栏自动消失的时间
	private final int AUTO_DISAPPEAR_TIME = 5000;
	// 添加PopupWindow的x和y的位置
	private final int ADD_POPUPWINDOW_X = 170;
	private final int ADD_POPUPWINDOW_Y = 450;

	private final int TRAN_ANIMATION_TIME = 50;

	private PopupWindow mAddPopupWindow = null;

	private Dialog mProgressDialog = null;
	//音乐播放服务
	private MusicPlayerService mMusicPlayerService = null;


	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == AUTO_DISAPPEAR) {
				// mMenuLayoutPictureBrower.setVisibility(View.GONE);
				// mMenuBar.setVisibility(View.GONE);
				// mLeftArrowBrower.setVisibility(View.GONE);
				// mRightArrowBrower.setVisibility(View.GONE);
				ImageViewPagerActivity.this.exitAnimation();
				// 当addPopupWindow存在时，dismiss
				if (mAddPopupWindow != null && mAddPopupWindow.isShowing()) {
					mAddPopupWindow.dismiss();
				}

				mViewPager.requestFocus();
			} else if (msg.what == PROGRESS_DIALOG_DISMISS) {
				Bundle data = msg.getData();
				if (data.getBoolean(WALL_PAGER_BOOLEAN)) {
					// 设置壁纸成功
					cancelProgressDialog();
					QuickToast.showToast(
									ImageViewPagerActivity.this,
									ImageViewPagerActivity.this
													.getString(R.string.set_wallpager_success));
				} else {
					// 设置壁纸失败
					cancelProgressDialog();
					QuickToast.showToast(
									ImageViewPagerActivity.this,
									ImageViewPagerActivity.this
													.getString(R.string.set_wallpager_error));
				}
			}
		}
	};;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}

	@SuppressWarnings("static-access")
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ImageLoader.getInstance(this).releaseInstance();
		//清空
		sCurrentBitmap = null;
		KeyScaleImageView.sRotateBitmap = null;
		KeyScaleImageView.sHasRotate = false;
		sActivity = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mViewPager.setCanScroll(true);
	}

	public static ImageViewPagerActivity getInstance() {
		return sActivity;
	}

	private void initView() { 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_image_viewpager);

		initProgressDialog();

		mViewPager = (KeyViewPager) findViewById(R.id.image_viewpager);
		mLeftArrowBrower = (ImageView) findViewById(R.id.left_arrow_viewpager);
		mRightArrowBrower = (ImageView) findViewById(R.id.right_arrow_viewpager);
		mMenuLayoutPictureBrower = (RelativeLayout) findViewById(R.id.menu_layout_picture_brower);
		mMenuBar = (LinearLayout) findViewById(R.id.menu_bar_viewpager);

		mMusicMenu = (ImageView) findViewById(R.id.music_menu);
		mMusicMenuTextView = (TextView) findViewById(R.id.music_menu_textView);

		mScaleMenu = (ImageView) findViewById(R.id.scale_menu);
		mScaleMenuTextView = (TextView) findViewById(R.id.scale_menu_textView);

		mClockwiseRotationMenu = (ImageView) findViewById(R.id.clockwise_rotation_menu);
		mClockwiseRotationMenuTextView = (TextView) findViewById(R.id.clockwise_rotation_menu_textView);

		mSlideShowMenu = (ImageView) findViewById(R.id.slide_show_menu);
		mSlideShowMenuTextView = (TextView) findViewById(R.id.slide_show_menu_textView);

		mWallPagerMenu = (ImageView) findViewById(R.id.wallpaper_menu);
		mWallPagerMenuTextView = (TextView) findViewById(R.id.wallpaper_menu_textView);

		mAddMenu = (ImageView) findViewById(R.id.add_menu);
		mAddMenuTextView = (TextView) findViewById(R.id.add_menu_textView);

		mDeleteMenu = (ImageView) findViewById(R.id.delete_menu);
		mDeleteMenuTextView = (TextView) findViewById(R.id.delete_menu_textView);

		mPictureBrowerFrameLayout = (FrameLayout) findViewById(R.id.picture_brower_frameLayout);
		
		mLeftArrowBrower.setOnClickListener(mOnClickListener);
		mRightArrowBrower.setOnClickListener(mOnClickListener);
		mMusicMenu.setOnClickListener(mOnClickListener);
		mScaleMenu.setOnClickListener(mOnClickListener);
		mClockwiseRotationMenu.setOnClickListener(mOnClickListener);
		mSlideShowMenu.setOnClickListener(mOnClickListener);
		mWallPagerMenu.setOnClickListener(mOnClickListener);
		mAddMenu.setOnClickListener(mOnClickListener);
		mDeleteMenu.setOnClickListener(mOnClickListener);

		mPictureBrowerFrameLayout.setOnHoverListener(mOnHoverListener);

		mMusicMenu.setOnKeyListener(mOnKeyListener);
		mScaleMenu.setOnKeyListener(mOnKeyListener);
		mClockwiseRotationMenu.setOnKeyListener(mOnKeyListener);
		mSlideShowMenu.setOnKeyListener(mOnKeyListener);
		mWallPagerMenu.setOnKeyListener(mOnKeyListener);
		mAddMenu.setOnKeyListener(mOnKeyListener);
		mDeleteMenu.setOnKeyListener(mOnKeyListener);

		mMusicMenu.setOnFocusChangeListener(mOnFocusChangeListener);
		mScaleMenu.setOnFocusChangeListener(mOnFocusChangeListener);
		mClockwiseRotationMenu.setOnFocusChangeListener(mOnFocusChangeListener);
		mSlideShowMenu.setOnFocusChangeListener(mOnFocusChangeListener);
		mWallPagerMenu.setOnFocusChangeListener(mOnFocusChangeListener);
		mAddMenu.setOnFocusChangeListener(mOnFocusChangeListener);
		mDeleteMenu.setOnFocusChangeListener(mOnFocusChangeListener);

		LauncherInfoData ld = new CurrentLauncher(getApplicationContext())
						.getCurrentLauncher();
		// 判断当前主题是否支持设置壁纸,不支持则设置不设置壁纸按钮不可见
		if (ld != null
						&& ld.GetPackageName().equals("com.konka.livelauncher")
						&& (ld.GetVersionCode() < 700 || ld.getVerseionName()
										.startsWith("V4."))) {
			mWallPagerMenu.setVisibility(View.GONE);
		}

		mMusicPlayerService = MusicPlayerService.getInstance();
		
		if(mMusicDialog == null) {
			mMusicDialog = new MusicEntryInPhotoDialog(ImageViewPagerActivity.this,
					R.style.progressDialog_holo);
			
			mMusicDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {						
				@Override
				public void onDismiss(DialogInterface dialog) {					
					final View view = mViewPager.findViewById(mViewPager.getCurrentItem());
					View viewGroup = (View) view.getParent().getParent().getParent();
					final ImageView blurImageView = (ImageView) viewGroup.findViewById(R.id.blur_imageview_viewpager);
					final FoldingAndOpenLayout foldingLayout = (FoldingAndOpenLayout) view.getParent().getParent();		
					final FrameLayout blackFrameLayout = (FrameLayout) viewGroup.findViewById(R.id.black_mask_viewpager);
					foldingLayout.setOnFoldListener(new OnFoldListener() {
						
						@Override
						public void onStartFold() {
							Trace.Debug("####OnFoldListener onStartFold");
						}
						
						@Override
						public void onEndFold() {
							Trace.Debug("####OnFoldListener onEndFold");
						}

						@Override
						public void onRecoverFold() {
							Trace.Debug("####OnFoldListener onRecoverFold");
							//使黑色蒙罩出现，档住页面打开时出现的闪烁现象
							blackFrameLayout.setVisibility(View.VISIBLE);
							blurImageView.setVisibility(View.GONE);
							view.setVisibility(View.VISIBLE);
							Handler handler = new Handler();
							handler.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									//延时一段时间，等到闪烁现象消失时才黑色蒙罩消失，这样就可以看到连贯的页面关闭和打开动画
									blackFrameLayout.setVisibility(View.GONE);
								}
							}, 5);
							
						}
					});
					foldingLayout.startFoldAnimation();									
				}
			});					
		}
		
		initData();

		sActivity = ImageViewPagerActivity.this;

	}

	private void initProgressDialog() {
		mProgressDialog = new Dialog(ImageViewPagerActivity.this,
						R.style.delete_dialog);
		View view = View.inflate(ImageViewPagerActivity.this,
						R.layout.loading_image_dialog, null);
		mProgressDialog.setContentView(view);
		mProgressDialog.setCancelable(false);
		mProgressDialog.getWindow().setLayout(
						Utils.dip2px(ImageViewPagerActivity.this, 300),
						Utils.dip2px(ImageViewPagerActivity.this, 100));
		AnimationDrawable anim = (AnimationDrawable) view.findViewById(
						R.id.loading_imageview).getBackground();
		if (anim != null) {
			anim.start();
		}
	}

	@SuppressWarnings("static-access")
	private void initData() {

		//显示加载等待Dialog
		showProgressDialog(R.string.wait_picture_load);
		// 清空
		mList.clear();

		// 清空一下图片缓存，已避免浏览缩略图和播放缩略图名称一致，而从缓存中取浏览缩略图
		// ImageLoader.getInstance(this).clearCache();

		Intent intent = ImageViewPagerActivity.this.getIntent();
		// 获取指定的位置索引
		int index = intent.getIntExtra(Constant.PLAY_INDEX, -1);

		mIndex = index;

		mPosition = mIndex;

		mColorIndex = intent.getIntExtra(Constant.COLOR_INDEX, -1);
		Trace.Debug("####initData colorIndex is " + mColorIndex);
		if (mColorIndex != -1) {
			List<CommonFileInfo> list = PhotoActivity
							.getLikePhotoList(mColorIndex);
			// 筛选出路径
			for (CommonFileInfo file : list) {
				mList.add(file.getPath());
			}
		} else {
			// 筛选出路径
			for (CommonFileInfo file : PhotoActivity.mSendList) {
				mList.add(file.getPath());
			}
		}

		mImageViewPagerAdapter = new ImageViewPagerAdapter(
						ImageViewPagerActivity.this, mList);

		mViewPager.setAdapter(mImageViewPagerAdapter);
		// 切换到指定页
		mViewPager.setCurrentItem(mIndex);
		
		if (PhotoActivity.mSendList.get(mIndex).getIsBlue()) {
			mAddMenu.setImageResource(R.drawable.tag_blue_unselected);
		} else if (PhotoActivity.mSendList.get(mIndex).getIsRed()) {
			mAddMenu.setImageResource(R.drawable.tag_red_unselected);
		} else if (PhotoActivity.mSendList.get(mIndex).getIsYellow()) {
			mAddMenu.setImageResource(R.drawable.tag_yellow_unselected);
		} else {
			mAddMenu.setImageResource(R.drawable.add);
		}

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				if (mViewPager.isCanScroll()) {
					// 切换后即设置不旋转，复位
					KeyScaleImageView.sHasRotate = false;
					QuickToast.showToast(ImageViewPagerActivity.this,
									(arg0 + 1) + "/" + KeyViewPager.sSize);
					
					if (PhotoActivity.mSendList.get(arg0).getIsBlue()) {
						mAddMenu.setImageResource(R.drawable.tag_blue_unselected);
					} else if (PhotoActivity.mSendList.get(arg0).getIsRed()) {
						mAddMenu.setImageResource(R.drawable.tag_red_unselected);
					} else if (PhotoActivity.mSendList.get(arg0).getIsYellow()) {
						mAddMenu.setImageResource(R.drawable.tag_yellow_unselected);
					} else {
						mAddMenu.setImageResource(R.drawable.add);
					}
					
				}

				// 记录位置
				mPosition = arg0;

				// 用于幻灯片退回后的取消加载框
				cancelProgressDialog();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				switch (arg0) {
				// 滑动结束
				case ViewPager.SCROLL_STATE_IDLE:
					Trace.Debug("####onPageScrollStateChanged SCROLL_STATE_IDLE");
					break;
				// 用手指拖拉
				case ViewPager.SCROLL_STATE_DRAGGING:
					Trace.Debug("####onPageScrollStateChanged SCROLL_STATE_DRAGGING");
					break;
				// 正在滑动
				case ViewPager.SCROLL_STATE_SETTLING:
					Trace.Debug("####onPageScrollStateChanged SCROLL_STATE_SETTLING");
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 获取被选中的序号
	 */
	public int getIndex() {
		return mIndex;
	}

	public void resetTime() {
        mHandler.removeMessages(AUTO_DISAPPEAR);
        Message msg = mHandler.obtainMessage(AUTO_DISAPPEAR);
        mHandler.sendMessageDelayed(msg, AUTO_DISAPPEAR_TIME);
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.left_arrow_viewpager:
				Trace.Debug("####onClick left_arrow");
				mViewPager.setCanScroll(true);
				// 到第一张，此时再按左箭头的处理
				if (mViewPager.getCurrentItem() - 1 == -1) {
					QuickToast.showToast(
									ImageViewPagerActivity.this,
									ImageViewPagerActivity.this
													.getString(R.string.first_picture));
					return;
				}

				if (((Boolean) true).equals(mViewPager.findViewById(
								mViewPager.getCurrentItem() - 1).getTag(
								R.id.tag_load))) {
					mViewPager.revertRotate();
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
				} else {
					// QuickToast.showToast(ImageViewPagerActivity.this,
					// ImageViewPagerActivity.this.getString(R.string.wait_picture_load));
					mViewPager.revertRotate();
					View viewGroup = (View) (mViewPager.findViewById(mViewPager
									.getCurrentItem() - 1)).getParent();
					viewGroup.findViewById(R.id.progressBar_viewpager)
									.setVisibility(View.VISIBLE);
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
				}
				break;
			case R.id.right_arrow_viewpager:
				Trace.Debug("####onClick right_arrow");
				mViewPager.setCanScroll(true);
				// 到最后一张，此时再按右箭头的处理
				if (mViewPager.getCurrentItem() + 1 == mViewPager.sSize) {
					QuickToast.showToast(
									ImageViewPagerActivity.this,
									ImageViewPagerActivity.this
													.getString(R.string.last_picture));
					return;
				}
				// 通过给每个子View设置一个id来查找对应的子View，并根据其加载的Tag来判断是否加载完成
				if (((Boolean) true).equals(mViewPager.findViewById(
								mViewPager.getCurrentItem() + 1).getTag(
								R.id.tag_load))) {
					mViewPager.revertRotate();
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				} else {
					// QuickToast.showToast(ImageViewPagerActivity.this,
					// ImageViewPagerActivity.this.getString(R.string.wait_picture_load));
					mViewPager.revertRotate();
					View viewGroup = (View) (mViewPager.findViewById(mViewPager
									.getCurrentItem() + 1)).getParent();
					viewGroup.findViewById(R.id.progressBar_viewpager)
									.setVisibility(View.VISIBLE);
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				}
			
				break;
			case R.id.music_menu: {								
//				if(mMusicPlayerService != null && mMusicPlayerService.getmMediaPlayer() !=
//						null && mMusicPlayerService.getmMediaPlayer().isPlaying()) {
//					mMusicPlayerService.pause();
//					mMusicMenu.setImageResource(R.drawable.music_entry_photo_select);
//					return;
//				}
				
				ImageViewPagerActivity.this.exitAnimation();
				//模糊化背景处理		
				View view = mViewPager.findViewById(mViewPager.getCurrentItem());
				
				View viewGroup = (View) view.getParent();
				ImageView blurImageView = (ImageView) viewGroup.findViewById(R.id.blur_imageview_viewpager);
				//先从缓存中，若找不到再进行缩放处理以节省运行时间
				String filePath = mList.get(mViewPager.getCurrentItem());
				Bitmap bitmap = ImageLoader.getInstance(ImageViewPagerActivity.this).getBitmapFromCache(filePath);
				if(bitmap == null) {
					Trace.Debug("####onClick Music Menu blur default image");
					Matrix localMatrix = new Matrix();
					Bitmap b = (Bitmap) view.getTag(R.id.tag_bitmap);
					float scaleW = 320 / (b.getWidth() * 1.0f);
					float scaleH = 180 / (b.getHeight() * 1.0f);
					float scale = (scaleW < scaleH) ? scaleW : scaleH;
					localMatrix.postScale(scale, scale);
					bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), 
							b.getHeight(), localMatrix, true);
				}

				View viewGroup2 = (View) view.getParent().getParent().getParent();
				FoldingAndOpenLayout foldingLayout = (FoldingAndOpenLayout) view.getParent().getParent();		
				FrameLayout blackFrameLayout = (FrameLayout) viewGroup2.findViewById(R.id.black_mask_viewpager);
				
				blackFrameLayout.setVisibility(View.GONE);
				foldingLayout.stopFoldAnimation();
				
				Bitmap tmpBitmap = Blur.fastblur(ImageViewPagerActivity.this,
						bitmap,18);
				
				blurImageView.setVisibility(View.VISIBLE);
				view.setVisibility(View.GONE);
				
				if (tmpBitmap == null) {
					tmpBitmap = BitmapUtils.bitmapFromResource(getResources(), 
							R.drawable.photo_open_failed, 0, 0);
				}
				
				blurImageView.setImageBitmap(tmpBitmap);

				mMusicDialog.show();
			}
				break;
			case R.id.scale_menu: {
				if (((Boolean)false).equals(mViewPager.findViewById(mViewPager.getCurrentItem())
						.getTag(R.id.tag_load))) {
					QuickToast.showToast(ImageViewPagerActivity.this, getString(R.string.wait_for_picture));
					break;
				}
				String path = mList.get(mViewPager.getCurrentItem());
				PictureInfo pictureInfo = new PictureInfo(
								ImageViewPagerActivity.this, path);
				if (pictureInfo.getType() == null) {
					QuickToast.showToast(
							ImageViewPagerActivity.this,
							ImageViewPagerActivity.this
											.getString(R.string.picture_no_support));
				} else if (pictureInfo.getType().equals(PictureInfo.GIF)) {
					QuickToast.showToast(
									ImageViewPagerActivity.this,
									ImageViewPagerActivity.this
													.getString(R.string.gif_no_support));
				} else {
					// Intent传输的bytes不能超过40k
					Intent intent = new Intent(ImageViewPagerActivity.this,
									ImageScaleActivity.class);
					sCurrentBitmap = mViewPager.getBitmapInCurrentView();
					startActivity(intent);
				}
			}
				break;
			case R.id.clockwise_rotation_menu:
				if (((Boolean)false).equals(mViewPager.findViewById(mViewPager.getCurrentItem())
						.getTag(R.id.tag_load))) {
					QuickToast.showToast(ImageViewPagerActivity.this, getString(R.string.wait_for_picture));
					break;
				}
				mViewPager.rotate(true);
				break;
			case R.id.slide_show_menu: {
				if (mList.size() == 1) {
					QuickToast.showToast(
									ImageViewPagerActivity.this,
									ImageViewPagerActivity.this
													.getString(R.string.slide_noly_one));
					break;
				}
				Intent intent = new Intent();
				intent.putStringArrayListExtra(ImageSlideActivity.TAG,
								(ArrayList<String>) mList);
				intent.putExtra(ImageSlideActivity.TAG_INDEX,
								mViewPager.getCurrentItem());
				intent.setClass(ImageViewPagerActivity.this,
								ImageSlideActivity.class);
				startActivity(intent);
			}
				break;
			case R.id.wallpaper_menu:
				if (((Boolean)false).equals(mViewPager.findViewById(mViewPager.getCurrentItem())
						.getTag(R.id.tag_load))) {
					QuickToast.showToast(ImageViewPagerActivity.this, getString(R.string.wait_for_picture));
					break;
				}
				showProgressDialog(R.string.set_the_wallpaper_please_wait);
				new Thread(new Runnable() {

					@Override
					public void run() {
						Bundle data = new Bundle();
						data.putBoolean(WALL_PAGER_BOOLEAN,
										mViewPager.setWallPager());
						Message msg = new Message();
						msg.what = PROGRESS_DIALOG_DISMISS;
						msg.setData(data);
						mHandler.sendMessage(msg);
					}
				}).start();

				break;
			case R.id.add_menu:
				showAddPopupWindow();
				break;
			case R.id.delete_menu:
				showDeleteDialog();
				break;
			default:
				break;
			}
		}
	};

	private OnClickListener mAddOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Trace.Debug("##click");
			CommonFileInfo fileInfo = PhotoActivity.mSendList.get(mPosition);
			FrameLayout frameLayout = (FrameLayout) v.getParent().getParent();
			TextView redTextView = (TextView) frameLayout
							.findViewById(R.id.text_red);
			TextView yellowTextView = (TextView) frameLayout
							.findViewById(R.id.text_yellow);
			TextView blueTextView = (TextView) frameLayout
							.findViewById(R.id.text_blue);

			ImageViewPagerActivity.this.setResult(4);

			switch (v.getId()) {

			case R.id.photo_details_popupwindow_red_imageView:
				Trace.Debug("##click red");
				if (fileInfo.getIsRed()) {
					fileInfo.setIsRed(false);
					redTextView.setText(R.string.mark_red);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.RED);
				} else {
					redTextView.setText(R.string.cancel_mark);
					blueTextView.setText(R.string.mark_blue);
					yellowTextView.setText(R.string.mark_yellow);
					fileInfo.setIsRed(true);
					fileInfo.setIsBlue(false);
					fileInfo.setIsYellow(false);
					PhotoActivity.addLikePhoto(fileInfo,PhotoActivity.RED);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.BLUE);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.YELLOW);
				}
				QuickToast.showToast(
								ImageViewPagerActivity.this,
								ImageViewPagerActivity.this
												.getString(R.string.tag_success));
				
				mAddMenu.setImageResource(R.drawable.tag_red_selected);
				
				mAddPopupWindow.dismiss();
				break;
			case R.id.photo_details_popupwindow_yellow_imageView:
				Trace.Debug("##click yellow");
				if (fileInfo.getIsYellow()) {
					fileInfo.setIsYellow(false);
					yellowTextView.setText(R.string.mark_yellow);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.YELLOW);
				}else {
					yellowTextView.setText(R.string.cancel_mark);
					blueTextView.setText(R.string.mark_blue);
					redTextView.setText(R.string.mark_red);
					fileInfo.setIsYellow(true);
					fileInfo.setIsBlue(false);
					fileInfo.setIsRed(false);
					PhotoActivity.addLikePhoto(fileInfo, PhotoActivity.YELLOW);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.BLUE);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.RED);
				}
				QuickToast.showToast(
								ImageViewPagerActivity.this,
								ImageViewPagerActivity.this
												.getString(R.string.tag_success));
				
				mAddMenu.setImageResource(R.drawable.tag_yellow_selected);
				
				mAddPopupWindow.dismiss();
				break;
			case R.id.photo_details_popupwindow_blue_imageView:
				Trace.Debug("##click blue");
				if (fileInfo.getIsBlue()) {
					fileInfo.setIsBlue(false);
					blueTextView.setText(R.string.mark_blue);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.BLUE);
				} else {
					blueTextView.setText(R.string.cancel_mark);
					redTextView.setText(R.string.mark_red);
					yellowTextView.setText(R.string.mark_yellow);
					fileInfo.setIsBlue(true);
					fileInfo.setIsYellow(false);
					fileInfo.setIsRed(false);
					PhotoActivity.addLikePhoto(fileInfo, PhotoActivity.BLUE);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.RED);
					PhotoActivity.deleteLikePhoto(fileInfo, PhotoActivity.YELLOW);
				}
				
				mAddMenu.setImageResource(R.drawable.tag_blue_selected);
				
				QuickToast.showToast(
								ImageViewPagerActivity.this,
								ImageViewPagerActivity.this
												.getString(R.string.tag_success));
				mAddPopupWindow.dismiss();
				break;
			default:
				break;
			}
		}
	};

	@SuppressWarnings("deprecation")
	private void showAddPopupWindow() {
		View view = View.inflate(this, R.layout.picture_add_popupwindow, null);

		ImageView addRed = (ImageView) view
						.findViewById(R.id.photo_details_popupwindow_red_imageView);
		ImageView addYellow = (ImageView) view
						.findViewById(R.id.photo_details_popupwindow_yellow_imageView);
		ImageView addBlue = (ImageView) view
						.findViewById(R.id.photo_details_popupwindow_blue_imageView);
		TextView redtext = (TextView) view.findViewById(R.id.text_red);
		TextView bluetext = (TextView) view.findViewById(R.id.text_blue);
		TextView yellowtext = (TextView) view.findViewById(R.id.text_yellow);

		// 设置显示
		if (PhotoActivity.mSendList.get(mPosition).getIsBlue()) {
			bluetext.setText(R.string.cancel_mark);
		} else if (PhotoActivity.mSendList.get(mPosition).getIsRed()) {
			redtext.setText(R.string.cancel_mark);
		} else if (PhotoActivity.mSendList.get(mPosition).getIsYellow()) {
			yellowtext.setText(R.string.cancel_mark);
		}

		mAddPopupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT, true);

		mAddPopupWindow.setAnimationStyle(R.style.PictureTagPopupAnimation);

		// 为了自己能够捕获返回键和自行处理，注释掉此句
		mAddPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mAddPopupWindow.showAtLocation(mAddMenu, Gravity.TOP,
						Utils.dip2px(this, ADD_POPUPWINDOW_X),
						Utils.dip2px(this, ADD_POPUPWINDOW_Y));
		addYellow.requestFocus();
		mAddMenu.setBackgroundColor(this.getResources().getColor(
						R.color.transparent));
		mAddPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override
			public void onDismiss() {
				mAddMenu.setBackgroundDrawable(ImageViewPagerActivity.this
								.getResources()
								.getDrawable(R.drawable.picture_menu_bar_selector));
			}
		});

		addRed.setOnClickListener(mAddOnClickListener);
		addYellow.setOnClickListener(mAddOnClickListener);
		addBlue.setOnClickListener(mAddOnClickListener);

		// addRed.setOnKeyListener(mAddOnKeyListener);
		// addYellow.setOnKeyListener(mAddOnKeyListener);
		// addBlue.setOnKeyListener(mAddOnKeyListener);
	}

	// 显示删除对话框
	private void showDeleteDialog() {
		final Dialog dialog = new Dialog(ImageViewPagerActivity.this,
						R.style.delete_dialog);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.delete_dialog, null);

		view.findViewById(R.id.decideButton).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
								// 删除操作
								Trace.Debug("####showDeleteDialog delete confirm");
								ImageViewPagerAdapter adapter = (ImageViewPagerAdapter) mViewPager
												.getAdapter();
								List<String> list = adapter.getPathList();
								int position = mViewPager.getCurrentItem();
								String filePath = list.get(position);
								PhotoActivity.deleteFile(PhotoActivity
												.getLikePhotoList(1), filePath);
								PhotoActivity.deleteFile(PhotoActivity
												.getLikePhotoList(2), filePath);
								PhotoActivity.deleteFile(PhotoActivity
												.getLikePhotoList(3), filePath);
								File file = new File(filePath);
								if (file != null) {
									// 删除文件
									file.delete();
									// 从外部链表进行删除
									if (mColorIndex != -1) {
										Trace.Debug("####showDeleteDialog delete color list");
										// 处理标签Activity的入口
										List<CommonFileInfo> colorList = PhotoActivity
														.getLikePhotoList(mColorIndex);
										colorList.remove(position);
									} else {
										Trace.Debug("####showDeleteDialog delete send list");
										// 处理正常浏览播放入口
										PhotoActivity.mSendList
														.remove(position);
							}
									// ImageViewPagerActivity.this.setResult(4);
								}
								list.remove(position);
								// 当删除完之后就退出Activity
								if (list.size() == 0) {
									ImageViewPagerActivity.this.finish();
								}
								mMenuLayoutPictureBrower
												.setVisibility(View.GONE);
								mMenuBar.setVisibility(View.GONE);
								// 刷新
								adapter.notifyDataSetChanged();
								PhotoActivity.setIsDelete(true);
								QuickToast.showToast(
												ImageViewPagerActivity.this,
												ImageViewPagerActivity.this
																.getString(R.string.delete_success));
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
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dialog.show();
	}

	public void showProgressDialog(int stringid) {
		// AnimationDrawable anim = (AnimationDrawable)
		// mProgressDialogView.findViewById(R.id.loading_imageview).getBackground();
		// if(anim != null) {
		// anim.start();
		// }
		String str = ImageViewPagerActivity.this.getString(stringid);
		TextView textView = (TextView) mProgressDialog
						.findViewById(R.id.loading_textView);
		textView.setText(str);
		mProgressDialog.show();
	}

	public void cancelProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	public KeyViewPager getKeyViewPager() {
		return mViewPager;
	}

	private OnKeyListener mOnKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {

			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				resetTime();
			}

			if (event.getAction() == KeyEvent.ACTION_DOWN
							&& keyCode == KeyEvent.KEYCODE_MENU) {
				if (mMenuLayoutPictureBrower.getVisibility() == View.VISIBLE) {
					Trace.Debug("####onKey menu");
					// mMenuLayoutPictureBrower.setVisibility(View.GONE);
					// mMenuBar.setVisibility(View.GONE);
					mLeftArrowBrower.setVisibility(View.GONE);
					mRightArrowBrower.setVisibility(View.GONE);
					ImageViewPagerActivity.this.exitAnimation();
					return true;
				}
			} else if (event.getAction() == KeyEvent.ACTION_DOWN
							&& keyCode == KeyEvent.KEYCODE_BACK) {
				if (mMenuLayoutPictureBrower.getVisibility() == View.VISIBLE) {
					Trace.Debug("####onKey back");
					// mMenuLayoutPictureBrower.setVisibility(View.GONE);
					ImageViewPagerActivity.this.exitAnimation();
					return true;
				}
			}

			return false;
		}
	};

	private OnKeyListener mAddOnKeyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {

			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				resetTime();
			}
			return false;
		}
	};

	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			ImageView imageView = (ImageView) v;
			switch (v.getId()) {
			case R.id.music_menu:
				if (hasFocus) {
					//判断音乐是否在播放，以是否显示波形动画
					if(mMusicPlayerService != null && mMusicPlayerService.getmMediaPlayer() !=
							null && mMusicPlayerService.getmMediaPlayer().isPlaying()) {
						mMusicMenu.setImageResource(R.anim.music_in_photo_select_wave_anim);
						AnimationDrawable anim = (AnimationDrawable) mMusicMenu.getDrawable();
						if(anim != null) {
							anim.start();
						}
					} else {
						mMusicMenu.setImageResource(R.drawable.music_entry_photo_select);
					}
					
					mMusicMenuTextView.setVisibility(View.VISIBLE);
				} else {
					
					if(mMusicPlayerService != null && mMusicPlayerService.getmMediaPlayer() !=
							null && mMusicPlayerService.getmMediaPlayer().isPlaying()) {
						mMusicMenu.setImageResource(R.anim.music_in_photo_no_select_wave_anim);
						AnimationDrawable anim = (AnimationDrawable) mMusicMenu.getDrawable();
						if(anim != null) {
							anim.start();
						}
					} else {
						mMusicMenu.setImageResource(R.drawable.music_entry_photo_no_select);
					}
					
					mMusicMenuTextView.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.scale_menu:
				if (hasFocus) {
					imageView.setImageResource(R.drawable.scale_selected);
					mScaleMenuTextView.setVisibility(View.VISIBLE);
				} else {
					imageView.setImageResource(R.drawable.scale);
					mScaleMenuTextView.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.clockwise_rotation_menu:
				if (hasFocus) {
					imageView.setImageResource(R.drawable.clockwise_rotation_selected);
					mClockwiseRotationMenuTextView.setVisibility(View.VISIBLE);
				} else {
					imageView.setImageResource(R.drawable.clockwise_rotation);
					mClockwiseRotationMenuTextView
									.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.slide_show_menu:
				if (hasFocus) {
					imageView.setImageResource(R.drawable.slide_selected);
					mSlideShowMenuTextView.setVisibility(View.VISIBLE);
				} else {
					imageView.setImageResource(R.drawable.slide);
					mSlideShowMenuTextView.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.wallpaper_menu:
				if (hasFocus) {
					imageView.setImageResource(R.drawable.wallpager_selected);
					mWallPagerMenuTextView.setVisibility(View.VISIBLE);
				} else {
					imageView.setImageResource(R.drawable.wallpager);
					mWallPagerMenuTextView.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.add_menu:
				if (hasFocus) {
					if (PhotoActivity.mSendList.get(mPosition).getIsBlue()) {
						mAddMenu.setImageResource(R.drawable.tag_blue_selected);
					} else if (PhotoActivity.mSendList.get(mPosition).getIsRed()) {
						mAddMenu.setImageResource(R.drawable.tag_red_selected);
					} else if (PhotoActivity.mSendList.get(mPosition).getIsYellow()) {
						mAddMenu.setImageResource(R.drawable.tag_yellow_selected);
					} else {
						imageView.setImageResource(R.drawable.add_selected);
					}
					
					mAddMenuTextView.setVisibility(View.VISIBLE);
				} else {
					if (PhotoActivity.mSendList.get(mPosition).getIsBlue()) {
						mAddMenu.setImageResource(R.drawable.tag_blue_unselected);
					} else if (PhotoActivity.mSendList.get(mPosition).getIsRed()) {
						mAddMenu.setImageResource(R.drawable.tag_red_unselected);
					} else if (PhotoActivity.mSendList.get(mPosition).getIsYellow()) {
						mAddMenu.setImageResource(R.drawable.tag_yellow_unselected);
					} else {
						imageView.setImageResource(R.drawable.add);
					}
					
					mAddMenuTextView.setVisibility(View.INVISIBLE);
				}
				break;
			case R.id.delete_menu:
				if (hasFocus) {
					imageView.setImageResource(R.drawable.delete_selected);
					mDeleteMenuTextView.setVisibility(View.VISIBLE);
				} else {
					imageView.setImageResource(R.drawable.delete);
					mDeleteMenuTextView.setVisibility(View.INVISIBLE);
				}
				break;
			default:
				break;
			}
		}
	};

	private OnHoverListener mOnHoverListener = new OnHoverListener() {

		@Override
		public boolean onHover(View v, MotionEvent event) {
			Trace.Debug("####onHover " + event.getRawX() + ","
							+ event.getRawY());
			int what = event.getAction();
			resetTime();
			Trace.Debug("####onHover what is " + what);
			switch (what) {
			case MotionEvent.ACTION_MOVE:
				Trace.Debug("####onHover ACTION_MOVE");
				break;
			case MotionEvent.ACTION_HOVER_ENTER: // 鼠标在view上
				Trace.Debug("####onHover ACTION_HOVER_ENTER");
			case MotionEvent.ACTION_HOVER_MOVE: // 鼠标在view上移动
				Trace.Debug("####onHover ACTION_HOVER_MOVE");
				if (mMenuLayoutPictureBrower.getVisibility() == View.GONE) {
					mMenuLayoutPictureBrower.setVisibility(View.VISIBLE);
					mMenuBar.setVisibility(View.VISIBLE);
					mRightArrowBrower.setVisibility(View.VISIBLE);
					mLeftArrowBrower.setVisibility(View.VISIBLE);
				}
                break;
			case MotionEvent.ACTION_HOVER_EXIT: // 鼠标离开view 点击会先执行这个，然后再执行enter
				Trace.Debug("####onHover ACTION_HOVER_EXIT");
				mViewPager.setCanScroll(false);
			default:
				break;
			}

			return true;
		}
	};

	public void enterAnimation() {
		Trace.Debug("####enterAnimation");
		int distance = mMenuBar.getHeight();
		TranslateAnimation translateAnimation = new TranslateAnimation(0, 0,
						distance, 0);
		translateAnimation.setDuration(TRAN_ANIMATION_TIME);
		translateAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation paramAnimation) {

			}

			@Override
			public void onAnimationRepeat(Animation paramAnimation) {

			}

			@Override
			public void onAnimationEnd(Animation paramAnimation) {
				mMenuLayoutPictureBrower.setVisibility(View.VISIBLE);
				mMenuBar.setVisibility(View.VISIBLE);
			}
		});

		mMenuBar.startAnimation(translateAnimation);
	}

	public void exitAnimation() {
		int distance = mMenuBar.getHeight();
		TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0,
						distance);
		translateAnimation.setDuration(TRAN_ANIMATION_TIME);
		translateAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation paramAnimation) {

			}

			@Override
			public void onAnimationRepeat(Animation paramAnimation) {

			}

			@Override
			public void onAnimationEnd(Animation paramAnimation) {
				mMenuLayoutPictureBrower.setVisibility(View.GONE);
			}
		});

		mMenuBar.startAnimation(translateAnimation);
	}

	public static void finishActivity() {
		if (getInstance()!=null) {
			
		getInstance().finish();
		}
	}
}