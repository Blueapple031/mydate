package com.example.mydate

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mydate.CourseAdapter
import com.example.mydate.data.model.Course
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView

class BookmarkActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: CourseAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.bookmarkRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CourseAdapter(emptyList(), "")
        recyclerView.adapter = adapter
        
        loadBookmarkedCourses()
    }

    private fun loadBookmarkedCourses() {
        db.collection("hearts")
            .get()
            .addOnSuccessListener { documents ->
                val courses = documents.mapNotNull { doc ->
                    try {
                        Course.fromMap(doc.data)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (courses.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    adapter = CourseAdapter(courses, "")
                    recyclerView.adapter = adapter
                }
            }
            .addOnFailureListener { e ->
                // 에러 처리
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                emptyView.text = "데이터를 불러오는데 실패했습니다"
            }
    }
} 