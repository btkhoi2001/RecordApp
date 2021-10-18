package com.ag18.record;

import android.animation.ValueAnimator;
import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class RecodingAdapter extends RecyclerView.Adapter<RecodingAdapter.ViewHolder> {
    private List<RecordingItem> recordingList;

    public RecodingAdapter(List<RecordingItem> recordingList) {
        this.recordingList = recordingList;
    }

    @NonNull
    @Override
    public RecodingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_item_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecodingAdapter.ViewHolder holder, int position) {
        String title = recordingList.get(position).getTitle();
        String createdAt = recordingList.get(position).getCreatedAt();
        String duration = recordingList.get(position).getDuration();

        holder.setData(title, createdAt, duration);
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvCreatedAt;
        private TextView tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = (TextView)itemView.findViewById(R.id.tv_title);
            tvCreatedAt = (TextView)itemView.findViewById(R.id.tv_createdat);
            tvDuration = (TextView)itemView.findViewById(R.id.tv_duration);
        }

        public void setData(String title, String createdAt, String duration) {
            tvTitle.setText(title);
            tvCreatedAt.setText(createdAt);
            tvDuration.setText(duration);
        }
    }
}
