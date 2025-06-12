package com.example.mydate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Course(
    val title: String,
    val morning: String,
    val lunch: String,
    val afternoon: String,
    val evening: String
) : Parcelable {
    companion object {
        fun fromPairs(pairs: List<Pair<String, String>>): Course {
            var title = ""
            var morning = ""
            var lunch = ""
            var afternoon = ""
            var evening = ""

            pairs.forEach { (key, value) ->
                when (key) {
                    "제목" -> title = value
                    "오전" -> morning = value
                    "점심" -> lunch = value
                    "오후" -> afternoon = value
                    "저녁" -> evening = value
                }
            }

            return Course(title, morning, lunch, afternoon, evening)
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "morning" to morning,
        "lunch" to lunch,
        "afternoon" to afternoon,
        "evening" to evening
    )
}