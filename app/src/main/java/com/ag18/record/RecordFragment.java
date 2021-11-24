package com.ag18.record;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RecordFragment extends Fragment {
    private NavController navController;
    private ImageButton btnRecord;
    private static int MICROPHONE_PERMISSION_CODE = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout layout_record = (FrameLayout)inflater.inflate(R.layout.fragment_record, container, false);
        return layout_record;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        btnRecord = (ImageButton)view.findViewById(R.id.button_topleft);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMicrophonePresent()) {
                    getMicrophonePermission();
                }

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    navController.navigate(R.id.action_recordFragment_to_recordingFragment);
//                    BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
//                    bottomNavigationView.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean isMicrophonePresent() {
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE))
            return true;
        return false;
    }

    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
    }
}