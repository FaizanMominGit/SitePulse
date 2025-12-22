package com.example.sitepulse.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.sitepulse.R;
import com.example.sitepulse.data.local.entity.DailyReport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DprAdapter extends RecyclerView.Adapter<DprAdapter.DprViewHolder> {

    private List<DailyReport> reportList = new ArrayList<>();
    private OnDprClickListener listener;

    public interface OnDprClickListener {
        void onDprClick(DailyReport report);
    }

    public void setOnDprClickListener(OnDprClickListener listener) {
        this.listener = listener;
    }

    public void setReports(List<DailyReport> reports) {
        this.reportList = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DprViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dpr, parent, false);
        return new DprViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DprViewHolder holder, int position) {
        DailyReport report = reportList.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    class DprViewHolder extends RecyclerView.ViewHolder {
        ImageView ivReportImage, ivSyncStatus;
        TextView tvReportDate, tvReportLaborCount;

        public DprViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReportImage = itemView.findViewById(R.id.ivReportImage);
            ivSyncStatus = itemView.findViewById(R.id.ivSyncStatus);
            tvReportDate = itemView.findViewById(R.id.tvReportDate);
            tvReportLaborCount = itemView.findViewById(R.id.tvReportLaborCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDprClick(reportList.get(position));
                }
            });
        }

        public void bind(DailyReport report) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            tvReportDate.setText(sdf.format(new Date(report.date)));
            tvReportLaborCount.setText("Labor: " + report.laborCount);

            // Use Glide to handle image loading from URL or local path
            if (report.imageUrl != null && !report.imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(report.imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivReportImage);
            } else if (report.imagePath != null && !report.imagePath.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(Uri.fromFile(new File(report.imagePath)))
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivReportImage);
            } else {
                // Set a default image if neither path nor URL is available
                ivReportImage.setImageResource(R.drawable.ic_launcher_background);
            }

            ivSyncStatus.setVisibility(report.isSynced ? View.GONE : View.VISIBLE);
        }
    }
}