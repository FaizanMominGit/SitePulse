package com.example.sitepulse.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitepulse.R;
import com.example.sitepulse.data.local.entity.MaterialRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerMaterialAdapter extends RecyclerView.Adapter<ManagerMaterialAdapter.ManagerMaterialViewHolder> {

    private List<MaterialRequest> requestList = new ArrayList<>();
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onApproveClick(MaterialRequest request);
        void onRejectClick(MaterialRequest request);
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        this.listener = listener;
    }

    public void setMaterialRequests(List<MaterialRequest> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ManagerMaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager_material_request, parent, false);
        return new ManagerMaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManagerMaterialViewHolder holder, int position) {
        MaterialRequest request = requestList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class ManagerMaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQuantity, tvDate, tvStatus, tvUrgency, tvEngineerName;
        Button btnApprove, btnReject;

        public ManagerMaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            tvEngineerName = itemView.findViewById(R.id.tvEngineerName);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);

            btnApprove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onApproveClick(requestList.get(position));
                }
            });

            btnReject.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onRejectClick(requestList.get(position));
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

            if (request.userId != null) {
                tvEngineerName.setText("Requested by: " + request.userId);
            } else {
                tvEngineerName.setText("Requested by: Unknown");
            }

            if ("PENDING".equalsIgnoreCase(request.status)) {
                btnApprove.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            } else {
                btnApprove.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
            }
        }
    }
}