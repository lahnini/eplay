package com.konka.eplay.modules.files;

import android.content.Context;
import android.os.Environment;

import com.konka.android.storage.KKStorageManager;
import com.konka.eplay.model.LocalDiskInfo;

import java.util.ArrayList;

import iapp.eric.utils.base.Trace;

public class UtilsNoKonka {

    private static ArrayList<LocalDiskInfo> sUsbList;

    /**
     * @brief 获取外部存储设备列表
     * @param context
     *            上下文
     * @return 外部存储设备列表
     */
    public static ArrayList<LocalDiskInfo> getExternalStorage(Context context) {
        Trace.Debug("#### getExternalStorage()");
        KKStorageManager kksm = KKStorageManager.getInstance(context);
        String[] volumes = kksm.getVolumePaths();

        sUsbList.clear();
        if (volumes == null) {

            return null;
        }

        Trace.Debug("volumes.length=" + volumes.length);

        for (int i = 0; i < volumes.length; ++i) {
            String state = kksm.getVolumeState(volumes[i]);

            if (state == null || !state.equals(Environment.MEDIA_MOUNTED)) {
                continue;
            }
            Trace.Debug("#### kksm.getVolumeLabel()"
                    + kksm.getVolumeLabel(volumes[i]));
            sUsbList.add(new LocalDiskInfo(volumes[i], kksm
                    .getVolumeLabel(volumes[i])));
        }
        Trace.Debug("sUsblistSize=" + sUsbList.size());
        return sUsbList;
    }
}