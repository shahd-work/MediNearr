package com.example.medinear

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientProfileActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        loadProfile()

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveProfile() }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val firstName = doc.getString("firstName") ?: ""
                val lastName  = doc.getString("lastName")  ?: ""
                val initials  = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
                findViewById<TextView>(R.id.tvProfileAvatar).text = initials
                findViewById<TextView>(R.id.tvProfileName).text   = "$firstName $lastName"
                findViewById<TextView>(R.id.tvProfileEmail).text  = doc.getString("email") ?: ""
                findViewById<EditText>(R.id.etUsername).setText("$firstName $lastName")
                findViewById<EditText>(R.id.etPhone).setText(doc.getString("phone") ?: "")
                findViewById<EditText>(R.id.etCity).setText(doc.getString("city") ?: "")
                findViewById<EditText>(R.id.etDob).setText(doc.getString("dob") ?: "")
                findViewById<EditText>(R.id.etBlood).setText(doc.getString("bloodType") ?: "")
            }
    }

    private fun saveProfile() {
        val uid   = auth.currentUser?.uid ?: return
        val phone = findViewById<EditText>(R.id.etPhone).text.toString().trim()
        val city  = findViewById<EditText>(R.id.etCity).text.toString().trim()

        db.collection("users").document(uid)
            .update(mapOf("phone" to phone, "city" to city))
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
    }
}