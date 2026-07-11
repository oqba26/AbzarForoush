package com.oqba26.abzarforoush.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeminiProxyRequest(val prompt: String)

@Serializable
data class GeminiProxyResponse(val result: String)

class GeminiService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
    }

    // آدرس پُل سوپابیس شما (از روی عکس استخراج شد)
    private val supabaseUrl = "https://qkhpoovfuvlusqvhdyjd.supabase.co/functions/v1/gemini-proxy"
    // کلید Anon سوپابیس شما (با موفقیت استخراج شد)
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFraHBvb3ZmdXZsdXNxdmhkeWpkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI4NjAzNzksImV4cCI6MjA5ODQzNjM3OX0.UV3KJMXFmDNp-62vEy_SOTIj1eCVn_Px2n7td4-HxY0"

    suspend fun analyzeFinancialData(prompt: String): String {
        return try {
            val response = client.post(supabaseUrl) {
                header(HttpHeaders.Authorization, "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(GeminiProxyRequest(prompt))
            }
            
            // اگه سرور جوابِ غیرِ جیسون داد، سعی می‌کنیم به صورت متن بخونیمش
            if (response.status == HttpStatusCode.OK) {
                try {
                    val proxyRes = response.body<GeminiProxyResponse>()
                    proxyRes.result
                } catch (e: Exception) {
                    // اگه جیسون نبود، متنِ خام رو برگردون
                    val rawText = response.body<String>()
                    if (rawText.contains("result")) {
                        // یه کلکِ رشتی برای استخراج متن از جیسونِ خام اگه سریالایزر قاطی کرد
                        rawText.substringAfter("\"result\":\"").substringBefore("\"}")
                    } else {
                        rawText
                    }
                }
            } else {
                "خطا در سرور سوپابیس: ${response.status}"
            }
        } catch (e: Exception) {
            "خطا در ارتباط: ${e.localizedMessage}"
        }
    }
}
