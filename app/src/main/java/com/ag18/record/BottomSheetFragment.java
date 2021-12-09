package com.ag18.record;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private ImageButton ibListen, ibShare, ibRename, ibEdit, ibDetails, ibDelete;
    private ImageButton ibSetRingtone, ibFilters;
    private File file;
    View rootView;
    String fileName;
    String newname ;
    private String externalStorage;

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

        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        externalStorage = sharedPreference.getString("recording_folder", Environment.getExternalStorageDirectory().getPath() + "/RecordApp");

        File file = new File(externalStorage + "/" + fileName);

        ibSetRingtone = view.findViewById(R.id.ib_ringtone);
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
                if (file.exists()) {
                    file.delete();
                }

                NavController navController = Navigation.findNavController(rootView);
                navController.navigate(R.id.action_folderFragment_self);
                dismiss();
            }
        });

        ibRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);

                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(50, 0, 50, 100);

                EditText input = new EditText(getActivity());
                input.setGravity(Gravity.TOP | Gravity.START);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                input.setTextColor(Color.BLACK);
                linearLayout.addView(input, layoutParams);

                alert.setMessage("Name");
                alert.setTitle("Rename");
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
                        String filename = input.getText().toString();
                        File newFile = new File(file.getParent(), filename + ".wav");

                        try {
                            Files.move(file.toPath(), newFile.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        dialogInterface.dismiss();

                        NavController navController = Navigation.findNavController(rootView);
                        navController.navigate(R.id.action_folderFragment_self);
                        dismiss();
                    }
                });

                alert.show();
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

        ibSetRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkSystemWritePermission()) return;

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, "My Ringtone");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav");
                values.put(MediaStore.MediaColumns.SIZE, file.length());
                values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                Uri uri = MediaStore.Audio.Media.getContentUriForPath(file
                        .getAbsolutePath());
                getActivity().getContentResolver().delete(
                        uri,
                        MediaStore.MediaColumns.DATA + "=\""
                                + file.getAbsolutePath() + "\"", null);
                Uri newUri = getActivity().getContentResolver().insert(uri, values);

                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                            getContext(), RingtoneManager.TYPE_RINGTONE,
                            newUri);
                } catch (Throwable t) {
                    Log.e("Exception", t.getMessage());
                }

                dismiss();
            }
        });

        ibDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);

                String duration = "";
                String created = "";

                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(file.getAbsolutePath());
                    mediaPlayer.prepare();
                    duration = Utils.millisecondsToTimer(mediaPlayer.getDuration());
                    mediaPlayer.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    created = String.valueOf(Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String name = FilenameUtils.removeExtension(file.getName());
                String format = FilenameUtils.getExtension(file.getName());
                long bytes = file.length();
                long kilobytes = (bytes / 1024);
                long megabytes = (kilobytes / 1024);
                String size = String.format("%,d MB", megabytes);
                String fileLocation = file.getAbsolutePath();

                alert.setMessage("Name: " + name + "\nFormat: " + format + "\nDuration: " + duration + "\nSize" + size + "\nFile location: " + fileLocation + "\nCreated: " + created);
                alert.setTitle("View Details");

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alert.show();
            }
        });

        return view;
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(getActivity());
            Log.d("TAG", "Can Write Settings: " + retVal);
            if(retVal){
                ///Permission granted by the user
            }else{
                //permission not granted navigate to permission screen
                openAndroidPermissionsMenu();
            }
        }
        return retVal;
    }

    private void openAndroidPermissionsMenu() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}