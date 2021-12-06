package com.ag18.record;

import android.Manifest;
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class RecordingFragment extends Fragment {
    private ImageButton btnRecord;
    private ImageButton btnPause;
    private ImageButton btnResume;
    private Button btnCancel;
    private AudioRecord audioRecord = null;
    private Thread recordingThread = null;

    private Chronometer chronometer;

    private boolean isRecording = false;
    private boolean isPause = false;
    private int PERMISSION_CODE = 21;
    private int minBufferSize = 0;
    private long timeWhenStopped = 0;


    NavController navController;

    int sampleRate = 44100;
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

        loadSettings();

        btnRecord = view.findViewById(R.id.btn_record);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnPause = view.findViewById(R.id.btn_pause);
        btnResume = view.findViewById(R.id.btn_resume);

        chronometer = view.findViewById(R.id.chronometer);

        navController = Navigation.findNavController(view);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chronometer.start();
                    btnPause.setVisibility(View.VISIBLE);
                    btnResume.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Recording Started", Toast.LENGTH_LONG).show();
                    btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_stop_24));
                    new Thread( (Runnable) () ->
                    {
                        try {
                            startRecording(false);
                        } catch (IOException e) {
                            isRecording = false;
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    timeWhenStopped = 0;
                    chronometer.stop();
                    Toast.makeText(getActivity(), "Recording Finished", Toast.LENGTH_LONG).show();
                    btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
                    btnPause.setVisibility(View.GONE);
                    btnResume.setVisibility(View.GONE);
                    Bundle bundle = new Bundle();
                    bundle.putInt("sample_rate", sampleRate);
                    bundle.putInt("channel", channelConfiguration);
                    bundle.putInt("encoding", audioEncoding);
                    stopRecording(false);
                    navController.navigate(R.id.action_recordingFragment_to_voiceFilterFragment, bundle);
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording(false);
                Toast.makeText(getActivity(), "Recording Paused", Toast.LENGTH_LONG).show();
                timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                chronometer.stop();
                btnResume.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.GONE);
            }
        });

        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startRecording(true);
                    Toast.makeText(getActivity(), "Recording Resumed", Toast.LENGTH_LONG).show();
                    btnPause.setVisibility(View.VISIBLE);
                    btnResume.setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_LONG).show();
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                stopRecording(true);
            }
        });
    }

    private void startRecording(final boolean b) throws IOException {
        //btnPause.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
        }
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfiguration, audioEncoding, minBufferSize);
        audioRecord.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writeAudioDataToFile(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void writeAudioDataToFile(boolean b) throws IOException {
        byte data[] = new byte[minBufferSize];
        String filename = getTempFilename();
//        FileOutputStream os = null;

        //File myFile = new File(Environment.getExternalStorageDirectory(), "temp.pcm");
        File myFile = new File(filename);
        myFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(myFile, b);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

        short[] audioData = new short[minBufferSize];

        while (isRecording) {
            int numShortsRead = audioRecord.read(audioData, 0, minBufferSize);
            for (int i = 0; i < numShortsRead; i++)
            {
                dataOutputStream.writeShort(audioData[i]);
            }
        }
        try {
            outputStream.close();
            bufferedOutputStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            os = new FileOutputStream(filename, b);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        int read = 0;
//
//        if (os != null) {
//                read = audioRecord.read(data, 0, minBufferSize);
//
//                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
//                    try {
//                        os.write(data);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        try {
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        //String externalStorage = System.getenv("EXTERNAL_STORAGE") + "/RecordApp";
        File file = new File(filepath + "/recording_temp.raw");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (file.getAbsolutePath());
    }

    private void stopRecording(boolean b) {
        if (audioRecord != null) {
            isRecording = false;

            audioRecord.stop();
            audioRecord.release();

            audioRecord = null;
            recordingThread = null;
        }

        if (b == true) {
            deleteTempFile();
            chronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenStopped = 0;
            chronometer.stop();
        }
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void loadSettings()
    {
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());

        sampleRate = sharedPreference.getInt("sample_rate", 48100);
    }




//    private void startRecord() throws IOException {
//        File myFile = new File(Environment.getExternalStorageDirectory(), "temp.pcm");
//        myFile.createNewFile();
//        OutputStream outputStream = new FileOutputStream(myFile);
//        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
//        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
//
//        int minBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
//        short[] audioData = new short[minBufferSize];
//
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
//        }
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, minBufferSize);
//        audioRecord.startRecording();
//
//        while (isRecording) {
//            int numShortsRead = audioRecord.read(audioData, 0, minBufferSize);
//            for (int i = 0; i < numShortsRead; i++)
//            {
//                dataOutputStream.writeShort(audioData[i]);
//            }
//        }
//        if(!isRecording) {
//            audioRecord.stop();
//            dataOutputStream.close();
//        }
//    }
}