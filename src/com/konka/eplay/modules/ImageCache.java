package com.konka.eplay.modules;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;

public class ImageCache {
	private static ImageCache instance = new ImageCache();

	public static ImageCache getInstance() {
		return instance;
	}

	// private static final int SOFT_CACHE_CAPACITY = 40;

	private final static LinkedHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache;

	static {
		sSoftBitmapCache = new LinkedHashMap<String, SoftReference<Bitmap>>();
		// sSoftBitmapCache = new LinkedHashMap<String, SoftReference<Bitmap>>
		// (SOFT_CACHE_CAPACITY, 0.75f, true){
		// private static final long serialVersionUID = 6709957555017438325L;
		//
		// @Override
		// public SoftReference<Bitmap> put(String key, SoftReference<Bitmap>
		// value) {
		// return super.put(key, value);
		// }
		//
		// @Override
		// protected boolean removeEldestEntry(
		// LinkedHashMap.Entry<String, SoftReference<Bitmap>> eldest) {
		// Trace.Debug("###remove cache. " + eldest.getKey());
		// return size() > SOFT_CACHE_CAPACITY ? true : false ;
		// }
		// };
	}

	public boolean putBitmap(String key, Bitmap bitmap) {
		if (bitmap != null) {
			if (sSoftBitmapCache.containsKey(key)
							&& sSoftBitmapCache.get(key).get() != null) {
				return true;
			}
			// Trace.Debug("###put bitmap. " + key + "||" +
			// Utils.formatSize(bitmap.getByteCount()));
			sSoftBitmapCache.put(key, new SoftReference<Bitmap>(bitmap));
			return true;
		}
		return false;
	}

	public Bitmap getBitmap(String key) {
		// long total = 0;
		// SoftReference<Bitmap> ref;
		// for(Iterator<String> iter = sSoftBitmapCache.keySet().iterator();
		// iter.hasNext();){
		// ref = sSoftBitmapCache.get(iter.next());
		// if(ref != null){
		// if(ref.get() != null){
		// total += ref.get().getByteCount();
		// }
		// }
		// }
		// Trace.Debug("###sSoftBitmapCache.size=" + Utils.formatSize(total));

		SoftReference<Bitmap> bitmapRef = sSoftBitmapCache.get(key);
		if (null != bitmapRef) {
			Bitmap bitmap = bitmapRef.get();
			if (bitmap != null /* && !bitmap.isRecycled() */) {
				// Trace.Debug("###get bitmap from cache." + key);
				return bitmap;
			} else {
				// if(bitmap == null)
				// Trace.Debug("###bitmap has recycled & moved from cache." +
				// key);
				sSoftBitmapCache.remove(key);
			}
		}
		return null;
	}

	/**
	 * 回收软引用bitmap
	 */
	public static void gcSorftReference() {
		String key;
		SoftReference<Bitmap> bmpRef;
		Bitmap bmp;
		for (Iterator<String> it = sSoftBitmapCache.keySet().iterator(); it
						.hasNext();) {
			key = it.next();
			bmpRef = sSoftBitmapCache.get(key);
			if (bmpRef != null) {
				if ((bmp = bmpRef.get()) != null) {
					bmp.recycle();
					bmp = null;
				}
			}
		}
		sSoftBitmapCache.clear();
	}
}
