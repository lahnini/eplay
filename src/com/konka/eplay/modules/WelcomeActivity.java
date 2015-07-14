package com.konka.eplay.modules;

import com.konka.eplay.model.AsyncTaskParam;
import com.konka.eplay.modules.photo.ImageViewPagerActivity;
import com.konka.eplay.services.MainAsyncTask;
import com.konka.eplay.services.MainTimerService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.konka.eplay.R;

import iapp.eric.utils.base.*;

public class WelcomeActivity extends Activity implements
				android.view.View.OnClickListener {
	private Button m_btnGo = null;
	private Button mPictureButtom = null;
	private MainTimerService mTimerServcie = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		m_btnGo = (Button) findViewById(R.id.idGo);
		mPictureButtom = (Button) findViewById(R.id.idPictureBrower);
		m_btnGo.setOnClickListener(this);
		mPictureButtom.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.idGo:
			Trace.Debug("11111111111111");
			// 启动线程处理的例子
			doSomething();
			Trace.Debug("222222222222222");

			// 启动异步任务例子
			new MainAsyncTask().execute(new AsyncTaskParam(
							WelcomeActivity.this, 1));

			// 启动计时服务例子
			mTimerServcie = new MainTimerService(WelcomeActivity.this, null);
			mTimerServcie.startXXXXTimer();
			break;

		case R.id.idPictureBrower:
			Intent intent = new Intent(WelcomeActivity.this,
							ImageViewPagerActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	private void doSomething() {
		new Thread() {

			@Override
			public void run() {
				// 添加代码
			}

		}.start();
	}

	/*
	 * （非 Javadoc）
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// 不一定需要调用，看情况而定
		// mTimerServcie.TerminateTimer();

		super.onDestroy();
	}

}