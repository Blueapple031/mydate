package com.example.mydate

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Arrays
import com.example.mydate.data.model.Course

private const val TAG = "HomeActivity"

class HomeActivity : AppCompatActivity() {
    private lateinit var keywordExtractor: KeywordExtractor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 'all_courses' 데이터를 전달받음
        @Suppress("DEPRECATION")
        val allCourses = intent.getParcelableArrayListExtra<Course>("all_courses") ?: return
        allCourses.forEach {
            Log.d(TAG, "onCreate: allCourses = ${it}")
        }

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

        keywordExtractor = KeywordExtractor()
        keywordExtractor.loadModel(applicationContext)

        val stringBuilder = StringBuilder() // StringBuilder 객체를 사용하여 문자열 생성

        for (date in visibleCourses) {
            for (pair in date) {
                if(pair.first == "제목"){
                    continue
                }
                if (pair.second.contains(",")) {
                    val items = pair.second.split(",")
                    for (item in items) {
                        stringBuilder.append(item.trim())
                        stringBuilder.append(" ")  // 공백 추가
                    }
                } else {
                    // 숫자도 아니고 쉼표도 없는 경우 그대로 추가
                    stringBuilder.append(pair.second)
                    stringBuilder.append(" ")
                }
            }
        }

        val text = stringBuilder.toString()

        Log.d("text",text)
        val result = keywordExtractor.predict(text)
        Log.d("KeywordExtractor", "추출된 키워드: ${result.joinToString(", ")}")
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        val keywordtext = StringBuilder()
        var cnt = 1
        for(keyword in result){
            keywordtext.append(cnt)
            keywordtext.append(". ")
            keywordtext.append(keyword)
            keywordtext.append("   ")
            cnt+=1
            if( cnt ==4)
                break
        }
        val temp= keywordtext.toString()
        resultTextView.text = temp
    }
}