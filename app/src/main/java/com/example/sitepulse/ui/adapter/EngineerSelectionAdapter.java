package com.example.sitepulse.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.R;
import com.example.sitepulse.data.local.entity.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EngineerSelectionAdapter extends RecyclerView.Adapter<EngineerSelectionAdapter.EngineerViewHolder> {

    private List<User> allEngineers = new ArrayList<>();
    private List<User> filteredEngineers = new ArrayList<>();
    private final Set<String> selectedEngineerIds = new HashSet<>();

    public void setEngineers(List<User> engineers) {
        this.allEngineers = new ArrayList<>(engineers);
        this.filteredEngineers = new ArrayList<>(engineers);
        notifyDataSetChanged();
    }

    public void setSelectedEngineerIds(Set<String> selectedIds) {
        this.selectedEngineerIds.clear();
        this.selectedEngineerIds.addAll(selectedIds);
        notifyDataSetChanged();
    }
    
    public void filter(String query) {
        filteredEngineers.clear();
        if (query.isEmpty()) {
            filteredEngineers.addAll(allEngineers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allEngineers) {
                if (user.name.toLowerCase().contains(lowerQuery) || user.email.toLowerCase().contains(lowerQuery)) {
                    filteredEngineers.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Set<String> getSelectedEngineerIds() {
        return selectedEngineerIds;
    }

    @NonNull
    @Override
    public EngineerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_engineer_selection, parent, false);
        return new EngineerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EngineerViewHolder holder, int position) {
        User user = filteredEngineers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return filteredEngineers.size();
    }

    class EngineerViewHolder extends RecyclerView.ViewHolder {
        TextView tvEngineerName, tvEngineerEmail;
        CheckBox cbSelect;

        public EngineerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEngineerName = itemView.findViewById(R.id.tvEngineerName);
            tvEngineerEmail = itemView.findViewById(R.id.tvEngineerEmail);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }

        public void bind(User user) {
            tvEngineerName.setText(user.name);
            tvEngineerEmail.setText(user.email);
            
            // Remove listener before setting state to avoid triggers
            cbSelect.setOnCheckedChangeListener(null);
            cbSelect.setChecked(selectedEngineerIds.contains(user.id));

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedEngineerIds.add(user.id);
                } else {
                    selectedEngineerIds.remove(user.id);
                }
            });
            
            itemView.setOnClickListener(v -> cbSelect.toggle());
        }
    }
}