package com.example.sitepulse.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitepulse.R;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    private List<MaterialRequest> requestList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MaterialRequest request);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRequests(List<MaterialRequest> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material_request, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        MaterialRequest request = requestList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQuantity, tvDate, tvStatus, tvUrgency;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(requestList.get(position));
                }
            });
        }

        public void bind(MaterialRequest request) {
            tvItemName.setText(request.itemName);
            tvQuantity.setText(request.quantity + " " + request.unit);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(request.date)));
            
            tvStatus.setText(request.status);
            tvUrgency.setText(request.urgency + " Urgency");
            
            // Set Status Background Color
            if ("APPROVED".equalsIgnoreCase(request.status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_approved);
            } else if ("REJECTED".equalsIgnoreCase(request.status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            }
        }
    }
}