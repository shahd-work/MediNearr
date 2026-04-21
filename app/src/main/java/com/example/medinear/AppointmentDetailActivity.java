package com.example.medinear;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppointmentDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);

        db = FirebaseFirestore.getInstance();
        String id     = getIntent().getStringExtra("id");
        String name   = getIntent().getStringExtra("name");
        String reason = getIntent().getStringExtra("reason");
        String date   = getIntent().getStringExtra("date");
        String time   = getIntent().getStringExtra("time");
        String phone  = getIntent().getStringExtra("phone");
        int    age    = getIntent().getIntExtra("age", 0);
        String gender = getIntent().getStringExtra("gender");
        String status = getIntent().getStringExtra("status");

        ((TextView) findViewById(R.id.tv_patient_name)).setText(name);
        ((TextView) findViewById(R.id.tv_patient_age)).setText(age + " years old · " + gender);
        ((TextView) findViewById(R.id.tv_date)).setText(date);
        ((TextView) findViewById(R.id.tv_time)).setText(time);
        ((TextView) findViewById(R.id.tv_phone)).setText(phone);
        ((TextView) findViewById(R.id.tv_reason)).setText(reason);
        ((TextView) findViewById(R.id.tv_status)).setText(status);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        // ── ACCEPT ──────────────────────────────────────
        Button btnAccept = findViewById(R.id.btn_accept);
        btnAccept.setOnClickListener(v -> {
            db.collection("apointments").document(id)
                    .update("Status", "Confirmed")
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "✅ " + name + " confirmed!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });

        // ── REJECT ──────────────────────────────────────
        Button btnReject = findViewById(R.id.btn_reject);
        btnReject.setOnClickListener(v -> {
            db.collection("apointments").document(id)
                    .update("Status", "Rejected")
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "❌ " + name + " Rejected.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });
    }
}