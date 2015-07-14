package com.konka.eplay.modules.music;

import com.konka.eplay.R;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @ClassName: AlphaFragment
 * @Description: 用作界面蒙版
 * @author xuyunyu
 * @date 2015年3月25日 下午5:31:39
 * @version
 * 
 */
public class AlphaFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.alpha_layout, container, false);
		return root;
	}

}
