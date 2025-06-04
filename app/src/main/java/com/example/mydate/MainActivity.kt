package com.example.mydate

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var editTextDate: EditText
    private lateinit var startBtn: Button
    private var isClicked = false // 중복 클릭 방지 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val locationInput = findViewById<EditText>(R.id.editTextLocation)
        editTextDate = findViewById(R.id.editTextDate)
        val preferenceInput = findViewById<EditText>(R.id.editTextPreference)

        startBtn = findViewById(R.id.buttonStart)

        editTextDate.setOnClickListener {
            showDatePicker()
        }

        startBtn.setOnClickListener {
            if (isClicked) return@setOnClickListener // 중복 클릭 방지
            isClicked = true
            startBtn.isEnabled = false

            val location = locationInput.text.toString()
            val date = editTextDate.text.toString()
            val preference = preferenceInput.text.toString()


            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra("location", location)
                putExtra("date", date)
                putExtra("preference", preference)
            }
            startActivity(intent)
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

    override fun onResume() {
        super.onResume()
        // 다시 활성화 (원할 경우)
        isClicked = false
        startBtn.isEnabled = true
    }
}

