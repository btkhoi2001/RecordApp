package com.ag18.record;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< Updated upstream
=======
import android.os.Environment;
import android.os.Handler;
>>>>>>> Stashed changes
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FolderFragment extends Fragment {
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<RecordingItem> recordingList;
    RecodingAdapter recordingAdapter;
    View view;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FolderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FolderFragment newInstance(String param1, String param2) {
        FolderFragment fragment = new FolderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_folder, container, false);

        initData();
        initRecyclerView();

        return view;
    }

    private void initData() {
        recordingList = new ArrayList<>();

        // Sample test
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
        recyclerView = view.findViewById(R.id.rv_recordings);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recordingAdapter = new RecodingAdapter(getContext(), view, recordingList);
        recyclerView.setAdapter(recordingAdapter);
        recordingAdapter.notifyDataSetChanged();
    }
}