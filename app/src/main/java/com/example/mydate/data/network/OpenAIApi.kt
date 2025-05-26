package com.example.mydate.data.network

import com.example.mydate.data.model.ChatRequest
import com.example.mydate.data.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    fun getChatResponse(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest
    ): Call<ChatResponse>
}
