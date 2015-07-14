package com.konka.eplay.modules.photo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 经过Least Recently Used（最近最少使用）算法处理的LinkedHashMap<br>
 * <b>创建时间<b> 2014-11-21
 * 
 * @version 1.0
 * @author 取自开源代码
 */
public class MemoryLruCache<K, V> {
	// LinkedHashMap与 HashMap 的不同之处在于维护着一个运行于所有条目的双向链表。
	// 每次put的value都是放在链表的头部
	private final LinkedHashMap<K, V> map;

	// 当前缓存区已使用大小
	private int size;
	private int maxSize;

	private int putCount;
	private int createCount;
	private int evictionCount;
	private int hitCount;
	private int missCount;

	/**
	 * @param maxSize
	 *            打开的缓存大小
	 * 
	 */
	public MemoryLruCache(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize <= 0");
		}
		this.maxSize = maxSize;
		this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
	}

	/**
	 * 设置缓存区最大值
	 * 
	 * @param maxSize
	 *            设置的最大值
	 */
	public void resize(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize <= 0");
		}
		synchronized (this) {
			this.maxSize = maxSize;
		}
		trimToSize(maxSize);
	}

	/**
	 * 根据key返回相应的value
	 * 
	 * @param key
	 *            键值
	 * @return 如果存在或可以Create则返回相应的value，否则返回null。如果一个value被返回，这个value将被移动到list首部
	 */
	public final V get(K key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

		V mapValue;
		synchronized (this) {
			mapValue = map.get(key);
			if (mapValue != null) {
				hitCount++;
				return mapValue;
			}
			missCount++;
		}

		/*
		 * 尝试创建一个value；这可能会花费很多时间，和当create()返回的时候map也有可能变得不一样了。
		 * 当create()方法运行的时候，如果发现有冲突的值加入到map中，将保留加入的值，释放创建的值
		 */
		V createdValue = create(key);
		if (createdValue == null) {
			return null;
		}

		synchronized (this) {
			createCount++;
			mapValue = map.put(key, createdValue);

			if (mapValue != null) {
				// 如果为null表示产生了线程冲突，重新执行最后一次put
				map.put(key, mapValue);
			} else {
				size += safeSizeOf(key, createdValue);
			}
		}

		if (mapValue != null) {
			entryRemoved(false, key, createdValue, mapValue);
			return mapValue;
		} else {
			trimToSize(maxSize);
			return createdValue;
		}
	}

	/**
	 * 放入值到map中，value会被保存在队列头部
	 * 
	 * @param key
	 *            键值
	 * @param value
	 *            要放入map中的值
	 * @return 先前映射到key的value值，若没有则返回null
	 */
	public final V put(K key, V value) {
		if (key == null || value == null) {
			throw new NullPointerException("key == null || value == null");
		}

		V previous;
		synchronized (this) {
			putCount++;
			size += safeSizeOf(key, value);
			previous = map.put(key, value);
			if (previous != null) {
				size -= safeSizeOf(key, previous);
			}
		}
		if (previous != null) {
			entryRemoved(false, key, previous, value);
		}
		trimToSize(maxSize);
		return previous;
	}

	// 修正map的大小
	private void trimToSize(int maxSize) {
		while (true) {
			K key;
			V value;
			synchronized (this) {
				if (size < 0 || (map.isEmpty() && size != 0)) {
					throw new IllegalStateException(
									getClass().getName()
													+ ".sizeOf() is reporting inconsistent results!");
				}

				if (size <= maxSize) {
					break;
				}

				Map.Entry<K, V> toEvict = null;
				for (Map.Entry<K, V> entry : map.entrySet()) {
					toEvict = entry;
				}

				if (toEvict == null) {
					break;
				}

				key = toEvict.getKey();
				value = toEvict.getValue();
				map.remove(key);
				size -= safeSizeOf(key, value);
				evictionCount++;
			}

			entryRemoved(true, key, value, null);
		}
	}

	/**
	 * 如果key对应的value存在，则移除它
	 * 
	 * @param key
	 *            键值
	 * @return 先前key对应的值
	 */
	public final V remove(K key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}
		V previous;
		synchronized (this) {
			previous = map.remove(key);
			if (previous != null) {
				size -= safeSizeOf(key, previous);
			}
		}
		if (previous != null) {
			entryRemoved(false, key, previous, null);
		}
		return previous;
	}

	/**
	 * 用于重载，用参数来规范对缓存中的值的操作行为
	 * 
	 * @param evicted
	 *            为true时将删除，为false将由put方法或remove方法确定
	 * @param key
	 *            键值
	 * @param oldValue
	 *            先前的旧值
	 * @param newValue
	 *            新的值
	 */
	protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
	}

	/**
	 * 用于重载，创建一个新的值
	 * 
	 * @param key
	 *            键值
	 */
	protected V create(K key) {
		return null;
	}

	/**
	 * 用于重载，计算缓存中每个条目的大小（对应返回的值进行了是否负数判断）
	 * 
	 * @param key
	 *            键值
	 * @param value
	 *            对应的值
	 */
	private int safeSizeOf(K key, V value) {
		int result = sizeOf(key, value);
		if (result < 0) {
			throw new IllegalStateException("Negative size: " + key + "="
							+ value);
		}
		return result;
	}

	/**
	 * 用于重载，计算缓存中每个条目的大小
	 * 
	 * @param key
	 *            键值
	 * @param value
	 *            对应的值
	 */
	protected int sizeOf(K key, V value) {
		return 1;
	}

	/**
	 * 清除缓存，通过调用entryRemoved方法来删除每个条目
	 */
	public final void evictAll() {
		trimToSize(-1); // -1 will evict 0-sized elements
	}

	/**
	 * 返回缓存的目前大小容量
	 */
	public synchronized final int size() {
		return size;
	}

	/**
	 * 返回缓存的最大容量
	 */
	public synchronized final int maxSize() {
		return maxSize;
	}

	/**
	 * 返回get查找到的次数
	 */
	public synchronized final int hitCount() {
		return hitCount;
	}

	/**
	 * 返回没有get查找到的次数
	 */
	public synchronized final int missCount() {
		return missCount;
	}

	/**
	 * 返回create创建的次数
	 */
	public synchronized final int createCount() {
		return createCount;
	}

	/**
	 * 返回put被调用的次数
	 */
	public synchronized final int putCount() {
		return putCount;
	}

	/**
	 * 返回map中的值被抛弃的次数
	 */
	public synchronized final int evictionCount() {
		return evictionCount;
	}

	/**
	 * 返回一个缓存的副本
	 */
	public synchronized final Map<K, V> snapshot() {
		return new LinkedHashMap<K, V>(map);
	}

	@Override
	public synchronized final String toString() {
		int accesses = hitCount + missCount;
		int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
		return String.format(
						"LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]",
						maxSize, hitCount, missCount, hitPercent);
	}
}
