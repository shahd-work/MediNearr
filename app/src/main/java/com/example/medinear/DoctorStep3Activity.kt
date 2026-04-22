package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity

class DoctorStep3Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_step3)

        val slots = listOf("15 min", "20 min", "30 min", "45 min", "60 min")
        findViewById<Spinner>(R.id.spinnerSlot).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, slots)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            android.util.Log.d("STEP3", "Next button clicked!")
            startActivity(Intent(this, DoctorStep4Activity::class.java))
            val clinic = findViewById<EditText>(R.id.etClinic).text.toString().trim()
            val address = findViewById<EditText>(R.id.etAddress).text.toString().trim()
            val city = findViewById<EditText>(R.id.etCity).text.toString().trim()
            val startTime = findViewById<EditText>(R.id.etStartTime).text.toString().trim()
            val endTime = findViewById<EditText>(R.id.etEndTime).text.toString().trim()
            val slot = findViewById<Spinner>(R.id.spinnerSlot).selectedItem?.toString() ?: "20 min"

            // Show exactly which field is empty for debugging
            when {
                clinic.isEmpty() -> Toast.makeText(
                    this,
                    "Please enter clinic name",
                    Toast.LENGTH_SHORT
                ).show()

                address.isEmpty() -> Toast.makeText(
                    this,
                    "Please enter address",
                    Toast.LENGTH_SHORT
                ).show()

                city.isEmpty() -> Toast.makeText(this, "Please enter city", Toast.LENGTH_SHORT)
                    .show()

                startTime.isEmpty() -> Toast.makeText(
                    this,
                    "Please enter start time",
                    Toast.LENGTH_SHORT
                ).show()

                endTime.isEmpty() -> Toast.makeText(
                    this,
                    "Please enter end time",
                    Toast.LENGTH_SHORT
                ).show()

                else -> {
                    Toast.makeText(this, "GOING TO STEP 4", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, DoctorStep4Activity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}