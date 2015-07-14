/**
* @Title: DataHolder.java
* @Package com.konka.eplay.modules.music
* @Description: TODO(用一句话描述该文件做什么)
* @author A18ccms A18ccms_gmail_com
* @date 2015年6月15日 下午3:02:32
* @version
*/
package com.konka.eplay.modules.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.konka.eplay.modules.CommonFileInfo;

/**
 * @ClassName: DataHolder
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年6月15日 下午3:02:32
 * @version
 *
 */
public class DataHolder {
	public  List<CommonFileInfo> allMusicList = new ArrayList<CommonFileInfo>();
	public  List<CommonFileInfo> folderList = new ArrayList<CommonFileInfo>();

	public  long tagTime;
	public  Map<String, List<CommonFileInfo>> singerCollections= new HashMap<String, List<CommonFileInfo>>();
	public  Map<String, List<CommonFileInfo>> albumCollections= new HashMap<String, List<CommonFileInfo>>();
	public  List<CommonFileInfo> songlistBySinger = new ArrayList<CommonFileInfo>();
	public  List<CommonFileInfo> songlistByAlbum = new ArrayList<CommonFileInfo>();
	public  List<CommonFileInfo> songlistByCollect = new ArrayList<CommonFileInfo>();

}
