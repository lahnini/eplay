package com.konka.eplay.modules;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.konka.eplay.R;

public class SetDialog extends Dialog implements OnFocusChangeListener,
				OnClickListener {
	private Button mSortLeftButton;
	private Button mSortRightButton;
	private TextView mSortMidTxt;
	private TextView mVersiontTextView;
	private TextView mCleanTextView;

	public SetDialog(Context context) {
		super(context, R.style.set_dialog);
		// TODO Auto-generated constructor stub

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window win = getWindow();
		WindowManager.LayoutParams params = win.getAttributes();
		params.dimAmount = 0.0f;
		// LayoutParams params = new LayoutParams();
		params.x = 1800;
		params.y = -180;

		win.setAttributes(params);
		win.setLayout(250, 300);
		setContentView(R.layout.dialog_setdialog);
		mSortLeftButton = (Button) findViewById(R.id.sort_leftbtn);
		mSortRightButton = (Button) findViewById(R.id.sort_rightbtn);
		mSortMidTxt = (TextView) findViewById(R.id.sortmidtxt);
		mCleanTextView = (TextView) findViewById(R.id.clean);
		mVersiontTextView = (TextView) findViewById(R.id.version);
		mSortLeftButton.setOnClickListener(this);
		mSortRightButton.setOnClickListener(this);
		mVersiontTextView.setOnClickListener(this);
		mCleanTextView.setOnClickListener(this);
		mSortLeftButton.setOnFocusChangeListener(this);
		mSortRightButton.setOnFocusChangeListener(this);
		mVersiontTextView.setOnFocusChangeListener(this);
		mCleanTextView.setOnFocusChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sort_leftbtn:
			mSortMidTxt.setText(R.string.sortauto);
			break;
		case R.id.sort_rightbtn:
			mSortMidTxt.setText(R.string.sortname);

			break;
		case R.id.clean:

			break;
		case R.id.version:

			break;

		default:
			break;
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sort_leftbtn:
			// if(hasFocus){
			// mSortLeftButton.setBackgroundResource(R.drawable.sort_left_selected);
			// // mSortRightButton.setBackgroundResource(R.drawable.sort_right);
			// }else {
			// mSortLeftButton.setBackgroundResource(R.drawable.sort_left);
			//
			// }
			break;
		case R.id.sort_rightbtn:
			// if(hasFocus){
			// // mSortLeftButton.setBackgroundResource(R.drawable.sort_left);
			// mSortRightButton.setBackgroundResource(R.drawable.sort_right_selected);
			// }else {
			// mSortRightButton.setBackgroundResource(R.drawable.sort_right);
			//
			// }

			break;
		case R.id.clean:
			// if(hasFocus){
			// mCleanTextView.setBackgroundResource(R.drawable.layout_selector);
			// }
			// else {
			// mCleanTextView.setBackgroundResource(R.drawable.layout_selector);
			// }
			break;
		case R.id.version:
			// if(hasFocus){
			// mCleanTextView.setBackgroundResource(R.drawable.layout_selector);
			// }
			// else {
			// mCleanTextView.setBackgroundResource(R.drawable.layout_selector);
			// }

			break;

		default:
			break;
		}
	}
}
