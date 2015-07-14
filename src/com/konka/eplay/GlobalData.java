package com.konka.eplay;

import java.util.ArrayList;

import iapp.eric.utils.base.Trace;
import iapp.eric.utils.metadata.Lyric;
import io.vov.vitamio.Vitamio;
import android.app.Application;

import com.konka.eplay.Constant.TAB_TYPE;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.movie.MovieActivity;
import com.konka.eplay.modules.music.MusicActivity;
import com.konka.eplay.modules.music.SearchResult;
import com.konka.eplay.modules.photo.PhotoActivity;

/**
 *
 * Created on: 2013-4-8
 *
 * @brief 应用内部模块之间可以相互访问的变量数据
 *
 *        有的时候我们在应用程序的任何一个地方都需要访问一个全局变量，也就是在任何一个Activity中
 *        都可以访问的变量。它不会因为Activity的生命周期结束而消失。要实现应用程序级的变量，我们可以 通过Application这个类来实现。
 *        配置方式：需要在AndroidManifest.xml中的application属性中添加android:name=
 *        "iapp.eric.template.project.GlobalData" 调用方式：GlobalData data =
 *        ((GlobalData)getApplicationContext()); String test =
 *        data.getValueString(KEY_NAME_TEST);
 * @author Eric Fung
 * @date Latest modified on: 2013-4-8
 * @version V1.0.00
 *
 */
public class GlobalData extends Application {

	// 装载各个子fragment的handler，用于消息分发
	//private Handler[] mSubHandlers = new Handler[Constant.TAB_TYPE.values().length];

	// 当前显示的子Fragment类型
	private TAB_TYPE mCurFragType;

	// 共用TAG
	private static final String TAG = "EPlay";

	public ArrayList<SearchResult> mSearchResults;
	public String mLastKeywords;
	public String mLastSongRealName;
	public ArrayList<Lyric> lyricList;
	public boolean isSortTypeChange = false;

	public void onCreate() {
		super.onCreate();
		//捕捉log，保存在程序目录中，以便查看     add by mcsheng
		CrashHandler.create(getApplicationContext());
		
		Utils.initExtList(this);
		Operation.init(this);
		Configuration.init(getApplicationContext());
		// 初始化TAG
		Trace.setTag(TAG);
		Trace.setFilter(Trace.TRACE_FATAL | Trace.TRACE_WARNING
						| Trace.TRACE_INFO | Trace.TRACE_DEBUG);

		mCurFragType = TAB_TYPE.TAB_ALL;

		if( !Configuration.IS_HWDECODER ){
			//软解播放需要解so
			new Thread(){
				public void run(){
					if( !Vitamio.isInitialized(getApplicationContext())){
						Vitamio.initialize(getApplicationContext(),
								getResources().getIdentifier("libarm", "raw", getPackageName()));
					}
				}
			}.start();
		}
	}

	/**
	 * 获得当前选中的fragment table类型
	 *
	 * @return
	 */
	public TAB_TYPE getCurSeletedFragType() {
		return mCurFragType;
	}

	/**
	 * 通知切换子Fragment
	 */
	public void notifySwitchFragment(TAB_TYPE eFragType) {
		if (mCurFragType == eFragType)
			return;
		mCurFragType = eFragType;
	}

	public void resetFileList() {
		// 数据源切换时，清空音视频、图片列表，重新拉取
		PhotoActivity.getFileList().clear();
		MusicActivity.getFileList().clear();
		MusicActivity.getAllMusicList().clear();
		MovieActivity.getFileList().clear();
	}

}
