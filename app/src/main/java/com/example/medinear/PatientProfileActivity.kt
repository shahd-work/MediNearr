package com.example.medinear

import android.content.Intent
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

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
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
        val uid      = auth.currentUser?.uid ?: return
        val fullName = findViewById<EditText>(R.id.etUsername).text.toString().trim()
        val phone    = findViewById<EditText>(R.id.etPhone).text.toString().trim()
        val city     = findViewById<EditText>(R.id.etCity).text.toString().trim()
        val dob      = findViewById<EditText>(R.id.etDob).text.toString().trim()
        val blood    = findViewById<EditText>(R.id.etBlood).text.toString().trim()

        // ✅ Split full name into first and last
        val nameParts = fullName.split(" ")
        val firstName = nameParts.firstOrNull() ?: ""
        val lastName  = nameParts.drop(1).joinToString(" ")

        db.collection("users").document(uid)
            .update(mapOf(
                "firstName" to firstName,
                "lastName"  to lastName,
                "phone"     to phone,
                "city"      to city,
                "dob"       to dob,
                "bloodType" to blood
            ))
            .addOnSuccessListener {
                // ✅ Update UI immediately
                findViewById<TextView>(R.id.tvProfileName).text = fullName
                val initials = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
                findViewById<TextView>(R.id.tvProfileAvatar).text = initials
                Toast.makeText(this, "Profile updated! ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}