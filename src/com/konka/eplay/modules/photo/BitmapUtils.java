package com.konka.eplay.modules.photo;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;

/**
 * 处理Bitmap的工具类<br>
 * 
 * <b>创建时间</b> 2014-10-29
 * 
 * @version 1.0
 * @author mcsheng <br>
 *         <b>2015-3-19 修改options选项：</b>options.inPreferredConfig =
 *         Config.RGB_565;
 */
public class BitmapUtils {

	/**
	 * 从资源中读取指定大小的Bitmap
	 * 
	 * @param res
	 *            应用资源
	 * @param resId
	 *            图片资源Id
	 * @param reqWidth
	 *            目标宽度
	 * @param reqHeight
	 *            目标高度
	 */
	public static Bitmap bitmapFromResource(Resources res, int resId,
					int reqWidth, int reqHeight) {

		// 根据图片资源Id转为输入流
		InputStream is = res.openRawResource(resId);
		// 从图片输入流中读取指定大小的Bitmap
		return bitmapFromStream(is, null, reqWidth, reqHeight);
	}

	/**
	 * 从文件中读取指定大小的Bitmap
	 * 
	 * @param pathName
	 *            文件路径
	 * @param reqWidth
	 *            目标宽度
	 * @param reqHeight
	 *            目标高度
	 */
	public static Bitmap bitmapFromFile(String pathName, int reqWidth,
					int reqHeight) {
		if (0 == reqWidth || 0 == reqHeight) {
			// 若目标高度或目标宽度有一个为0，则不压缩图片输出Bitmap
			return BitmapFactory.decodeFile(pathName);
		} else {
			// 创建一个options对象
			BitmapFactory.Options options = new BitmapFactory.Options();

			// 将inJustDecodeBounds置为true，即decodeFile时仅获取图片的宽度、高度、图片格式信息
			options.inJustDecodeBounds = true;
			// 配置像素质量为565，降低大小
			options.inPreferredConfig = Config.RGB_565;

			// 进行decodeFile获取图片信息配置options，这时函数会返回null，而不是实际的Bitmap
			BitmapFactory.decodeFile(pathName, options);

			// 计算压缩比例值，给options配置适合的值
			options = calculateInSampleSize(options, reqWidth, reqHeight);

			return BitmapFactory.decodeFile(pathName, options);
		}
	}

	/**
	 * 从byte数组中读取指定大小的Bitmap
	 * 
	 * @param data
	 *            图片的byte数组
	 * @param offset
	 *            图片从在数组中的起始位置
	 * @param length
	 *            图片在数组中的长度
	 * @param reqWidth
	 *            目标宽度
	 * @param reqHeight
	 *            目标高度
	 */
	public static Bitmap bitmapFromByteArray(byte[] data, int offset,
					int length, int reqWidth, int reqHeight) {
		if (0 == reqWidth || 0 == reqHeight) {
			// 若目标高度或目标宽度有一个为0，则不压缩图片输出Bitmap
			return BitmapFactory.decodeByteArray(data, offset, length);
		} else {

			// 创建一个options对象
			BitmapFactory.Options options = new BitmapFactory.Options();

			// 将inJustDecodeBounds置为true，即decodeByteArray时仅获取图片的宽度、高度、图片格式信息
			options.inJustDecodeBounds = true;
			// 配置像素质量为565，降低大小
			options.inPreferredConfig = Config.RGB_565;

			// 进行decodeByteArray获取图片信息配置options，这时函数会返回null，而不是实际的Bitmap
			BitmapFactory.decodeByteArray(data, offset, length, options);

			// 计算压缩比例值，给options配置适合的值
			options = calculateInSampleSize(options, reqWidth, reqHeight);

			return BitmapFactory.decodeByteArray(data, offset, length, options);
		}
	}

	/**
	 * 从流中读取指定大小的Bitmap
	 * 
	 * @param is
	 *            图片输入流
	 * @param outPadding
	 *            限定图片输出的矩形框，可以不限定，即null
	 * @param reqWidth
	 *            目标宽度
	 * @param reqHeight
	 *            目标高度
	 */
	public static Bitmap bitmapFromStream(InputStream is, Rect outPadding,
					int reqWidth, int reqHeight) {
		if (0 == reqWidth || 0 == reqHeight) {
			// 若目标高度或目标宽度有一个为0，则不压缩图片输出Bitmap
			return BitmapFactory.decodeStream(is);
		} else {
			// 创建一个options对象
			BitmapFactory.Options options = new BitmapFactory.Options();

			// 将inJustDecodeBounds置为true，即decodeStream时仅获取图片的宽度、高度、图片格式信息
			options.inJustDecodeBounds = true;
			// 配置像素质量为565，降低大小
			options.inPreferredConfig = Config.RGB_565;

			// 进行decodeStream获取图片信息配置options，这时函数会返回null，而不是实际的Bitmap
			BitmapFactory.decodeStream(is, outPadding, options);

			// 计算压缩比例值，给options配置适合的值
			options = calculateInSampleSize(options, reqWidth, reqHeight);

			return BitmapFactory.decodeStream(is, outPadding, options);
		}
	}

	/**
	 * 从流中读取指定大小的Bitmap(没有outPadding参数的)
	 * 
	 * @param is
	 *            图片输入流
	 * @param reqWidth
	 *            目标宽度
	 * @param reqHeight
	 *            目标高度
	 */
	public static Bitmap bitmapFromStream(InputStream is, int reqWidth,
					int reqHeight) {
		if (0 == reqWidth || 0 == reqHeight) {
			// 若目标高度或目标宽度有一个为0，则不压缩图片输出Bitmap
			return BitmapFactory.decodeStream(is);
		} else {

			// 将输入流转换为byte数组
			byte[] data = FileOperation.inputStreamToByte(is);
			// 调用从byte数组中读取指定大小Bitmap的方法
			return bitmapFromByteArray(data, 0, data.length, reqWidth,
							reqHeight);
		}
	}

	/**
	 * 根据目标宽度和目标高度来给Options赋值
	 * 
	 * @param options
	 *            包含着图片原始宽度和原始高度的Options
	 * @param reqWidth
	 *            目标宽度（相当于是阈值，不一定是这个宽度）
	 * @param reqHeight
	 *            目标高度（相当于是阈值，不一定是这个高度）
	 * 
	 * @return 返回配置好的Options，BitmapFactory可以根据这个Options来生成 适合目标宽度和目标高度的Bitmap
	 */
	public static BitmapFactory.Options calculateInSampleSize(
					BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// 获取图片的原始宽与高
		int width = options.outWidth;
		int height = options.outHeight;
		// 给压缩比例值为1，即不压缩
		int inSampleSize = 1;
		
		if (width > reqWidth || height > reqHeight) {

			// 当原始宽或高大于目标宽度或目标高度时，四舍五入取比例值
			int widthRatio = Math.round((float) width / (float) reqWidth);
			int heightRatio = Math.round((float) height / (float) reqHeight);
			// 取两者最小的比例值给inSampleSize
			inSampleSize = heightRatio > widthRatio ? widthRatio : heightRatio;

		}
		// 配置Options
		options.inSampleSize = inSampleSize;
		// 将inJustDecodeBounds置为false，不仅是获取图片信息
		options.inJustDecodeBounds = false;
		return options;
	}

	/**
	 * 根据需要特定的宽与高来缩放得到Bitmap
	 * 
	 * @param paramBitmap
	 *            需要处理的Bitmap
	 * @param paramInt1
	 *            宽
	 * @param paramInt2
	 *            高
	 */
	public static Bitmap getScaleBitmap(Bitmap paramBitmap, int paramInt1,
					int paramInt2) {
		Bitmap localBitmap = paramBitmap;
		int i = localBitmap.getWidth();
		int j = localBitmap.getHeight();
		float f1 = paramInt1 / i;
		float f2 = paramInt2 / j;
		if (f1 <= 0.0F) {
			f1 = 1.0F;
		}
		if (f2 <= 0.0F) {
			f2 = 1.0F;
		}
		Matrix localMatrix = new Matrix();
		localMatrix.postScale(f1, f2);
		return Bitmap.createBitmap(localBitmap, 0, 0, i, j, localMatrix, true);
	}

	/***
	 * 图片的缩放方法,如果参数宽高为0,则不处理<br>
	 * 
	 * <b>注意</b> src实际并没有被回收，如果你不需要，请手动置空
	 * 
	 * @param src
	 *            源图片资源
	 * @param w
	 *            缩放后宽度
	 * @param h
	 *            缩放后高度
	 */
	public static Bitmap scaleWithWH(Bitmap src, double w, double h) {
		if (w == 0 || h == 0 || src == null) {
			return src;
		} else {
			// 记录src的宽高
			int width = src.getWidth();
			int height = src.getHeight();
			// 创建一个matrix容器
			Matrix matrix = new Matrix();
			// 计算缩放比例
			float scaleWidth = (float) (w / width);
			float scaleHeight = (float) (h / height);
			// 开始缩放
			matrix.postScale(scaleWidth, scaleHeight);
			// 创建缩放后的图片
			return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
		}
	}

}