package com.konka.eplay.modules.photo;

import com.konka.eplay.Constant;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 
 * Created on: 2015-4-13
 * 
 * @brief 图片详情PopupWindow
 * @author mcsheng
 * @date Latest modified on: 2015-4-13
 * @version V1.0.00
 */
public class PhotoDetailsPopupWindow extends PopupWindow {

	private Context mContext;
	private CommonFileInfo mFileInfo;
	private TextView mRedTextView;
	private TextView mBlueTextView;
	private TextView mYellowTextView;
	private PhotoActivity mPhotoActivity;
	
	private ImageView mDetailsImageView = null;
	private ImageView mRedImgaeView = null;
	private ImageView mYellowImageView = null;
	private ImageView mBlueImageView = null;
	private int mPosition = -1;
	private boolean isChanged=false;
	private int mColorIndex = -1;

	public PhotoDetailsPopupWindow(Context context, CommonFileInfo info,
					int position) {
		super(context);
		mContext = context;
		mFileInfo = info;
		mPosition = position;
		initView();
	}
	
	public PhotoDetailsPopupWindow(Context context, CommonFileInfo info,
			int position,int colorIndex) {
		super(context);
		mContext = context;
		mFileInfo = info;
		mPosition = position;
		mColorIndex = colorIndex;
		initView();
	}

	private void initView() {
		View view = View.inflate(mContext, R.layout.popupwindow_photo_details,
						null);
		mDetailsImageView = (ImageView) view
						.findViewById(R.id.photo_details_popupwindow_detail_imageView);
		mRedImgaeView = (ImageView) view
						.findViewById(R.id.photo_details_popupwindow_red_imageView);
		mYellowImageView = (ImageView) view
						.findViewById(R.id.photo_details_popupwindow_yellow_imageView);
		mBlueImageView = (ImageView) view
						.findViewById(R.id.photo_details_popupwindow_blue_imageView);
		mBlueTextView=(TextView)view.findViewById(R.id.mark_blue);
		mRedTextView=(TextView)view.findViewById(R.id.mark_red);
		mYellowTextView=(TextView)view.findViewById(R.id.mark_yellow);
		if (mFileInfo.getIsBlue()) {
			mBlueTextView.setText(R.string.cancel_mark);
		}else if (mFileInfo.getIsRed()) {
			mRedTextView.setText(R.string.cancel_mark);
		}else if (mFileInfo.getIsYellow()) {
			mYellowTextView.setText(R.string.cancel_mark);
			
		}

		setContentView(view);
		setHeight(LayoutParams.WRAP_CONTENT);
		setWidth(LayoutParams.WRAP_CONTENT);
		// 设背景透明，并自行处理返回键
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		mDetailsImageView.requestFocus();

		mDetailsImageView.setOnClickListener(mOnClickListener);
		mRedImgaeView.setOnClickListener(mOnClickListener);
		mYellowImageView.setOnClickListener(mOnClickListener);
		mBlueImageView.setOnClickListener(mOnClickListener);
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View paramView) {
			switch (paramView.getId()) {
			case R.id.photo_details_popupwindow_detail_imageView:
				Intent intent = new Intent(mContext, PictureInfoActivity.class);
				intent.putExtra(PictureInfoActivity.TAG, mFileInfo.getPath());
				intent.putExtra(Constant.PLAY_INDEX, mPosition);
				intent.putExtra(Constant.COLOR_INDEX, mColorIndex);
				((Activity) mContext).startActivityForResult(intent,3);
				PhotoDetailsPopupWindow.this.dismiss();
				//startActivityForResult
				break;
			case R.id.photo_details_popupwindow_red_imageView:
				isChanged=true;
				if (mFileInfo.getIsRed()) {
					mFileInfo.setIsRed(false);
					mRedTextView.setText(R.string.mark_red);
					PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.RED);
				}else {
				mRedTextView.setText(R.string.cancel_mark);	
				mBlueTextView.setText(R.string.mark_blue);	
				mYellowTextView.setText(R.string.mark_yellow);	
				mFileInfo.setIsRed(true);
				mFileInfo.setIsBlue(false);
				mFileInfo.setIsYellow(false);
				PhotoActivity.addLikePhoto(mFileInfo, PhotoActivity.RED);
				PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.BLUE);
				PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.YELLOW);
				}
				PhotoDetailsPopupWindow.this.dismiss();

				break;
			case R.id.photo_details_popupwindow_yellow_imageView:
				isChanged=true;
				if (mFileInfo.getIsYellow()) {
					mFileInfo.setIsYellow(false);
					mYellowTextView.setText(R.string.mark_yellow);
					PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.YELLOW);
				}else {
				
				mYellowTextView.setText(R.string.cancel_mark);	
				mBlueTextView.setText(R.string.mark_blue);	
				mRedTextView.setText(R.string.mark_red);	
				mFileInfo.setIsYellow(true);
				mFileInfo.setIsBlue(false);
				mFileInfo.setIsRed(false);
				PhotoActivity.addLikePhoto(mFileInfo, PhotoActivity.YELLOW);
				PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.BLUE);
				PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.RED);
				}				
				PhotoDetailsPopupWindow.this.dismiss();

				break;
			case R.id.photo_details_popupwindow_blue_imageView:
				isChanged=true;
				if (mFileInfo.getIsBlue()) {
					mFileInfo.setIsBlue(false);
					mBlueTextView.setText(R.string.mark_blue);
					PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.BLUE);
				}else {
				mBlueTextView.setText(R.string.cancel_mark);	
				mRedTextView.setText(R.string.mark_red);	
				mYellowTextView.setText(R.string.mark_yellow);	
				mFileInfo.setIsBlue(true);
				mFileInfo.setIsYellow(false);
				mFileInfo.setIsRed(false);
				PhotoActivity.addLikePhoto(mFileInfo, PhotoActivity.BLUE);
				PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.RED);
				PhotoActivity.deleteLikePhoto(mFileInfo, PhotoActivity.YELLOW);
				}
				PhotoDetailsPopupWindow.this.dismiss();
				break;
			default:
				break;
			}
		}
	};
	public boolean isChanged() {
		return isChanged;
	}
	
}