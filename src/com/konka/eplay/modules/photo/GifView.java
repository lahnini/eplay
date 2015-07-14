package com.konka.eplay.modules.photo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.konka.eplay.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * 
 * Created on: 2015-3-25
 * 
 * @brief 显示Gif图片的View
 * @date Latest modified on: 2015-3-25
 * @version V1.0.00
 * 
 */
public class GifView extends ImageView {

	private static final int DEFAULT_MOVIEW_DURATION = 1000;

	private int mMovieResourceId;
	private Movie mMovie;

	private long mMovieStart;
	private int mCurrentAnimationTime = 0;
	private int mDegree = 0;

	/**
	 * 用于决定位置绘制动画帧的中心点
	 */
	private float mLeft;
	private float mTop;

	private float mScale;

	/**
	 * 缩放帧的高与宽
	 */
	private int mMeasuredMovieWidth;
	private int mMeasuredMovieHeight;

	private volatile boolean mPaused = false;
	private boolean mVisible = true;

	private RotateAnimation mRotateAnimation = null;

	public GifView(Context context) {
		this(context, null);
	}

	public GifView(Context context, AttributeSet attrs) {
		this(context, attrs, R.styleable.CustomTheme_gifMoviewViewStyle);
	}

	public GifView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setViewAttributes(context, attrs, defStyle);
	}

	@SuppressLint("NewApi")
	private void setViewAttributes(Context context, AttributeSet attrs,
					int defStyle) {

		// 当sdk版本大于3.0时，需要关闭硬件加速
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		final TypedArray array = context.obtainStyledAttributes(attrs,
						R.styleable.GifMoviewView, defStyle,
						R.style.Widget_GifMoviewView);

		mMovieResourceId = array.getResourceId(R.styleable.GifMoviewView_gif,
						-1);
		mPaused = array.getBoolean(R.styleable.GifMoviewView_paused, false);

		array.recycle();

		if (mMovieResourceId != -1) {
			mMovie = Movie.decodeStream(getResources().openRawResource(
							mMovieResourceId));
		}
	}

	public void setMovieResource(int movieResId) {
		this.mMovieResourceId = movieResId;
		mMovie = Movie.decodeStream(getResources().openRawResource(
						mMovieResourceId));
		requestLayout();
	}

	public void setMovieFile(String filePath) {
		InputStream is = null;
		File file = new File(filePath);
		try {
			// 需要根据gif文件的大小来设置缓存流才可以
			is = new BufferedInputStream(new FileInputStream(filePath),
							(int) file.length());
			// 在流reset之前，设置需要截取的大小，即截取图片大小的byte
			is.mark((int) file.length());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.gc();
		}
		mMovie = Movie.decodeStream(is);
		requestLayout();
	}

	public void setMovie(Movie movie) {
		this.mMovie = movie;
		requestLayout();
	}

	public Movie getMovie() {
		return mMovie;
	}

	public void setMovieTime(int time) {
		mCurrentAnimationTime = time;
		invalidate();
	}

	public void setPaused(boolean paused) {
		this.mPaused = paused;

		// 计算新的动画开始时间，为了能够从同一帧恢复
		if (!paused) {
			mMovieStart = android.os.SystemClock.uptimeMillis()
							- mCurrentAnimationTime;
		}

		invalidate();
	}

	public boolean isPaused() {
		return this.mPaused;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if (mMovie != null) {
			int movieWidth = mMovie.width();
			int movieHeight = mMovie.height();

			// 计算水平缩放比例
			float scaleH = 1f;
			int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);

			if (measureModeWidth != MeasureSpec.UNSPECIFIED) {
				int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
				if (movieWidth > maximumWidth) {
					scaleH = (float) movieWidth / (float) maximumWidth;
				}
			}

			// 计算垂直缩放比例
			float scaleW = 1f;
			int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);

			if (measureModeHeight != MeasureSpec.UNSPECIFIED) {
				int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
				if (movieHeight > maximumHeight) {
					scaleW = (float) movieHeight / (float) maximumHeight;
				}
			}

			// 计算整体的缩放比例
			mScale = 1f / Math.max(scaleH, scaleW);

			mMeasuredMovieWidth = (int) (movieWidth * mScale);
			mMeasuredMovieHeight = (int) (movieHeight * mScale);

			setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);

		} else {

			// 当没有Moive可设置时，设置最小的可用的大小
			setMeasuredDimension(getSuggestedMinimumWidth(),
							getSuggestedMinimumHeight());
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		// 为在中心绘制，计算出left和top
		mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
		mTop = (getHeight() - mMeasuredMovieHeight) / 2f;

		mVisible = getVisibility() == View.VISIBLE;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mMovie != null) {
			if (!mPaused) {
				updateAnimationTime();
				drawMovieFrame(canvas);
				invalidateView();
			} else {
				drawMovieFrame(canvas);
			}
		}
	}

	// 用于更新View
	@SuppressLint("NewApi")
	private void invalidateView() {
		if (mVisible) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				postInvalidateOnAnimation();
			} else {
				invalidate();
			}
		}
	}

	// 计算当前的动画时间
	private void updateAnimationTime() {
		long now = android.os.SystemClock.uptimeMillis();
		if (mMovieStart == 0) {
			mMovieStart = now;
		}

		int dur = mMovie.duration();

		if (dur == 0) {
			dur = DEFAULT_MOVIEW_DURATION;
		}

		mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
	}

	// 绘制当前的Gif帧
	private void drawMovieFrame(Canvas canvas) {

		mMovie.setTime(mCurrentAnimationTime);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(mScale, mScale);
		mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
		canvas.restore();
	}

	@SuppressLint("NewApi")
	@Override
	public void onScreenStateChanged(int screenState) {
		super.onScreenStateChanged(screenState);
		mVisible = screenState == SCREEN_STATE_ON;
		invalidateView();
	}

	@SuppressLint("NewApi")
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}

	/**
	 * 旋转操作 true顺时针旋转 false逆时针旋转
	 */
	public void rotate(Boolean mark) {

		if (mark) {
			mDegree += 90;
			mRotateAnimation = new RotateAnimation(mDegree - 90, mDegree,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
		} else {
			mDegree -= 90;
			mRotateAnimation = new RotateAnimation(mDegree + 90, mDegree,
							Animation.RELATIVE_TO_SELF, 0.5f,
							Animation.RELATIVE_TO_SELF, 0.5f);
		}

		if (mDegree == 360) {
			mDegree = 0;
		} else if (mDegree == -360) {
			mDegree = 0;
		}
		mRotateAnimation.setDuration(500);
		mRotateAnimation.setInterpolator(new LinearInterpolator());
		mRotateAnimation.setFillAfter(true);
		this.startAnimation(mRotateAnimation);
	}

	/**
	 * 恢复旋转之前的状态
	 */
	public void revertRotate() {
		if (mRotateAnimation != null) {
			mRotateAnimation.setFillAfter(false);
		}
	}
}
