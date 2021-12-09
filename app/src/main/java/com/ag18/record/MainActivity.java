package com.ag18.record;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    NavController navController;

    private String externalStorage;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        externalStorage = sharedPreference.getString("recording_folder", Environment.getExternalStorageDirectory().getPath() + "/RecordApp");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        verifyStoragePermissions(this);
        createFolder();
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.recordFragment || destination.getId() == R.id.folderFragment || destination.getId() == R.id.settingsFragment)
                    bottomNavigationView.setVisibility(View.VISIBLE);
                else
                    bottomNavigationView.setVisibility(View.GONE);
            }
        });

    }

    private void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
    }

    private void createFolder() {
        File dir = new File(externalStorage);

        if (!dir.exists())
            dir.mkdir();

        String tmpDir = externalStorage + "/.temp";
        File hiddenTmpDir = new File(tmpDir);

        if (!hiddenTmpDir.exists())
            hiddenTmpDir.mkdir();
    }
}