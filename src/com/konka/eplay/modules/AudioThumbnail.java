package com.konka.eplay.modules;

import iapp.eric.utils.custom.model.APIC;
import iapp.eric.utils.metadata.Mp3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AudioThumbnail {
	/**
	 * 获取音频缩略图，失败则返回null
	 * 
	 * @param path
	 * @return
	 */

	public static Bitmap getThumb(String path) {
		Mp3 m = new Mp3(path, false, true);
		if (null != m.getTagID3V2()) {
			// System.out.println("####ID3V2不为空");
			if (m.getTagID3V2().getTagHeader().equals("ID3")) {
				// Trace.Info("获取到了DI3V2标签中的数据");
				if (null != m.getTagID3V2().getTagFrame().get("APIC")) {
					APIC apic = (APIC) (m.getTagID3V2().getTagFrame()
									.get("APIC").getContent());
					if (null != apic) {
						if (apic.pictureData != null) {
							// Trace.Info("music cover byte[] is not null length-->"
							// + apic.pictureData.length);
							byte[] buffer = apic.pictureData;
							Bitmap bm = BitmapFactory.decodeByteArray(buffer,
											0, buffer.length);
							return bm;
						} else if (apic.url != null) {
							// Trace.Info("music cover url -->" + apic.url);
						}
					}
				}
			}
		}
		return null;
	}
}
