package com.konka.eplay.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * Created on: 2013-4-8
 * 
 * @brief 整个工程对外的service
 * @author Eric Fung
 * @date Latest modified on: 2013-4-8
 * @version V1.0.00
 * 
 */
public class MainService extends Service {

	//private Context m_Context = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 做一些善后工作，如解除与某个service的绑定
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

}
