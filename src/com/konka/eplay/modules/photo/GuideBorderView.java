package com.konka.eplay.modules.photo;


import iapp.eric.utils.base.Trace;

import com.konka.eplay.R;
import com.konka.eplay.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 
 * Created on: 2015-3-30
 * 
 * @brief 图片缩放移动指导框
 * @author mcsheng
 * @date Latest modified on: 2015-4-27
 * @version V1.0.00
 * 
 * 以移动框为做为屏幕的参照，是比较好的实现。
 */
public class GuideBorderView extends ImageView {

	public final static int MOVE_LEFT = 1;
	public final static int MOVE_RIGHT = 2;
	public final static int MOVE_UP = 3;
	public final static int MOVE_DOWN = 4;
	private int mStepDistance = 10;
	private View mReferableView = null;
	private View mGuideView = null;
	private int mLeft, mTop, mRight, mBottom;
	private int mReferLeft, mReferTop, mReferRight, mReferBottom;
	private int mGuideLeft, mGuideTop, mGuideRight, mGuideBottom;

	private Boolean mHasMove = false;
	private Boolean mHasMoveForMouse = false;
	private Boolean mFirst = true;
	private OnMoveEndListener mMoveEndListener = null;

	private View mCoordinateView;
	private int mDirection = 0;
	private ImageView mImageView = null;
	//颜色位和透明度
	private int mColorBit = 0;
	
	private int mMouseDistanceX = -1;
	private int mMouseDistanceY = -1;

	public GuideBorderView(Context context) {
		super(context);
		init();
	}

	public GuideBorderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GuideBorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setBorderStyle(R.drawable.picture_scale_focus);
	}

	// 处理一些固定的坐标值，只在View第一次获取到焦点时计算。
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (mFirst && hasWindowFocus) {
			Trace.Debug("####onWindowFocusChanged First");
			mFirst = false;

			mReferableView = (View) this.getParent();
			mImageView = (ImageView) ((FrameLayout)mReferableView).findViewById(R.id.image_guide_view);
						
			ViewLocation referLocation = findLocationWithView(mReferableView);
			mReferLeft = referLocation.x;
			mReferTop = referLocation.y;
			mReferRight = mReferLeft + mReferableView.getWidth();
			mReferBottom = mReferTop + mReferableView.getHeight();

			ViewLocation ownLocation = findLocationWithView(this);
			mLeft = ownLocation.x;
			mTop = ownLocation.y;
			mRight = ownLocation.x + this.getWidth();
			mBottom = ownLocation.y + this.getHeight();

			Point point = getActualImageMeasure();
			//这里的处理使全景缩略图中的Bitmap靠右下角显示，以使可以通过调节ImageView的边距控制其显示位置
			mImageView.setScaleType(ScaleType.MATRIX);
			Matrix matrix = new Matrix();
			matrix.set(mImageView.getImageMatrix());
			int imageRight = (mImageView.getWidth() - point.x)/2;
			int imageBottom= (mImageView.getHeight() - point.y)/2;
			matrix.postTranslate(imageRight,imageBottom);
			mImageView.setImageMatrix(matrix);
			//Bitmap的显示坐标，相对于窗体来说的
			mGuideLeft = findLocationWithView(mImageView).x + 
					(mImageView.getWidth() - point.x);
			mGuideRight = mGuideLeft + point.x;
			mGuideTop = findLocationWithView(mImageView).y + 
					(mImageView.getHeight() - point.y);
			mGuideBottom = mGuideTop + point.y;
			Trace.Debug("mGuideLeft is " + mGuideLeft);
			Trace.Debug("mReferLeft is " + mReferLeft);

			// 居中定位  
			ViewGroup.LayoutParams lp = (LayoutParams) this.getLayoutParams();		
			
			PointF pointF = ((KeyScaleImageView)mCoordinateView).getScreenScaleToBitmap();
			float borderWidth = -1;
			float borderHeight = -1;
			//当屏幕宽度大于显示的图片的宽度时，指导框以参考图片的宽度为准
			if(pointF.x > 1.0f) {
				borderWidth = point.x;
			} else {
				borderWidth = this.getActualImageMeasure().x * pointF.x;
			}		
			//当屏幕高度大于显示的图片的高度时，指导框以参考图片的高度为准
			if(pointF.y > 1.0f) {
				borderHeight = point.y;
			} else {
				borderHeight = this.getActualImageMeasure().y * pointF.y;
			}
				
			float bitmapCenterX = (mGuideLeft - mReferLeft) + point.x / 2.0f;
			float bitmapCenterY = (mGuideTop - mReferTop) + point.y / 2.0f;
			int left = (int) (bitmapCenterX - borderWidth / 2);
			int right = (int) (bitmapCenterX + borderWidth / 2);
			int top = (int) (bitmapCenterY - borderHeight / 2);
			int bottom = (int) (bitmapCenterY + borderHeight / 2);
			
			Trace.Debug("####left,top,right,bottom: " + left + "," + top + ","
					+ "," + right + "," + bottom);
			// 定位操作，通过setMargins来进行操作的
			((MarginLayoutParams) lp).setMargins(left, top, right, bottom);
			
			lp.width = (int) borderWidth;
			lp.height = (int) borderHeight;
			
			this.setLayoutParams(lp);
			
			//高亮指导框内部
			highLightInBorder(mGuideLeft + point.x / 2, mGuideTop + point.y / 2,
					(int)borderWidth, (int)borderHeight);
		}
	}

	// 重新布局回调
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if(mHasMove) {
			// 设置移动后回调
			if (mMoveEndListener != null) {
				mMoveEndListener.moveEnd(this, mDirection);
			}

			mHasMove = false;
			return;
		}
		
		if(mHasMoveForMouse) {
			// 设置移动后回调
			if (mMoveEndListener != null) {
				mMoveEndListener.moveEndForMouse(this,mMouseDistanceX,mMouseDistanceY);
			}
			mHasMoveForMouse = false;
			return;
		}		
	}

	/**
	 * 获取被指导的ImageView中的图片的实际宽与高
	 */
	public Point getActualImageMeasure() {
		// ImageView在显示图片的时候，受限于屏幕大小，和图片宽高。通常图片是被缩放过，且不是宽和高都充满ImageView的

		Drawable d = ((ImageView) mGuideView).getDrawable();
		// 获得ImageView中Image的真实宽高，
		int dw = d.getBounds().width();
		int dh = d.getBounds().height();

		// 获得ImageView中Image的变换矩阵
		Matrix m = ((ImageView) mGuideView).getImageMatrix();
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

	/**
	 * 设置移动结束后的回调监听
	 */
	public void setOnMoveEndListener(OnMoveEndListener listener) {
		mMoveEndListener = listener;
	}

	/**
	 * 设置框框的显示风格
	 */
	public void setBorderStyle(int id) {
		this.setBackgroundResource(id);
	}

	/**
	 * 设置每次移动距离
	 */
	public void setStepDistance(int distance) {
		mStepDistance = distance;
	}

	/**
	 * 获取每次移动距离
	 */
	public int getStepDistance() {
		return mStepDistance;
	}

	/**
	 * 使框框移动的方法
	 * 
	 * @param direction
	 *            所要移动的方向
	 */
	public void move(int direction) {
		ViewLocation ownLocation = findLocationWithView(this);

		switch (direction) {
		case MOVE_LEFT:
			mLeft = ownLocation.x - mReferLeft - mStepDistance;
			mTop = ownLocation.y - mReferTop;
			break;
		case MOVE_RIGHT:
			mLeft = ownLocation.x - mReferLeft + mStepDistance;
			mTop = ownLocation.y - mReferTop;
			break;
		case MOVE_UP:
			mLeft = ownLocation.x - mReferLeft;
			mTop = ownLocation.y - mReferTop - mStepDistance;
			break;
		case MOVE_DOWN:
			mLeft = ownLocation.x - mReferLeft;
			mTop = ownLocation.y - mReferTop + mStepDistance;
			break;
		default:
			return;
		}

		// 越界处理
		overDeal(direction);

		// 将move标志置为true表示开始移动
		mHasMove = true;
		mDirection = direction;
		// 相对于父界面的定位
		this.layout(mLeft, mTop, mRight, mBottom);
		
		//高亮指导框内部
		highLightInBorder(-1,-1,-1,-1);
	}

	public void setGuideView(View view) {
		mGuideView = view;

	}

	/**
	 * 获取移动框的中心点坐标
	 */
	public ViewLocation getViewCenter() {
		int[] location = new int[2];
		this.getLocationInWindow(location);
		int centerX = location[0] + (this.getWidth() / 2);
		int centerY = location[1] + (this.getHeight() / 2);
		return new ViewLocation(centerX, centerY);
	}

	/**
	 * 设置对应的坐标View
	 */
	public void setCoordinateView(View view) {
		mCoordinateView = view;
	}
	
	/**
	 * 获取相对坐标，相对于KeyScaleImageView里面的Bitmap来说的
	 */
	public ViewLocation getRelativeCoordinateForBitmap(Matrix matrix) {
		KeyScaleImageView imageView = ((KeyScaleImageView)mCoordinateView);	
		ViewLocation centerLocation = getViewCenter();				
		RectF rect = new RectF(0, 0, imageView.getImageWidth(), imageView.getImageHeight());
		
		matrix.mapRect(rect);
				
		Trace.Debug("####getRelativeCoordinateForBitmap " + rect.centerX() + "," + rect.centerY());
		
		Point pointRefer = this.getActualImageMeasure();
		float relativeScaleX = rect.width() / pointRefer.x;
		float relativeScaleY = rect.height() / pointRefer.y;
		
		PointF pointDistance = new PointF();
		pointDistance.x = (centerLocation.x - mGuideLeft) * relativeScaleX;
		pointDistance.y = (centerLocation.y - mGuideTop) * relativeScaleY;		
		
		int xImage = (int) (rect.left + pointDistance.x);
		int yImage = (int) (rect.top + pointDistance.y);
		ViewLocation location = new ViewLocation(xImage, yImage); 
		return location;
	}

	// 越界处理
	private void overDeal(int direction) {
		
		int left = mGuideLeft - mReferLeft;
		int top = mGuideTop - mReferTop;
		int right = mGuideRight - mReferLeft;
		int bottom = mGuideBottom - mReferTop;

		switch (direction) {
		case MOVE_LEFT:
			if (left >= mLeft) {
				mLeft = left;
				//修复处理,以避免移动框到达边界时，缩放图片还没有完全移动出来
				//imageView.fixTranslationForGuide(MOVE_LEFT);
			}
			mRight = mLeft + this.getWidth();
			mBottom = mTop + this.getHeight();
			break;
		case MOVE_RIGHT:
			mRight = mLeft + this.getWidth();
			if (mRight >= right) {
				mRight = right;
				mLeft = mRight - this.getWidth();
				//imageView.fixTranslationForGuide(MOVE_RIGHT);
			}
			mBottom = mTop + this.getHeight();
			break;
		case MOVE_UP:
			if (mTop <= top) {
				mTop = top;
				//imageView.fixTranslationForGuide(MOVE_UP);
			}
			mBottom = mTop + this.getHeight();
			mRight = mLeft + this.getWidth();
			break;
		case MOVE_DOWN:
			mBottom = mTop + this.getHeight();
			if (mBottom >= bottom) {
				mBottom = bottom;
				mTop = mBottom - this.getHeight();
				//imageView.fixTranslationForGuide(MOVE_DOWN);
			}
			mRight = mLeft + this.getWidth();
			break;
		default:
			return;
		}

	}

	/**
	 * 指导框缩放
	 */
	public void zoom() {
		Trace.Debug("####zoom");
		
		ViewGroup.LayoutParams lp = (LayoutParams) this.getLayoutParams();
		int left = -1;
		int top = -1;
		int right = -1;
		int bottom = -1;
		
		KeyScaleImageView imageView = (KeyScaleImageView) mCoordinateView;
		
		PointF pointF = ((KeyScaleImageView)mCoordinateView).getScreenScaleToBitmap();
		float borderWidth = this.getActualImageMeasure().x * pointF.x;
		float borderHeight = this.getActualImageMeasure().y * pointF.y;
		Point point = this.getActualImageMeasure();
		
		float bitmapCenterX = (mGuideLeft - mReferLeft) + point.x / 2.0f;
		float bitmapCenterY = (mGuideTop - mReferTop) + point.y / 2.0f;
		//处理scale为1.0f的情况
		if(imageView.getScale() == 1.0f) {	
			//当屏幕宽度大于显示的图片的宽度时，指导框以参考图片的宽度为准
			if(pointF.x > 1) {
				borderWidth = this.getActualImageMeasure().x;
			} 
			//当屏幕高度大于显示的图片的高度时，指导框以参考图片的高度为准
			if(pointF.y > 1) {
				borderHeight = this.getActualImageMeasure().y;
			} 
			
			left = (int) (bitmapCenterX - borderWidth / 2);
			right = (int) (bitmapCenterX + borderWidth / 2);
			top = (int) (bitmapCenterY - borderHeight / 2);
			bottom = (int) (bitmapCenterY + borderHeight / 2);
			//高亮指导框内部
			highLightInBorder(mGuideLeft + point.x / 2, mGuideTop + point.y / 2,
					(int)borderWidth, (int)borderHeight);
		} else if(imageView.getActualImageMeasure().x > Utils.getScreenW(getContext()) ||
				imageView.getActualImageMeasure().y > Utils.getScreenH(getContext())) {
			//当缩放的图片的大于屏幕时，框框才进行缩放变形
			ViewLocation location = this.getViewCenter();
			Trace.Debug("####zoom location " + location.x + "," + location.y);
			Trace.Debug("####zoom border width and height " + borderWidth + "," + borderHeight);	
			if(pointF.x > 1) {
				borderWidth = this.getActualImageMeasure().x;
			} 
			
			if(pointF.y > 1) {
				borderHeight = this.getActualImageMeasure().y;
			} 
			
			left = (int) (location.x - mReferLeft - borderWidth / 2);
			top = (int) (location.y - mReferTop - borderHeight / 2);
			right = (int) (left + borderWidth);
			bottom = (int) (top + borderHeight);	
			//高亮指导框内部
			highLightInBorder(location.x, location.y,(int)borderWidth, (int)borderHeight);
		} else {							

			left = (int) (bitmapCenterX - borderWidth / 2);
			right = (int) (bitmapCenterX + borderWidth / 2);
			top = (int) (bitmapCenterY - borderHeight / 2);
			bottom = (int) (bitmapCenterY + borderHeight / 2);
			//高亮指导框内部
			highLightInBorder(mGuideLeft + point.x / 2, mGuideTop + point.y / 2,
					(int)borderWidth, (int)borderHeight);
		}		
		
		// 由于通过LayoutParam来设置ImageView的高度和宽度时会改ImageView的位置也会随着改变，所以需要进行
		// 定位操作，通过setMargins来进行操作的
		((MarginLayoutParams) lp).setMargins(left, top, right, bottom);
		lp.width = (int) borderWidth;
		lp.height = (int) borderHeight;	
		
		this.setLayoutParams(lp);
	}
	
	/**
	 * 获取被指导的显示的Bitmap的相对坐标中心坐标，相对于父View的坐标
	 */
	public PointF getGuideBitmapCenter() {
		Point point = this.getActualImageMeasure();
		float bitmapCenterX = (mGuideLeft - mReferLeft) + point.x / 2.0f;
		float bitmapCenterY = (mGuideTop - mReferTop) + point.y / 2.0f;
		PointF pointF = new PointF();
		pointF.x = bitmapCenterX;
		pointF.y = bitmapCenterY;
		return pointF;
	}
	
	/**
	 * 高亮指导框内部
	 */
	public void highLightInBorder(int centerX, int centerY, int borderWidth, int borderHeight) {
		ImageView imageView = (ImageView) ((FrameLayout)this.getParent()).findViewById(R.id.scale_mask_imageview);
		Bitmap bitmap = null;
		
		//首先获取颜色的Drawable，然后后面使用颜色的Bitmap的填充蒙罩
		if (imageView.getDrawable() instanceof ColorDrawable) {
			//使用Guide Bitmap的宽度和高度来进行定义蒙罩的Bitmap
			bitmap = Bitmap.createBitmap(this.getActualImageMeasure().x, 
					this.getActualImageMeasure().y, Config.ARGB_8888);
			ColorDrawable drawable = (ColorDrawable) imageView.getDrawable();
			//8位透明度 +24色，前八位表示颜色的透明度，后24位分别表示红绿蓝
			mColorBit = drawable.getColor();
			Trace.Debug("####highLightInBorder ColorBit is " + mColorBit);
		} else {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
			bitmap = bitmapDrawable.getBitmap();
		}
		
		if(borderWidth == -1 && borderHeight == -1) {
			borderWidth = this.getWidth();
			borderHeight = this.getHeight();
		}
				
		ViewLocation location = new ViewLocation(-1, -1);
		if(centerX == -1 && centerY == -1) {
			location = this.getViewCenter();
		} else {
			location.x = centerX;
			location.y = centerY;
		}
		
		float borderLeft = location.x - borderWidth / 2.0f;
		float borderTop = location.y - borderHeight / 2.0f;
		
		float borderLeftRelative = borderLeft - mGuideLeft;
		float borderRightRelative = borderLeftRelative + borderWidth;
		float borderTopRelative = borderTop - mGuideTop;
		float borderBottomRelative = borderTopRelative + borderHeight;
				
		int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        
        int bit = 0;
        
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
            	int k = width * i + j;
                //高亮内部区域
                if((i > borderTopRelative && i < borderBottomRelative) && 
                		(j > borderLeftRelative && j < borderRightRelative)) {
                	bit = 0x00000000;//透明色
                    pixels[k] = bit;
                } else {//不是内部区域则设置特定的颜色
                    bit = mColorBit;
                    pixels[k] = bit;
                }
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bitmap);	
	}
	
	public Rect getGuideBitmapRect() {
		Rect rect = new Rect(mGuideLeft, mGuideTop, mGuideRight, mGuideBottom);
		return rect;
	}
	
	public Rect getReferRect() {
		Rect rect = new Rect(mReferLeft, mReferTop, mReferRight, mReferBottom);
		return rect;
	}
	
	public void setMoveForMouse(Boolean mark) {
		mHasMoveForMouse = true;
	}
	
	public void setMoverForMouseDistance(int dx, int dy) {
		mMouseDistanceX = dx;
		mMouseDistanceY = dy;
	}
	
	// 获取View的位置
	private ViewLocation findLocationWithView(View view) {
		int[] location = new int[2];
		// 这里坐标的定位要选择好，发现dialog不是全屏显示的时候getLocationOnScreen定位移动是会有偏差的
		// Dialog全屏显示的时候，getLocationOnScreen方法和getLocationInWindow方法获取到的坐标值是一致的
		// 若不全屏显示的时候，坐标值不一致的，所以对dialog类的窗体，最好使用getLocationInWindow方法来定位坐标
		// view.getLocationOnScreen(location);
		view.getLocationInWindow(location);
		return new ViewLocation(location[0], location[1]);
	}

	// 坐标位置类
	public class ViewLocation {
		public int x;
		public int y;

		public ViewLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	/**
	 * 移动结束回调接口
	 */
	public interface OnMoveEndListener {
		/**
		 * 回调方法
		 * 
		 * @param view
		 *            移动的View
		 */
		public void moveEnd(View view, int direction);
		
		/**
		 * 鼠标移动结束回调
		 */
		public void moveEndForMouse(View view, int dx, int dy);
	}
}