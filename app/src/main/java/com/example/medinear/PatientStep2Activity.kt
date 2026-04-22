package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PatientStep2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_step2)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            val phone = findViewById<EditText>(R.id.etPhone).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val city  = findViewById<EditText>(R.id.etCity).text.toString().trim()

            if (phone.isEmpty() || email.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val step3Intent = Intent(this, PatientStep3Activity::class.java)
            step3Intent.putExtra("firstName", getIntent().getStringExtra("firstName"))
            step3Intent.putExtra("lastName",  getIntent().getStringExtra("lastName"))
            step3Intent.putExtra("dob",       getIntent().getStringExtra("dob"))
            step3Intent.putExtra("gender",    getIntent().getStringExtra("gender"))
            step3Intent.putExtra("phone",     phone)
            step3Intent.putExtra("email",     email)
            step3Intent.putExtra("city",      city)
            startActivity(step3Intent)
        }
    }
}