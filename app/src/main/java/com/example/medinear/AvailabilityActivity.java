package com.example.medinear;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilityActivity extends AppCompatActivity {

    TextView dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;
    TextView slot15, slot30, slot60, btnGenerate, btnSave, tvBack;
    TimePicker tpStart, tpEnd;
    RecyclerView rvSlots;
    FirebaseFirestore db;
    FirebaseAuth auth;

    int selectedSlotDuration = 30;
    List<String> generatedSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvBack = findViewById(R.id.tv_back);
        tvBack.setOnClickListener(v -> finish());

        dayMon = findViewById(R.id.day_mon);
        dayTue = findViewById(R.id.day_tue);
        dayWed = findViewById(R.id.day_wed);
        dayThu = findViewById(R.id.day_thu);
        dayFri = findViewById(R.id.day_fri);
        daySat = findViewById(R.id.day_sat);
        daySun = findViewById(R.id.day_sun);

        setupDayToggle(dayMon, true);
        setupDayToggle(dayTue, true);
        setupDayToggle(dayWed, true);
        setupDayToggle(dayThu, true);
        setupDayToggle(dayFri, true);
        setupDayToggle(daySat, false);
        setupDayToggle(daySun, false);

        tpStart = findViewById(R.id.tp_start);
        tpEnd = findViewById(R.id.tp_end);
        tpStart.setHour(9);
        tpStart.setMinute(0);
        tpEnd.setHour(17);
        tpEnd.setMinute(0);

        slot15 = findViewById(R.id.slot_15);
        slot30 = findViewById(R.id.slot_30);
        slot60 = findViewById(R.id.slot_60);

        slot15.setOnClickListener(v -> selectSlot(15));
        slot30.setOnClickListener(v -> selectSlot(30));
        slot60.setOnClickListener(v -> selectSlot(60));

        rvSlots = findViewById(R.id.rv_slots);
        rvSlots.setLayoutManager(new GridLayoutManager(this, 3));

        btnGenerate = findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(v -> generateSlots());

        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveAvailability());

        loadAvailability();
    }

    private void loadAvailability() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String startTime = doc.getString("startTime");
                        String endTime = doc.getString("endTime");
                        String slotDur = doc.getString("slotDuration");

                        if (startTime != null && startTime.contains(":")) {
                            String[] parts = startTime.split(":");
                            tpStart.setHour(Integer.parseInt(parts[0]));
                            tpStart.setMinute(Integer.parseInt(parts[1]));
                        }
                        if (endTime != null && endTime.contains(":")) {
                            String[] parts = endTime.split(":");
                            tpEnd.setHour(Integer.parseInt(parts[0]));
                            tpEnd.setMinute(Integer.parseInt(parts[1]));
                        }
                        if (slotDur != null) {
                            if (slotDur.contains("15")) selectSlot(15);
                            else if (slotDur.contains("60")) selectSlot(60);
                            else selectSlot(30);
                        }
                    }
                });
    }

    private void saveAvailability() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        String startTime = String.format("%02d:%02d",
                tpStart.getHour(), tpStart.getMinute());
        String endTime = String.format("%02d:%02d",
                tpEnd.getHour(), tpEnd.getMinute());
        String slotDur = selectedSlotDuration + " min";

        List<String> selectedDays = new ArrayList<>();
        if ((boolean) dayMon.getTag()) selectedDays.add("Monday");
        if ((boolean) dayTue.getTag()) selectedDays.add("Tuesday");
        if ((boolean) dayWed.getTag()) selectedDays.add("Wednesday");
        if ((boolean) dayThu.getTag()) selectedDays.add("Thursday");
        if ((boolean) dayFri.getTag()) selectedDays.add("Friday");
        if ((boolean) daySat.getTag()) selectedDays.add("Saturday");
        if ((boolean) daySun.getTag()) selectedDays.add("Sunday");

        Map<String, Object> updates = new HashMap<>();
        updates.put("startTime", startTime);
        updates.put("endTime", endTime);
        updates.put("slotDuration", slotDur);
        updates.put("workingDays", selectedDays);
        updates.put("slots", generatedSlots);

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                "Availability saved! ✅",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void setupDayToggle(TextView day, boolean selected) {
        day.setTag(selected);
        updateDayStyle(day, selected);
        day.setOnClickListener(v -> {
            boolean isSelected = (boolean) day.getTag();
            day.setTag(!isSelected);
            updateDayStyle(day, !isSelected);
        });
    }

    private void updateDayStyle(TextView day, boolean selected) {
        if (selected) {
            day.setBackgroundResource(R.drawable.bg_day_selected);
            day.setTextColor(0xFFFFFFFF);
        } else {
            day.setBackgroundResource(R.drawable.bg_tag_unselected);
            day.setTextColor(0xFF94A3B8);
        }
    }

    private void selectSlot(int minutes) {
        selectedSlotDuration = minutes;
        slot15.setBackgroundResource(R.drawable.bg_tag_unselected);
        slot15.setTextColor(0xFF94A3B8);
        slot30.setBackgroundResource(R.drawable.bg_tag_unselected);
        slot30.setTextColor(0xFF94A3B8);
        slot60.setBackgroundResource(R.drawable.bg_tag_unselected);
        slot60.setTextColor(0xFF94A3B8);

        TextView selected = minutes == 15 ? slot15 :
                minutes == 30 ? slot30 : slot60;
        selected.setBackgroundResource(R.drawable.bg_day_selected);
        selected.setTextColor(0xFFFFFFFF);
    }

    private void generateSlots() {
        generatedSlots.clear();

        int current = tpStart.getHour() * 60 + tpStart.getMinute();
        int end = tpEnd.getHour() * 60 + tpEnd.getMinute();

        while (current + selectedSlotDuration <= end) {
            int h = current / 60;
        }
    }
}