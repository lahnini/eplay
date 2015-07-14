package com.konka.eplay.modules.music.roundedview;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.widget.ImageView.ScaleType;
/**
 *
* @ClassName: RoundedDrawable
* @Description: 为RoundedImageView所用的Drawable子类，处理图像资源或者画图，用于显示在画布上
* @author xuyunyu
* @date 2014年12月5日 下午1:45:21
* @version
*
 */
public class RoundedDrawable extends Drawable {

	public static final String TAG = "RoundedDrawable";
	// 默认的边界颜色
	public static final int DEFAULT_BORDER_COLOR = Color.BLACK;
	// 相应的坐标矩形矩阵
	private final RectF mBounds = new RectF();
	private final RectF mDrawableRect = new RectF();
	private final RectF mBitmapRect = new RectF();
	private final BitmapShader mBitmapShader;
	// bitmap画笔
	private final Paint mBitmapPaint;
	// bitmap的宽度和高度
	private final int mBitmapWidth;
	private final int mBitmapHeight;
	// 边界的矩形变换矩阵
	private final RectF mBorderRect = new RectF();
	private final Paint mBorderPaint;
	// 渲染矩阵
	private final Matrix mShaderMatrix = new Matrix();
	// 角度半径
	private float mCornerRadius = 0;
	// 是否椭圆
	private boolean mOval = false;
	// 边界宽度
	private float mBorderWidth = 0;
	// 边界颜色
	private ColorStateList mBorderColor = ColorStateList
			.valueOf(DEFAULT_BORDER_COLOR);
	// 默认缩放类型为FIT_CENTER
	private ScaleType mScaleType = ScaleType.FIT_CENTER;

	public RoundedDrawable(Bitmap bitmap) {

		mBitmapWidth = bitmap.getWidth();
		mBitmapHeight = bitmap.getHeight();
		mBitmapRect.set(0, 0, mBitmapWidth, mBitmapHeight);
		// 获得位图的渲染器，CLAMP ：如果渲染器超出原始边界范围，会复制范围内边缘染色。
		mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
				Shader.TileMode.CLAMP);
		mBitmapShader.setLocalMatrix(mShaderMatrix);
		// 配置bitmap画笔
		mBitmapPaint = new Paint();
		mBitmapPaint.setStyle(Paint.Style.FILL);
		mBitmapPaint.setAntiAlias(true);
		mBitmapPaint.setShader(mBitmapShader);
		// 配置边界画笔
		mBorderPaint = new Paint();
		mBorderPaint.setStyle(Paint.Style.STROKE);
		mBorderPaint.setAntiAlias(true);
		mBorderPaint.setColor(mBorderColor.getColorForState(getState(),
				DEFAULT_BORDER_COLOR));
		mBorderPaint.setStrokeWidth(mBorderWidth);
	}

	/**
	 *
	 * @Title: fromBitmap
	 * @Description: 由bitmap生成RoundedDrawable对象
	 * @param bitmap
	 * @return RoundedDrawable
	 */
	public static RoundedDrawable fromBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			return new RoundedDrawable(bitmap);
		} else {
			return null;
		}
	}

	/**
	 *
	 * @Title: fromDrawable
	 * @Description: 将drawable转换成RoundedDrawable
	 * @param drawable
	 * @return Drawable
	 */
	public static Drawable fromDrawable(Drawable drawable) {
		if (drawable != null) {
			if (drawable instanceof RoundedDrawable) {
				// 如果已经是RoundedDrawable类型就返回
				return drawable;
			} else if (drawable instanceof LayerDrawable) {
				LayerDrawable ld = (LayerDrawable) drawable;
				int num = ld.getNumberOfLayers();
				// 循环layers来转换成RoundedDrawable
				for (int i = 0; i < num; i++) {
					Drawable d = ld.getDrawable(i);
					ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d));
				}
				return ld;
			}
			// 试图从 drawable 中获得bitmap
			Bitmap bm = drawableToBitmap(drawable);
			if (bm != null) {
				return new RoundedDrawable(bm);
			} else {
				Log.w(TAG, "Failed to create bitmap from drawable!");
			}
		}
		return drawable;
	}

	/**
	 * @Title: drawableToBitmap
	 * @Description:将drawableToBitmap转换成bitmap
	 * @param drawable
	 * @return Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		Bitmap bitmap;
		int width = Math.max(drawable.getIntrinsicWidth(), 1);
		int height = Math.max(drawable.getIntrinsicHeight(), 1);
		try {
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
		} catch (Exception e) {
			e.printStackTrace();
			bitmap = null;
		}

		return bitmap;
	}

	@Override
	public boolean isStateful() {
		return mBorderColor.isStateful();
	}

	@Override
	protected boolean onStateChange(int[] state) {
		int newColor = mBorderColor.getColorForState(state, 0);
		if (mBorderPaint.getColor() != newColor) {
			mBorderPaint.setColor(newColor);
			return true;
		} else {
			return super.onStateChange(state);
		}
	}

	/**
	 * @Title: updateShaderMatrix
	 * @Description: 更新渲染器矩阵
	 */
	private void updateShaderMatrix() {
		float scale;
		float dx;
		float dy;

		// 根据不同的缩放类型来绘制不同的边界矩阵和渲染矩阵
		switch (mScaleType) {
		case CENTER:
			mBorderRect.set(mBounds);
			mBorderRect.inset((mBorderWidth) / 2, (mBorderWidth) / 2);

			mShaderMatrix.set(null);
			mShaderMatrix
					.setTranslate(
							(int) ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f),
							(int) ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f));
			break;

		case CENTER_CROP:
			mBorderRect.set(mBounds);
			mBorderRect.inset((mBorderWidth) / 2, (mBorderWidth) / 2);

			mShaderMatrix.set(null);

			dx = 0;
			dy = 0;

			if (mBitmapWidth * mBorderRect.height() > mBorderRect.width()
					* mBitmapHeight) {
				scale = mBorderRect.height() / (float) mBitmapHeight;
				dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f;
			} else {
				scale = mBorderRect.width() / (float) mBitmapWidth;
				dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f;
			}

			mShaderMatrix.setScale(scale, scale);
			mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth,
					(int) (dy + 0.5f) + mBorderWidth);
			break;

		case CENTER_INSIDE:
			mShaderMatrix.set(null);

			if (mBitmapWidth <= mBounds.width()
					&& mBitmapHeight <= mBounds.height()) {
				scale = 1.0f;
			} else {
				scale = Math.min(mBounds.width() / (float) mBitmapWidth,
						mBounds.height() / (float) mBitmapHeight);
			}

			dx = (int) ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f);
			dy = (int) ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f);

			mShaderMatrix.setScale(scale, scale);
			mShaderMatrix.postTranslate(dx, dy);

			mBorderRect.set(mBitmapRect);
			mShaderMatrix.mapRect(mBorderRect);
			mBorderRect.inset((mBorderWidth) / 2, (mBorderWidth) / 2);
			mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect,
					Matrix.ScaleToFit.FILL);
			break;

		default:
		case FIT_CENTER:
			mBorderRect.set(mBitmapRect);
			mShaderMatrix.setRectToRect(mBitmapRect, mBounds,
					Matrix.ScaleToFit.CENTER);
			mShaderMatrix.mapRect(mBorderRect);
			mBorderRect.inset((mBorderWidth) / 2, (mBorderWidth) / 2);
			mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect,
					Matrix.ScaleToFit.FILL);
			break;

		case FIT_END:
			mBorderRect.set(mBitmapRect);
			mShaderMatrix.setRectToRect(mBitmapRect, mBounds,
					Matrix.ScaleToFit.END);
			mShaderMatrix.mapRect(mBorderRect);
			mBorderRect.inset((mBorderWidth) / 2, (mBorderWidth) / 2);
			mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect,
					Matrix.ScaleToFit.FILL);
			break;

		case FIT_START:
			mBorderRect.set(mBitmapRect);
			mShaderMatrix.setRectToRect(mBitmapRect, mBounds,
					Matrix.ScaleToFit.START);
			mShaderMatrix.mapRect(mBorderRect);
			mBorderRect.inset((mBorderWidth) / 2, (mBorderWidth) / 2);
			mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect,
					Matrix.ScaleToFit.FILL);
			break;

		case FIT_XY:
			mBorderRect.set(mBounds);
			mBorderRect.inset((mBorderWidth) / 2, (mBorderWidth) / 2);
			mShaderMatrix.set(null);
			mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect,
					Matrix.ScaleToFit.FILL);
			break;
		}

		// 为图片更新数据
		mDrawableRect.set(mBorderRect);
		mBitmapShader.setLocalMatrix(mShaderMatrix);
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		mBounds.set(bounds);
		updateShaderMatrix();
	}

	@Override
	public void draw(Canvas canvas) {

		// 判断是否使能椭圆，是则画圆形，不是则画方形
		if (mOval) {
			if (mBorderWidth > 0) {
				canvas.drawOval(mDrawableRect, mBitmapPaint);
				canvas.drawOval(mBorderRect, mBorderPaint);
			} else {
				canvas.drawOval(mDrawableRect, mBitmapPaint);
			}
		} else {
			if (mBorderWidth > 0) {
				canvas.drawRoundRect(mDrawableRect, Math.max(mCornerRadius, 0),
						Math.max(mCornerRadius, 0), mBitmapPaint);
				canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius,
						mBorderPaint);
			} else {
				canvas.drawRoundRect(mDrawableRect, mCornerRadius,
						mCornerRadius, mBitmapPaint);
			}
		}
	}

	// 获取透明度
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	/**
	 * @Title: getCornerRadius
	 * @Description: 获取边角宽度
	 * @return float
	 */
	public float getCornerRadius() {
		return mCornerRadius;
	}

	/**
	 * @Title: setCornerRadius
	 * @Description: 设置边角宽度
	 * @param radius
	 * @return RoundedDrawable
	 */
	public RoundedDrawable setCornerRadius(float radius) {
		mCornerRadius = radius;
		return this;
	}

	/**
	 * @Title: getBorderWidth
	 * @Description: 设置边界宽度
	 * @return float
	 */
	public float getBorderWidth() {
		return mBorderWidth;
	}

	/**
	 * @Title: setBorderWidth
	 * @Description: 设置边界宽度
	 * @param width
	 * @return RoundedDrawable
	 */
	public RoundedDrawable setBorderWidth(float width) {
		mBorderWidth = width;
		mBorderPaint.setStrokeWidth(mBorderWidth);
		return this;
	}

	/**
	 * @Title: getBorderColor
	 * @Description: 获取边界颜色
	 * @return int
	 */
	public int getBorderColor() {
		return mBorderColor.getDefaultColor();
	}

	/**
	 * @Title: setBorderColor
	 * @Description: 设置边界颜色
	 * @param color
	 * @return RoundedDrawable
	 */
	public RoundedDrawable setBorderColor(int color) {
		return setBorderColor(ColorStateList.valueOf(color));
	}

	/**
	 * @Title: getBorderColors
	 * @Description:设置边界颜色
	 * @return ColorStateList
	 */
	public ColorStateList getBorderColors() {
		return mBorderColor;
	}

	/**
	 * @Title: setBorderColor
	 * @Description: 设置边界颜色
	 * @param colors
	 * @return RoundedDrawable
	 */
	public RoundedDrawable setBorderColor(ColorStateList colors) {
		mBorderColor = colors != null ? colors : ColorStateList.valueOf(0);
		mBorderPaint.setColor(mBorderColor.getColorForState(getState(),
				DEFAULT_BORDER_COLOR));
		return this;
	}

	/**
	 * @Title: isOval
	 * @Description: 获取是否椭圆
	 * @return boolean
	 */
	public boolean isOval() {
		return mOval;
	}

	/**
	 * @Title: setOval
	 * @Description: 设置是否椭圆
	 * @param oval
	 * @return RoundedDrawable
	 */
	public RoundedDrawable setOval(boolean oval) {
		mOval = oval;
		return this;
	}

	/**
	 * @Title: getScaleType
	 * @Description: 获取缩放类型
	 * @return ScaleType
	 */
	public ScaleType getScaleType() {
		return mScaleType;
	}

	/**
	 * @Title: setScaleType
	 * @Description: 设置缩放类型
	 * @param scaleType
	 * @return RoundedDrawable
	 */
	public RoundedDrawable setScaleType(ScaleType scaleType) {
		if (scaleType == null) {
			scaleType = ScaleType.FIT_CENTER;
		}
		if (mScaleType != scaleType) {
			mScaleType = scaleType;
			// 更新矩阵
			updateShaderMatrix();
		}
		return this;
	}

	@Override
	public void setAlpha(int alpha) {
		mBitmapPaint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mBitmapPaint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override
	public void setDither(boolean dither) {
		mBitmapPaint.setDither(dither);
		invalidateSelf();
	}

	@Override
	public void setFilterBitmap(boolean filter) {
		mBitmapPaint.setFilterBitmap(filter);
		invalidateSelf();
	}

	@Override
	public int getIntrinsicWidth() {
		return mBitmapWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return mBitmapHeight;
	}

}
