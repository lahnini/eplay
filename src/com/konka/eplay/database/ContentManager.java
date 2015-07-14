package com.konka.eplay.database;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap.Config;
import android.net.Uri;

import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Configuration;
import com.konka.eplay.Utils;
import com.konka.eplay.model.LocalDiskInfo;
import com.konka.eplay.modules.CommonFileInfo;

/**
 * 
 * @Created on 2013-12-3
 * @brief 【移动设备全盘扫描数据缓存】数据库缓存数据的读取写入。涉及操作数据库，尽量减少调用次数
 * 
 * @author LiLiang
 * @date Latest modified on: 2013-12-3
 * @version V1.0.00
 * 
 */
public class ContentManager {


	/**
	 * 保存数据到数据库中，目前只缓存图片、音乐、电影、文档的path 建议异步调用 只在程序退出时onDestroy时调用一次，避免频繁打开/关闭DB
	 * 
	 * @param fileList
	 * @param eType
	 */
	public static void writeData2DB(Context context,
					List<CommonFileInfo> fileList, MultimediaType eType) {
		Trace.Info("++Write2DB");

		DBHelper dbHelper = null;
		try {
			Map<String, List<String>> result = convert(context, fileList);
			Trace.Info("###resultsize();##"+result.size());
			//if(result==null)
			//	return ;
			Uri uri;
			switch (eType) {
			case MMT_PHOTO:
				uri = MyContentProvider.IMG_CONTENT_URI;
				break;
			case MMT_MUSIC:
				uri = MyContentProvider.AUDIO_CONTENT_URI;
				break;
			case MMT_MOVIE:
				uri = MyContentProvider.VIDEO_CONTENT_URI;
				break;
			case MMT_DOCUMENT:
				uri = MyContentProvider.DOC_CONTENT_URI;
				break;
			case MMT_REDPHOTO:
				Trace.Debug("##switch#MMT_REDPHOTO");
				uri=MyContentProvider.REDPHOTO_CONTENT_URI;
				break;
			case MMT_BLUEPHOTO:
				Trace.Debug("##switch#MMT_BLUEPHOTO");
				uri=MyContentProvider.BLUEPHOTO_CONTENT_URI;
				break;
			case MMT_YELLOWPHOTO:
				Trace.Debug("##switch#MMT_YELLOWPHOTO");
				uri=MyContentProvider.YELLOWPHOTO_CONTENT_URI;
				break;
			case MMT_LIKEMUSIC:
				Trace.Debug("##switch#MMT_LIKEMUSIC");
				uri=MyContentProvider.LIKEMUSIC_CONTENT_URI;
				break;
			case MMT_ALLMUSIC:
				Trace.Debug("##switch#MMT_ALLMUSIC");
				uri=MyContentProvider.ALLMUSIC_CONTENT_URI;
				break;
			default:
				return;
			}
if( result.size() == 0 )
	context.getContentResolver().delete(uri, null, null);
			String usbRootPath;
			List<String> subs;
			for (Iterator<String> it = result.keySet().iterator(); it.hasNext();) {
				usbRootPath = it.next();
				subs = result.get(usbRootPath);
//			for (Iterator it = result.entrySet().iterator(); it.hasNext();) {
//				Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>)it.next();
//				usbRootPath = entry.getKey();
//				subs = entry.getValue();
				Trace.Debug("subs.size##"+subs.size());
				if (subs != null && subs.size() > 0) {
					File dir = new File(usbRootPath + "/" + DBHelper.DB_PATH);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					dbHelper = new DBHelper(context, dir.getAbsolutePath()
									+ "/" + DBHelper.DB_NAME);
					MyContentProvider.setCurDBInstance(dbHelper);
					context.getContentResolver().delete(uri, null, null);

					int size = subs.size();
					for (int i = 0; i < size; i++) {
						if (Configuration.isStopWrite) {
							break;
						}
						ContentValues values = new ContentValues();
						values.put(DBHelper.PATH, subs.get(i));
						context.getContentResolver().insert(uri, values);
						Trace.Debug("#WRITE path2DB##"+i);
					}
					dbHelper.close();
					dbHelper = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dbHelper != null) {
				dbHelper.close();
				MyContentProvider.setCurDBInstance(null);
				dbHelper = null;
			}
		}
	}

	/**
	 * 从某一个盘符缓存的数据库读取数据 删除掉的文件可以检测到，但新增的文件检测不到
	 * 
	 * @param context
	 * @param usbRootPath
	 * @return null or List<CommonFileInfo>
	 */
	public static List<File> readDataFromDB(Context context,
					String usbRootPath, MultimediaType eType) {
		Trace.Info("++ReadfromDB");
		try {
			Uri uri;
			switch (eType) {
			case MMT_PHOTO:
				uri = MyContentProvider.IMG_CONTENT_URI;
				break;
			case MMT_MUSIC:
				uri = MyContentProvider.AUDIO_CONTENT_URI;
				break;
			case MMT_MOVIE:
				uri = MyContentProvider.VIDEO_CONTENT_URI;
				break;
			case MMT_DOCUMENT:
				uri = MyContentProvider.DOC_CONTENT_URI;
				break;
			case MMT_REDPHOTO:
				uri=MyContentProvider.REDPHOTO_CONTENT_URI;
				break;
			case MMT_BLUEPHOTO:
				uri=MyContentProvider.BLUEPHOTO_CONTENT_URI;
				break;
			case MMT_YELLOWPHOTO:
				Trace.Debug("##switch yellowphoto" );
				uri=MyContentProvider.YELLOWPHOTO_CONTENT_URI;
				break;
			case MMT_ALLMUSIC:
				uri=MyContentProvider.ALLMUSIC_CONTENT_URI;
				break;
			case MMT_LIKEMUSIC:
				uri=MyContentProvider.LIKEMUSIC_CONTENT_URI;
				break;
			default:
				return null;
			}

			String dbPath = usbRootPath + "/" + DBHelper.DB_PATH + "/"
							+ DBHelper.DB_NAME;
			if (!new File(dbPath).exists()) {
				
				return null;
			}
			DBHelper dbHelper = new DBHelper(context, dbPath);
			MyContentProvider.setCurDBInstance(dbHelper);

			Cursor cursor = context.getContentResolver().query(uri, null, null,
							null, null);
			dbHelper.close();
			//读取标记为红的图片
				if (cursor == null)
					return null;
				if (cursor.moveToFirst()) {
					int count = cursor.getCount();
					// 使用map再转成list，避免数据库中有重复的缓存数据。LiLiang added on 2014-3-13
					Map<String, File> map = new HashMap<String, File>();
					String relativePath;
					String filePath;
					File file;
					for (int i = 0; i < count; i++) {
						cursor.moveToPosition(i);
						relativePath = cursor.getString(1); // 第0列存的是id，第一列存的是path
						filePath = usbRootPath + relativePath;
						file = new File(filePath);
						if (file.exists()) {
							map.put(file.getAbsolutePath(), file);
						}
					}
					cursor.close();
					Collection<File> files = map.values();
					List<File> list = new ArrayList<File>(files);
					 Trace.Info("###list.size() = " + list.size());
					 return list;
				} else {
				// 没有数据项
				return new ArrayList<File>();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		 Trace.Warning("###null 4444");
		return null;
	}

	/**
	 * 把文件列表按照所属盘符分类
	 * 
	 * @return
	 */
	private static Map<String, List<String>> convert(Context context,
					List<CommonFileInfo> fileList) {

		 Trace.Debug("###convert fileListSize="+fileList.size());
		// Trace.Debug("##### getExternalStorage()");
		ArrayList<LocalDiskInfo> usbList = Utils.getExternalStorage(context);
		if (usbList == null || usbList.size() == 0 || fileList == null)
			return null;

		Map<String, List<String>> result = new HashMap<String, List<String>>();
		int i;
		int count = usbList.size();
		String[] roots = new String[count];
		for (i = 0; i < count; i++) {
			roots[i] = usbList.get(i).getPath();
		}

		int size = fileList.size();
		String filePath;
		List<String> subs = null;
		String relativePath;
		for (i = 0; i < size; i++) {
			filePath = fileList.get(i).getPath(); // 绝对路径

			for (String usbRootPath : roots) {
				if (filePath.contains(usbRootPath)) { // 当前文件属于哪个盘符

					subs = result.get(usbRootPath);
					if (subs == null) {
						subs = new ArrayList<String>();
						result.put(usbRootPath, subs);
					}
					// 存入相对路径，relativePath以"/"开头。
					// eg "/mnt/usb/sda1/folder/test.jpg" 截断后=>
					// "/folder/test/jpg"
					relativePath = filePath.substring(usbRootPath.length());
					subs.add(relativePath);

					break;
				}
			}

		}
		return result;
	}
}
