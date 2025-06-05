package com.example.mydate

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 'all_courses' 데이터를 전달받음
        val allCourses = intent.getSerializableExtra("all_courses") as? ArrayList<List<Pair<String, String>>> ?: return


        val date = intent.getStringExtra("date") ?: ""
        val location = intent.getStringExtra("location") ?: ""

        // 날짜와 위치 텍스트뷰 업데이트
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        val locationTextView = findViewById<TextView>(R.id.locationTextView)

        dateTextView.text = "날짜: $date"
        locationTextView.text = "위치: $location"

        // RecyclerView 설정
        val courseRecyclerView = findViewById<RecyclerView>(R.id.courseRecyclerView)

        // 첫 3개 코스만 표시하도록 설정
        val visibleCourses = allCourses.take(3)

        // Adapter 설정
        val adapter = CourseAdapter(allCourses, date) // 전체 코스를 전달
        courseRecyclerView.layoutManager = LinearLayoutManager(this)  // LayoutManager 설정
        courseRecyclerView.adapter = adapter // Adapter 설정

        // 로그로 확인
        Log.d("HomeActivity", "Visible courses: $visibleCourses")
    }
}