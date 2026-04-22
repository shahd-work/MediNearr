package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RoleSelectionActivity : ComponentActivity() {

    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto login if user already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role") ?: ""
                    val intent = when (role) {
                        "patient" -> Intent(this, PatientHomeActivity::class.java)
                        "doctor"  -> Intent(this, DoctorHomeActivity::class.java)
                        else      -> null
                    }
                    intent?.let {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }
        }

        setContentView(R.layout.activity_role_selection)

        val cardPatient = findViewById<LinearLayout>(R.id.cardPatient)
        val cardDoctor  = findViewById<LinearLayout>(R.id.cardDoctor)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val btnSignIn   = findViewById<Button>(R.id.btnSignIn)

        cardPatient.setOnClickListener {
            selectedRole = "patient"
            cardPatient.setBackgroundResource(R.drawable.card_role_selected)
            cardDoctor.setBackgroundResource(R.drawable.card_role_unselected)
            btnContinue.isEnabled = true
        }

        cardDoctor.setOnClickListener {
            selectedRole = "doctor"
            cardDoctor.setBackgroundResource(R.drawable.card_role_selected)
            cardPatient.setBackgroundResource(R.drawable.card_role_unselected)
            btnContinue.isEnabled = true
        }

        btnContinue.setOnClickListener {
            when (selectedRole) {
                "patient" -> startActivity(
                    Intent(this, PatientStep1Activity::class.java)
                )
                "doctor"  -> startActivity(
                    Intent(this, DoctorStep1Activity::class.java)
                )
                else -> Toast.makeText(
                    this, "Please select a role", Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}