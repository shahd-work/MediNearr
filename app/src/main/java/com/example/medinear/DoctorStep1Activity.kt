package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity

class DoctorStep1Activity :  ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_step1)

        val genders = listOf("Select gender", "Male", "Female", "Other")
        findViewById<Spinner>(R.id.spinnerGender).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            val firstName = findViewById<EditText>(R.id.etFirstName).text.toString().trim()
            val lastName  = findViewById<EditText>(R.id.etLastName).text.toString().trim()
            val gender    = findViewById<Spinner>(R.id.spinnerGender).selectedItem.toString()
            val phone     = findViewById<EditText>(R.id.etPhone).text.toString().trim()
            val email     = findViewById<EditText>(R.id.etEmail).text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DoctorStep2Activity::class.java)
            intent.putExtra("firstName", firstName)
            intent.putExtra("lastName", lastName)
            intent.putExtra("gender", gender)
            intent.putExtra("phone", phone)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }
}