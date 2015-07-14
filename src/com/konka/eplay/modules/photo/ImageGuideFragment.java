package com.konka.eplay.modules.photo;


import iapp.eric.utils.base.Trace;

import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.modules.photo.GuideBorderView.OnMoveEndListener;
import com.konka.eplay.modules.photo.GuideBorderView.ViewLocation;

import android.app.Fragment;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnHoverListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageGuideFragment extends Fragment {
	
	private ImageView mImageView;
	private TextView mTextView;
	private GuideBorderView mGuideBorderView;
	
	private KeyScaleImageView mKeyScaleImageView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//第三个参数要为false,不然在代码中添加Fragment时会报错
		View view = inflater.inflate(R.layout.fragment_image_guide, container, false);
		mImageView = (ImageView) view.findViewById(R.id.image_guide_view);
		mTextView = (TextView) view.findViewById(R.id.guide_textView);
		mGuideBorderView = (GuideBorderView) view.findViewById(R.id.guide_border_view);
		
		if(KeyScaleImageView.sHasRotate) {
			if(KeyScaleImageView.sRotateBitmap == null) {
				Trace.Fatal("####initView sRotateBitmap is null");
			}
			//当高度大于限制值时关闭ImageView的硬件加速，以使可以显示
			if(KeyScaleImageView.sRotateBitmap.getWidth() > KeyScaleImageView.LIMIT_LENGTH || 
					KeyScaleImageView.sRotateBitmap.getHeight() > KeyScaleImageView.LIMIT_LENGTH) {
				mImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
			mImageView.setImageBitmap(KeyScaleImageView.sRotateBitmap);
			 
		} else {
			//当高度大于限制值时关闭ImageView的硬件加速，以使可以显示
			if(ImageViewPagerActivity.sCurrentBitmap.getWidth() > KeyScaleImageView.LIMIT_LENGTH || 
					ImageViewPagerActivity.sCurrentBitmap.getHeight() > KeyScaleImageView.LIMIT_LENGTH) {
				mImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
			mImageView.setImageBitmap(ImageViewPagerActivity.sCurrentBitmap);
		}
		
		
		mGuideBorderView.setOnMoveEndListener(new OnMoveEndListener() {
			
			@Override
			public void moveEnd(View view, int direction) {
				
				Point pointGuide = mGuideBorderView.getActualImageMeasure();
				Point pointKeyScale = mKeyScaleImageView.getActualImageMeasure();
				float widthScale = pointKeyScale.x / (float)pointGuide.x;
				float heightScale = pointKeyScale.y / (float)pointGuide.y;
				int distance = mGuideBorderView.getStepDistance();
				//左右移就宽的比例进行移动，上下移就以高的比例进行移动
				int heightDistance = (int) (distance * heightScale);
				int widthDistance = (int) (distance * widthScale);
				Trace.Debug("####moveEnd " + widthDistance + "," + heightDistance);
				switch(direction) {
				case GuideBorderView.MOVE_LEFT:
					mKeyScaleImageView.setTranslateDistance(widthDistance);
					mKeyScaleImageView.translate(KeyScaleImageView.TRAN_LEFT);
					break;
				case GuideBorderView.MOVE_RIGHT:
					mKeyScaleImageView.setTranslateDistance(widthDistance);
					mKeyScaleImageView.translate(KeyScaleImageView.TRAN_RIGNT);
					break;
				case GuideBorderView.MOVE_UP:
					mKeyScaleImageView.setTranslateDistance(heightDistance);
					mKeyScaleImageView.translate(KeyScaleImageView.TRAN_UP);
					break;
				case GuideBorderView.MOVE_DOWN:
					mKeyScaleImageView.setTranslateDistance(heightDistance);
					mKeyScaleImageView.translate(KeyScaleImageView.TRAN_DOWN);
					break;
				default:
					break;
				}
			}

			@Override
			public void moveEndForMouse(View view, int dx, int dy) {
				
				Point pointGuide = mGuideBorderView.getActualImageMeasure();
				Point pointKeyScale = mKeyScaleImageView.getActualImageMeasure();
				float widthScale = pointKeyScale.x / (float)pointGuide.x;
				float heightScale = pointKeyScale.y / (float)pointGuide.y;
				//左右移就宽的比例进行移动，上下移就以高的比例进行移动
				//mKeyScaleImageView移动的方向要取反,所以加-
				int heightDistance = (int) (-dy * heightScale);
				int widthDistance = (int) (-dx * widthScale);
				
				mKeyScaleImageView.translateForMouse(widthDistance, heightDistance);
				mGuideBorderView.highLightInBorder(-1, -1, -1, -1);
			}
		
		});

		mGuideBorderView.setOnTouchListener(mOnTouchListener);
		
		return view;
	}	
	
	@Override
	public void onStart() {
		super.onStart();
		mKeyScaleImageView = ((ImageScaleActivity)getActivity()).getKeyScaleImageView();
		mGuideBorderView.requestFocus();
		mGuideBorderView.setGuideView(mImageView);
		mGuideBorderView.setCoordinateView(mKeyScaleImageView);
		
		//倍数的获取不能放在onCreateView获取，因为初始化未完成，会出现空指针错误
		int scale = (int) ((ImageScaleActivity)getActivity()).getScale();
		mTextView.setText(scale + "");		
	}
	
	private int mLastTouchX = 0;
	private int mLastTouchY = 0;
	
	//监听鼠标事件
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View paramView, MotionEvent event) {
			Trace.Debug("####onTouch");
			int what = event.getAction();
			Rect guideBitmapRect = mGuideBorderView.getGuideBitmapRect();
			Rect referRect = mGuideBorderView.getReferRect();
			
			switch(what) {
			case MotionEvent.ACTION_DOWN:
				Trace.Debug("####onTouch down");
				mLastTouchX = (int) event.getRawX();  
				mLastTouchY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_UP:	
				Trace.Debug("####onTouch up");
				break;
			case MotionEvent.ACTION_MOVE:
				Trace.Debug("####onTouch move");
				int dx =(int)event.getRawX() - mLastTouchX;  
	            int dy =(int)event.getRawY() - mLastTouchY;  
	          
	            int left = mGuideBorderView.getLeft() + dx;  
	            int top = mGuideBorderView.getTop() + dy;  
	            int right = mGuideBorderView.getRight() + dx;  
	            int bottom = mGuideBorderView.getBottom() + dy; 
	            
	            //越界判断处理
	            if(left < guideBitmapRect.left - referRect.left) {
	            	left = guideBitmapRect.left - referRect.left;
	            	right = left + mGuideBorderView.getWidth();
	            }
	            if(right > guideBitmapRect.right - referRect.left) {
	            	right = guideBitmapRect.right - referRect.left;
	            	left = right - mGuideBorderView.getWidth();
	            }
	            if(top < guideBitmapRect.top - referRect.top) {
	            	top = guideBitmapRect.top - referRect.top;
	            	bottom = top + mGuideBorderView.getHeight();
	            }
	            if(bottom > guideBitmapRect.bottom - referRect.top) {
	            	bottom = guideBitmapRect.bottom - referRect.top;
	            	top = bottom - mGuideBorderView.getHeight();
	            }
	            
	            mGuideBorderView.setMoveForMouse(true);
	            mGuideBorderView.setMoverForMouseDistance(dx,dy);
	            mGuideBorderView.layout(left, top, right, bottom);
	            mLastTouchX = (int) event.getRawX();  
				mLastTouchY = (int) event.getRawY();
				break;
			default:
				break;
			}
			
			return false;
		}
	};

}