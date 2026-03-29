package com.example.safetrack;

import android.os.Bundle;
import android:view.LayoutInflater;
import android:view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android:widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.ArchivedIncident;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArchivesActivity extends AppCompatActivity {

    RecyclerView recyclerArchives;
    TextView tvEmptyArchives;
    List<ArchivedIncident> archiveList = new ArrayList<>();
    ArchiveAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archives);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Archives");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerArchives = findViewById(R.id.recyclerArchives);
        tvEmptyArchives = findViewById(R.id.tvEmptyArchives);

        adapter = new ArchiveAdapter();
        recyclerArchives.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerArchives.setAdapter(adapter);

        loadArchives();
    }

    private void loadArchives() {
        RetrofitClient.getService()
                .getArchivedIncidents("*", "deleted_at.desc")
                .enqueue(new Callback<List<ArchivedIncident>>() {
                    @Override
                    public void onResponse(
                            Call<List<ArchivedIncident>> call,
                            Response<List<ArchivedIncident>> response) {
                        if (response.isSuccessful()
                                && response.body() != null) {
                            archiveList.clear();
                            archiveList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            tvEmptyArchives.setVisibility(
                                    archiveList.isEmpty()
                                            ? View.VISIBLE
                                            : View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<ArchivedIncident>> call,
                            Throwable t) {
                        Toast.makeText(ArchivesActivity.this,
                                "Failed to load archives",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void permanentlyDelete(String id) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permanent Delete")
                .setMessage(
                        "Permanently delete this archived item? " +
                                "This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    RetrofitClient.getService()
                            .deleteArchivedIncident("eq." + id)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(
                                        Call<Void> call,
                                        Response<Void> response) {
                                    Toast.makeText(
                                            ArchivesActivity.this,
                                            "Permanently deleted",
                                            Toast.LENGTH_SHORT).show();
                                    loadArchives();
                                }
                                @Override
                                public void onFailure(
                                        Call<Void> call,
                                        Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    class ArchiveAdapter extends
            RecyclerView.Adapter<ArchiveAdapter.VH> {

        @Override
        public VH onCreateViewHolder(
                ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_archived_incident,
                            parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            ArchivedIncident item = archiveList.get(pos);

            h.tvTitle.setText(item.title);
            h.tvType.setText(item.type);
            h.tvReporter.setText("By: " + item.reporterName);
            h.tvDeletedAt.setText("Deleted: "
                    + (item.deletedAt != null
                    ? item.deletedAt.substring(0, 10)
                    : ""));

            if (item.imageBase64 != null
                    && !item.imageBase64.isEmpty()) {
                h.imgArchived.setVisibility(View.VISIBLE);
                byte[] bytes = android.util.Base64.decode(
                        item.imageBase64,
                        android.util.Base64.DEFAULT);
                Glide.with(ArchivesActivity.this)
                        .load(bytes)
                        .into(h.imgArchived);
            } else {
                h.imgArchived.setVisibility(View.GONE);
            }

            h.btnPermanentDelete.setOnClickListener(v ->
                    permanentlyDelete(item.id));
        }

        @Override
        public int getItemCount() {
            return archiveList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView imgArchived;
            TextView tvType, tvTitle, tvReporter, tvDeletedAt;
            Button btnPermanentDelete;

            VH(View v) {
                super(v);
                imgArchived = v.findViewById(
                        R.id.imgArchivedIncident);
                tvType = v.findViewById(R.id.tvArchivedType);
                tvTitle = v.findViewById(R.id.tvArchivedTitle);
                tvReporter = v.findViewById(
                        R.id.tvArchivedReporter);
                tvDeletedAt = v.findViewById(
                        R.id.tvDeletedAt);
                btnPermanentDelete = v.findViewById(
                        R.id.btnPermanentDelete);
            }
        }
    }
}
