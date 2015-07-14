package com.konka.eplay;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Created on: 2013-4-8
 * 
 * @brief 错误码类，单实例，通过getInstance获取
 * @author Eric Fung
 * @date Latest modified on: 2013-4-8
 * @version V1.0.00
 * 
 */
public class ErrorCode {
	/** 错误码以及对应的错误信息的键值对 */
	private static Map<Integer, String> mErrorCodeMap;

	public static final int ERROR_CODE_DEFAULT = 0;
	public static final int ERROR_CODE_NETWORK = 1;
	public static final int ERROR_CODE_EXCEPTION = 2;

	static {
		mErrorCodeMap = new HashMap<Integer, String>();
		/** 此处添加错误码和对应的错误信息 */
		mErrorCodeMap.put(ERROR_CODE_DEFAULT, "未知错误");
		mErrorCodeMap.put(ERROR_CODE_NETWORK, "网络不给力");
		mErrorCodeMap.put(ERROR_CODE_EXCEPTION, "代码执行异常");
	}

	// 单实例实现
	static class ErrorCodeHolder {
		static ErrorCode instance = new ErrorCode();
	}

	public static ErrorCode getInstance() {
		return ErrorCodeHolder.instance;
	}

	/**
	 * @brief 根据错误码获取错误信息
	 * @param errorCode
	 * @return
	 */
	public static String getMsgByErrorCode(int errorCode) {
		if (mErrorCodeMap.containsKey(errorCode)) {
			return (mErrorCodeMap.get(errorCode));
		}

		return mErrorCodeMap.get(ERROR_CODE_DEFAULT);
	}

	public static String getMsgByErrorCode(String errorCode) {
		int code = Integer.parseInt(errorCode);
		if (mErrorCodeMap.containsKey(code)) {
			return (mErrorCodeMap.get(code));
		}

		return mErrorCodeMap.get(ERROR_CODE_DEFAULT);
	}
}
