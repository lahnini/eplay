package com.konka.eplay.modules.photo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

/**
 * 使用lru算法的Bitmap内存缓存池<br>
 * <b>创建时间</b> 2014-11-21
 * 
 * @version 1.0
 * @author mcsheng
 */
public final class BitmapMemoryCache {

	private MemoryLruCache<String, Bitmap> cache;
	private final int MIN_SDK = 12;

	public BitmapMemoryCache() {
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		init(maxMemory / 8);
	}

	/**
	 * @param maxSize
	 *            使用内存缓存的内存大小，单位：kb
	 */
	public BitmapMemoryCache(int maxSize) {
		init(maxSize);
	}

	/**
	 * 初始化操作
	 * 
	 * @param maxSize
	 *            使用内存缓存的内存大小，单位：kb
	 */
	@SuppressLint("NewApi")
	private void init(int maxSize) {
		cache = new MemoryLruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				super.sizeOf(key, value);

				if (android.os.Build.VERSION.SDK_INT >= MIN_SDK) {
					return value.getByteCount() / 1024;
				} else {
					return value.getRowBytes() * value.getHeight() / 1024;
				}
			}
		};
	}

	/**
	 * 将特定键值的Bitmap放入缓存
	 * 
	 * @param key
	 *            缓存中的键值
	 * @param bitmap
	 *            放入缓存的Bitmap
	 */
	public void put(String key, Bitmap bitmap) {
		if (this.get(key) == null) {
			cache.put(key, bitmap);
		}
	}

	/**
	 * 将特定键值的Bitmap从缓存中取出
	 * 
	 * @param key
	 *            对应于Bitmap的键值
	 */
	public Bitmap get(String key) {
		return cache.get(key);
	}
	
	public int size() {
		return cache.size();
	}
	
	public int maxSize() {
		return cache.maxSize();
	}
	
	/**
	 * 清空缓存
	 */
	public void clearAll() {
		cache.evictAll();
	}

}
