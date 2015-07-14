package com.konka.eplay.modules.photo;

import java.util.Arrays;

import com.konka.eplay.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * Created on: 2015-5-14
 * 
 * @brief 沙漏动画View
 * @author mcsheng
 * @date Latest modified on: 2015-5-18
 * @version V1.0.00
 * 
 */
class SandGlassImageView extends ImageView {
	
    private int[] mOriginalPixels;
    private int[] mDownSectionPixels;
    private int[] mAnimationPixels;
    private final int HEIGHT = 48;
    private final int WIDTH = 27;    
    
    //这里为-1以避免第一次加载时不下滑像素点
    private int mAnimationTick = -1;	
    
    private static final int UPDATE = 0x01;
    private static final int ROTATE = 0x02;
    private static final int UPDATE_TIME = 200;
    private static final int FILL_WITH_COLOR = 0xffffffff;
    private static final int TRANSPARENCY_COLOR = 0X00000000;
    //每次流走的沙子量
    private static final float PER_FLOW_SAND = 5.0f;
    
    
    private Boolean mIsStop = false;
    
    private OnSandGlassEnd mOnSandGlassEnd;
    
    private Boolean mIsRotate = false;
    
    private RotateAnimation mRotateAnimation;
    
	private int mCanvasWidth = -1;
	private int mCanvasHeight = -1;
	
    private int mUpdateTime = -1;
    
    private int mTotalTime = 0;
    
    private int mRestTime = 0;
    
    private TextView mTimeTextView = null;
    
	//注意int数的除法转换为float类型后再进行除法，否则精度会失去，特别要注意
	float mA1 = (HEIGHT) / (float)WIDTH;	
	float mA2 = (-HEIGHT) / (float)WIDTH;
	float mH = (-mA2) * WIDTH;
	    
    @SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		if(msg.what == UPDATE) {
    			if(!mIsStop) {
    				mRestTime = mTotalTime - mUpdateTime * mAnimationTick;
    				if(mTimeTextView != null) {
    					mTimeTextView.setText((int)(mRestTime / 1000.0f + 0.5f) + "s");
    				}
		            SandGlassImageView.this.invalidate();
		            mHandler.sendEmptyMessageDelayed(UPDATE, mUpdateTime);
    			} 
    		} else if(msg.what == ROTATE) {
    			mRestTime = mTotalTime - mUpdateTime * mAnimationTick;
    			if(mTimeTextView != null) {
					mTimeTextView.setText((int)(mRestTime / 1000.0f + 0.5f) + "s");
				}
    			Bitmap TmpBitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Config.ARGB_8888);
    			Canvas canvas = new Canvas(TmpBitmap);
    			SandGlassImageView.this.draw(canvas);
    			
    			SandGlassImageView.this.setImageBitmap(TmpBitmap);    			
    			
    			//startAnimation之后会先onDraw重绘一次然后再开始动画
    			SandGlassImageView.this.startAnimation(mRotateAnimation);
    			if(mOnSandGlassEnd != null) {
					mOnSandGlassEnd.onTimeUp(SandGlassImageView.this);
				}
    			
    		}
    	}
    };
	
	public SandGlassImageView(Context context) {
		super(context);
		init();
	}
	
	public SandGlassImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public SandGlassImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
				
		this.setScaleType(ScaleType.CENTER);
		this.setImageResource(R.drawable.sand_glass);
		
		mOriginalPixels = new int[WIDTH * HEIGHT];
		Arrays.fill(mOriginalPixels,TRANSPARENCY_COLOR);
		
		mDownSectionPixels = new int[WIDTH * HEIGHT];
		Arrays.fill(mDownSectionPixels,TRANSPARENCY_COLOR);
		
		initSandGlass();

        mAnimationPixels = new int[mOriginalPixels.length];
        
        //旋转动画
        mRotateAnimation = new RotateAnimation(0, 180,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
        //设置500毫秒，与幻灯片切换同步
        mRotateAnimation.setDuration(500);
        mRotateAnimation.setFillAfter(true);
        mRotateAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				//动画完成后注意要清除一下动画效果，否则会影响下一次的重绘
				SandGlassImageView.this.clearAnimation();
				SandGlassImageView.this.setImageResource(R.drawable.sand_glass);

				SandGlassImageView.this.startSandGlassAnimation();
			}
		});
        
	}
	
	//初始化像素点
	private void initSandGlass() {
				
		for(int y = 0; y < HEIGHT; y++) {
            for(int x = 0; x < WIDTH; x++) {
                int index = y * WIDTH + x;
                
                float hy1 = (x * mA1);
                
                float hy2 = (x * mA2)  + mH;
                
                //上三角
                if(((float)y) < hy1 && ((float)y) < hy2) {
//                	mUpOriginalPixels[index] = FILL_WITH_COLOR;        	
                //下三角
                } else if(((float)y) > hy1 && ((float)y) > hy2) {
                	mDownSectionPixels[index] = FILL_WITH_COLOR;
                }  
                
                if(y <= HEIGHT / 2.0f) {
                	mOriginalPixels[index] = FILL_WITH_COLOR;
                } else {
                	mOriginalPixels[index] = TRANSPARENCY_COLOR;
                }
            }
        }
	}
	
    
	//刷新像素点，以显示流动效果
    private void updatePixels() {
        Arrays.fill(mAnimationPixels, TRANSPARENCY_COLOR);
        final int width = WIDTH;
        final int height = HEIGHT;
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
            	int index = y * width + x;

                float hy1 = (x * mA1);
                
                float hy2 = (x * mA2)  + mH;

                //outS表示上三角已经流走的面积
                float outS = PER_FLOW_SAND * mAnimationTick;
                
                //当显示流走的面积大于上三角的面积，表示已经流完
                if(outS > WIDTH * (HEIGHT / 2.0f) / 2.0f) {
            		mAnimationTick = 0;
            		mIsStop = true;
            		mIsRotate = true;
            		//完整填充下部分
            		mAnimationPixels = Arrays.copyOf(mDownSectionPixels, mDownSectionPixels.length);
            		return;
                }
                
                //由上三角的一元二次方程求得已经流走得高度outY
                float tmp1 = (float) (4 * Math.pow(WIDTH, 2) * Math.pow(HEIGHT / 2.0f, 2) - 4 * WIDTH * HEIGHT * outS);
                float tmp2 = (float) (Math.sqrt(tmp1) / (2.0f * WIDTH));
                float outY = (HEIGHT / 2.0f) - tmp2;                
                
                //由于判断流动是否到达底部
                float limitValue = HEIGHT / 2.0f + mAnimationTick;
                 
              //上三角
                if(((float)y) <= hy1 && ((float)y) <= hy2) {     
                	if(((float)y) > outY) {
                		mAnimationPixels[index] = FILL_WITH_COLOR;
                	}               	
                //下三角 
                } else if(((float)y) >= hy1 && ((float)y) >= hy2) {
                	if(WIDTH / 2.0f - 1.0f <= ((float)x) && ((float)x) <= WIDTH / 2.0f + 1.0f) {
                		//流动柱的显示
                		if(limitValue >= HEIGHT * 1.0f) {
                			mAnimationPixels[index] = FILL_WITH_COLOR;
                		} else {
                			if(y < limitValue) {
                				mAnimationPixels[index] = FILL_WITH_COLOR;
	                		} else {
	                			mAnimationPixels[index] = TRANSPARENCY_COLOR;
	                		} 
                		}
                	} else if(limitValue >= HEIGHT * 1.0f) {
                		//当达到底部时，下三角开始增加
                		float restY = HEIGHT - outY;
                		if(y > restY) {
                			mAnimationPixels[index] = FILL_WITH_COLOR;
                		}
                	}
                } else {
                	mAnimationPixels[index] = TRANSPARENCY_COLOR;
                }  
                                                                                                
            }                                                                                                
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {   	   	
    	if(mIsRotate) {
    		//super.onDraw会先绘制原先的（比如先前设置的setImageBitmap或setImageResource等)
    		super.onDraw(canvas);
    		//将画布定位到中间
	        canvas.translate((getWidth() * 1.0f - WIDTH)/2, (getHeight() * 1.0f - HEIGHT)/2);
    		canvas.drawBitmap(mAnimationPixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT, true, null); 
    		mIsRotate = false;
    		return;
    	} else {
    		super.onDraw(canvas);
    	}

    	//经过super.onDraw(canvas)之后，再次进行canvas.drawBitmap会在之前的画布上进行绘制Bitmap。
    	//也就是说在setImageBitmap设置的Bitmap的基础上进行覆盖绘制   	
    	if(!mIsStop) {
	    	//将画布定位到中间
	        canvas.translate((getWidth() * 1.0f - WIDTH)/2, (getHeight() * 1.0f - HEIGHT)/2);
	        mAnimationTick++;
	        updatePixels();
	        canvas.drawBitmap(mAnimationPixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT, true, null); 
	        //经过旋转之前的绘制后就开始旋转
	        if(mIsRotate) {
	        	mCanvasHeight = canvas.getHeight();
	        	mCanvasWidth = canvas.getWidth();				
	        	mHandler.sendEmptyMessage(ROTATE);
	        	return;
	        }
    	}
    }
    
    public void setOnSandGlassEnd(OnSandGlassEnd listener) {
    	mOnSandGlassEnd = listener;
    }
    
    public void startSandGlassAnimation() {
        mIsStop = false;
        mIsRotate = false;
        mHandler.sendEmptyMessage(UPDATE);
    }
    
    /**
     * 将动态计数器清0
     */
    public void zeroAnimationTick() {
    	mAnimationTick = 0;
    }
    
    /**
     * 设置沙漏流完的时间
     */
    public void setSandEndTime(int time) {
    	mTotalTime = time;
    	float totalCount = ((WIDTH * (HEIGHT / 2.0f) / 2.0f) / PER_FLOW_SAND + 0.5f);
    	mUpdateTime = (int) (time / totalCount);    	
    }
    
    /**
     * 暂停沙漏流动
     */
    public void pauseRun() {
    	mHandler.removeMessages(UPDATE);
    }
    
    /**
     * 恢复沙漏流动
     */
    public void resumeRun() {
    	mIsStop = false;
        mIsRotate = false;
        mHandler.sendEmptyMessage(UPDATE);
    }
    
    
    /**
     * 设置显示沙漏流动时间的TextView
     */
    public void setSandTimeView(TextView textView) {
    	mTimeTextView = textView;
    }
    
    /**
     * 沙漏流完监听接口
     */
    public interface OnSandGlassEnd {
    	public void onTimeUp(SandGlassImageView imageView);
    }
		
}