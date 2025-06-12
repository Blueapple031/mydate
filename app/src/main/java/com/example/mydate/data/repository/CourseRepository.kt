package com.example.mydate.data.repository

import android.util.Log
import com.example.mydate.data.model.Course
import com.google.firebase.firestore.FirebaseFirestore

class CourseRepository {
    private val database = FirebaseFirestore.getInstance()
    private val coursesRef = database.collection("courses")

    fun saveCourses(courses: List<Course>) {
        courses.forEachIndexed { index, course ->
            val courseId = "course${index + 1}"
            coursesRef.document(courseId).set(course.toMap())
                .addOnSuccessListener {
                    Log.d("Firebase", "코스 $courseId 저장 성공")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "코스 $courseId 저장 실패", exception)
                }
        }
    }
} 