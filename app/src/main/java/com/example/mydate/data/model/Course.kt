package com.example.mydate.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class Course(
    val title: String,
    val morning: String,
    val lunch: String,
    val afternoon: String,
    val evening: String,
    val morningLatLng: LatLng? = null,
    val lunchLatLng: LatLng? = null,
    val afternoonLatLng: LatLng? = null,
    val eveningLatLng: LatLng? = null,
    var isFavorite: Boolean = false
) : Parcelable {
    companion object {
        fun fromPairs(pairs: List<Pair<String, String>>): Course {
            var title = ""
            var morning = ""
            var lunch = ""
            var afternoon = ""
            var evening = ""
            var morningLatLng: LatLng? = null
            var lunchLatLng: LatLng? = null
            var afternoonLatLng: LatLng? = null
            var eveningLatLng: LatLng? = null

            pairs.forEach { (key, value) ->
                when (key) {
                    "제목" -> title = value
                    "오전" -> {
                        val (location, latLng) = parseLocationWithCoordinates(value)
                        morning = location
                        morningLatLng = latLng
                    }
                    "점심" -> {
                        val (location, latLng) = parseLocationWithCoordinates(value)
                        lunch = location
                        lunchLatLng = latLng
                    }
                    "오후" -> {
                        val (location, latLng) = parseLocationWithCoordinates(value)
                        afternoon = location
                        afternoonLatLng = latLng
                    }
                    "저녁" -> {
                        val (location, latLng) = parseLocationWithCoordinates(value)
                        evening = location
                        eveningLatLng = latLng
                    }
                }
            }

            return Course(
                title = title,
                morning = morning,
                lunch = lunch,
                afternoon = afternoon,
                evening = evening,
                morningLatLng = morningLatLng,
                lunchLatLng = lunchLatLng,
                afternoonLatLng = afternoonLatLng,
                eveningLatLng = eveningLatLng
            )
        }

        private fun parseLocationWithCoordinates(value: String): Pair<String, LatLng?> {
            // "장소명 (위도, 경도)" 형식에서 위도/경도 추출
            val regex = """(.*?)\s*\(([-\d.]+),\s*([-\d.]+)\)""".toRegex()
            val matchResult = regex.find(value)
            
            return if (matchResult != null) {
                val (location, lat, lng) = matchResult.destructured
                Pair(location.trim(), LatLng(lat.toDouble(), lng.toDouble()))
            } else {
                Pair(value.trim(), null)
            }
        }
    }

    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "morning" to morning,
        "lunch" to lunch,
        "afternoon" to afternoon,
        "evening" to evening,
        "morningLatLng" to (morningLatLng?.let { "${it.latitude},${it.longitude}" } ?: ""),
        "lunchLatLng" to (lunchLatLng?.let { "${it.latitude},${it.longitude}" } ?: ""),
        "afternoonLatLng" to (afternoonLatLng?.let { "${it.latitude},${it.longitude}" } ?: ""),
        "eveningLatLng" to (eveningLatLng?.let { "${it.latitude},${it.longitude}" } ?: "")
    )
}