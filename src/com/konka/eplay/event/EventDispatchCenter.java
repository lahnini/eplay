package com.konka.eplay.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import de.greenrobot.event.EventBus;

/**
 * 
 * @Created on 2015年5月5日
 * @brief 消息事件分发中心
 * @author LiLiang
 * @date Latest modified on: 2015年5月5日
 * @version V1.0.00 
 *
 */
public class EventDispatchCenter {

	private static EventDispatchCenter instance;

	private static final int MSG_POST_EVENT = 1;
	private static final int MSG_POST_STICKY_EVENT = 2;

	// 事件处理的核心
	private EventBus mEventBus;

	/**
	 * 获取静态实例
	 * 
	 * @return
	 */
	public static EventDispatchCenter getInstance() {
		if (instance == null) {
			instance = new EventDispatchCenter();
		}
		return instance;
	}

	private EventDispatchCenter() {
		mEventBus = EventBus.getDefault();
	}

	/**
	 * 订阅者注册接收Event事件,比如Activity::onResume()中register
	 * 
	 * @param o
	 */
	public void register(Object o) {
		mEventBus.register(o);
	}

	
	public void registerSticky(Object o) { 
		 mEventBus.registerSticky(o); 
	}
	 
	public Object getStickyEvent(Class<?> eventType) {
		return mEventBus.getStickyEvent(eventType);
	}

	/**
	 * 订阅者解绑不再接收Event事件，比如Activity::onPause()中unregister
	 * 
	 * @param o
	 */
	public void unregister(Object o) {
		mEventBus.unregister(o);
	}

	/**
	 * 事件分发者分发Event，本工程中TXDeviceService为分发者角色
	 * 
	 * @param o
	 *            Object implements IEvent
	 */
	public void post(Object o) {
		Message msg = Message.obtain(mHandler);
		msg.obj = o;
		msg.what = MSG_POST_EVENT;
		mHandler.sendMessage(msg);
	}

	
	public void postSticky(Object o) { 
		Message msg = Message.obtain(mHandler);
		msg.obj = o; 
		msg.what = MSG_POST_STICKY_EVENT;
	    mHandler.sendMessage(msg); 
	}
	
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		
		public void handleMessage(Message msg) {
			if (msg.what == MSG_POST_EVENT) {
				Object event = msg.obj;
				if (event != null && event instanceof IEvent) {
					mEventBus.post(event);
				}
			} 
			else if (msg.what == MSG_POST_STICKY_EVENT) {
				Object event = msg.obj;
				if (event != null && event instanceof IEvent) {
					mEventBus.postSticky(event);
				}
			} 
		}
	};

}
