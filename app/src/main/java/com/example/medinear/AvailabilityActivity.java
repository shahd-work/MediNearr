package com.example.medinear;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityActivity extends AppCompatActivity {

    TextView dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;
    TextView slot15, slot30, slot60, btnGenerate, tvBack;
    TimePicker tpStart, tpEnd;
    RecyclerView rvSlots;

    int selectedSlotDuration = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability);

        // Back button
        tvBack = findViewById(R.id.tv_back);
        tvBack.setOnClickListener(v -> finish());

        // Days
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

        // Time pickers
        tpStart = findViewById(R.id.tp_start);
        tpEnd   = findViewById(R.id.tp_end);
        tpStart.setHour(9);  tpStart.setMinute(0);
        tpEnd.setHour(17);   tpEnd.setMinute(0);

        // Slot duration
        slot15 = findViewById(R.id.slot_15);
        slot30 = findViewById(R.id.slot_30);
        slot60 = findViewById(R.id.slot_60);

        slot15.setOnClickListener(v -> selectSlot(15));
        slot30.setOnClickListener(v -> selectSlot(30));
        slot60.setOnClickListener(v -> selectSlot(60));

        // RecyclerView
        rvSlots = findViewById(R.id.rv_slots);
        rvSlots.setLayoutManager(new GridLayoutManager(this, 3));

        // Generate button
        btnGenerate = findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(v -> generateSlots());
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

        TextView selected = minutes == 15 ? slot15 : minutes == 30 ? slot30 : slot60;
        selected.setBackgroundResource(R.drawable.bg_day_selected);
        selected.setTextColor(0xFFFFFFFF);
    }

    private void generateSlots() {
        int startHour   = tpStart.getHour();
        int startMinute = tpStart.getMinute();
        int endHour     = tpEnd.getHour();
        int endMinute   = tpEnd.getMinute();

        List<String> slots = new ArrayList<>();
        int current = startHour * 60 + startMinute;
        int end     = endHour * 60 + endMinute;

        while (current + selectedSlotDuration <= end) {
            int h = current / 60;
            int m = current % 60;
            String ampm = h >= 12 ? "PM" : "AM";
            int displayH = h > 12 ? h - 12 : (h == 0 ? 12 : h);
            slots.add(String.format("%d:%02d %s", displayH, m, ampm));
            current += selectedSlotDuration;
        }

        rvSlots.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 80));
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setTextSize(12);
                tv.setTextColor(0xFF2C6E8A);
                tv.setBackgroundResource(R.drawable.bg_input_field);
                tv.setPadding(8, 8, 8, 8);
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 80);
                params.setMargins(4, 4, 4, 4);
                tv.setLayoutParams(params);
                return new RecyclerView.ViewHolder(tv) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText(slots.get(position));
            }

            @Override
            public int getItemCount() { return slots.size(); }
        });
    }
}
