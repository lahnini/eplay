package com.konka.eplay.modules.music.roundedview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.konka.eplay.R;

/**
 *
 * @ClassName: RoundedImageView
 * @Description:自定义imageview，使其可以设置为圆形，设置边界和各自的颜色
 * @author xuyunyu
 * @date 2014年11月17日 下午4:37:26
 * @version V1.0
 *
 */
public class RoundedImageView extends ImageView {
	public static final String TAG = "RoundedImageView";
	/** 默认图片半径为0 */
	public static final float DEFAULT_RADIUS = 0f;
	/** 默认边界宽度为0 */
	public static final float DEFAULT_BORDER_WIDTH = 0f;
	// imageview的缩放类型
	private static final ScaleType[] SCALE_TYPES = { ScaleType.MATRIX,
			ScaleType.FIT_XY, ScaleType.FIT_START, ScaleType.FIT_CENTER,
			ScaleType.FIT_END, ScaleType.CENTER, ScaleType.CENTER_CROP,
			ScaleType.CENTER_INSIDE };
	// 记录RoundedImageView的半径
	private float cornerRadius = DEFAULT_RADIUS;
	// 记录RoundedImageView的边界宽度
	private float borderWidth = DEFAULT_BORDER_WIDTH;
	// 记录RoundedImageView的边界颜色
	private ColorStateList borderColor = ColorStateList
			.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
	// 记录RoundedImageView是否为椭圆
	private boolean isOval = false;
	// 记录RoundedImageView是否改变背景
	private boolean mutateBackground = false;
	// 记录图像id
	private int mResource;
	private Drawable mDrawable;
	private Drawable mBackgroundDrawable;
	// 记录图像的缩放类型
	private ScaleType mScaleType;

	public RoundedImageView(Context context) {
		super(context);
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// 获取在xml定义中设置的值，并初始化各参数
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.RoundedImageView, defStyle, 0);
		int index = a
				.getInt(R.styleable.RoundedImageView_android_scaleType, -1);
		if (index >= 0) {
			setScaleType(SCALE_TYPES[index]);
		} else {
			// default scaletype to FIT_CENTER
			setScaleType(ScaleType.FIT_CENTER);
		}
		// 获取xml设置的cornerRadius和borderWidth
		cornerRadius = a.getDimensionPixelSize(
				R.styleable.RoundedImageView_corner_radius, -1);
		borderWidth = a.getDimensionPixelSize(
				R.styleable.RoundedImageView_border_width, -1);

		// 不允许为负值，如果为负值，则设置为默认值
		if (cornerRadius < 0) {
			cornerRadius = DEFAULT_RADIUS;
		}
		if (borderWidth < 0) {
			borderWidth = DEFAULT_BORDER_WIDTH;
		}
		// 获取xml设置的borderColor
		borderColor = a
				.getColorStateList(R.styleable.RoundedImageView_border_color);
		// 如果没有设置，则给一个默认颜色 黑色
		if (borderColor == null) {
			borderColor = ColorStateList
					.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
		}
		// 获取xml设置的mutateBackground和isOval
		mutateBackground = a.getBoolean(
				R.styleable.RoundedImageView_mutate_background, false);
		isOval = a.getBoolean(R.styleable.RoundedImageView_oval, false);
		// 更新
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(true);
		// 记得回收TypedArray实例，释放资源
		a.recycle();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

	/**
	 * 返回现在ImageView正在用的 scale type
	 *
	 * @attr ref android.R.styleable#ImageView_scaleType
	 * @see android.widget.ImageView.ScaleType
	 */
	@Override
	public ScaleType getScaleType() {
		return mScaleType;
	}

	/**
	 * 控制图片进行何种比例的缩放或者移动及显示图片的整体还是部分来适应imageview的大小
	 *
	 * @param scaleType
	 *            设置的缩放类型
	 * @attr ref android.R.styleable#ImageView_scaleType
	 */
	@Override
	public void setScaleType(ScaleType scaleType) {
		assert scaleType != null;
		if (mScaleType != scaleType) {
			mScaleType = scaleType;
			switch (scaleType) {
			case CENTER:
			case CENTER_CROP:
			case CENTER_INSIDE:
			case FIT_CENTER:
			case FIT_START:
			case FIT_END:
			case FIT_XY:
				super.setScaleType(ScaleType.FIT_XY);
				break;
			default:
				super.setScaleType(scaleType);
				break;
			}
			// 更新参数
			updateDrawableAttrs();
			updateBackgroundDrawableAttrs(false);
			invalidate();
		}
	}

	// 设置图片drawable
	@Override
	public void setImageDrawable(Drawable drawable) {
		mResource = 0;
		mDrawable = RoundedDrawable.fromDrawable(drawable);
		updateDrawableAttrs();
		super.setImageDrawable(mDrawable);
	}

	// 设置图片bitmap
	@Override
	public void setImageBitmap(Bitmap bm) {
		mResource = 0;
		mDrawable = RoundedDrawable.fromBitmap(bm);
		updateDrawableAttrs();
		super.setImageDrawable(mDrawable);
	}

	// 设置图片资源
	@Override
	public void setImageResource(int resId) {
		if (mResource != resId) {
			mResource = resId;
			mDrawable = resolveResource();
			updateDrawableAttrs();
			super.setImageDrawable(mDrawable);
		}
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		setImageDrawable(getDrawable());
	}

	/**
	 *
	 * @Title: resolveResource
	 * @Description:根据设置的图片resource id来创建图片Drawable
	 * @return Drawable
	 */
	private Drawable resolveResource() {
		Resources rsrc = getResources();
		if (rsrc == null) {
			return null;
		}
		Drawable d = null;
		if (mResource != 0) {
			try {
				d = rsrc.getDrawable(mResource);
			} catch (Exception e) {
				Log.w(TAG, "Unable to find resource: " + mResource, e);
				mResource = 0;
			}
		}
		return RoundedDrawable.fromDrawable(d);
	}

	@Override
	public void setBackground(Drawable background) {
		setBackgroundDrawable(background);
	}

	// 更新图片的参数
	private void updateDrawableAttrs() {
		updateAttrs(mDrawable);
	}

	// 更新背景图片的参数
	private void updateBackgroundDrawableAttrs(boolean convert) {
		if (mutateBackground) {
			if (convert) {
				mBackgroundDrawable = RoundedDrawable
						.fromDrawable(mBackgroundDrawable);
			}
			updateAttrs(mBackgroundDrawable);
		}
	}

	/**
	 *
	 * @Title: updateAttrs
	 * @Description: 更新 drawable的参数
	 * @param drawable
	 */
	private void updateAttrs(Drawable drawable) {
		if (drawable == null) {
			return;
		}
		if (drawable instanceof RoundedDrawable) {
			((RoundedDrawable) drawable).setScaleType(mScaleType)
					.setCornerRadius(cornerRadius).setBorderWidth(borderWidth)
					.setBorderColor(borderColor).setOval(isOval);
		} else if (drawable instanceof LayerDrawable) {
			// loop through layers to and set drawable attrs
			LayerDrawable ld = ((LayerDrawable) drawable);
			for (int i = 0, layers = ld.getNumberOfLayers(); i < layers; i++) {
				updateAttrs(ld.getDrawable(i));
			}
		}
	}

	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		mBackgroundDrawable = background;
		updateBackgroundDrawableAttrs(true);
		super.setBackgroundDrawable(mBackgroundDrawable);
	}

	/**
	 *
	 * @Title: getCornerRadius
	 * @Description: 获取角度半径数值
	 * @return float
	 */
	public float getCornerRadius() {
		return cornerRadius;
	}

	/**
	 *
	 * @Title: setCornerRadius
	 * @Description: 设置角度半径
	 * @param resId
	 *            半径数值的索引id
	 */
	public void setCornerRadius(int resId) {
		setCornerRadius(getResources().getDimension(resId));
	}

	/**
	 *
	 * @Title: setCornerRadius
	 * @Description: 设置角度半径
	 * @param radius
	 */
	public void setCornerRadius(float radius) {
		if (cornerRadius == radius) {
			return;
		}
		cornerRadius = radius;
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
	}

	/**
	 *
	 * @Title: getBorderWidth
	 * @Description:获取边界宽度
	 * @return float
	 */
	public float getBorderWidth() {
		return borderWidth;
	}

	/**
	 *
	 * @Title: setBorderWidth
	 * @Description: 设置边界宽度
	 * @param resId
	 *            宽度的索引id
	 */
	public void setBorderWidth(int resId) {
		setBorderWidth(getResources().getDimension(resId));
	}

	/**
	 *
	 * @Title: setBorderWidth
	 * @Description: 设置边界宽度
	 * @param width
	 */
	public void setBorderWidth(float width) {
		if (borderWidth == width) {
			return;
		}

		borderWidth = width;
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
		invalidate();
	}

	/**
	 *
	 * @Title: getBorderColor
	 * @Description: 获取边界颜色
	 * @return int 颜色值
	 */
	public int getBorderColor() {
		return borderColor.getDefaultColor();
	}

	/**
	 *
	 * @Title: setBorderColor
	 * @Description: 设置边界颜色
	 * @param color
	 *            int类型
	 */
	public void setBorderColor(int color) {
		setBorderColor(ColorStateList.valueOf(color));
	}

	/**
	 *
	 * @Title: getBorderColors
	 * @Description: 获取边界颜色
	 * @return ColorStateList
	 */
	public ColorStateList getBorderColors() {
		return borderColor;
	}

	/**
	 *
	 * @Title: setBorderColor
	 * @Description: 设置边界颜色
	 * @param colors
	 *            ColorStateList类型颜色
	 */
	public void setBorderColor(ColorStateList colors) {
		if (borderColor.equals(colors)) {
			return;
		}

		borderColor = (colors != null) ? colors : ColorStateList
				.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
		if (borderWidth > 0) {
			invalidate();
		}
	}

	/**
	 *
	 * @Title: isOval
	 * @Description: 获取当前设置中imageview是否为椭圆
	 * @return boolean true或者false
	 */
	public boolean isOval() {
		return isOval;
	}

	/**
	 *
	 * @Title: setOval
	 * @Description: 设置是否为椭圆
	 * @param oval
	 *            true或者false
	 */
	public void setOval(boolean oval) {
		isOval = oval;
		updateDrawableAttrs();
		updateBackgroundDrawableAttrs(false);
		invalidate();
	}

	/**
	 *
	 * @Title: isMutateBackground
	 * @Description: 获取当前设置的背景是否可变
	 * @return boolean
	 */
	public boolean isMutateBackground() {
		return mutateBackground;
	}

	/**
	 *
	 * @Title: setMutateBackground
	 * @Description:设置背景是否可变
	 * @param mutate
	 *            true或者false
	 */
	public void setMutateBackground(boolean mutate) {
		if (mutateBackground == mutate) {
			return;
		}

		mutateBackground = mutate;
		updateBackgroundDrawableAttrs(true);
		invalidate();
	}
}
