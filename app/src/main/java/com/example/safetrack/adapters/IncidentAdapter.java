package com.example.safetrack.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.safetrack.IncidentDetailActivity;
import com.example.safetrack.R;
import com.example.safetrack.models.Incident;
import java.util.List;

public class IncidentAdapter extends
        RecyclerView.Adapter<IncidentAdapter.ViewHolder> {

    private Context context;
    private List<Incident> incidents;
    private OnIncidentActionListener listener;
    private String currentUserId;

    public interface OnIncidentActionListener {
        void onUpvote(String incidentId, int currentUpvotes);
        void onEdit(Incident incident);
        void onDelete(String incidentId);
    }

    public IncidentAdapter(Context context,
                           List<Incident> incidents,
                           OnIncidentActionListener listener,
                           String currentUserId) {
        this.context       = context;
        this.incidents     = incidents;
        this.listener      = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_incident,
                        parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h, int pos) {
        Incident item = incidents.get(pos);

        h.tvTitle.setText(item.title);
        h.tvType.setText(item.type);
        h.tvDescription.setText(item.description);
        h.tvLocation.setText("📍 " + item.location);
        h.tvStatus.setText(item.status);
        h.btnUpvote.setText("▲ " + item.upvotes);

        // Reporter — respect anonymous setting
        if (item.isAnonymous) {
            h.tvReporter.setText("By: Anonymous");
        } else {
            h.tvReporter.setText("By: " + item.reporterName);
        }

        // Status color
        if (item.status != null) {
            switch (item.status) {
                case "Pending":
                    h.tvStatus.setBackgroundColor(
                            Color.parseColor("#FFA500"));
                    break;
                case "Reviewed":
                    h.tvStatus.setBackgroundColor(
                            Color.parseColor("#2196F3"));
                    break;
                case "In Progress":
                    h.tvStatus.setBackgroundColor(
                            Color.parseColor("#9C27B0"));
                    break;
                case "Resolved":
                    h.tvStatus.setBackgroundColor(
                            Color.parseColor("#4CAF50"));
                    break;
                default:
                    h.tvStatus.setBackgroundColor(
                            Color.parseColor("#888888"));
            }
        }

        // Dim resolved incidents
        h.itemView.setAlpha(
                "Resolved".equals(item.status) ? 0.6f : 1.0f);

        // Load image
        if (item.imageBase64 != null
                && !item.imageBase64.isEmpty()) {
            h.imgIncident.setVisibility(View.VISIBLE);
            byte[] bytes = android.util.Base64.decode(
                    item.imageBase64,
                    android.util.Base64.DEFAULT);
            Glide.with(context)
                    .load(bytes)
                    .into(h.imgIncident);
        } else {
            h.imgIncident.setVisibility(View.GONE);
        }

        // Show Edit/Delete only for own posts
        boolean isOwner = currentUserId != null
                && currentUserId.equals(item.reporterId);

        h.btnEdit.setVisibility(
                isOwner ? View.VISIBLE : View.GONE);
        h.btnDelete.setVisibility(
                isOwner ? View.VISIBLE : View.GONE);

        // ✅ Click card to open detail
        // — CORRECTLY placed in onBindViewHolder
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context,
                    IncidentDetailActivity.class);
            intent.putExtra("id",
                    item.id);
            intent.putExtra("title",
                    item.title);
            intent.putExtra("type",
                    item.type);
            intent.putExtra("description",
                    item.description);
            intent.putExtra("location",
                    item.location);
            intent.putExtra("status",
                    item.status);
            intent.putExtra("reporter_id",
                    item.reporterId);
            intent.putExtra("reporter_name",
                    item.reporterName);
            intent.putExtra("is_anonymous",
                    item.isAnonymous);
            intent.putExtra("upvotes",
                    item.upvotes);
            intent.putExtra("image_base64",
                    item.imageBase64);
            intent.putExtra("created_at",
                    item.createdAt);
            context.startActivity(intent);
        });

        // Upvote
        h.btnUpvote.setOnClickListener(v ->
                listener.onUpvote(item.id, item.upvotes));

        // Edit
        h.btnEdit.setOnClickListener(v ->
                listener.onEdit(item));

        // Delete
        h.btnDelete.setOnClickListener(v ->
                listener.onDelete(item.id));
    }

    @Override
    public int getItemCount() {
        return incidents.size();
    }

    // ✅ ViewHolder — fields ONLY, no code here
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIncident;
        TextView tvType, tvTitle, tvDescription,
                tvLocation, tvStatus, tvReporter;
        Button btnUpvote, btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            imgIncident   = v.findViewById(R.id.imgIncident);
            tvType        = v.findViewById(R.id.tvType);
            tvTitle       = v.findViewById(R.id.tvTitle);
            tvDescription = v.findViewById(
                    R.id.tvDescription);
            tvLocation    = v.findViewById(R.id.tvLocation);
            tvStatus      = v.findViewById(R.id.tvStatus);
            tvReporter    = v.findViewById(R.id.tvReporter);
            btnUpvote     = v.findViewById(R.id.btnUpvote);
            btnEdit       = v.findViewById(R.id.btnEdit);
            btnDelete     = v.findViewById(R.id.btnDelete);
        }
    }
}