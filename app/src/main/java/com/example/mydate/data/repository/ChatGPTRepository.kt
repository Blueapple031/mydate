package com.example.mydate.data.repository

import com.example.mydate.BuildConfig
import com.example.mydate.data.model.ChatRequest
import com.example.mydate.data.model.ChatResponse
import com.example.mydate.data.model.Message
import com.example.mydate.data.network.OpenAIClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatGPTRepository {
    private val api = OpenAIClient.instance
    private val apiKey = BuildConfig.OPENAI_API_KEY

    fun requestChatGPT(
        prompt: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = ChatRequest(messages = listOf(Message("user", prompt)))
        val call = api.getChatResponse("Bearer $apiKey", request)

        call.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val reply = response.body()?.choices?.get(0)?.message?.content
                    if (reply != null) {
                        onSuccess(reply)
                    } else {
                        onError("답변이 없습니다.")
                    }
                } else {
                    onError("오류 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                onError("실패: ${t.message}")
            }
        })
    }
}