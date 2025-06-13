package com.example.mydate

import android.app.AlertDialog
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
        val courseTitle: TextView = itemView.findViewById(R.id.courseTitle)
        val morningLocation: TextView = itemView.findViewById(R.id.morningLocation)
        val lunchLocation: TextView = itemView.findViewById(R.id.lunchLocation)
        val afternoonLocation: TextView = itemView.findViewById(R.id.afternoonLocation)
        val eveningLocation: TextView = itemView.findViewById(R.id.eveningLocation)
        val favoriteButton: ImageView = itemView.findViewById(R.id.favoriteButton)
        var course: Course? = null

        init {
            favoriteButton.setOnClickListener {
                course?.let { currentCourse ->
                    if (currentCourse.isFavorite) {
                        // 찜 취소 시 확인 대화상자 표시
                        showUnfavoriteConfirmationDialog(currentCourse)
                    } else {
                        // 찜하기
                        currentCourse.isFavorite = true
                        updateCourseInDatabase(currentCourse)
                        favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
                    }
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

        private fun showUnfavoriteConfirmationDialog(course: Course) {
            AlertDialog.Builder(itemView.context)
                .setTitle("찜 취소")
                .setMessage("찜 목록에서 삭제하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    course.isFavorite = false
                    updateCourseInDatabase(course)
                    favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
                }
                .setNegativeButton("아니오", null)
                .show()
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
        holder.courseTitle.text = course.title
        holder.morningLocation.text = course.morning
        holder.lunchLocation.text = course.lunch
        holder.afternoonLocation.text = course.afternoon
        holder.eveningLocation.text = course.evening
        if (course.isFavorite) {
            holder.favoriteButton.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            holder.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    override fun getItemCount(): Int {
        return courses.size
    }
}