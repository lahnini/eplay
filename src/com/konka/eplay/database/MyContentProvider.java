package com.konka.eplay.database;

import iapp.eric.utils.base.Trace;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * 
 * @Created on 2013-12-4
 * @brief 【移动设备全盘扫描数据缓存】从数据库查询数据，接口不对外使用
 * @author LiLiang
 * @date Latest modified on: 2013-12-4
 * @version V1.0.00
 * 
 */
public class MyContentProvider extends ContentProvider {
	public static final String AUTHORITY = "com.konka.eplay.provider";

	public static final Uri IMG_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_IMG);
	public static final Uri AUDIO_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_AUDIO);
	public static final Uri VIDEO_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_VIDEO);
	public static final Uri DOC_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_DOC);
	public static final Uri REDPHOTO_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_REDPHOTO);
	public static final Uri BLUEPHOTO_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_BLUEPHOTO);
	public static final Uri YELLOWPHOTO_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_YELLOWPHOTO);
	public static final Uri LIKEMUSIC_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_LIKEMUSIC);
	public static final Uri ALLMUSIC_CONTENT_URI = Uri.parse("content://"
					+ AUTHORITY + "/" + DBHelper.TABLE_ALLMUSIC);

	private static UriMatcher sUriMatcher;
	private static final int MATCH_IMG = 11;
	private static final int MATCH_AUDIO = 12;
	private static final int MATCH_VIDEO = 13;
	private static final int MATCH_DOC = 14;
	private static final int MATCH_REDPHOTO = 15;
	private static final int MATCH_BLUEPHOTO = 16;
	private static final int MATCH_YELLOWPHOTO = 17;
	private static final int MATCH_LIKEMUSIC = 18;
	private static final int MATCH_ALLMUSIC = 19;

	static {
		// Uri匹配
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_IMG, MATCH_IMG);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_AUDIO, MATCH_AUDIO);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_VIDEO, MATCH_VIDEO);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_DOC, MATCH_DOC);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_REDPHOTO, MATCH_REDPHOTO);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_BLUEPHOTO, MATCH_BLUEPHOTO);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_YELLOWPHOTO, MATCH_YELLOWPHOTO);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_LIKEMUSIC, MATCH_LIKEMUSIC);
		sUriMatcher.addURI(AUTHORITY, DBHelper.TABLE_ALLMUSIC, MATCH_ALLMUSIC);
	}

	private static DBHelper sCurDBHelper;

	public static void setCurDBInstance(DBHelper dbHelper) {
		sCurDBHelper = dbHelper;
//		 if(sCurDBHelper == null){
//		 throw new
//		 RuntimeException("MyContentProvider::sCurDBHelper is null");
//		 }
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
					String[] selectionArgs, String sortOrder) {

		try {
			Cursor c = null;
			SQLiteDatabase db = sCurDBHelper.getReadableDatabase();
			String table;

			switch (sUriMatcher.match(uri)) {
			case MATCH_IMG:
				table = DBHelper.TABLE_IMG;
				break;
			case MATCH_AUDIO:
				table = DBHelper.TABLE_AUDIO;
				break;
			case MATCH_VIDEO:
				table = DBHelper.TABLE_VIDEO;
				break;
			case MATCH_DOC:
				table = DBHelper.TABLE_DOC;
			case MATCH_REDPHOTO:
				table=DBHelper.TABLE_REDPHOTO;
				break;
			case MATCH_BLUEPHOTO:
				table=DBHelper.TABLE_BLUEPHOTO;
				break;
			case MATCH_YELLOWPHOTO:
				table=DBHelper.TABLE_YELLOWPHOTO;
				break;
			case MATCH_LIKEMUSIC:
				table=DBHelper.TABLE_LIKEMUSIC;
				break;
			case MATCH_ALLMUSIC:
				table=DBHelper.TABLE_ALLMUSIC;
				break;
			default:
				return null;
			}

			c = db.query(table, projection, selection, selectionArgs, null,
							null, null);

			return c;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		try {
			SQLiteDatabase db = sCurDBHelper.getWritableDatabase();
			String table;
			Uri contentUri;

			switch (sUriMatcher.match(uri)) {
			case MATCH_IMG:
				table = DBHelper.TABLE_IMG;
				contentUri = IMG_CONTENT_URI;
				break;
			case MATCH_AUDIO:
				table = DBHelper.TABLE_AUDIO;
				contentUri = AUDIO_CONTENT_URI;
				break;
			case MATCH_VIDEO:
				table = DBHelper.TABLE_VIDEO;
				contentUri = VIDEO_CONTENT_URI;
				break;
			case MATCH_DOC:
				table = DBHelper.TABLE_DOC;
				contentUri = DOC_CONTENT_URI;
				break;
			case MATCH_REDPHOTO:
				table = DBHelper.TABLE_REDPHOTO;
				contentUri = REDPHOTO_CONTENT_URI;
				break;
			case MATCH_BLUEPHOTO:
				table = DBHelper.TABLE_BLUEPHOTO;
				contentUri = BLUEPHOTO_CONTENT_URI;
				break;
			case MATCH_YELLOWPHOTO:
				table = DBHelper.TABLE_YELLOWPHOTO;
				contentUri = YELLOWPHOTO_CONTENT_URI;
				break;
			case MATCH_LIKEMUSIC:
				table = DBHelper.TABLE_LIKEMUSIC;
				contentUri = LIKEMUSIC_CONTENT_URI;
				break;
			case MATCH_ALLMUSIC:
				table = DBHelper.TABLE_ALLMUSIC;
				contentUri = ALLMUSIC_CONTENT_URI;
				break;
			default:
				return null;
			}

			long id = db.insert(table, null, values);
			if (id > -1) {
				Uri insertId = ContentUris.withAppendedId(contentUri, id);
				getContext().getContentResolver().notifyChange(insertId, null);
				return insertId;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		try {
			SQLiteDatabase db = sCurDBHelper.getWritableDatabase();
			String table;

			switch (sUriMatcher.match(uri)) {
			case MATCH_IMG:
				table = DBHelper.TABLE_IMG;
				break;
			case MATCH_AUDIO:
				table = DBHelper.TABLE_AUDIO;
				break;
			case MATCH_VIDEO:
				table = DBHelper.TABLE_VIDEO;
				break;
			case MATCH_DOC:
				table = DBHelper.TABLE_DOC;
				break;
			case MATCH_REDPHOTO:
				table = DBHelper.TABLE_REDPHOTO;
				break;
			case MATCH_BLUEPHOTO:
				table = DBHelper.TABLE_BLUEPHOTO;
				break;
			case MATCH_YELLOWPHOTO:
				table = DBHelper.TABLE_YELLOWPHOTO;
				break;
			case MATCH_LIKEMUSIC:
				table = DBHelper.TABLE_LIKEMUSIC;
				break;
			case MATCH_ALLMUSIC:
				table = DBHelper.TABLE_ALLMUSIC;
				break;
			default:
				return 0;
			}
			int count = db.delete(table, selection, selectionArgs);
			if (count > 0) {
				getContext().getContentResolver().notifyChange(uri, null);
			}
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					String[] selectionArgs) {
		Trace.Debug("##Mycontent upgrade");
		try {
			SQLiteDatabase db = sCurDBHelper.getWritableDatabase();
			String table;

			switch (sUriMatcher.match(uri)) {
			case MATCH_IMG:
				table = DBHelper.TABLE_IMG;
				break;
			case MATCH_AUDIO:
				table = DBHelper.TABLE_AUDIO;
				break;
			case MATCH_VIDEO:
				table = DBHelper.TABLE_VIDEO;
				break;
			case MATCH_DOC:
				table = DBHelper.TABLE_DOC;
				break;
			case MATCH_REDPHOTO:
				table = DBHelper.TABLE_REDPHOTO;
				break;
			case MATCH_BLUEPHOTO:
				table = DBHelper.TABLE_BLUEPHOTO;
				break;
			case MATCH_YELLOWPHOTO:
				table = DBHelper.TABLE_YELLOWPHOTO;
				break;
			case MATCH_ALLMUSIC:
				table = DBHelper.TABLE_ALLMUSIC;
				break;
			default:
				return 0;
			}
			int count = db.update(table, values, selection, selectionArgs);
			if (count == 0) {
				insert(uri, values);
			} else {
				getContext().getContentResolver().notifyChange(uri, null);
			}
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

}
