package com.konka.eplay.modules.photo;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.eplay.R;
import com.konka.eplay.Utils;

/**
 * 
 * Created on: 2015-3-13
 * 
 * @brief 自定义的Toast类，可以实现快速地重复显示Toast
 * @author mcsheng
 * @date Latest modified on: 2015-3-26
 * @version V1.0.00
 * 
 */
public class QuickToast {

	/* 先注释掉，因为这个版本，如果快速显示Toast，不稳定 */
	// private static Toast mToast;
	// private static Handler mHandler = new Handler();
	// private static Runnable r = new Runnable() {
	// public void run() {
	// mToast.cancel();
	// }
	// };
	//
	// /**
	// * 显示Toast
	// * @param mContext
	// * 上下文配置
	// * @param text
	// * 要显示的文本
	// * @param duration
	// * 持续显示的时间
	// */
	// public static void showToast(Context mContext, String text, int duration)
	// {
	//
	// mHandler.removeCallbacks(r);
	// if (mToast != null)
	// mToast.setText(text);
	// else
	// mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
	// mHandler.postDelayed(r, duration);
	//
	// mToast.show();
	// }
	//
	//
	// /**
	// * 显示Toast
	// * @param mContext
	// * 上下文配置
	// * @param resId
	// * 要显示文本的资源Id
	// * @param duration
	// * 持续显示的时间
	// */
	// public static void showToast(Context mContext, int resId, int duration) {
	// showToast(mContext, mContext.getResources().getString(resId), duration);
	// }
	private static Toast sToast = null;
	
	private static Toast sNameToast = null;
	
	private static TextView sTextView = null;
	
	private static TextView sNameTextView = null;
	
	private static int sTime = 3000;

	public static void showToast(Context context, String text) {

		if (sToast != null) {
			if(sNameToast != null) {
				sNameToast.cancel();
			}
			// 不用cancel，直接用setText就不会出现闪烁的现象
			//sToast.setText(text);
			sTextView.setText(text);
		} else {
			sToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			sToast = new Toast(context);
			View view = View.inflate(context, R.layout.quick_toast, null);
			sTextView = (TextView) view.findViewById(R.id.toast_textView);
			sToast.setView(view);
			sTextView.setText(text);
			sToast.setDuration(sTime);
		}

		sToast.show();
	}
	
	public static void showToastForName(Context context, String text) {

		if (sNameToast != null) {
			if(sToast != null) {
				sToast.cancel();
			}
			// 不用cancel，直接用setText就不会出现闪烁的现象
			//sToast.setText(text);
			sNameTextView.setText(text);
		} else {
			sNameToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			sNameToast = new Toast(context);
			View view = View.inflate(context, R.layout.quick_toast, null);
			sNameTextView = (TextView) view.findViewById(R.id.toast_textView);
			sNameToast.setView(view);
			sNameTextView.setText(text);
			sNameToast.setDuration(sTime);
			sNameToast.setGravity(Gravity.TOP | Gravity.LEFT, 
					Utils.dip2px(context, 50), Utils.dip2px(context, 50));			
		}

		sNameToast.show();
	}
	
	public static void cancelToast() {
		if (sToast != null) {
			sToast.cancel();
		}
	
		sToast =null;
	}

}