package com.ag18.record;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
<<<<<<< HEAD
=======
import android.media.RingtoneManager;
>>>>>>> 7d16892eb8e78e70446d5e095752dfffc4da97c7
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class BottomSheetFragment extends BottomSheetDialogFragment {
<<<<<<< HEAD
    private ImageButton ibListen, ibShare, ibRename, ibEdit, ibDetails, ibDelete;
    private ImageButton ibSetRingtone, ibFilters;
    private File file;
=======
    private ImageButton ibSetRingtone, ibShare, ibRename, ibEdit, ibDetails, ibDelete;
>>>>>>> 7d16892eb8e78e70446d5e095752dfffc4da97c7
    View rootView;
    String fileName;

    public BottomSheetFragment(View rootView, String fileName) {
        this.rootView = rootView;
        this.fileName = fileName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        ContentValues values = new ContentValues();
        File file = new File(path + "/" + fileName);


        ibSetRingtone = view.findViewById(R.id.ib_listen);
        ibShare = view.findViewById(R.id.ib_share);
        ibRename = view.findViewById(R.id.ib_rename);
        ibEdit = view.findViewById(R.id.ib_edit);
        ibDetails = view.findViewById(R.id.ib_details);
        ibDelete = view.findViewById(R.id.ib_delete);

        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
<<<<<<< HEAD
                bundle.putString("filePath", "/sdcard/RecordApp/test.wav");
=======
                bundle.putString("filePath", path + "/" + fileName);
>>>>>>> 7d16892eb8e78e70446d5e095752dfffc4da97c7
                // Ai làm phần bottom sheet fragment thì nhớ thay test.wav bằng đường dẫn file khi chọn trong list view

                NavController navController = Navigation.findNavController(rootView);
                navController.navigate(R.id.action_folderFragment_to_voiceEditorFragment, bundle);
                dismiss();

            }
        });
<<<<<<< HEAD

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
=======
>>>>>>> 7d16892eb8e78e70446d5e095752dfffc4da97c7

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

        /*ibSetRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
                Uri newUri = this.getContentResolver().insert(uri, values);

                RingtoneManager.setActualDefaultRingtoneUri(
                        getActivity(),
                        RingtoneManager.TYPE_RINGTONE,
                        newUri
                );
            }
        });*/

        return view;
    }
}