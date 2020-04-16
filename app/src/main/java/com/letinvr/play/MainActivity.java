package com.letinvr.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


import com.letinvr.playsdk.androidplay.AndroidPlayManager;
import com.letinvr.playsdk.util.LogToFile;
import com.letinvr.playsdk.util.LogUtil;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    /**
     * normal
     */
    private boolean isDragging;
    private Button mPause;
    private Button mStop;
    private SeekBar mSeekBar;
    private TextView mMaxDuration;
    public static final int MASSSSSSSSSSS = 110;
    private int mCurrentBufferPercentage;
    AndroidPlayManager playWrapper = new AndroidPlayManager();
    private boolean isPlaying;
    private Button mNext;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder;
    private TextView mMsg;

    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MASSSSSSSSSSS:
                    long pos = syncProgress();
                    if (!isDragging) {
                        msg = obtainMessage(MASSSSSSSSSSS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));

                    }
                    break;
            }
        }
    };

    BroadcastReceiver receiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(com.letinvr.playsdk.API.PLAY_DETAILS)){
                String stringExtra = intent.getStringExtra(com.letinvr.playsdk.API.PLAY_KEY);
                mMsg.setText(stringExtra);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        mSurfaceView = findViewById(R.id.mSurfaceView);
        holder = mSurfaceView.getHolder();
        holder.addCallback(callback);

        mPause = findViewById(R.id.mPause);
        mStop = findViewById(R.id.mStop);
        mNext = findViewById(R.id.mNext);
        mSeekBar = findViewById(R.id.mSeekBar);
        mMaxDuration = findViewById(R.id.mMaxDuration);
        mMsg = findViewById(R.id.mMsg);
        mMsg.getBackground().setAlpha(100);
        mPause.getBackground().setAlpha(100);
        mStop.getBackground().setAlpha(100);
        mNext.getBackground().setAlpha(100);
        mSeekBar.setMax(1000);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
        mPause.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mNext.setOnClickListener(this);

        IntentFilter filter =new IntentFilter();
        filter.addAction(com.letinvr.playsdk.API.PLAY_DETAILS);
        registerReceiver(receiver,filter);

    }

    SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            long duration = playWrapper.mMediaPlayerWrapper.getPlayer().getDuration();
            int position = (int) ((duration * progress * 1.0) / 1000);
            String time = generateTime(position);
            mMaxDuration.setText(time + "/00:00");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            isDragging = true;
            mHandler.removeMessages(MASSSSSSSSSSS);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isDragging = false;
            mHandler.sendEmptyMessageAtTime(MASSSSSSSSSSS, 1000);
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
     * 同步进度
     */
    private long syncProgress() {

        if (isDragging){
            return 0;
        }
        IjkMediaPlayer androidPlay = playWrapper.getAndroidPlay();
        if (androidPlay==null){
            return 0;
        }

        long position = androidPlay.getCurrentPosition();
        long duration = androidPlay.getDuration();
        if (mSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            int percent = mCurrentBufferPercentage;
            mSeekBar.setSecondaryProgress(percent * 10);
        }

        mMaxDuration.setText(generateTime(position) + "/" + generateTime(duration));


        return position;
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MASSSSSSSSSSS);
        playWrapper.playPause();
        isPlaying=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playWrapper.playDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

      if (playWrapper!=null&&!isPlaying){
          mHandler.sendEmptyMessage(MASSSSSSSSSSS);
          playWrapper.playResume();
          isPlaying=true;
      }
    }



    private Surface surface;
    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {


        @Override
        public void surfaceCreated(SurfaceHolder holder) {


            surface = holder.getSurface();

            playWrapper.initAndroidPlay(surface, MainActivity.this);


            playWrapper.openPlay(AndroidAPI.HTTPS_PLAYE);
            SLog.e("Srz  ---> 开始播放............... ");
            mHandler.sendEmptyMessage(MASSSSSSSSSSS);
            playWrapper.mMediaPlayerWrapper.getPlayer().setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {


                @Override
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            });
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            LogUtil.e("Srz  ---> 变化 ");

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

//            playWrapper.playDestroy();

        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {




        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mPause:
                if (isPlaying) {
                    playWrapper.playPause();
                    isPlaying = false;
                } else {
                    playWrapper.playResume();
                    isPlaying = true;
                }

                break;
            case R.id.mStop:
                playWrapper.playDestroy();
                isPlaying = false;
                break;
            case R.id.mNext:


                break;

        }
    }
}
