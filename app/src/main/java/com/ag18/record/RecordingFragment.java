package com.ag18.record;

import android.Manifest;
import android.content.pm.PackageManager;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.IOException;


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class RecordingFragment extends Fragment {
    private ImageButton btnRecord;
//    private ImageButton btnPlayback;
    private Chronometer chronometer;

    private boolean isRecording = false;
    private int PERMISSION_CODE = 21;

    File file = new File(Environment.getExternalStorageDirectory(), "temp.pcm");


    NavController navController;

    int frequency = 44100;
    int channelConfiguration = 2;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout layout_recording = (LinearLayout) inflater.inflate(R.layout.fragment_recording, container, false);
        return layout_recording;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRecord = view.findViewById(R.id.btn_pause);
//        btnPlayback = view.findViewById(R.id.btn_play);
        chronometer = view.findViewById(R.id.chronometer);

        navController = Navigation.findNavController(view);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    Toast.makeText(getActivity(), "Recording Started", Toast.LENGTH_LONG).show();
//                    isRecording = true;
//                    chronometer.setBase(0);
//                    chronometer.start();
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_stop_24));
                    isRecording = true;
                    new Thread( (Runnable) () ->
                    {
                        try {
                            startRecord();
                        } catch (IOException e) {
                            isRecording = false;
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    isRecording = false;
                    chronometer.stop();
                    Bundle bundle = new Bundle();
                    bundle.putInt("frequency", frequency);
                    bundle.putInt("channel", channelConfiguration);
                    bundle.putInt("encoding", audioEncoding);
                    btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_stop_24));
                    navController.navigate(R.id.action_recordingFragment_to_voiceFilterFragment, bundle);
                }
            }
        });
    }

    private void startRecord() throws IOException {
        File myFile = new File(Environment.getExternalStorageDirectory(), "temp.pcm");
        myFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(myFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

        int minBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        short[] audioData = new short[minBufferSize];

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
        }
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, minBufferSize);
        audioRecord.startRecording();

        while (isRecording) {
            int numShortsRead = audioRecord.read(audioData, 0, minBufferSize);
            for (int i = 0; i < numShortsRead; i++)
            {
                dataOutputStream.writeShort(audioData[i]);
            }
        }
        if(!isRecording) {
            audioRecord.stop();
            dataOutputStream.close();
        }
    }
}