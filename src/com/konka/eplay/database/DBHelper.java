package com.konka.eplay.database;

import iapp.eric.utils.base.Trace;

import com.konka.eplay.Constant;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @Created on 2013-12-4
 * @brief 【移动设备全盘扫描数据缓存】数据库相关，不对外使用
 * @author LiLiang
 * @date Latest modified on: 2013-12-4
 * @version V1.0.00
 * 
 */
public class DBHelper extends SQLiteOpenHelper {

	public static final String DB_PATH = Constant.DB_DIR + "/database";
	public static final String DB_NAME = "eplay.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE_REDPHOTO="redphoto";
	public static final String TABLE_BLUEPHOTO="bluephoto";
	public static final String TABLE_YELLOWPHOTO="yellowphoto";
	public static final String TABLE_LIKEMUSIC="likemusic";
	public static final String TABLE_ALLMUSIC="allmusic";
	public static final String TABLE_IMG = "image";
	public static final String TABLE_AUDIO = "audio";
	public static final String TABLE_VIDEO = "video";
	public static final String TABLE_DOC = "doc";

	public static final String ID = "_id";
	public static final String PATH = "_path"; // 相对根目录路径

	// 创建图片路径缓存的数据库命令
	public static final String CREATE_TABLE_IMG = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_IMG + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + PATH
					+ " TEXT NOT NULL);";

	// 音频sql_cmd
	public static final String CREATE_TABLE_AUDIO = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_AUDIO
					+ "("
					+ ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PATH
					+ " TEXT NOT NULL);";
	//添加所有音乐
	public static final String CREATE_TABLE_ALLMUSIC = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_ALLMUSIC
					+ "("
					+ ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PATH
					+ " TEXT NOT NULL);";
	
	//添加喜欢的音乐
	public static final String CREATE_TABLE_LIKEMUSIC = "CREATE TABLE IF NOT EXISTS "
					+ TABLE_LIKEMUSIC
					+ "("
					+ ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PATH
					+ " TEXT NOT NULL);";
	// 视频sql_cmd
	public static final String CREATE_TABLE_VIDEO = "CREATE TABLE IF NOT EXISTS "
					+ DBHelper.TABLE_VIDEO
					+ "("
					+ ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PATH
					+ " TEXT NOT NULL);";

	// 文档sql_cmd
	public static final String CREATE_TABLE_DOC = "CREATE TABLE IF NOT EXISTS "
					+ DBHelper.TABLE_DOC + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + PATH
					+ " TEXT NOT NULL);";
	//图片分类
	public static final String CREATE_TABLE_REDPHOTO = "CREATE TABLE IF NOT EXISTS "
					+ DBHelper.TABLE_REDPHOTO + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + PATH
					+ " TEXT NOT NULL);";
	public static final String CREATE_TABLE_BLUEPHOTO = "CREATE TABLE IF NOT EXISTS "
					+ DBHelper.TABLE_BLUEPHOTO + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + PATH
					+ " TEXT NOT NULL);";
	public static final String CREATE_TABLE_YELLOWPHOTO = "CREATE TABLE IF NOT EXISTS "
					+ DBHelper.TABLE_YELLOWPHOTO + "(" + ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + PATH
					+ " TEXT NOT NULL);";
	

	public DBHelper(Context context, String name) {
		super(context, name, null, DB_VERSION, null);
//		   dbHelper = new DBHelper(context, name );
//           db = dbHelper.getWritableDatabase();
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		 Trace.Debug("###onCreate()");
		db.execSQL(CREATE_TABLE_REDPHOTO);
		db.execSQL(CREATE_TABLE_BLUEPHOTO);
		db.execSQL(CREATE_TABLE_YELLOWPHOTO);
		db.execSQL(CREATE_TABLE_LIKEMUSIC);
		db.execSQL(CREATE_TABLE_ALLMUSIC);
		db.execSQL(CREATE_TABLE_IMG);
		db.execSQL(CREATE_TABLE_AUDIO);
		db.execSQL(CREATE_TABLE_VIDEO);
		db.execSQL(CREATE_TABLE_DOC);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		 Trace.Debug("###onUpgrade");
		db.execSQL("drop table if exists " + TABLE_IMG);
		db.execSQL("drop table if exists " + TABLE_AUDIO);
		db.execSQL("drop table if exists " + TABLE_VIDEO);
		db.execSQL("drop table if exists " + TABLE_DOC);
		db.execSQL("drop table if exists " + TABLE_REDPHOTO);
		db.execSQL("drop table if exists " + TABLE_BLUEPHOTO);
		db.execSQL("drop table if exists " + TABLE_YELLOWPHOTO);
		db.execSQL("drop table if exists " + TABLE_LIKEMUSIC);
		db.execSQL("drop table if exists " + TABLE_ALLMUSIC);
		onCreate(db);
	}

}
