package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.google.firebase.FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_doctor_dashboard)

        val db = FirebaseFirestore.getInstance()
        android.util.Log.d("FIREBASE", "Connected! $db")

        // Go to Appointments
        findViewById<TextView>(R.id.tv_see_all).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        // Go to Profile
        findViewById<ImageView>(R.id.iv_profile).setOnClickListener {
            startActivity(Intent(this, DoctorProfileActivity::class.java))
        }

        // Go to Availability via notification bell
        findViewById<ImageView>(R.id.iv_notification).setOnClickListener {
            startActivity(Intent(this, AvailabilityActivity::class.java))
        }

        loadDashboardAppointments()
    }

    private fun loadDashboardAppointments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("apointments").get()
            .addOnSuccessListener { result ->
                var total = 0
                var pending = 0
                var newPatients = 0

                for (doc in result) {
                    total++
                    val status = doc.getString("Status") ?: ""
                    val age = doc.get("Age")?.toString()?.toIntOrNull() ?: 0
                    if (status == "Pending") pending++
                    if (age < 18) newPatients++
                }

                findViewById<TextView>(R.id.tv_patient_count).text = total.toString()
                findViewById<TextView>(R.id.tv_pending_count).text = pending.toString()
                findViewById<TextView>(R.id.tv_new_count).text = newPatients.toString()
            }
    }
}