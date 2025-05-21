package com.example.mydate

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mydate.data.model.ChatRequest
import com.example.mydate.data.model.ChatResponse
import com.example.mydate.data.model.Message
import com.example.mydate.data.network.OpenAIApi
import com.example.mydate.data.network.OpenAIClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val api = OpenAIClient.instance
        val request = ChatRequest(
            messages = listOf(
                Message("user", "안녕 GPT!")
            )
        )

        val call = api.getChatResponse("Bearer YOUR_API_KEY", request)

        call.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.choices?.get(0)?.message?.content
                    Log.d("GPT", "답변: $reply")
                } else {
                    Log.e("GPT", "오류 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Log.e("GPT", "실패: ${t.message}")
            }
        })
    }
}
