package com.ag18.record;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {
//
//    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
//    private static final String LOG_TAG = "AndroidExample";

    private static String path;
    public SettingsFragment() {

    }
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsPreferenceFragment())
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public static class SettingsPreferenceFragment extends PreferenceFragmentCompat {
        @SuppressLint("ResourceAsColor")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Preference preference_about_us = (Preference) findPreference("about_us");
            Preference preference_recording_folder = (Preference) findPreference("recording_folder");

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();

            path = String.valueOf(Environment.getExternalStorageDirectory());
            preference_recording_folder.setSummary(path);
            System.out.println(path);

            preference_about_us.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view = inflater.inflate(R.layout.about_us_dialog, null);

                    String name[] = {"19120260 - Hoàng Trần Thiên Khôi",
                            "19120272 - Nguyễn Sỹ Liêm",
                            "19120402 - Huỳnh Nguyễn Sơn Trà",
                            "19120452 - Trần Trọng Hoàng Anh",
                            "19120549 - Bạch Thiên Khôi"};

                    ListView studentList = view.findViewById(R.id.student_list);

                    ArrayAdapter<String> nameArray = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, name);
                    studentList.setAdapter(nameArray);

                    builder.setView(view);

                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }
            });

            preference_recording_folder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final String[] m_chosenDir = {""};
                    boolean m_newFolderEnabled = true;

                    DirectoryChooserDialog directoryChooserDialog =
                            new DirectoryChooserDialog(getActivity(),
                                    new DirectoryChooserDialog.ChosenDirectoryListener()
                                    {
                                        @Override
                                        public void onChosenDir(String chosenDir)
                                        {
                                            m_chosenDir[0] = chosenDir;
                                            Toast.makeText(
                                                    getActivity(), "Chosen directory: " +
                                                            chosenDir, Toast.LENGTH_LONG).show();
                                            path = chosenDir;
                                            preference_recording_folder.setSummary(path);
                                            //preference_recording_folder.setDefaultValue(path);
                                            editor.putString("recording_folder", path);
                                            editor.apply();
                                        }
                                    });
                    // Toggle new folder button enabling
                    directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
                    // Load directory chooser dialog for initial 'm_chosenDir' directory.
                    // The registered callback will be called upon final directory selection.
                    directoryChooserDialog.chooseDirectory(m_chosenDir[0]);
                    m_newFolderEnabled = ! m_newFolderEnabled;

                    return true;
                }
            });
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case MY_RESULT_CODE_FILECHOOSER:
//                if (resultCode == Activity.RESULT_OK ) {
////                    if(data != null)  {
////                        Uri fileUri = data.getData();
////                        Log.i(LOG_TAG, "Uri: " + fileUri);
////
////                        String filePath = null;
////                        try {
////                            filePath = FileUtils.getPath(this.getContext(),fileUri);
////                        } catch (Exception e) {
////                            Log.e(LOG_TAG,"Error: " + e);
////                            Toast.makeText(this.getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
////                        }
//////                        this.editTextPath.setText(filePath);
//////                        path = filePath;
////                    }
//                    path = String.valueOf(data.getData());
//                }
//                break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//
//    private static class FileUtils {
//        public static String getPath(Context context, Uri uri) throws URISyntaxException {
//            if ("content".equalsIgnoreCase(uri.getScheme())) {
//                String[] projection = { "_data" };
//                Cursor cursor = null;
//
//                try {
//                    cursor = context.getContentResolver().query(uri, projection, null, null, null);
//                    int column_index = cursor.getColumnIndexOrThrow("_data");
//                    if (cursor.moveToFirst()) {
//                        return cursor.getString(column_index);
//                    }
//                } catch (Exception e) {
//                    // Eat it
//                }
//            }
//            else if ("file".equalsIgnoreCase(uri.getScheme())) {
//                return uri.getPath();
//            }
//
//            return null;
//        }
//    }
//
//    public void ShowDirectoryPicker(){
//        // 1. Initialize dialog
//        final StorageChooser chooser = new StorageChooser.Builder()
//                .withActivity(getActivity())
//                .withFragmentManager(getFragmentManager())
//                .withMemoryBar(true)
//                .allowCustomPath(true)
//                .setType(StorageChooser.DIRECTORY_CHOOSER)
//                .build();
//
//        // 2. Retrieve the selected path by the user and show in a toast !
//        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
//            @Override
//            public void onSelect(String path) {
//                Toast.makeText(getActivity(), "The selected path is : " + path, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // 3. Display File Picker !
//        chooser.show();
//    }
}