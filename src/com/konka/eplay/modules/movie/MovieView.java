package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

/**
 * 自定义的movie播放控件,继承SurfaceView
 */
public class MovieView extends SurfaceView {

	// 此surfaceview对应的holder
	// private SurfaceHolder mSurfaceHolder = null;
	// 与此movieview关联的movieplayer
	private MoviePlayer mMoviePlayer = null;

	public MovieView(Context context) {
		super(context);
		initMovieView(context);

	}

	public MovieView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMovieView(context);
	}

	public MovieView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMovieView(context);
	}

	// 初始化控件
	private void initMovieView(Context context) {
		// mSurfaceHolder = getHolder();
		// mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mMoviePlayer = new MoviePlayer(context, this);
	}

	/**
	 * @return mMoviePlayer 控制播放的MoviePlayer
	 */
	public MoviePlayer getMoviePlayer() {
		return mMoviePlayer;
	}

	/**
	 * @brief 缩放view，适配视频展示
	 * @param rectL
	 *            区域水平起点
	 * @param rectT
	 *            区域垂直起点
	 * @param rectW
	 *            区域宽度
	 * @param rectH
	 *            区域高度
	 */
	private void scaleView(int rectL, int rectT, int rectW, int rectH) {
		Trace.Info("scale view to:(" + rectL + "," + rectT + "," + rectW + "," + rectH + ")");
		LayoutParams params = this.getLayoutParams();
		if (rectW == 0 || rectH == 0) {
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.MATCH_PARENT;
		}
		else {
			params.width = rectW;
			params.height = rectH;
		}
		this.setLayoutParams(params);
	}

	/**
	 * @brief 计算当前view和video之间的比例
	 * @param viewW
	 *            屏幕宽度
	 * @param viewH
	 *            屏幕高度
	 * @param videoW
	 *            视频宽度
	 * @param videoH
	 *            视频高度
	 * @return 缩放比例
	 */
	private double calculateZoom(double viewW, double viewH, double videoW, double videoH) {
		Trace.Info("calculate Zoom : " + "videoWidth = " + videoW + " & videoHeight = " + videoH);
		if (videoW == 0 || videoH == 0) {
			return 1;
		}
		double dRet = 0.5;
		double rate = 1;
		// 使视频缩小到小于屏幕
		while (videoW > viewW || videoH > viewH) {
			videoW = videoW / 2;
			videoH = videoH / 2;
			rate = rate / 2;
		}

		// 再等比拉伸
		if (viewW >= videoW && viewH >= videoH) {
			double dw = viewW / videoW;
			double dh = viewH / videoH;
			if (dw > dh)
				dRet = dh;
			else
				dRet = dw;
		}
		else {
			Trace.Info("should never run to here");
		}
		Trace.Info("video zoom rate: " + dRet * rate);
		return dRet * rate;
	}

	/**
	 * 切换屏显模式
	 * 
	 * @param playerType
	 *            这个参数暂时没用，底层播放器类型，目前直接使用中间件定义的getMediaPlayerVersion。如MSTAR，RTK，
	 *            HISI等等
	 * @param mode
	 *            屏显模式
	 * @param rectL
	 *            view的水平起点
	 * @param rectT
	 *            view的垂直起点
	 * @param rectW
	 *            view的宽度
	 * @param rectH
	 *            view的高度
	 */
	public void switchScreenDisplayMode(int playerType, int mode, int rectL, int rectT, int rectW, int rectH) {
		if (mode == MoviePlayer.SCREEN_FULL_RESIZE) {
			// 拉伸全屏模式
			scaleView(rectL, rectT, rectW, rectH);
			return;
		}

		// 获取视频uri
		// Uri uri = mMoviePlayer.getDataSourceUri();
		// if (uri == null) {
		// return;
		// }
		int videoW = 0;
		int videoH = 0;
		// MediaMetadataRetriever retriever = null;
		// try {
		// // 获取视频原宽高
		// retriever = new MediaMetadataRetriever();
		// retriever.setDataSource(uri.getPath());
		// String width =
		// retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
		// String height =
		// retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
		// if (width == null || height == null) {
		// return;
		// }
		// videoW = Integer.parseInt(width);
		// videoH = Integer.parseInt(height);
		// } catch (RuntimeException e) {
		// Trace.Info("get Video resolution runtime exception,video path=" +
		// uri.getPath());
		// // 拉伸全屏
		// scaleView(rectL, rectT, rectW, rectH);
		// return;
		// } finally {
		// if (retriever != null) {
		// retriever.release();
		// }
		// }

		videoW = mMoviePlayer.getVideoWidth();
		videoH = mMoviePlayer.getVideoHeight();
		double zoom = calculateZoom(rectW, rectH, videoW, videoH);
		if (mode == MoviePlayer.SCREEN_DEFAULT) {
			// 原始模式
			if (videoW <= rectW && videoH <= rectH) {
				int left = (rectW - videoW) / 2;
				int top = (rectH - videoH) / 2;
				scaleView(left, top, videoW, videoH);
				return;
			}
			else {
				// 如果原视频大于屏幕，进行等比拉伸
				mode = MoviePlayer.SCREEN_FULL;
			}
		}
		if (mode == MoviePlayer.SCREEN_FULL) {
			// 等比模式
			int left = (int) (rectW - videoW * zoom) / 2;
			int top = (int) (rectH - videoH * zoom) / 2;
			scaleView(left, top, (int) (videoW * zoom), (int) (videoH * zoom));
			return;
		}

	}
}
