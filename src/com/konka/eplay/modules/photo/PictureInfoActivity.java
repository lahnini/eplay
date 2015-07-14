package com.konka.eplay.modules.photo;

import java.io.File;
import java.util.List;

import iapp.eric.utils.base.Trace;

import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.AlwaysMarqueeTextView;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.photo.music.Blur;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 
 * Created on: 2015-4-10
 * 
 * @brief 图片详情页Activity
 * @author mcsheng
 * @date Latest modified on: 2015-4-10
 * @version V1.0.00
 */
public class PictureInfoActivity extends Activity {

	public static final String TAG = "PictureInfoActivity";

	private AlwaysMarqueeTextView mContentSizeView;
	private AlwaysMarqueeTextView mContentDateView;
	private AlwaysMarqueeTextView mContentDimenView;
	private AlwaysMarqueeTextView mContentPathView;

	private TextView mContentManufacturersView;
	private TextView mContentCameraModelView;
	private TextView mContentFNumberview;
	private TextView mContentExposureTimeView;
	private TextView mContentIsoView;
	private TextView mContentExposureCompensationView;
	private TextView mContentFocalLengthView;
	private TextView mContentMaximumApertureView;
	private TextView mContentMeteringModeView;
	private TextView mContentObjectDistanceView;
	private TextView mContentFlashModeView;

	private TextView mContentContrastView;
	private TextView mContentBrightnessView;
	private TextView mContentExposureProgramView;
	private TextView mContentSaturabilityView;
	private TextView mContentDefinitionView;
	private TextView mContentWhiteBalanceView;

	private AlwaysMarqueeTextView mNameView;
	private ImageView mThumbnailView;
	private LinearLayout mPictureInfoLinearLayout;

	private TextView mOpenView;
	private TextView mDeleteView;

	private PictureInfo mPictureInfo;
	private ImageView mPictureInfoImageViewBack;
	private LinearLayout mHeadBackLinearLayout;
	private int mColorIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}

	@SuppressLint("CutPasteId")
	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_pictureinfo);

		// 大小 尺寸 日期 路径
		LinearLayout pictureInfoSizeLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_size);
		LinearLayout pictureInfoDimenLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_dimen);
		LinearLayout pictureInfoDateLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_date);
		LinearLayout pictureInfoPathLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_path);

		TextView titleSizeView = (TextView) pictureInfoSizeLayout
						.findViewById(R.id.picture_info_title);
		TextView titleDimenView = (TextView) pictureInfoDimenLayout
						.findViewById(R.id.picture_info_title);
		TextView titleDateView = (TextView) pictureInfoDateLayout
						.findViewById(R.id.picture_info_title);
		TextView titlePathView = (TextView) pictureInfoPathLayout
						.findViewById(R.id.picture_info_title);

		mContentSizeView = (AlwaysMarqueeTextView) pictureInfoSizeLayout
						.findViewById(R.id.picture_info_content);
		mContentDimenView = (AlwaysMarqueeTextView) pictureInfoDimenLayout
						.findViewById(R.id.picture_info_content);
		mContentDateView = (AlwaysMarqueeTextView) pictureInfoDateLayout
						.findViewById(R.id.picture_info_content);
		mContentPathView = (AlwaysMarqueeTextView) pictureInfoPathLayout
						.findViewById(R.id.picture_info_content);

		titleSizeView.setText(getString(R.string.picture_info_size));
		titleDimenView.setText(getString(R.string.picture_info_dimen));
		titleDateView.setText(getString(R.string.picture_info_date));
		titlePathView.setText(getString(R.string.picture_info_path));

		// 照相机属性
		LinearLayout pictureInfoManufacturersLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_camera_manufacturers);
		LinearLayout pictureInfoCameraModelLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_camera_model);
		LinearLayout pictureInfoFNumberLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_f_number);
		LinearLayout pictureInfoExposureTimeLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_exposure_time);
		LinearLayout pictureInfoIsoLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_iso);
		LinearLayout pictureInfoExposureCompensationLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_exposure_compensation);
		LinearLayout pictureInfoFocalLengthLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_focal_length);
		LinearLayout pictureInfoMaximumApertureLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_maximum_aperture);
		LinearLayout pictureInfoMeteringModeLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_metering_mode);
		LinearLayout pictureInfoObjectDistanceLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_object_distance);
		LinearLayout pictureInfoFlashModeLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_flash_mode);

		TextView titleCameraManufacturersView = (TextView) pictureInfoManufacturersLayout
						.findViewById(R.id.picture_info_title);
		TextView titleCameraModelView = (TextView) pictureInfoCameraModelLayout
						.findViewById(R.id.picture_info_title);
		TextView titleFNumberView = (TextView) pictureInfoFNumberLayout
						.findViewById(R.id.picture_info_title);
		TextView titleExposureTimeView = (TextView) pictureInfoExposureTimeLayout
						.findViewById(R.id.picture_info_title);
		TextView titleIsoView = (TextView) pictureInfoIsoLayout
						.findViewById(R.id.picture_info_title);
		TextView titleExposureCompensationView = (TextView) pictureInfoExposureCompensationLayout
						.findViewById(R.id.picture_info_title);
		TextView titleFocalLengthView = (TextView) pictureInfoFocalLengthLayout
						.findViewById(R.id.picture_info_title);
		TextView titleMaximumApertureView = (TextView) pictureInfoMaximumApertureLayout
						.findViewById(R.id.picture_info_title);
		TextView titleMeteringModeView = (TextView) pictureInfoMeteringModeLayout
						.findViewById(R.id.picture_info_title);
		TextView titleObjectDistanceView = (TextView) pictureInfoObjectDistanceLayout
						.findViewById(R.id.picture_info_title);
		TextView titleFlashModeView = (TextView) pictureInfoFlashModeLayout
						.findViewById(R.id.picture_info_title);

		mContentManufacturersView = (TextView) pictureInfoManufacturersLayout
						.findViewById(R.id.picture_info_content);
		mContentCameraModelView = (TextView) pictureInfoCameraModelLayout
						.findViewById(R.id.picture_info_content);
		;
		mContentFNumberview = (TextView) pictureInfoFNumberLayout
						.findViewById(R.id.picture_info_content);
		;
		mContentExposureTimeView = (TextView) pictureInfoExposureTimeLayout
						.findViewById(R.id.picture_info_content);
		;
		mContentIsoView = (TextView) pictureInfoIsoLayout
						.findViewById(R.id.picture_info_content);
		;
		mContentExposureCompensationView = (TextView) pictureInfoExposureCompensationLayout
						.findViewById(R.id.picture_info_content);
		;
		mContentFocalLengthView = (TextView) pictureInfoFocalLengthLayout
						.findViewById(R.id.picture_info_content);
		;
		mContentMaximumApertureView = (TextView) pictureInfoMaximumApertureLayout
						.findViewById(R.id.picture_info_content);
		mContentMeteringModeView = (TextView) pictureInfoMeteringModeLayout
						.findViewById(R.id.picture_info_content);
		mContentObjectDistanceView = (TextView) pictureInfoObjectDistanceLayout
						.findViewById(R.id.picture_info_content);
		mContentFlashModeView = (TextView) pictureInfoFlashModeLayout
						.findViewById(R.id.picture_info_content);

		titleCameraManufacturersView
						.setText(getString(R.string.picture_info_camera_manufacturers));
		titleCameraModelView
						.setText(getString(R.string.picture_info_camera_model));
		titleFNumberView.setText(getString(R.string.picture_info_f_number));
		titleExposureTimeView
						.setText(getString(R.string.picture_info_exposure_time));
		titleIsoView.setText(getString(R.string.picture_info_iso));
		titleExposureCompensationView
						.setText(getString(R.string.picture_info_exposure_compensation));
		titleFocalLengthView
						.setText(getString(R.string.picture_info_focal_length));
		titleMaximumApertureView
						.setText(getString(R.string.picture_info_maximum_aperture));
		titleMeteringModeView
						.setText(getString(R.string.picture_info_metering_mode));
		titleObjectDistanceView
						.setText(getString(R.string.picture_info_object_distance));
		titleFlashModeView.setText(getString(R.string.picture_info_flash_mode));

		// 高级照片属性
		LinearLayout pictureInfoContrastLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_contrast);
		LinearLayout pictureInfoBrightnessLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_brightness);
		LinearLayout pictureInfoExposureProgramLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_exposure_program);
		LinearLayout pictureInfoSaturabilityLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_saturability);
		LinearLayout pictureInfoDefinitionLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_definition);
		LinearLayout pictureInfoWhiteBalanceLayout = (LinearLayout) this
						.findViewById(R.id.picture_info_white_balance);

		TextView titleContrastView = (TextView) pictureInfoContrastLayout
						.findViewById(R.id.picture_info_title);
		TextView titleBrightnessView = (TextView) pictureInfoBrightnessLayout
						.findViewById(R.id.picture_info_title);
		TextView titleExposureProgramView = (TextView) pictureInfoExposureProgramLayout
						.findViewById(R.id.picture_info_title);
		TextView titleSaturabilityView = (TextView) pictureInfoSaturabilityLayout
						.findViewById(R.id.picture_info_title);
		TextView titleDefinitionView = (TextView) pictureInfoDefinitionLayout
						.findViewById(R.id.picture_info_title);
		TextView titleWhiteBalanceView = (TextView) pictureInfoWhiteBalanceLayout
						.findViewById(R.id.picture_info_title);

		mContentContrastView = (TextView) pictureInfoContrastLayout
						.findViewById(R.id.picture_info_content);
		mContentBrightnessView = (TextView) pictureInfoBrightnessLayout
						.findViewById(R.id.picture_info_content);
		mContentExposureProgramView = (TextView) pictureInfoExposureProgramLayout
						.findViewById(R.id.picture_info_content);
		mContentSaturabilityView = (TextView) pictureInfoSaturabilityLayout
						.findViewById(R.id.picture_info_content);
		mContentDefinitionView = (TextView) pictureInfoDefinitionLayout
						.findViewById(R.id.picture_info_content);
		mContentWhiteBalanceView = (TextView) pictureInfoWhiteBalanceLayout
						.findViewById(R.id.picture_info_content);

		titleContrastView.setText(getString(R.string.picture_info_contrast));
		titleBrightnessView
						.setText(getString(R.string.picture_info_brightness));
		titleExposureProgramView
						.setText(getString(R.string.picture_info_exposure_program));
		titleSaturabilityView
						.setText(getString(R.string.picture_info_saturability));
		titleDefinitionView
						.setText(getString(R.string.picture_info_definition));
		titleWhiteBalanceView
						.setText(getString(R.string.picture_info_white_balance));

		mNameView = (AlwaysMarqueeTextView) findViewById(R.id.picture_info_name);
		mThumbnailView = (ImageView) findViewById(R.id.picture_info_thumbnail);
		//mPictureInfoLinearLayout = (LinearLayout) findViewById(R.id.pictureInfo_L);
		mPictureInfoImageViewBack = (ImageView) findViewById(R.id.picture_info_image_back);
		
		mOpenView = (TextView) findViewById(R.id.picture_info_open);
		mOpenView.setFocusable(true);
		mOpenView.requestFocus();
		mDeleteView = (TextView) findViewById(R.id.picture_info_delete);
		
		mHeadBackLinearLayout = (LinearLayout) findViewById(R.id.head_linearLayout);

		mOpenView.setOnClickListener(mOnClickListener);
		mDeleteView.setOnClickListener(mOnClickListener);
		mHeadBackLinearLayout.setOnClickListener(mOnClickListener);

		initData();

	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void initData() {
		Intent intent = getIntent();
		String filePath = intent.getStringExtra(TAG);
		mColorIndex = intent.getIntExtra(Constant.COLOR_INDEX, -1);
		mPictureInfo = new PictureInfo(this, filePath);

		// 读取信息
		mPictureInfo.readExifTagInfoInEnhance();

		mContentSizeView.setText(mPictureInfo.getSize());
		mContentDateView.setText(mPictureInfo.getDate());
		mContentDimenView.setText(mPictureInfo.getWidth() + " * "
						+ mPictureInfo.getHeight());
		mContentPathView.setText(mPictureInfo.getFilePath());

		// 照相机
		mContentManufacturersView.setText(mPictureInfo.getMaker());
		mContentCameraModelView.setText(mPictureInfo.getModel());
		mContentFNumberview.setText(mPictureInfo.getFNumber());
		mContentExposureTimeView.setText(mPictureInfo.getExposureTime());
		mContentIsoView.setText(mPictureInfo.getIsoSpeedRate());
		mContentExposureCompensationView.setText(mPictureInfo
						.getExposureBiasValue());
		mContentFocalLengthView.setText(mPictureInfo.getFocalLength());
		mContentMaximumApertureView.setText(mPictureInfo.getMaxApertureValue());
		mContentMeteringModeView.setText(mPictureInfo.getMeteringMode());
		mContentObjectDistanceView.setText(mPictureInfo.getTargetDistance());
		mContentFlashModeView.setText(mPictureInfo.getFlash());

		// 高级照片
		mContentContrastView.setText(mPictureInfo.getContrast());
		mContentBrightnessView.setText(mPictureInfo.getBrightness());
		mContentExposureProgramView.setText(mPictureInfo.getExposureProgram());
		mContentSaturabilityView.setText(mPictureInfo.getSaturation());
		mContentDefinitionView.setText(mPictureInfo.getSharpness());
		mContentWhiteBalanceView.setText(mPictureInfo.getWhiteBalanceMode());

		// 名字
		Trace.Debug("####initData " + mPictureInfo.getName());
		int index = mPictureInfo.getName().lastIndexOf(".");
		String name = mPictureInfo.getName().substring(0, index);
		mNameView.setText(name); 

		// 缩略图 getDimensionPixelSize获取的是像素值（四舍五入取整）
//		int width = this.getResources().getDimensionPixelSize(
//						R.dimen.picture_info_thumbnail_width);
//
//		int height = this.getResources().getDimensionPixelSize(
//						R.dimen.picture_info_thumbnail_height);

		Bitmap bitmap = mPictureInfo.getThumbnail(320, 240);
		//当图片浏览时出现oom时(或者decode出错时，即文件不能被解开），然后进入图片详情页查看时是getThumbnail获取到的缩略图有可能是为空的，因为系统有可能尚未恢复
		//所以默认设置详情页的缩略图为裂开的图
		if (bitmap == null) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) this.getResources().getDrawable(R.drawable.photo_open_failed);
			bitmap = bitmapDrawable.getBitmap();
		}
		mThumbnailView.setImageBitmap(bitmap);

		//放大Bitmap，用于适应背景
//		Matrix localMatrix = new Matrix();
//		float scaleW = Utils.getScreenW(this) / (bitmap.getWidth() * 1.0f);
//		float scaleH = Utils.getScreenH(this) / (bitmap.getHeight() * 1.0f);
//		float scale = (scaleW > scaleH) ? scaleW : scaleH;
//		localMatrix.postScale(scale, scale);
//		Bitmap bitmapScreen = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), 
//				bitmap.getHeight(), localMatrix, true);
		
		//先从缓存中，若找不到再进行缩放处理以节省运行时间
//		String filePath = mList.get(mViewPager.getCurrentItem());
//		Bitmap bitmap = ImageLoader.getInstance(PictureInfoActivity.this).getBitmapFromCache(filePath);
//		if(bitmap == null) {
//			Trace.Debug("####onClick Music Menu blur default image");
//			Matrix localMatrix = new Matrix();
//			Bitmap b = (Bitmap) view.getTag(R.id.tag_bitmap);
//			float scaleW = 480 / (b.getWidth() * 1.0f);
//			float scaleH = 270 / (b.getHeight() * 1.0f);
//			float scale = (scaleW < scaleH) ? scaleW : scaleH;
//			localMatrix.postScale(scale, scale);
//			bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), 
//					b.getHeight(), localMatrix, true);
//		}

		Bitmap tmpBitmap = Blur.fastblur(PictureInfoActivity.this,
				bitmap,18);

		mPictureInfoImageViewBack.setImageBitmap(tmpBitmap);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// 显示删除对话框
	private void showDeleteDialog() {
		final Dialog dialog = new Dialog(PictureInfoActivity.this,R.style.delete_dialog);
		// 去掉标题栏
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.delete_dialog, null);

		view.findViewById(R.id.decideButton).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
								// 删除操作
								Trace.Debug("####showDeleteDialog delete confirm");
								Intent intent = getIntent();
								String filePath = intent.getStringExtra(TAG);
								File file = new File(filePath);
								PhotoActivity.deleteFile(PhotoActivity.getLikePhotoList(1),filePath);
								PhotoActivity.deleteFile(PhotoActivity.getLikePhotoList(2),filePath);
								PhotoActivity.deleteFile(PhotoActivity.getLikePhotoList(3),filePath);
								int position = intent.getIntExtra(Constant.PLAY_INDEX, -1);
								if (file != null) {
									file.delete();
									
									if(mColorIndex != -1) {
										Trace.Debug("####showDeleteDialog delete color list");
										//处理标签Activity的入口
										List<CommonFileInfo> colorList = PhotoActivity.getLikePhotoList(mColorIndex);
										colorList.remove(position);
									} else {
										Trace.Debug("####showDeleteDialog delete send list");
										//处理正常浏览播放入口
										PhotoActivity.mSendList.remove(position);		
									}
								}																					
								QuickToast.showToast(
												PictureInfoActivity.this,
												PictureInfoActivity.this
																.getString(R.string.delete_success));
								PhotoActivity.setIsDelete(true);
								//PictureInfoActivity.this.setResult(4);								
								PictureInfoActivity.this.finish();
							}
						});

		view.findViewById(R.id.cancelButton).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
		
		view.findViewById(R.id.cancelButton).requestFocus();

		dialog.setContentView(view);
		Window window = dialog.getWindow();
		window.setLayout(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		dialog.show();
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.picture_info_open:
				Intent intent = new Intent(PictureInfoActivity.this,
								ImageViewPagerActivity.class);
				int index = PictureInfoActivity.this.getIntent().getIntExtra(
								Constant.PLAY_INDEX, 0);
				Trace.Debug("###PictureInfoActivity " + index);
				intent.putExtra(Constant.PLAY_INDEX, index);
				//为颜色标签页准备的
				intent.putExtra(Constant.COLOR_INDEX, mColorIndex);
				Trace.Debug("####PictureInfoActivity mColorIndex is " + mColorIndex);
				startActivity(intent);
				PictureInfoActivity.this.finish();
				break;
			case R.id.picture_info_delete:
				showDeleteDialog();
				break;
			case R.id.head_linearLayout:
				PictureInfoActivity.this.finish();
				break;
			default:
				break;
			}
		}
	};
}