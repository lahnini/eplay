package com.konka.eplay.modules;

import java.util.List;


import com.konka.eplay.Constant.SortType;

/**
 * 
 * Created on: 2013-10-10
 * 
 * @brief 对文件的操作
 * @author Eric Fung
 * @date Latest modified on: 2013-10-10
 * @version V1.0.00
 * 
 */
public interface IOperation {

	/**
	 * 
	 * @param path
	 */
	public void list(String path);
	
	/**
	 * @brief 排序
	 * @return 标准结果
	 */
	public void sort(List<CommonFileInfo> list, SortType sortBy);

	/**
	 * @brief 新建
	 * @return 标准结果
	 */
	//public void create(String path);

	/**
	 * @brief 重命�?
	 * @return 标准结果
	 */
	//public void rename(String oldPath, String newPath);

	/**
	 * @brief 单个删除
	 * @return 标准结果
	 */
	//public void delete(CommonFileInfo file);

	/**
	 * @brief 多个删除
	 * @return 标准结果
	 */
	//public void delete(List<CommonFileInfo> fileList);

	/**
	 * @brief 单个上传
	 * @return 标准结果
	 */
	// public CommonResult upload(UploadParam param);

	/**
	 * @brief 多个上传
	 * @return 标准结果
	 */
	// public CommonResult uploadDir(String dir, String cloudDir,
	// List<UploadParam> paramsList);

	/**
	 * @brief 单个下载
	 * @return 标准结果
	 */
	// public CommonResult download(DownloadParam param, boolean overrite);

	/**
	 * @brief 多个上传
	 * @return 标准结果
	 */
	// public CommonResult downloadDir(String dir, List<DownloadParam>
	// paramsList, boolean overrite);

	/**
	 * @brief 运行。比如，图片就跳转到图片播放器，apk就跳转到安装界面等等
	 * @return 标准结果
	 */
	//public CommonResult run(Context context, CommonFileInfo file);

	/**
	 * @brief 收藏
	 * @return 标准结果
	 */
	//public CommonResult collect(CommonFileInfo file);

	/**
	 * @param file
	 * @return 标准结果
	 * 
	 *         public List<CommonFileInfo> open(CommonFileInfo file,
	 *         MultimediaType eMediaType);
	 */

	/**
	 * @brief 移动单个文件
	 * @param oldPath
	 * @param newPath
	 * @return 标准结果
	 */
	//public void move(String oldPath, String newPath);

	/**
	 * @brief 移动多个文件
	 * @param list
	 * @param newPath
	 * @return 标准结果
	 */
	//public void move(List<CommonFileInfo> list, String newPath);

}
