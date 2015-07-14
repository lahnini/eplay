package com.konka.eplay.receivers;

import com.konka.eplay.Constant;
import com.konka.eplay.services.MainService;

import iapp.eric.utils.base.Trace;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @brief 整个工程对外的广播接收器,是常驻型监听广播,要监听的广播需要在AndroidManifest.xml先声明,如：
 * 
 *        <receiver android:name=".MainReceiver"> <intent-filter> <!-- 系统启动广播
 *        --> <action
 *        android:name="android.intent.action.BOOT_COMPLETED"></action>
 *        <category android:name="android.intent.category.HOME"></category>
 *        </intent-filter> </receiver>
 * @author Administrator
 * @date 2013-2-22
 * @version V1.0.00
 * 
 */

public class MainResidentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Trace.Info("receive broadcast:" + intent.getAction());

		/** 系统启动完成的广播 */
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, MainService.class);
			i.putExtra(Constant.INTENT_KEY_MESSAGE,
							Constant.MessageType.MSG_BOOT);
			context.startService(i);
		}
		/**
		 * 添加需要接收的广播 else if(){ xxx }
		 */
	}

}
