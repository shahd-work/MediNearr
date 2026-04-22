package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class AppointmentSuccessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_success)

        val doctorName = intent.getStringExtra("doctorName") ?: ""
        val date       = intent.getStringExtra("date")       ?: ""
        val timeSlot   = intent.getStringExtra("timeSlot")   ?: ""

        findViewById<TextView>(R.id.tvSumDoc).text  = doctorName
        findViewById<TextView>(R.id.tvSumDate).text = date
        findViewById<TextView>(R.id.tvSumTime).text = timeSlot

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, PatientHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}