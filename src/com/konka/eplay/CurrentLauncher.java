package com.konka.eplay;

import iapp.eric.utils.base.Trace;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

public class CurrentLauncher {

	Context context = null;
	public CurrentLauncher(Context context) {
		this.context = context;
	}
	
	public class LauncherInfoData
	{
		private String name;
		private String packageName;
		private int versionCode;
		private String verseionName;
		
		public void SetName(String name) {
			this.name = name;
		}
		
		public void SetPackageName(String packageName) {
			this.packageName = packageName;
		}
		
		public void SetVersionCode(int versionCode) {
			this.versionCode = versionCode;
		}
		
		public String GetName() {
			return name;
		}
		
		public String GetPackageName() {
			return packageName;
		}
		
		public int GetVersionCode() {
			return versionCode;
		}

		public String getVerseionName() {
			return verseionName;
		}

		public void setVerseionName(String verseionName) {
			this.verseionName = verseionName;
		}
		
	}
	
	public int getLauncherNum() 
	{
		PackageManager pm = context.getPackageManager();
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> ra = pm.queryIntentActivities(it, 0);
		//获取已系统已安装的Launcher信息列表
		return ra.size();	
	}

	public LauncherInfoData getCurrentLauncher() 
	{
		List<LauncherInfoData> list = new ArrayList<LauncherInfoData>();

		PackageManager pm = context.getPackageManager();
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> ra = pm.queryIntentActivities(it, 0);
		//获取已系统已安装的Launcher信息列表
		int ra_size = ra.size();
		for (int i = 0; i < ra_size; i++) 
		{
			LauncherInfoData LinfoData = new LauncherInfoData();

			ActivityInfo ai = ra.get(i).activityInfo;
			String PackageName = ai.packageName;

			PackageInfo pakinfo = null;

			try {
				pakinfo = pm.getPackageInfo(PackageName, PackageManager.GET_ACTIVITIES);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			LinfoData.SetName(pakinfo.applicationInfo.loadLabel(pm).toString());
			LinfoData.SetPackageName(PackageName);
			LinfoData.SetVersionCode(pakinfo.versionCode);
			LinfoData.setVerseionName(pakinfo.versionName);
	//		System.out.println(pakinfo.applicationInfo.loadLabel(pm).toString());
	//		System.out.println(PackageName);
	//		System.out.println("------------------------------------");
			
			list.add(LinfoData);
		}
		
		//获取当前正在运行的进程信息，并比较进程名与包名从而得到当前正在使用的Launcher信息
		if (list.size() > 0) 
		{
			int list_size = list.size();
			List<IntentFilter> intentList = new ArrayList<IntentFilter>();  
			List<ComponentName> cnList = new ArrayList<ComponentName>();
			
			pm.getPreferredActivities(intentList, cnList, null);
			
			IntentFilter dhIF;
			if(cnList != null && cnList.size() > 0)
			{
				int cnList_size = cnList.size();
				for(int i = 0; i < cnList_size; i++) {  
					dhIF = intentList.get(i);  

					if(dhIF.hasAction(Intent.ACTION_MAIN) &&  
							dhIF.hasCategory(Intent.CATEGORY_HOME) &&  
							dhIF.hasCategory(Intent.CATEGORY_DEFAULT)) 
					{  
							Trace.Info("-----------" + cnList.get(i).getPackageName());
							
							for (int j = 0; j < list_size; j++) 
							{
								if (list.get(j).GetPackageName().equals(cnList.get(i).getPackageName()))
								{
									return list.get(j);
								}
							}
					}  
				}  
			}
			
			ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> appProList = mActivityManager.getRunningAppProcesses();

			for (int j = 0; j < list_size; j++) 
			{
				for (ActivityManager.RunningAppProcessInfo running : appProList) 
				{
					Trace.Info("packagename-->>" + running.processName);
					if (list.get(j).GetPackageName().equals(running.processName)) 
					{
						return list.get(j);
					}
				}
			}
		}
		return null;
	}
}
