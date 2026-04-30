package com.example.medinear;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

        // ✅ Set data
        ((TextView) findViewById(R.id.tv_patient_name)).setText(name);
        ((TextView) findViewById(R.id.tv_patient_age)).setText(
                (age > 0 ? age + " years old" : "") +
                        (gender != null && !gender.isEmpty() ? " · " + gender : "")
        );
        ((TextView) findViewById(R.id.tv_date)).setText(date);
        ((TextView) findViewById(R.id.tv_time)).setText(time);
        ((TextView) findViewById(R.id.tv_phone)).setText(
                phone != null && !phone.isEmpty() ? phone : "N/A"
        );
        ((TextView) findViewById(R.id.tv_reason)).setText(
                reason != null && !reason.isEmpty() ? reason : "No reason provided"
        );

        // ✅ Status badge with correct colors
        TextView tvStatus = findViewById(R.id.tv_status);
        tvStatus.setText(status);
        if (status != null) {
            switch (status.toLowerCase()) {
                case "confirmed":
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_green);
                    tvStatus.setTextColor(0xFF16A34A);
                    break;
                case "cancelled":
                case "rejected":
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_red);
                    tvStatus.setTextColor(0xFFDC2626);
                    break;
                default:
                    tvStatus.setBackgroundResource(R.drawable.bg_tag_orange);
                    tvStatus.setTextColor(0xFFEA580C);
                    break;
            }
        }

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        Button btnAccept = findViewById(R.id.btn_accept);
        Button btnReject = findViewById(R.id.btn_reject);

        // ✅ Disable buttons if already actioned
        if ("confirmed".equalsIgnoreCase(status)) {
            btnAccept.setEnabled(false);
            btnAccept.setAlpha(0.5f);
        }
        if ("cancelled".equalsIgnoreCase(status) ||
                "rejected".equalsIgnoreCase(status)) {
            btnReject.setEnabled(false);
            btnReject.setAlpha(0.5f);
        }

        // ✅ Accept — save lowercase "confirmed"
        btnAccept.setOnClickListener(v -> {
            if (id == null) return;
            String notes = ((EditText) findViewById(R.id.et_notes))
                    .getText().toString().trim();

            db.collection("apointments").document(id)
                    .update("status", "confirmed",
                            "doctorNotes", notes)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "✅ Appointment confirmed!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });

        // ✅ Reject — save lowercase "cancelled"
        btnReject.setOnClickListener(v -> {
            if (id == null) return;
            db.collection("apointments").document(id)
                    .update("status", "cancelled")
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "❌ Appointment rejected.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });
    }
}