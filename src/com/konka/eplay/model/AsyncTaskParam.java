package com.konka.eplay.model;

import android.content.Context;

/**
 * 
 * Created on: 2014年8月17日
 * 
 * @brief 异步任务所需要的参数类
 * @author Eric Fung
 * @date Latest modified on: 2014年8月17日
 * @version V1.0.00
 * 
 */
public class AsyncTaskParam {
	/** 上下文 */
	public Context context;
	/** 请求的类型，方便其他环节针对处理 */
	public int type;
	/** 任务处理时使用到的数据 */
	public Object data;
	/** 任务处理完通知调用者的方法 */
	public AsyncTaskListener callback;

	/** 按需添加所需要的的属性 */

	public AsyncTaskParam(Context context, int type) {
		super();
		this.context = context;
		this.type = type;
	}
}
