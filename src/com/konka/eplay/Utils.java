package com.konka.eplay;

import iapp.eric.utils.base.FormatConversion;
import iapp.eric.utils.base.Trace;
import iapp.eric.utils.enhance.HanziToPinyin;
import iapp.eric.utils.enhance.HanziToPinyin.Token;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.konka.android.storage.KKStorageManager;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.R.string;
import com.konka.eplay.model.LocalDiskInfo;
import com.konka.eplay.modules.CommonFileInfo;

public class Utils {
	/** 本项目支持的多媒体文件后缀 */
	private static String[] sExtImage = null; // 图片
	private static String[] sExtAudio = null; // 音乐
	private static String[] sExtVideo = null; // 电影
	private static String[] sExtDoc = null; // 文档
	private static String[] sExtApk = null; // apk
	private static String[] sExtArc = null; // 压缩包
	private static Map<String, MultimediaType> sExtMap;
	private static ArrayList<LocalDiskInfo> sUsbList;
	static {
		sExtMap = new HashMap<String, MultimediaType>();
		sUsbList = new ArrayList<LocalDiskInfo>();
	}

	static final int GB_SP_DIFF = 160;
	// 存放国标一级汉字不同读音的起始区位码
	static final int[] secPosValueList = { 1601, 1637, 1833, 2078, 2274, 2302,
					2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858,
					4027, 4086, 4390, 4558, 4684, 4925, 5249, 5600 };
	// 存放国标一级汉字不同读音的起始区位码对应读音
	static final char[] firstLetter = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
					'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w',
					'x', 'y', 'z' };

	/**
	 * @brief 获取网络数据 默认15秒超时。支持302跳转。
	 * @param url
	 *            目标数据的链接地址
	 * @param charset
	 *            服务器返回数据的字符编码 本方法根据指定的字符编码转化数据。可以为NULL。
	 * @return 字符串数据
	 */
	public static String get(String url, String charset) {
		String encodeUrl = FormatConversion.toURLEncode(url);

		HttpGet httpRequest = new HttpGet(encodeUrl);

		String ret = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpClientParams.setCookiePolicy(httpClient.getParams(),
							CookiePolicy.BROWSER_COMPATIBILITY);
			httpClient.getParams().setParameter(
							CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
			httpClient.getParams().setParameter(
							CoreConnectionPNames.SO_TIMEOUT, 15000);
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
							|| httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
				if (null == charset) {
					ret = EntityUtils.toString(httpResponse.getEntity());
				} else {
					ret = EntityUtils.toString(httpResponse.getEntity(),
									charset);
				}

				Trace.Debug(ret);
			} else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
				Trace.Warning("302 found!");
				String newUrl = httpResponse.getHeaders("location")[0]
								.getValue();
				String newEncodeUrl = FormatConversion.toURLEncode(newUrl);
				Trace.Info("newUrl=" + newUrl);
				String cookies = null;
				if (httpResponse.getHeaders("set-cookie") != null) {
					cookies = httpResponse.getHeaders("set-cookie")[0]
									.getValue();
				}
				HttpClient newClient = new DefaultHttpClient();
				newClient.getParams().setParameter(
								CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
				newClient.getParams().setParameter(
								CoreConnectionPNames.SO_TIMEOUT, 15000);

				HttpGet newGetMethod = new HttpGet(newEncodeUrl);
				if (cookies != null) {
					Trace.Info("cookie");
					newGetMethod.addHeader("set-cookie", cookies);
				}
				HttpResponse newResponse = newClient.execute(newGetMethod);
				;

				int newStatusCode = newResponse.getStatusLine().getStatusCode();
				if (newStatusCode == HttpStatus.SC_PARTIAL_CONTENT
								|| newStatusCode == HttpStatus.SC_OK) {
					if (null == charset) {
						ret = EntityUtils.toString(newResponse.getEntity());
					} else {
						ret = EntityUtils.toString(newResponse.getEntity(),
										charset);
					}
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * @brief 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 * @param context
	 *            上下文
	 * @param dpValue
	 *            设备独立像素dip
	 * @return 像素值
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 将px转换为dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将字体尺寸由px转换为sp
	 */
	public static int px2sp(Context context, float pxValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将字体尺寸由sp转换为px
	 */
	public static int sp2px(Context context, float spValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	/**
	 * 获取屏幕宽度
	 */
	public static int getScreenW(Context aty) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = aty.getResources().getDisplayMetrics();
		int w = dm.widthPixels;
		return w;
	}

	/**
	 * 获取屏幕高度
	 */
	public static int getScreenH(Context aty) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = aty.getResources().getDisplayMetrics();
		int h = dm.heightPixels;
		return h;
	}

	/*
	 * @brief 初始化后缀列表
	 * 
	 * @param gd Application实例
	 */
	public static void initExtList(Context context) {
		sExtAudio = context.getResources().getStringArray(
						R.array.extensionAudio);
		sExtImage = context.getResources().getStringArray(
						R.array.extensionImage);
		sExtVideo = context.getResources().getStringArray(
						R.array.extensionVideo);
		sExtDoc = context.getResources().getStringArray(
				R.array.extensionDocument);
		sExtApk = context.getResources().getStringArray(
				R.array.extensionPackage);
		sExtArc = context.getResources().getStringArray(
				R.array.extensionArchive);

		for (String ext : sExtAudio) {
			sExtMap.put(ext, MultimediaType.MMT_MUSIC);
		}
		for (String ext : sExtImage) {
			sExtMap.put(ext, MultimediaType.MMT_PHOTO);
		}
		for (String ext : sExtVideo) {
			sExtMap.put(ext, MultimediaType.MMT_MOVIE);
		}

		for (String ext : sExtDoc) {
			sExtMap.put(ext, MultimediaType.MMT_DOCUMENT);
		}
		for (String ext : sExtApk) {
			sExtMap.put(ext, MultimediaType.MMT_APK);
		}
		for (String ext : sExtArc) {
			sExtMap.put(ext, MultimediaType.MMT_ARCHIVE);
		}
	}

	/**
	 * 根据文件后缀获取所属类型
	 * 
	 * @param path
	 *            ，文件名或路径
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static MultimediaType getMmt(String path) {
		int index = path.lastIndexOf('.');
		if (index == -1)
			return MultimediaType.MMT_NONE;
		String postfix = path.substring(index).toLowerCase();
		if (sExtMap.containsKey(postfix)) {
			return sExtMap.get(postfix);
		}
		return MultimediaType.MMT_NONE;
	}

	/**
	 * @brief 获取程序的版本信息
	 * @param context
	 *            上下文
	 * @return 版本信息字符串
	 */
	public static String getVersion(Context context) {
		try {
			return context.getString(
							R.string.versionFormatter,
							context.getPackageManager().getPackageInfo(
											context.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Trace.Fatal("获取版本失败!");
			return "??";
		}
	}

	/**
	 * 将U盘路径替换为设备名称 eg "/mnt/usb/sda1/xx.txt" => "[盘符名]/xx.txt"
	 * 
	 * @param path
	 * @return
	 */
	public static String getWrapperPath(String path) {
		int size = sUsbList.size();
		LocalDiskInfo diskInfo;
		String label;
		for (int i = 0; i < size; i++) {
			diskInfo = sUsbList.get(i);
			if (path.contains(diskInfo.getPath())) {
				label = diskInfo.getLabel();
				if (!TextUtils.isEmpty(label)) {
					return path.replace(diskInfo.getPath(), label.trim());
				}
			}
		}
		return path;
	}

	/**
	 * 将U盘文件绝对路径中的U盘路径去掉 eg "/mnt/usb/sda1/xx.txt" => "/xx.txt"
	 * 
	 * @param path
	 * @return
	 */
	public static String getSubPath(String path) {
		String rootPath = getRootPath(path);
		if (rootPath != null && !rootPath.equals("")) {
			path = path.replace(rootPath, "");
		}
		return path;
	}

	/**
	 * @brief 根据时间戳获取“年-月-日”
	 * @param timestamp
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public static String getDate(long timestamp) {
		Date date = new Date(timestamp);
		return sdf.format(date);
	}

	public static String Md5(String plainText) {
		StringBuffer buf = new StringBuffer("");
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;

			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			// 32位加密
			// System.out.println("result: " + buf.toString());
			// 16位
			// System.out.println("result: " + buf.toString().substring(8, 24));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return buf.toString();
	}

	/**
	 * 对文件file的前bytes个字节作md5运算，若出错或参数不合，返回空字符串 若文件小于bytes个字节，对整个文件作md5
	 * 
	 * @author situ hui
	 * @param file
	 * @param bytes
	 * @return
	 */
	public static String Md5(File file, int bytes) {
		if (file == null || !file.exists()) {
			return "";
		}
		if (bytes <= 0) {
			return "";
		}
		String value = "";
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			byte[] buffer = new byte[1];
			MessageDigest md = MessageDigest.getInstance("MD5");
			int len;
			int count = 0;
			while ((len = in.read(buffer)) != -1 && count < bytes) {
				md.update(buffer, 0, len);
				count++;
			}
			BigInteger bi = new BigInteger(1, md.digest());
			value = bi.toString();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}

	/**
	 * 通过文件全路径获取父文件夹的路径
	 * 
	 * @param path
	 * @return
	 */
	public static String getParentPath(String path) {
		if (path.equals("/"))
			return path;
		else if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		int index = path.lastIndexOf("/");
		if (index == 0)
			index += 1;
		else if (index == -1) {
			return path;
		}
		return path.substring(0, index);
	}

	/**
	 * @brief 通过文件路径获取文件名
	 * @param 文件路径
	 * @return 文件名
	 */
	public static String getFileName(String filepath) {
		String name = null;
		if (filepath != null) {
			int start = filepath.lastIndexOf("/") + 1;
			int end = filepath.length();
			name = filepath.substring(start, end);
		}
		return name;
	}

	/**
	 * @brief 获取外部存储设备列表
	 * @param context
	 *            上下文
	 * @return 外部存储设备列表
	 */
	public static ArrayList<LocalDiskInfo> getExternalStorage(Context context) {
		Trace.Debug("#### getExternalStorage()");
		KKStorageManager kksm = KKStorageManager.getInstance(context);
		String[] volumes = kksm.getVolumePaths();

		sUsbList.clear();
		if (volumes == null) {

			return null;
		}
		Trace.Debug("volumes.length=" + volumes.length);
		for (int i = 0; i < volumes.length; ++i) {
			String state = kksm.getVolumeState(volumes[i]);

			if (state == null || !state.equals(Environment.MEDIA_MOUNTED)) {
				continue;
			}
			Trace.Debug("#### kksm.getVolumeLabel()"
							+ kksm.getVolumeLabel(volumes[i]));
			sUsbList.add(new LocalDiskInfo(volumes[i], kksm
							.getVolumeLabel(volumes[i])));
		}
		Trace.Debug("sUsblistSize=" + sUsbList.size());
		return sUsbList;
	}

	/**
	 * @brief 根据路径获取跟路径
	 * @param path
	 * @return rootPath
	 * @author Li Huiqi
	 */
	public static String getRootPath(String path) {
		int size = sUsbList.size();
		for (int i = 0; i < size; i++) {
			if (path.contains(sUsbList.get(i).getPath())) {
				return sUsbList.get(i).getPath();
			}
		}
		return "";
	}

	/***
	 * @brief 比较list中是否包含file（路径比较）
	 * @param list
	 * @param file
	 * @return
	 */
	public static boolean contains(List<CommonFileInfo> list,
					CommonFileInfo file) {
		int size = list.size();
		CommonFileInfo cfi;
		for (int i = 0; i < size; i++) {
			cfi = list.get(i);
			if (cfi.getPath().equals(file.getPath()))
				return true;
		}
		return false;
	}

	/**
	 * @brief 将毫秒数格式化成00:00的时间格式
	 * @author Eric
	 * @param ms
	 *            数值，单位毫秒
	 * @return 形如08:08的字符串
	 */
	@SuppressLint("DefaultLocale")
	public static String formatMusicDuration(int ms) {
		if (ms <= 0) {
			return "未知";
		}
		long s = ms / 1000;

		if (s <= 0) {
			return ("00:00");
		}

		long min = s / 60 % 60;
		long sec = s % 60;

		return (String.format("%02d:%02d", min, sec));
	}

	/**
	 * @brief 当前网络是否连通
	 * @param context
	 * @return 网络连接返回true，否则返回false
	 */
	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo()
						.isConnected());
	}

	/**
	 * @brief 得到densityDpi
	 * @param context
	 */
	public static int getDensityDpi(Context context) {
		int scale = context.getResources().getDisplayMetrics().densityDpi;
		return scale;
	}

	/**
	 * 将短时间格式时间转换为字符串 ddMMyyyy
	 * 
	 * @param dateDate
	 * @param k
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String dateToStr(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 将短时间格式时间转换为字符串 MM-yyyy
	 * 
	 * @param dateDate
	 * @param k
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String dateToStrMon(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM-yyyy");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	@SuppressLint("DefaultLocale")
	public static String getFirstChar(String nameString) {
		String string = "#";
		if (nameString != null && nameString.length() > 0) {
			 String spells =
			 getFullPinYin(nameString).toUpperCase().substring(
			 0, 1);
			if (spells.matches("[a-zA-Z0-9]+")) {

				return spells;
			} else {
				return string;
			}
		} else {

			return string;
		}

	}

	//
	// public static String getSpells(String characters) {
	// StringBuffer buffer = new StringBuffer();
	// for (int i = 0; i < characters.length(); i++) {
	//
	// char ch = characters.charAt(i);
	// if ((ch >> 7) == 0 || getFirstLetter(ch) == null) {
	// buffer.append(characters);
	// // 判断是否为汉字，如果左移7为为0就不是汉字，否则是汉字
	// }
	// else {
	//
	// char spell = getFirstLetter(ch);
	// buffer.append(String.valueOf(spell));
	//
	// }
	// }
	// return buffer.toString();
	// }

	// 获取一个汉字的首字母
	// public static Character getFirstLetter(char ch) {
	//
	// byte[] uniCode = null;
	// try {
	// uniCode = String.valueOf(ch).getBytes("GBK");
	// } catch (UnsupportedEncodingException e) {
	// e.printStackTrace();
	// return null;
	// }
	// if (uniCode[0] < 128 && uniCode[0] > 0) { // 非汉字
	// return null;
	// }
	// else {
	// return convert(uniCode);
	// }
	// }

	/**
	 * 获取一个汉字的拼音首字母。 GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码
	 * 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
	 * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
	 */
	static char convert(byte[] bytes) {
		char result = '-';
		int secPosValue = 0;
		int i;
		for (i = 0; i < bytes.length; i++) {
			bytes[i] -= GB_SP_DIFF;
		}
		secPosValue = bytes[0] * 100 + bytes[1];
		for (i = 0; i < 23; i++) {
			if (secPosValue >= secPosValueList[i]
							&& secPosValue < secPosValueList[i + 1]) {
				result = firstLetter[i];
				break;
			}
		}
		return result;
	}

	/**
	 * @author Li Huiqi
	 * @brief 检查当前播放的U盘是否被拔出
	 * @param rootPath
	 * @return
	 */
	public static boolean checkUsbEject(String rootPath, Context context) {
		Trace.Debug("##### getExternalStorage()");
		getExternalStorage(context);
		if (rootPath == null)
			return true;
		if (sUsbList.size() <= 0) {
			return true;
		} else {
			for (int i = 0; i < sUsbList.size(); i++) {
				if (rootPath.equals(sUsbList.get(i).getPath())) {
					return false;
				}
			}
		}
		return true;
	}

	public static String getFullPinYin(String source) {
		if (!Arrays.asList(Collator.getAvailableLocales()).contains(
						Locale.CHINA)) {
			return source;
		}
		ArrayList<Token> tokens = HanziToPinyin.getInstance().get(source);
		if (tokens == null || tokens.size() == 0) {
			return source;
		}
		StringBuffer result = new StringBuffer();
		for (Token token : tokens) {
			if (token.type == Token.PINYIN) {
				result.append(token.target);
			} else {
				result.append(token.source);
			}
		}
		if (result.toString().substring(0, 1).matches("[a-z0-9A-Z]")) {
			
			return result.toString();
		}else {
			return "#";
		}
	}

	public static String getFirstPinYin(String source) {

		if (!Arrays.asList(Collator.getAvailableLocales()).contains(
						Locale.CHINA)) {

			return source;

		}

		ArrayList<Token> tokens = HanziToPinyin.getInstance().get(source);

		if (tokens == null || tokens.size() == 0) {

			return source;

		}

		StringBuffer result = new StringBuffer();

		for (Token token : tokens) {

			if (token.type == Token.PINYIN) {

				result.append(token.target.charAt(0));

			} else {

				result.append("#");

			}

		}

		return result.toString();
	}

}