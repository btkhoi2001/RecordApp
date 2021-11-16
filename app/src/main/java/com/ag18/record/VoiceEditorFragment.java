package com.ag18.record;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Stack;
import java.util.UUID;

public class VoiceEditorFragment extends Fragment {
    private ImageButton ibPlayPause, ibSave, ibUndo, ibRedo, ibTrim, ibRemoveMiddle;
    private TextView tvCurrentTime, tvTotalDuration, tvFileName;
    private RangeSeekBar rsbEditor;
    private SeekBar sbPlayer;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private View view;
    private int totalDuration;
    private String externalStorage = System.getenv("EXTERNAL_STORAGE") + "/RecordApp";
    private String suffix;
    private String prefix;
    private Boolean isPlaying = false, isDrag = false;
    private Stack<String> undo = new Stack<String>();
    private Stack<String> redo = new Stack<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_voice_editor, container, false);

        ibPlayPause = view.findViewById(R.id.ib_play_editor);
        ibSave = view.findViewById(R.id.ib_save);
        ibUndo = view.findViewById(R.id.ib_undo);
        ibTrim = view.findViewById(R.id.ib_trim);
        ibRemoveMiddle = view.findViewById(R.id.ib_remove_middle);
        ibRedo = view.findViewById(R.id.ib_redo);

        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTotalDuration = view.findViewById(R.id.tv_total_duration);
        tvFileName = view.findViewById(R.id.tv_file_name);

        rsbEditor = view.findViewById(R.id.rsb_editor);
        sbPlayer = view.findViewById(R.id.sb_player);

        ibUndo.setEnabled(false);
        ibRedo.setEnabled(false);

        String filePath = requireArguments().getString("filePath");
        File file = new File(filePath);
        prefix = file.getName();
        int i = prefix.lastIndexOf('.');
        suffix = prefix.substring(i + 1);
        prefix = prefix.substring(0, i);
        tvFileName.setText(file.getName());
        undo.push(filePath);

        String tmpDir = externalStorage + "/.temp";
        File hiddenTmpDir = new File(tmpDir);

        if (!hiddenTmpDir.exists())
            hiddenTmpDir.mkdir();

        preparedMediaPlayer();
        setListener();

        return view;
    }

    private void preparedMediaPlayer() {
        try {
            isPlaying = false;
            isDrag = false;

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(undo.peek());

            mediaPlayer.prepare();
            totalDuration = mediaPlayer.getDuration();

            tvCurrentTime.setText(millisecondsToTimer(0));
            tvTotalDuration.setText(millisecondsToTimer(totalDuration));

            sbPlayer.setMax(totalDuration);
            sbPlayer.setProgress(0);

            ibTrim.setEnabled(false);
            ibRemoveMiddle.setEnabled(false);
            ibPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);

            rsbEditor.setRange(0, totalDuration);
            rsbEditor.setProgress(0, totalDuration);
            rsbEditor.getLeftSeekBar().setIndicatorText(millisecondsToTimer(0));
            rsbEditor.getRightSeekBar().setIndicatorText(millisecondsToTimer(totalDuration));

        } catch (Exception exception) {
            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setListener() {
        ibPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                    ibPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    isPlaying = false;
                } else {
                    mediaPlayer.start();
                    ibPlayPause.setImageResource(R.drawable.ic_pause);
                    updateSeekBar();
                    isPlaying = true;
                }
            }
        });

        rsbEditor.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                rsbEditor.getLeftSeekBar().setIndicatorText(millisecondsToTimer((int)leftValue));
                rsbEditor.getRightSeekBar().setIndicatorText(millisecondsToTimer((int)rightValue));

                tvCurrentTime.setText(millisecondsToTimer((int)leftValue));
                tvTotalDuration.setText(millisecondsToTimer((int)rightValue));

                if (leftValue == rsbEditor.getMinProgress() && rightValue == rsbEditor.getMaxProgress()) {
                    ibTrim.setEnabled(false);
                    ibRemoveMiddle.setEnabled(false);
                }
                else {
                    ibTrim.setEnabled(true);
                    ibRemoveMiddle.setEnabled(true);
                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                if (isPlaying) {
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                int leftValue = (int) rsbEditor.getLeftSeekBar().getProgress();
                int rightValue = (int) rsbEditor.getRightSeekBar().getProgress();
                tvCurrentTime.setText(millisecondsToTimer(leftValue));
                totalDuration = rightValue - leftValue;
                sbPlayer.setProgress(0);
                sbPlayer.setMax(totalDuration);
                mediaPlayer.seekTo(leftValue, MediaPlayer.SEEK_CLOSEST);

                if (isPlaying) {
                    mediaPlayer.start();
                    updateSeekBar();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo((int) rsbEditor.getLeftSeekBar().getProgress(), MediaPlayer.SEEK_CLOSEST);
                ibPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                isPlaying = false;
            }
        });

        sbPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    int currentDuration = (int) rsbEditor.getLeftSeekBar().getProgress() + sbPlayer.getProgress();
                    mediaPlayer.seekTo(currentDuration, MediaPlayer.SEEK_CLOSEST);
                    tvCurrentTime.setText(millisecondsToTimer(currentDuration));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDrag = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isDrag = false;
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                sbPlayer.setSecondaryProgress(i);
            }
        });

        ibTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dest = externalStorage + "/.temp/" + UUID.randomUUID().toString() + "." + suffix;
                String command = "-ss " + (int) rsbEditor.getLeftSeekBar().getProgress() + "ms -i \"" + undo.peek() + "\" -to " + (int) rsbEditor.getRightSeekBar().getProgress() + "ms -c copy \"" + dest +"\"";

                FFmpegKit.execute(command);
                ibUndo.setEnabled(true);
                ibRedo.setEnabled(false);
                redo.clear();
                undo.push(dest);
                mediaPlayer.release();
                preparedMediaPlayer();
            }
        });

        ibRemoveMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dest = externalStorage + "/.temp/" + UUID.randomUUID().toString() + "." + suffix;
                String command = "-i \"" + undo.peek() + "\" -filter_complex \"[0]atrim=end=" + (int) rsbEditor.getLeftSeekBar().getProgress() + "ms[a];[0]atrim=start=" + (int)rsbEditor.getRightSeekBar().getProgress() + "ms[b];[a][b]concat=n=2:v=0:a=1\" \"" + dest + "\"";
                FFmpegKit.execute(command);

                ibUndo.setEnabled(true);
                ibRedo.setEnabled(false);
                redo.clear();
                undo.push(dest);
                mediaPlayer.release();
                preparedMediaPlayer();
            }
        });

        ibUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redo.push(undo.pop());

                if (undo.size() == 1)
                    ibUndo.setEnabled(false);

                ibRedo.setEnabled(true);

                mediaPlayer.release();
                preparedMediaPlayer();
            }
        });

        ibRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undo.push(redo.pop());

                if (redo.isEmpty())
                    ibRedo.setEnabled(false);

                ibUndo.setEnabled(true);

                mediaPlayer.release();
                preparedMediaPlayer();
            }
        });

        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(50, 0, 50, 100);

                EditText input = new EditText(getActivity());
                input.setGravity(Gravity.TOP | Gravity.START);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                int index = 1;
                while (new File(externalStorage + '/' + prefix + "-" + index + "." + suffix).exists())
                    index++;

                input.setText(prefix + "-" + index);
                linearLayout.addView(input, layoutParams);

                alert.setMessage("Name");
                alert.setTitle("Save As");
                alert.setView(linearLayout);

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String prefix = input.getText().toString();
                        File src = new File(undo.peek());
                        File dest = new File(externalStorage + '/' + prefix + "." + suffix);

                        try {
                            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Navigation.findNavController(view).popBackStack();
                        dialogInterface.dismiss();
                    }
                });

                alert.show();
            }
        });
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            long currentDuration = mediaPlayer.getCurrentPosition();
            sbPlayer.setProgress((int) ((float) (currentDuration - rsbEditor.getLeftSeekBar().getProgress())));

            if (currentDuration >= rsbEditor.getRightSeekBar().getProgress()) {
                tvCurrentTime.setText(tvTotalDuration.getText().toString());
                mediaPlayer.pause();
                mediaPlayer.seekTo((int) rsbEditor.getLeftSeekBar().getProgress(), MediaPlayer.SEEK_CLOSEST);
                ibPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                isPlaying = false;
            }
            else if (!isDrag)
                tvCurrentTime.setText(millisecondsToTimer(currentDuration));

            if (isPlaying)
                updateSeekBar();
        }
    };

    private void updateSeekBar() {
        handler.postDelayed(updater, 10);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPlaying)
            ibPlayPause.performClick();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isPlaying)
            mediaPlayer.stop();

        try {
            org.apache.commons.io.FileUtils.cleanDirectory(new File(externalStorage + "/.temp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String millisecondsToTimer(long milliSeconds) {
        String timerString = "";
        String secondsString;

        int hours = (int)(milliSeconds / (1000 * 60 * 60));
        int minutes = (int)(milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int)((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0)
            timerString = hours + ":";

        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        timerString = timerString + minutes + ":" + secondsString;
        return timerString;
    }
}