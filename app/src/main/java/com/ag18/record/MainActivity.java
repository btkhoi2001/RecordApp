package com.ag18.record;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent myIntent = new Intent(MainActivity.this, ListRecordingsActivity.class);
//        startActivity(myIntent);

        Intent playIntent = new Intent(MainActivity.this, RecordingPlayList.class);
        startActivity(playIntent);
    }
}