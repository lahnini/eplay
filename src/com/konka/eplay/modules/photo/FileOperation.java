package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 处理文件与流的工具类<br>
 * 
 * <b>创建时间</b> 2014-10-29
 * 
 * @version 1.0
 * @author mcsheng
 */
public class FileOperation {

	private final static String TAG = "FileOperation";

	/**
	 * 检测SD卡是否存在
	 * 
	 * @return 存在SD卡返回true，不存在返回false
	 */
	public static boolean checkSDCard() {
		return Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED);
	}

	/**
	 * 将Byte类型的数据保存到本地文件,若本地文件已经存在则不保存
	 * 
	 */
	public static void saveDataToFile(byte[] fileData, String folderPath,
					String fileName) {
		File folder = new File(folderPath);
		// 若folder文件夹已经存在则返回false，即不创建
		folder.mkdirs();
		File file = new File(folder, fileName);
		ByteArrayInputStream is = new ByteArrayInputStream(fileData);
		OutputStream os = null;
		if (!file.exists()) {
			try {
				file.createNewFile();
				os = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				// 每次读1024字节数据
				while (-1 != (len = is.read(buffer))) {
					os.write(buffer, 0, len);
				}
				// 清除缓存
				os.flush();
			} catch (Exception e) {
				// 抛出运行时异常，使程序终止，同时将异常信息打印输出
				throw new RuntimeException("saveDataToFile of FileUtils"
								+ " Runtime error", e);
			} finally {
				closeIOStream(is, os);
			}
		} else {
			Log.i(TAG, "File has exists");
		}

	}

	/**
	 * 强制将byte类型的数据写入文件，即是当文件存在时就将文件删除掉，然后重新创建写入
	 * 
	 */
	public static void forceSaveDataToFile(byte[] filedata, String folderPath,
					String fileName) {
		File folder = new File(folderPath);
		folder.mkdirs();
		File file = new File(folder, fileName);
		OutputStream os = null;
		InputStream is = new ByteArrayInputStream(filedata);
		if (file.exists()) {
			file.delete();
			try {
				file.createNewFile();
				os = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				// 每次读1024字节数据
				while (-1 != (len = is.read(buffer))) {
					// 根据读取到的数据长度写入
					os.write(buffer, 0, len);
				}
				os.flush();
			} catch (Exception e) {
				throw new RuntimeException("forceSaveDataToFile of FileUtils"
								+ " Runtime error", e);
			} finally {
				closeIOStream(is, os);
			}

		} else {
			try {
				file.createNewFile();
				os = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				while (-1 != (len = is.read(buffer))) {
					os.write(buffer, 0, len);
				}
				os.flush();
			} catch (Exception e) {
				throw new RuntimeException("forceSaveDataToFile of FileUtils"
								+ " Runtime error", e);
			} finally {
				closeIOStream(is, os);
			}
		}

	}

	/**
	 * 将输入流转换为byte[]<br>
	 * <b>注意</b> 方法并没有关闭输入流参数，必须手动关闭is参数
	 */
	public static byte[] inputStreamToByte(InputStream is) {
		if (null == is) {
			return null;
		}
		byte[] in2b = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[100];
		int len = 0;
		try {
			while ((len = is.read(buffer, 0, 100)) > 0) {
				os.write(buffer, 0, len);
			}
			in2b = os.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("inputStreamToByte of FileUtils"
							+ " Runtime error", e);
		} finally {
			closeIOStream(os);
		}
		return in2b;
	}

	/**
	 * 将字节流的输入流中的数据转换为String类型数据
	 * 
	 * @return 成功返回String类型数据，输入流为空或读取失败则返回null
	 */
	public static String inputStreamToString(InputStream is) {
		if (null == is) {
			return null;
		}
		// StringBuilder用于处理或存储可变字符串
		StringBuilder result = null;
		try {
			// InputStreamReader将InputStream类型的字节流转换为Reader类型的字符流
			// BufferedReader将字符流转换为对应于字符的缓冲流
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			result = new StringBuilder();
			String tmpString = null;
			while (null != (tmpString = br.readLine())) {
				result.append(tmpString);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeIOStream(is);
		}
		return null == result ? null : result.toString();
	}

	/**
	 * 从指定的文件路径下读取文件内容，并将其转换为String类型数据返回
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 读取到的String类型数据，失败则为null
	 */
	public static String readFileToString(String filePath) {

		InputStream is = null;
		try {
			is = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("readFileToString of FileUtils error:"
							+ filePath + " not found", e);
		}
		return inputStreamToString(is);
	}

	/**
	 * 从assets目录下读取文件内容，并将其转换为String类型数据返回
	 * 
	 * @param context
	 * @param fileName
	 *            asset目录下的文件名
	 * @return 从assets目录下fileName文件读取到的String类型数据，失败则为null
	 */
	public static String readFileFromAssets(Context context, String fileName) {
		InputStream is = null;
		try {
			is = context.getResources().getAssets().open(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("readFileFromAssets of FileUtils error:"
							+ fileName + " not found", e);
		}
		return inputStreamToString(is);
	}

	/**
	 * 复制文件
	 */
	public static void copyFile(File from, File to) {
		if (null == from || !from.exists()) {
			return;
		}
		if (null == to) {
			return;
		}
		FileInputStream is = null;
		FileOutputStream os = null;
		try {
			if (!to.exists()) {
				to.createNewFile();
			}
			is = new FileInputStream(from);
			os = new FileOutputStream(to);
			copyFileStream(is, os);
		} catch (Exception e) {
			throw new RuntimeException("copyFile of FileUtils"
							+ " Runtime error", e);
		} finally {
			closeIOStream(is, os);
		}

	}

	/**
	 * 复制文件流对象<br>
	 * 利用nio来实现的
	 * 
	 * @param is
	 *            数据来源
	 * @param os
	 *            数据目标
	 */
	public static void copyFileStream(FileInputStream is, FileOutputStream os)
					throws Exception {
		FileChannel in = is.getChannel();
		FileChannel out = os.getChannel();
		in.transferTo(0, in.size(), out);
	}

	/**
	 * 保存Bitmap的图片数据类型到文件（保存得到的文件数据类型是png类型，并且压缩30%）
	 * 
	 * @param bitmap
	 *            Bitmap类型数据
	 * @param filePath
	 *            保存的文件路径
	 * @return true保存成功，fasle保存失败
	 */
	public static boolean saveBitmapToFile(Bitmap bitmap, String filePath) {
		boolean isSuccess = false;
		if (null == bitmap) {
			return isSuccess;
		}
		OutputStream out = null;
		try {
			// 以FileOutStream来创建一个缓存流，缓存大小为8*1024（即8K）
			out = new BufferedOutputStream(new FileOutputStream(filePath),
							8 * 1024);

			// 以png格式压缩Bitmap到out缓存流中，压缩（100-70）%即30%
			isSuccess = bitmap.compress(CompressFormat.PNG, 70, out);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 关闭输出流
			closeIOStream(out);
		}
		return isSuccess;
	}

	/**
	 * 关闭流<br>
	 * Closeable...为可用close方法关闭的对象的可变参数<br>
	 * closeables为参数集合
	 */
	public static void closeIOStream(Closeable... closeables) {
		// 判断为空或集合中没有存储元素则返回
		if (null == closeables || closeables.length <= 0) {
			return;
		}
		for (Closeable cb : closeables) {
			try {
				if (null == cb) {
					continue;
				}
				cb.close();
			} catch (Exception e) {
				// 抛出运行异常，程序终止运行
				throw new RuntimeException("closeIOStream of FileUtils"
								+ " runtime error", e);
			}
		}
	}

	public static void scanImagePath(final Context context,
					final ScanEndListener listener) {
		final HashMap<String, List<String>> groupMap = new HashMap<String, List<String>>();
		new Thread(new Runnable() {

			@Override
			public void run() {
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = context.getContentResolver();

				// 只查询jpeg和png的图片
				Cursor mCursor = mContentResolver
								.query(mImageUri,
												null,
												MediaStore.Images.Media.MIME_TYPE
																+ "=? or "
																+ MediaStore.Images.Media.MIME_TYPE
																+ "=?",
												new String[] { "image/jpeg",
																"image/png" },
												MediaStore.Images.Media.DATE_MODIFIED);

				if (mCursor == null) {
					return;
				}

				while (mCursor.moveToNext()) {
					// 获取图片的路径
					String path = mCursor.getString(mCursor
									.getColumnIndex(MediaStore.Images.Media.DATA));

					// 获取该图片的父路径名
					String parentName = new File(path).getParentFile()
									.getName();
					Trace.Debug("####scanImagePath " + parentName);

					// 根据父路径名将图片放入到gruopMap中
					if (!groupMap.containsKey(parentName)) {
						List<String> chileList = new ArrayList<String>();
						chileList.add(path);
						groupMap.put(parentName, chileList);
					} else {
						groupMap.get(parentName).add(path);
					}
				}

				mCursor.close();
				// 扫描完成后的回调
				if (listener != null) {
					listener.onEnd(groupMap);
				}
			}
		}).start();
	}

	public static List<String> getImageNames(String folderPath) {

		List<String> list = new ArrayList<String>();

		File file01 = new File(folderPath);

		String[] files01 = file01.list();

		for (int i = 0; i < files01.length; i++) {
			File file02 = new File(folderPath + "/" + files01[i]);

			// 当不是一个目录时
			if (!file02.isDirectory()) {

				// 当是图片文件时
				if (isImageFile(file02.getName())) {
					list.add(file02.getAbsolutePath());
				}
			}
		}
		return list;
	}

	private static boolean isImageFile(String fileName) {
		String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1,
						fileName.length());
		if (fileEnd.equalsIgnoreCase("jpg")) {
			return true;
		} else if (fileEnd.equalsIgnoreCase("png")) {
			return true;
		} else if (fileEnd.equalsIgnoreCase("bmp")) {
			return true;
		} else {
			return false;
		}
	}

	public interface ScanEndListener {
		public void onEnd(HashMap<String, List<String>> groupMap);

		public void onEnd(List<String> list);
	}

}