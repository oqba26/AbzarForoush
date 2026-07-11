package com.oqba26.abzarforoush.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DeepSeekRequest(val prompt: String)

@Serializable
data class DeepSeekResponse(val analysis: String)

class DeepSeekService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // آدرس Supabase Edge Function شما
    private val supabaseUrl = "https://your-project-id.supabase.co/functions/v1/deepseek-analyze"
    // این توکن رو باید در محیط امن نگه داشت، فعلاً به عنوان جایگزین
    private val supabaseKey = "YOUR_SUPABASE_ANON_KEY"

    suspend fun analyzeFinancialData(prompt: String): String {
        return try {
            val response: DeepSeekResponse = client.post(supabaseUrl) {
                header(HttpHeaders.Authorization, "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(DeepSeekRequest(prompt))
            }.body()
            response.analysis
        } catch (e: Exception) {
            "خطا در تحلیل داده‌ها: ${e.localizedMessage}"
        }
    }
}
