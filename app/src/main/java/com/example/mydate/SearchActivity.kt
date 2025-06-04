package com.example.mydate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mydate.data.model.ChatRequest
import com.example.mydate.data.model.ChatResponse
import com.example.mydate.data.model.Message
import com.example.mydate.data.network.OpenAIClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class SearchActivity : AppCompatActivity() {

    private var isRequestInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // null 체크를 통해 Intent 값이 제대로 전달되는지 확인
        val location = intent?.getStringExtra("location") ?: ""  // null 처리
        val date = intent?.getStringExtra("date") ?: ""  // null 처리
        val preference = intent?.getStringExtra("preference") ?: ""  // null 처리

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
            오전: 장소 이름, 위치
            점심: 장소 이름, 위치
            오후: 장소 이름, 위치
            저녁: 장소 이름, 위치
        
            5개의 코스를 각각 나열하고, 각 코스의 장소와 위치를 정확하게 제시해줘.
            """.trimIndent()

        requestChatGPT(prompt,date,location)
    }

    private fun requestChatGPT(prompt: String, date: String,location:String) {
        if (isRequestInProgress) {
            Toast.makeText(this, "잠시만 기다려 주세요...", Toast.LENGTH_SHORT).show()
            return
        }

        isRequestInProgress = true

        val api = OpenAIClient.instance
        val request = ChatRequest(
            messages = listOf(Message("user", prompt))
        )

        val call = api.getChatResponse("Bearer ", request)

        call.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                isRequestInProgress = false // 요청 종료

                if (response.isSuccessful) {
                    val reply = response.body()?.choices?.get(0)?.message?.content
                    Log.d("GPT", "답변: $reply")

                    if (reply != null) {
                        val allCourses = extractAllCourses(reply)
                        val intent = Intent(this@SearchActivity, HomeActivity::class.java)
                        intent.putExtra("all_courses", ArrayList(allCourses))
                        intent.putExtra("location", location)
                        intent.putExtra("date", date)

                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SearchActivity, "답변이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("GPT", "오류 코드: ${response.code()}")
                    Toast.makeText(this@SearchActivity, "오류 코드: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                isRequestInProgress = false // 요청 종료
                Log.e("GPT", "실패: ${t.message}")
                Toast.makeText(this@SearchActivity, "실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun extractAllCourses(response: String): List<List<Pair<String, String>>> {
        val courses = mutableListOf<List<Pair<String, String>>>()
        val lines = response.lines()

        var currentCourse = mutableListOf<Pair<String, String>>()

        for (i in 0 until lines.size) {
            val line = lines.getOrNull(i)?.trim() ?: continue

            // 제목을 찾은 경우
            if (line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") || line.startsWith("4.") || line.startsWith("5.")) {
                // 이미 있는 코스가 있다면 추가 후 새 코스를 시작
                if (currentCourse.isNotEmpty()) {
                    courses.add(currentCourse)
                }
                currentCourse = mutableListOf()  // 새로운 코스를 위한 초기화

                // 제목 부분 처리
                val title = line.split(":").getOrNull(1)?.trim() ?: continue
                currentCourse.add(Pair("제목", title))  // 제목 추가
            }

            // 각 구간에 대해 오전, 점심, 오후, 저녁 장소 정보 찾기
            else if ( line.startsWith("제목:")|| line.startsWith("오전:") || line.startsWith("점심:") || line.startsWith("오후:") || line.startsWith("저녁:")) {
                val parts = line.split(":").map { it.trim() }
                if (parts.size >= 2) {
                    val timeOfDay = parts[0] // 오전, 점심, 오후, 저녁
                    val location = parts[1] // 장소
                    currentCourse.add(Pair(timeOfDay, location))  // 시간대와 장소 추가
                }
            }
        }

        // 마지막 코스도 추가
        if (currentCourse.isNotEmpty()) {
            courses.add(currentCourse)
        }

        return courses
    }
}


