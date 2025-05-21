package com.example.mydate

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var editTextDate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val locationInput = findViewById<EditText>(R.id.editTextLocation)
        editTextDate = findViewById(R.id.editTextDate)
        val preferenceInput = findViewById<EditText>(R.id.editTextPreference)

        val indoorBtn = findViewById<ToggleButton>(R.id.buttonIndoor)
        val outdoorBtn = findViewById<ToggleButton>(R.id.buttonOutdoor)
        val cultureBtn = findViewById<ToggleButton>(R.id.buttonCulture)
        val cafeBtn = findViewById<ToggleButton>(R.id.buttonCafe)

        val startBtn = findViewById<Button>(R.id.buttonStart)

        editTextDate.setOnClickListener {
            showDatePicker()
        }

        startBtn.setOnClickListener {
            val location = locationInput.text.toString()
            val date = editTextDate.text.toString()
            val preference = preferenceInput.text.toString()

            val selectedStyles = mutableListOf<String>()
            if (indoorBtn.isChecked) selectedStyles.add("실내 활동")
            if (outdoorBtn.isChecked) selectedStyles.add("야외 활동")
            if (cultureBtn.isChecked) selectedStyles.add("문화 예술")
            if (cafeBtn.isChecked) selectedStyles.add("맛집 탐방")

            // 여기에 추천 로직을 추가하세요
            Toast.makeText(this, "위치: $location\n날짜: $date\n스타일: $selectedStyles", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            editTextDate.setText("${y}년 ${m + 1}월 ${d}일")
        }, year, month, day)

        datePicker.show()
    }
}
