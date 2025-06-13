package com.example.mydate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mydate.data.model.Course
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class CourseAdapter(private val courses: List<Course>, private val date: String) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    // ViewHolder 클래스 정의
    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        val courseLocationTextView: TextView = itemView.findViewById(R.id.courseLocationTextView)
        val courseDateTextView: TextView = itemView.findViewById(R.id.courseDateTextView)
        val heartImageView: ImageView = itemView.findViewById(R.id.heartImageView)
        var course: Course? = null

        init {
            heartImageView.setOnClickListener {
                course?.isFavorite = !(course?.isFavorite ?: false)

                // Firestore에서 찜 상태 업데이트

                updateCourseInDatabase(course)
                if (course?.isFavorite == true) {
                    heartImageView.setImageResource(android.R.drawable.btn_star_big_on) // 빨간색 하트

                } else {
                    heartImageView.setImageResource(android.R.drawable.btn_star_big_off) // 빈 하트
                }
            }
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val course = courses[position]
                    val intent = Intent(itemView.context, MapActivity::class.java)
                    intent.putExtra("course", course)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }
    private fun updateCourseInDatabase(course: Course?) {
        val coursesRef = FirebaseFirestore.getInstance().collection("hearts")
        course?.let {
            val courseId = "${it.title.replace(" ", "_")}" // 문서 ID 설정 (제목을 기준으로)

            if (it.isFavorite) {
                // 찜 상태가 true일 때는 Firestore에 저장
                coursesRef.document(courseId).set(it.toMap(), SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Firebase", "코스 $courseId 저장/업데이트 성공")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "코스 $courseId 저장/업데이트 실패", exception)
                    }
            } else {

                coursesRef.document(courseId).delete()
                    .addOnSuccessListener {
                        Log.d("Firebase", "코스 $courseId 삭제 성공")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "코스 $courseId 삭제 실패", exception)
                    }
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.course = course
        // 제목, 위치, 날짜를 TextView에 바인딩
        holder.courseNameTextView.text = course.title
        holder.courseLocationTextView.text = course.morning
        holder.courseDateTextView.text = date
        if (course.isFavorite) {
            holder.heartImageView.setImageResource(android.R.drawable.btn_star_big_on)  // 빨간색 하트
        } else {
            holder.heartImageView.setImageResource(android.R.drawable.btn_star_big_off)  // 빈 하트
        }
    }

    override fun getItemCount(): Int {
        return courses.size
    }
}