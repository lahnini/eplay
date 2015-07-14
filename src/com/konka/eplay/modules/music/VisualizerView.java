/**
 * @Title: VisualizerView.java
 * @Package com.konka.eplay.modules.music.spectrum
 * @Description: TODO(用一句话描述该文件做什么)
 * @author A18ccms A18ccms_gmail_com
 * @date 2015年5月8日 上午9:37:20
 * @version
 */
package com.konka.eplay.modules.music;

import java.util.Random;

import com.konka.eplay.R;
import com.konka.eplay.Utils;

import iapp.eric.utils.base.Trace;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * @ClassName: VisualizerView
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年5月8日 上午9:37:20
 * @version
 *
 */
public class VisualizerView extends View {

	public VisualizerView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}

	private byte[] mBytes;
	private float[] mPoints;
	private Rect mRect = new Rect();
	private Rect mRectForprogress = new Rect();
	private Rect mRectForThum = new Rect();
	private Rect mRectForbg = new Rect();

	private Paint mForePaint = new Paint();
	private float mSpectrumNum = 192.0f;
	private boolean mFirst = true;
	private Random mRandom;

	private Context mContext;

	private int mMusicDuration;
	private int mProgress;;
	Bitmap bg;
	Bitmap progress;
	Bitmap thum;
	int mTotalHeight;
	int mTotalWidth;
	boolean mIsPlaying = true;

	boolean mStopDraw = false;



	public void setmStopDraw(boolean mStopDraw) {
		this.mStopDraw = mStopDraw;
	}

	public void setmMusicDuration(int mMusicDuration) {
		this.mMusicDuration = mMusicDuration;
	}

	private void init() {
		Trace.Info("audio=====init");
		mRandom = new Random();
		mBytes = null;
		mForePaint.setStrokeWidth(4f);
		mForePaint.setAntiAlias(true);
		// mForePaint.setColor(Color.rgb(0, 128, 255));
		mForePaint.setColor(mContext.getResources().getColor(R.color.wave_color));
		progress = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.seekbat_audio_progress);
		bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.seekbar_audio_bg);
		thum = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.seekbar_progress_thum);
		mTotalWidth = Utils.dip2px(mContext, 838.7f);
		mTotalHeight = Utils.dip2px(mContext, 43f);
	}

	public void updateVisualizer(byte[] fft, int progress,boolean isPlaying) {

//		if (mStopDraw) {
//			return;
//		}
		this.mIsPlaying = isPlaying;

		if (!isPlaying) {
			if (!mFirst) {
				return;
			}else {
				mFirst = false;
			}

		}

		byte[] model = new byte[fft.length / 2 + 1];

		model[0] = (byte) Math.abs(fft[0]);
		for (int i = 2, j = 1; j < mSpectrumNum;) {
			model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
			i += 2;
			j++;
		}
		mBytes = model;
		mProgress = progress;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mBytes == null) {
			return;
		}

		if (mPoints == null || mPoints.length < mBytes.length * 4) {
			mPoints = new float[mBytes.length * 4];
		}

		mRect.set(0, 0, Utils.dip2px(mContext, 838.7f), Utils.dip2px(mContext, 37.3f));

		// // 绘制波形
		// for (int i = 0; i < mBytes.length - 1; i++) {
		// mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
		// mPoints[i * 4 + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128))
		// * (mRect.height() / 2) / 128;
		// mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
		// mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] +
		// 128)) * (mRect.height() / 2) / 128;
		// }
		// canvas.drawLines(mPoints, mForePaint);

		// 绘制频谱
		final float baseX = ((float) mRect.width()) / mSpectrumNum;
		final float height = mRect.height();

		for (int i = 0; i < mSpectrumNum; i++) {
			if (mBytes[i] < 0) {
				mBytes[i] = 127;
			}

			final float xi = baseX * i + baseX / 2;

			mPoints[i * 4] = xi;
			mPoints[i * 4 + 1] = height;

			mPoints[i * 4 + 2] = xi;
			//推波助澜，实际出来的频谱图太安静了，随机加点数值
			if (mBytes[i] < 20&&mIsPlaying) {
				mPoints[i * 4 + 3] = height - mBytes[i] - mRandom.nextInt(20) - mRandom.nextInt(20);
			} else {
				mPoints[i * 4 + 3] = height - mBytes[i];
			}

		}
		canvas.drawLines(mPoints, mForePaint);
		mRectForbg.set(0, mTotalHeight, Utils.dip2px(mContext, 838.7f), mTotalHeight + 4);

		int thumpoint = (int)(((float)mTotalWidth / (float)mMusicDuration) * (float)mProgress);

		mRectForprogress.set(0, mTotalHeight, thumpoint, mTotalHeight + 4);
		mRectForThum.set(thumpoint - Utils.dip2px(mContext, 15f), mTotalHeight - Utils.dip2px(mContext, 5f), thumpoint
				+ Utils.dip2px(mContext, 15f), mTotalHeight + 10);

		canvas.drawBitmap(bg, null, mRectForbg, null);
		canvas.drawBitmap(progress, null, mRectForprogress, null);
		canvas.drawBitmap(thum, null, mRectForThum, null);
	}

}
