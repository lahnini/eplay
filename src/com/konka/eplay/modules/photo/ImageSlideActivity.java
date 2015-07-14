package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.konka.eplay.Configuration;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.photo.SandGlassImageView.OnSandGlassEnd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * 
 * Created on: 2015-3-30
 * 
 * @brief 图片幻灯片Activity
 * @author mcsheng
 * @date Latest modified on: 2015-5-15
 * @version V1.0.00
 * 
 */
public class ImageSlideActivity extends Activity {
	
	public static final String TAG = "ImageSlideActivity";
	public static final String TAG_INDEX = "index";
	private ImageView mPlayImageView;
	private CustomScrollViewPager mViewPager;
	private SeekBar mSeekBar;
	private ImageViewPagerAdapter mImageViewPagerAdapter;
	
	private SandGlassImageView mSandGlassImageView;
	private TextView mSandTimeTextView;
	private  Dialog mDialog;
	
	private enum ViewPagerState {
		NONE,//初始化的状态
		SLIDE,//幻灯片的状态
	}
	
	// 自动播放的间隔时间
	private int mAutoRunIntervalTime = 5000;
	private int mAutoRunIndex = 0;
	private static final int FINISH = 2;
	private Boolean mIsAutoRunning = false;
	private ViewPagerState mode = ViewPagerState.NONE;
	
	//幻灯片的所播放到的页数
	private int mIndex = -1;
	//开始的页数
	private int mInitIndex = -1;
	
	private ImageView mImageView5sBack, mImageView10sBack, mImageView15sBack;
	private ImageView mImageView5sSelect, mImageView10sSelect, mImageView15sSelect;
	
	
	private static final String SLIDE_TIME_SAVE = "slide_time_save";
	private static final String SLIDE_TIME = "slide_time";
	private static final int FIVE_SECOND = 5000;
	private static final int TEN_SECOND = 10000;
	private static final int FIFTEEN_SECOND = 15000;
	
	List<String> mInitialList = null;
	
	private FrameLayout mPictureSlideFrameLayout;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {  
        public void handleMessage(Message msg) {  
            if(msg.what == FINISH) {
            	ImageSlideActivity.this.finish();
            	QuickToast.showToast(ImageSlideActivity.this, 
            			ImageSlideActivity.this.getString(R.string.slide_end));
            }
            super.handleMessage(msg);  
        };  
    };  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_imageslide);
		initView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mIndex != mInitIndex) {
			if(ImageViewPagerActivity.getInstance() != null) {
				ImageViewPagerActivity.getInstance().showProgressDialog(R.string.wait_picture_load);
			}
			
			//延时2秒后再进行切换，为了让加载框能够显示
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					if(ImageViewPagerActivity.getInstance() != null) {
						
						KeyViewPager viewPager = ImageViewPagerActivity.
								getInstance().getKeyViewPager();
						viewPager.setCurrentItem(mIndex, false);
					}
					
				}
			}, 2000);
		}
		
	}
	
	private void initView() {
		mPictureSlideFrameLayout = (FrameLayout) findViewById(R.id.picture_slide_frameLayout);
		mPlayImageView = (ImageView) findViewById(R.id.play_slide);
		mViewPager = (CustomScrollViewPager) findViewById(R.id.image_slide_viewpager);
		mSeekBar = (SeekBar) findViewById(R.id.image_seekbar);
		
		mSandGlassImageView = (SandGlassImageView) findViewById(R.id.sand_glass_view);
		mSandTimeTextView = (TextView) findViewById(R.id.sand_time_textView);
		
		mPictureSlideFrameLayout.setOnHoverListener(mOnHoverListener);
		
		Intent intent = this.getIntent(); 
		mIndex = intent.getIntExtra(TAG_INDEX, 0);
		mInitIndex = mIndex;
		mInitialList = intent.getStringArrayListExtra(TAG);
		
		List<String> firstList = mInitialList.subList(mIndex, mInitialList.size());
		List<String> secondList = mInitialList.subList(0, mIndex);
		List<String> lastList = new ArrayList<String>();
		
		lastList.addAll(firstList);
		lastList.addAll(secondList);
		
		//使SeekBar不能够被拖动
		mSeekBar.setEnabled(false);
		mSeekBar.setMax(mInitialList.size());
		mSeekBar.setProgress(1);
		
		mImageViewPagerAdapter = new ImageViewPagerAdapter(this, lastList);
		
		//开启循环播放模式
		mImageViewPagerAdapter.setLoopPlay(true);
		
		mViewPager.setAdapter(mImageViewPagerAdapter);
		//设置切换动画
		mViewPager.setPageTransformer(true, new CubeOutTransformer());
		//mViewPager.setCurrentItem(mIndex);
		mViewPager.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(mode == ViewPagerState.SLIDE && event.getAction() == KeyEvent.ACTION_DOWN) {
					switch(keyCode) {
					case KeyEvent.KEYCODE_DPAD_LEFT:
						QuickToast.showToast(ImageSlideActivity.this, 
								ImageSlideActivity.this.getString(R.string.slide_no_support_switch));
						return true;
					case KeyEvent.KEYCODE_DPAD_RIGHT:
						QuickToast.showToast(ImageSlideActivity.this, 
								ImageSlideActivity.this.getString(R.string.slide_no_support_switch));
						return true;
					case KeyEvent.KEYCODE_ENTER:
						if(mIsAutoRunning) {
							mPlayImageView.setVisibility(View.VISIBLE);
							pauseAutoRun();
						} else {
							mPlayImageView.setVisibility(View.GONE);
							resumeAutoRun();
						}
						return true;
					case KeyEvent.KEYCODE_MENU:
						showSetSlideDialog();
						return false;
					default:
						break;
					}
				}
				return false;
			}
		});	
		
		initSlideTimeSave();
		
		
		mSandGlassImageView.setOnSandGlassEnd(new OnSandGlassEnd() {
			
			@Override
			public void onTimeUp(SandGlassImageView imageView) {
				Trace.Debug("####Auto Run " + mAutoRunIndex);
				View view = mViewPager.findViewById(mAutoRunIndex % mImageViewPagerAdapter.getPathList().size());
            	if(view != null && ((Boolean)true).equals(view.getTag(R.id.tag_load))) {
            		mViewPager.setCanScroll(true);
            		mViewPager.setCurrentItem(mAutoRunIndex);
//            		mSeekBar.setProgress(mAutoRunIndex + 1);   
            		
            		mAutoRunIndex++;
            		mIndex++;
            	} else {
            		//不可见时显示等待加载框
            		mViewPager.setCanScroll(true);
            		mViewPager.setCurrentItem(mAutoRunIndex);
            		
            		View viewGroup = (View) (mViewPager.findViewById(mAutoRunIndex % mImageViewPagerAdapter
            				.getPathList().size())).getParent();
    				viewGroup.findViewById(R.id.progressBar_viewpager).setVisibility(View.VISIBLE);
            		
            		mAutoRunIndex++;
            		mIndex++;
            		
            	}
				
            	//复位mIndex  此时mIndex记录的是播放Activity的位置
            	if(mIndex == mInitialList.size()) {
            		mIndex = 0;
            	}
            	
            	//开启循环播放后，理论上这里是不会执行的，因为mViewPager.getAdapter().getCount()足够大，使播放近乎无穷
				if (mAutoRunIndex == mViewPager.getAdapter().getCount()) {
					mAutoRunIndex = 0;
					stopAutoRun();
					
					Message message = new Message();  
			        message.what = FINISH;  
			        mHandler.sendMessageDelayed(message, mAutoRunIntervalTime);
				}
			}
		});
		
		mSandGlassImageView.setSandTimeView(mSandTimeTextView);
		startAutoRun();
	}
	
	//初始化SharePreferences，用于保存幻灯片播放时间
	private void initSlideTimeSave() {
		// 生成SharedPreferences实例 保存在/data/data/包名/shared_prefs
		int time = Configuration.getInt(SLIDE_TIME_SAVE, SLIDE_TIME);
		Trace.Debug("####initSlideTimeSave " + time);
		if(time == -1) {
			setAutoRunTime(FIVE_SECOND);
			Configuration.save(SLIDE_TIME_SAVE, SLIDE_TIME, FIVE_SECOND);
		} else {
			setAutoRunTime(time);
		}

	}
	

	//开启定时自动播放(即幻灯片播放)
	private void startAutoRun() {		
		Trace.Debug("####startAutoRun");
		
		if(mode != ViewPagerState.SLIDE && !mIsAutoRunning) {
			mIsAutoRunning = true;
			mode = ViewPagerState.SLIDE;
			mAutoRunIndex = mViewPager.getCurrentItem() + 1;
			mSandGlassImageView.setSandEndTime(mAutoRunIntervalTime);
			mSandGlassImageView.startSandGlassAnimation();
		}
	}
	 

	//暂停播放
	private void pauseAutoRun() {
		
		if(mode == ViewPagerState.SLIDE && mIsAutoRunning) {
			
			mIsAutoRunning = false;
			mSandGlassImageView.pauseRun();
			System.gc();
		}
	}
	

	//恢复播放
	private void resumeAutoRun() {
		
		if(mode == ViewPagerState.SLIDE && !mIsAutoRunning) {
			mSandGlassImageView.resumeRun();
			mIsAutoRunning = true;
		}
	}
	
	

	//停止播放
	private void stopAutoRun() {					
		mode = ViewPagerState.NONE;
		mIsAutoRunning = false;
		mAutoRunIndex = 0;
		System.gc();
	}
	
	//设置幻灯片播放时间
	private void setAutoRunTime(int time) {
		mAutoRunIntervalTime = time;
	}
	
	private void showSetSlideDialog() {
		//暂停幻灯片播放
		pauseAutoRun();
		
		mDialog = new Dialog(ImageSlideActivity.this,R.style.delete_dialog);
		//去掉标题栏
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(ImageSlideActivity.this);
		View view = inflater.inflate(R.layout.slide_time_dialog, null);
		mImageView5sBack = (ImageView) view.findViewById(R.id.FrameLayout_5s_imageView_back);
		mImageView10sBack = (ImageView) view.findViewById(R.id.FrameLayout_10s_imageView_back);
		mImageView15sBack = (ImageView) view.findViewById(R.id.FrameLayout_15s_imageView_back);
		
		mImageView5sSelect = (ImageView) view.findViewById(R.id.linearLayout_5s_imageView_selected);
		mImageView10sSelect = (ImageView) view.findViewById(R.id.linearLayout_10s_imageView_selected);
		mImageView15sSelect = (ImageView) view.findViewById(R.id.linearLayout_15s_imageView_selected);
		
		mImageView5sBack.setOnClickListener(mOnClickListener);
		mImageView10sBack.setOnClickListener(mOnClickListener);
		mImageView15sBack.setOnClickListener(mOnClickListener);
		
		mDialog.setContentView(view);
		
		Window window = mDialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		//更改显示位置  居中时lp.x=0,lp.y=0,即屏幕的坐标原点正好位于屏幕的正中间。
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.x = 0;
		lp.y = Utils.getScreenH(ImageSlideActivity.this) / 4;
		
		mImageView5sBack.requestFocus();
		
		mDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				if(mPlayImageView.getVisibility() == View.VISIBLE) {
					//空操作
				} else if(mPlayImageView.getVisibility() == View.GONE) {
					resumeAutoRun();
				}
			}
		});
		
		int time = Configuration.getInt(SLIDE_TIME_SAVE, SLIDE_TIME);
		if(time == -1) {
			setAutoRunTime(FIVE_SECOND);
			Configuration.save(SLIDE_TIME_SAVE, SLIDE_TIME, FIVE_SECOND);
			mImageView5sSelect.setImageResource(R.drawable.slide_time_selected);
			mImageView10sSelect.setImageResource(R.drawable.slide_time_not_selected);
			mImageView15sSelect.setImageResource(R.drawable.slide_time_not_selected);
			mImageView5sBack.requestFocus();
		} else {
			if(time == FIVE_SECOND) {
				mImageView5sSelect.setImageResource(R.drawable.slide_time_selected);
				mImageView10sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView15sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView5sBack.requestFocus();
			} else if(time == TEN_SECOND) {
				mImageView10sSelect.setImageResource(R.drawable.slide_time_selected);
				mImageView5sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView15sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView10sBack.requestFocus();
			} else if(time == FIFTEEN_SECOND) {
				mImageView15sSelect.setImageResource(R.drawable.slide_time_selected);
				mImageView5sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView10sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView15sBack.requestFocus();
			}
		}
		
		mDialog.show();
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View paramView) {
			switch(paramView.getId()) {
			case R.id.FrameLayout_5s_imageView_back:
				mImageView5sSelect.setImageResource(R.drawable.slide_time_selected);
				mImageView10sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView15sSelect.setImageResource(R.drawable.slide_time_not_selected);
				setAutoRunTime(FIVE_SECOND);
				mSandGlassImageView.setSandEndTime(FIVE_SECOND);
				mSandGlassImageView.zeroAnimationTick();
				Configuration.save(SLIDE_TIME_SAVE, SLIDE_TIME, FIVE_SECOND);
				if(mDialog != null) {
					mDialog.dismiss();
				}
				break;
			case R.id.FrameLayout_10s_imageView_back:
				mImageView5sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView10sSelect.setImageResource(R.drawable.slide_time_selected);
				mImageView15sSelect.setImageResource(R.drawable.slide_time_not_selected);
				setAutoRunTime(TEN_SECOND);
				mSandGlassImageView.setSandEndTime(TEN_SECOND);
				mSandGlassImageView.zeroAnimationTick();
				Configuration.save(SLIDE_TIME_SAVE, SLIDE_TIME, TEN_SECOND);
				if(mDialog != null) {
					mDialog.dismiss();
				}
				break;
			case R.id.FrameLayout_15s_imageView_back:
				mImageView5sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView10sSelect.setImageResource(R.drawable.slide_time_not_selected);
				mImageView15sSelect.setImageResource(R.drawable.slide_time_selected);
				setAutoRunTime(FIFTEEN_SECOND);
				mSandGlassImageView.setSandEndTime(FIFTEEN_SECOND);
				mSandGlassImageView.zeroAnimationTick();
				Configuration.save(SLIDE_TIME_SAVE, SLIDE_TIME, FIFTEEN_SECOND);
				if(mDialog != null) {
					mDialog.dismiss();
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
			Trace.Debug("####onHover "+ event.getRawX() + "," + event.getRawY());
			int what = event.getAction();
			Trace.Debug("####onHover what is " + what);
			switch(what) {
			case MotionEvent.ACTION_MOVE:
				Trace.Debug("####onHover ACTION_MOVE");
				break;
			case MotionEvent.ACTION_HOVER_ENTER:  //鼠标在view上
            	Trace.Debug("####onHover ACTION_HOVER_ENTER");
            case MotionEvent.ACTION_HOVER_MOVE:  //鼠标在view上移动
            	Trace.Debug("####onHover ACTION_HOVER_MOVE"); 
                break;  
            case MotionEvent.ACTION_HOVER_EXIT:  //鼠标离开view  点击会先执行这个，然后再执行enter
            	Trace.Debug("####onHover ACTION_HOVER_EXIT");
            	mViewPager.setCanScroll(false);
                if(mIsAutoRunning) {
					mPlayImageView.setVisibility(View.VISIBLE);
					pauseAutoRun();
				} else {
					mPlayImageView.setVisibility(View.GONE);
					resumeAutoRun();
				}
            default:
            	break;
			}
			
			return true;
		}
	};

}