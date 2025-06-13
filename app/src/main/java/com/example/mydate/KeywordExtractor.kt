package com.example.mydate

import android.content.Context
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.tensorflow.lite.Interpreter
import android.content.res.AssetFileDescriptor
import android.util.Log

class KeywordExtractor {

    private var tflite: Interpreter? = null

    // 모델 로드
    fun loadModel(context: Context) {
        try {
            // 모델 파일을 assets에서 로드
            val fileDescriptor: AssetFileDescriptor = context.assets.openFd("k_extraction_model.tflite")
            val inputStream = fileDescriptor.createInputStream()
            val byteArray = ByteArray(fileDescriptor.length.toInt())
            inputStream.read(byteArray)
            val byteBuffer = ByteBuffer.allocateDirect(byteArray.size).order(ByteOrder.nativeOrder())
            byteBuffer.put(byteArray)
            tflite = Interpreter(byteBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("keyword", "모델 로드 중 에러 발생", e)
        }
    }

    // 예측을 위한 모델 실행
    fun predict(text: String): List<String> {
        val inputTensor = tflite?.getInputTensor(0)
        val inputShape = inputTensor?.shape()
        val inputSize = inputShape?.get(1) ?: 0

        // 텍스트를 문장 단위로 나누기
        val sentences = text.split("\n")

        // 문장을 처리하여 입력 데이터를 생성
        val inputBuffer = convertTextToInput(sentences, inputSize)

        // 출력 배열 크기 설정 (여기서는 3개의 키워드를 추출한다고 가정)
        val output = Array(1) { FloatArray(30) }

        // 모델 실행
        tflite?.run(inputBuffer, output)

        // 예측된 확률 값을 기준으로 가장 높은 값을 가진 인덱스를 반환
        val predictedKeywords = output[0].mapIndexed { index, value ->
            val keywords = extractKeywordsFromText(text) // 실제 키워드 추출
            val predictedKeyword = keywords.getOrElse(index) { "Unknown" }
            // 확률과 키워드를 함께 저장
            Pair(predictedKeyword, value)
        }

        // 확률 값을 기준으로 내림차순 정렬
        val sortedKeywords = predictedKeywords
            .sortedByDescending { it.second } // 확률 값을 기준으로 내림차순 정렬

        // 상위 10개의 키워드를 반환
        return sortedKeywords.take(30).map { "${it.first} " }
    }

    // 텍스트에서 중요한 키워드 추출 (실제 단어 추출)
    private fun extractKeywordsFromText(text: String): List<String> {
        val words = text.split("\\s+".toRegex())
        val filteredWords = words.filter { word ->
            word.length > 1 && !word.matches("\\d+".toRegex())  // 길이가 1 이상이고 숫자가 아니면 포함
        }

        val stopwords = setOf("느끼기","속에서", '1', '2', '3', '4', '5', '6', '7', '8', '9', '0') // 예시 불용어

        // 불용어를 제외하고 중요한 단어만 추출
        val importantWords =  filteredWords.filter { it !in stopwords && !it.matches(".*\\d.*-.*\\d.*".toRegex())}.toSet()

        // 예시로 상위 3개의 단어를 반환
        return importantWords.take(30)
    }

    // 텍스트를 입력 데이터로 변환하는 방법 (문장 단위로 처리)
    private fun convertTextToInput(sentences: List<String>, inputSize: Int): ByteBuffer {
        val input = FloatArray(inputSize)

        sentences.forEachIndexed { index, sentence ->
            if (index < inputSize) {
                input[index] = Math.random().toFloat() // 예시로 랜덤 값으로 채움
            }
        }

        val byteBuffer = ByteBuffer.allocateDirect(input.size * 4).order(ByteOrder.nativeOrder())
        for (value in input) {
            byteBuffer.putFloat(value)
        }
        return byteBuffer
    }
}