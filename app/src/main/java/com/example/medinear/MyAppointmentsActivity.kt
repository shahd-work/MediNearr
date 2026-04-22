package com.example.medinear

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyAppointmentsActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var appointmentsList: LinearLayout
    private lateinit var tvMonthTitle: TextView
    private lateinit var tvEmpty: LinearLayout

    private var currentMonth = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_appointments)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        appointmentsList = findViewById(R.id.appointmentsList)
        tvMonthTitle = findViewById(R.id.tvMonthTitle)
        tvEmpty = findViewById(R.id.emptyState)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnPrevMonth).setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            loadAppointments()
        }
        findViewById<Button>(R.id.btnNextMonth).setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            loadAppointments()
        }

        loadAppointments()
    }

    private fun loadAppointments() {
        val uid = auth.currentUser?.uid ?: return

        val fmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthTitle.text = fmt.format(currentMonth.time)

        val startCal = currentMonth.clone() as Calendar
        startCal.set(Calendar.DAY_OF_MONTH, 1)
        val endCal = currentMonth.clone() as Calendar
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))

        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFmt.format(startCal.time)
        val endDate = dateFmt.format(endCal.time)

        db.collection("appointments")
            .whereEqualTo("patientUid", uid)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                appointmentsList.removeAllViews()

                if (result.isEmpty) {
                    tvEmpty.visibility = View.VISIBLE
                    appointmentsList.visibility = View.GONE
                    return@addOnSuccessListener
                }

                tvEmpty.visibility = View.GONE
                appointmentsList.visibility = View.VISIBLE

                for (doc in result) {
                    val date = doc.getString("date") ?: ""
                    val timeSlot = doc.getString("timeSlot") ?: ""
                    val doctorName = doc.getString("doctorName") ?: ""
                    val doctorSpec = doc.getString("doctorSpec") ?: ""
                    val status = doc.getString("status") ?: "pending"
                    val doctorClinic = doc.getString("doctorClinic") ?: ""
                    val appointmentId = doc.getString("appointmentId") ?: doc.id

                    addAppointmentCard(date, timeSlot, doctorName, doctorSpec, status, doctorClinic, appointmentId)
                }
            }
            .addOnFailureListener {
                appointmentsList.removeAllViews()
                tvEmpty.visibility = View.VISIBLE
                appointmentsList.visibility = View.GONE
            }
    }

    private fun addAppointmentCard(
        date: String, timeSlot: String, doctorName: String,
        doctorSpec: String, status: String, clinic: String, appointmentId: String
    ) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(20, 18, 20, 18)
        card.background = getDrawable(R.drawable.bg_doc_card)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.bottomMargin = 12
        card.layoutParams = lp

        // Top row: status badge + date
        val topRow = LinearLayout(this)
        topRow.orientation = LinearLayout.HORIZONTAL
        topRow.gravity = Gravity.CENTER_VERTICAL
        val topLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        topLp.bottomMargin = 10
        topRow.layoutParams = topLp

        val statusBadge = TextView(this)
        statusBadge.textSize = 11f
        statusBadge.setPadding(20, 6, 20, 6)
        when (status) {
            "pending" -> {
                statusBadge.text = "⏳ Pending"
                statusBadge.setTextColor(0xFFE6A817.toInt())
                statusBadge.setBackgroundColor(0x1AE6A817.toInt())
            }
            "confirmed" -> {
                statusBadge.text = "✓ Confirmed"
                statusBadge.setTextColor(0xFF2E7D32.toInt())
                statusBadge.setBackgroundColor(0x1A2E7D32.toInt())
            }
            "cancelled" -> {
                statusBadge.text = "✗ Cancelled"
                statusBadge.setTextColor(0xFFD32F2F.toInt())
                statusBadge.setBackgroundColor(0x1AD32F2F.toInt())
            }
            "completed" -> {
                statusBadge.text = "✓ Completed"
                statusBadge.setTextColor(0xFF4A8BA8.toInt())
                statusBadge.setBackgroundColor(0x1A4A8BA8.toInt())
            }
            else -> {
                statusBadge.text = status.replaceFirstChar { it.uppercase() }
                statusBadge.setTextColor(0xFF888888.toInt())
            }
        }
        topRow.addView(statusBadge)

        val spacer = View(this)
        spacer.layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        topRow.addView(spacer)

        val tvDateTime = TextView(this)
        try {
            val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            val parsedDate = inputFmt.parse(date)
            tvDateTime.text = "${outputFmt.format(parsedDate!!)} • $timeSlot"
        } catch (e: Exception) {
            tvDateTime.text = "$date • $timeSlot"
        }
        tvDateTime.textSize = 12f
        tvDateTime.setTextColor(0xFF666666.toInt())
        topRow.addView(tvDateTime)

        card.addView(topRow)

        // Doctor info row
        val docRow = LinearLayout(this)
        docRow.orientation = LinearLayout.HORIZONTAL
        docRow.gravity = Gravity.CENTER_VERTICAL

        val initials = doctorName.replace("Dr. ", "").split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")

        val avatar = TextView(this)
        avatar.text = initials
        avatar.textSize = 14f
        avatar.setTextColor(0xFF74B3CE.toInt())
        avatar.gravity = Gravity.CENTER
        avatar.background = getDrawable(R.drawable.bg_doc_avatar)
        val avLp = LinearLayout.LayoutParams(44.dp, 44.dp)
        avLp.marginEnd = 12
        avatar.layoutParams = avLp

        val info = LinearLayout(this)
        info.orientation = LinearLayout.VERTICAL

        val tvName = TextView(this)
        tvName.text = doctorName
        tvName.textSize = 14f
        tvName.setTextColor(0xFF222222.toInt())

        val tvSpec = TextView(this)
        tvSpec.text = doctorSpec
        tvSpec.textSize = 12f
        tvSpec.setTextColor(0xFF888888.toInt())

        info.addView(tvName)
        info.addView(tvSpec)

        if (clinic.isNotEmpty()) {
            val tvClinic = TextView(this)
            tvClinic.text = "🏥 $clinic"
            tvClinic.textSize = 11f
            tvClinic.setTextColor(0xFFAAAAAA.toInt())
            val cLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cLp.topMargin = 2
            tvClinic.layoutParams = cLp
            info.addView(tvClinic)
        }

        docRow.addView(avatar)
        docRow.addView(info)
        card.addView(docRow)

        // Cancel button for pending
        if (status == "pending") {
            val btnCancel = TextView(this)
            btnCancel.text = "Cancel Appointment"
            btnCancel.textSize = 12f
            btnCancel.setTextColor(0xFFD32F2F.toInt())
            btnCancel.gravity = Gravity.END
            val cancelLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cancelLp.topMargin = 12
            btnCancel.layoutParams = cancelLp
            btnCancel.setOnClickListener {
                db.collection("appointments").document(appointmentId)
                    .update("status", "cancelled")
                    .addOnSuccessListener {
                        loadAppointments()
                    }
            }
            card.addView(btnCancel)
        }

        appointmentsList.addView(card)
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
