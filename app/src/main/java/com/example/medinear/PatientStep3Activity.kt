package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientStep3Activity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_step3)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        val bloodTypes = listOf("Unknown","A+","A-","B+","B-","AB+","AB-","O+","O-")
        findViewById<Spinner>(R.id.spinnerBlood).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bloodTypes)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnFinish).setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val password   = findViewById<EditText>(R.id.etPassword).text.toString()
        val confirm    = findViewById<EditText>(R.id.etConfirmPassword).text.toString()
        val blood      = findViewById<Spinner>(R.id.spinnerBlood).selectedItem.toString()
        val conditions = findViewById<EditText>(R.id.etConditions).text.toString().trim()

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

        val firstName = intent.getStringExtra("firstName") ?: ""
        val lastName  = intent.getStringExtra("lastName")  ?: ""
        val dob       = intent.getStringExtra("dob")       ?: ""
        val gender    = intent.getStringExtra("gender")    ?: ""
        val phone     = intent.getStringExtra("phone")     ?: ""
        val email     = intent.getStringExtra("email")     ?: ""
        val city      = intent.getStringExtra("city")      ?: ""

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val patient = hashMapOf(
                    "uid"        to uid,
                    "role"       to "patient",
                    "firstName"  to firstName,
                    "lastName"   to lastName,
                    "dob"        to dob,
                    "gender"     to gender,
                    "phone"      to phone,
                    "email"      to email,
                    "city"       to city,
                    "bloodType"  to blood,
                    "conditions" to conditions ,
                    "createdAt"  to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(patient)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        val i = Intent(this,  PatientHomeActivity::class.java)
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