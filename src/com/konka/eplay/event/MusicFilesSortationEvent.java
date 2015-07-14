package com.konka.eplay.event;

import com.konka.eplay.modules.music.DataHolder;

/**
 * @ClassName: SortationFinishEvent
 * @Description: 发送此事件表明排序完成
 * @author xuyunyu
 * @date 2015年6月11日 上午10:48:34
 *
 */
public class MusicFilesSortationEvent implements IEvent {

	public static final int REFRESH_MESSAGE=300;
	public static final int SORTATION_FINISH=301;

	public int eventType;
	public String message = "";
	public DataHolder dataHolder;

	public MusicFilesSortationEvent() {
	}

}
