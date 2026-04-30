package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DoctorHomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // ✅ Set today's real date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd · yyyy", Locale.ENGLISH)
        findViewById<TextView>(R.id.tv_today_date).text = dateFormat.format(Date())

        // ✅ Load doctor's real name from Firebase
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val firstName = doc.getString("firstName") ?: ""
                    val lastName  = doc.getString("lastName")  ?: ""
                    findViewById<TextView>(R.id.tv_doctor_name).text = "Dr. $firstName $lastName"
                }
        }

        // Navigation
        findViewById<TextView>(R.id.tv_see_all).setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }
        findViewById<ImageView>(R.id.iv_profile).setOnClickListener {
            startActivity(Intent(this, DoctorProfileActivity::class.java))
        }
        findViewById<ImageView>(R.id.iv_notification).setOnClickListener {
            startActivity(Intent(this, AvailabilityActivity::class.java))
        }

        // ✅ Load real appointments
        loadDashboardAppointments()
    }

    private fun loadDashboardAppointments() {
        db.collection("apointments").get()
            .addOnSuccessListener { result ->
                var total   = 0
                var pending = 0
                var newP    = 0

                // ✅ Get today's date string to filter today's appointments
                val todayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                val today = todayFormat.format(Date())

                for (doc in result) {
                    val status = doc.getString("Status") ?: ""
                    val date   = doc.getString("Date")   ?: ""

                    // Only count today's appointments
                    if (date == today) {
                        total++
                        if (status == "Pending")   pending++
                        if (status == "New")       newP++
                    }
                }

                findViewById<TextView>(R.id.tv_patient_count).text = total.toString()
                findViewById<TextView>(R.id.tv_pending_count).text = pending.toString()
                findViewById<TextView>(R.id.tv_new_count).text     = newP.toString()

                // ✅ Load first 3 real appointments into the cards
                val docs = result.documents.take(3)
                val itemIds = listOf(
                    R.id.appointment_item_1,
                    R.id.appointment_item_2,
                    R.id.appointment_item_3
                )

                for (i in docs.indices) {
                    val doc  = docs[i]
                    val name   = doc.getString("Name")   ?: "Unknown"
                    val reason = doc.getString("Reason") ?: ""
                    val time   = doc.getString("Time")   ?: ""
                    val status = doc.getString("Status") ?: ""

                    val item = findViewById<LinearLayout>(itemIds[i])

                    // Set name
                    item.findViewById<TextView>(
                        resources.getIdentifier("tv_name_$i", "id", packageName)
                    )

                    // Find TextViews inside each appointment card
                    val textViews = ArrayList<TextView>()
                    for (j in 0 until item.childCount) {
                        val child = item.getChildAt(j)
                        if (child is LinearLayout) {
                            for (k in 0 until child.childCount) {
                                val v = child.getChildAt(k)
                                if (v is TextView) textViews.add(v)
                            }
                        } else if (child is TextView) {
                            textViews.add(child)
                        }
                    }

                    // textViews: [0]=avatar letter, [1]=name, [2]=reason·time, [3]=status tag
                    if (textViews.size >= 4) {
                        textViews[0].text = name.firstOrNull()?.toString() ?: "?"
                        textViews[1].text = name
                        textViews[2].text = "$reason · $time"
                        textViews[3].text = status
                    }
                }
            }
    }
}