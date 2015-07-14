package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.konka.eplay.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Bitmap.Config;
import android.media.ExifInterface;

/**
 * 
 * Created on: 2015-3-17
 * 
 * @brief 获取图片信息的类(其中的Exif信息获取是针对于JPEG格式图片的)
 * @author mcsheng
 * @date Latest modified on: 2015-4-13
 * @version V1.0.00
 * @使用步骤 一般先是readExifTagInfoInEnhance()方法之后才调用其他的方法
 */
public class PictureInfo {

	public static final String TAG = "PictureInfo";

	private ExifInterface mExifInterface;

	public static final String GIF = "gif";
	public static final String JPEG = "jpeg";
	public static final String PNG = "png";
	public static final String BMP = "bmp";
	public static final String JPE = "jpe";

	private String mType = null;
	// 拍照日期
	private String mDate = null;
	// 图片宽度
	private String mWidth = null;
	// 图片高度
	private String mHeight = null;
	// 照相机制造商
	private String mMaker = null;
	// 照相机型号
	private String mModel = null;
	// 光圈值
	private String mFNumber = null;
	// 曝光时间
	private String mExposureTime = null;
	// ISO速度
	private String mIsoSpeedRate = null;
	// 曝光补偿
	private String mExposureBiasValue = null; // 原生android接口取不了
	// 焦距
	private String mFocalLength = null;
	// 最大光圈
	private String mMaxApertureValue = null; // 原生android接口取不了
	// 测光模式
	private String mMeteringMode = null; // 原生android接口取不了
	// 目标距离
	private String mTargetDistance = null; // 原生android接口取不了
	// 闪光灯模式，即是否使用闪光灯
	private String mFlash = null;

	// 对比度
	private String mContrast = null;
	// 亮度
	private String mBrightness = null;
	// 曝光程序
	private String mExposureProgram = null;
	// 饱和度
	private String mSaturation = null;
	// 清晰度
	private String mSharpness = null;
	// 白平衡
	private String mWhiteBalanceMode = null;

	private Context mContext;
	private String mUnknown = null;
	private String mFilePath = null;
	private String mSize = null;

	private BitmapFactory.Options mOptions;

	public PictureInfo(Context context, String filePath) {
		
		mContext = context;
		mUnknown = mContext.getString(R.string.unknown);
		mFilePath = filePath;
		mType = mUnknown;
		if (judgeType(mFilePath).equals(GIF)) {
			Trace.Debug(mFilePath + " is gif");
			mType = GIF;
		} else {
			// 创建一个options对象
			mOptions = new BitmapFactory.Options();

			// 将inJustDecodeBounds置为true，即decodeFile时仅获取图片的宽度、高度、图片格式信息
			mOptions.inJustDecodeBounds = true;

			// 进行decodeFile获取图片信息配置options，这时函数会返回null，而不是实际的Bitmap
			BitmapFactory.decodeFile(mFilePath, mOptions);
		}

		try {
			mExifInterface = new ExifInterface(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取图片路径（绝对路径）
	 */
	public String getFilePath() {
		int index = mFilePath.lastIndexOf("/");
		return mFilePath.substring(0, index);
	}

	/**
	 * 获取图片大小，返回的值单位为MB
	 */
	public String getSize() {
		File file = new File(mFilePath);
		if (file.length() > 1024 * 10) {
			float size = file.length() / 1024f / 1024f;
			// 四进五入，保留小数点后两位小数
			mSize = (float) (Math.round(size * 100f) / 100f) + "MB";
			return mSize;
		} else {
			float size = file.length() / 1024f;
			// 四进五入，保留小数点后两位小数
			mSize = (float) (Math.round(size * 100f) / 100f) + "KB";
			return mSize;
		}

	}

	/**
	 * 获取图片的缩略图
	 * 
	 * @param width
	 *            指定缩略的宽
	 * @param height
	 *            指定缩略的高
	 */
	public Bitmap getThumbnail(int width, int height) {
		// gif图片处理
		if (mType.equals(GIF)) {

			Bitmap bitmap = Bitmap.createBitmap(Integer.parseInt(mWidth),
							Integer.parseInt(mHeight), Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			InputStream is = null;
			File file = new File(mFilePath);
			try {
				// 需要根据gif文件的大小来设置缓存流才可以
				is = new BufferedInputStream(new FileInputStream(mFilePath),
								(int) file.length());
				// 在流reset之前，设置需要截取的大小，即截取图片大小的byte
				is.mark((int) file.length());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Movie movie = Movie.decodeStream(is);
			// 获取第一帧来作为图片
			movie.setTime(0);
			movie.draw(canvas, 0, 0);
			// 若实际图片的宽和高小于目标宽和高，则不进行压缩，按原来比例返回
			if (Integer.parseInt(getWidth()) < width
							&& Integer.parseInt(getHeight()) < height) {
				return bitmap;
			}
			// 按照固定宽与高进行缩放
			Bitmap scalebitmap = BitmapUtils.scaleWithWH(bitmap, width, height);
			// 手动回收之前的Bitmap
			if (bitmap != null && bitmap.isRecycled()) {
				bitmap.recycle();
			}
			return scalebitmap;
		} else {
			return BitmapUtils.bitmapFromFile(mFilePath, width, height);
		}
	}

	/**
	 * 获取Exif信息（利用android原生接口)
	 */
	public void readExifTagInfo() {

		mDate = mExifInterface.getAttribute(ExifInterface.TAG_DATETIME);

		// mWidth = mExifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
		//
		// mHeight =
		// mExifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
		mWidth = getWidth();
		mHeight = getHeight();

		mMaker = mExifInterface.getAttribute(ExifInterface.TAG_MAKE);

		mModel = mExifInterface.getAttribute(ExifInterface.TAG_MODEL);

		mFNumber = mExifInterface.getAttribute(ExifInterface.TAG_APERTURE);

		mExposureTime = mExifInterface
						.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);

		mIsoSpeedRate = mExifInterface.getAttribute(ExifInterface.TAG_ISO);

		mFocalLength = mExifInterface
						.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);

		mFlash = mExifInterface.getAttribute(ExifInterface.TAG_FLASH);

		mDate = doNull(mDate);
		mWidth = doNull(mWidth);
		mHeight = doNull(mHeight);
		mMaker = doNull(mMaker);
		mModel = doNull(mModel);
		mFNumber = doNull(mFNumber);
		mExposureTime = doNull(mExposureTime);
		mIsoSpeedRate = doNull(mIsoSpeedRate);
		mFocalLength = doNull(mFocalLength);
		mFlash = doNull(mFlash);

	}

	/**
	 * 基于metadata-extractor库的Exif信息获取（可以获取比原生android接口更多的信息）
	 */
	public void readExifTagInfoInEnhance() {

		File file = new File(mFilePath);
		Metadata metadata = null;
		try {
			metadata = JpegMetadataReader.readMetadata(file);
		} catch (JpegProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 当图片格式不是jpeg时metadata是为null的，所以在此处理不是jpeg的情况
		if (metadata == null) {
			mWidth = getWidth();
			mHeight = getHeight();
			mMaker = mUnknown;
			mModel = mUnknown;
			mDate = mUnknown;
			mFNumber = mUnknown;
			mExposureTime = mUnknown;
			mIsoSpeedRate = mUnknown;
			mFocalLength = mUnknown;
			mExposureBiasValue = mUnknown;
			mMaxApertureValue = mUnknown;
			mTargetDistance = mUnknown;
			mBrightness = mUnknown;
			mFlash = mUnknown;
			mMeteringMode = mUnknown;
			mContrast = mUnknown;
			mExposureProgram = mUnknown;
			mSaturation = mUnknown;
			mSharpness = mUnknown;
			mWhiteBalanceMode = mUnknown;
			return;
		}

		Directory exifSubIFDDirectory = null;
		Directory exifIFD0Directory = null;

		exifSubIFDDirectory = metadata.getDirectory(ExifSubIFDDirectory.class);
		exifIFD0Directory = metadata.getDirectory(ExifIFD0Directory.class);

		// 发生处理过程中exifIFD0Directory是有可能为空的，所以处理空的情况
		if (exifIFD0Directory != null) {
			mMaker = exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE);
			mModel = exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL);
			mMaker = doNull(mMaker);
			mModel = doNull(mModel);
		} else {
			mMaker = mUnknown;
			mModel = mUnknown;
		}

		// 发生处理过程中exifSubIFDDirectory是有可能为空的，所以处理空的情况
		if (exifSubIFDDirectory != null) {
			mDate = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

			mWidth = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);

			mHeight = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);

			mFNumber = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_FNUMBER);

			mExposureTime = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);

			mIsoSpeedRate = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);

			mFocalLength = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
			mFlash = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_FLASH);

			mExposureBiasValue = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_EXPOSURE_BIAS);

			mMaxApertureValue = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_MAX_APERTURE);

			mMeteringMode = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_METERING_MODE);

			mTargetDistance = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_SUBJECT_DISTANCE);

			mContrast = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_CONTRAST);

			mBrightness = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_BRIGHTNESS_VALUE);

			mExposureProgram = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_EXPOSURE_PROGRAM);

			mSaturation = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_SATURATION);

			mSharpness = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_SHARPNESS);

			mWhiteBalanceMode = exifSubIFDDirectory
							.getString(ExifSubIFDDirectory.TAG_WHITE_BALANCE_MODE);

			mDate = doNull(mDate);
			mWidth = doNull(mWidth);
			mHeight = doNull(mHeight);
			mFNumber = doNull(mFNumber);
			mExposureTime = doNull(mExposureTime);
			mIsoSpeedRate = doNull(mIsoSpeedRate);
			mFocalLength = doNull(mFocalLength);
			mExposureBiasValue = doNull(mExposureBiasValue);
			mMaxApertureValue = doNull(mMaxApertureValue);
			mTargetDistance = doNull(mTargetDistance);
			mBrightness = doNull(mBrightness);

			mFlash = matchFlashModeString(mFlash);
			mMeteringMode = matchMeteringMethodString(mMeteringMode);
			mContrast = matchContrastString(mContrast);
			mExposureProgram = matchExposureProgramString(mExposureProgram);
			mSaturation = matchSaturationString(mSaturation);
			mSharpness = matchSharpnessString(mSharpness);
			mWhiteBalanceMode = matchWhiteBalanceModeString(mWhiteBalanceMode);
		} else {
			mDate = mUnknown;
			mWidth = getWidth();
			mHeight = getHeight();
			mFNumber = mUnknown;
			mExposureTime = mUnknown;
			mIsoSpeedRate = mUnknown;
			mFocalLength = mUnknown;
			mExposureBiasValue = mUnknown;
			mMaxApertureValue = mUnknown;
			mTargetDistance = mUnknown;
			mBrightness = mUnknown;
			mFlash = mUnknown;
			mMeteringMode = mUnknown;
			mContrast = mUnknown;
			mExposureProgram = mUnknown;
			mSaturation = mUnknown;
			mSharpness = mUnknown;
			mWhiteBalanceMode = mUnknown;
		}

	}

	/**
	 * 基于metadata-extractor库的Exif信息获取（可以获取比原生android接口更多的信息）
	 */
	public void readExifWithTagInEnhance() {
		File file = new File(mFilePath);
		Metadata metadata = null;
		try {
			metadata = JpegMetadataReader.readMetadata(file);
		} catch (JpegProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Directory exifSubIFDDirectory = metadata
						.getDirectory(ExifSubIFDDirectory.class);
		Directory exifIFD0Directory = metadata
						.getDirectory(ExifIFD0Directory.class);

		Collection<Tag> collectionSubIFD = exifSubIFDDirectory.getTags();
		Collection<Tag> collectionIFD0 = exifIFD0Directory.getTags();

		for (Tag tag : collectionSubIFD) {
			String description = tag.getDescription();
			switch (tag.getTagType()) {
			case ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL:
				mDate = description;
				break;
			case ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH:
				mWidth = description;
				break;
			case ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT:
				mHeight = description;
				break;
			case ExifSubIFDDirectory.TAG_FNUMBER:
				mFNumber = description;
				break;
			case ExifSubIFDDirectory.TAG_EXPOSURE_TIME:
				mExposureTime = description;
				break;
			case ExifSubIFDDirectory.TAG_ISO_EQUIVALENT:
				mIsoSpeedRate = description;
				break;
			case ExifSubIFDDirectory.TAG_FOCAL_LENGTH:
				mFocalLength = description;
				break;
			case ExifSubIFDDirectory.TAG_FLASH:
				mFlash = description;
				break;
			case ExifSubIFDDirectory.TAG_EXPOSURE_BIAS:
				mExposureBiasValue = description;
				break;
			case ExifSubIFDDirectory.TAG_MAX_APERTURE:
				mMaxApertureValue = description;
				break;
			case ExifSubIFDDirectory.TAG_METERING_MODE:
				mMeteringMode = description;
				break;
			case ExifSubIFDDirectory.TAG_SUBJECT_DISTANCE:
				mTargetDistance = description;
				break;
			case ExifSubIFDDirectory.TAG_CONTRAST:
				mContrast = description;
				break;
			case ExifSubIFDDirectory.TAG_BRIGHTNESS_VALUE:
				mBrightness = description;
				break;
			case ExifSubIFDDirectory.TAG_EXPOSURE_PROGRAM:
				mExposureProgram = description;
				break;
			case ExifSubIFDDirectory.TAG_SATURATION:
				mSaturation = description;
				break;
			case ExifSubIFDDirectory.TAG_SHARPNESS:
				mSharpness = description;
				break;
			case ExifSubIFDDirectory.TAG_WHITE_BALANCE_MODE:
				mWhiteBalanceMode = description;
				break;
			}
		}

		for (Tag tag : collectionIFD0) {
			String description = tag.getDescription();
			switch (tag.getTagType()) {

			case ExifIFD0Directory.TAG_MAKE:
				mMaker = description;
				break;
			case ExifIFD0Directory.TAG_MODEL:
				mModel = description;
				break;
			}
		}

		doAllNull();
	}

	public void logTagInfo() {
		File file = new File(mFilePath);
		Metadata metadata = null;
		try {
			metadata = JpegMetadataReader.readMetadata(file);
		} catch (JpegProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Directory exifSubIFDDirectory = metadata
						.getDirectory(ExifSubIFDDirectory.class);
		Directory exifIFD0Directory = metadata
						.getDirectory(ExifIFD0Directory.class);

		Collection<Tag> collection = exifSubIFDDirectory.getTags();
		for (Tag tag : collection) {
			Trace.Info(tag.toString());
		}

		Trace.Debug("隔开");

		Collection<Tag> collection2 = exifIFD0Directory.getTags();
		for (Tag tag : collection2) {
			Trace.Info(tag.toString());
		}
	}

	/**
	 * 获取图片的日期(若拍摄日期为未知，则返回最后修改日期)
	 */
	public String getDate() {
		if (mDate.equals(mUnknown)) {
			return getModifiedDate();
		} else {
			String date = mDate.substring(0, 4) + "." + mDate.substring(5, 7)
							+ "." + mDate.substring(8, 10);
			return date;
		}
	}

	/**
	 * 获取图片的修改时间
	 */
	@SuppressLint("SimpleDateFormat")
	public String getModifiedDate() {
		File file = new File(mFilePath);
		// 获取图片最后的修改时间
		Date date = new Date(file.lastModified());
		// 将时间按自己定义的格式进行格式化
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");

		return dateFormat.format(date);
	}

	/**
	 * 获取图片的名称
	 */
	public String getName() {

		File file = new File(mFilePath);
		String name = file.getName();
		// int index = name.lastIndexOf(".");
		// return name.substring(0, index);
		return name;
	}

	/**
	 * 获取图片的宽度
	 */
	public String getWidth() {

		if (mType.equals(GIF)) {
			return mWidth;
		} else {
			return mOptions.outWidth + "";
		}

	}

	/**
	 * 获取图片的高度
	 */
	public String getHeight() {

		if (mType.equals(GIF)) {
			return mHeight;
		} else {
			return mOptions.outHeight + "";
		}

	}

	/**
	 * 获取图片的类型
	 */
	public String getType() {
		if (mType.equals(GIF)) {
			return mType;
		} else {
			// image/jpeg image/png image/bmp
			String type = mOptions.outMimeType;
			if(type == null) {
				return null;
			}
			int i = type.lastIndexOf("/");
			mType = type.substring(i + 1, type.length());
			return mType;
		}
	}

	/**
	 * 获取Exif中的生产商信息
	 */
	public String getMaker() {
		return mMaker;
	}

	/**
	 * 获取Exif中的照相机信息
	 */
	public String getModel() {
		return mModel;
	}

	/**
	 * 获取Exif中的光圈值信息
	 */
	public String getFNumber() {
		return mFNumber;
	}

	/**
	 * 获取Exif中的曝光值信息
	 */
	public String getExposureTime() {
		if (mExposureTime.equals(mUnknown)) {
			return mExposureTime;
		} else {
			return (mExposureTime + " " + mContext.getString(R.string.second));
		}
	}

	/**
	 * 获取Exif中的ISO信息
	 */
	public String getIsoSpeedRate() {
		if (mIsoSpeedRate.equals(mUnknown)) {
			return mIsoSpeedRate;
		} else {
			return "ISO-" + mIsoSpeedRate;
		}

	}

	/**
	 * 获取Exif中的曝光补偿信息
	 */
	public String getExposureBiasValue() {
		return mExposureBiasValue;
	}

	/**
	 * 获取Exif中的焦距信息
	 */
	public String getFocalLength() {
		if (mFocalLength.equals(mUnknown)) {
			return mFocalLength;
		} else {
			return (mFocalLength + " " + mContext
							.getString(R.string.millimeter));
		}
	}

	/**
	 * 获取Exif中的最大光圈值信息
	 */
	public String getMaxApertureValue() {
		return mMaxApertureValue;
	}

	/**
	 * 获取Exif中的测光模式信息
	 */
	public String getMeteringMode() {
		return mMeteringMode;
	}

	/**
	 * 获取Exif中的目标距离信息
	 */
	public String getTargetDistance() {
		return mTargetDistance;
	}

	/**
	 * 获取Exif中的闪光灯模式信息
	 */
	public String getFlash() {
		return mFlash;
	}

	/**
	 * 获取Exif中的对比度信息
	 */
	public String getContrast() {
		return mContrast;
	}

	/**
	 * 获取Exif中的亮度信息
	 */
	public String getBrightness() {
		return mBrightness;
	}

	/**
	 * 获取Exif中的曝光程序信息
	 */
	public String getExposureProgram() {
		return mExposureProgram;
	}

	/**
	 * 获取Exif中的饱和度信息
	 */
	public String getSaturation() {
		return mSaturation;
	}

	/**
	 * 获取Exif中的亮度信息
	 */
	public String getSharpness() {
		return mSharpness;
	}

	/**
	 * 获取Exif中的白平衡模式信息
	 */
	public String getWhiteBalanceMode() {
		return mWhiteBalanceMode;
	}

	/**
	 * 返回获取各项信息的字符串(基于android原生接口)
	 */
	public String toString() {
		readExifTagInfo();
		getSize();
		String logString = "####拍照日期：" + mDate + "####图片宽度：" + mWidth
						+ "####图片高度：" + mHeight + "####照相机制造商： " + mMaker
						+ "####照相机型号：" + mModel + "####光圈值：" + mFNumber
						+ "####曝光时间：" + mExposureTime + "####ISO速度："
						+ mIsoSpeedRate + "####焦距：" + mFocalLength
						+ "####闪光灯模式：" + mFlash + "####图片大小：" + mSize + "MB";
		return logString;
	}

	/**
	 * 返回获取各项信息的字符串(基于metadata-extractor库)
	 */
	public String toStringInEnhance() {
		readExifTagInfoInEnhance();
		getSize();
		String logString = "####图片类型：" + getType() + "####拍照日期：" + mDate
						+ "####图片宽度：" + mWidth + "####图片高度：" + mHeight
						+ "####照相机制造商： " + mMaker + "####照相机型号：" + mModel
						+ "####光圈值：" + mFNumber + "####曝光时间：" + mExposureTime
						+ "####ISO速度：" + mIsoSpeedRate + "####曝光补偿："
						+ mExposureBiasValue + "####焦距：" + mFocalLength
						+ "####闪光灯模式：" + mFlash + "####最大光圈："
						+ mMaxApertureValue + "####测光模式：" + mMeteringMode
						+ "####目标距离：" + mTargetDistance + "####闪光灯模式：" + mFlash
						+ "####图片大小：" + mSize + "MB" + "####对比度:" + mContrast
						+ "####亮度：" + mBrightness + "####曝光程序："
						+ mExposureProgram + "####饱和度：" + mSaturation
						+ "####清晰度：" + mSharpness + "####白平衡："
						+ mWhiteBalanceMode + "####修改时间：" + getModifiedDate()
						+ "####图片名称：" + getName();
		return logString;
	}

	/**
	 * 返回获取各项信息的字符串(基于metadata-extractor库，使用tag来获取的)
	 */
	public String toStringWithTagInEnhance() {
		readExifWithTagInEnhance();
		getSize();
		// none表示未知
		String logString = "####图片类型：" + getType() + "####拍照日期：" + mDate
						+ "####图片宽度：" + mWidth + "####图片高度：" + mHeight
						+ "####照相机制造商： " + mMaker + "####照相机型号：" + mModel
						+ "####光圈值：" + mFNumber + "####曝光时间：" + mExposureTime
						+ "####ISO速度：" + mIsoSpeedRate + "####曝光补偿："
						+ mExposureBiasValue + "####焦距：" + mFocalLength
						+ "####闪光灯模式：" + mFlash + "####最大光圈："
						+ mMaxApertureValue + "####测光模式：" + mMeteringMode
						+ "####目标距离：" + mTargetDistance + "####闪光灯模式：" + mFlash
						+ "####图片大小：" + mSize + "MB" + "####对比度:" + mContrast
						+ "####亮度：" + mBrightness + "####曝光程序："
						+ mExposureProgram + "####饱和度：" + mSaturation
						+ "####清晰度：" + mSharpness + "####白平衡："
						+ mWhiteBalanceMode;
		return logString;
	}

	// 空处理
	private String doNull(String s) {
		if (s == null) {
			return mUnknown;
		} else {
			return s;
		}
	}

	// 所有的都进行空处理
	private void doAllNull() {
		mDate = doNull(mDate);
		mWidth = doNull(mWidth);
		mHeight = doNull(mHeight);
		mMaker = doNull(mMaker);
		mModel = doNull(mModel);
		mFNumber = doNull(mFNumber);
		mExposureTime = doNull(mExposureTime);
		mIsoSpeedRate = doNull(mIsoSpeedRate);
		mFocalLength = doNull(mFocalLength);
		mFlash = doNull(mFlash);
		mExposureBiasValue = doNull(mExposureBiasValue);
		mMaxApertureValue = doNull(mMaxApertureValue);
		mMeteringMode = doNull(mMeteringMode);
		mTargetDistance = doNull(mTargetDistance);
		mContrast = doNull(mContrast);
		mBrightness = doNull(mBrightness);
		mExposureProgram = doNull(mExposureProgram);
		mSaturation = doNull(mSaturation);
		mSharpness = doNull(mSharpness);
		mWhiteBalanceMode = doNull(mWhiteBalanceMode);
	}

	// 匹配测光模式字符串
	private String matchMeteringMethodString(String str) {

		String tmp = null;

		if (str == null) {
			return mUnknown;
		}

		int i = Integer.parseInt(str);
		switch (i) {
		case ExifConstant.ExposureMeteringMethod.UNKNOWN:
			tmp = mUnknown;
			break;
		case ExifConstant.ExposureMeteringMethod.AVERAGE:
			tmp = mContext.getString(R.string.meter_average);
			break;
		case ExifConstant.ExposureMeteringMethod.CENTER_WEIGHTED_AVERAGE:
			tmp = mContext.getString(R.string.meter_center_average);
			break;
		case ExifConstant.ExposureMeteringMethod.SPOT:
			tmp = mContext.getString(R.string.meter_spot);
			break;
		case ExifConstant.ExposureMeteringMethod.MULTI_SPOT:
			tmp = mContext.getString(R.string.meter_multi_spot);
			break;
		case ExifConstant.ExposureMeteringMethod.MULTI_SEGMENT:
			tmp = mContext.getString(R.string.meter_multi_segment);
			break;
		case ExifConstant.ExposureMeteringMethod.PARTIAL:
			tmp = mContext.getString(R.string.meter_partial);
			break;
		case ExifConstant.ExposureMeteringMethod.OTHER:
			tmp = mUnknown;
			break;
		default:
			tmp = mUnknown;
			break;
		}

		return tmp;
	}

	// 匹配闪光灯模式字符串
	private String matchFlashModeString(String str) {

		String tmp = null;

		if (str == null) {
			return mUnknown;
		}

		int i = Integer.parseInt(str);
		switch (i) {
		case ExifConstant.FlashMode.NO_FLASH:
			tmp = mContext.getString(R.string.no_flash);
			break;
		case ExifConstant.FlashMode.FIRED:
			tmp = mContext.getString(R.string.fired);
			break;
		case ExifConstant.FlashMode.FIRED_RETURN_NOT_DETECTED:
			tmp = mContext.getString(R.string.fired_return_not_detected);
			break;
		case ExifConstant.FlashMode.FIRED_RETURN_DETECTED:
			tmp = mContext.getString(R.string.fired_return_detected);
			break;
		case ExifConstant.FlashMode.ON:
			tmp = mContext.getString(R.string.on);
			break;
		case ExifConstant.FlashMode.ON_RETURN_NOT_DETECTED:
			tmp = mContext.getString(R.string.on_return_not_detected);
			break;
		case ExifConstant.FlashMode.ON_RETURN_DETECTED:
			tmp = mContext.getString(R.string.on_return_detected);
			break;
		case ExifConstant.FlashMode.OFF:
			tmp = mContext.getString(R.string.off);
			break;
		case ExifConstant.FlashMode.AUTO_DID_NOT_FIRE:
			tmp = mContext.getString(R.string.auto_did_not_fire);
			break;
		case ExifConstant.FlashMode.AUTO_FIRED:
			tmp = mContext.getString(R.string.auto_fired);
			break;
		case ExifConstant.FlashMode.AUTO_FIRED_RETURN_NOT_DETECTED:
			tmp = mContext.getString(R.string.auto_fired_return_not_detected);
			break;
		case ExifConstant.FlashMode.AUTO_FIRED_RETURN_DETECTED:
			tmp = mContext.getString(R.string.auto_fired_return_detected);
			break;
		case ExifConstant.FlashMode.NO_FLASH_FUNCTION:
			tmp = mContext.getString(R.string.no_flash_function);
			break;
		case ExifConstant.FlashMode.FIRED_RED_EYE_REDUCTION:
			tmp = mContext.getString(R.string.firde_red_eye_reduction);
			break;
		case ExifConstant.FlashMode.FIRED_RED_EYE_NOT_DETECTED:
			tmp = mContext.getString(R.string.fired_red_eye_not_detected);
			break;
		case ExifConstant.FlashMode.FIRED_RED_EYE_DETECTED:
			tmp = mContext.getString(R.string.fired_red_eye_detected);
			break;
		case ExifConstant.FlashMode.ON_RED_EYE_REDUCTION:
			tmp = mContext.getString(R.string.on_red_eye_reduction);
			break;
		case ExifConstant.FlashMode.ON_RED_EYE_NOT_DETECTED:
			tmp = mContext.getString(R.string.on_red_eye_not_detected);
			break;
		case ExifConstant.FlashMode.ON_RED_EYE_DETECTED:
			tmp = mContext.getString(R.string.on_red_eye_detected);
			break;
		case ExifConstant.FlashMode.AUTO_FIRED_RED_EYE_REDUCTION:
			tmp = mContext.getString(R.string.auto_fired_red_eye_reduction);
			break;
		case ExifConstant.FlashMode.AUTO_FIRED_RED_EYE_NOT_DETECTED:
			tmp = mContext.getString(R.string.auto_fired_red_eye_not_detected);
			break;
		case ExifConstant.FlashMode.AUTO_FIRED_RED_EYE_DETECTED:
			tmp = mContext.getString(R.string.auto_fired_red_eye_detected);
			break;
		default:
			tmp = mUnknown;
			break;
		}

		return tmp;
	}

	// 匹配对比度字符串
	private String matchContrastString(String str) {

		String tmp = null;

		if (str == null) {
			return mUnknown;
		}

		int i = Integer.parseInt(str);
		switch (i) {
		case ExifConstant.Contrast.NORMAL:
			tmp = mContext.getString(R.string.normal);
			break;
		case ExifConstant.Contrast.SOFT:
			tmp = mContext.getString(R.string.soft);
			break;
		case ExifConstant.Contrast.HARD:
			tmp = mContext.getString(R.string.hard);
			break;
		default:
			tmp = mUnknown;
			break;
		}

		return tmp;
	}

	// 匹配曝光程序字符串
	private String matchExposureProgramString(String str) {

		String tmp = null;

		if (str == null) {
			return mUnknown;
		}

		int i = Integer.parseInt(str);
		switch (i) {
		case ExifConstant.ExposureProgram.MANUAL_CONTROL:
			tmp = mContext.getString(R.string.manual_control);
			break;
		case ExifConstant.ExposureProgram.PROGRAM_NORMAL:
			tmp = mContext.getString(R.string.program_normal);
			break;
		case ExifConstant.ExposureProgram.APERTURE_PRIORITY:
			tmp = mContext.getString(R.string.aperture_priority);
			break;
		case ExifConstant.ExposureProgram.SHUTTER_PRIORITY:
			tmp = mContext.getString(R.string.shutter_priority);
			break;
		case ExifConstant.ExposureProgram.PROGRAM_CREATIVE:
			tmp = mContext.getString(R.string.program_creative);
			break;
		case ExifConstant.ExposureProgram.PROGRAM_ACTION:
			tmp = mContext.getString(R.string.program_action);
			break;
		case ExifConstant.ExposureProgram.PORTRAIT_MODE:
			tmp = mContext.getString(R.string.portrait_mode);
			break;
		case ExifConstant.ExposureProgram.LANDSCAPE_MODE:
			tmp = mContext.getString(R.string.landscape_mode);
			break;
		default:
			tmp = mUnknown;
			break;
		}

		return tmp;
	}

	// 匹配饱和度字符串
	private String matchSaturationString(String str) {

		String tmp = null;

		if (str == null) {
			return mUnknown;
		}

		int i = Integer.parseInt(str);
		switch (i) {
		case ExifConstant.Saturation.NORMAL:
			tmp = mContext.getString(R.string.normal);
			break;
		case ExifConstant.Saturation.LOW_SATURATION:
			tmp = mContext.getString(R.string.low_saturation);
			break;
		case ExifConstant.Saturation.HIGH_SATURATION:
			tmp = mContext.getString(R.string.high_saturation);
			break;
		default:
			tmp = mUnknown;
			break;
		}

		return tmp;
	}

	// 匹配清晰度字符串
	private String matchSharpnessString(String str) {

		String tmp = null;

		if (str == null) {
			return mUnknown;
		}

		int i = Integer.parseInt(str);
		switch (i) {
		case ExifConstant.Sharpness.NORMAL:
			tmp = mContext.getString(R.string.normal);
			break;
		case ExifConstant.Sharpness.SOFT:
			tmp = mContext.getString(R.string.soft);
			break;
		case ExifConstant.Sharpness.HARD:
			tmp = mContext.getString(R.string.hard);
			break;
		default:
			tmp = mUnknown;
			break;
		}

		return tmp;
	}

	// 匹配白平衡模式
	private String matchWhiteBalanceModeString(String str) {
		String tmp = null;

		if (str == null) {
			return mUnknown;
		}

		int i = Integer.parseInt(str);
		switch (i) {
		case ExifConstant.WhiteBalanceMode.AUTO_WHITE_BALANCE:
			tmp = mContext.getString(R.string.auto_white_balance);
			break;
		case ExifConstant.WhiteBalanceMode.MANUAL_WHITE_BALANCE:
			tmp = mContext.getString(R.string.manual_white_balance);
			break;
		default:
			tmp = mUnknown;
			break;
		}

		return tmp;
	}

	// 判断图片类型是不是gif格式的
	@SuppressWarnings("resource")
	private String judgeType(String filePath) {

		int length = 10;
		int perSize = 2;
		InputStream is = null;
		byte[] data = new byte[length];
		byte[] width = new byte[perSize];
		byte[] height = new byte[perSize];

		try {
			is = new FileInputStream(filePath);
			is.read(data, 0, length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 6、7个byte为width，8、9个byte为height
		width[0] = data[6];
		width[1] = data[7];
		height[0] = data[8];
		height[1] = data[9];
		Trace.Debug(byteToInt(width) + "");
		Trace.Debug(byteToInt(height) + "");
		mWidth = byteToInt(width) + "";
		mHeight = byteToInt(height) + "";

		String type = mUnknown;

		// 判断是不是gif格式文件 前三个八位进行判断
		if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
			type = GIF;
			return type;
		}
		return type;
	}

	private int byteToInt(byte hex) {
		return hex & 0xFF;
	}

	private int byteToInt(byte[] hex) {
		if (hex == null || hex.length == 0) {
			return Integer.MIN_VALUE;
		}
		int result = 0;
		for (int i = 0; i < hex.length; i++) {
			result += byteToInt(hex[i]) * ((int) (Math.pow(2, 8 * i)));
		}
		return result;
	}

}