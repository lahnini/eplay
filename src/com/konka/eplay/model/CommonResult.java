package com.konka.eplay.model;

/**
 * 
 * Created on: 2013-4-10
 * 
 * @brief 上层UI和底层数据交互接口中返回统一结果形式
 * @author Eric Fung
 * @date Latest modified on: 2013-4-10
 * @version V1.0.00
 * 
 */
public class CommonResult {
	/** 正确结果�? */
	public static final int OK = 0;
public long time=0;
	/**
	 * 结果�?, 0表示正常，其他�?�对应错误码
	 */
	public int code;

	/**
	 * code!=0时，对应错误信息字符�? code==0时，对应某个实体封装�?
	 */
	public Object data;

	/**
	 * 备用字段
	 */
	public Object data2;
	public Object data3;

	public CommonResult() {
		// code = ErrorCode.ERROR_CODE_DEFAULT;
	}
}
