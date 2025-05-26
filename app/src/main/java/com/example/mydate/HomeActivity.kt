package com.example.mydate

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

class HomeActivity : AppCompatActivity() {

    private var isRequestInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val location = intent.getStringExtra("location") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val preference = intent.getStringExtra("preference") ?: ""

        val prompt = """
            아래 정보를 바탕으로 데이트 코스를 추천해줘.
            - 지역: $location
            - 날짜: $date
            - 스타일: $preference
            답변 형식은 
            오전: 장소 이름,위치
            점심: 장소 이름,위치
            오후: 장소 이름,위치
            저녁: 장소 이름,위치 
        """.trimIndent()

        requestChatGPT(prompt)
    }

    private fun requestChatGPT(prompt: String) {
        if (isRequestInProgress) {
            Toast.makeText(this, "잠시만 기다려 주세요...", Toast.LENGTH_SHORT).show()
            return
        }

        isRequestInProgress = true

        val api = OpenAIClient.instance
        val request = ChatRequest(
            messages = listOf(Message("user", prompt))
        )

        val call = api.getChatResponse("Bearer", request)

        call.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                isRequestInProgress = false // 요청 종료

                if (response.isSuccessful) {
                    val reply = response.body()?.choices?.get(0)?.message?.content
                    Log.d("GPT", "답변: $reply")
                    Toast.makeText(this@HomeActivity, reply, Toast.LENGTH_LONG).show()
                } else {
                    Log.e("GPT", "오류 코드: ${response.code()}")
                    Toast.makeText(this@HomeActivity, "오류 코드: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                isRequestInProgress = false // 요청 종료
                Log.e("GPT", "실패: ${t.message}")
                Toast.makeText(this@HomeActivity, "실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

