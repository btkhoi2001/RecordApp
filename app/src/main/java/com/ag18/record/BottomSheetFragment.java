package com.ag18.record;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private ImageButton ibListen, ibShare, ibRename, ibEdit, ibDetails, ibDelete;
    private ImageButton ibSetRingtone, ibFilters;
    private File file;
    View rootView;

    public BottomSheetFragment(View rootView) {
        this.rootView = rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        ibListen = view.findViewById(R.id.ib_listen);
        ibShare = view.findViewById(R.id.ib_share);
        ibRename = view.findViewById(R.id.ib_rename);
        ibEdit = view.findViewById(R.id.ib_edit);
        ibDetails = view.findViewById(R.id.ib_details);
        ibDelete = view.findViewById(R.id.ib_delete);

        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("filePath", "/sdcard/RecordApp/test.wav");
                // Ai làm phần bottom sheet fragment thì nhớ thay test.wav bằng đường dẫn file khi chọn trong list view

                NavController navController = Navigation.findNavController(rootView);
                navController.navigate(R.id.action_folderFragment_to_voiceEditorFragment, bundle);
                dismiss();

            }
        });

//        ibEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Bundle bundle = new Bundle();
//                bundle.putString("filePath", path + "/" + fileName);
//
//                NavController navController = Navigation.findNavController(rootView);
//                navController.navigate(R.id.action_folderFragment_to_voiceEditorFragment, bundle);
//                dismiss();
//
//            }
//        });

        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!file.exists()){
                    file.mkdir();
                }

                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("audio/3gp");
                intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file));

                startActivity(Intent.createChooser(intentShare,"Share the file"));
            }
        });

        return view;
    }
}