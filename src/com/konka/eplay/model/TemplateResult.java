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
public class TemplateResult {
	public boolean success;
	public int type;
	public Object data;
	public int errorMsgType;
}
