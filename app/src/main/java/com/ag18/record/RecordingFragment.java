package com.ag18.record;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.FileNotFoundException;
import java.io.IOException;


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class RecordingFragment extends Fragment {
    private static final int RECORDER_SAMPLE_RATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private ImageButton btnStop, btnPause;
    private Button btnCancel;

    private Chronometer chronometer;
    private NavController navController;

    boolean isRecording = true;
    private AudioRecord audioRecord = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;

    private View view = null;
    private String externalStorage = System.getenv("EXTERNAL_STORAGE") + "/RecordApp";

    private long timeWhenStopped = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recording, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        btnStop = view.findViewById(R.id.btn_stop);
        btnPause = view.findViewById(R.id.btn_pause);
        btnCancel = view.findViewById(R.id.btn_cancel);

        chronometer = view.findViewById(R.id.chronometer);
        navController = Navigation.findNavController(view);
        
        setListener();
        startRecording(false);
    }

    private void setListener() {
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording(false);
                    btnPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                }
                else {
                    startRecording(true);
                    btnPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording(true);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).popBackStack();
            }
        });
    }

    private void startRecording(final boolean b) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Navigation.findNavController(view).popBackStack();
            return;
        }

        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chronometer.start();

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
        audioRecord.startRecording();
        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile(b);
            }
        });

        recordingThread.start();
    }

    private void stopRecording(boolean b) {
        if (audioRecord != null) {
            chronometer.stop();
            timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();

            isRecording = false;

            audioRecord.stop();
            audioRecord.release();

            audioRecord = null;
            recordingThread = null;
        }

        if (b) {
            navController.navigate(R.id.action_recordingFragment_to_voiceFilterFragment);
        }
    }

    private String getTempFilename() {
        File file = new File(externalStorage + "/.tmp/recording_temp.raw");

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (file.getAbsolutePath());
    }

    private void writeAudioDataToFile(boolean b) {
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(filename, b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;

        if (fileOutputStream != null) {
            while (isRecording) {
                read = audioRecord.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        fileOutputStream.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}