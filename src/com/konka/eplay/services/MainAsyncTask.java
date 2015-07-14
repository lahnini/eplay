package com.konka.eplay.services;

import com.konka.eplay.ErrorCode;
import com.konka.eplay.model.AsyncTaskParam;
import com.konka.eplay.model.AsyncTaskResult;

import android.os.AsyncTask;

public class MainAsyncTask extends
				AsyncTask<AsyncTaskParam, Void, AsyncTaskResult> {

	@Override
	protected AsyncTaskResult doInBackground(AsyncTaskParam... params) {
		AsyncTaskParam param = params[0];
		AsyncTaskResult result = new AsyncTaskResult();
		result.success = false;
		result.type = param.type;
		result.callback = param.callback;

		switch (param.type) {
		default:
			result.errorMsgType = ErrorCode.ERROR_CODE_DEFAULT;
			result.data = ErrorCode.getMsgByErrorCode(result.errorMsgType);
			break;
		}

		return result;
	}

	@Override
	protected void onPostExecute(AsyncTaskResult result) {
		try {
			if (result.success) {
				// do something
				switch (result.type) {
				default:
					break;
				}
			} else {
				// do something
				switch (result.type) {
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
