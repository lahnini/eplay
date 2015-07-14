package com.konka.eplay.modules;

import com.konka.eplay.R;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @ClassName: VersionToast
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年4月29日 上午10:55:08
 *
 */
public class VersionToast {

	private static Toast sToast = null;

	private static TextView sTextView = null;

	private static int sTime = 5000;

	public static void showToast(Context context, String text) {

		if (sToast != null) {
			// sToast.cancel();
			// 不用cancel，直接用setText就不会出现闪烁的现象
			// sToast.setText(text);
			sTextView.setText(text);
		} else {
			sToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			sToast = new Toast(context);
			View view = View.inflate(context, R.layout.version_toast_layout, null);

			sTextView = (TextView) view.findViewById(R.id.version_toast_text);
			sToast.setView(view);
			sTextView.setText(text);
			sToast.setDuration(sTime);
		}

		sToast.show();
	}

}
