package com.konka.eplay.model;

/**
 * 
 * Created on: 2014年12月3日
 * 
 * @brief 异步任务返回的结果
 * @author Eric Fung
 * @date Latest modified on: 2014年12月3日
 * @version V1.0.00
 * 
 */
public class AsyncTaskResult {
	/** 标记任务处理是否成功 */
	public boolean success;
	/** 任务的类型 */
	public int type;
	/** 任务返回的数据（如果任务处理失败，此处为详细原因String） */
	public Object data;
	/** 任务处理失败的类型 */
	public int errorMsgType;
	/** 将结果回调给调用者的方法 */
	public AsyncTaskListener callback;
}
