package com.ag18.record;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.List;

public class RecodingAdapter extends RecyclerView.Adapter<RecodingAdapter.ViewHolder> {

    private View rootView;
    private File[] allFiles;
    private Context context;
    private onItemListClick onItemListClick;

    Activity activity;

    public RecodingAdapter(Context context, View rootView, File[] allFiles, onItemListClick onItemListClick) {
        this.context = context;
        this.rootView = rootView;
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_item_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.list_title.setText(allFiles[position].getName());
        holder.list_date.setText(Utils.getTime(allFiles[position].lastModified()));
    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView list_title;
        private TextView list_date;
        private ImageButton ibMoreItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            list_title = itemView.findViewById(R.id.tv_title);
            list_date = itemView.findViewById(R.id.tv_createdat);
            ibMoreItems = (ImageButton)itemView.findViewById(R.id.ib_more_items);

            ibMoreItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(rootView, allFiles[getAdapterPosition()].getName());
                    bottomSheetFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), bottomSheetFragment.getTag());
                }
            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition());
        }
    }

    public interface onItemListClick {
        void onClickListener(File file, int position);
    }
}
