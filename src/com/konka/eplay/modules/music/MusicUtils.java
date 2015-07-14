package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Audio;
import iapp.eric.utils.base.Audio.LyricEngineType;
import iapp.eric.utils.base.Trace;
import iapp.eric.utils.metadata.Lyric;
import iapp.eric.utils.metadata.Mp3;
import iapp.eric.utils.metadata.SongInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;

/**
 * @ClassName: MusicUtils
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年4月2日 上午10:16:30
 */
public class MusicUtils {

	public static final String APP_DIR = "kk.com.konka.eplay";

	/**
	 * 实现背景模糊
	 *
	 * @param bmp
	 * @return
	 */
	public static Bitmap blurBackgroundImage(Context context, Bitmap bmp,int radius) {

		Bitmap bitmap;

		//bitmap尺寸太大的话，得缩小，避免崩溃
		if (bmp.getWidth()>530||bmp.getHeight()>530) {
			float scaleWidth1 = ((float) 300) / bmp.getWidth();
			float scaleHeight1 = ((float) 180) / bmp.getHeight();
			Matrix matrix1 = new Matrix();
			matrix1.postScale(scaleWidth1, scaleHeight1);
		    Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix1, true);
			bitmap = fastblur(context, resizedBitmap, radius);
		}else {
			bitmap = fastblur(context, bmp, radius);
		}

		int newWidth = Utils.getScreenW(context);
		int newHeight = Utils.getScreenH(context);
		Bitmap newBitmap = null;

		Matrix matrix = new Matrix();
		float scaleWidth = ((float) newWidth) / bitmap.getWidth();
		float scaleHeight = ((float) newHeight) / bitmap.getHeight();
		// float scale = Math.max(scaleWidth, scaleHeight);
		matrix.postScale(scaleWidth, scaleHeight);

		newBitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) bitmap.getWidth(), (int) bitmap.getHeight(), matrix, true);
		return newBitmap;
	}

	/**
	 * @Description: 快速生成毛玻璃效果，使用renderscript
	 * @param context
	 * @param sentBitmap
	 *            源bitmap
	 * @param radius
	 *            数值越大，越模糊，取值范围 1-24
	 * @return Bitmap
	 */
	@SuppressLint("NewApi")
	public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) {
		if (VERSION.SDK_INT > 16) {

			Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

			final RenderScript rs = RenderScript.create(context);
			final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
					Allocation.USAGE_SCRIPT);
			final Allocation output = Allocation.createTyped(rs, input.getType());
			final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
			script.setRadius(radius /* e.g. 3.f */);
			script.setInput(input);
			script.forEach(output);
			output.copyTo(bitmap);
			return bitmap;
		}

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		// Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		// Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}

	/**
	 *
	 * @Title: downloadLyricByUser
	 * @Description: 用户搜索歌词进行下载，无论本地是否有歌词缓存文件，在网络畅通的情况下都会进行下载
	 */
	public static void downloadLyricByUser(Context context, Handler handler, Lyric lyric, String title, String tag) {

		if (null == lyric) {
			sendNoLyricMessage(handler, tag);
			return;
		}
		Trace.Info("downloadLyric  by user");

		String rootPath = Utils.getRootPath(tag);
		String musicPath = rootPath + "/" + APP_DIR + "/music";

		if (!new File(musicPath).exists()) {
			new File(musicPath).mkdirs();
		}

		String md5Name = Utils.Md5(tag.substring(rootPath.length()));
		String lrcFilePath = rootPath + "/" + APP_DIR + "/music/" + md5Name + ".lrc";

		if (!Utils.isConnected(context)) {
			Trace.Info("用户搜索下载，网络没有连通，开始搜索本地歌词缓存");
			if (new File(lrcFilePath).exists()) {
				Trace.Info("歌词文件已存在");
				Message message = Message.obtain();
				message.what = LyricViewFragment.LYRICS_SHOWING;
				LyricResult result = new LyricResult();
				result.filepath = lrcFilePath;
				result.LRCtag = tag;
				message.obj = result;
				handler.sendMessage(message);
				return;
			} else {
				Trace.Info("没有网络，也没有缓存的歌词文件");
				// sendNoLyricMessage(handler, tag);
				Message message = Message.obtain();
				message.what = LyricViewFragment.LYRICS_SEARCH_NETWORK_DISCONNECTED;
				LyricResult result = new LyricResult();
				result.LRCtag = tag;
				message.obj = result;
				handler.sendMessage(message);
				return;
			}
		}

		Trace.Info("网络通畅，用户搜索歌词，开始下载");
		Audio a = new Audio();
		String conString;
		try {
			conString = a.downloadLyric(lyric);
		} catch (Exception e) {
			conString = null;
			e.printStackTrace();
		}
		if (null != conString) {
			// 下载到歌词内容缓存到文件

			Trace.Debug(conString);
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(lrcFilePath))));
				bw.write(conString);
				bw.close();

			} catch (Exception e) {
				e.printStackTrace();
				sendNoLyricMessage(handler, tag);
				return;
			} finally {
				if (new File(lrcFilePath).exists()) {
					Message message = Message.obtain();
					message.what = LyricViewFragment.LYRICS_SHOWING;
					LyricResult result = new LyricResult();
					result.filepath = lrcFilePath;
					result.LRCtag = tag;
					message.obj = result;
					handler.sendMessage(message);
				}
			}

		} else {
			Trace.Info("下载歌词对象为null");
			sendNoLyricMessage(handler, tag);
		}

	}

	/**
	 *
	 * @Title: downloadLyric
	 * @Description: 下载歌词
	 * @param title
	 * @param singer
	 * @param handler
	 * @param tag
	 *            标记唯一的tag，避免显示错误的歌词。
	 */
	public static void downloadLyric(Context context, String title, String singer, Handler handler, String tag) {

		Trace.Info("downloadLyric");

		String rootPath = Utils.getRootPath(tag);
		String musicPath = rootPath + "/" + APP_DIR + "/music";

		if (!new File(musicPath).exists()) {
			new File(musicPath).mkdirs();
		}

		String md5Name = Utils.Md5(tag.substring(rootPath.length()));
		String lrcFilePath = rootPath + "/" + APP_DIR + "/music/" + md5Name + ".lrc";

		if (new File(lrcFilePath).exists()) {
			Trace.Info("歌词文件已存在");
			Message message = Message.obtain();
			message.what = LyricViewFragment.LYRICS_SHOWING;
			LyricResult result = new LyricResult();
			result.filepath = lrcFilePath;
			result.LRCtag = tag;
			message.obj = result;
			handler.sendMessage(message);
			return;
		}

		Trace.Info("没有歌词缓存文件，开始下载");
		if (!Utils.isConnected(context)) {
			Trace.Info("网络没有连通");
			Message message = Message.obtain();
			message.what = LyricViewFragment.LYRICS_SEARCH_NETWORK_DISCONNECTED;
			LyricResult result = new LyricResult();
			result.LRCtag = tag;
			message.obj = result;
			handler.sendMessage(message);
			return;
		}

		Trace.Debug("###lhq 歌名" + title + "歌手" + singer + "lhq");
		Audio a = new Audio();
		SongInfo si = a.searchSongInfo(title, singer);

		if (null == si) {
			Trace.Debug("SongInfo == null");
			sendNoLyricMessage(handler, tag);
			return;
		}
		Trace.Debug("搜索歌词列表成功");
		ArrayList<Lyric> lyricList = si.getLyricList();
		if (null != lyricList && lyricList.size() != 0) {
			String lrcUrl = lyricList.get(0).getLrcUrl();
			// LyricEngineType let = lyricList.get(0).getLet();
			// Lyric l = new Lyric(let, lrcUrl);
			// Trace.Info("lrc url -->" + lrcUrl);
			Trace.Info("lrc url size-->" + lyricList.size());
			LyricEngineType let = lyricList.get(0).getLet();
			Lyric l = new Lyric(let, lrcUrl);
			Trace.Info("lrc url -->" + lrcUrl);
			// 开始下载歌词url
			String conString;
			try {
				conString = a.downloadLyric(l);
			} catch (Exception e) {
				conString = null;
				e.printStackTrace();
			}
			// 开始下载歌词url
			// String conString;
			// try {
			// conString = getLysriString(lrcUrl);
			// } catch (Exception e) {
			// conString = null;
			// e.printStackTrace();
			// }

			// a.parseLyric(paramPath);
			if (null != conString) {
				// 将歌词内容缓存到本地文件
				Trace.Debug(conString);
				BufferedWriter bw;
				try {
					bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(lrcFilePath))));
					bw.write(conString);
					bw.close();

				} catch (Exception e) {
					e.printStackTrace();
					sendNoLyricMessage(handler, tag);
					return;
				} finally {
					if (new File(lrcFilePath).exists()) {

						Message message = Message.obtain();
						message.what = LyricViewFragment.LYRICS_SHOWING;
						LyricResult result = new LyricResult();
						result.filepath = lrcFilePath;
						result.LRCtag = tag;
						message.obj = result;
						handler.sendMessage(message);
					}
				}

			} else {
				sendNoLyricMessage(handler, tag);
			}

		} else {
			sendNoLyricMessage(handler, tag);
		}
	}

	/**
	 * @Title: sendNoLyricMessage
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param handler
	 */
	private static void sendNoLyricMessage(Handler handler, String tag) {
		Message message = Message.obtain();
		message.what = LyricViewFragment.LYRICS_NO_SHOW_TIPS;
		LyricResult result = new LyricResult();
		result.LRCtag = tag;
		message.obj = result;
		handler.sendMessage(message);
	}

	private static String getLysriString(String url) {

		URL u;
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader buffer = null;
		try {
			u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();

			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("contentType", "UTF-8");
			// InputStream is = conn.getInputStream();

			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			in.mark(4);
			byte[] first3bytes = new byte[3];
			in.read(first3bytes);// 找到文档的前三个字节并自动判断文档类型。
			in.reset();
			if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {// utf-8

				buffer = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			} else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {

				buffer = new BufferedReader(new InputStreamReader(in, "unicode"));
			} else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {

				buffer = new BufferedReader(new InputStreamReader(in, "utf-16be"));
			} else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {

				buffer = new BufferedReader(new InputStreamReader(in, "utf-16le"));
			} else {

				buffer = new BufferedReader(new InputStreamReader(in, "GBK"));
			}

			// buffer = new BufferedReader(new
			// InputStreamReader(conn.getInputStream(), "UTF-8"));
			while ((line = buffer.readLine()) != null) {
				sb.append(line + "\n");
			}
			Trace.Info(sb.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				buffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static String getResourceString(Context context,int id){
		if (null!=context) {
			return context.getResources().getString(id);
		}else {
			return "";
		}

	}

	public static void unZipFile(String archive, String decompressDir) throws IOException, FileNotFoundException,
			ZipException {
		BufferedInputStream bi;
		ZipFile zf = new ZipFile(archive, "GBK");
		Enumeration<ZipEntry> e = zf.getEntries();
		while (e.hasMoreElements()) {
			ZipEntry ze2 = (ZipEntry) e.nextElement();
			String entryName = ze2.getName();
			String path = decompressDir + "/" + entryName;
			if (ze2.isDirectory()) {
				Trace.Debug("正在创建解压目录 - " + entryName);
				File decompressDirFile = new File(path);
				if (!decompressDirFile.exists()) {
					decompressDirFile.mkdirs();
				}
			} else {
				Trace.Debug("正在创建解压文件 - " + entryName);
				String fileDir = path.substring(0, path.lastIndexOf("/"));
				File fileDirFile = new File(fileDir);
				if (!fileDirFile.exists()) {
					fileDirFile.mkdirs();
				}
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decompressDir + "/"
						+ entryName));
				bi = new BufferedInputStream(zf.getInputStream(ze2));
				byte[] readContent = new byte[1024];
				int readCount = bi.read(readContent);
				while (readCount != -1) {
					bos.write(readContent, 0, readCount);
					readCount = bi.read(readContent);
				}
				bos.close();
			}
		}
		zf.close();
		// bIsUnzipFinsh = true;
	}

	public static void copySingerPhotoToCache(Context context) {
		InputStream myInput = null;

		String rootpath = context.getFilesDir().getAbsolutePath() + "/";
		String path = rootpath + "portraitThumbnail.zip";

		Trace.Debug(path);
		OutputStream myOutput = null;
		try {
			myOutput = new FileOutputStream(path);
			myInput = context.getAssets().open("portraitThumbnail.zip");
			byte[] buffer = new byte[1024];
			int length = myInput.read(buffer);
			while (length > 0) {
				myOutput.write(buffer, 0, length);
				length = myInput.read(buffer);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (myOutput != null && myInput != null) {
				try {
					myOutput.flush();
					myInput.close();
					myOutput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

	}

	public static void getAllSongInfo(List<CommonFileInfo> musicList, Handler handler){

		if (musicList == null) {
			return;
		}
		Mp3 m = null;
		CommonFileInfo fileInfo = null;
		String singer;
		String title;
		String time;
		String special;
		int bitrate;
		for (int i = 0; i < musicList.size(); i++) {

			if (musicList.size()<=i||musicList.size()==0) {
				return;
			}
			singer = "未知歌手";
			title = "未知歌名";
			time = "未知时间";
			special = "未知专辑";
			bitrate = 0;
			fileInfo = musicList.get(i);
			if (!new File(musicList.get(i).getPath()).exists()) {
				continue;
			}
			try {
				m = new Mp3(fileInfo.getPath(), false, false);
			} catch (Exception e) {
				e.printStackTrace();
				fileInfo.special = "未知专辑";
				fileInfo.singer = "未知歌手";
				fileInfo.title = "未知歌名";
				fileInfo.time = "未知时间";
				continue;
			}

			// 获取歌曲时间长度
			time = Utils.formatMusicDuration((int) m.getDuration());
			// 获取歌曲的比特率
			bitrate = m.getBitrate();
			if (null != m.getTagID3V2()) {
				if (m.getTagID3V2().getTagHeader().equals("ID3")) {
					Trace.Info("获取到了DI3V2标签中的数据");
					// 获取歌曲专辑
					if (null != m.getTagID3V2().getTagFrame().get("TALB")) {
						special = m.getTagID3V2().getTagFrame().get("TALB").getContent().toString().trim();
						Trace.Info("###专辑名称" + special);
					} else if (null != m.getTagID3V1()) {
						if (null != m.getTagID3V1().getAlbum()) {
							special = m.getTagID3V1().getAlbum().trim();
						}
						Trace.Info("###v1标签中的专辑名称" + special);
					}

					// 获取歌曲演唱者
					if (null != m.getTagID3V2().getTagFrame().get("TPE1")) {
						singer = m.getTagID3V2().getTagFrame().get("TPE1").getContent().toString().trim();
						Trace.Info("###歌手名称" + singer);
					} else if (null != m.getTagID3V1()) {
						if (null != m.getTagID3V1().getArtist()) {
							singer = m.getTagID3V1().getArtist().trim();
						}
					}

					// 获取歌曲名称
					if (null != m.getTagID3V2().getTagFrame().get("TIT2")) {
						title = m.getTagID3V2().getTagFrame().get("TIT2").getContent().toString().trim();
						Trace.Info("###标题名称" + title);
					} else if (null != m.getTagID3V1()) {
						if (null != m.getTagID3V1().getTitle()) {
							title = m.getTagID3V1().getTitle().trim();
						}
					}
				}
			}
			fileInfo.special = special;
			fileInfo.singer = singer;
			fileInfo.title = title;
			fileInfo.time = time;
			fileInfo.bitrate = bitrate;
		}

		if (null != handler) {
			handler.sendEmptyMessage(MusicActivity.REFRESH_DATA);
		}

	}

	public static String[] getSingleSongInfo(String songPath) {

		String[] results = new String[5];
		results[0] = "未知专辑";
		results[1] = "未知歌手";
		results[2] = "未知歌名";
		results[3] = "未知时间";
		results[4] = "0";
		Mp3 m = null;
		try {
			m = new Mp3(songPath, false, false);
		} catch (Exception e) {
			e.printStackTrace();
			return results;
		}

		results[3] = Utils.formatMusicDuration((int) m.getDuration());
		results[4] = m.getBitrate() + "";
		if (null != m.getTagID3V2()) {
			if (m.getTagID3V2().getTagHeader().equals("ID3")) {
				Trace.Info("获取到了DI3V2标签中的数据");
				if (null != m.getTagID3V2().getTagFrame().get("TALB")) {
					results[0] = m.getTagID3V2().getTagFrame().get("TALB").getContent().toString().trim();
				} else if (null != m.getTagID3V1()) {
					if (null != m.getTagID3V1().getAlbum()) {
						results[0] = m.getTagID3V1().getAlbum().trim();
					}
				}

				if (null != m.getTagID3V2().getTagFrame().get("TPE1")) {
					results[1] = m.getTagID3V2().getTagFrame().get("TPE1").getContent().toString().trim();
				} else if (null != m.getTagID3V1()) {
					if (null != m.getTagID3V1().getArtist()) {
						results[1] = m.getTagID3V1().getArtist().trim();
					}
				}

				if (null != m.getTagID3V2().getTagFrame().get("TIT2")) {
					results[2] = m.getTagID3V2().getTagFrame().get("TIT2").getContent().toString().trim();
				} else if (null != m.getTagID3V1()) {
					if (null != m.getTagID3V1().getTitle()) {
						results[2] = m.getTagID3V1().getTitle().trim();
					}
				}
			}

		}
		return results;

	}

}
