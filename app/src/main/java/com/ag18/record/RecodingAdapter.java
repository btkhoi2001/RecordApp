package com.ag18.record;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.OnClick;

public class RecodingAdapter extends RecyclerView.Adapter<RecodingAdapter.ViewHolder> {
    private List<RecordingItem> recordingList;
    private Context context;
    private View rootView;

    public RecodingAdapter(Context context, View rootView, List<RecordingItem> recordingList) {
        this.context = context;
        this.rootView = rootView;
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
        private ImageButton ibMoreItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = (TextView)itemView.findViewById(R.id.tv_title);
            tvCreatedAt = (TextView)itemView.findViewById(R.id.tv_createdat);
            tvDuration = (TextView)itemView.findViewById(R.id.tv_duration);
            ibMoreItems = (ImageButton)itemView.findViewById(R.id.ib_more_items);

            ibMoreItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(rootView);
                    bottomSheetFragment.show(((AppCompatActivity)context).getSupportFragmentManager(), bottomSheetFragment.getTag());
                }
            });
        }

        public void setData(String title, String createdAt, String duration) {
            tvTitle.setText(title);
            tvCreatedAt.setText(createdAt);
            tvDuration.setText(duration);
        }
    }
}
