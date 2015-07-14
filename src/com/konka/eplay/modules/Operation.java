package com.konka.eplay.modules;

import java.io.File;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.konka.eplay.Constant.LIST_TYPE;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.GlobalData;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.model.LocalProvider;

/**
 * 
 * Created on: 2013-10-9
 * 
 * @brief 文件管理模块涉及到的所有功能在此实现
 * @author Eric Fung
 * @date Latest modified on: 2013-10-9
 * @version V1.0.00
 * 
 */
public class Operation implements IOperation {
	private static GlobalData sGlobalApp;

	// 本地文件操作provider
	private LocalProvider mLocalProvider;

	private static Operation sInstance;

	/**
	 * @brief 私有化构造函数，避免被使用。
	 */
	private Operation() {
		mLocalProvider = new LocalProvider(sGlobalApp);
	}

	/**
	 * @brief 获取唯一实例接口
	 * @return
	 */
	public static Operation getInstance() {
		if (sInstance == null) {
			sInstance = new Operation();
		}
		return sInstance;
	}

	public static void init(GlobalData app) {
		sGlobalApp = (GlobalData) app;
		// 保证Operation构造函数在UI主线程 LiLiang added on 2014-1-16
		Operation.getInstance();
	}

	/**
	 * 获取HWProvider，方便操作不通用的接口
	 * 
	 * @return
	 */
	public LocalProvider getLocalProvider() {
		return mLocalProvider;
	}

	public Context getContext() {
		return sGlobalApp;
	}

	/**
	 * @brief 列出当前路径下的子文件 通过消息返回结果。 Message.what = Constant.MSG_LIST Message.obj
	 *        = List<CommonFileInfo>
	 */
	@Override
	public void list(String path) {
		list(path, LIST_TYPE.ALL);
	}

	public void list(String path, LIST_TYPE eType) {
		mLocalProvider.list(path, eType);
	}

	/**
	 * 暂只支持本地 列出目录下的指定子文件，不包含子文件夹，非递归
	 */
	public List<CommonFileInfo> getSpecificFiles(String path,
			MultimediaType eMediaType) {
		return mLocalProvider.getSpecificFiles(new File(path), eMediaType);
	}

	/**
	 * 递归扫描指定类型的文件（视频、音频、文档等）,通过消息返回非文件树结构的文件列表
	 * 
	 * @param eMediaType
	 * @param roots
	 *            ：本地时传递各个盘符的根目录路径集合；云端文件时置为null
	 * @param listParent
	 *            : 是否列出指定文件的父文件夹？true:按两层显示;false:按一层显示
	 *            按照需求图片按两层展示（true）、音乐和视频按一层展示（false）
	 * @param readCached
	 *            :true读取缓存文件，false：读取实时数据
	 */
	public void listWithSpecificMediaType(MultimediaType eMediaType,
			List<String> roots, boolean listParent) {
		Log.i("onlist1", "start");
		listWithSpecificMediaType(eMediaType, roots, listParent, true);
	}

	public void listWithSpecificMediaType(MultimediaType eMediaType,
			List<String> roots, boolean listParent, boolean readCached) {
		mLocalProvider.listWithSpecificMediaType(eMediaType, roots, listParent,
				readCached);
	}

	@Override
	public void sort(List<CommonFileInfo> list, SortType sortBy) {
		if (list == null || list.size() == 0)
			return;
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		switch (sortBy) {
		case ST_BY_NAME:
			Collections.sort(list, new FileComparator.sortByName());
			break;
		case ST_BY_TIME:
			Collections.sort(list, new FileComparator.sortByModifyDate());
			Collections.reverse(list);
			break;
		case ST_BY_SINGER:
			Collections.sort(list, new FileComparator.sortBySinger());
			break;
		case ST_BY_SPECIAL:
			Collections.sort(list, new FileComparator.sortBySpecial());
		default:
			break;

		}
	}

}
