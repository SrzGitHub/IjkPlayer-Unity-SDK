package com.letinvr.playsdk;

import android.media.MediaPlayer;
import android.util.Log;

import com.letinvr.playsdk.util.LogToFile;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * //                    .::::.
 * //                  .::::::::.
 * //                 :::::::::::  FUCK YOU
 * //             ..:::::::::::'
 * //           '::::::::::::'
 * //             .::::::::::
 * //        '::::::::::::::..
 * //             ..::::::::::::.
 * //           ``::::::::::::::::
 * //            ::::``:::::::::'        .:::.
 * //           ::::'   ':::::'       .::::::::.
 * //         .::::'      ::::     .:::::::'::::.
 * //        .:::'       :::::  .:::::::::' ':::::.
 * //       .::'        :::::.:::::::::'      ':::::.
 * //      .::'         ::::::::::::::'         ``::::.
 * //  ...:::           ::::::::::::'              ``::.
 * // ```` ':.          ':::::::::'                  ::::..
 * //                    '.:::::'                    ':'````..
 * ===========================================================
 * You may think you know what the following code does.
 * But you dont. Trust me.
 * Fiddle with it, and youll spend many a sleepless
 * night cursing the moment you thought youd be clever
 * enough to "optimize" the code below.
 * Now close this file and go play with something else.
 * <p>
 * 2019/06/26 15:06 星期三
 **/
public class IMPlayListener {

    private static final String TAG = "IMPlayListener";

    private static int mVideoWidth, mVideoHeight, mVideoSarNum, mVideoSarDen;


    public static IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    mVideoSarNum = mp.getVideoSarNum();
                    mVideoSarDen = mp.getVideoSarDen();
                    LogToFile.e("Srz  --->  " + mVideoWidth + "*" + mVideoHeight + "*" + mVideoSarNum + "*" + mVideoSarDen);
                    LogToFile.e("Srz   " + width + "*" + height + "*" + sarNum + "*" + sarDen);
                }
            };

    public static IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
           API.senUnityMessage("READY_TO_PLAY");
        }
    };

    public static IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    LogToFile.e("Srz  ---> 播放完毕 ");
                }
            };

    public static IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {

            switch (arg1) {
                case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    LogToFile.e( "MEDIA_INFO_VIDEO_TRACK_LAGGING:"+arg2);
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    LogToFile.e(  "MEDIA_INFO_VIDEO_RENDERING_START:"+arg2);//开始播放
                    API.senUnityMessage("MEDIA_INFO_VIDEO_RENDERING_START");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    LogToFile.e(  "MEDIA_INFO_BUFFERING_START:"+arg2);//开始缓冲
                    API.senUnityMessage("MEDIA_INFO_BUFFERING_START");
                    UnityForAndroidPlayManager.isDragging =true;
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    LogToFile.e(  "MEDIA_INFO_BUFFERING_END:");//缓冲结束
                    API.senUnityMessage("MEDIA_INFO_BUFFERING_END"+arg2);
                    UnityForAndroidPlayManager.isDragging =false;
                    UnityForAndroidPlayManager.mHandler.sendEmptyMessage(UnityForAndroidPlayManager.MESSAGE_SHOW_PROGRESS);
                    break;
                case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                    LogToFile.e(  "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);//带宽信息
                    break;
                case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    LogToFile.e(  "MEDIA_INFO_BAD_INTERLEAVING:"+arg2);//媒体文件损坏
                    API.senUnityMessage("MEDIA_INFO_BAD_INTERLEAVING");
                    break;
                case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    LogToFile.e( "MEDIA_INFO_NOT_SEEKABLE:"+arg2);//
                    break;
                case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    LogToFile.e(  "MEDIA_INFO_METADATA_UPDATE:"+arg2);
                    break;
                case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    LogToFile.e(  "MEDIA_INFO_UNSUPPORTED_SUBTITLE:"+arg2);
                    break;
                case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                    LogToFile.e(  "MEDIA_INFO_SUBTITLE_TIMED_OUT:"+arg2);
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    LogToFile.e("MEDIA_INFO_VIDEO_ROTATION_CHANGED: "+arg2);//视频旋转
                    break;
                case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    LogToFile.e(  "MEDIA_INFO_AUDIO_RENDERING_START:"+arg2);//音频渲染开始
                    break;
            }
            return true;
        }
    };

    public static IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {

                private String messageId;

                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */


                    if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                        messageId = "invalid progressive playback";
                        LogToFile.e("Srz  --->  " + messageId);
                        API.senUnityMessage(messageId);
                    } else {
                        messageId = "unknown";
                        LogToFile.e("Srz  --->  " + messageId);
                        API.senUnityMessage(messageId);
                    }


                    return true;
                }
            };


    public static IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {

            LogToFile.e("Srz  ---> mSeekCompleteListener ");
        }
    };

    public static IMediaPlayer.OnTimedTextListener mOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
            if (text != null) {
                LogToFile.e("Srz  --->  " + text.getText());
            }

        }
    };
    IjkMediaPlayer.OnMediaCodecSelectListener mediaCodecSelectListener =new IjkMediaPlayer.OnMediaCodecSelectListener() {
        @Override
        public String onMediaCodecSelect(IMediaPlayer mp, String mimeType, int profile, int level) {



            return null;
        }
    };
}
