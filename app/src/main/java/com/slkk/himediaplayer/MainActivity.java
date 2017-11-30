package com.slkk.himediaplayer;

import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = "MainActivity";
    @BindView(R.id.video_surface)
    SurfaceView videoSurface;
    @BindView(R.id.skb_video)
    SeekBar skbVideo;
    @BindView(R.id.btn_play)
    Button btnPlay;
    @BindView(R.id.btn_pause)
    Button btnPause;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.video_current_position)
    TextView videoCurrentPosition;

    private MediaPlayer player;
    private int seekProgress;
    private SurfaceHolder surfaceholder;
    private String videoPath = "/sdcard/test.mp4";
    private VideoProgressUpdateTask videoprogressupdatetask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        Log.i(TAG, "initView: ");
        skbVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, "onProgressChanged: progress:" + progress);
                Log.i(TAG, "onProgressChanged: player :" + player.getDuration());
                seekProgress = progress * (player.getDuration() / seekBar.getMax());
                Log.i(TAG, "onProgressChanged: seekProgress :" + seekProgress);
                Log.i(TAG, "onProgressChanged: seekBar.Max :" + seekBar.getMax());
                seekTo(seekProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "onStartTrackingTouch: ");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "onStopTrackingTouch: ");
                seekTo(seekProgress);
            }
        });

        surfaceholder = videoSurface.getHolder();
        surfaceholder.setFormat(PixelFormat.TRANSPARENT);
        surfaceholder.setKeepScreenOn(true);
        surfaceholder.addCallback(this);
        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    /**
     * 停止播放
     */
    private void stop() {
        if (player == null) {
            return;
        }
        player.stop();
    }

    /**
     * 释放资源
     */
    private void release() {
        if (player == null) {
            return;
        }
        player.release();
    }

    /**
     * 重置播放器
     */
    private void reset() {
        if (player == null) {
            return;
        }
        player.reset();
    }

    /**
     * 指定位置播放
     *
     * @param pos
     */
    private void seekTo(int pos) {
        if (player == null) {
            return;
        }
        player.seekTo(pos);
        start();
    }

    /**
     * 开始播放
     */
    private void start() {
        if (player == null || player.isPlaying()) {
            return;
        }
        player.start();
    }

    /**
     * 暂停播放
     */
    private void pause() {
        if (player == null || !player.isPlaying()) {
            return;
        }
        player.pause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated: ");
        autoPlay();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: ");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceChanged: ");
        stop();
        release();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        release();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                Log.i(TAG, "onClick: play");
                start();
                break;
            case R.id.btn_pause:
                Log.i(TAG, "onClick: pause");
                pause();
                break;
            case R.id.btn_stop:
                Log.i(TAG, "onClick: stop");
                stop();
                break;
        }
    }

    /**
     * 自动播放
     */
    private void autoPlay() {
        Log.i(TAG, "autoPlay: ");
        //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //设置显示视频显示在SurfaceView上
        player.setDisplay(surfaceholder);
        try {
            int currentPosition = player.getCurrentPosition();
            player.setDataSource(videoPath);
            Log.i(TAG, "currentPosition: " + currentPosition);
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnBufferingUpdateListener(this);
            player.prepare();
            int duration = player.getDuration();
            videoprogressupdatetask = new VideoProgressUpdateTask();
            videoprogressupdatetask.execute(duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        skbVideo.setProgress(0);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.i(TAG, "onBufferingUpdate: ");
        skbVideo.setSecondaryProgress(percent);
        int currentProgress = skbVideo.getMax() * player.getCurrentPosition() / player.getDuration();
        Log.e("currentProgress", "currentProgress----->" + currentProgress);
    }

    private class VideoProgressUpdateTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... values) {
            int currentPosition = 0;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG, "run: ");
                    publishProgress();
                }
            }, 0, 1000);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int currentPosition = player.getCurrentPosition();
            Log.i(TAG, "onProgressUpdate: "+currentPosition);
            String timeCurrent = "00:00:00";
            if(currentPosition<60000){
                timeCurrent = "00:"+"00:"+currentPosition/1000;
            }else if(currentPosition <600000){
                int mm = currentPosition / 1000 / 60;
                int ss = currentPosition/1000 - mm*60;
                timeCurrent = "00:"+mm+":"+ss;
            }else{
                int hh = currentPosition/1000/60/60;
                int mm = currentPosition / 1000 / 60-hh*60;
                int ss = currentPosition/1000- hh*60*60-mm*60;
                timeCurrent = hh+":"+mm+":"+ss;
            }
            videoCurrentPosition.setText(timeCurrent);

        }
    }
}
