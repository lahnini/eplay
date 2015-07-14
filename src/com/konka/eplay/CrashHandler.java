package com.konka.eplay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

/**
 * 
 * Created on: 2015-6-16
 * 
 * @brief 捕捉程序崩溃异常，并将异常信息保存在程序目录下，以便查看找错
 * @author mcsheng
 * @date Latest modified on: 2015-6-16
 * @version V1.0.00
 *
 */
public class CrashHandler implements UncaughtExceptionHandler {
	
	private static final String TAG = "CrashHandler";
	
	//限制log数目总量为100，超过时将进行删除
	private static final int LIMIT_TOTAL = 100;
	
    private Context mContext;
    
    private static final String FILE_NAME = "log";
    
    private static final String FILE_CRASH_LOG_DIR = "error_logs_dir";
    
    private static CrashHandler sInstance = null;
    // 系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private UncaughtExceptionHandler mDefaultCrashHandler;
    
    private File mFile = null;

    private CrashHandler(Context context) {
        // 获取系统默认的异常处理器
        mDefaultCrashHandler = Thread
                .getDefaultUncaughtExceptionHandler();
        // 将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        // 获取Context，方便内部使用
        mContext = context.getApplicationContext();
        
        //获取保存路径 app_ + FILE_CRASH_LOG_DIR
        mFile = mContext.getDir(FILE_CRASH_LOG_DIR, Context.MODE_PRIVATE);
    }

    public synchronized static CrashHandler create(Context context) {
        if (sInstance == null) {
            sInstance = new CrashHandler(context);
        }
        return sInstance;
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
    	
    	Log.i(TAG,"####uncaughtException");
    	
		try {
		    // 保存异常信息
		    save(ex);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//信息保存后，就进行系统默认的异常处理
		    if (mDefaultCrashHandler != null) {
		    	mDefaultCrashHandler.uncaughtException(thread, ex);
		    }           
		}
    }

    private void save(Throwable ex) throws Exception {   
    	
    	File file = null;
    	
        if (mFile != null) {
        	//当存放log的目录中的文件数超过LIMIT_TOTAL时就清除里面的文件，以避免log文件过多
        	File[] files = mFile.listFiles();
        	if (files != null && files.length >= LIMIT_TOTAL) {
        		for (File tmpFile : files) {
        			tmpFile.delete();
        		}
        	}
        	String dataString = getDataTime("yyyy-MM-dd_HH:mm:ss");
        	String fileName = mContext.getPackageName() + "_" + FILE_NAME + "_" + dataString;
        	file = new File(mFile.getAbsolutePath() + File.separator + fileName);
        	file.createNewFile();
        }
        
        PrintWriter pw = new PrintWriter(new BufferedWriter(
                new FileWriter(file)));
        // 导出发生异常的时间
        pw.println(getDataTime("yyyy-MM-dd_HH:mm:ss"));
        // 导出信息
        dumpInfo(pw);

        pw.println();
        // 导出异常的调用栈信息
        ex.printStackTrace(pw);
        pw.close();
    }
    
    @SuppressLint("SimpleDateFormat")
	private String getDataTime(String format) {
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(new Date());
	}

    private void dumpInfo(PrintWriter pw)
            throws NameNotFoundException {
        // 应用的版本名称和版本号
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);
        pw.println();

        // android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        pw.println();

        // 制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);
        pw.println();

        // 型号
        pw.print("Model: ");
        pw.println(Build.MODEL);
        pw.println();

        // cpu架构
        pw.print("CPU ABI: ");
        pw.println(Build.CPU_ABI);
        pw.println();
    }
    
}