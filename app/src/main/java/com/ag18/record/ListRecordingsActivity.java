package com.ag18.record;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class ListRecordingsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<RecordingItem> recordingList;
    RecodingAdapter recordingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_recordings);

        initData();
        initRecyclerView();
    }

    private void initData() {
        recordingList = new ArrayList<>();

        // Cái này test. Khi chạy thật sẽ lấy từ header của file.
        recordingList.add(new RecordingItem("Interview 1", "14 Dec 2021", "08:23"));
        recordingList.add(new RecordingItem("Interview 2", "15 Dec 2021", "09:23"));
        recordingList.add(new RecordingItem("Interview 3", "16 Dec 2021", "10:23"));
        recordingList.add(new RecordingItem("Interview 4", "17 Dec 2021", "11:23"));
        recordingList.add(new RecordingItem("Interview 5", "18 Dec 2021", "12:23"));
        recordingList.add(new RecordingItem("Interview 6", "19 Dec 2021", "13:23"));
        recordingList.add(new RecordingItem("Interview 1", "14 Dec 2021", "08:23"));
        recordingList.add(new RecordingItem("Interview 2", "15 Dec 2021", "09:23"));
        recordingList.add(new RecordingItem("Interview 3", "16 Dec 2021", "10:23"));
        recordingList.add(new RecordingItem("Interview 4", "17 Dec 2021", "11:23"));
        recordingList.add(new RecordingItem("Interview 5", "18 Dec 2021", "12:23"));
        recordingList.add(new RecordingItem("Interview 6", "19 Dec 2021", "13:23"));
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.rv_recordings);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recordingAdapter = new RecodingAdapter(recordingList);
        recyclerView.setAdapter(recordingAdapter);
        recordingAdapter.notifyDataSetChanged();
    }
}