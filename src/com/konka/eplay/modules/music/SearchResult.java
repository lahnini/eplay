/**
 * @Title: SearchResult.java
 * @Package com.konka.eplay.modules.music
 * @Description: TODO(用一句话描述该文件做什么)
 * @author A18ccms A18ccms_gmail_com
 * @date 2015年4月15日 上午9:51:53
 * @version
 */
package com.konka.eplay.modules.music;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ClassName: SearchResult
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author xuyunyu
 * @date 2015年4月15日 上午9:51:53
 * @version
 *
 */
public class SearchResult implements Parcelable {

	public String songname = "";
	public String singer = "";
	public String url = "";



	public SearchResult() {
		super();
	}

	public SearchResult(Parcel source){

		this.songname = source.readString();
		this.singer = source.readString();
		this.url = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.songname);
		dest.writeString(this.singer);
		dest.writeString(this.url);

	}

	public static final Parcelable.Creator<SearchResult> CREATOR= new Parcelable.Creator<SearchResult>(){

		@Override
		public SearchResult createFromParcel(Parcel source) {
			return new SearchResult(source);
		}

		@Override
		public SearchResult[] newArray(int size) {
			return new SearchResult[size];
		}

	};

}
