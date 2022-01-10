package com.ag18.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SettingsFragment extends Fragment {


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

            if(preferences.getString("recording_folder", "") == "")
            {
                path = String.valueOf(Environment.getExternalStorageDirectory() + "/RecordApp");
                editor.putString("recording_folder", path);
                editor.apply();
            } else
            {
                path = preferences.getString("recording_folder", "");
            }

            preference_recording_folder.setSummary(path);

            preference_about_us.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
                    View view = inflater.inflate(R.layout.dialog_about_us, null);

                    String name[] = {
                            "19120260 - Hoàng Trần Thiên Khôi",
                            "19120272 - Nguyễn Sĩ Liêm",
                            "19120402 - Huỳnh Nguyễn Sơn Trà",
                            "19120452 - Trần Trọng Hoàng Anh",
                            "19120549 - Bạch Thiên Khôi"};

                    String github[] = {
                            "https://github.com/thienkhoi0604",
                            "https://github.com/ngslim",
                            "https://github.com/gftrftrdfr",
                            "https://github.com/hatieudao",
                            "https://github.com/btkhoi2001"
                    };

                    ListView studentList = view.findViewById(R.id.student_list);

                    ArrayAdapter<String> nameArray = new ArrayAdapter<String>(getContext(), R.layout.line_student, name);

                    studentList.setAdapter(nameArray);

                    studentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(github[position]));
                            startActivity(browserIntent);
                        }
                    });

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
}