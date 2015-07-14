package com.konka.eplay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

/**
 * 
 * Created on: 2013-3-28
 * 
 * @brief 
 *        动态注册的广播处理，非常驻.start和stop方法配套使用，一般在onCreate中调用start方法，在onDestroy中调用stop方法
 * @author Eric Fung
 * @date Latest modified on: 2013-3-28
 * @version V1.0.00
 * 
 */
public class MainNonResidentReceiver {
	private BroadcastReceiver m_Receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Uri uri = intent.getData();
			String path = uri.getPath();

			if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED)) {

			} else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_UNMOUNTED)) {

			}
		}

	};

	public void startUsbReceiver(Context context, BroadcastReceiver receiver) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");

		context.registerReceiver(receiver, filter);
	}

	public void stopUsbReceiver(Context context, BroadcastReceiver receiver) {
		context.unregisterReceiver(receiver);
	}
}
