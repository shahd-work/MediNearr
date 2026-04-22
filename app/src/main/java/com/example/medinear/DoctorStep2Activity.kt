package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity

class DoctorStep2Activity :ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_step2)

        val specializations = listOf(
            "Select specialization", "General Practitioner", "Cardiologist",
            "Dermatologist", "Pediatrician", "Neurologist",
            "Orthopedist", "Gynecologist", "Ophthalmologist", "Psychiatrist", "Other"
        )
        findViewById<Spinner>(R.id.spinnerSpec).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, specializations)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            val license    = findViewById<EditText>(R.id.etLicense).text.toString().trim()
            val experience = findViewById<EditText>(R.id.etExperience).text.toString().trim()
            val fee        = findViewById<EditText>(R.id.etFee).text.toString().trim()
            val spec       = findViewById<Spinner>(R.id.spinnerSpec).selectedItem.toString()

            if (license.isEmpty() || experience.isEmpty() || fee.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DoctorStep3Activity::class.java)
            intent.putExtra("firstName",      getIntent().getStringExtra("firstName"))
            intent.putExtra("lastName",       getIntent().getStringExtra("lastName"))
            intent.putExtra("gender",         getIntent().getStringExtra("gender"))
            intent.putExtra("phone",          getIntent().getStringExtra("phone"))
            intent.putExtra("email",          getIntent().getStringExtra("email"))
            intent.putExtra("specialization", spec)
            intent.putExtra("license",        license)
            intent.putExtra("experience",     experience)
            intent.putExtra("fee",            fee)
            startActivity(intent)
        }
    }
}