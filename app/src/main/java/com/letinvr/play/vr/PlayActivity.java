package com.letinvr.play.vr;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.letinvr.play.R;
import com.letinvr.play.api.URL;
import com.letinvr.play.model.VideoModel;
import com.letinvr.playsdk.MediaPlayerWrapper;
import com.letinvr.playsdk.util.LogToFile;

import tv.danmaku.ijk.media.player.IMediaPlayer;


public class PlayActivity extends M360PlayerActivity implements View.OnClickListener {

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
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();

    private int current;
    //显示菜单的全屏区域
    private RelativeLayout touchRl;
    //音量的seekbar
    private VerticalSeekBar verticalSeekBar;
    private RelativeLayout addVoice;
    private RelativeLayout subVoice;
    private Button btn_vr_nor;//全景模式
    private Button btn_vr_glass; //眼镜模式
    private ImageView img_stop_vr; //停止vr按钮
    private ImageView img_start_vr;//继续vr按钮
    private TextView endTimeTv; //总时长
    private TextView currentTimeTv; //当前时长
    private SeekBar playSeekbar; //视频播放进度
    private TextView titleTv;  //视频标题
    private ImageView img_back; //返回键
    private ImageView img_next_video; //下一部视频
    //当前声音大小
    private int volume;
    //设备最大音量
    private int mMaxVolume;
    //音频管理器
    private AudioManager audioManager;
    /**
     * 播放总时长
     */

    private VideoModel videoModel;
    private int playType = MDVRLibrary.DISPLAY_MODE_NORMAL;


    private boolean isFirstGlass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        videoModel= (VideoModel) getIntent().getSerializableExtra("videoModel");
        videoModel = new VideoModel(1, "name1", "", URL.VIDEO_1);
        playType = getIntent().getIntExtra("playType", MDVRLibrary.DISPLAY_MODE_NORMAL);
        initView();

        //初始化页面动画
        touchRl.setVisibility(View.VISIBLE);
        initMediaPlayer();
        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);

    }

    //初始化播放器
    private void initMediaPlayer() {
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                cancelBusy();
                if (getVRLibrary() != null) {
                    getVRLibrary().notifyPlayerChanged();
                }
                if (isFirstGlass) {
                    isFirstGlass = false;
                    mMediaPlayerWrapper.pause();
                }
            }
        });
        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d", what, extra);
                Toast.makeText(PlayActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {


                getVRLibrary().onTextureResize(width, height);
            }
        });

        if (videoModel.getVideoUrl() != null) {
            mMediaPlayerWrapper.openRemoteFile(videoModel.getVideoUrl());
            mMediaPlayerWrapper.prepare();
        }
        mMediaPlayerWrapper.getPlayer().setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
                mCurrentBufferPercentage = percent;
            }
        });
        mMediaPlayerWrapper.getPlayer().setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                mHandler.sendEmptyMessage(MESSAGE_START_COUNTDOWN);

            }
        });
    }

    //初始化页面菜单功能
    private void initView() {
        img_back = findViewById(R.id.img_back);
        img_back.setOnClickListener(this);
        touchRl = findViewById(R.id.rl_touch_menu);
        touchRl.setOnClickListener(this);
        verticalSeekBar = findViewById(R.id.seekbar_voice);
        addVoice = findViewById(R.id.rl_voice_add);
        subVoice = findViewById(R.id.rl_voice_sub);
        addVoice.setOnClickListener(this);
        subVoice.setOnClickListener(this);
        //不要显示进度球
        verticalSeekBar.setThumbSize(1, 1);
        verticalSeekBar.setUnSelectColor(Color.parseColor("#ff707070"));
        verticalSeekBar.setSelectColor(Color.parseColor("#ffffffff"));
        //单位px
        verticalSeekBar.setmInnerProgressWidth(3);
        verticalSeekBar.setOnSlideChangeListener(slideChangeListener);
        titleTv = findViewById(R.id.tv_vr_title);
        titleTv.setText(videoModel.getName());

        btn_vr_nor = findViewById(R.id.btn_vr_nor);
        btn_vr_glass = findViewById(R.id.btn_vr_glass);
        btn_vr_nor.setOnClickListener(this);
        btn_vr_glass.setOnClickListener(this);
        img_stop_vr = findViewById(R.id.img_stop_vr);
        img_start_vr = findViewById(R.id.img_start_vr);
        img_stop_vr.setOnClickListener(this);
        img_start_vr.setOnClickListener(this);
        endTimeTv = findViewById(R.id.tv_end_play_time);
        currentTimeTv = findViewById(R.id.tv_current_play_time);
        img_next_video = findViewById(R.id.img_next_video);
        img_next_video.setOnClickListener(this);

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        verticalSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / mMaxVolume);

        playSeekbar = findViewById(R.id.seekbar_play_video);
        playSeekbar.setMax(1000);
        playSeekbar.setOnSeekBarChangeListener(mSeekListener);
        if (playType == MDVRLibrary.DISPLAY_MODE_GLASS) {
            //因为默认是360全景，所有只有当playType为眼镜模式时,才需要改变默认.在M360PlayerActivity中，createVRLibrary带不过参数，所有是先设置全景，再改变
            LogToFile.e("Srz  --->  ");
            changeToglass();
        }


    }


    @Override
    protected MDVRLibrary createVRLibrary() {

        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL) //默认360度全景
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH)//触摸和重力
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(PlayActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(true)
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        Log.e("Srz", "index = " + index);
                        return MD360Director.builder().setPitch(90).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory()).listenGesture(new MDVRLibrary.IGestureListener() {
                    @Override
                    public void onClick(MotionEvent e) {
                        touchRl.setVisibility(View.VISIBLE);
                        if (mMediaPlayerWrapper.getPlayer().isPlaying()) {
                            img_start_vr.setVisibility(View.GONE);
                            img_stop_vr.setVisibility(View.VISIBLE);
                        } else {
                            img_start_vr.setVisibility(View.VISIBLE);
                            img_stop_vr.setVisibility(View.GONE);
                        }
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
                        hasMenuAction();
                        verticalSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / mMaxVolume);
                    }
                })
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .build(findViewById(R.id.gl_view));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Srz", "onDestroy");
        mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        mMediaPlayerWrapper.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("Srz", "onPause");
        mMediaPlayerWrapper.pause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayerWrapper.resume();
    }

    int curProgress;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.rl_touch_menu://点击菜单无点击事件的区域，菜单消失
                touchRl.setVisibility(View.GONE);
                mHandler.removeMessages(MESSAGE_TOUCH_MENU);
                break;
            case R.id.rl_voice_add://增加音量
                curProgress = verticalSeekBar.getProgress();
                Log.d("vrdemo", "音量增加=" + verticalSeekBar.getProgress());
                if (curProgress < 100) {
                    curProgress = curProgress + 5;
                    verticalSeekBar.setProgress(curProgress);
                }
                volume = (int) (mMaxVolume * curProgress * 0.01);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                hasMenuAction();
                break;
            case R.id.rl_voice_sub://减少音量
                Log.d("vrdemo", "音量减少=" + verticalSeekBar.getProgress());
                curProgress = verticalSeekBar.getProgress();
                if (curProgress > 0) {
                    curProgress = curProgress - 5;
                    verticalSeekBar.setProgress(curProgress);
                }
                volume = (int) (mMaxVolume * curProgress * 0.01);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                hasMenuAction();
                break;
            case R.id.btn_vr_nor://360全景模式
                if (playType != MDVRLibrary.DISPLAY_MODE_NORMAL) {
                    changeTo360nor();
                }
                hasMenuAction();
                break;
            case R.id.btn_vr_glass://眼镜模式
                if (playType != MDVRLibrary.DISPLAY_MODE_GLASS) {
                    changeToglass();
                }
                hasMenuAction();
                break;
            case R.id.img_stop_vr://暂停
                img_stop_vr.setVisibility(View.GONE);
                img_start_vr.setVisibility(View.VISIBLE);
                mMediaPlayerWrapper.pause();
                mHandler.removeMessages(MESSAGE_TOUCH_MENU);
                break;
            case R.id.img_start_vr://继续播放
                img_start_vr.setVisibility(View.GONE);
                img_stop_vr.setVisibility(View.VISIBLE);
                mMediaPlayerWrapper.resume();
                hasMenuAction();
                break;
            case R.id.img_back://返回
                finish();
                break;
            case R.id.img_next_video://下一部
                playNext();
                break;
        }
    }

    private void playNext() {
//        //获取下一步内容
//        //TODO 获取新片资源
//        if(videoModel.getId()==15){
//            videoModel=MainActivity.videoModels.get(0);
//        }else {
//            videoModel=MainActivity.videoModels.get(videoModel.getId());
//        }
//        titleTv.setText(videoModel.getName());
//        mMediaPlayerWrapper.pause();
//        mMediaPlayerWrapper.destroy();
//        mMediaPlayerWrapper.init();
//        mMediaPlayerWrapper.openRemoteFile(videoModel.getVideoUrl());
//        mMediaPlayerWrapper.prepare();
    }


    //切换成360全景模式
    public void changeTo360nor() {
        playType = MDVRLibrary.DISPLAY_MODE_NORMAL;
        getVRLibrary().switchDisplayMode(PlayActivity.this, MDVRLibrary.DISPLAY_MODE_NORMAL);
    }

    public void changeToglass() {
        playType = MDVRLibrary.DISPLAY_MODE_GLASS;
        getVRLibrary().switchDisplayMode(PlayActivity.this, MDVRLibrary.DISPLAY_MODE_GLASS);
    }

    private VerticalSeekBar.SlideChangeListener slideChangeListener = new VerticalSeekBar.SlideChangeListener() {
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
    };

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

    /**
     * 进度条滑动监听
     */
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        /**数值的改变*/
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            long duration = mMediaPlayerWrapper.getPlayer().getDuration();
            int position = (int) ((duration * progress * 1.0) / 1000);
            String time = generateTime(position);
            currentTimeTv.setText(time);

        }

        /**开始拖动*/
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
        }

        /**停止拖动*/
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            long duration = mMediaPlayerWrapper.getPlayer().getDuration();
            Log.e("Srz", "seekBar.getProgress() = " + seekBar.getProgress());
            mMediaPlayerWrapper.getPlayer().seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));

            mHandler.removeMessages(MESSAGE_SHOW_PROGRESS);
            isDragging = false;
            mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
            hasMenuAction();
        }
    };

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
                    touchRl.setVisibility(View.GONE);
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
        long position = mMediaPlayerWrapper.getPlayer().getCurrentPosition();
        long duration = mMediaPlayerWrapper.getPlayer().getDuration();
        if (playSeekbar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                playSeekbar.setProgress((int) pos);
            }
            int percent = mCurrentBufferPercentage;
            playSeekbar.setSecondaryProgress(percent * 10);
        }

        currentTimeTv.setText(generateTime(position));
        endTimeTv.setText(generateTime(duration));
        verticalSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / mMaxVolume);

        return position;
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

}
