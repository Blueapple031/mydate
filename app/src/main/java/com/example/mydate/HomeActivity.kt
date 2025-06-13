package com.example.mydate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mydate.data.model.Course

private const val TAG = "HomeActivity"
private const val MAX_VISIBLE_COURSES = 3
private const val MAX_KEYWORDS = 3

class HomeActivity : AppCompatActivity() {
    private lateinit var keywordExtractor: KeywordExtractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        initializeData()
        processKeywords()
    }

    private fun initializeData() {
        @Suppress("DEPRECATION")
        val allCourses = intent.getParcelableArrayListExtra<Course>("all_courses") ?: return
        val date = intent.getStringExtra("date") ?: ""
        val location = intent.getStringExtra("location") ?: ""

        setupRecyclerView(allCourses, date)
        updateLocationInfo(date, location)

        keywordExtractor = KeywordExtractor()
        keywordExtractor.loadModel(applicationContext)
    }

    private fun setupRecyclerView(courses: ArrayList<Course>, date: String) {
        val courseRecyclerView = findViewById<RecyclerView>(R.id.courseRecyclerView)
        val visibleCourses = courses.take(MAX_VISIBLE_COURSES)

        val adapter = CourseAdapter(courses, date)
        courseRecyclerView.layoutManager = LinearLayoutManager(this)
        courseRecyclerView.adapter = adapter

        Log.d(TAG, "Visible courses: $visibleCourses")
    }

    private fun updateLocationInfo(date: String, location: String) {
        findViewById<TextView>(R.id.dateTextView).text = "날짜: $date"
        findViewById<TextView>(R.id.locationTextView).text = "위치: $location"
    }

    private fun processKeywords() {
        val visibleCourses = intent.getParcelableArrayListExtra<Course>("all_courses")?.take(MAX_VISIBLE_COURSES) ?: return
        val text = buildCourseText(visibleCourses)

        Log.d(TAG, "Processed text: $text")
        val keywords = keywordExtractor.predict(text)
        Log.d(TAG, "Extracted keywords: ${keywords.joinToString(", ")}")

        displayKeywords(keywords)
    }

    private fun buildCourseText(courses: List<Course>): String {
        return StringBuilder().apply {
            for (course in courses) {
                val courseMap = course.toMap()
                courseMap.forEach { (key, value) ->
                    if (key == "title") return@forEach

                    if (value is String && value.contains(",")) {
                        value.split(",").forEach { item ->
                            append(item.trim())
                            append(" ")
                        }
                    } else {
                        append(value)
                        append(" ")
                    }
                }
            }
        }.toString()
    }

    private fun displayKeywords(keywords: List<String>) {
        val resultTextView = findViewById<TextView>(R.id.resultTextView)
        val keywordText = StringBuilder().apply {
            keywords.take(MAX_KEYWORDS).forEachIndexed { index, keyword ->
                append("${index + 1}. ")
                append(keyword)
                append("   ")
            }
        }.toString()

        resultTextView.text = keywordText
    }

    fun onClickBookmark(view: View) {
        val intent = Intent(this, BookmarkActivity::class.java)
        startActivity(intent)
    }
}