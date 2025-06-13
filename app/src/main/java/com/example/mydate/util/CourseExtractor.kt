package com.example.mydate.util

import com.example.mydate.data.model.Course

object CourseExtractor {
    fun extractCourses(response: String): List<Course> {
        val courses = mutableListOf<Course>()
        val lines = response.lines()
        var currentCourse = mutableListOf<Pair<String, String>>()

        for (i in 0 until lines.size) {
            val line = lines.getOrNull(i)?.trim() ?: continue

            if (line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") ||
                line.startsWith("4.") || line.startsWith("5.")) {
                if (currentCourse.isNotEmpty()) {
                    courses.add(Course.fromPairs(currentCourse))
                }
                currentCourse = mutableListOf()

                val title = line.split(":").getOrNull(1)?.trim() ?: continue
                currentCourse.add(Pair("제목", title))
            } else if (line.startsWith("제목:") || line.startsWith("오전:") ||
                line.startsWith("점심:") || line.startsWith("오후:") ||
                line.startsWith("저녁:")) {
                val parts = line.split(":").map { it.trim() }
                if (parts.size >= 2) {
                    val timeOfDay = parts[0]
                    val location = parts[1]
                    currentCourse.add(Pair(timeOfDay, location))
                }
            }
        }

        if (currentCourse.isNotEmpty()) {
            courses.add(Course.fromPairs(currentCourse))
        }

        return courses
    }
}