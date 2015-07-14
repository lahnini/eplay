package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipException;

import com.konka.eplay.modules.MainActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * @ClassName: MusicDBManager
 * @Description: 数据库操作，获取相关的歌手名
 * @author xuyunyu
 * @date 2015年6月9日 上午11:33:38
 * @version 1.0
 *
 */
public class MusicDBManager {

	public static final String DB_NAME = "singer.db"; // 保存的数据库文件名

	private SQLiteDatabase mDatabase;
	private Context mContext;

	public MusicDBManager(Context context) {
		this.mContext = context;
	}

	public void openDatabase() {
		if (null==mContext) {
			return;
		}
		this.mDatabase = this.openDatabase(mContext.getFilesDir().getAbsolutePath() + "/" + DB_NAME);
	}

	private SQLiteDatabase openDatabase(String dbfile) {
		copyDatabase();

		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
		return db;
	}

	public String queryDatabase(String singerID) {
		openDatabase();
		String singerName = "";
		if (null == mDatabase) {
			return null;
		}
		//查询姓名
		Cursor cur = mDatabase.rawQuery("SELECT * FROM artist WHERE _id = '" + singerID + "'", null);
		if (cur != null && cur.getCount() != 0) {
			if (cur.moveToFirst()) {
				do {
					singerName = cur.getString(cur.getColumnIndex("_name"));
					Trace.Debug("id=="+singerID+"    singername=="+singerName);
				} while (cur.moveToNext());
			}
		} else {
			singerName = null;
		}
		//关闭cursor和数据库
		cur.close();
		mDatabase.close();
		return singerName;
	}

	public void copyDatabase() {

		String path = mContext.getFilesDir().getAbsolutePath() + "/" + DB_NAME;
		if (!new File(path).exists()) {
			copyFileToLocal(mContext.getFilesDir().getAbsolutePath() + "/" + "singer.zip");
			try {
				MusicUtils.unZipFile(mContext.getFilesDir().getAbsolutePath() + "/" + "singer.zip", mContext.getFilesDir().getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ZipException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void copyFileToLocal(String path) {

		InputStream myInput = null;

		Trace.Debug(path);
		OutputStream myOutput = null;
		try {
			myOutput = new FileOutputStream(path);
			myInput = mContext.getAssets().open("singer.zip");
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

}
