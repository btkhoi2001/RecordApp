package com.ag18.record;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

public class FolderFragment extends Fragment implements RecodingAdapter.onItemListClick {

    View view;

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView recordList;
    private File[] allFiles;

    private RecodingAdapter recodingAdapter;

    static MediaPlayer mediaPlayer;

    private File fileToPlay = null;

    //UI Elements
    private ImageButton playBtn, btnForwardLeft, btnForwardRight;
    private TextView playerHeader;
    private TextView playerFilename;

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;
    BarVisualizer visualizer;
    private String path = System.getenv("EXTERNAL_STORAGE") + "/RecordApp";

    public FolderFragment() {
        // Required empty public constructor
    }
    @Override
    public void onDestroy() {
        if (visualizer != null){
            visualizer.release();
        }
        super.onDestroy();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_folder, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        recordList = view.findViewById(R.id.audio_list_view);
        visualizer = view.findViewById(R.id.blast);
        playBtn = view.findViewById(R.id.play_btn);
        playerHeader = view.findViewById(R.id.player_header_title);
        playerFilename = view.findViewById(R.id.player_filename);

        playerSeekbar = view.findViewById(R.id.player_seekbar);
        btnForwardLeft = view.findViewById(R.id.btnForwadLeft);
        btnForwardRight = view.findViewById(R.id.btnForwadRight);
        File directory = new File(path);
        allFiles = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String extensions[] = {".wav", ".mp3", ".mp4", ".3gp"};
                String name = file.getName().toLowerCase();

                for (String extension : extensions)
                    if (name.endsWith(extension) && file.isFile())
                        return true;

                return false;
            }
        });

        recodingAdapter = new RecodingAdapter(getContext(), view, allFiles, this);

        recordList.setHasFixedSize(true);
        recordList.setLayoutManager(new LinearLayoutManager(getContext()));
        recordList.setAdapter(recodingAdapter);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //We cant do anything here for this app
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    pauseAudio();
                } else {
                    if(fileToPlay != null){
                        resumeAudio();
                    }
                }
            }
        });

        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                resumeAudio();
            }
        });
        btnForwardRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    final int newTime = mediaPlayer.getCurrentPosition()+5000;
                    playerSeekbar.setProgress(newTime);
                    mediaPlayer.seekTo(newTime);
                }
            }
        });
        btnForwardLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    final int newTime = (mediaPlayer.getCurrentPosition()-5000 < 0) ? 0 : mediaPlayer.getCurrentPosition()-5000 ;
                    playerSeekbar.setProgress(newTime);
                    mediaPlayer.seekTo(newTime);
                }
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        if(mediaPlayer !=null && mediaPlayer.isPlaying()){
            stopAudio();
            playAudio(fileToPlay);
        } else {
            playAudio(fileToPlay);
        }
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24, null));
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void resumeAudio() {
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause, null));
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void stopAudio() {
        //Stop The Audio
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24, null));
        playerHeader.setText("Stopped");
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(File fileToPlay) {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_pause, null));
        playerFilename.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        //Play the audio
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                playerHeader.setText("Finished");
            }
        });

        playerSeekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1){
            visualizer.setAudioSessionId(audioSessionId);
        }

    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mediaPlayer.isPlaying()) {
            stopAudio();
        }
    }

}
