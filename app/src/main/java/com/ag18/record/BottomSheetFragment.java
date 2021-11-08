package com.ag18.record;

import android.content.Context;
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

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private ImageButton ibListen, ibShare, ibRename, ibEdit, ibDetails, ibDelete;
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
                NavController navController = Navigation.findNavController(rootView);
                navController.navigate(R.id.action_folderFragment_to_voiceEditorFragment);
                dismiss();
            }
        });

        return view;
    }
}