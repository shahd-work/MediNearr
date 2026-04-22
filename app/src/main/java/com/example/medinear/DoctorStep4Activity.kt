package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorStep4Activity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_step4)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnFinish).setOnClickListener { createAccount() }
    }

    private fun createAccount() {
        val password = findViewById<EditText>(R.id.etPassword).text.toString()
        val confirm  = findViewById<EditText>(R.id.etConfirmPassword).text.toString()

        if (password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        val email = intent.getStringExtra("email") ?: ""

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val doctor = hashMapOf(
                    "uid"            to uid,
                    "role"           to "doctor",
                    "status"         to "pending",
                    "firstName"      to (intent.getStringExtra("firstName") ?: ""),
                    "lastName"       to (intent.getStringExtra("lastName")  ?: ""),
                    "gender"         to (intent.getStringExtra("gender")    ?: ""),
                    "phone"          to (intent.getStringExtra("phone")     ?: ""),
                    "email"          to email,
                    "specialization" to (intent.getStringExtra("specialization") ?: ""),
                    "license"        to (intent.getStringExtra("license")   ?: ""),
                    "experience"     to (intent.getStringExtra("experience")?: ""),
                    "fee"            to (intent.getStringExtra("fee")       ?: ""),
                    "clinic"         to (intent.getStringExtra("clinic")    ?: ""),
                    "address"        to (intent.getStringExtra("address")   ?: ""),
                    "city"           to (intent.getStringExtra("city")      ?: ""),
                    "startTime"      to (intent.getStringExtra("startTime") ?: ""),
                    "endTime"        to (intent.getStringExtra("endTime")   ?: ""),
                    "slotDuration"   to (intent.getStringExtra("slotDuration") ?: ""),
                    "createdAt"      to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(doctor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account submitted for review!", Toast.LENGTH_LONG).show()
                        val i = Intent(this,  DoctorHomeActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(i)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}