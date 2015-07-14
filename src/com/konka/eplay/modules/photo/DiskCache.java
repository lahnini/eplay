package com.konka.eplay.modules.photo;

import iapp.eric.utils.base.Trace;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.model.LocalDiskInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

/**
 * 基于Least Recently Used（最近最少使用）算法处理的文件缓存<br>
 * <b>创建时间<b> 2015-5-20
 * 
 * @version 1.0
 * @author 取自开源代码
 */
public final class DiskCache {

    private static long maxSize;
    // constant
    private static final String CACHE_FILE_PATH_PREFIX = "kk.com.konka.eplay/picture";
    private static final int MAX_REMOVALS = 4;
    private static final int INITIAL_CAPACITY = 32;
    private static final float LOAD_FACTOR = 0.75f;
    
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    
    private CompressFormat mCompressFormat = CompressFormat.PNG;

    //缓存图片文件数量
    private int mCacheItemCount = 0;
    //缓存图片文件全部所占磁盘容量大小
    private int mCacheByteSize = 0;
    //缓存的图片文件数量限制
    private final int mMaxCacheItemCount = 8192; // 8192 item default
    private int mCompressQuality = 70;

    private final Map<String, String> mLinkedHashMap = Collections
            .synchronizedMap(new LinkedHashMap<String, String>(
                    INITIAL_CAPACITY, LOAD_FACTOR, true));

    public Context mContext;
    
    public DiskCache(Context context, long maxByteSize) {
        maxSize = maxByteSize;
        mContext = context;
    }

    /**
     * 将bitmap写入文件缓存，然后再put
     * 
     * @param filePath 缩略图所对应真图的绝对路径
     * @param data  缩略图对应的Bitmap
     */
    public void putImage(String filePath, Bitmap data) {
        synchronized (mLinkedHashMap) {
            if (mLinkedHashMap.get(filePath) == null) {
                try {
                    final String toSaveFilePath = createFilePath(filePath);
                    if (writeBitmapToFile(data, toSaveFilePath)) { // 如果成功将图片写入文件
                        put(filePath, toSaveFilePath);
                        Trace.Debug("put - Added cache file, " + toSaveFilePath);
                        flushCache();
                    }
                } catch (final FileNotFoundException e) {
                	Trace.Debug("Error in put: " + e.getMessage());
                } catch (final IOException e) {
                    Trace.Debug("Error in put: " + e.getMessage());
                }
            }
        }
    }

    private void put(String key, String file) {
        mLinkedHashMap.put(key, file);
        mCacheItemCount = mLinkedHashMap.size();
        mCacheByteSize += new File(file).length();
    }

    /**
     * 刷新缓存，当前Cache占用空间超过了最大空间，从最少使用的entry开始删除，直到占用空间小于标准
     */
    private void flushCache() {
        Entry<String, String> eldestEntry;
        File eldestFile;
        long eldestFileSize;
        int count = 0;
        while (count < MAX_REMOVALS
                && (mCacheItemCount > mMaxCacheItemCount || mCacheByteSize > maxSize)) {
            eldestEntry = mLinkedHashMap.entrySet().iterator().next();
            eldestFile = new File(eldestEntry.getValue());
            eldestFileSize = eldestFile.length();
            mLinkedHashMap.remove(eldestEntry.getKey());
            eldestFile.delete();
            mCacheItemCount = mLinkedHashMap.size();
            mCacheByteSize -= eldestFileSize;
            count++;
            Trace.Debug("flushCache - Removed cache file, " + eldestFile
                    + ", " + eldestFileSize);
        }
    }

    /**
     * 从缓存读取bitmap
     * 
     * @param filePath
     * 		
     * @return The bitmap or null if not found
     */
    public Bitmap getImage(String filePath) {
        synchronized (mLinkedHashMap) {
            final String file = mLinkedHashMap.get(filePath);
            if (file != null) {
                Trace.Debug("Disk cache hit");
                return BitmapFactory.decodeFile(file);
            } else {
            	//这里的执行是Map中不存在时，在实际的路径中确认是否存在
                final String existingFile = createFilePath(filePath);
                if (new File(existingFile).exists()) {
                    put(filePath, existingFile);
                    Trace.Debug("Disk cache hit (existing file)");
                    Bitmap bitmap = null;
                    try {
                    	bitmap = BitmapFactory.decodeFile(existingFile);
                    } catch(OutOfMemoryError e) {
        				//发生内存溢出，让系统gc一下
        				System.gc();
        				System.runFinalization();
        				bitmap = BitmapUtils.bitmapFromResource(mContext.getResources(),
        							R.drawable.photo_open_failed,321, 241);
                    }
                    
                    return bitmap;                    
                }
            }
            return null;
        }
    }

    
    /**
     * 检测文件缓存中是否存在特定的缩略图文件
     * 
     * @param filePath
     *            真图的绝对路径
     * @return 如果包含返回true，不包含返回false
     */
    public boolean containsFile(String filePath) {
        if (mLinkedHashMap.containsKey(filePath)) {
            return true;
        }
        // 检测key是否对应一个实际的文件
        final String existingFile = createFilePath(filePath);
        if (new File(existingFile).exists()) {
            // 如果找到key对应的实际文件，则加入map
            put(filePath, existingFile);
            return true;
        }
        return false;
    }

    /**
     * 清除全部文件图片缓存
     */
    public void clearCache(Context context) {
    	ArrayList<LocalDiskInfo> list = Utils.getExternalStorage(context);
    	for(LocalDiskInfo info : list) {
    		File file = new File(info.getPath() + File.separator + CACHE_FILE_PATH_PREFIX);
    		deleteFile(file);
    	}
    }
    
    private void deleteFile(File file){  
        if(file.isFile()){//表示该文件不是文件夹  
            file.delete();  
        }else{  
            //首先得到当前的路径  
            String[] childFilePaths = file.list();  
            for(String childFilePath : childFilePaths){  
                File childFile=new File(file.getAbsolutePath()+ File.separator + childFilePath);  
                deleteFile(childFile);  
            }  
            file.delete();  
        }  
    }

    /**
     * 返回缓存文件的绝对路径(以真图的路径来构造一个保存缓存文件的路径）
     * 
     * @param filePath
     *            真图文件的绝对路径
     * @return
     */
    public static String createFilePath(String filePath) {

    	int index = filePath.lastIndexOf(File.separator);
    	
    	String fileName = filePath.substring(index + 1, filePath.length());
    
    	String cachePathDir = Utils.getRootPath(filePath) + 
    			File.separator + CACHE_FILE_PATH_PREFIX;
    	//当目录不存在时创建
    	if (!new File(cachePathDir).exists()) {
			new File(cachePathDir).mkdirs();
		}
    	
    	String cacheFilePath = cachePathDir + File.separator + Utils.Md5(fileName);
    	
    	Trace.Debug("####createFilePath path is " + cacheFilePath);
    	
    	return cacheFilePath;
    }

    /**
     * 设置压缩格式与压缩质量
     * 
     * @param compressFormat
     * @param quality
     */
    public void setCompressParams(CompressFormat compressFormat,
            int quality) {
        mCompressFormat = compressFormat;
        mCompressQuality = quality;
    }

    /**
     * 图片写入文件
     */
    private boolean writeBitmapToFile(Bitmap bitmap, String file)
            throws IOException, FileNotFoundException {
        if (bitmap == null)
            return false;
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(
                    new FileOutputStream(file), IO_BUFFER_SIZE);
            return bitmap.compress(mCompressFormat, mCompressQuality,
                    out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

}
