package com.konka.eplay.modules.photo;

import com.konka.eplay.R;

import iapp.eric.utils.base.Trace;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
*
* Created on: 2015-6-11
*
* @brief 页面折叠和打开动画效果Layout
* @author mcsheng
* @date Latest modified on: 2015-6-11
* @version V1.0.00
*
*/
public class FoldingAndOpenLayout extends ViewGroup {

	public static enum Orientation {
		VERTICAL, HORIZONTAL
	}

	private final String FOLDING_VIEW_EXCEPTION_MESSAGE = "Folding Layout can only 1 child at "
			+ "most";

	private final float SHADING_ALPHA = 0.8f;
	private final float SHADING_FACTOR = 0.5f;
	private final int DEPTH_CONSTANT = 1500;
	private final int NUM_OF_POLY_POINTS = 8;

	private Rect[] mFoldRectArray;

	private Matrix[] mMatrix;

	//默认是垂直折叠
	protected Orientation mOrientation = Orientation.VERTICAL;

	//mAnchorFactor指示哪里折叠，默认是从中间折叠
	protected float mAnchorFactor = 0.5f;
	//折叠因子，0~1  0表示不折叠，1表示完全折叠
	private float mFoldFactor = 0;

	//默认的折叠数是2个
	private int mNumberOfFolds = 2;

	private boolean mIsHorizontal = true;

	private int mOriginalWidth = 0;
	private int mOriginalHeight = 0;

	private float mFoldMaxWidth = 0;
	private float mFoldMaxHeight = 0;
	private float mFoldDrawWidth = 0;
	private float mFoldDrawHeight = 0;

	private boolean mIsFoldPrepared = false;
	private boolean mShouldDraw = true;

	//绘制阴影的Paint和Matrix
	private Paint mSolidShadow;
	private Paint mGradientShadow;
	private LinearGradient mShadowLinearGradient;
	private Matrix mShadowGradientMatrix;

	private float[] mSrc;
	private float[] mDst;

	private OnFoldListener mFoldListener;

	private Bitmap mFullBitmap;
	private Rect mDstRect;
	
	private ValueAnimator mValueAnimator = null;
	
	//当需要开启折叠动画时才将其置为true，避免在onLayout中一直updateFold导致ViewPager滑动操作卡顿的现象
	private Boolean mIsFoldAnimation = false;
	
	private static final boolean IS_JBMR2 = (Build.VERSION.SDK_INT == 18);//4.3
	private static final boolean IS_ISC = (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH);//4.0
	private static final boolean IS_GINGERBREAD_MR1 = (Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1);//2.3.3

	public FoldingAndOpenLayout(Context context) {
		super(context);
	}

	public FoldingAndOpenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FoldingAndOpenLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {
		mValueAnimator = ValueAnimator.ofFloat(0.0f,1.0f);
        mValueAnimator.setDuration(900);
        mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float rotateFactor = (Float) animation.getAnimatedValue();
				FoldingAndOpenLayout.this.setFoldFactor(rotateFactor);
			}
		});
        
        mValueAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				//首先更新一次，准备参数
				FoldingAndOpenLayout.this.updateFold();
				//动画开始时置true
				mIsFoldAnimation = true;
				if (mFoldListener != null) {
					mFoldListener.onStartFold();
				}
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {	
				if (mFoldListener != null) {
					mFoldListener.onRecoverFold();
				}
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				//置false
				mIsFoldAnimation = false;
				if (mFoldListener != null) {
					mFoldListener.onEndFold();
				}
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
        
        mValueAnimator.setRepeatCount(1);
        mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        
	}

	@Override
	protected boolean addViewInLayout(View child, int index,
			LayoutParams params, boolean preventRequestLayout) {
		//先判断是否已有child View ，若有则抛出异常
		throwCustomException(getChildCount());
		boolean returnValue = super.addViewInLayout(child, index, params,
				preventRequestLayout);
		return returnValue;
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		//先判断是否已有child View ，若有则抛出异常
		throwCustomException(getChildCount());
		super.addView(child, index, params);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		View child = getChildAt(0);
		//测量孩子的大小，从而测量自己
		measureChild(child, widthMeasureSpec, heightMeasureSpec);
		//存储得到的值widthMeasureSpec和widthMeasureSpec 不调用这个会触发IllegalStateException异常
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Trace.Debug("####onLayout");
		View child = getChildAt(0);
		//调用孩子View的layout，实际定位ViewGroup
		child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
		if (mIsFoldAnimation) {
			updateFold();
		}	
	}
	
	/**
	 * 异常，用于限制BaseFoldingLayout最多只能有一个Child View
	 */
	private class NumberOfFoldingLayoutChildrenException extends
			RuntimeException {

		private static final long serialVersionUID = 1L;

		public NumberOfFoldingLayoutChildrenException(String message) {
			super(message);
		}
	}

	//抛出异常
	private void throwCustomException(int numOfChildViews) {
		if (numOfChildViews == 1) {
			throw new NumberOfFoldingLayoutChildrenException(
					FOLDING_VIEW_EXCEPTION_MESSAGE);
		}
	}

	public void setOnFoldListener(OnFoldListener foldListener) {
		mFoldListener = foldListener;
	}

	public void setFoldFactor(float foldFactor) {
		if (foldFactor != mFoldFactor) {
			mFoldFactor = foldFactor;
			calculateMatrices();
			invalidate();
		}
	}
	
	public void startFoldAnimation() {
		mValueAnimator.start();
	}

	public void setOrientation(Orientation orientation) {
		if (orientation != mOrientation) {
			mOrientation = orientation;
			updateFold();
		}
	}

	public void setAnchorFactor(float anchorFactor) {
		if (anchorFactor != mAnchorFactor) {
			mAnchorFactor = anchorFactor;
			updateFold();
		}
	}

	public void setNumberOfFolds(int numberOfFolds) {
		if (numberOfFolds != mNumberOfFolds) {
			mNumberOfFolds = numberOfFolds;
			updateFold();
		}
	}

	public float getAnchorFactor() {
		return mAnchorFactor;
	}

	public Orientation getOrientation() {
		return mOrientation;
	}

	public float getFoldFactor() {
		return mFoldFactor;
	}

	public int getNumberOfFolds() {
		return mNumberOfFolds;
	}

	private void updateFold() {
		prepareFold(mOrientation, mAnchorFactor, mNumberOfFolds);
		calculateMatrices();
		invalidate();
	}

	//用于根据orientation、anchorFactor、numberOfFolds准备新的参数
	private void prepareFold(Orientation orientation, float anchorFactor,
			int numberOfFolds) {

		mSrc = new float[NUM_OF_POLY_POINTS];
		mDst = new float[NUM_OF_POLY_POINTS];

		mDstRect = new Rect();

		mFoldFactor = 0;

		mIsFoldPrepared = false;

		mSolidShadow = new Paint();
		mGradientShadow = new Paint();

		mOrientation = orientation;
		mIsHorizontal = (orientation == Orientation.HORIZONTAL);

		//根据方向设置阴影效果
		if (mIsHorizontal) {
			mShadowLinearGradient = new LinearGradient(0, 0, SHADING_FACTOR, 0,
					Color.BLACK, Color.TRANSPARENT, TileMode.CLAMP);
		} else {
			mShadowLinearGradient = new LinearGradient(0, 0, 0, SHADING_FACTOR,
					Color.BLACK, Color.TRANSPARENT, TileMode.CLAMP);
		}

		mGradientShadow.setStyle(Style.FILL);
		mGradientShadow.setShader(mShadowLinearGradient);
		mShadowGradientMatrix = new Matrix();

		mAnchorFactor = anchorFactor;
		mNumberOfFolds = numberOfFolds;

		mOriginalWidth = getMeasuredWidth();
		mOriginalHeight = getMeasuredHeight();
		Trace.Debug("####prepareFold getMeasuredWidth() + getMeasuredHeight() is "
				+ getMeasuredWidth() + "," + getMeasuredHeight());

		//根据折叠数产生对应个数的Rect和Matrix
		mFoldRectArray = new Rect[mNumberOfFolds];
		mMatrix = new Matrix[mNumberOfFolds];

		for (int x = 0; x < mNumberOfFolds; x++) {
			mMatrix[x] = new Matrix();
		}

		int h = mOriginalHeight;
		int w = mOriginalWidth;

		//android 4.3 截取整个子View的Bitmap保存在mFullBitmap中
		if (IS_JBMR2 &&h!=0 &&w!=0) {
			if (mFullBitmap != null && mFullBitmap.isRecycled()) {
				mFullBitmap.recycle();
			}
			try {
				mFullBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
				Canvas canvas = new Canvas(mFullBitmap);
				getChildAt(0).draw(canvas);
			} catch (OutOfMemoryError e) {
				System.gc();
				mFullBitmap = BitmapUtils.bitmapFromResource(getResources(),
						R.drawable.photo_open_failed, 0, 0);
				QuickToast.showToast(getContext(), "折叠动画oom");
			}
		}

		//计算每个fold折叠的大小，若是水平折叠则是宽度除以折叠数；若是垂直折叠则高度除以折叠数
		int delta = Math.round(mIsHorizontal ? ((float) w)
				/ ((float) mNumberOfFolds) : ((float) h)
				/ ((float) mNumberOfFolds));

		//计算配置折叠Rect
		for (int x = 0; x < mNumberOfFolds; x++) {
			//水平折叠
			if (mIsHorizontal) {
				int deltap = (x + 1) * delta > w ? w - x * delta : delta;
				mFoldRectArray[x] = new Rect(x * delta, 0, x * delta + deltap,
						h);
			} else {
				//垂直折叠
				int deltap = (x + 1) * delta > h ? h - x * delta : delta;
				mFoldRectArray[x] = new Rect(0, x * delta, w, x * delta
						+ deltap);
			}
		}

		if (mIsHorizontal) {
			mFoldMaxHeight = h;
			mFoldMaxWidth = delta;
		} else {
			mFoldMaxHeight = delta;
			mFoldMaxWidth = w;
		}

		mIsFoldPrepared = true;
	}

	//计算配置每个折叠的Matrix
	private void calculateMatrices() {

		mShouldDraw = true;

		if (!mIsFoldPrepared) {
			return;
		}

		//当折叠因子为1时表明，已经折叠完成，返回，不再绘制折叠
		if (mFoldFactor == 1) {
			mShouldDraw = false;
			return;
		}

		//在计算新的Matrix之前，先重设Matrix
		for (int x = 0; x < mNumberOfFolds; x++) {
			mMatrix[x].reset();
		}

		float cTranslationFactor = 1 - mFoldFactor;

		float translatedDistance = mIsHorizontal ? mOriginalWidth
				* cTranslationFactor : mOriginalHeight * cTranslationFactor;

		//算得每个折叠得平移距离
		float translatedDistancePerFold = Math.round(translatedDistance
				/ mNumberOfFolds);

		mFoldDrawWidth = mFoldMaxWidth < translatedDistancePerFold ? translatedDistancePerFold
				: mFoldMaxWidth;
		mFoldDrawHeight = mFoldMaxHeight < translatedDistancePerFold ? translatedDistancePerFold
				: mFoldMaxHeight;

		float translatedDistanceFoldSquared = translatedDistancePerFold
				* translatedDistancePerFold;

		//由勾股定理求得折叠的深度
		float depth = mIsHorizontal ? (float) Math
				.sqrt((double) (mFoldDrawWidth * mFoldDrawWidth - translatedDistanceFoldSquared))
				: (float) Math
						.sqrt((double) (mFoldDrawHeight * mFoldDrawHeight - translatedDistanceFoldSquared));

		//当fold慢慢折叠时，显示的面积也会变小的，所以根据depth来定义一个缩放因子，以此来进行缩小
		float scaleFactor = DEPTH_CONSTANT / (DEPTH_CONSTANT + depth);

		float scaledWidth, scaledHeight, bottomScaledPoint, topScaledPoint, rightScaledPoint, leftScaledPoint;

		if (mIsHorizontal) {
			//水平折叠时是折叠的宽度平移，高度缩小
			scaledWidth = mFoldDrawWidth * cTranslationFactor;
			scaledHeight = mFoldDrawHeight * scaleFactor;
		} else {
			//垂直折叠时是折叠的高度平移，宽度缩小
			scaledWidth = mFoldDrawWidth * scaleFactor;
			scaledHeight = mFoldDrawHeight * cTranslationFactor;
		}

		//水平折叠后的坐标点
		topScaledPoint = (mFoldDrawHeight - scaledHeight) / 2.0f;
		bottomScaledPoint = topScaledPoint + scaledHeight;

		//垂直折叠后的坐标点
		leftScaledPoint = (mFoldDrawWidth - scaledWidth) / 2.0f;
		rightScaledPoint = leftScaledPoint + scaledWidth;

		//根据mAnchorFactor因子定义折叠点
		float anchorPoint = mIsHorizontal ? mAnchorFactor * mOriginalWidth
				: mAnchorFactor * mOriginalHeight;

		//沿锚点所在的折叠位置点
		float midFold = mIsHorizontal ? (anchorPoint / mFoldDrawWidth)
				: anchorPoint / mFoldDrawHeight;

		mSrc[0] = 0;
		mSrc[1] = 0;//左上角
		mSrc[2] = 0;
		mSrc[3] = mFoldDrawHeight;//左下角
		mSrc[4] = mFoldDrawWidth;
		mSrc[5] = 0;//右上角
		mSrc[6] = mFoldDrawWidth;
		mSrc[7] = mFoldDrawHeight;//右下角

		//利用上面的值，计算出每个fold的matrix（通过ployToploy来实现）
		for (int x = 0; x < mNumberOfFolds; x++) {
			//isEven为true偶数位，为false奇数位
			boolean isEven = (x % 2 == 0);

			if (mIsHorizontal) {
				//水平折叠
				mDst[0] = (anchorPoint > x * mFoldDrawWidth) ? anchorPoint
						+ (x - midFold) * scaledWidth : anchorPoint
						- (midFold - x) * scaledWidth;
				mDst[1] = isEven ? 0 : topScaledPoint;
				mDst[2] = mDst[0];
				mDst[3] = isEven ? mFoldDrawHeight : bottomScaledPoint;
				mDst[4] = (anchorPoint > (x + 1) * mFoldDrawWidth) ? anchorPoint
						+ (x + 1 - midFold) * scaledWidth
						: anchorPoint - (midFold - x - 1) * scaledWidth;
				mDst[5] = isEven ? topScaledPoint : 0;
				mDst[6] = mDst[4];
				mDst[7] = isEven ? bottomScaledPoint : mFoldDrawHeight;

			} else {
				//垂直折叠
				mDst[0] = isEven ? 0 : leftScaledPoint;
				mDst[1] = (anchorPoint > x * mFoldDrawHeight) ? anchorPoint
						+ (x - midFold) * scaledHeight : anchorPoint
						- (midFold - x) * scaledHeight;
				mDst[2] = isEven ? leftScaledPoint : 0;
				mDst[3] = (anchorPoint > (x + 1) * mFoldDrawHeight) ? anchorPoint
						+ (x + 1 - midFold) * scaledHeight
						: anchorPoint - (midFold - x - 1) * scaledHeight;
				mDst[4] = isEven ? mFoldDrawWidth : rightScaledPoint;
				mDst[5] = mDst[1];
				mDst[6] = isEven ? rightScaledPoint : mFoldDrawWidth;
				mDst[7] = mDst[3];
			}

			//四舍五入取整
			for (int y = 0; y < 8; y++) {
				mDst[y] = Math.round(mDst[y]);
			}

			if (mIsHorizontal) {
				//表示水平折叠完成
				if (mDst[4] <= mDst[0] || mDst[6] <= mDst[2]) {
					mShouldDraw = false;
					return;
				}
			} else {
				//表示垂直折叠完成
				if (mDst[3] <= mDst[1] || mDst[7] <= mDst[5]) {
					mShouldDraw = false;
					return;
				}
			}

			//polyTopoly转换
			mMatrix[x].setPolyToPoly(mSrc, 0, mDst, 0, NUM_OF_POLY_POINTS / 2);
		}
		
		
		//设置阴影效果
		int alpha = (int) (mFoldFactor * 255 * SHADING_ALPHA);

		mSolidShadow.setColor(Color.argb(alpha, 0, 0, 0));

		if (mIsHorizontal) {
			mShadowGradientMatrix.setScale(mFoldDrawWidth, 1);
			mShadowLinearGradient.setLocalMatrix(mShadowGradientMatrix);
		} else {
			mShadowGradientMatrix.setScale(1, mFoldDrawHeight);
			mShadowLinearGradient.setLocalMatrix(mShadowGradientMatrix);
		}

		mGradientShadow.setAlpha(alpha);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		
		//当没有执行prepareFold方法，并且mFoldFactor为0时只执行子类的dispatchDraw
		if (!mIsFoldPrepared || mFoldFactor == 0) {
			Trace.Debug("####dispatchDraw !mIsFoldPrepared || mFoldFactor == 0");
			super.dispatchDraw(canvas);
			return;
		}

		if (!mShouldDraw) {
			Trace.Debug("####dispatchDraw !mShouldDraw");
			return;
		}

		Rect src;

		//根据适当的转换的信息来绘制Bitmap和阴影
		for (int x = 0; x < mNumberOfFolds; x++) {

			src = mFoldRectArray[x];
			
			canvas.save();
			//设置canvas中的Matrix
			canvas.concat(mMatrix[x]);
			if (IS_JBMR2) {
				mDstRect.set(0, 0, src.width(), src.height());
				canvas.drawBitmap(mFullBitmap, src, mDstRect, null);
			} else {
				
				//通过剪切并平移画布，来进行绘制每个fold到正确的位置
				canvas.clipRect(0, 0, src.right - src.left, src.bottom
						- src.top);

				//移动画布
				if (mIsHorizontal) {
					
					canvas.translate(-src.left, 0);
				} else {
					canvas.translate(0, -src.top);
				}
				
				Trace.Debug("####dispatchDraw Draw matrix");
				super.dispatchDraw(canvas);
			

				//复位画布
				if (mIsHorizontal) {
					canvas.translate(src.left, 0);
				} else {
					canvas.translate(0, src.top);
				}
			}
			//根据每个折叠绘制阴影
			if (x % 2 == 0) {
				canvas.drawRect(0, 0, mFoldDrawWidth, mFoldDrawHeight,
						mSolidShadow);
			} else {
				canvas.drawRect(0, 0, mFoldDrawWidth, mFoldDrawHeight,
						mGradientShadow);
			}

			canvas.restore();
		}
	}
	
	public Boolean isFoldAnimationRunning() {
		return mValueAnimator.isRunning();
	}
	
	public void stopFoldAnimation() {
		mValueAnimator.end();
		mFoldFactor = 0;
	}
	
	public interface OnFoldListener {
	    public void onStartFold();
	    public void onEndFold();
	    public void onRecoverFold();
	}

}