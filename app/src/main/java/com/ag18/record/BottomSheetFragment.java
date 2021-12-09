package com.ag18.record;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private ImageButton ibListen, ibShare, ibRename, ibEdit, ibDetails, ibDelete;
    private ImageButton ibSetRingtone, ibFilters;
    private File file;
    View rootView;
    String fileName;
    String newname ;
    private String externalStorage = System.getenv("EXTERNAL_STORAGE") + "/RecordApp";

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

        File file = new File(externalStorage + "/" + fileName);



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
                bundle.putString("filePath", externalStorage + "/" + fileName);

                NavController navController = Navigation.findNavController(rootView);
                navController.navigate(R.id.action_folderFragment_to_voiceEditorFragment, bundle);
                dismiss();
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.exists()){
                    file.delete();
                    Toast.makeText(getActivity(), "File Deleted Successfully!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getActivity(), "Can not Delete this file", Toast.LENGTH_LONG).show();
                }
                /*Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.detach(currentFragment);
                fragmentTransaction.attach(currentFragment);
                fragmentTransaction.commit();*/
                NavController navController = Navigation.findNavController(view);
                navController.popBackStack();
                navController.navigate(R.id.folderFragment);
            }
        });

        ibRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_rename_file);

                Window window =dialog.getWindow();
                if(window == null){
                    return;
                }

                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                WindowManager.LayoutParams windowAttributes = window.getAttributes();
                windowAttributes.gravity=Gravity.CENTER;
                window.setAttributes(windowAttributes);

                if(Gravity.BOTTOM == Gravity.CENTER){
                    dialog.setCancelable(true);
                }else{
                    dialog.setCancelable(true);
                }

                EditText nameText = dialog.findViewById(R.id.new_name);
                Button btnOk = dialog.findViewById(R.id.ok_button);
                TextView tvChangename = dialog.findViewById(R.id.change_name);

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newname = nameText.getText().toString();
                        File rename = new File(externalStorage + "/" + newname);

                        if (file.renameTo(rename)) {
                            Toast.makeText(getActivity(), "File Successfully Rename", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getActivity(), "Operation Failed", Toast.LENGTH_SHORT).show();
                        }
                        dismiss();
                    }
                });

                dialog.show();
            }
        });

        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(file.exists()){
                    Uri uri = Uri.parse(externalStorage + "/" + fileName);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("audio/*");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(share, "Share Sound File"));
                }
            }
        });

        /*ibSetRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent to select Ringtone.
                final Uri currentTone=
                        RingtoneManager.getActualDefaultRingtoneUri(getActivity(),
                                RingtoneManager.TYPE_ALARM);
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                startActivityForResult(intent, 999);
            }
        });*/

        return view;
    }

}