package com.ag18.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class VoiceFilterFragment extends Fragment{
    private ImageButton ibPlay, ibSave, ibStop;
    private TextView tvFileName;
    private AudioTrack audioTrack;
    private View view;

    int sampleRateConfiguration;
    int channelConfiguration;
    int audioEncoding;

    private ListView listView;
    ArrayList<Filter> filtersList;
    FilterAdapter filterAdapter;

    float pitch = 1f;
    String selectedEffect = "None";

    String saveName = "test";
    String extension = ".wav";

    String path;

    NavController navController;

    PlayTask playTask;

    File file;

    private class PlayTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            ibPlay.setVisibility(View.GONE);
            ibStop.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            double playTime = 0;
            try {
                playTime = playRecord();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep((long) (playTime*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v)
        {
            ibPlay.setVisibility(View.VISIBLE);
            ibStop.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_voice_filter, container, false);

        ibPlay = view.findViewById(R.id.ib_play_test);
        ibSave = view.findViewById(R.id.ib_save);
        ibStop = view.findViewById(R.id.ib_stop_test);

        tvFileName = view.findViewById(R.id.tv_file_name);

        tvFileName.setText("Tune up your record");

        listView = view.findViewById(R.id.filter_list);

        filtersList = new ArrayList<>();
        filtersList.add(new Filter("None", " "));
        filtersList.add(new Filter("Clown", "🤡"));
        filtersList.add(new Filter("Chipmunk", "🐿️"));
        filtersList.add(new Filter("Monster", "👹"));
        filtersList.add(new Filter("Echo", "〰"));
        filtersList.add(new Filter("Bee", "🐝"));
        filtersList.add(new Filter("Reverse", "🔁"));

        filterAdapter = new FilterAdapter(getContext(), R.layout.filter_line, filtersList);
        listView.setAdapter(filterAdapter);

        sampleRateConfiguration = getArguments().getInt("sample_rate");
        channelConfiguration = getArguments().getInt("channel");
        audioEncoding = getArguments().getInt("encoding");
        path = getArguments().getString("path");

        file = new File(path, "recording_temp.raw");

        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTask = new PlayTask();
                playTask.execute();
            }
        });

        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibPlay.setVisibility(View.VISIBLE);
                ibStop.setVisibility(View.GONE);
                audioTrack.stop();
                audioTrack.flush();
                audioTrack.release();
                playTask.cancel(true);
            }
        });

        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEffect = filtersList.get(i).getName();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private short[] readData() throws IOException {
        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (file.length() / shortSizeInBytes);

        short[] data = new short[bufferSizeInBytes];

        InputStream inputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

        int j = 0;

        while(dataInputStream.available() > 0)
        {
            data[j] = (dataInputStream.readShort());
            j++;
        }

        return data;
    }

    @SuppressLint("ResourceAsColor")
    private double playRecord() throws IOException {
        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (file.length() / shortSizeInBytes);

        short[] audioData = readData();

        //audioData = echoFilter(audioData, 2048, 0.6f);
        //audioData = reverseFilter(audioData);

        switch (selectedEffect)
        {
            case "None":
                pitch = 1f;
                break;
            case "Clown":
                pitch = 1.2f;
                break;
            case "Chipmunk":
                pitch = 1.6f;
                break;
            case "Monster":
                pitch = 0.7f;
                break;
            case "Echo":
                audioData = echoFilter(audioData, 6000, 0.6f);
                pitch = 1f;
                break;
            case "Bee":
                pitch = 3f;
                break;
            case "Reverse":
                audioData = reverseFilter(audioData);
                pitch = 1f;
                break;
            default:
                pitch = 1f;
        }

        audioTrack = new AudioTrack(3, (int) (sampleRateConfiguration *pitch), channelConfiguration, audioEncoding, bufferSizeInBytes, 1);
        audioTrack.play();
        audioTrack.write(audioData, 0, bufferSizeInBytes);

        return audioData.length / (sampleRateConfiguration * pitch);
    }

    private short[] echoFilter(short[] data, int numDelay, float decay)
    {
        short[] delayBuffer = new short[numDelay];
        int delayBufferPos = 0;

        int length = data.length;

        short[] modifiedData = new  short[length];

        for (int i = 0; i < length; i ++)
        {
            short oldSample = data[i];
            short newSample = (short) (oldSample + decay * delayBuffer[delayBufferPos]);

            modifiedData[i] = newSample;

            delayBuffer[delayBufferPos] = newSample;
            delayBufferPos++;

            if (delayBufferPos == delayBuffer.length)
            {
                delayBufferPos = 0;
            }
        }

        return modifiedData;
    }

    private short[] reverseFilter(short[] data)
    {
        int length = data.length;

        short[] modifiedData = new  short[length];

        for (int i = length - 1; i >=0;  i --)
        {
            modifiedData[length - 1 - i] = data[i];
        }

        return modifiedData;
    }

    private byte[] shortToBytes(@NonNull short[] shortArray) {
        int shortArrSize = shortArray.length;
        byte[] bytes = new byte[shortArrSize * 2];

        for (int i = 0; i < shortArrSize; i++) {
            bytes[i * 2] = (byte) (shortArray[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (shortArray[i] >> 8);
            shortArray[i] = 0;
        }
        return bytes;
    }

    private byte[] wavFileHeader(long totalAudioLen, long totalDataLen, int sampleRate, int channels, long byteRate, byte bitsPerSample) {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * (bitsPerSample / 8)); //
        // block align
        header[33] = 0;
        header[34] = bitsPerSample; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }

    private void saveRecord() throws IOException {
        short[] audioData = readData();

        switch (selectedEffect)
        {
            case "None":
                pitch = 1f;
                break;
            case "Clown":
                pitch = 1.2f;
                break;
            case "Chipmunk":
                pitch = 1.6f;
                break;
            case "Monster":
                pitch = 0.7f;
                break;
            case "Echo":
                audioData = echoFilter(audioData, 6000, 0.6f);
                pitch = 1f;
                break;
            case "Bee":
                pitch = 3f;
                break;
            case "Reverse":
                audioData = reverseFilter(audioData);
                pitch = 1f;
                break;
            default:
                pitch = 1f;
        }

        File out = new File(path, saveName + extension);
        byte[] audio = shortToBytes(audioData);

        long chunk1Size = 16; //RIFF chunk
        byte bitsPerSample = 16; //ENCODING-16-BITS
        int format = 1; //PCM
        int channels = channelConfiguration;
        int sampleRate = (int) (sampleRateConfiguration / 2 * pitch);
        long byteRate = (long) sampleRate * channels * bitsPerSample/8;
        int blockAlign = (int) (channels * bitsPerSample/8);

        long audioLen = audio.length;
        long chunk2Size = audioLen * channels * bitsPerSample/8;
        long chunkSize = 36 + chunk2Size;

        byte[] header = wavFileHeader(audioLen, chunkSize, sampleRate, channels, byteRate, bitsPerSample);

        OutputStream outputStream = new FileOutputStream(out);

        byte[] data = new byte[audio.length + header.length];

        for (int i = 0; i < data.length; ++i)
        {
            data[i] = i < header.length ? header[i] : audio[i - header.length];
        }

        outputStream.write(data);
        System.out.println("Wrote to " + out.getAbsolutePath());
        outputStream.close();
    }

    @SuppressLint("ResourceAsColor")
    public void saveDialog(){
        TextView title = new TextView(getActivity());
        title.setText("Save name");
        title.setPadding(20, 30, 20, 30);
        title.setTextSize(20F);
        title.setBackgroundColor(R.color.design_default_color_secondary_variant);
        title.setTextColor(Color.WHITE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.save_dialog, null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        EditText fileName = (EditText) view.findViewById(R.id.saveName);
        fileName.setText("Recording_" + currentDateAndTime);

        builder.setView(view).setCustomTitle(title).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveName = fileName.getText().toString();
                try {
                    saveRecord();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(), "Saved file", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_voiceFilterFragment_to_recordFragment);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}

