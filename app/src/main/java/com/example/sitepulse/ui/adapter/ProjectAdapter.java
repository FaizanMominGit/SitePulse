package com.example.sitepulse.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sitepulse.R;
import com.example.sitepulse.data.local.entity.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects = new ArrayList<>();
    private final OnProjectActionListener listener;

    public interface OnProjectActionListener {
        void onDeleteClick(Project project);
        void onItemClick(Project project);
    }

    public ProjectAdapter(OnProjectActionListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_manager, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectName, tvProjectLocation;
        Button btnDelete;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectLocation = itemView.findViewById(R.id.tvProjectLocation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Project project) {
            tvProjectName.setText(project.name);
            tvProjectLocation.setText(project.location);

            itemView.setOnClickListener(v -> listener.onItemClick(project));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(project));
        }
    }
}