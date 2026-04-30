package com.example.medinear;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {

    private RecyclerView rvAppointments;
    private List<Appointment> appointmentList = new ArrayList<>();
    private List<Appointment> filteredList = new ArrayList<>();
    private AppointmentAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth; // ✅ Added

    private TextView tabAll, tabPending, tabConfirmed, tabRejected;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        auth = FirebaseAuth.getInstance(); // ✅ Added
        db   = FirebaseFirestore.getInstance();

        tabAll       = findViewById(R.id.tab_all);
        tabPending   = findViewById(R.id.tab_pending);
        tabConfirmed = findViewById(R.id.tab_confirmed);
        tabRejected  = findViewById(R.id.tab_rejected);

        tabAll.setOnClickListener(v -> applyFilter("All"));
        tabPending.setOnClickListener(v -> applyFilter("pending"));
        tabConfirmed.setOnClickListener(v -> applyFilter("confirmed"));
        tabRejected.setOnClickListener(v -> applyFilter("cancelled"));

        rvAppointments = findViewById(R.id.rv_appointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentAdapter(filteredList);
        rvAppointments.setAdapter(adapter);

        loadAppointments();
    }

    private void applyFilter(String filter) {
        currentFilter = filter;

        tabAll.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabAll.setTextColor(0xFFDBEAFE);
        tabPending.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabPending.setTextColor(0xFFDBEAFE);
        tabConfirmed.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabConfirmed.setTextColor(0xFFDBEAFE);
        tabRejected.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabRejected.setTextColor(0xFFDBEAFE);

        switch (filter) {
            case "All":
                tabAll.setBackgroundResource(R.drawable.bg_tab_selected);
                tabAll.setTextColor(0xFF5BA4CF);
                break;
            case "pending":
                tabPending.setBackgroundResource(R.drawable.bg_tab_selected);
                tabPending.setTextColor(0xFF5BA4CF);
                break;
            case "confirmed":
                tabConfirmed.setBackgroundResource(R.drawable.bg_tab_selected);
                tabConfirmed.setTextColor(0xFF5BA4CF);
                break;
            case "cancelled":
                tabRejected.setBackgroundResource(R.drawable.bg_tab_selected);
                tabRejected.setTextColor(0xFF5BA4CF);
                break;
        }

        filteredList.clear();
        if (filter.equals("All")) {
            filteredList.addAll(appointmentList);
        } else {
            for (Appointment apt : appointmentList) {
                // ✅ case-insensitive comparison
                if (apt.status != null && apt.status.equalsIgnoreCase(filter)) {
                    filteredList.add(apt);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadAppointments() {
        // ✅ Get logged-in doctor's UID
        String doctorUid = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid() : null;

        if (doctorUid == null) return;

        // ✅ Filter by doctorUid
        db.collection("apointments")
                .whereEqualTo("doctorUid", doctorUid)
                .get()
                .addOnSuccessListener(result -> {
                    appointmentList.clear();
                    for (QueryDocumentSnapshot doc : result) {

                        Appointment apt = new Appointment(
                                doc.getId(),
                                doc.getString("doctorName"),  // ✅ real field names
                                doc.getString("note"),
                                doc.getString("date"),
                                doc.getString("timeSlot"),
                                "",                           // phone not saved yet
                                0,                            // age not saved yet
                                "",                           // gender not saved yet
                                doc.getString("status")
                        );
                        appointmentList.add(apt);
                    }
                    applyFilter("All");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FIRESTORE", "Error: " + e.getMessage());
                });
    }

    public static class Appointment {
        public String id, name, reason, date, time, phone, status, gender;
        public int age;

        public Appointment(String id, String name, String reason, String date,
                           String time, String phone, int age, String gender, String status) {
            this.id     = id;
            this.name   = name;
            this.reason = reason;
            this.date   = date;
            this.time   = time;
            this.phone  = phone;
            this.age    = age;
            this.gender = gender;
            this.status = status;
        }
    }

    public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

        private List<Appointment> list;

        public AppointmentAdapter(List<Appointment> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_appointment_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Appointment apt = list.get(position);

            holder.tvName.setText(apt.name);
            holder.tvReason.setText(apt.reason);
            holder.tvDate.setText(apt.date);
            holder.tvTime.setText(apt.time);
            holder.tvStatus.setText(apt.status);

            int[] colors = {
                    0xFF1A56DB, 0xFF16A34A, 0xFFDC2626, 0xFF9333EA,
                    0xFFEA580C, 0xFF0891B2, 0xFFDB2777, 0xFF65A30D,
                    0xFFD97706, 0xFF0284C7
            };

            if (apt.name != null && !apt.name.isEmpty()) {
                holder.tvAvatar.setText(String.valueOf(apt.name.charAt(0)).toUpperCase());
                int colorIndex = Math.abs(apt.name.charAt(0)) % colors.length;
                holder.tvAvatar.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(colors[colorIndex])
                );
            } else {
                holder.tvAvatar.setText("?");
                holder.tvAvatar.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFF94A3B8)
                );
            }

            // ✅ lowercase status comparison
            switch (apt.status != null ? apt.status : "") {
                case "confirmed":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_green);
                    holder.tvStatus.setTextColor(0xFF16A34A);
                    break;
                case "cancelled":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_red);
                    holder.tvStatus.setTextColor(0xFFDC2626);
                    break;
                default:
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_orange);
                    holder.tvStatus.setTextColor(0xFFEA580C);
                    break;
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AppointmentsActivity.this, AppointmentDetailActivity.class);
                intent.putExtra("id",     apt.id);
                intent.putExtra("name",   apt.name);
                intent.putExtra("reason", apt.reason);
                intent.putExtra("date",   apt.date);
                intent.putExtra("time",   apt.time);
                intent.putExtra("phone",  apt.phone);
                intent.putExtra("age",    apt.age);
                intent.putExtra("gender", apt.gender);
                intent.putExtra("status", apt.status);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvReason, tvDate, tvTime, tvStatus, tvAvatar;
            public ViewHolder(View v) {
                super(v);
                tvName   = v.findViewById(R.id.tv_patient_name);
                tvReason = v.findViewById(R.id.tv_reason);
                tvDate   = v.findViewById(R.id.tv_date);
                tvTime   = v.findViewById(R.id.tv_time);
                tvStatus = v.findViewById(R.id.tv_status);
                tvAvatar = v.findViewById(R.id.tv_avatar);
            }
        }
    }
}