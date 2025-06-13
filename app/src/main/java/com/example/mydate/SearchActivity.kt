package com.example.mydate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mydate.data.model.Course
import com.example.mydate.data.repository.ChatGPTRepository
import com.example.mydate.data.repository.CourseRepository
import com.example.mydate.util.CourseExtractor
import com.example.mydate.data.model.ChatRequest
import com.example.mydate.data.model.ChatResponse
import com.example.mydate.data.model.Message
import com.example.mydate.data.network.OpenAIClient
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private var isRequestInProgress = false
    private val chatGPTRepository = ChatGPTRepository()
    private val courseRepository = CourseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)


        val location = intent?.getStringExtra("location") ?: ""
        val date = intent?.getStringExtra("date") ?: ""
        val preference = intent?.getStringExtra("preference") ?: ""

        // Intent 값이 제대로 전달되지 않으면 예외 처리
        if (location.isEmpty() || date.isEmpty() || preference.isEmpty()) {
            Toast.makeText(this, "필요한 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val prompt = """
            아래 정보를 바탕으로 데이트 코스 10가지를 추천해줘.
            - 지역: $location
            - 날짜: $date
            - 스타일: $preference 
            형식은
            
            제목: 각 코스 특징을 간단하게 표현
            오전: 장소 이름, 위치 (위도, 경도)
            점심: 장소 이름, 위치 (위도, 경도)
            오후: 장소 이름, 위치 (위도, 경도)
            저녁: 장소 이름, 위치 (위도, 경도)
        
            5개의 코스를 각각 나열하고, 각 코스의 장소와 위치를 정확하게 제시해줘.
            반드시 한국어로 대답해줘.
            """.trimIndent()

        requestChatGPT(prompt, date, location)
    }

    private fun requestChatGPT(prompt: String, date: String, location: String) {
        if (isRequestInProgress) {
            Toast.makeText(this, "잠시만 기다려 주세요...", Toast.LENGTH_SHORT).show()
            return
        }

        isRequestInProgress = true

        chatGPTRepository.requestChatGPT(
            prompt = prompt,
            onSuccess = { reply ->
                isRequestInProgress = false
                val courses = CourseExtractor.extractCourses(reply)
                courseRepository.saveCourses(courses)

                val intent = Intent(this, HomeActivity::class.java)
                intent.putParcelableArrayListExtra("all_courses", ArrayList<Course>(courses))
                intent.putExtra("location", location)
                intent.putExtra("date", date)
                startActivity(intent)
            },
            onError = { error ->
                isRequestInProgress = false
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        )
    }
}


