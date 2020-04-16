package com.letinvr.playsdk.androidplay;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import com.letinvr.playsdk.API;
import com.letinvr.playsdk.util.LogUtil;
import com.letinvr.playsdk.MediaPlayerWrapper;

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
public class AndroidPlayManager {



    /**
     * 是否在拖动进度条中，默认为停止拖动，true为在拖动中，false为停止拖动
     */
    private boolean isDragging;
    /**
     * 播放缓冲监听
     */
    private int mCurrentBufferPercentage = 0;
    /**
     * 同步进度
     */
    private static final int MESSAGE_SHOW_PROGRESS = 1;
    /**
     * 5秒菜单操作，隐藏菜单面板
     */
    private static final int MESSAGE_TOUCH_MENU = 3;
    /**
     * 开启倒计时
     */
    private static final int MESSAGE_START_COUNTDOWN = 4;
    /**
     * 重新播放
     */
    private static final int MESSAGE_PLAY_NEXT = 5;
    public MediaPlayerWrapper mMediaPlayerWrapper;

    private static Surface mSurface;
    private AudioManager audioManager;
    private LogUtil logUtil;
    private Context mContext;

    public AndroidPlayManager(){
        mMediaPlayerWrapper = new MediaPlayerWrapper();
    }


    //初始化Android播放器
    public void initAndroidPlay(Surface surface, Context context) {
        mSurface = surface;
        mContext =context;
        logUtil = new LogUtil(context);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setSurface(mSurface);

        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                LogUtil.e("Srz  ---> onPrepared ");
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                LogUtil.e("Srz  ---> onError " + String.format("Play Error what=%d ectra=%d", what, extra));
                return true;
            }
        });
        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                LogUtil.e("Srz  ---> onVideoSizeChanged " + String.format("w=%d h=%d num=%d den=%d ", width, height, sar_num, sar_den));
            }
        });
        mMediaPlayerWrapper.getPlayer().setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
                mCurrentBufferPercentage = percent;
                LogUtil.e("Srz  ---> onBufferingUpdate " + mCurrentBufferPercentage);
            }
        });
        mMediaPlayerWrapper.getPlayer().setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                mHandler.sendEmptyMessage(MESSAGE_START_COUNTDOWN);
                LogUtil.e("Srz  ---> onCompletion ");
            }
        });
        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
    }
    public IjkMediaPlayer getAndroidPlay(){

        if(mMediaPlayerWrapper!=null)
        return (IjkMediaPlayer) mMediaPlayerWrapper.getPlayer();
        else
            return null;
    }

    //开始播放
    public void openPlay(String plaUrl) {
        if (plaUrl.isEmpty()) {
            LogUtil.e("Srz  ---> 播放地址为空 ");
            return;
        }
        mMediaPlayerWrapper.openRemoteFile(plaUrl);

        mMediaPlayerWrapper.prepare();
    }

    //下一首
    public void playNext(String url) {

        mMediaPlayerWrapper.pause();
        mMediaPlayerWrapper.destroy();
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.openRemoteFile(url);
        mMediaPlayerWrapper.prepare();
    }

    //暂停
    public void playPause() {
        if (mMediaPlayerWrapper == null) {
            return;
        }
        mMediaPlayerWrapper.pause();
    }

    //继续播放
    public void playResume() {
        if (mMediaPlayerWrapper == null) {
            return;
        }
        LogUtil.e("Srz  ---> 继续播放 ");
        mMediaPlayerWrapper.resume();
    }

    //停止/销毁
    public void playDestroy() {
        if (mMediaPlayerWrapper == null) {
            return;
        }
        LogUtil.e("Srz  ---> 销毁播放器 ");
        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        mMediaPlayerWrapper.destroy();
    }

    //停止播放
    public void playStop() {
        if (mMediaPlayerWrapper == null) {
            return;
        }
        LogUtil.e("Srz  ---> 停止播放 ");
        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        mMediaPlayerWrapper.stop();
    }

    //设置进度
    public void playSeeto(int progress) {
        if (mMediaPlayerWrapper == null) {
            return;
        }
        LogUtil.e("Srz  ---> 设置进度 " + progress);
        long duration = mMediaPlayerWrapper.getPlayer().getDuration();
        mMediaPlayerWrapper.getPlayer().seekTo((int) ((duration * progress * 1.0) / 1000));
    }

    //设置倍速
    public void plaSpeed(float flt) {
        LogUtil.e("Srz  ---> 设置倍速 " + flt);
        mMediaPlayerWrapper.setSpeed(flt);
    }

    private int VolumeAdd;

    //声音++
    public void playVolumeAdd() {

        VolumeAdd = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        VolumeAdd++;
        LogUtil.e("Srz  ---> 音量+  " + VolumeAdd);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                VolumeAdd,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

    }

    //声音--
    public void playVolumeReduce() {
        VolumeAdd = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        VolumeAdd--;
        LogUtil.e("Srz  ---> 音量- " + VolumeAdd);
        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                VolumeAdd,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    public int playVertica() {

        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    }
    /***********************************************************************************************/
    /**
     * 消息处理
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /**滑动中，同步播放进度*/
                case MESSAGE_SHOW_PROGRESS:
                    long pos = syncProgress();
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
                    sendBroadcastMsg(
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

                    break;
                /**隐藏菜单面板*/
                case MESSAGE_TOUCH_MENU:
//                    touchRl.setVisibility(View.GONE);
                    //防止点触面引起的显示冲突
                    break;
                case MESSAGE_PLAY_NEXT:

                    LogUtil.e("Srz  ---> next ");
                    break;
                case MESSAGE_START_COUNTDOWN://播放完成时，开始倒计时，抛出handler是不明确完成时是否有线程安全问题

                    API.senUnityMessage("Play finish");
                    mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
                    LogUtil.e("Srz  ---> 播放完成 ");
                    break;
            }
        }

        private void sendBroadcastMsg(String msg) {
            Intent intent =new Intent();
            intent.setAction(API.PLAY_DETAILS);
            intent.putExtra(API.PLAY_KEY,msg);
            mContext.sendBroadcast(intent);
        }
    };



    /**
     * 同步进度
     */
    private long syncProgress() {
        if (isDragging) {
            return 0;
        }

        long position = mMediaPlayerWrapper.getPlayer().getCurrentPosition();
        long duration = mMediaPlayerWrapper.getPlayer().getDuration();
        if (duration > 0) {
            long pos = 1000L * position / duration;
        }
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

//        API.senUnityMessage(generateTime(position) + "/" + generateTime(duration) + "|" + mMediaPlayerWrapper.getSpeel()+"|"+playVertica()+"|"+streamVolume);
        return position;
    }


    /**
     * 时长格式化显示
     */
    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }


}
