package com.ag18.record;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btnPlay, btnNext, btnPrevious, btnForwardLeft, btnForwardRight;
    TextView txtRecordingName, txtStart, txtStop;
    SeekBar seekBar;
    BarVisualizer visualizer;
    String recordingName;
    public static final String EXTRA_NAME = "recording_name";
    static MediaPlayer mediaPlayer;
    int position;

    ArrayList<File> myRecordings;

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

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    mediaPlayer.pause();
                }
                else{
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.start();
                }
            }
        });
    }
}
