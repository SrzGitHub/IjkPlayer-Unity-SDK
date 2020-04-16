package com.letinvr.play.normal;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.letinvr.play.AndroidAPI;
import com.letinvr.play.R;
import com.letinvr.play.SLog;
import com.letinvr.play.api.URL;
import com.letinvr.play.base.BaseActivity;
import com.letinvr.play.vr.VerticalSeekBar;
import com.letinvr.playsdk.MediaPlayerWrapper;

import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IMediaPlayer;
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
 * 2019/07/22 13:28 星期一
 **/
public class NormalPlayActivity extends BaseActivity implements View.OnClickListener, SurfaceHolder.Callback, SeekBar.OnSeekBarChangeListener,
        VerticalSeekBar.SlideChangeListener, IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnTimedTextListener, IMediaPlayer.OnVideoSizeChangedListener {


    private SurfaceView mGLSurfaceView;
    private ImageView playImgBack;
    private TextView playTvVrTitle;
    private RelativeLayout playVoiceAdd;
    private VerticalSeekBar playSeekbarVoice;
    private RelativeLayout playVoiceSub;
    private ImageView playImgNextVideo;
    private ImageView playImgStopVr;
    private ImageView playImgStartVr;
    private TextView playTvCurrentPlayTime;
    private SeekBar playSeekbarPlayVideo;
    private TextView playTvEndPlayTime;
    private RelativeLayout playTouchMenu;
    private ProgressBar playProgress;

    private SurfaceHolder holder;
    private MediaPlayerWrapper playerWrapper = new MediaPlayerWrapper();
    private Surface mSurface;


    /*** 是否在拖动进度条中，默认为停止拖动，true为在拖动中，false为停止拖动*/
    private boolean isDragging;
    /*** 播放缓冲监听*/
    private int mCurrentBufferPercentage = 0;
    /*** 同步进度*/
    private static final int MESSAGE_SHOW_PROGRESS = 1;
    /*** 5秒菜单操作，隐藏菜单面板*/
    private static final int MESSAGE_TOUCH_MENU = 3;
    /*** 开启倒计时*/
    private static final int MESSAGE_START_COUNTDOWN = 4;
    /*** 重新播放*/
    private static final int MESSAGE_PLAY_NEXT = 5;

    //当前声音大小
    private int volume;
    //设备最大音量
    private int mMaxVolume;
    //音频管理器
    private AudioManager audioManager;
    private FrameLayout mFrameLayout;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_play;
    }

    @Override
    protected void initView() {

        initViews();
        playTouchMenu.setVisibility(View.VISIBLE);
//        initMediaPlay();

    }

    private void initMediaPlay() {
        playerWrapper.init();//初始化播放器
        playerWrapper.getPlayer().setOnInfoListener(this);
        playerWrapper.getPlayer().setOnErrorListener(this);
        playerWrapper.getPlayer().setOnPreparedListener(this);
        playerWrapper.getPlayer().setOnTimedTextListener(this);
        playerWrapper.getPlayer().setOnSeekCompleteListener(this);
        playerWrapper.getPlayer().setOnBufferingUpdateListener(this);
        playerWrapper.getPlayer().setOnVideoSizeChangedListener(this);
        playerWrapper.getPlayer().setOnVideoSizeChangedListener(this);
        SLog.e("Srz  ---> 设置完成 ");

    }

    private void initViews() {
        mGLSurfaceView = findViewById(R.id.mGLSrfaceView);
        holder = mGLSurfaceView.getHolder();
        mFrameLayout = findViewById(R.id.mFrameLayout);
        playImgBack = findViewById(R.id.play_img_back);
        playTvVrTitle = findViewById(R.id.play_tv_vr_title);
        playVoiceAdd = findViewById(R.id.play_voice_add);
        playSeekbarVoice = findViewById(R.id.play_seekbar_voice);
        playVoiceSub = findViewById(R.id.play_voice_sub);
        playImgNextVideo = findViewById(R.id.play_img_next_video);
        playImgStopVr = findViewById(R.id.play_img_stop_vr);
        playImgStartVr = findViewById(R.id.play_img_start_vr);
        playTvCurrentPlayTime = findViewById(R.id.play_tv_current_play_time);
        playTvEndPlayTime = findViewById(R.id.play_tv_end_play_time);
        playSeekbarPlayVideo = findViewById(R.id.play_seekbar_play_video);
        playTouchMenu = findViewById(R.id.play_touch_menu);
        playProgress = findViewById(R.id.play_progress);

        holder.addCallback(this);
        playImgBack.setOnClickListener(this);
        playVoiceAdd.setOnClickListener(this);
        playVoiceSub.setOnClickListener(this);
        playImgNextVideo.setOnClickListener(this);
        playImgStopVr.setOnClickListener(this);
        playImgStartVr.setOnClickListener(this);
        playTouchMenu.setOnClickListener(this);


        playSeekbarPlayVideo.setMax(1000);
        playSeekbarPlayVideo.setOnSeekBarChangeListener(this);

        //不要显示进度球
        playSeekbarVoice.setThumbSize(1, 1);
        playSeekbarVoice.setUnSelectColor(Color.parseColor("#ff707070"));
        playSeekbarVoice.setSelectColor(Color.parseColor("#ffffffff"));
        playSeekbarVoice.setmInnerProgressWidth(3);
        playSeekbarVoice.setOnSlideChangeListener(this);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        playSeekbarVoice.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / mMaxVolume);
        SLog.e("Srz  ---> 初始化完成 ");

    }
//----------------------------------------------------------------------------------------------------------//

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        playerWrapper.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerWrapper.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerWrapper.resume();
    }

    //----------------------------------------------------------------------------------------------------------//
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mSurface = holder.getSurface();
        initMediaPlay();
        if (mSurface != null) {
            playerWrapper.setSurface(mSurface);
            playerWrapper.openRemoteFile(URL.VIDEO_1);
            playerWrapper.prepare();

        }
        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        SLog.e("Srz  --->  ");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        SLog.e("Srz  --->  ");
    }

    //-----------------------------------------------------------------------------------------------------------//

    @Override
    public void onPrepared(IMediaPlayer mp) {
        SLog.e("Srz  --->  ");
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mHandler.sendEmptyMessage(MESSAGE_START_COUNTDOWN);
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        mCurrentBufferPercentage = percent;
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        SLog.e("Srz  --->  ");
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {

        SLog.e("Srz  --->  " + width + "*" + height + "*" + sar_num + "*" + sar_den);

    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {

        String messageId;

        if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            messageId = "invalid progressive playback";
            SLog.e("Srz  --->  " + messageId);

        } else {
            messageId = "unknown";
            SLog.e("Srz  --->  " + messageId);

        }


        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int arg2) {

        switch (what) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                SLog.e("MEDIA_INFO_VIDEO_TRACK_LAGGING:" + arg2);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                SLog.e("MEDIA_INFO_VIDEO_RENDERING_START:" + arg2);//开始播放

                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                SLog.e("MEDIA_INFO_BUFFERING_START:" + arg2);//开始缓冲

                isDragging = true;
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                SLog.e("MEDIA_INFO_BUFFERING_END:");//缓冲结束

                isDragging = false;
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                SLog.e("MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);//带宽信息
                break;
            case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                SLog.e("MEDIA_INFO_BAD_INTERLEAVING:" + arg2);//媒体文件损坏

                break;
            case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                SLog.e("MEDIA_INFO_NOT_SEEKABLE:" + arg2);//
                break;
            case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                SLog.e("MEDIA_INFO_METADATA_UPDATE:" + arg2);
                break;
            case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                SLog.e("MEDIA_INFO_UNSUPPORTED_SUBTITLE:" + arg2);
                break;
            case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                SLog.e("MEDIA_INFO_SUBTITLE_TIMED_OUT:" + arg2);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                SLog.e("MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);//视频旋转
                break;
            case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                SLog.e("MEDIA_INFO_AUDIO_RENDERING_START:" + arg2);//音频渲染开始
                break;
        }
        return true;
    }

    @Override
    public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
        SLog.e("Srz  --->  " + text.getText());
    }

    //----------------------------------------------------------------------------------------/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        long duration = playerWrapper.getPlayer().getDuration();
        int position = (int) ((duration * progress * 1.0) / 1000);
        String time = generateTime(position);
        playTvCurrentPlayTime.setText(time);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isDragging = true;
        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long duration = playerWrapper.getPlayer().getDuration();
        Log.e("Srz", "seekBar.getProgress() = " + seekBar.getProgress());
        playerWrapper.getPlayer().seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));

        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        isDragging = false;
        mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        hasMenuAction();
    }

    //------------------------------------------------------------------------------------------/
    @Override
    public void onStart(VerticalSeekBar slideView, int progress) {

    }

    @Override
    public void onProgress(VerticalSeekBar slideView, int progress) {
        volume = (int) (mMaxVolume * progress * 0.01);
        if (volume > mMaxVolume)
            volume = mMaxVolume;
        else if (volume < 0)
            volume = 0;
        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    @Override
    public void onStop(VerticalSeekBar slideView, int progress) {

    }

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
                    if (!isDragging) {
                        msg = obtainMessage(MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                /**隐藏菜单面板*/
                case MESSAGE_TOUCH_MENU:
                    playTouchMenu.setVisibility(View.GONE);
                    //防止点触面引起的显示冲突
                    break;
                case MESSAGE_PLAY_NEXT:
                    playNext();
                    break;
                case MESSAGE_START_COUNTDOWN://播放完成时，开始倒计时，抛出handler是不明确完成时是否有线程安全问题
                    playNext();
                    break;
            }
        }
    };

    /**
     * 同步进度
     */
    private long syncProgress() {
        if (isDragging) {
            return 0;
        }
        long position = playerWrapper.getPlayer().getCurrentPosition();
        long duration = playerWrapper.getPlayer().getDuration();
        if (playSeekbarPlayVideo != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                playSeekbarPlayVideo.setProgress((int) pos);
            }
            int percent = mCurrentBufferPercentage;
            playSeekbarPlayVideo.setSecondaryProgress(percent * 10);
        }

        playTvCurrentPlayTime.setText(generateTime(position));
        playTvEndPlayTime.setText(generateTime(duration));
        playSeekbarVoice.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / mMaxVolume);

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

    //有菜单操作
    public void hasMenuAction() {
        if (isDragging) {
            mHandler.removeMessages(MESSAGE_TOUCH_MENU);
        } else {
            mHandler.removeMessages(MESSAGE_TOUCH_MENU);
            mHandler.sendEmptyMessageDelayed(MESSAGE_TOUCH_MENU, 5000);


        }
    }

    int curProgress;


    private void playNext() {
        playerWrapper.pause();
        playerWrapper.destroy();
        playerWrapper.init();
        playerWrapper.setSurface(mSurface);
        playerWrapper.openRemoteFile(AndroidAPI.LT_PLAYER);
        playerWrapper.prepare();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_img_back:
                finish();
                break;
            case R.id.play_voice_add:
                curProgress = playSeekbarVoice.getProgress();
                Log.d("vrdemo", "音量增加=" + playSeekbarVoice.getProgress());
                if (curProgress < 100) {
                    curProgress = curProgress + 5;
                    playSeekbarVoice.setProgress(curProgress);
                }
                volume = (int) (mMaxVolume * curProgress * 0.01);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                hasMenuAction();
                break;
            case R.id.play_voice_sub:
                Log.d("vrdemo", "音量减少=" + playSeekbarVoice.getProgress());
                curProgress = playSeekbarVoice.getProgress();
                if (curProgress > 0) {
                    curProgress = curProgress - 5;
                    playSeekbarVoice.setProgress(curProgress);
                }
                volume = (int) (mMaxVolume * curProgress * 0.01);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                hasMenuAction();
                break;
            case R.id.play_img_next_video:
                playNext();
                break;
            case R.id.play_img_stop_vr:
                playImgStopVr.setVisibility(View.GONE);
                playImgStartVr.setVisibility(View.VISIBLE);
                playerWrapper.pause();
                mHandler.removeMessages(MESSAGE_TOUCH_MENU);
                break;
            case R.id.play_img_start_vr:
                playImgStartVr.setVisibility(View.GONE);
                playImgStopVr.setVisibility(View.VISIBLE);
                playerWrapper.resume();
                hasMenuAction();
                break;
            case R.id.play_touch_menu:
                playTouchMenu.setGravity(View.GONE);
                mHandler.removeMessages(MESSAGE_TOUCH_MENU);
                break;
        }
    }
}
