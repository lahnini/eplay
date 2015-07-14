package com.konka.eplay.model;

import iapp.eric.utils.base.StringOperations;
import iapp.eric.utils.base.Trace;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

import com.konka.eplay.Utils;
import com.konka.eplay.modules.CommonFileInfo;

/**
 * 
 * Created on: 2014-1-9
 * 
 * @brief 比较器工具，默认升序排序
 * @author Eric Fung
 * @date Latest modified on: 2014-1-9
 * @version V1.0.00
 * 
 */
public class FileComparator {

	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 根据文件名比较大小（兼容类似连续剧文件带数字排序） 目前约定： 1、对于连续剧文件，只兼容数字打头或结尾的 2、文件夹总排在文件前面
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortByName implements Comparator<CommonFileInfo> {

		@Override
		public int compare(CommonFileInfo o1, CommonFileInfo o2) {
//			if (o1.isDir() && o2.isDir()) {
//				return o1.getFirstLetter().compareToIgnoreCase(o2.getFirstLetter());
//			}
//
//			if (o1.isDir() && !o2.isDir()) {
//				return -1;
//			}
//
//			if (!o1.isDir() && o2.isDir()) {
//				return 1;
//			}
//
//			String l, r;// 去掉后缀的名称
//			int index;// 后缀的位置
//
//			// 先去掉后缀
//			if ((index = o1.getFirstLetter().lastIndexOf('.')) != -1) {
//				l = o1.getFirstLetter().substring(0, index);
//			} else {
//				l = o1.getFirstLetter();
//			}
//
//			if ((index = o2.getFirstLetter().lastIndexOf('.')) != -1) {
//				r = o2.getFirstLetter().substring(0, index);
//			} else {
//				r = o2.getFirstLetter();
//			}
//
//			// Constant.trace("l=" + l);
//			// Constant.trace("r=" + r);
//
//			// 待比较的数字
//			int lNum = 0, rNum = 0;
//			// 待比较的文字
//			String lText = null, rText = null;
//
//			try {
//				// 两个比较的字串存在共同的特性才进行特殊比较
//				if (StringOperations.endsWithDigit(l)
//								&& StringOperations.endsWithDigit(r)) {
//					String lNumStr = StringOperations.getEndDigit(l);
//					String rNumStr = StringOperations.getEndDigit(r);
//					lNum = Integer.parseInt(lNumStr);
//					rNum = Integer.parseInt(rNumStr);
//					lText = l.substring(0, l.indexOf(lNumStr));
//					rText = r.substring(0, r.indexOf(rNumStr));
//				} else if (StringOperations.startWithDigit(l)
//								&& StringOperations.startWithDigit(r)) {
//					String lNumStr = StringOperations.getStartDigit(l);
//					String rNumStr = StringOperations.getStartDigit(r);
//					lNum = Integer.parseInt(lNumStr);
//					rNum = Integer.parseInt(rNumStr);
//					lText = l.substring(lNumStr.length());
//					rText = r.substring(rNumStr.length());
//				} else {
//					// 正常比较
//					// Constant.trace("正常比较1");
//					return o1.getFirstLetter().compareToIgnoreCase(o2.getFirstLetter());
//				}
//			} catch (NumberFormatException e) {
//				// 数字字符串超长，超过整型范围则按正常比较
//				return o1.getFirstLetter().compareToIgnoreCase(o2.getFirstLetter());
//			}
//
//			/**
//			 * 1、同时为null，则比较的名称是纯数字
//			 */
//			if ((lText == null && rText == null) || lText.compareTo(rText) == 0) {
//				// 文字部分一致
//				if (lNum == rNum) {
//					return 0;
//				} else {
//					// 默认升序
//					return (lNum > rNum ? 1 : -1);
//				}
//			}

			// 正常比较
			// Constant.trace("正常比较2");
//			o1.setFirstLetter(o1.getFirstLetter());
//			o2.setFirstLetter(o2.getFirstLetter());
			return o1.getFullPinyin().compareToIgnoreCase(o2.getFullPinyin());
		}

	}

	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 根据文件大小排序
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortBySize implements Comparator<CommonFileInfo> {

		@Override
		public int compare(CommonFileInfo o1, CommonFileInfo o2) {
			if (o1.isDir() && o2.isDir()) {
				return 0;
			}

			if (o1.isDir() && !o2.isDir()) {
				return 1;
			}

			if (!o1.isDir() && o2.isDir()) {
				return -1;
			}

			long size1 = o1.getSize();
			long size2 = o2.getSize();

			if (size1 == size2) {
				return 0;
			} else {
				// 默认升序
				return (size1 > size2 ? 1 : -1);
			}
		}

	}

	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 根据修改日期排序。
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortByModifyDate implements Comparator<CommonFileInfo> {

		@Override
		public int compare(CommonFileInfo o1, CommonFileInfo o2) {
			return o1.getModifiedTime().compareTo(o2.getModifiedTime());
		}

	}
//	public static class sortByName implements Comparator<CommonFileInfo> {
//		
//		@Override
//		public int compare(CommonFileInfo o1, CommonFileInfo o2) {
//			return o1.getFirstLetter().compareTo(o2.getFirstLetter());
//		}
//		
//	}

	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 按后缀类型排序。 目前的约定： 1、文件夹总排在文件前面 2、有后缀文件总排在的无后缀文件前面 3、后缀一样的根据文件名称排序
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortByType implements Comparator<CommonFileInfo> {

		@Override
		public int compare(CommonFileInfo o1, CommonFileInfo o2) {
			if (o1.isDir() && o2.isDir()) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}

			if (o1.isDir() && !o2.isDir()) {
				return -1;
			}

			if (!o1.isDir() && o2.isDir()) {
				return 1;
			}

			int length1 = o1.getName().lastIndexOf(".");
			int length2 = o2.getName().lastIndexOf(".");
			// 都存在后缀
			if (length1 > 0 && length2 > 0) {
				if (o1.getName()
								.substring(length1)
								.equalsIgnoreCase(
												o2.getName().substring(length2))) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				} else {
					return o1.getName()
									.substring(length1)
									.compareToIgnoreCase(
													o2.getName().substring(
																	length2));
				}
			}

			// o1没有后缀，o2有后缀
			if (length1 < 0 && length2 > 0) {
				return 1;
			}
			// o1有后缀，o2没后缀
			if (length1 > 0 && length2 < 0) {
				return -1;
			}
			// 都没有后缀
			return o1.getName().compareToIgnoreCase(o2.getName());
		}

	}
	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 根据歌手名比较大小（兼容类似连续剧文件带数字排序） 目前约定： 1、对于连续剧文件，只兼容数字打头或结尾的 2、文件夹总排在文件前面
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortBySinger implements Comparator<CommonFileInfo> {

		@Override
		public int compare(CommonFileInfo o1, CommonFileInfo o2) {
//			if (o1.isDir() && o2.isDir()) {
//				return Utils.getFirstChar(o1.getSinger()).compareToIgnoreCase(Utils.getFirstChar(o2.getSinger()));
//			}
//
//			if (o1.isDir() && !o2.isDir()) {
//				return -1;
//			}
//
//			if (!o1.isDir() && o2.isDir()) {
//				return 1;
//			}
//
//			String l, r;// 去掉后缀的名称
//			int index;// 后缀的位置
//
//			// 先去掉后缀
//			if ((index = o1.getSinger().lastIndexOf('.')) != -1) {
//				l =Utils.getFirstChar(o1.getSinger());
//			} else {
//				l = Utils.getFirstChar(o1.getSinger());
//			}
//
//			if ((index = o2.getSinger().lastIndexOf('.')) != -1) {
//				r =Utils.getFirstChar(o2.getSinger());
//			} else {
//				r =Utils.getFirstChar(o2.getSinger());
//			}
//
//			// Constant.trace("l=" + l);
//			// Constant.trace("r=" + r);
//
//			// 待比较的数字
//			int lNum = 0, rNum = 0;
//			// 待比较的文字
//			String lText = null, rText = null;
//
//			try {
//				// 两个比较的字串存在共同的特性才进行特殊比较
//				if (StringOperations.endsWithDigit(l)
//								&& StringOperations.endsWithDigit(r)) {
//					String lNumStr = StringOperations.getEndDigit(l);
//					String rNumStr = StringOperations.getEndDigit(r);
//					lNum = Integer.parseInt(lNumStr);
//					rNum = Integer.parseInt(rNumStr);
//					lText =Utils.getFirstChar(o1.getSinger());
//					rText =Utils.getFirstChar(o2.getSinger());
//				} else if (StringOperations.startWithDigit(l)
//								&& StringOperations.startWithDigit(r)) {
//					String lNumStr = StringOperations.getStartDigit(l);
//					String rNumStr = StringOperations.getStartDigit(r);
//					lNum = Integer.parseInt(lNumStr);
//					rNum = Integer.parseInt(rNumStr);
//					lText = Utils.getFirstChar(o1.getSinger());
//					rText =Utils.getFirstChar(o2.getSinger());
//				} else {
//					// 正常比较
//					// Constant.trace("正常比较1");
//					return Utils.getFirstChar(o1.getSinger()).compareToIgnoreCase(Utils.getFirstChar(o2.getSinger()));
//				}
//			} catch (NumberFormatException e) {
//				// 数字字符串超长，超过整型范围则按正常比较
//				return Utils.getFirstChar(o1.getSinger()).compareToIgnoreCase(Utils.getFirstChar(o2.getSinger()));
//			}
//
//			/**
//			 * 1、同时为null，则比较的名称是纯数字
//			 */
//			if ((lText == null && rText == null) || lText.compareTo(rText) == 0) {
//				// 文字部分一致
//				if (lNum == rNum) {
//					return 0;
//				} else {
//					// 默认升序
//					return (lNum > rNum ? 1 : -1);
//				}
//			}
//
//			// 正常比较
//			// Constant.trace("正常比较2");
			return  o1.getSingerFirstLetter().compareToIgnoreCase(o2.getSingerFirstLetter());
		}

	}
	
	
	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 根据专辑名比较大小（兼容类似连续剧文件带数字排序） 目前约定： 1、对于连续剧文件，只兼容数字打头或结尾的 2、文件夹总排在文件前面
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortBySpecial implements Comparator<CommonFileInfo> {

		@Override
		public int compare(CommonFileInfo o1, CommonFileInfo o2) {
//			if (o1.isDir() && o2.isDir()) {
//				return Utils.getFirstChar(o1.getSpecial()).compareToIgnoreCase(Utils.getFirstChar(o2.getSpecial()));
//			}
//
//			if (o1.isDir() && !o2.isDir()) {
//				return -1;
//			}
//
//			if (!o1.isDir() && o2.isDir()) {
//				return 1;
//			}
//
//			String l, r;// 去掉后缀的名称
//			int index;// 后缀的位置
//
//			// 先去掉后缀
//			if ((index = o1.getSpecial().lastIndexOf('.')) != -1) {
//				l = o1.getSpecial().substring(0, index);
//			} else {
//				l = Utils.getFirstChar(o1.getSpecial());
//			}
//
//			if ((index = o2.getSpecial().lastIndexOf('.')) != -1) {
//				r = o2.getSpecial().substring(0, index);
//			} else {
//				r =Utils.getFirstChar(o2.getSpecial());
//			}
//
//			// Constant.trace("l=" + l);
//			// Constant.trace("r=" + r);
//
//			// 待比较的数字
//			int lNum = 0, rNum = 0;
//			// 待比较的文字
//			String lText = null, rText = null;
//
//			try {
//				// 两个比较的字串存在共同的特性才进行特殊比较
//				if (StringOperations.endsWithDigit(l)
//								&& StringOperations.endsWithDigit(r)) {
//					String lNumStr = StringOperations.getEndDigit(l);
//					String rNumStr = StringOperations.getEndDigit(r);
//					lNum = Integer.parseInt(lNumStr);
//					rNum = Integer.parseInt(rNumStr);
//					lText = Utils.getFirstChar(o1.getSpecial());
//					rText =Utils.getFirstChar(o2.getSpecial());
//				} else if (StringOperations.startWithDigit(l)
//								&& StringOperations.startWithDigit(r)) {
//					String lNumStr = StringOperations.getStartDigit(l);
//					String rNumStr = StringOperations.getStartDigit(r);
//					lNum = Integer.parseInt(lNumStr);
//					rNum = Integer.parseInt(rNumStr);
//					lText = l.substring(lNumStr.length());
//					rText = r.substring(rNumStr.length());
//				} else {
//					// 正常比较
//					// Constant.trace("正常比较1");
//					return Utils.getFirstChar(o1.getSpecial()).compareToIgnoreCase(Utils.getFirstChar(o2.getSpecial()));
//				}
//			} catch (NumberFormatException e) {
//				// 数字字符串超长，超过整型范围则按正常比较
//				return Utils.getFirstChar(o1.getSpecial()).compareToIgnoreCase(Utils.getFirstChar(o2.getSpecial()));
//			}
//
//			/**
//			 * 1、同时为null，则比较的名称是纯数字
//			 */
//			if ((lText == null && rText == null) || lText.compareTo(rText) == 0) {
//				// 文字部分一致
//				if (lNum == rNum) {
//					return 0;
//				} else {
//					// 默认升序
//					return (lNum > rNum ? 1 : -1);
//				}
//			}
//
//			// 正常比较
//			// Constant.trace("正常比较2");
			return o1.getSpecialFirstLetter().compareToIgnoreCase(o2.getSpecialFirstLetter());
		}

	}
	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 根据文件名比较大小（兼容类似连续剧文件带数字排序） 目前约定： 1、对于连续剧文件，只兼容数字打头或结尾的 2、文件夹总排在文件前面
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortListByName implements Comparator<File> {

		@Override
		public int compare(File o1, File o2) {
			String l, r;// 去掉后缀的名称
			l=Utils.getFirstChar(o1.getName());
			r=Utils.getFirstChar(o2.getName());
			if (o1.isDirectory() && o2.isDirectory()) {
				return l.compareToIgnoreCase(r);
			}

			if (o1.isDirectory() && !o2.isDirectory()) {
				return -1;
			}

			if (!o1.isDirectory() && o2.isDirectory()) {
				return 1;
			}

			// 待比较的数字
			int lNum = 0, rNum = 0;
			// 待比较的文字
			String lText = null, rText = null;

			try {
				// 两个比较的字串存在共同的特性才进行特殊比较
				if (StringOperations.endsWithDigit(l)
								&& StringOperations.endsWithDigit(r)) {
					String lNumStr = StringOperations.getEndDigit(l);
					String rNumStr = StringOperations.getEndDigit(r);
					lNum = Integer.parseInt(lNumStr);
					rNum = Integer.parseInt(rNumStr);
					lText = l.substring(0, l.indexOf(lNumStr));
					rText = r.substring(0, r.indexOf(rNumStr));
				} else if (StringOperations.startWithDigit(l)
								&& StringOperations.startWithDigit(r)) {
					String lNumStr = StringOperations.getStartDigit(l);
					String rNumStr = StringOperations.getStartDigit(r);
					lNum = Integer.parseInt(lNumStr);
					rNum = Integer.parseInt(rNumStr);
					lText = l.substring(lNumStr.length());
					rText = r.substring(rNumStr.length());
				} else {
					// 正常比较
					// Constant.trace("正常比较1");
					return l.compareToIgnoreCase(r);
				}
			} catch (NumberFormatException e) {
				// 数字字符串超长，超过整型范围则按正常比较
				return l.compareToIgnoreCase(r);
			}

			/**
			 * 1、同时为null，则比较的名称是纯数字
			 */
			if ((lText == null && rText == null) || lText.compareTo(rText) == 0) {
				// 文字部分一致
				if (lNum == rNum) {
					return 0;
				} else {
					// 默认升序
					return (lNum > rNum ? 1 : -1);
				}
			}

			// 正常比较
			// Constant.trace("正常比较2");
			return l.compareToIgnoreCase(r);
		}

	}
	/**
	 * 
	 * Created on: 2014-1-9
	 * 
	 * @brief 根据修改日期排序。
	 * @author Eric Fung
	 * @date Latest modified on: 2014-1-9
	 * @version V1.0.00
	 * 
	 */
	public static class sortListByModifyDate implements Comparator<File> {

		@Override
		public int compare(File o1, File o2) {
			Date modifiedTime1=new Date(( o1).lastModified());
			Date modifiedTime2=new Date(( o2).lastModified());
			return modifiedTime1.compareTo(modifiedTime2);
		}

	}

}
