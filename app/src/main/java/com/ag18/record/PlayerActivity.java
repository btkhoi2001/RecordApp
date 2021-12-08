package com.ag18.record;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {
    Button btnPlay, btnNext, btnPrevious, btnForwardLeft, btnForwardRight;
    TextView txtRecordingName, txtStart, txtStop;
    SeekBar seekBar;
    BarVisualizer visualizer;
    String recordingName;
    public static final String EXTRA_NAME = "recording_name";
    static MediaPlayer mediaPlayer;
    int position;
    ImageView imageView;
    Thread updateSeekbar;
    ArrayList<File> myRecordings;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnForwardLeft = findViewById(R.id.btnForwadLeft);
        btnForwardRight = findViewById(R.id.btnForwadRight);
        txtRecordingName = findViewById(R.id.txtsn);
        txtStart = findViewById(R.id.txtStart);
        txtStop = findViewById(R.id.txtStop);
        seekBar = findViewById(R.id.seekbar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imageview);
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        myRecordings = (ArrayList) bundle.getParcelableArrayList("recordings");
        String rcdName = i.getStringExtra("recordingName");
        position = bundle.getInt("pos", 0);
        txtRecordingName.setSelected(true);

        Uri uri = Uri.parse(myRecordings.get(position).toString());
        recordingName = myRecordings.get(position).getName();
        txtRecordingName.setText(recordingName);

        mediaPlayer = mediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeekbar = new Thread(){
            @Override
            public  void run(){
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while( currentPosition < totalDuration){
                    try{
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPink),
                PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPink), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtStop.setText(endTime);

        final  Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtStart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1){
            visualizer.setAudioSessionId(audioSessionId);
        }

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else{
                    btnPlay.setBackgroundResource(R.drawable.ic_pause_circle);
                    mediaPlayer.start();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)% myRecordings.size());
                Uri u = Uri.parse(myRecordings.get(position).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(), u);
                recordingName = myRecordings.get(position).getName();
                txtRecordingName.setText(recordingName);
                mediaPlayer.start();
                seekBar.setProgress(0);
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1 < 0)? myRecordings.size() - 1 : position - 1);
                Uri u = Uri.parse(myRecordings.get(position).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(), u);
                recordingName = myRecordings.get(position).getName();
                txtRecordingName.setText(recordingName);
                mediaPlayer.start();
                seekBar.setProgress(0);
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(imageView);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });



        btnForwardRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    final int newTime = mediaPlayer.getCurrentPosition()+5000;
                    seekBar.setProgress(newTime);
                    mediaPlayer.seekTo(newTime);
                }
            }
        });
        btnForwardLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    final int newTime = (mediaPlayer.getCurrentPosition()-5000 < 0) ? 0 : mediaPlayer.getCurrentPosition()-5000 ;
                    seekBar.setProgress(newTime);
                    mediaPlayer.seekTo(newTime);
                }
            }
        });
        buttonEffect(btnPlay);
        buttonEffect(btnForwardLeft);
        buttonEffect(btnForwardRight);
        buttonEffect(btnNext);
        buttonEffect(btnPrevious);
    }

    public  void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time += min + ":";
        if(sec < 10){
            time += "0";
        }
        time += sec;
        return time;
    }
    public static void buttonEffect(View button){
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0f47521,PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }
}
