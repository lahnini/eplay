/**
* @Title: MusicListView.java
* @Package com.konka.eplay.modules.music
* @Description: TODO(用一句话描述该文件做什么)
* @author A18ccms A18ccms_gmail_com
* @date 2015年5月8日 下午9:26:44
* @version
*/
package com.konka.eplay.modules.music;

import iapp.eric.utils.base.Trace;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * @ClassName: MusicListView
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年5月8日 下午9:26:44
 * @version
 *
 */
public class MusicListView extends ListView {


	public boolean isOnMeasure;

	public MusicListView(Context context) {
		super(context);
	}


	public MusicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public MusicListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    isOnMeasure = true;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		isOnMeasure = false;
		super.onLayout(changed, l, t, r, b);
	}








}
