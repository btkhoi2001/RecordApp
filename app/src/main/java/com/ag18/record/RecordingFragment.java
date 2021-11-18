package com.ag18.record;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.IOException;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
/*
public class RecordingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button btnSave;
    private Button btnDelete;
    private ImageButton btnRecording;
    private Chronometer chronometer;
    private MediaRecorder mediaRecorder;

    private boolean isRecording = false;
    private int PERMISSION_CODE = 21;
<<<<<<< Updated upstream
    private String recordFile;
=======
    private String filename;

    File filepath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordApp");
>>>>>>> Stashed changes

    public RecordingFragment() {
        // Required empty public constructor
    }

    public static RecordingFragment newInstance(String param1, String param2) {
        RecordingFragment fragment = new RecordingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout layout_recording = (LinearLayout)inflater.inflate(R.layout.fragment_recording, container, false);
        return layout_recording;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSave = view.findViewById(R.id.btn_save);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnRecording = view.findViewById(R.id.btn_pause);
        chronometer = view.findViewById(R.id.chronometer);

        btnRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording) {
                    stopRecording();
                    btnRecording.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                    isRecording = false;
                }
                else {
                    if(checkPermission()) {
                        starRecording();
                        btnRecording.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_stop_24));
                        isRecording = true;
                    }
                }
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording) {
                    stopRecording();
                    btnRecording.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                    isRecording = false;
                }
            }
        });
    }

    private void stopRecording() {
        Toast.makeText(getActivity(), "Stop recording", Toast.LENGTH_LONG).show();
        mediaRecorder.stop();
        mediaRecorder.release();

        mediaRecorder = null;
    }

<<<<<<< Updated upstream
    private void starRecording() {
=======
    @SuppressLint("WrongConstant")
    private void startRecording(){
        //Start timer from 0
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        //Get app external directory path
>>>>>>> Stashed changes
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        recordFile = "filename.3gp";

<<<<<<< Updated upstream
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

=======
        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();

        filename = "Recording_" + formatter.format(now) + ".wav";
        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
        mediaRecorder.setAudioChannels(2);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(48000);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(recordPath + "/" + filename);
>>>>>>> Stashed changes
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
<<<<<<< Updated upstream

=======
        //Start Recording
>>>>>>> Stashed changes
        mediaRecorder.start();
        Toast.makeText(getActivity(), "Recording Started", Toast.LENGTH_LONG).show();
    }

    public boolean checkPermission() {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
            return false;
        }
    }
}
*/
public class RecordingFragment extends Fragment {
    private ImageButton btnRecord;
    private ImageButton btnPlayback;
    private Chronometer chronometer;

    private boolean isRecording = false;
    private int PERMISSION_CODE = 21;
    private String filename;

    File file = new File(Environment.getExternalStorageDirectory(), "temp.pcm");

    AudioTrack audioTrack;

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
        System.out.println(file.getAbsolutePath());
        return layout_recording;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRecord = view.findViewById(R.id.btn_pause);
        btnPlayback = view.findViewById(R.id.btn_play);
        chronometer = view.findViewById(R.id.chronometer);

        navController = Navigation.findNavController(view);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
                    Toast.makeText(getActivity(), "Recording Started", Toast.LENGTH_LONG).show();
                    isRecording = true;
                    chronometer.setBase(0);
                    chronometer.start();
                    new Thread( (Runnable) () ->
                    {
                        try {
                            startRecord();
                        } catch (IOException e) {
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
                    System.out.println("got here");
                    navController.navigate(R.id.action_recordingFragment_to_voiceFilterFragment, bundle);
                }
            }
        });

        btnPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecording = false;
                chronometer.stop();
                Bundle bundle = new Bundle();
                bundle.putInt("frequency", frequency);
                bundle.putInt("channel", channelConfiguration);
                bundle.putInt("encoding", audioEncoding);
                System.out.println("got here");
                navController.navigate(R.id.action_recordingFragment_to_voiceFilterFragment, bundle);
            }
        });
    }

    private void startRecord() throws IOException {
        //Start timer from 0
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        File myFile = new File(Environment.getExternalStorageDirectory(), "temp.pcm");
        myFile.createNewFile();
        System.out.println(myFile.getAbsolutePath());

        OutputStream outputStream = new FileOutputStream(myFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

        int minBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        short[] audioData = new short[minBufferSize];

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
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