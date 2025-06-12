package com.example.mydate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mydate.data.model.Course

class CourseAdapter(private val courses: List<Course>, private val date: String) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    // ViewHolder 클래스 정의
    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        val courseLocationTextView: TextView = itemView.findViewById(R.id.courseLocationTextView)
        val courseDateTextView: TextView = itemView.findViewById(R.id.courseDateTextView)

        init {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]

        // 제목, 위치, 날짜를 TextView에 바인딩
        holder.courseNameTextView.text = course.title
        holder.courseLocationTextView.text = course.morning
        holder.courseDateTextView.text = date
    }

    override fun getItemCount(): Int {
        return courses.size
    }
}