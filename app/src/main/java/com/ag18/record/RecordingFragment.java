package com.ag18.record;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageButton btnRecording;
    private Chronometer chronometer;
    private MediaRecorder mediaRecorder;

    private boolean isRecording = false;
    private int PERMISSION_CODE = 21;
    private String filename;

    //File filepath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordApp");

    public RecordingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordingFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        btnRecording = view.findViewById(R.id.btn_pause);
        chronometer = view.findViewById(R.id.chronometer);


        btnRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording) {
                    //Stop Recording
                    stopRecording();
                    btnRecording.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_stop_24));
                    isRecording = false;
                }
                else {
                    //Check permission to record audio
                    if(checkPermission()) {
                        //Start Recording
                        startRecording();
                        btnRecording.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                        isRecording = true;
                    }
                }
            }
        });
    }


    private void stopRecording() {
        Toast.makeText(getActivity(), "Stop recording", Toast.LENGTH_LONG).show();
        //Stop Timer, very obvious
        chronometer.stop();

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private void startRecording() {
        //Start timer from 0
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        //Get app external directory path
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA);
        Date now = new Date();

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        filename = "Recording_" + formatter.format(now) + ".3gp";

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + filename);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
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