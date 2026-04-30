package com.example.medinear;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DoctorProfileActivity extends AppCompatActivity {

    EditText etName, etEmail, etPhone, etSpecialty, etLicense;
    TextView tvProfileName, tvProfileSpecialty, btnSave, btnLogout;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        db = FirebaseFirestore.getInstance();

        etName       = findViewById(R.id.et_name);
        etEmail      = findViewById(R.id.et_email);
        etPhone      = findViewById(R.id.et_phone);
        etSpecialty  = findViewById(R.id.et_specialty);
        etLicense    = findViewById(R.id.et_license);
        tvProfileName      = findViewById(R.id.tv_profile_name);
        tvProfileSpecialty = findViewById(R.id.tv_profile_specialty);
        btnSave    = findViewById(R.id.btn_save);
        btnLogout  = findViewById(R.id.btn_logout);

        // Load existing profile from Firestore
        loadProfile();

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());

        // Logout button
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfile() {
        db.collection("doctor_profile").document("main_doctor")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name      = doc.getString("Name");
                        String email     = doc.getString("Email");
                        String phone     = doc.getString("Phone");
                        String specialty = doc.getString("Specialty");
                        String license   = doc.getString("License");

                        etName.setText(name);
                        etEmail.setText(email);
                        etPhone.setText(phone);
                        etSpecialty.setText(specialty);
                        etLicense.setText(license);

                        tvProfileName.setText(name != null ? name : "Dr. Name");
                        tvProfileSpecialty.setText(specialty != null ? specialty : "Specialty");
                    }
                });
    }

    private void saveProfile() {
        String name      = etName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String specialty = etSpecialty.getText().toString().trim();
        String license   = etLicense.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("Name",      name);
        profile.put("Email",     email);
        profile.put("Phone",     phone);
        profile.put("Specialty", specialty);
        profile.put("License",   license);

        db.collection("doctor_profile").document("main_doctor")
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved! ✅", Toast.LENGTH_SHORT).show();
                    tvProfileName.setText(name);
                    tvProfileSpecialty.setText(specialty);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
