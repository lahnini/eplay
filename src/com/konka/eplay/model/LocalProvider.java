package com.konka.eplay.model;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.sax.StartElementListener;
import android.util.Log;

import com.konka.eplay.Constant;
import com.konka.eplay.Constant.LIST_TYPE;
import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;
import com.konka.eplay.GlobalData;
import com.konka.eplay.Utils;
import com.konka.eplay.database.ContentManager;
import com.konka.eplay.event.EventCommResult;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventTimeout;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.movie.MovieActivity;
import com.konka.eplay.modules.music.MusicActivity;
import com.konka.eplay.modules.photo.PhotoActivity;

/**
 * 
 * @Created on
 * @brief 优化后台盘符扫描，listWithSpecificMediaType函数 1.15s后还未扫描出数据向UI发�?�一次消息；
 *        2.�?次扫出全部的视频、音频�?�图片文件； 3.处理扫描前后用户切换了Fragment的操作；
 *        4.单线程扫描工作，避免多个thread并发扫描造成ANR
 * @author LiLiang
 * @date Latest modified on: 2014-7-15
 * @version V1.0.00
 * 
 */
public class LocalProvider {
	private GlobalData mGlobalApp;

	// 扫描超过15s时发个消息，UI的ProgressBar更改提示文本
	private final int TIME_LIMIT = 15 * 1000;
	// 是否扫描�?
	private boolean isScanning = false;
	private Handler mHandler;
	//排序用的临时列表
	private List<CommonFileInfo> tmpPhotoList=new ArrayList<CommonFileInfo>();
	private List<CommonFileInfo> tmpMusicList=new ArrayList<CommonFileInfo>();
	private List<CommonFileInfo> tmpMusicAllList=new ArrayList<CommonFileInfo>();
	private List<CommonFileInfo> tmpMovieList=new ArrayList<CommonFileInfo>();
	private Runnable mTimeoutRunnable;
	// 扫描线程
	private static Thread mScanThread;
private static boolean stopScan=false;
	private static List<CommonFileInfo> musicAllList = null;

	public static boolean isScanSucess=true;
	private long timeStart;
	static {
		musicAllList = new ArrayList<CommonFileInfo>();
	}

	// 扫描时间超过TIME_LIMIT时发送MSG_TIMEOUT消息
	private static final int MSG_TIMEOUT = 101;
	// 扫描到所有音视频图片数据后，非当前Fragment对应的数据mHandler发�?�MSG_SET_VALUE
	private static final int MSG_SET_VALUE = 102;

	@SuppressLint("HandlerLeak")
	public LocalProvider(GlobalData app) {
		if (app == null) {
			throw new RuntimeException("param app is null");
		}
		mGlobalApp = app;
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == MSG_TIMEOUT) {
					if (msg.obj != null && msg.obj instanceof Runnable) {
						if (isScanning) {
							Runnable runnable = (Runnable) msg.obj;
							runnable.run();
						}
					}
				} else if (msg.what == MSG_SET_VALUE) {
					if (msg.obj == null
									|| !(msg.obj instanceof ListSpecificReturn)) {
						return;
					}
					ListSpecificReturn ret = (ListSpecificReturn) msg.obj;
					Trace.Debug("ret.starttime="+ret.startTime+"timestart="+timeStart);
					if(ret.startTime>=timeStart){
						Trace.Debug("copy");
						switch (ret.eMediaType) {
						case MMT_MOVIE:
							tmpMusicList=ret.musicList;
							tmpMusicAllList=ret.musicAllList;
							tmpPhotoList=ret.photoList;
							new Thread(new  Runnable() {
								public void run() {
									
							
							if (com.konka.eplay.Configuration.sortType==SortType.ST_BY_NAME) {
								Collections.sort(tmpMusicList, new FileComparator.sortByName());
								Collections.sort(tmpPhotoList, new FileComparator.sortByName());
								Collections.sort(tmpMusicAllList, new FileComparator.sortByName());
								
							}else {
								Collections.sort(tmpMusicList,new FileComparator.sortByModifyDate());
								Collections.sort(tmpMusicAllList,new FileComparator.sortByName());
								Collections.sort(tmpPhotoList,new FileComparator.sortByModifyDate());
							}
							MusicActivity.copyTo(tmpMusicList,
											tmpMusicAllList);
							PhotoActivity.copyTo(tmpPhotoList);
								}
							}).start();
							break;
						case MMT_MUSIC:
							tmpPhotoList=ret.photoList;
							tmpMovieList=ret.videoList;
							new Thread(new  Runnable() {
								public void run() {
									
							if (com.konka.eplay.Configuration.sortType==SortType.ST_BY_NAME) {
								Collections.sort(tmpPhotoList, new FileComparator.sortByName());
								Collections.sort(tmpMovieList, new FileComparator.sortByName());
							}else {
								Collections.sort(tmpPhotoList,new FileComparator.sortByModifyDate());
								Collections.sort(tmpMovieList,new FileComparator.sortByModifyDate());
							}
							MovieActivity.copyTo(tmpMovieList);
							PhotoActivity.copyTo(tmpPhotoList);
								}
							}).start();
							break;
						case MMT_PHOTO:
							tmpMusicList=ret.musicList;
							tmpMusicAllList=ret.musicAllList;
							tmpMovieList=ret.videoList;
							new Thread( new Runnable() {
								public void run() {
									
							if (com.konka.eplay.Configuration.sortType==SortType.ST_BY_NAME) {
								Collections.sort(tmpMusicList, new FileComparator.sortByName());
								Collections.sort(tmpMovieList, new FileComparator.sortByName());
								Collections.sort(tmpMusicAllList, new FileComparator.sortByName());
								
							}else {
								Collections.sort(tmpMusicList,new FileComparator.sortByModifyDate());
								Collections.sort(tmpMusicAllList,new FileComparator.sortByName());
								Collections.sort(tmpMovieList,new FileComparator.sortByModifyDate());
							}
							MovieActivity.copyTo(tmpMovieList);
							MusicActivity.copyTo(tmpMusicList,tmpMusicAllList);
							Trace.Debug("###ret.musicAllList##"+tmpMusicAllList.size());
								}
							}).start();
							break;
						default:
							break;
					}
				}
				}
			}
		};
		mTimeoutRunnable = new Runnable() {
			@Override
			public void run() {
				// Trace.Warning("###全盘扫描超出时间限制" + (TIME_LIMIT / 1000) +
				// "s!!!");
				EventTimeout event = new EventTimeout();
				event.type = Constant.MSG_LIST_SPECIFIC_MEDIATYPE_MORE;
				EventDispatchCenter.getInstance().post(event);
			}
		};

	}

	/**
	 * 按照文件树结构列出当前路径下的子文件
	 * 
	 * @param path
	 * @param eType
	 */
	public void list(String path, LIST_TYPE eType) {
		CommonResult result = new CommonResult();
		File parent = new File(path);
		if (parent.exists() && parent.isDirectory()) {
			File[] subFiles = parent.listFiles();
			List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
			if (subFiles != null && subFiles.length > 0) {
				for (int i = 0; i < subFiles.length; i++) {
					if (subFiles[i].isDirectory()
									&& subFiles[i].getName().startsWith(".")) {
						// 隐藏的文�?
						continue;
					}
					if (subFiles[i].getAbsolutePath()
									.contains(Constant.APP_DIR)) {
						continue;
					}
					switch (eType) {
					case ALL:
						list.add(new CommonFileInfo(subFiles[i]));
						break;
					case FILE_ONLY:
						if (subFiles[i].isFile()) {
							list.add(new CommonFileInfo(subFiles[i]));
						}
						break;
					case FOLDER_ONLY:
						if (subFiles[i].isDirectory()) {
							list.add(new CommonFileInfo(subFiles[i]));
						}
						break;
					default:
						break;
					}

				}
			}
			result.code = CommonResult.OK;
			Trace.Debug("resultcode="+CommonResult.OK);
			result.data = list;

		} else {
			result.data = new String("传入路径无效");
		}

		EventCommResult event = new EventCommResult();
		event.type = Constant.MSG_LIST;
		event.result = result;
		EventDispatchCenter.getInstance().post(event);

	}

	/**
	 * 递归列出指定类型的文件（视频、音频�?�文档等�?,非文件树结构 默认从数据库读取缓存数据，然后再进行扫描盘符，二次校�?
	 * 
	 * @param eMediaType
	 *            : 特别文件类型
	 * @param dirs
	 *            : 各个盘符的根目录集合
	 * @param listParent
	 *            : 已无�?
	 * @param readCached
	 *            是否从数据库中读取缓存文件，否则全盘递归扫描文件
	 * 
	 */
	public void listWithSpecificMediaType(final MultimediaType eMediaType,
					final List<String> roots, final boolean listParent,
					boolean readCached) {
		Log.i("onList3", "startr");
		Trace.Debug("###listWithSpecificMediaType()");
		try {
			if (roots == null || roots.size() == 0) {
				 Trace.Warning("###LocalProvider::listWithSpecificMediaType() param usbList error!");
				Log.i("onroot", "null");
				
				return;
			}

//			ListSpecificReturn ret1 = readCachedFiles(eMediaType, roots);
//			ListSpecificReturn retTmp=readCachedFiles(MultimediaType.MMT_ALLMUSIC, roots);
			// cachedList留作全盘扫描时比�?
//			final List<CommonFileInfo> cachedList = new ArrayList<CommonFileInfo>();
//			final List<CommonFileInfo> cachedListTmp = new ArrayList<CommonFileInfo>();
//			cachedListTmp.addAll(retTmp.getSpecificList(MultimediaType.MMT_ALLMUSIC));
//			cachedList.addAll(ret1.getSpecificList(eMediaType));

//			Trace.Debug("###readCached=" + readCached + "; ret1.isSucc="
//					+ ret1.isSucc);
//			final boolean isReadDBOk = readCached && ret1.isSucc;
//			if (isReadDBOk) {
//				// ========从数据库缓存读取数据成功,send message.=============//
//				Trace.Debug("###read data from db ok");
//				if (retTmp.musicAllList.size()>0) 
//				postFilesToUI(eMediaType,
//								ret1.getSpecificList(eMediaType),retTmp.musicAllList);
//				Trace.Debug("###retTmp.musicAllList="+retTmp.musicAllList.size());
//			}

			// =============从数据库缓存读取成功或失败，都需要全盘扫描，�?次扫完视频�?�音频�?�图�?==============//
			if (mScanThread != null && mScanThread.isAlive() /* isScanning */) {
			stopScan();
			}

			isScanning = true;
			Trace.Debug("###全盘扫描");

			mScanThread = new Thread() {
				public void run() {
						stopScan=false;
					long timestamp1 = System.currentTimeMillis();
					timeStart=timestamp1;
					try {
						// 延时1s后再�?启校验，避免UI也有操作导致ANR�?
						Thread.sleep(1000);	
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// ret2是实时扫描的�?新结果，视频、音频�?�图片数据全部一次扫�?
					ListSpecificReturn ret2 = getAllSpecificFiles(roots);
					Trace.Debug("###ret2.musicalllist"+ret2.musicAllList.size());
					isScanning = false;
					// 从数据库读取�?
//					List<CommonFileInfo> list1 = cachedList;
//					List<CommonFileInfo> list1tmp = cachedListTmp;
					// 实时扫描到的
					List<CommonFileInfo> list2 = ret2
									.getSpecificList(eMediaType);
					List<CommonFileInfo> list2tmp = ret2
									.getSpecificList(MultimediaType.MMT_ALLMUSIC);
					
					
				
					long timestamp2 = System.currentTimeMillis();
					Trace.Debug("###" + eMediaType.name() + "全盘扫描耗时"
							+ (timestamp2 - timestamp1) / 1000 + "s");
					if (!stopScan/*modified*/ /*|| !isReadDBOk*/) { // 校验胡数据有不同||没从数据库读取ok必须发数据到UI
						Trace.Debug("###发全盘扫描的实时数据");
						// 当前的特定媒体类型文件�?�过handler发�?�出去，其他的后台赋�?
						postFilesToUI(eMediaType, list2,ret2.musicAllList);
						Trace.Debug("###校验缓存数据与实时数据相�?");
						Trace.Debug("###ret2.musicalllist"+ret2.musicAllList.size());
					}
					ret2.startTime=timestamp1;
					ret2.eMediaType = eMediaType;
					Trace.Debug("###ret2.musicalllist"+ret2.musicAllList.size());
					if (!stopScan) {
						
					Message msg = Message.obtain();
				
					msg.what = MSG_SET_VALUE;
					msg.obj = ret2;
					mHandler.sendMessage(msg);
					}
			}
			};
			// 设置�?小优先级，避免IO阻塞，引起ANR
			mScanThread.setPriority(Thread.MIN_PRIORITY);
			mScanThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发�?�指定类型文件到对应的UI
	 * 
	 * @param eMediaType
	 * @param list
	 */
	private void postFilesToUI(MultimediaType eMediaType,
					List<CommonFileInfo> list,List<CommonFileInfo>list2) {
		Trace.Debug("@@postFilesToUI" + eMediaType + list.size());
		CommonResult result = new CommonResult();
		result.code = CommonResult.OK;
		result.time=timeStart;
		if (com.konka.eplay.Configuration.sortType==SortType.ST_BY_NAME) {
			Collections.sort(list, new FileComparator.sortByName());
			Collections.sort(list2, new FileComparator.sortByName());
		}else {
			
			Collections.sort(list,new FileComparator.sortByModifyDate());
			Collections.sort(list2,new FileComparator.sortByName());
		}
		result.data = list;
		result.data3 = list2;
		Trace.Debug("###musicAllList" + list2.size());
		result.data2 = eMediaType;
		EventCommResult event = new EventCommResult();
		event.type = Constant.MSG_LIST_SPECIFIC_MEDIATYPE;
		event.result = result;
		EventDispatchCenter.getInstance().post(event);
	}

	/**
	 * 读取缓存文件
	 * 
	 * @param eMediaType
	 *            指定文件类型
	 * @param roots
	 *            �?有挂载的存储设备路径
	 * @return ListSpecificReturn::isReadCached [true] 表示读取数据�?
	 *         ListSpecificReturn::isSucc [true or false] 从数据库读取成功或失�?
	 *         ListSpecificReturn::videoList eMediaType==MMT_MOVIE时有�?
	 *         ListSpecificReturn::musicList eMediaType==MMT_MUSIC时有�?
	 *         ListSpecificReturn::photoList eMediaType==MMT_PHOTO时有�?
	 */
	private ListSpecificReturn readCachedFiles(MultimediaType eMediaType,
					List<String> roots) {
		Trace.Debug("###readCachedFiles");
		ListSpecificReturn ret = new ListSpecificReturn();
		ret.isReadCached = true;
		ret.isSucc = true;
		int volumeSize = roots.size();
		List<CommonFileInfo> allList = ret.getSpecificList(eMediaType);
		
		if (allList == null) {
			ret.isSucc = false;
			return ret;
		}
		for (int i = 0; i < volumeSize; i++) {
			List<File> cachedList = ContentManager.readDataFromDB(mGlobalApp,
							roots.get(i), eMediaType);
			if (cachedList == null) {
				// 读取缓存失败
				ret.isSucc = false;
				Trace.Debug("###读取缓存失败");
			} else if (cachedList.size() > 0) {
				int count = cachedList.size();
				List<CommonFileInfo> tmpList = new ArrayList<CommonFileInfo>();

				for (int j = 0; j < count; j++) {
					tmpList.add(new CommonFileInfo(cachedList.get(j)));
				}

				if (tmpList.size() > 0) {
					allList.addAll(tmpList);
				}
			}

		} // cachedList.size()==0时，读取DB成功，但无对应类型数据，nothing to do.
		return ret;
	}

	/**
	 * @param roots
	 *            :�?有USB设备的根目录路径
	 * @return see ListSpecificReturn
	 */
	private ListSpecificReturn getAllSpecificFiles(List<String> roots) {
		int volumeSize = roots.size();
		ListSpecificReturn ret = new ListSpecificReturn();
		ret.isReadCached = false;
		ret.isSucc = false;

		if (isScanning) {
			Message msg = Message.obtain();
			msg.what = MSG_TIMEOUT;
			msg.obj = mTimeoutRunnable;
			mHandler.sendMessageDelayed(msg, TIME_LIMIT);
		}

		// �?有盘符的文件列表
		for (int i = 0; i < volumeSize; i++) {
			if (stopScan) {
				Trace.Debug("getAllspecialFiles-stopScan");
				break;
			}
			// 每个盘符的扫描结�?
			ListSpecificReturn subRet = getSpecificFilesRec(new File(
							roots.get(i)));

			// =============处理两层显示的图片列�?=======================//
			// 此时subRet.photoList无�?�，subRet.tmpPhotoList有�?��??
			// tmpPhotoList->photoList，并且剔除根目录，把根目录下符合的子文件添加进来
			if (subRet != null && subRet.tmpPhotoList.size() > 0) {
				Trace.Info("videosize" + subRet.tmpVideoList.size());
				Trace.Info("两层显示图片");
				subRet.photoList.clear();
				Trace.Info("videosize()" + subRet.tmpVideoList.size());
				int num = subRet.tmpPhotoList.size();
				String lostDirPath = roots.get(i) + "/" + Constant.LOST_DIR;
				String kkPath = roots.get(i) + "/" + Constant.APP_DIR;
				File file;
				String tmpPath;
				for (int j = 0; j < num; j++) {
					if (stopScan) {
						Trace.Debug("getAllspecialFiles music-stopScan");
						break;
					}
					file = subRet.tmpPhotoList.get(j);
					tmpPath = file.getAbsolutePath();
//					if (file.isDirectory() && file.getName().startsWith(".")) {
//						// 跳过隐藏的文�?
//						continue;
//					}
					if (tmpPath.equals(roots.get(i))) {
						// 根路径文件夹剔除，加载根目录下的图片子文�?

						List<CommonFileInfo> subs = getSpecificFiles(file,
										MultimediaType.MMT_PHOTO);
						if (subs != null && subs.size() > 0) {
							CommonFileInfo rootCommonFileInfo = new CommonFileInfo(
											new File(roots.get(i)));
							rootCommonFileInfo.setName(Utils
											.getWrapperPath(roots.get(i)));
							Trace.Debug("U盘名称="+rootCommonFileInfo.getName());
							subRet.photoList.add(rootCommonFileInfo);
							subs.clear();
						}
					} else if (tmpPath.equals(lostDirPath)
									|| tmpPath.equals(kkPath)) {
						continue;
					} else {
						subRet.photoList.add(new CommonFileInfo(file));
					}
				}
				Trace.Info("videosize3" + subRet.tmpVideoList.size());

				// 此时photoList有�?�，清空tmpPhotoList已无用，避免copyFrom时ret.tmpPhotoList被赋�?
				subRet.tmpPhotoList.clear();
				// 把每个盘符的结果赋给ret
				Trace.Info("" + subRet.tmpVideoList.size());
			}
			if (subRet != null && subRet.tmpVideoList.size() > 0) {
				Trace.Info("两层显示视频");
				subRet.videoList.clear();

				int num = subRet.tmpVideoList.size();
				String lostDirPath = roots.get(i) + "/" + Constant.LOST_DIR;
				String kkPath = roots.get(i) + "/" + Constant.APP_DIR;
				File file;
				String tmpPath;
				for (int j = 0; j < num; j++) {
					if (stopScan) {
						Trace.Debug("getAllspecialFiles movie-stopScan");
						break;
					}
					file = subRet.tmpVideoList.get(j);
					tmpPath = file.getAbsolutePath();
//					if (file.isDirectory() && file.getName().startsWith(".")) {
//						// 跳过隐藏的文�?
//						continue;
//					}
					if (tmpPath.equals(roots.get(i))) {
						// 根路径文件夹剔除，加载根目录下的图片子文�?

						List<CommonFileInfo> subs = getSpecificFiles(file,
										MultimediaType.MMT_MOVIE);
						if (subs != null && subs.size() > 0) {
							CommonFileInfo rootCommonFileInfo = new CommonFileInfo(
											new File(roots.get(i)));
							rootCommonFileInfo.setName(Utils.getWrapperPath(
											roots.get(i)).toString());
							subRet.videoList.add(rootCommonFileInfo);
							subs.clear();
						}
					} else if (tmpPath.equals(lostDirPath)
									|| tmpPath.equals(kkPath)) {
						continue;
					} else {
						subRet.videoList.add(new CommonFileInfo(file));
					}
				}

				// 此时photoList有�?�，清空tmpPhotoList已无用，避免copyFrom时ret.tmpPhotoList被赋�?
				subRet.tmpVideoList.clear();
			}
			// =============处理两层显示的图片列�?=======================//
			// 此时subRet.photoList无�?�，subRet.tmpPhotoList有�?��??
			// tmpPhotoList->photoList，并且剔除根目录，把根目录下符合的子文件添加进来
			if (subRet != null && subRet.tmpMusicList.size() > 0) {
				Trace.Info("两层显示音乐");
				subRet.musicList.clear();

				int num = subRet.tmpMusicList.size();
				String lostDirPath = roots.get(i) + "/" + Constant.LOST_DIR;
				String kkPath = roots.get(i) + "/" + Constant.APP_DIR;
				File file;
				String tmpPath;
				for (int j = 0; j < num; j++) {
					if (stopScan) {
						Trace.Debug("getAllSpecial music-stopScan");
						break;
					}
					file = subRet.tmpMusicList.get(j);
					tmpPath = file.getAbsolutePath();
//					if (file.isDirectory() && file.getName().startsWith(".")) {
//						// 跳过隐藏的文�?
//						continue;
//					}
					if (tmpPath.equals(roots.get(i))) {
						// 根路径文件夹剔除，加载根目录下的图片子文�?

						List<CommonFileInfo> subs = getSpecificFiles(file,
										MultimediaType.MMT_MUSIC);
						if (subs != null && subs.size() > 0) {
							CommonFileInfo rootCommonFileInfo = new CommonFileInfo(
											new File(roots.get(i)));
							rootCommonFileInfo.setName(Utils
											.getWrapperPath(roots.get(i)));
							Trace.Info("" + roots.get(i).toString());
							subRet.musicList.add(rootCommonFileInfo);
							subs.clear();
						}
					} else if (tmpPath.equals(lostDirPath)
									|| tmpPath.equals(kkPath)) {
						continue;
					} else {
						subRet.musicList.add(new CommonFileInfo(file));
					}
				}

				// 此时photoList有�?�，清空tmpPhotoList已无用，避免copyFrom时ret.tmpPhotoList被赋�?
				subRet.tmpMusicList.clear();
				// 把每个盘符的结果赋给ret
				//ret.copyFrom(subRet);
			}
			ret.copyFrom(subRet);
		}
		Trace.Debug("###SallList=" + ret.musicAllList.size());
		ret.isSucc = true;
		Trace.Info("music" + ret.musicList.size() + "photo"
						+ ret.photoList.size() + "video" + ret.videoList.size());
		return ret;
	}

	/**
	 * 递归扫描，得到所有的视频、音频、图片文件
	 */
	private ListSpecificReturn getSpecificFilesRec(File dir) {
		File[] children = dir.listFiles();
		ListSpecificReturn tmp = null;
		ListSpecificReturn ret = null;
		// Trace.Info("###" + dir.getAbsolutePath());
		if (children != null && children.length > 0
						&& !dir.getAbsolutePath().contains(Constant.RECYCLE)
						&& !dir.getAbsolutePath().contains(Constant.APP_DIR)
						&& !dir.getAbsolutePath().contains(Constant.RECYCLE2)) {
			// 如果文件夹下有文�?
			ret = new ListSpecificReturn();

			for (int i = 0; i < children.length; i++) {
				if (stopScan) {
					Trace.Debug("getsPecilasRec-stopScan");
					break;
				}
				if (children[i].isDirectory()) {// 如果文件是文件夹
					if (children[i].getAbsolutePath().length() < 256
									&& !children[i].getAbsolutePath().contains(
													Constant.APP_DIR)) {
						// 【注】不加长度判断会抛出StackOverflowError
						tmp = getSpecificFilesRec(children[i]);
					} else
						continue;
				} else {
					switch (Utils.getMmt(children[i].getAbsolutePath())) {
					case MMT_MOVIE:
						// ret.videoList.add(new CommonFileInfo(children[i]));
						// 图片文件两层展示，先赋给List<File>类型的tmpPhotoList�?
						// 再在getAllSpecificFiles函数中�?�过tmpPhotoList�?
						// List<CommonFileInfo>photoList赋�?�，同时去除非法数据
						if (!ret.tmpVideoList.contains(children[i]
										.getParentFile())) {
							ret.tmpVideoList.add(children[i].getParentFile());
						}
						break;
					case MMT_MUSIC:
						ret.musicAllList.add(new CommonFileInfo(children[i]));
						// 音乐文件两层展示，先赋给List<File>类型的tmpPhotoList�?
						// 再在getAllSpecificFiles函数中�?�过tmpMusicList�?
						// List<CommonFileInfo>musicList赋�?�，同时去除非法数据
						if (!ret.tmpMusicList.contains(children[i]
										.getParentFile())) {
							ret.tmpMusicList.add(children[i].getParentFile());
						}
						break;
					case MMT_PHOTO:
						// 图片文件两层展示，先赋给List<File>类型的tmpPhotoList�?
						// 再在getAllSpecificFiles函数中�?�过tmpPhotoList�?
						// List<CommonFileInfo>photoList赋�?�，同时去除非法数据
						if (!ret.tmpPhotoList.contains(children[i]
										.getParentFile())) {

							ret.tmpPhotoList.add(children[i].getParentFile());
						}
						break;
					default:
						break;
					}
				}
				// 主要把tmp.videoList、musicList、tmpPhotoList添加到ret.
				ret.copyFrom(tmp);
			}
		}

		return ret;
	}

	/**
	 * listWithSpecificMedia返回结果封装 数据是从扫描盘符得到还是缓存得到�? 缓存得到的数据要二次校正
	 */
	class ListSpecificReturn {
		// 是否是从数据库缓存读�?
		public boolean isReadCached;
		// 是否读取结果成功
		public boolean isSucc;
		public MultimediaType eMediaType;
		// 视频列表
		public List<CommonFileInfo> videoList;
		// 音频列表
		public List<CommonFileInfo> musicList;
		public List<CommonFileInfo> musicAllList;
		// 相册列表
		public List<CommonFileInfo> photoList;
		// 临时相册列表，List<File>类型，辅助处理相册文件的两层展示
		public List<File> tmpPhotoList;
		// 临时视频列表，List<File>类型，辅助处理视频文件的两层展示
		public List<File> tmpVideoList;
		// 临时音乐列表，List<File>类型，辅助处理音乐文件的两层展示
		public List<File> tmpMusicList;
public long startTime=0;
		public ListSpecificReturn() {
			isReadCached = false;
			isSucc = false;
			eMediaType = MultimediaType.MMT_OTHER;

			videoList = new ArrayList<CommonFileInfo>();
			musicList = new ArrayList<CommonFileInfo>();
			photoList = new ArrayList<CommonFileInfo>();
			musicAllList = new ArrayList<CommonFileInfo>();
			tmpPhotoList = new ArrayList<File>();
			tmpVideoList = new ArrayList<File>();
			tmpMusicList = new ArrayList<File>();
		}

		/**
		 * 根据MultimediaType枚举返回对应的特定媒体文件列�?
		 * 
		 * @param mediaType
		 * @return
		 */
		public List<CommonFileInfo> getSpecificList(MultimediaType mediaType) {
			switch (mediaType) {
			case MMT_MOVIE:
				return videoList;
			case MMT_MUSIC:
				return musicList;
			case MMT_PHOTO:
				return photoList;
			case MMT_ALLMUSIC:
				return musicAllList;
			default:
				return new ArrayList<CommonFileInfo>();
			}
		}

		public void copyFrom(ListSpecificReturn other) {
			if (other == null)
				return;

			if (other.videoList.size() > 0) {
				videoList.addAll(other.videoList);
				other.videoList.clear();
			}
			if (other.musicList.size() > 0) {
				musicList.addAll(other.musicList);
				other.musicList.clear();
			}
			if (other.photoList.size() > 0) {
				photoList.addAll(other.photoList);
				other.photoList.clear();
			}
			if (other.tmpPhotoList.size() > 0) {
				tmpPhotoList.addAll(other.tmpPhotoList);
				other.tmpPhotoList.clear();
			}
			if (other.tmpVideoList.size() > 0) {
				tmpVideoList.addAll(other.tmpVideoList);
				other.tmpVideoList.clear();
			}
			if (other.tmpMusicList.size() > 0) {
				tmpMusicList.addAll(other.tmpMusicList);
				other.tmpMusicList.clear();
			}
			if (other.musicAllList.size() > 0) {
				musicAllList.addAll(other.musicAllList);
				other.musicAllList.clear();
			}
		}
	}

	/**
	 * 获取文件夹下特定类型的所有子文件，非递归
	 */
	public List<CommonFileInfo> getSpecificFiles(File dir,
					MultimediaType eMediaType) {
		File[] children = dir.listFiles();
		List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
		if (children != null && children.length > 0
						&& !dir.getAbsolutePath().contains(Constant.RECYCLE)
						&& !dir.getAbsolutePath().contains(Constant.APP_DIR)
						&& !dir.getAbsolutePath().contains(Constant.RECYCLE2)) {
			for (int i = 0; i < children.length; i++) {
				if(stopScan){
					Trace.Debug("getspecialFiles-stopScan");
					break;
				}
				if (children[i].isFile()) {
					if (eMediaType == Utils.getMmt(children[i]
									.getAbsolutePath())) {
						list.add(new CommonFileInfo(children[i]));
						if(com.konka.eplay.Configuration.sortType==SortType.ST_BY_NAME)
						list.get(list.size()-1).setFullPinyin(list.get(list.size()-1).getFullPinyin());
					} else {
						continue;
					}
				}
			}
		}
		return list;
	}
public static  void stopScan(){
	Trace.Debug("stopScan");
	stopScan=true;
}
}
