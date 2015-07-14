package com.konka.eplay.modules.photo;

import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.photo.GuideBorderView.ViewLocation;

import iapp.eric.utils.base.Trace;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class KeyScaleImageView extends ImageView {

	public static final int TRAN_LEFT = 1;
	public static final int TRAN_RIGNT = 2;
	public static final int TRAN_UP = 3;
	public static final int TRAN_DOWN = 4;
	// 每次的平移量
	private int mTranDistance = 20;
	private float mImageW; // 图片宽度
	private float mImageH; // 图片高度

	private Matrix mMatrix = new Matrix();
	private int mDegree = 0;
	private float mScale = 1f;

	// 保存当前的旋转的Bitmap,用于缩放使用
	public static Bitmap sRotateBitmap = null;

	public static Boolean sHasRotate = false;
	
	private GuideBorderView mGuideBorderView = null;
	
	public static final int LIMIT_LENGTH = 1920 * 2;

	public KeyScaleImageView(Context context) {
		super(context);
		init();
	}

	public KeyScaleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public KeyScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
//		mScreenWidth = Utils.getScreenW(getContext());
//		mScreenHeight = Utils.getScreenH(getContext());
	}

	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		setImageWidthHeight();
	}

	// 在xml文件中使用ImageView时调用的是setImageDrawable方法来加载图片
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		setImageWidthHeight();
	}

	public void setImageResource(int resId) {
		super.setImageResource(resId);
		setImageWidthHeight();
	}

	/**
	 * 旋转操作方法
	 * 
	 * @param mark
	 *            true 顺时针旋转 false 逆时针旋转
	 */
	public void rotate(Boolean mark, Bitmap bitmap) {
		sHasRotate = true;
		setScaleType(ImageView.ScaleType.FIT_CENTER);
		// true 顺时针旋转； false 逆时针旋转
		if (mark) {
			// 顺时针旋转 每次旋转90度
			mDegree = mDegree + 90;
			// 到达一圈时复位
			if (mDegree >= 360) {
				mDegree = 0;
			}
		} else {
			// 逆时针旋转 每次旋转90度
			mDegree = mDegree - 90;
			// 到达一圈时复位
			if (-360 >= mDegree) {
				mDegree = 0;
			}
		}
		// 设置翻转的角度
		mMatrix.setRotate(mDegree, mImageW / 2, mImageH / 2);
		Bitmap roBitmap = null;
		try {
			roBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
							bitmap.getHeight(), mMatrix, true);
		} catch(OutOfMemoryError e) {
			System.gc();
			QuickToast.showToast(getContext(), "旋转oom，显示默认图");
			roBitmap = BitmapUtils.bitmapFromResource(getResources(), R.drawable.photo_open_failed, 
					0, 0);		
		}
		this.setImageBitmap(roBitmap);

		if(sRotateBitmap != null && sRotateBitmap.isRecycled()) {
			sRotateBitmap.recycle();
		}
		// 记录保存
		sRotateBitmap = roBitmap;
		
		if (bitmap.isRecycled()) {
			bitmap.recycle();
		}
		// 内存回收，还是有用的，去掉这个容易oom
		System.gc();
	}

	/**
	 * 缩放操作
	 */
	public void zoom() {
		Trace.Debug("####zoom");
		// 设置scale类型为matrix才可以对ImageView进行matrix缩放
		setScaleType(ImageView.ScaleType.MATRIX);
		 		
		mScale *= 2.0;
		if (mScale > 4.0f) {
			mScale = 1.0f;
			//一倍后就居中显示，相当于恢复原来的状态
			mMatrix.setScale(mScale, mScale);
			center();
			return;
		}
		//set是会重设的，注意其与post的区别
		mMatrix.setScale(mScale, mScale);
		//居中显示
		zoomAndToCenter();
		
		setImageMatrix(mMatrix);
	}
	
	/**
	 * 相反缩放，主要是为鼠标滚轮操作
	 */
	public void reverseZoom() {
		Trace.Debug("####reverseZoom");
		// 设置scale类型为matrix才可以对ImageView进行matrix缩放
		setScaleType(ImageView.ScaleType.MATRIX);
		 		
		mScale /= 2.0;
		if (mScale < 1.0f) {
			mScale = 4.0f;
			//一倍后就居中显示，相当于恢复原来的状态
			mMatrix.setScale(mScale, mScale);
			center();
			return;
		}
		//set是会重设的，注意其与post的区别
		mMatrix.setScale(mScale, mScale);
		//居中显示
		zoomAndToCenter();
		
		setImageMatrix(mMatrix);
	}
	
	/**
	 * 将放大后的图片进行居中处理
	 */
	private void zoomAndToCenter() {
		
		int[] location = new int[2];
		this.getLocationInWindow(location);
		
		int viewCenterX = location[0] + this.getWidth() / 2;
		int viewCenterY = location[1] + this.getHeight() / 2;
		RectF rect = new RectF(0, 0, mImageW, mImageH);
		mMatrix.mapRect(rect);
		float bitmapCenterX = rect.centerX();
		float bitmapCenterY = rect.centerY();
		//先居中处理
		mMatrix.postTranslate(viewCenterX - bitmapCenterX, 
					 viewCenterY - bitmapCenterY);	
		
		//当KeyScaleImageView显示的Bitmap大于屏幕时才进行按放大点后的平移居中操作
		if(rect.width() > Utils.getScreenW(getContext()) ||
				rect.height() > Utils.getScreenH(getContext())) {
			ViewLocation locationTrans = mGuideBorderView.
					getRelativeCoordinateForBitmap(mMatrix);
			//再次居中处理，将指定放大的位置移动View的中间显示
			mMatrix.postTranslate(viewCenterX - locationTrans.x, 
					 viewCenterY - locationTrans.y);
			Trace.Debug("####center viewCenter and locationTrans " + viewCenterX +
					"," + viewCenterY + "##" + locationTrans.x + "," + locationTrans.y);
		}
	}
	
	public void center() {
		int[] location = new int[2];
		this.getLocationInWindow(location);
		
		int viewCenterX = location[0] + this.getWidth() / 2;
		int viewCenterY = location[1] + this.getHeight() / 2;
		RectF rect = new RectF(0, 0, mImageW, mImageH);
		mMatrix.mapRect(rect);
		float bitmapCenterX = rect.centerX();
		float bitmapCenterY = rect.centerY();
		//先居中处理
		mMatrix.postTranslate(viewCenterX - bitmapCenterX, 
					 viewCenterY - bitmapCenterY);	
		
		this.setImageMatrix(mMatrix);
	}
	
	/**
	 * 获取屏幕相对于Bitmap的比例
	 */
	public PointF getScreenScaleToBitmap() {
		PointF point = new PointF();
		//注意进行float换算，以求精度准确
		point.x = Utils.getScreenW(getContext()) / (float)this.getActualImageMeasure().x;
		point.y = Utils.getScreenH(getContext()) / (float)this.getActualImageMeasure().y;
		Trace.Debug("####getScreenScaleToBitmap " + point.x + "," + point.y);
		return point;
	}

	/**
	 * 平移（先暂定这样，以后再决定修改）
	 */
	public void translate(int direction) {
		Trace.Debug("####translate");
		setScaleType(ImageView.ScaleType.MATRIX);
		
		switch (direction) {
		case TRAN_LEFT:
			Trace.Debug("####translate left");
			mMatrix.postTranslate(mTranDistance, 0);
			fixTranslation();
			break;
		case TRAN_RIGNT:
			Trace.Debug("####translate right");
			mMatrix.postTranslate(-mTranDistance, 0);
			fixTranslation();
			break;
		case TRAN_UP:
			Trace.Debug("####translate up");
			mMatrix.postTranslate(0, mTranDistance);
			fixTranslation();
			break;
		case TRAN_DOWN:
			Trace.Debug("####translate down");
			mMatrix.postTranslate(0, -mTranDistance);
			fixTranslation();
			break;
		default:
			break;
		}

		setImageMatrix(mMatrix);
	}
	
	public void translateForMouse(int dx , int dy) {
		Trace.Debug("####translateForMouse");
		setScaleType(ImageView.ScaleType.MATRIX);
		mMatrix.postTranslate(dx, dy);
		fixTranslation();
		setImageMatrix(mMatrix);
	}

	public void setTranslateDistance(int distance) {
		mTranDistance = distance;
	}

	public float getScale() {
		return mScale;
	}

	/**
	 * 获取ImageView中的图片的实际宽与高
	 */
	public Point getActualImageMeasure() {
		// ImageView在显示图片的时候，受限于屏幕大小，和图片宽高。通常图片是被缩放过，且不是宽和高都充满ImageView的

		Drawable d = this.getDrawable();
		// 获得ImageView中Image的真实宽高，
		int dw = d.getBounds().width();
		int dh = d.getBounds().height();

		// 获得ImageView中Image的变换矩阵
		Matrix m = this.getImageMatrix();
		float[] values = new float[10];
		m.getValues(values);

		// Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
		float sx = values[0];
		float sy = values[4];

		// 计算Image在屏幕上实际绘制的宽高
		int cw = (int) (dw * sx);
		int ch = (int) (dh * sy);

		Point point = new Point();
		point.x = cw;
		point.y = ch;

		return point;
	}
	

	public int getImageHeight() {
		return (int) mImageH;
	}

	public int getImageWidth() {
		return (int) mImageW;
	}

	/**
	 * 获取当前显示的Bitmap
	 */
	public Bitmap getBitmap() {
		Drawable d = getDrawable();
		if (d == null) {
			return null;
		}
		Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
		return bitmap;
	}
	
	public void setGuideBorderView(GuideBorderView view) {
		mGuideBorderView = view;
	}

	/**
	 * 越界处理，使被放大后的图片移动时不会超出边界
	 */
	private void fixTranslation() {

		RectF rect = new RectF(0, 0, mImageW, mImageH);
		mMatrix.mapRect(rect);

		float viewW = getWidth();
		float viewH = getHeight();

		float height = rect.height();
		float width = rect.width();

		// deltaX和deltaY分别为越界后需要返回的距离，所以当不越界时deltaX和deltaY都会为0的
		float deltaX = 0, deltaY = 0;

		if (width < viewW) {
			deltaX = (viewW - width) / 2 - rect.left;
		} else if (rect.left > 0) {
			deltaX = -rect.left;
		} else if (rect.right < viewW) {
			deltaX = viewW - rect.right;
		}

		if (height < viewH) {
			deltaY = (viewH - height) / 2 - rect.top;
		} else if (rect.top > 0) {
			deltaY = -rect.top;
		} else if (rect.bottom < viewH) {
			deltaY = viewH - rect.bottom;
		}

		// 暂注释掉 打印提示越界信息
//		 if (deltaX > 0) {
//			QuickToast.showToast(getContext(), 
//					getContext().getString(R.string.come_to_picture_right));
//		 } else if (deltaX < 0) {
//			QuickToast.showToast(getContext(), 
//					getContext().getString(R.string.come_to_picture_left));
//		 }
//		
//		 if (deltaY > 0) {
//			QuickToast.showToast(getContext(), 
//					getContext().getString(R.string.come_to_picture_bottom));
//		 } else if (deltaY < 0) {
//			QuickToast.showToast(getContext(), 
//					getContext().getString(R.string.come_to_picture_top));
//		 }

		Trace.Debug("####fixTranslation deltaX is " + deltaX);
		Trace.Debug("####fixTranslation deltaY is " + deltaY);
		mMatrix.postTranslate(deltaX, deltaY);
	}
	
	/**
	 * 用于修复当移动框到达边界时，图片还没被完全移动出来的情况
	 */
	public void fixTranslationForGuide(int direction) {

		//通过rect可以获取到实际的Bitmap的坐标
		RectF rect = new RectF(0, 0, mImageW, mImageH);
		mMatrix.mapRect(rect);

		float deltaX = 0, deltaY = 0;
		
		switch(direction) {
		case GuideBorderView.MOVE_LEFT:
			if(rect.left < 0) {
				deltaX = Math.abs(rect.left);
			}
			break;
		case GuideBorderView.MOVE_RIGHT:
			if(rect.right > Utils.getScreenW(getContext())) {
				deltaX = -(rect.right - Utils.getScreenW(getContext()));
			}
			break;
		case GuideBorderView.MOVE_UP:
			if(rect.top < 0) {
				deltaY = Math.abs(rect.top);
			}
			break;
		case GuideBorderView.MOVE_DOWN:
			if(rect.bottom > Utils.getScreenH(getContext())) {
				deltaY = -(rect.bottom - Utils.getScreenH(getContext()));
			}
			break;
		default:
			break;
		}
		
		Trace.Debug("####fixTranslationForGuide deltaX is " + deltaX);
		Trace.Debug("####fixTranslationForGuide deltaY is " + deltaY);
		mMatrix.postTranslate(deltaX, deltaY);
	}

	// 获取图片的宽度和高度（是图片的而不是ImageView的，这两者是有区别的，因为图片是会根据ScaleType类型适配ImageView的
	// 所以两者是不一样的）
	private void setImageWidthHeight() {

		Drawable d = getDrawable();
		if (d == null) {
			return;
		}

		mImageW = d.getIntrinsicWidth();

		mImageH = d.getIntrinsicHeight();

		Trace.Debug("####setImageWidthHeight mImageW is " + mImageW);
		Trace.Debug("####setImageWidthHeight mImageH is " + mImageH);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		//实现居中定位操作，因为matrix默认是不居中显示的，是在屏幕的左上角显示的
		if(this.getScaleType() == ScaleType.MATRIX && hasWindowFocus) {
			Trace.Debug("####onWindowFocusChanged is Matrix and hasWindowFocus");
			//给mMatrix设置ImageView原有的Matrix值，是不可以直接对this.getMatrix进行修改的
			mMatrix.set(this.getMatrix());
			center();
		}
	}
}