package com.letinvr.playsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.WindowManager;

import com.letinvr.playsdk.brodcast.NetStateChangeReceiver;
import com.letinvr.playsdk.brodcast.NetworkType;
import com.letinvr.playsdk.observer.NetStateChangeObserver;
import com.letinvr.playsdk.util.LogToFile;
import com.letinvr.playsdk.util.PowerManagerUtil;
import com.unity3d.player.UnityPlayer;



import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

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
 * 2019/06/25 16:21 星期二
 **/
public class UnityForAndroidPlayManager implements SurfaceTexture.OnFrameAvailableListener, NetStateChangeObserver {

    public static boolean isDragging;
    /**
     * 播放缓冲监听
     */
    private static int mCurrentBufferPercentage = 0;
    /**
     * 同步进度
     */
    public static final int MESSAGE_SHOW_PROGRESS = 1;
    /**
     * 播放完成
     */
    public static final int MESSAGE_START_COUNTDOWN = 4;
    public static MediaPlayerWrapper mMediaPlayerWrapper;

    private static int surfaceTextureId = 0;
    private static SurfaceTexture surfaceTexture;
    private static Surface mSurface;
    private static boolean newFrameAvailable = false;
    private static AudioManager audioManager;

    //初始化 textureID
    public int InitSurfaceTexture() {
        LogToFile.e("Srz  ---> 初始化TextureID ");

        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        CheckGlError("glGenTextures");
        surfaceTextureId = textures[0];

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, surfaceTextureId);
        CheckGlError("glBindTexture");
        //RegistrationBroadcast
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        surfaceTexture = new SurfaceTexture(surfaceTextureId);
        surfaceTexture.setOnFrameAvailableListener(UnityForAndroidPlayManager.this);
        mSurface = new Surface(surfaceTexture);

        newFrameAvailable = false;
        return surfaceTextureId;
    }


    /****************************************************************************************************************/
    public void initBroadcast(Context context) {
        LogToFile.e("Srz  ---> 初始化广播 ");
        NetStateChangeReceiver.registerReceiver(context);
    }

    public void unRegisterReceiver(Context context) {
        LogToFile.e("Srz  ---> 解除广播- ");
        NetStateChangeReceiver.unRegisterObserver(this);
        NetStateChangeReceiver.unRegisterReceiver(context);
        PowerManagerUtil.release();
    }

    /******************************************************************************************************************/
    //初始化Unity播放器
    public void initPlay() {
        LogToFile.e("Srz  ---> 初始化播放器 ");
        NetStateChangeReceiver.registerObserver(this);
        PowerManagerUtil.theScreenIsAlwaysOn();
        mMediaPlayerWrapper = new MediaPlayerWrapper();
        PowerManagerUtil.initPowerManager(UnityPlayer.currentActivity);
        audioManager = (AudioManager) UnityPlayer.currentActivity.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setSurface(mSurface);
        mMediaPlayerWrapper.setPreparedListener(IMPlayListener.mPreparedListener);
        mMediaPlayerWrapper.getPlayer().setOnErrorListener(IMPlayListener.mErrorListener);
        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(IMPlayListener.mSizeChangedListener);
        mMediaPlayerWrapper.getPlayer().setOnSeekCompleteListener(IMPlayListener.mSeekCompleteListener);
        mMediaPlayerWrapper.getPlayer().setOnInfoListener(IMPlayListener.mInfoListener);
        mMediaPlayerWrapper.getPlayer().setOnTimedTextListener(IMPlayListener.mOnTimedTextListener);
        mMediaPlayerWrapper.getPlayer().setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
                mCurrentBufferPercentage = percent;
                API.senUnityMessage("CurrentBufferPercentage|" + mCurrentBufferPercentage);

            }
        });
        mMediaPlayerWrapper.getPlayer().setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {

                mHandler.sendEmptyMessage(MESSAGE_START_COUNTDOWN);

            }
        });

        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
    }

    //开始播放
    public void openPlay(String plaUrl) {
        if (plaUrl.isEmpty()) {
            LogToFile.e("Srz  ---> 播放地址为空 ");
            return;
        }
        mMediaPlayerWrapper.openRemoteFile(plaUrl);
        mMediaPlayerWrapper.prepare();
    }

    //返回是否可用帧
    public static boolean isNewFrameAvailable() {
        return newFrameAvailable;
    }

    //更新texture
    public synchronized void UpdateTexture() {
        if (newFrameAvailable) {
            surfaceTexture.updateTexImage();
            newFrameAvailable = false;
        }
    }

    //暂停
    public void playPause() {
        if (mMediaPlayerWrapper == null) {
            LogToFile.e("Srz  --->  MediaPlayerWrapper is null");
            return;
        }
        LogToFile.e("Srz  ---> 暂停 ");
        mMediaPlayerWrapper.pause();
    }

    //继续播放
    public void playResume() {
        if (mMediaPlayerWrapper == null) {
            LogToFile.e("Srz  --->  MediaPlayerWrapper is null");
            return;
        }
        LogToFile.e("Srz  ---> 继续播放 ");
        mMediaPlayerWrapper.resume();
    }

    //停止/销毁
    public void playDestroy() {
        if (mMediaPlayerWrapper == null) {
            LogToFile.e("Srz  --->  MediaPlayerWrapper is null");
            return;
        }
        LogToFile.e("Srz  ---> 销毁播放器 ");
        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        mMediaPlayerWrapper.destroy();
        mMediaPlayerWrapper = null;

    }

    //停止播放
    public void playStop() {
        if (mMediaPlayerWrapper == null) {
            LogToFile.e("Srz  --->  MediaPlayerWrapper is null");
            return;
        }
        LogToFile.e("Srz  ---> 停止播放 ");
        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        mMediaPlayerWrapper.stop();
    }

    //设置进度
    public void playSeeto(int progress) {
        if (mMediaPlayerWrapper == null) {
            LogToFile.e("Srz  --->  MediaPlayerWrapper is null");
            return;
        }
        LogToFile.e("Srz  ---> 设置进度 " + progress);
        long duration = mMediaPlayerWrapper.getPlayer().getDuration();
        LogToFile.e("Srz  ---> 总时长： " + duration);
        long i = (long) ((duration * progress * 1.0) / 1000);
        LogToFile.e("Srz  ---> 进度 " + i);
        mMediaPlayerWrapper.getPlayer().seekTo((int) ((duration * progress * 1.0) / 1000));
    }

    //设置倍速
    public void playSpeed(float flt) {
        if (mMediaPlayerWrapper == null) {
            LogToFile.e("Srz  --->  MediaPlayerWrapper is null");
            return;
        }
        LogToFile.e("Srz  ---> 设置倍速 " + flt);
        mMediaPlayerWrapper.setSpeed(flt);
    }

    private int Volume;

    //声音++
    public void playVolumeAdd() {
        if (audioManager == null) {
            LogToFile.e("Srz  ---> AudioManager is  null ");
            return;
        }
        Volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Volume++;
        LogToFile.e("Srz  ---> 音量+  " + Volume);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                Volume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

    }

    //声音--
    public void playVolumeReduce() {
        if (audioManager == null) {
            LogToFile.e("Srz  ---> AudioManager is null ");
            return;
        }
        Volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Volume--;
        LogToFile.e("Srz  ---> 音量- " + Volume);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                Volume,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);//倍速
    }

    //获取最大音量
    public static int playVertica() {

        if (audioManager == null) {
            LogToFile.e("Srz  ---> AudioManager is null ");
            return 0;
        }
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }
    //当前音量
    public static int playVerticas() {

        if (audioManager == null) {
            LogToFile.e("Srz  ---> AudioManager  is null ");
            return 0;
        }
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    //获取当前倍速
    public float getPlaySpeed() {
        if (mMediaPlayerWrapper == null) {

            return 0;
        }
        return mMediaPlayerWrapper.getSpeel();
    }
    //播放状态
    public boolean isPlaying() {
        if (mMediaPlayerWrapper == null) {
            LogToFile.e("Srz  ---> MediaPlayerWrapper is  null ");
            return false;
        }
        return mMediaPlayerWrapper.getPlayer().isPlaying();
    }
    //判断管理类是否为空
    public boolean isMediaPlayWrapper() {
        return mMediaPlayerWrapper != null;
    }

    /***********************************************************************************************/

    private static void CheckGlError(String func) {
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            LogToFile.e("Srz  ---> CheckGlError " + func + " error: " + error);
        }
    }


    @Override
    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        newFrameAvailable = true;
//        UpdateTexture();
    }


    /**
     * 消息处理
     */
    public static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /**滑动中，同步播放进度*/
                case MESSAGE_SHOW_PROGRESS:
                    long pos = syncProgress();

                    if (!isDragging) {
                        msg = obtainMessage(MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                        IjkMediaPlayer mp = (IjkMediaPlayer) mMediaPlayerWrapper.getPlayer();
                        float fpsOutput = mp.getVideoOutputFramesPerSecond();
                        float fpsDecode = mp.getVideoDecodeFramesPerSecond();
                        long videoCachedDuration = mp.getVideoCachedDuration();
                        long audioCachedDuration = mp.getAudioCachedDuration();
                        long videoCachedBytes = mp.getVideoCachedBytes();
                        long audioCachedBytes = mp.getAudioCachedBytes();
                        long tcpSpeed = mp.getTcpSpeed();
                        long bitRate = mp.getBitRate();
                        long videoCachedPackets = mp.getVideoCachedPackets();
                        long audioCachedPackets = mp.getAudioCachedPackets();
                        long trafficStatisticByteCount = mp.getTrafficStatisticByteCount();
                        float dropFrameRate = mp.getDropFrameRate();


                        String bitm = String.valueOf((float) bitRate / (1024 * 1024));
                        int bit = bitm.indexOf(".");
                        String videoCache = String.valueOf((float) videoCachedBytes / (1024 * 1024));
                        int vi = videoCache.indexOf(".");
                        String audioCache = String.valueOf((float) audioCachedBytes / (1024 * 1024));
                        int ai = audioCache.indexOf(".");
                        String tcp = String.valueOf((float) tcpSpeed / (1024 * 1024));
                        int tc = tcp.indexOf(".");

                        API.senUnityMessage("videoMessage|" +
                                "码率: " + bitm.substring(0, bit + 2) + "/MB" +
                                "\n输出帧: " + fpsOutput +
                                "\n解码帧: " + fpsDecode +
                                "\n丢帧率: " + dropFrameRate + "%" +
                                "\nTCP速度: " + tcp.substring(0, tc + 2) + "/MB" +
                                "\n流量统计: " + (trafficStatisticByteCount / (1024 * 1024)) + "/MB" +
                                "\n视频缓存包: " + videoCachedPackets +
                                "\n音频缓存包: " + audioCachedPackets +
                                "\n视频缓存大小: " + videoCache.substring(0, vi + 2) + "/MB" +
                                "\n音频缓存大小: " + audioCache.substring(0, ai + 2) + "/MB" +
                                "\n视频缓存持续: " + generateTime(videoCachedDuration) +
                                "\n音频缓存持续: " + generateTime(audioCachedDuration));
                    }


                    break;
                case MESSAGE_START_COUNTDOWN://播放完成时，开始倒计时，抛出handler是不明确完成时是否有线程安全问题
                    API.senUnityMessage("Play finish");
                    mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
                    LogToFile.e("Srz  ---> 播放完成 ");
                    break;
            }
        }
    };


    /**
     * 同步进度
     */
    private static long syncProgress() {
        long position = mMediaPlayerWrapper.getPlayer().getCurrentPosition();
        long duration = mMediaPlayerWrapper.getPlayer().getDuration();

        API.senUnityMessage(position + "|" + duration + "|" + mMediaPlayerWrapper.getSpeel() + "|" + playVertica() + "|" + playVerticas());
        return position;
    }


    /**
     * 时长格式化显示
     */
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }


    @Override
    public void onNetDisconnected() {

        API.senUnityMessage("NetworkIsDisconnected");

    }

    @Override
    public void onNetConnected(NetworkType networkType) {

        switch (networkType) {
            case NETWORK_2G:
                LogToFile.e("Srz  ---> 2G网络 ");
                API.senUnityMessage("NetworkIsConnected-2G");
                break;
            case NETWORK_4G:
                LogToFile.e("Srz  ---> 4G网络 ");
                API.senUnityMessage("NetworkIsConnected-4G");
                break;
            case NETWORK_WIFI:
                LogToFile.e("Srz  ---> WIFI网络 ");
                API.senUnityMessage("NetworkIsConnected-WIFI");
                break;
            case NETWORK_UNKNOWN:
//                API.senUnityMessage("UnknownNetwork");
                break;
        }

    }


}
