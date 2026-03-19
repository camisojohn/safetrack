package com.example.safetrack.utils;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.safetrack.api.RetrofitClient;
import com.example.safetrack.models.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recycler;
    SessionManager session;
    List<Notification> notifList = new ArrayList<>();
    NotifAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rv = new RecyclerView(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        setContentView(rv);
        this.recycler = rv;

        session = new SessionManager(this);
        adapter = new NotifAdapter();
        recycler.setAdapter(adapter);

        loadNotifications();
        markAllRead();
    }

    private void loadNotifications() {
        RetrofitClient.getService()
                .getNotifications("*",
                        "eq." + session.getUserId(),
                        "created_at.desc")
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(
                            Call<List<Notification>> call,
                            Response<List<Notification>> res) {
                        if (res.isSuccessful()
                                && res.body() != null) {
                            notifList.clear();
                            notifList.addAll(res.body());
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<List<Notification>> call,
                            Throwable t) {}
                });
    }

    private void markAllRead() {
        Map<String, Object> update = new HashMap<>();
        update.put("is_read", true);
        RetrofitClient.getService()
                .markNotificationRead(
                        "eq." + session.getUserId(), update)
                .enqueue(new Callback<List<Notification>>() {
                    @Override
                    public void onResponse(
                            Call<List<Notification>> call,
                            Response<List<Notification>> res) {}
                    @Override
                    public void onFailure(
                            Call<List<Notification>> call,
                            Throwable t) {}
                });
    }

    class NotifAdapter extends
            RecyclerView.Adapter<NotifAdapter.VH> {
        @Override
        public VH onCreateViewHolder(
                ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(
                            android.R.layout.simple_list_item_2,
                            parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Notification n = notifList.get(pos);
            h.text1.setText(n.message);
            h.text2.setText(n.createdAt != null
                    ? n.createdAt.substring(0, 10) : "");
            h.itemView.setAlpha(n.isRead ? 0.6f : 1.0f);
        }

        @Override
        public int getItemCount() {
            return notifList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}