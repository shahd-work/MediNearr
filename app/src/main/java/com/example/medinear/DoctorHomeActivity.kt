package com.example.medinear

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DoctorHomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var calendarOffset = 0 // weeks offset from today

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        db   = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // ✅ Set today's real date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd · yyyy", Locale.ENGLISH)
        findViewById<TextView>(R.id.tv_today_date).text = dateFormat.format(Date())

        // ✅ Load doctor's real name
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val firstName = doc.getString("firstName") ?: ""
                    val lastName  = doc.getString("lastName")  ?: ""
                    findViewById<TextView>(R.id.tv_doctor_name).text = "Dr. $firstName $lastName"
                }
        }

        // ✅ Build calendar
        renderCalendar()

        // Calendar navigation
        findViewById<TextView>(R.id.tv_prev_week).setOnClickListener {
            calendarOffset--
            renderCalendar()
        }
        findViewById<TextView>(R.id.tv_next_week).setOnClickListener {
            calendarOffset++
            renderCalendar()
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

        loadDashboardAppointments()
    }

    private fun renderCalendar() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, calendarOffset)

        // Go to start of week (Monday)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val monthNames = listOf("January","February","March","April","May","June",
            "July","August","September","October","November","December")
        val monthTitle = "📅  ${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
        findViewById<TextView>(R.id.tv_cal_month).text = monthTitle

        val today = Calendar.getInstance()
        val dayNames = listOf("M","T","W","T","F","S","S")
        val calendarRow = findViewById<LinearLayout>(R.id.calendarRow)
        calendarRow.removeAllViews()

        for (i in 0..6) {
            val dayNum  = cal.get(Calendar.DAY_OF_MONTH)
            val isToday = cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
                    cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR)  == today.get(Calendar.YEAR)

            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            container.gravity = Gravity.CENTER
            container.setPadding(6, 6, 6, 6)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            container.layoutParams = lp

            if (isToday) {
                container.setBackgroundResource(R.drawable.bg_day_selected)
            }

            val tvDayName = TextView(this)
            tvDayName.text = dayNames[i]
            tvDayName.textSize = 11f
            tvDayName.gravity = Gravity.CENTER
            tvDayName.setTextColor(if (isToday) Color.parseColor("#DBEAFE") else Color.parseColor("#94A3B8"))

            val tvDayNum = TextView(this)
            tvDayNum.text = dayNum.toString()
            tvDayNum.textSize = 14f
            tvDayNum.gravity = Gravity.CENTER
            tvDayNum.setTextColor(if (isToday) Color.WHITE else Color.parseColor("#64748B"))

            container.addView(tvDayName)
            container.addView(tvDayNum)
            calendarRow.addView(container)

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun loadDashboardAppointments() {
        val uid = auth.currentUser?.uid ?: return

        val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val today = todayFormat.format(Date())

        db.collection("apointments")
            .whereEqualTo("doctorUid", uid)
            .get()
            .addOnSuccessListener { result ->
                var total     = 0
                var pending   = 0
                var confirmed = 0
                var firstPendingId   = ""
                var firstPendingName = ""

                val todayDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()

                for (doc in result) {
                    val status = doc.getString("status") ?: ""
                    val date   = doc.getString("date")   ?: ""

                    if (date == today) {
                        total++
                        todayDocs.add(doc)
                        if (status == "pending") {
                            pending++
                            if (firstPendingId.isEmpty()) {
                                firstPendingId   = doc.id
                                firstPendingName = doc.getString("patientName") ?: "A patient"
                            }
                        }
                        if (status == "confirmed") confirmed++
                    }
                }

                // ✅ Update counts
                findViewById<TextView>(R.id.tv_patient_count).text = total.toString()
                findViewById<TextView>(R.id.tv_pending_count).text = pending.toString()
                findViewById<TextView>(R.id.tv_new_count).text     = confirmed.toString()

                // ✅ Show new patient alert if there's a pending appointment
                val layoutAlert = findViewById<LinearLayout>(R.id.layoutNewRequest)
                if (pending > 0) {
                    layoutAlert.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.tv_new_request_name).text =
                        "$firstPendingName is waiting for approval"
                    // ✅ View button opens AppointmentsActivity
                    findViewById<TextView>(R.id.tv_view_request).setOnClickListener {
                        startActivity(Intent(this, AppointmentsActivity::class.java))
                    }
                } else {
                    layoutAlert.visibility = View.GONE
                }

                // ✅ Load today's appointments dynamically
                val appointmentsList = findViewById<LinearLayout>(R.id.appointmentsList)
                appointmentsList.removeAllViews()

                if (todayDocs.isEmpty()) {
                    val tv = TextView(this)
                    tv.text = "No appointments today"
                    tv.textSize = 13f
                    tv.setTextColor(Color.parseColor("#AAAAAA"))
                    tv.gravity = Gravity.CENTER
                    tv.setPadding(0, 32, 0, 32)
                    appointmentsList.addView(tv)
                    return@addOnSuccessListener
                }

                for (doc in todayDocs.take(3)) {
                    val patientName = doc.getString("patientName") ?: "Unknown"
                    val note        = doc.getString("note")        ?: ""
                    val timeSlot    = doc.getString("timeSlot")    ?: ""
                    val status      = doc.getString("status")      ?: ""
                    val initial     = patientName.firstOrNull()?.toString() ?: "?"

                    // Card
                    val card = LinearLayout(this)
                    card.orientation = LinearLayout.HORIZONTAL
                    card.gravity = Gravity.CENTER_VERTICAL
                    card.setBackgroundResource(R.drawable.bg_white_card)
                    card.setPadding(14, 14, 14, 14)
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                    lp.bottomMargin = 10
                    card.layoutParams = lp
                    card.isClickable = true
                    card.setOnClickListener {
                        startActivity(Intent(this, AppointmentsActivity::class.java))
                    }

                    // Avatar
                    val avatar = TextView(this)
                    avatar.text = initial
                    avatar.textSize = 20f
                    avatar.setTextColor(Color.WHITE)
                    avatar.gravity = Gravity.CENTER
                    avatar.setBackgroundResource(R.drawable.bg_avatar_circle)
                    val avLp = LinearLayout.LayoutParams(48.dp, 48.dp)
                    avLp.marginEnd = 12
                    avatar.layoutParams = avLp

                    // Info
                    val info = LinearLayout(this)
                    info.orientation = LinearLayout.VERTICAL
                    info.layoutParams = LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                    val tvName = TextView(this)
                    tvName.text = patientName
                    tvName.textSize = 14f
                    tvName.setTextColor(Color.parseColor("#1E293B"))

                    val tvDetail = TextView(this)
                    tvDetail.text = if (note.isNotEmpty()) "$note · $timeSlot" else timeSlot
                    tvDetail.textSize = 12f
                    tvDetail.setTextColor(Color.parseColor("#64748B"))

                    info.addView(tvName)
                    info.addView(tvDetail)

                    // Status badge
                    val badge = TextView(this)
                    badge.text = status.replaceFirstChar { it.uppercase() }
                    badge.textSize = 11f
                    when (status) {
                        "confirmed" -> {
                            badge.setTextColor(Color.parseColor("#16A34A"))
                            badge.setBackgroundResource(R.drawable.bg_tag_green)
                        }
                        "cancelled" -> {
                            badge.setTextColor(Color.parseColor("#DC2626"))
                            badge.setBackgroundResource(R.drawable.bg_tag_red)
                        }
                        else -> {
                            badge.setTextColor(Color.parseColor("#EA580C"))
                            badge.setBackgroundResource(R.drawable.bg_tag_orange)
                        }
                    }
                    badge.setPadding(8, 3, 8, 3)

                    card.addView(avatar)
                    card.addView(info)
                    card.addView(badge)
                    appointmentsList.addView(card)
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("DASHBOARD", "Error: ${e.message}")
            }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}