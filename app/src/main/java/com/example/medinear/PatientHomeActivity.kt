package com.example.medinear

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Doctor(
    val uid: String,
    val name: String,
    val specialization: String,
    val city: String,
    val rating: String,
    val initials: String,
    val fee: String,
    val clinic: String,
    val startTime: String,
    val endTime: String,
    val slotDuration: String
)

class PatientHomeActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val allDoctors = mutableListOf<Doctor>()
    private var activeSpec = "All"
    private lateinit var emptyState: LinearLayout
    private lateinit var resultsState: LinearLayout
    private lateinit var docList: LinearLayout
    private lateinit var tvResultsTitle: TextView
    private lateinit var chipRow: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        emptyState     = findViewById(R.id.emptyState)
        resultsState   = findViewById(R.id.resultsState)
        docList        = findViewById(R.id.docList)
        tvResultsTitle = findViewById(R.id.tvResultsTitle)
        chipRow        = findViewById(R.id.chipRow)

        loadPatientName()
        loadDoctors()
        setupSearch()
        setupChips()
        setupQuickGrid()
        setupBottomNav()

        findViewById<TextView>(R.id.tvAvatar).setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadDoctors()
    }

    private fun loadPatientName() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val firstName = doc.getString("firstName") ?: "User"
                val lastName  = doc.getString("lastName")  ?: ""
                val initials  = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
                findViewById<TextView>(R.id.tvGreetName).text = "$firstName 👋"
                findViewById<TextView>(R.id.tvAvatar).text    = initials
            }
    }

    private fun loadDoctors() {
        db.collection("users")
            .whereEqualTo("role", "doctor")
            .get()
            .addOnSuccessListener { result ->
                allDoctors.clear()
                for (doc in result) {
                    val firstName = doc.getString("firstName") ?: ""
                    val lastName  = doc.getString("lastName")  ?: ""
                    val spec      = doc.getString("specialization") ?: ""
                    val city      = doc.getString("city") ?: ""
                    val fee       = doc.getString("fee") ?: ""
                    val clinic    = doc.getString("clinic") ?: ""
                    val startTime = doc.getString("startTime") ?: ""
                    val endTime   = doc.getString("endTime") ?: ""
                    val slotDur   = doc.getString("slotDuration") ?: "30 min"
                    val initials  = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
                    allDoctors.add(Doctor(
                        uid            = doc.id,
                        name           = "Dr. $firstName $lastName",
                        specialization = spec,
                        city           = city,
                        rating         = "5.0",
                        initials       = initials,
                        fee            = fee,
                        clinic         = clinic,
                        startTime      = startTime,
                        endTime        = endTime,
                        slotDuration   = slotDur
                    ))
                }
            }
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                if (q.isEmpty()) {
                    if (activeSpec == "All") showEmpty()
                    else filterBySpec(activeSpec)
                } else {
                    searchDoctors(q)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupChips() {
        val specs = listOf("All","General Practitioner","Cardiologist","Dermatologist",
            "Pediatrician","Neurologist","Gynecologist")
        chipRow.removeAllViews()
        for (spec in specs) {
            val chip = TextView(this)
            chip.text = if (spec == "General Practitioner") "General" else spec
            chip.textSize = 12f
            chip.setPadding(32, 18, 32, 18)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.marginEnd = 8
            chip.layoutParams = lp
            chip.background = if (spec == "All")
                getDrawable(R.drawable.bg_spec_chip_active)
            else
                getDrawable(R.drawable.bg_spec_chip)
            chip.setTextColor(
                if (spec == "All") 0xFFFFFFFF.toInt() else 0xFF555555.toInt()
            )
            chip.setOnClickListener {
                activeSpec = spec
                updateChips(chip)
                if (spec == "All") showEmpty()
                else filterBySpec(spec)
            }
            chipRow.addView(chip)
        }
    }

    private fun updateChips(selected: TextView) {
        for (i in 0 until chipRow.childCount) {
            val chip = chipRow.getChildAt(i) as TextView
            val isSelected = chip == selected
            chip.background = if (isSelected)
                getDrawable(R.drawable.bg_spec_chip_active)
            else
                getDrawable(R.drawable.bg_spec_chip)
            chip.setTextColor(
                if (isSelected) 0xFFFFFFFF.toInt() else 0xFF555555.toInt()
            )
        }
    }

    private fun setupQuickGrid() {
        val grid = findViewById<GridLayout>(R.id.quickGrid)
        val categories = listOf(
            Pair("🏥", "General Practitioner"),
            Pair("❤️", "Cardiologist"),
            Pair("✨", "Dermatologist"),
            Pair("👶", "Pediatrician"),
            Pair("🧠", "Neurologist"),
            Pair("🌸", "Gynecologist")
        )
        grid.removeAllViews()
        for (cat in categories) {
            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.gravity = Gravity.CENTER
            card.setPadding(16, 20, 16, 20)
            card.background = getDrawable(R.drawable.bg_quick_card)
            val lp = GridLayout.LayoutParams()
            lp.width = 0
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            lp.setMargins(8, 8, 8, 8)
            card.layoutParams = lp

            val icon = TextView(this)
            icon.text = cat.first
            icon.textSize = 24f
            icon.gravity = Gravity.CENTER

            val label = TextView(this)
            label.text = when (cat.second) {
                "General Practitioner" -> "General"
                else -> cat.second
            }
            label.textSize = 12f
            label.setTextColor(0xFF333333.toInt())
            label.gravity = Gravity.CENTER
            label.setPadding(0, 6, 0, 0)

            card.addView(icon)
            card.addView(label)
            card.setOnClickListener { filterBySpec(cat.second) }
            grid.addView(card)
        }
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { }
        findViewById<LinearLayout>(R.id.navAppointments).setOnClickListener {
            startActivity(Intent(this, MyAppointmentsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
        }
    }

    private fun searchDoctors(query: String) {
        val q = query.lowercase()
        var list = allDoctors.filter {
            it.name.lowercase().contains(q) ||
                    it.specialization.lowercase().contains(q) ||
                    it.city.lowercase().contains(q)
        }
        if (activeSpec != "All") {
            list = list.filter { it.specialization.equals(activeSpec, ignoreCase = true) }
        }
        showResults(list, "Results for \"$query\"")
    }

    private fun filterBySpec(spec: String) {
        val list = allDoctors.filter { it.specialization.equals(spec, ignoreCase = true) }
        showResults(list, spec)
    }

    private fun showResults(list: List<Doctor>, title: String) {
        emptyState.visibility   = View.GONE
        resultsState.visibility = View.VISIBLE
        tvResultsTitle.text     = "$title (${list.size} found)"
        docList.removeAllViews()

        if (list.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No doctors found"
            tv.textSize = 13f
            tv.setTextColor(0xFFAAAAAA.toInt())
            tv.gravity = Gravity.CENTER
            tv.setPadding(0, 48, 0, 48)
            docList.addView(tv)
            return
        }

        for (doc in list) {
            val card = LinearLayout(this)
            card.orientation = LinearLayout.HORIZONTAL
            card.setPadding(14, 14, 14, 14)
            card.background = getDrawable(R.drawable.bg_doc_card)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = 10
            card.layoutParams = lp

            card.isClickable = true
            card.isFocusable = true
            card.setOnClickListener {
                val intent = Intent(this, BookAppointmentActivity::class.java)
                intent.putExtra("doctorUid", doc.uid)
                intent.putExtra("doctorName", doc.name)
                intent.putExtra("doctorSpec", doc.specialization)
                intent.putExtra("doctorCity", doc.city)
                intent.putExtra("doctorFee", doc.fee)
                intent.putExtra("doctorClinic", doc.clinic)
                intent.putExtra("doctorStartTime", doc.startTime)
                intent.putExtra("doctorEndTime", doc.endTime)
                intent.putExtra("doctorSlotDuration", doc.slotDuration)
                startActivity(intent)
            }

            val avatar = TextView(this)
            avatar.text = doc.initials
            avatar.textSize = 16f
            avatar.setTextColor(0xFF74B3CE.toInt())
            avatar.gravity = Gravity.CENTER
            avatar.background = getDrawable(R.drawable.bg_doc_avatar)
            val avLp = LinearLayout.LayoutParams(52.dp, 52.dp)
            avLp.marginEnd = 12
            avatar.layoutParams = avLp

            val info = LinearLayout(this)
            info.orientation = LinearLayout.VERTICAL
            info.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )

            val name = TextView(this)
            name.text = doc.name
            name.textSize = 14f
            name.setTextColor(0xFF222222.toInt())

            val spec = TextView(this)
            spec.text = doc.specialization
            spec.textSize = 12f
            spec.setTextColor(0xFF888888.toInt())

            val meta = LinearLayout(this)
            meta.orientation = LinearLayout.HORIZONTAL
            val metaLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            metaLp.topMargin = 6
            meta.layoutParams = metaLp

            val rating = TextView(this)
            rating.text = "★ ${doc.rating}"
            rating.textSize = 11f
            rating.setTextColor(0xFFF59E0B.toInt())
            rating.setPadding(0, 0, 10, 0)

            val city = TextView(this)
            city.text = "📍 ${doc.city}"
            city.textSize = 11f
            city.setTextColor(0xFF888888.toInt())
            city.setPadding(0, 0, 10, 0)

            val badge = TextView(this)
            badge.text = "Book →"
            badge.textSize = 11f
            badge.setTextColor(0xFF4A8BA8.toInt())
            badge.background = getDrawable(R.drawable.bg_badge)
            badge.setPadding(16, 4, 16, 4)

            meta.addView(rating)
            meta.addView(city)
            meta.addView(badge)

            info.addView(name)
            info.addView(spec)
            info.addView(meta)

            card.addView(avatar)
            card.addView(info)

            docList.addView(card)
        }
    }

    private fun showEmpty() {
        emptyState.visibility   = View.VISIBLE
        resultsState.visibility = View.GONE
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}