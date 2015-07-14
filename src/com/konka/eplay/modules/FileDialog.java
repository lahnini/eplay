package com.konka.eplay.modules;

//package com.example.multimedia;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Stack;
//
//import com.example.multimedia.Constant.DataSourceType;
//
//import android.annotation.SuppressLint;
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.BaseAdapter;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
///**
// * Created on: 2013-6-8
// * 
// * @brief what to do
// * @author zhangzhaoyong
// * @date Latest modified on: 2013-6-8
// * @version V1.0.00
// * 
// */
//@SuppressLint("HandlerLeak")
//public class FileDialog extends Dialog implements View.OnClickListener,
//		OnItemClickListener, android.view.View.OnKeyListener {
//	private static final int CLOUD_NO_LOGIN = 11;
//
//	private Context mContext;
////	private AlwaysMarqueeTextView mPathNameTV;
//	private Button mOkBtn;
//	private ListView mListView;
//	private LinearLayout mBackView;
//	private ProgressBar mProgressBar;
//	private FileAdapter mAdapter;
//
//	private OPERATE_TYPE mType;
//	private String mMark;
//	private List<CommonFileInfo> mCurFileList;
//	private Stack<List<CommonFileInfo>> mStack;
//	private Stack<Integer> mCount;
//	private Callback mCall;
//	private String mFilePath;
//	private int mIndex = 0;
//	private boolean isAll = false;
//
//	private boolean mShowAllDiskFlag;
//	private String mSourcePath;
//	private DataSourceType mDataSourceType;
//
//	public static enum OPERATE_TYPE {
//		FILE_MOVE, FILE_DOWN, FILE_UP;
//	}
//
//	public static interface Callback {
//		public void callBack(String path);
//	}
//
//	/**
//	 * @param context
//	 * @param type
//	 *            何种业务（上传�?�下载or移动）起FileDialog
//	 * @param sourcePath
//	 *            被操作的文件路径
//	 * @param theme
//	 */
//	public FileDialog(Context context, OPERATE_TYPE type, String sourcePath,
//			Callback call) {
//		super(context, R.style.common_dialog_style);
//		mContext = context;
//		mType = type;
//		mCurFileList = new ArrayList<CommonFileInfo>();
//		mStack = new Stack<List<CommonFileInfo>>();
//		mCount = new Stack<Integer>();
//		mCall = call;
//		mSourcePath = sourcePath;
//		if (type == OPERATE_TYPE.FILE_DOWN) {
//			mShowAllDiskFlag = true;
//		}
//	}
//
//	public FileDialog(Context context, boolean isAll, String sourcePath,
//			Callback call) {
//		super(context, R.style.common_dialog_style);
//		mContext = context;
//		mCurFileList = new ArrayList<CommonFileInfo>();
//		mStack = new Stack<List<CommonFileInfo>>();
//		mCount = new Stack<Integer>();
//		mCall = call;
//		mSourcePath = sourcePath;
//		this.isAll = isAll;
//		mShowAllDiskFlag = true;
//
//	}
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.file_list_pop);
//		// this.setCancelable(false);
//		GlobalData app = (GlobalData) mContext.getApplicationContext();
//		app.setTempHandler(mHandler);
//		mDataSourceType = app.getCurDataSourceType();
//		initView();
//
//		Trace.Debug("##### getExternalStorage()");
//		ArrayList<LocalDiskInfo> usbList = Utils.getExternalStorage(mContext);
//		String root = null;
//		int size = usbList.size();
//		if (size <= 0) {
//			showToast(mContext.getString(R.string.toast_no_usb));
//			dismissDialog();
//		} else if (size == 1) {
//			root = usbList.get(0).getPath();
//		} else if (size > 1) {
//			if (mShowAllDiskFlag) {
//				// 展示�?有盘�?
//				root = mContext.getString(R.string.choose_disk);
//				mOkBtn.setEnabled(false);
//			} else {
//				root = Utils.getRootPath(mSourcePath);
//			}
//		}
//		if (isAll) {
//			mMark = mContext.getString(R.string.upload_file);
//			mFilePath = root;
//			mPathNameTV.setText(mMark + Utils.getWrapperPath(mFilePath));
//		} else {
//			if (mType == OPERATE_TYPE.FILE_DOWN) {
//				mMark = mContext.getString(R.string.downloader_to);
//				mFilePath = root;
//				mPathNameTV.setText(mMark + Utils.getWrapperPath(mFilePath));
//			} else if (mType == OPERATE_TYPE.FILE_MOVE) {
//				mMark = mContext.getString(R.string.move_to);
//				switch (app.getCurDataSourceType()) {
//				case DST_CLOUD:
//					mFilePath = Constant.CLOUD_ROOT_PATH;
//					break;
//				case DST_LOCAL:
//					mFilePath = root;
//					mPathNameTV
//							.setText(mMark + Utils.getWrapperPath(mFilePath));
//					break;
//				case DST_LAN:
//				default:
//					break;
//				}
//			} else if (mType == OPERATE_TYPE.FILE_UP) {
//				mMark = mContext.getString(R.string.uploader_to);
//				mFilePath = Constant.CLOUD_ROOT_PATH;
//				mPathNameTV.setText(mMark + mFilePath);
//			}
//		}
//
//		if (mFilePath.equals(mContext.getString(R.string.choose_disk))) {
//			for (int i = 0; i < size; i++) {
//				String path = usbList.get(i).getPath();
//				CommonFileInfo file = new CommonFileInfo(new File(path));
//				String name = usbList.get(i).getLabel();
//				if (name == null)
//					name = usbList.get(i).getPath();
//				file.setName(name.trim());
//				mCurFileList.add(file);
//			}
//			mStack.push(mCurFileList);
//			mAdapter.notifyDataSetChanged();
//		} else {
//			CommonFileInfo file = new CommonFileInfo();
//			file.setPath(mFilePath);
//			file.setDir(true);
//			List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
//			list.add(file);
//			mStack.push(list);
//			getSubFileList(mFilePath);
//		}
//	}
//
//	private void dismissDialog() {
//		GlobalData app = (GlobalData) mContext.getApplicationContext();
//		app.setTempHandler(null);
//		dismiss();
//	}
//
//	private Handler mHandler = new Handler() {
//		@SuppressWarnings("unchecked")
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case Constant.MSG_LIST:
//				showProgressBar(false);
//				CommonResult result = (CommonResult) msg.obj;
//				if (result.code == CommonResult.OK) {
//					List<CommonFileInfo> list = (List<CommonFileInfo>) result.data;
//					mCurFileList.clear();
//					mCurFileList.addAll(list);
//					mAdapter.notifyDataSetChanged();
//				} else {
//					// showToast(mContext.getString(R.string.toast_data_error));
//					if (result.data != null) {
//						String errDesc = (String) result.data;
//						if (errDesc.contains("invalid session")
//								|| errDesc.contains("session timeout")) {
//							showToast(mContext.getString(R.string.cloud_konka_over_date));
//						}
//					}
//				}
//				break;
//			case CLOUD_NO_LOGIN:
//				showToast(mContext.getString(R.string.cloud_konka_not_login));
//				break;
//			default:
//				break;
//			}
//		}
//	};
//
//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.file_pop_confirm:
//			if (mCall != null) {
//				mCall.callBack(mFilePath);
//				Trace.Info("mFilePath=" + mFilePath);
//				dismissDialog();
//			}
//			break;
//		case R.id.file_pop_back:
//			dealWithBackEvent();
//			break;
//		default:
//			break;
//		}
//	}
//
//	private void initView() {
//		mListView = (ListView) findViewById(R.id.file_pop_list);
//		mAdapter = new FileAdapter();
//		mListView.setAdapter(mAdapter);
//		mListView.setOnKeyListener(this);
//		mListView.setOnItemClickListener(this);
//		mProgressBar = (ProgressBar) findViewById(R.id.file_pop_progressbar);
//
//		mPathNameTV = (AlwaysMarqueeTextView) findViewById(R.id.file_pop_path);
//		mOkBtn = (Button) findViewById(R.id.file_pop_confirm);
//		mOkBtn.setOnClickListener(this);
//		mBackView = (LinearLayout) findViewById(R.id.file_pop_back);
//		mBackView.setOnClickListener(this);
//		mBackView.setOnKeyListener(this);
//	}
//
//	@Override
//	public void onStop() {
//		GlobalData app = (GlobalData) mContext.getApplicationContext();
//		app.setTempHandler(null);
//		mCurFileList.clear();
//		int size = mStack.size();
//		for (int i = 0; i < size; i++) {
//			mStack.get(i).clear();
//		}
//		mStack.clear();
//	}
//	
//
//	private void getSubFileList(final String parentPath) {
//		showProgressBar(true);
//		new Thread() {
//			public void run() {
//				if (mType == OPERATE_TYPE.FILE_UP
//						|| (mDataSourceType == DataSourceType.DST_CLOUD && mType == OPERATE_TYPE.FILE_MOVE)) {
//					if (HWProvider.isLogined()) {
//						// 如果异步请求没有执行完毕就�??出FileDialog，则Fragment会误接收消息
//						// Operation.getInstance().getHWProvider().list(parentPath,
//						// LIST_TYPE.FOLDER_ONLY);
//						CommonResult result = Operation.getInstance()
//								.getHWProvider().listFolder(parentPath);
//						Message msg = Message.obtain();
//						msg.what = Constant.MSG_LIST;
//						msg.obj = result;
//						mHandler.sendMessage(msg);
//					} else {
//						mHandler.sendEmptyMessage(CLOUD_NO_LOGIN);
//					}
//				} else {
//					if (isAll) {
//						Operation.getInstance().getLocalProvider()
//								.list(parentPath, LIST_TYPE.ALL);
//					} else {
//						Operation.getInstance().getLocalProvider()
//								.list(parentPath, LIST_TYPE.FOLDER_ONLY);
//					}
//				}
//			};
//		}.start();
//	}
//
//	@Override
//	public void onBackPressed() {
//		Trace.Info("###onBackPressed");
//		Trace.Info("###mStack.size() = " + mStack.size());
//		if(mStack.size() == 1){
//			dismissDialog();
//			return;
//		}
//		dealWithBackEvent();
//	}
//
//	private void dealWithBackEvent() {
//		if (mStack.size() > 1) {
//			List<CommonFileInfo> list = mStack.pop();
//			mCurFileList.clear();
//			mCurFileList.addAll(list);
//			if (!mCount.empty()) {
//				mIndex = mCount.pop();
//			}
//			if (mStack.size() == 1 && mType != OPERATE_TYPE.FILE_UP) {
//				Trace.Debug("##### getExternalStorage()");
//				List<LocalDiskInfo> usbList = Utils
//						.getExternalStorage(mContext);
//				boolean isRootPath = false;
//				int size = usbList.size();
//				for (int i = 0; i < size; i++) {
//					if (mFilePath.equals(usbList.get(i).getPath())) {
//						isRootPath = true;
//						break;
//					}
//				}
//				if (isRootPath) {
//					// �?到盘符展示界�?
//					mPathNameTV.setText(mMark + mContext.getString(R.string.choose_disk));
//					mOkBtn.setEnabled(false);
//					mAdapter.notifyDataSetChanged();
//					mListView.requestFocus();
//					mListView.setSelection(mIndex);
//					return;
//				}
//			}
//			mFilePath = getParentPath(mFilePath);
//			if (mType == OPERATE_TYPE.FILE_DOWN) {
//				mPathNameTV.setText(mMark + Utils.getWrapperPath(mFilePath));
//			} else if (mType == OPERATE_TYPE.FILE_MOVE
//					&& mDataSourceType == DataSourceType.DST_LOCAL) {
//				mPathNameTV.setText(mMark + Utils.getWrapperPath(mFilePath));
//			} else {
//				mPathNameTV.setText(mMark + mFilePath);
//			}
//			mAdapter.notifyDataSetChanged();
//			mListView.requestFocus();
//			mListView.setSelection(mIndex);
//		} else {
//			mBackView.setClickable(false);
////			dismiss();
//		}
//	}
//
//	@Override
//	public boolean onKey(View v, int keyCode, KeyEvent event) {
//		if (event.getAction() != KeyEvent.ACTION_DOWN)
//			return false;
//		switch (v.getId()) {
//		case R.id.file_pop_list:
//			if (keyCode == KeyEvent.KEYCODE_BACK) {
//				Trace.Info("###mStack.size() = "+mStack.size());
//				if(mStack.size() == 1)
//					dismissDialog();
//				else
//					dealWithBackEvent();
//				return true;
//			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
//				mBackView.requestFocus();
//				return true;
//			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//				mOkBtn.requestFocus();
//				return true;
//			}
//			break;
//		case R.id.file_pop_back:
//			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//				mOkBtn.requestFocus();
//				return true;
//			}
//		default:
//			break;
//		}
//		return false;
//	}
//
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position,
//			long id) {
//		if (!mOkBtn.isEnabled())
//			mOkBtn.setEnabled(true);
//		if (mType == OPERATE_TYPE.FILE_UP) {
//			List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
//			list.addAll(mCurFileList);
//			mStack.push(list);
//			mFilePath = mCurFileList.get(position).getPath();
//			mPathNameTV.setText(mMark + mFilePath);
//			// mOkBtn.requestFocus();
//			getSubFileList(mFilePath);
//		} else {
//			mCount.push(position);
//			List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
//			list.addAll(mCurFileList);
//			mStack.push(list);
//			mFilePath = mCurFileList.get(position).getPath();
//			if (mType == OPERATE_TYPE.FILE_DOWN) {
//				mPathNameTV.setText(mMark + Utils.getWrapperPath(mFilePath));
//			} else if (mType == OPERATE_TYPE.FILE_MOVE
//					&& mDataSourceType == DataSourceType.DST_LOCAL) {
//				mPathNameTV.setText(mMark + Utils.getWrapperPath(mFilePath));
//			} else {
//				mPathNameTV.setText(mMark + mFilePath);
//			}
//			getSubFileList(mFilePath);
//		}
//		mBackView.setClickable(true);
//	}
//
//	private String getParentPath(String path) {
//		String parentPath;
//		int start = path.lastIndexOf("/");
//		if (start == 0) {
//			start += 1;
//		} else if (start == -1) {
//			return "";
//		}
//		parentPath = path.substring(0, start);
//		return parentPath;
//	}
//
//	private void showProgressBar(boolean isShow) {
//		if (isShow) {
//			mProgressBar.setVisibility(View.VISIBLE);
//		} else {
//			mProgressBar.setVisibility(View.INVISIBLE);
//		}
//	}
//	
//	private void showToast(String msg) {
//		if (mContext.getApplicationContext() != null)
//			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
//	}
//
//	private class FileAdapter extends BaseAdapter {
//		@Override
//		public int getCount() {
//			return mCurFileList == null ? 0 : mCurFileList.size();
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return mCurFileList.get(position);
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			ViewHolder holder;
//			CommonFileInfo data = mCurFileList.get(position);
//			if (convertView == null) {
//				holder = new ViewHolder();
//				LayoutInflater inflater = LayoutInflater.from(mContext);
//				convertView = inflater.inflate(R.layout.move_item, null);
//				holder.tv = (TextView) convertView
//						.findViewById(R.id.move_folder_name);
//				convertView.setTag(holder);
//			} else {
//				holder = (ViewHolder) convertView.getTag();
//			}
//			holder.tv.setText(data.getName());
//			return convertView;
//		}
//
//		class ViewHolder {
//			TextView tv;
//		}
//	}
//
// }
