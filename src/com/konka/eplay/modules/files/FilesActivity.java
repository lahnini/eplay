package com.konka.eplay.modules.files;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.eplay.Constant;
import com.konka.eplay.GlobalData;
import com.konka.eplay.R;
import com.konka.eplay.Utils;
import com.konka.eplay.event.EventCommResult;
import com.konka.eplay.event.EventDispatchCenter;
import com.konka.eplay.event.EventMusicStateChange;
import com.konka.eplay.event.EventTimeout;
import com.konka.eplay.event.IEvent;
import com.konka.eplay.model.CommonResult;
import com.konka.eplay.model.FileComparator;
import com.konka.eplay.model.LocalDiskInfo;
import com.konka.eplay.modules.CommonFileInfo;
import com.konka.eplay.modules.Operation;
import com.konka.eplay.modules.ScrollGridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import iapp.eric.utils.base.Trace;

public class FilesActivity extends Activity {

    private ScrollGridView mGridView;
    private TextView mEmptyView;

    /***
     * 当前USB链表
     */
    private List<LocalDiskInfo> mUsbList;
    private Operation mOperation;
    private GlobalData mApp;
    private Constant.DataSourceType mCurDataSourceType;
    private IntentFilter mUsbFilter;
    private FilesUsbReceiver mUsbReceiver;

    private static long startTime=0;

    private ImageView mBordView = null;

    /***
     * 存储根目录文件链表
     */
    public static List<CommonFileInfo> sFileList = null;

    /***
     * 要传递给下一个Activity当前文件链表
     */
    public static List<CommonFileInfo> sSendList = null;

    private FilesBrowseAdapter mAdapter = null;

    private int mOnEventCount = 0;

    static {
        sFileList = new ArrayList<CommonFileInfo>();
        sSendList = new ArrayList<CommonFileInfo>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventDispatchCenter.getInstance().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventDispatchCenter.getInstance().register(this);
    }

    private void initData() {
        mApp = (GlobalData) getApplication();
        mApp.notifySwitchFragment(Constant.TAB_TYPE.TAB_IMG);
        mOperation = Operation.getInstance();
        mCurDataSourceType = Constant.DataSourceType.DST_LOCAL;
        mUsbFilter = new IntentFilter();
        mUsbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mUsbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        mUsbFilter.addDataScheme("file");
        mUsbReceiver = new FilesUsbReceiver();
        registerReceiver(mUsbReceiver, mUsbFilter);

        mUsbList = Utils.getExternalStorage(this);

        sFileList.clear();

        new Thread(new FindFilesRunnable()).start();

        mAdapter = new FilesBrowseAdapter(this,sFileList);
        mGridView.setAdapter(mAdapter);

    }

    private class FindFilesRunnable implements Runnable {

        @Override
        public void run() {
            Trace.Debug("####FindFilesRunnable run");
            if (sFileList.size() == 0) {
                if (mUsbList != null && mUsbList.size() > 0) {
                    Trace.Info("###find data");
                    mEmptyView.setVisibility(View.GONE);
                    for (int i = 0; i < mUsbList.size(); i++) {
                        String path = mUsbList.get(i).getPath();
                        mOperation.getLocalProvider().list(path, Constant.LIST_TYPE.ALL);
                    }

                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                    mGridView.setEmptyView(mEmptyView);
                }
            }
        }
    }



    private void initView() {
        setContentView(R.layout.activity_files);
        mGridView = (ScrollGridView) this.findViewById(R.id.local_files_gridview);
        mBordView = (ImageView) this.findViewById(R.id.border_view_in_files_gridView);
        mEmptyView = (TextView) this.findViewById(R.id.local_files_empty_view);

        mGridView.setBorderView(mBordView);
    }


    public void onEventMainThread(IEvent event) {
        Trace.Debug("####onEventMainThread");
        if (event instanceof EventCommResult) {
            EventCommResult commEvent = (EventCommResult) event;
            if (commEvent.type != Constant.MSG_LIST)
                return;
            CommonResult result = commEvent.result;

            if (result.code == CommonResult.OK && result.time >= startTime) {
                startTime = result.time;

//                sFileList.clear();
                List<CommonFileInfo> list = (List<CommonFileInfo>) (result.data);
                sFileList.addAll(list);

                mOnEventCount ++;

                list.clear();

                if (mOnEventCount == mUsbList.size()) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * Activity监听到USB状态变化后会调用此函数，子Fragment无需注册/解绑广播
     *
     * @param intent
     */
//    public void notifyUsbChanged(Intent intent) {
//        Trace.Debug("###notifyUsbChanged");
//        if (mProDialog.isShowing())
//            mProDialog.dismiss();
//        if (mProDialog != null) {
//            mProDialog.setMessageText(mApp.getResources().getString(
//                    R.string.progress_dialog_loading));
//            mProDialog.show();
//        }
//        String action = intent.getAction();
//        Trace.Debug("##### getExternalStorage()");
//        mUsbList = Utils.getExternalStorage(mApp.getApplicationContext());
//        Trace.Info("###mUsbList.size() = " + mUsbList.size());
//        mEnterFolder = false;
//        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {// sdcard绑定
//            if (emptyView != null)
//                emptyView.setVisibility(View.GONE);
//            // if (mToast == null)
//            // showToast(R.string.usb_mount);f
//            if (mUsbList != null && mUsbList.size() > 0) {
//                mHandler.sendEmptyMessage(GET_PHOTO);
//                // mCurrentFilePath.setVisibility(View.VISIBLE);
//            }
//        } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {// sdcard移除
//            // if (mToast == null)
//            // showToast(R.string.usb_remove);
//            if (writeMap != null) {
//                Thread dummy = writeMap;
//                writeMap = null;
//                dummy.interrupt();
//            }
//            if (null == mUsbList || mUsbList.size() == 0) {
//                mGridView.setOnItemSelectedListener(null);
//                sFileList.clear();
//                mHandler.sendEmptyMessage(NO_DATA);
//            } else if (mUsbList != null && mUsbList.size() > 0) {
//                mHandler.sendEmptyMessage(GET_PHOTO);
//            }
//        }
//
//    }


    /**
     * 监听USB状态
     */
    private class FilesUsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Trace.Info("PhotoActivity###UsbChanged");
//            notifyUsbChanged(intent);
        }
    }

    public ScrollGridView getGridView() {
        return mGridView;
    }
}