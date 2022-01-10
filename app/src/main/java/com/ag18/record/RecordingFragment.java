package com.ag18.record;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
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
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class RecordingFragment extends Fragment {
    private ImageButton btnStop, btnPause;
    private Button btnCancel;
    private TextView tvRemainingStorage;

    private Chronometer chronometer;
    private NavController navController;

    boolean isRecording = true;
    private AudioRecord audioRecord = null;
    private int minBufferSize = 0;
    private Thread recordingThread = null;

    private View view = null;
    private String externalStorage;

    private int sampleRate;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private boolean pauseDuringACall;
    private boolean isCalling = false;

    private Handler storageHandler = new Handler();
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

        loadSettings();

        try {
            org.apache.commons.io.FileUtils.cleanDirectory(new File(externalStorage + "/.temp"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        btnStop = view.findViewById(R.id.btn_stop);
        btnPause = view.findViewById(R.id.btn_pause);
        btnCancel = view.findViewById(R.id.btn_cancel);
        tvRemainingStorage = view.findViewById(R.id.tv_remaining_storage);

        chronometer = view.findViewById(R.id.chronometer);
        navController = Navigation.findNavController(view);

        setListener();
        startRecording(false);
        updateStorage();
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
        File file = new File(externalStorage + "/.temp/recording_temp.raw");

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (file.getAbsolutePath());
    }

    private void writeAudioDataToFile(boolean b) throws IOException {
        String filename = getTempFilename();

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
    }

    private void loadSettings()
    {
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        sampleRate = Integer.parseInt(sharedPreference.getString("sample_rate", "44100"));
        pauseDuringACall = sharedPreference.getBoolean("pause_during_a_call", false);
        externalStorage = sharedPreference.getString("recording_folder", Environment.getExternalStorageDirectory().getPath() + "/RecordApp");
        File tempFolder = new File(externalStorage + "/.temp");
        tempFolder.mkdirs();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (pauseDuringACall && isRecording) {
            stopRecording(false);
            isCalling = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (pauseDuringACall && isCalling) {
            startRecording(true);
            isCalling = false;
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            File iPath = Environment.getDataDirectory();
            StatFs iStat = new StatFs(iPath.getPath());
            long iBlockSize = iStat.getBlockSizeLong();
            long iAvailableBlocks = iStat.getAvailableBlocksLong();
            long iAvailableSpace = iAvailableBlocks * iBlockSize;

            tvRemainingStorage.setText(Utils.readableFileSize(iAvailableSpace));
            updateStorage();

            if (iAvailableSpace <= 10 * 1024 * 1024)
                ShowStorageFullDialog();
        }
    };

    private void updateStorage() {
        storageHandler.postDelayed(updater, 10);
    }

    private void ShowStorageFullDialog() {
        storageHandler.removeCallbacks(updater);

        if (isRecording)
            stopRecording(false);

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);

        alert.setMessage("Do you want to save this record?");
        alert.setTitle("Not Enough Storage");
        alert.setCancelable(false);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Navigation.findNavController(view).popBackStack();
            }
        });

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                navController.navigate(R.id.action_recordingFragment_to_voiceFilterFragment);
            }
        });

        alert.show();
    }
}