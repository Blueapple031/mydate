package com.example.mydate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class CourseAdapter(private val courses: List<List<Pair<String, String>>>, private val date: String) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    // ViewHolder 클래스 정의
    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        val courseLocationTextView: TextView = itemView.findViewById(R.id.courseLocationTextView)
        val courseDateTextView: TextView = itemView.findViewById(R.id.courseDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        val title = course[0].second
        val location = course[1].second

        // 제목, 위치, 날짜를 TextView에 바인딩
        holder.courseNameTextView.text = title
        holder.courseLocationTextView.text = location
        holder.courseDateTextView.text = date  // 날짜 추가
    }

    override fun getItemCount(): Int {
        return courses.size
    }
}