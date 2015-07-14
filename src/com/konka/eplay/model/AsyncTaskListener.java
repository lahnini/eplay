package com.konka.eplay.model;

/**
 * 
 * Created on: 2014年12月3日
 * 
 * @brief 异步任务监听器
 * @author Eric Fung
 * @date Latest modified on: 2014年12月3日
 * @version V1.0.00
 * 
 */
public interface AsyncTaskListener {
	/** 成功时调用 */
	public void onSuccess(AsyncTaskResult result);

	/** 失败时调用 */
	public void onFailure(AsyncTaskResult result);
}
