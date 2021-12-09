package com.ag18.record;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

public class FolderFragment extends Fragment implements RecodingAdapter.onItemListClick {
    View view;

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView recordList;
    private File[] allFiles;

    private RecodingAdapter recodingAdapter;

    static MediaPlayer mediaPlayer = null;

    private File fileToPlay = null;
    int current = 0;
    //UI Elements
    private Button btnPlay, btnForwardLeft, btnForwardRight, btnNext, btnPrevious;
    private TextView playerHeader;
    private TextView playerFilename;

    private SeekBar seekbar;
    private Handler seekbarHandler = new Handler();
    BarVisualizer visualizer;
    private String path;

    public FolderFragment() {
        // Required empty public constructor
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (visualizer != null) {
            visualizer.release();
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            seekbarHandler.removeCallbacks(updateSeekbar);
        }
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

        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        path = sharedPreference.getString("recording_folder", Environment.getExternalStorageDirectory().getPath() + "/RecordApp");

        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        recordList = view.findViewById(R.id.audio_list_view);
        visualizer = view.findViewById(R.id.blast);
        btnPlay = view.findViewById(R.id.btnPlay);
        playerHeader = view.findViewById(R.id.player_header_title);
        playerFilename = view.findViewById(R.id.player_filename);

        seekbar = view.findViewById(R.id.player_seekbar);
        btnForwardLeft = view.findViewById(R.id.btnForwadLeft);
        btnForwardRight = view.findViewById(R.id.btnForwadRight);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrevious = view.findViewById(R.id.btnPrevious);

        mediaPlayer = new MediaPlayer();

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

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileToPlay == null)
                    return;

                if(mediaPlayer.isPlaying()){
                        pauseAudio();
                }
                else {
                    playAudio();
                    int audioSessionId = mediaPlayer.getAudioSessionId();

                    if(audioSessionId != -1){
                        visualizer.setAudioSessionId(audioSessionId);
                    }
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int next = (current+1) % allFiles.length;
                onClickListener(allFiles[next], next);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pre = (current - 1 + allFiles.length ) % allFiles.length;
                onClickListener(allFiles[pre], pre);
            }
        });

        btnForwardRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    final int newTime = mediaPlayer.getCurrentPosition()+5000;
                    seekbar.setProgress(newTime);
                    mediaPlayer.seekTo(newTime);
                }
            }
        });

        btnForwardLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    final int newTime = (mediaPlayer.getCurrentPosition()-5000 < 0) ? 0 : mediaPlayer.getCurrentPosition()-5000 ;
                    seekbar.setProgress(newTime);
                    mediaPlayer.seekTo(newTime);
                }
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        current = position;
        System.out.println(position);
        if(mediaPlayer != null ){
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                seekbarHandler.removeCallbacks(updateSeekbar);
            }
            mediaPlayer.release();
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        playerFilename.setText(fileToPlay.getName());
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            btnPlay.setBackgroundResource(R.drawable.ic_play);
            seekbar.setProgress(0);
            playAudio();
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if(audioSessionId != -1){
                visualizer.setAudioSessionId(audioSessionId);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseAudio(){
        playerHeader.setText("Pause");
        btnPlay.setBackgroundResource(R.drawable.ic_play);
        mediaPlayer.pause();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(){
        btnPlay.setBackgroundResource(R.drawable.ic_pause_circle);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playerHeader.setText("Not Playing");
                btnPlay.setBackgroundResource(R.drawable.ic_play);
                seekbar.setProgress(0);
//                mediaPlayer.stop();
                mediaPlayer.seekTo(0);
                seekbarHandler.removeCallbacks(updateSeekbar);
            }
        });

        seekbar.setMax(mediaPlayer.getDuration());
        seekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPink),
                PorterDuff.Mode.MULTIPLY);
        seekbar.getThumb().setColorFilter(getResources().getColor(R.color.colorPink), PorterDuff.Mode.SRC_IN);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekbarHandler.removeCallbacks(updateSeekbar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                updateRunnable();
            }
        });

        playerFilename.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        updateRunnable();
    }

    private void updateRunnable() {
        seekbarHandler.postDelayed(updateSeekbar, 10);
    }

    private Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {
            seekbar.setProgress(mediaPlayer.getCurrentPosition());
            updateRunnable();
        }
    };
}
