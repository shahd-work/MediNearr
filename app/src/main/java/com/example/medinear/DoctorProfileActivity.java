package com.example.medinear;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class DoctorProfileActivity extends AppCompatActivity {

    EditText etName, etEmail, etPhone, etSpecialty, etLicense;
    TextView tvProfileName, tvProfileSpecialty, btnSave, btnLogout;
    FirebaseFirestore db;
    FirebaseAuth auth; // ✅ Added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // ✅ Added

        etName             = findViewById(R.id.et_name);
        etEmail            = findViewById(R.id.et_email);
        etPhone            = findViewById(R.id.et_phone);
        etSpecialty        = findViewById(R.id.et_specialty);
        etLicense          = findViewById(R.id.et_license);
        tvProfileName      = findViewById(R.id.tv_profile_name);
        tvProfileSpecialty = findViewById(R.id.tv_profile_specialty);
        btnSave            = findViewById(R.id.btn_save);
        btnLogout          = findViewById(R.id.btn_logout);

        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());

        // ✅ Logout properly — sign out and go to RoleSelectionActivity
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, RoleSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfile() {
        // ✅ Load logged-in doctor's data from users collection
        String uid = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String firstName = doc.getString("firstName") != null ? doc.getString("firstName") : "";
                        String lastName  = doc.getString("lastName")  != null ? doc.getString("lastName")  : "";
                        String fullName  = firstName + " " + lastName;
                        String email     = doc.getString("email")     != null ? doc.getString("email")     : "";
                        String phone     = doc.getString("phone")     != null ? doc.getString("phone")     : "";
                        String specialty = doc.getString("specialization") != null ? doc.getString("specialization") : "";
                        String license   = doc.getString("license")   != null ? doc.getString("license")   : "";

                        etName.setText(fullName.trim());
                        etEmail.setText(email);
                        etPhone.setText(phone);
                        etSpecialty.setText(specialty);
                        etLicense.setText(license);

                        tvProfileName.setText("Dr. " + fullName.trim());
                        tvProfileSpecialty.setText(specialty);
                    }
                });
    }

    private void saveProfile() {
        String uid = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        String fullName  = etName.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String specialty = etSpecialty.getText().toString().trim();
        String license   = etLicense.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Split name into first and last
        String[] parts    = fullName.split(" ");
        String firstName  = parts.length > 0 ? parts[0] : "";
        String lastName   = parts.length > 1 ? fullName.substring(firstName.length()).trim() : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName",      firstName);
        updates.put("lastName",       lastName);
        updates.put("phone",          phone);
        updates.put("specialization", specialty);
        updates.put("license",        license);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // ✅ Update UI immediately
                    tvProfileName.setText("Dr. " + fullName);
                    tvProfileSpecialty.setText(specialty);
                    Toast.makeText(this, "Profile saved! ✅",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}