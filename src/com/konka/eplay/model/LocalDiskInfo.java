package com.konka.eplay.model;

/**
 * 
 * Created on: 2013-5-16
 * 
 * @brief 挂载磁盘的信�?
 * @author Eric Fung
 * @date Latest modified on: 2013-5-16
 * @version V1.0.00
 * 
 */
public class LocalDiskInfo {
	/** 挂载的绝对路�? */
	private String path;
	/** 卷标名称，可能为null */
	private String label;

	public LocalDiskInfo(String path, String label) {
		super();
		this.path = path;
		if (this.path.endsWith("/")) {
			this.path = this.path.substring(0, this.path.length() - 1);
		}
		this.label = label;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
		if (this.path.endsWith("/")) {
			this.path = this.path.substring(0, this.path.length() - 1);
		}
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {

		this.label = label;
	}
}
