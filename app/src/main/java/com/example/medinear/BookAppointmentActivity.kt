package com.example.medinear

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookAppointmentActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedYear  = 0
    private var selectedMonth = 0
    private var selectedDay   = 0
    private var selectedSlot  = ""
    private var viewYear      = 0
    private var viewMonth     = 0

    private lateinit var tvMonth: TextView
    private lateinit var calendarGrid: GridLayout
    private lateinit var chipGroupSlots: ChipGroup
    private lateinit var tvSlotsTitle: TextView
    private lateinit var btnConfirm: Button

    private var doctorId   = ""
    private var doctorName = ""
    private var doctorSpec = ""
    private var doctorFee  = ""

    private val allSlots = listOf(
        "09:00","09:30","10:00","10:30","11:00","11:30",
        "14:00","14:30","15:00","15:30","16:00","16:30"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        doctorId   = intent.getStringExtra("doctorUid")   ?: ""
        doctorName = intent.getStringExtra("doctorName") ?: ""
        doctorSpec = intent.getStringExtra("doctorSpec") ?: ""
        doctorFee  = intent.getStringExtra("doctorFee")  ?: ""

        tvMonth        = findViewById(R.id.tvMonth)
        calendarGrid   = findViewById(R.id.calendarGrid)
        chipGroupSlots = findViewById(R.id.chipGroupSlots)
        tvSlotsTitle   = findViewById(R.id.tvSlotsTitle)
        btnConfirm     = findViewById(R.id.btnConfirm)

        val cal = Calendar.getInstance()
        viewYear  = cal.get(Calendar.YEAR)
        viewMonth = cal.get(Calendar.MONTH)

        setupDoctorCard()
        setupDayNames()
        renderCalendar()
        renderSlots()

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnPrevMonth).setOnClickListener {
            viewMonth--
            if (viewMonth < 0) { viewMonth = 11; viewYear-- }
            renderCalendar()
        }
        findViewById<Button>(R.id.btnNextMonth).setOnClickListener {
            viewMonth++
            if (viewMonth > 11) { viewMonth = 0; viewYear++ }
            renderCalendar()
        }

        btnConfirm.setOnClickListener { confirmBooking() }
    }

    private fun setupDoctorCard() {
        val initials = doctorName.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
        findViewById<TextView>(R.id.tvDocInitials).text = initials
        findViewById<TextView>(R.id.tvDocName).text     = doctorName
        findViewById<TextView>(R.id.tvDocSpec).text     = doctorSpec
        findViewById<TextView>(R.id.tvDocFee).text      = if (doctorFee.isNotEmpty()) "$doctorFee TND / consultation" else ""
    }

    private fun setupDayNames() {
        val grid = findViewById<GridLayout>(R.id.dayNamesGrid)
        val days = listOf("Su","Mo","Tu","We","Th","Fr","Sa")
        grid.removeAllViews()
        days.forEach { day ->
            val tv = TextView(this)
            tv.text = day
            tv.textSize = 11f
            tv.setTextColor(Color.parseColor("#AAAAAA"))
            tv.gravity = Gravity.CENTER
            val lp = GridLayout.LayoutParams()
            lp.width = 0
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            lp.height = GridLayout.LayoutParams.WRAP_CONTENT
            tv.layoutParams = lp
            grid.addView(tv)
        }
    }

    private fun renderCalendar() {
        val monthNames = listOf("January","February","March","April","May","June",
            "July","August","September","October","November","December")
        tvMonth.text = "${monthNames[viewMonth]} $viewYear"
        calendarGrid.removeAllViews()

        val cal = Calendar.getInstance()
        cal.set(viewYear, viewMonth, 1)
        val firstDay   = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()

        for (i in 0 until firstDay) {
            val empty = TextView(this)
            empty.text = ""
            val lp = GridLayout.LayoutParams()
            lp.width = 0
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            lp.height = 44
            empty.layoutParams = lp
            calendarGrid.addView(empty)
        }

        for (d in 1..daysInMonth) {
            val tv = TextView(this)
            tv.text = d.toString()
            tv.textSize = 13f
            tv.gravity = Gravity.CENTER
            val lp = GridLayout.LayoutParams()
            lp.width = 0
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            lp.height = 44
            lp.setMargins(2,2,2,2)
            tv.layoutParams = lp

            val isPast = viewYear < today.get(Calendar.YEAR) ||
                    (viewYear == today.get(Calendar.YEAR) && viewMonth < today.get(Calendar.MONTH)) ||
                    (viewYear == today.get(Calendar.YEAR) && viewMonth == today.get(Calendar.MONTH) && d < today.get(Calendar.DAY_OF_MONTH))

            val isSelected = selectedDay == d && selectedMonth == viewMonth && selectedYear == viewYear

            when {
                isSelected -> {
                    tv.setBackgroundResource(R.drawable.bg_spec_chip_active)
                    tv.setTextColor(Color.WHITE)
                    tv.setTypeface(null, Typeface.BOLD)
                }
                isPast -> {
                    tv.setTextColor(Color.parseColor("#CCCCCC"))
                }
                else -> {
                    tv.setTextColor(Color.parseColor("#333333"))
                    tv.setOnClickListener {
                        selectedDay   = d
                        selectedMonth = viewMonth
                        selectedYear  = viewYear
                        selectedSlot  = ""
                        renderCalendar()
                        loadTakenSlots()
                        updateButton()
                    }
                }
            }
            calendarGrid.addView(tv)
        }
    }

    private fun loadTakenSlots() {
        if (selectedDay == 0) return
        val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
        tvSlotsTitle.text = "Time slots for $dateStr"

        db.collection("apointments")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", dateStr)
            .whereIn("status", listOf("pending", "confirmed"))
            .get()
            .addOnSuccessListener { result ->
                val taken = result.documents.mapNotNull { it.getString("timeSlot") }
                renderSlots(taken)
            }
            .addOnFailureListener {
                renderSlots(emptyList())
            }
    }

    private fun renderSlots(takenSlots: List<String> = emptyList()) {
        chipGroupSlots.removeAllViews()

        if (selectedDay == 0) {
            val tv = TextView(this)
            tv.text = "Please select a date first"
            tv.textSize = 13f
            tv.setTextColor(Color.parseColor("#AAAAAA"))
            chipGroupSlots.addView(tv)
            return
        }

        allSlots.forEach { slot ->
            val chip = Chip(this)
            chip.text = slot
            chip.isCheckable = true
            val isTaken = takenSlots.contains(slot)

            if (isTaken) {
                chip.isEnabled = false
                chip.setChipBackgroundColorResource(android.R.color.transparent)
                chip.setTextColor(Color.parseColor("#CCCCCC"))
            } else {
                chip.setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        selectedSlot = slot
                        updateButton()
                    }
                }
            }
            chipGroupSlots.addView(chip)
        }
    }

    private fun updateButton() {
        if (selectedDay > 0 && selectedSlot.isNotEmpty()) {
            btnConfirm.isEnabled = true
            btnConfirm.text = "Confirm appointment"
        } else {
            btnConfirm.isEnabled = false
            btnConfirm.text = "Select a date and time slot"
        }
    }

    private fun confirmBooking() {
        val uid = auth.currentUser?.uid ?: return
        val note = findViewById<EditText>(R.id.etNote).text.toString().trim()
        val dateStr = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

        val appointment = hashMapOf(
            "patientUid"  to uid,
            "doctorUid"   to doctorId,
            "doctorName" to doctorName,
            "date"       to dateStr,
            "timeSlot"   to selectedSlot,
            "note"       to note,
            "status"     to "pending",
            "createdAt"  to System.currentTimeMillis()
        )

        db.collection("apointments").add(appointment)
            .addOnSuccessListener {
                val intent = Intent(this, AppointmentSuccessActivity::class.java)
                intent.putExtra("doctorName", doctorName)
                intent.putExtra("date",       dateStr)
                intent.putExtra("timeSlot",   selectedSlot)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}