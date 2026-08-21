package com.example.data.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class AiStreamClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ApiChatMessage>,
        temperature: Float = 0.7f,
        maxTokens: Int = 4096,
        customHeaders: Map<String, String> = emptyMap()
    ): Flow<StreamEvent> = flow {
        var cleanBaseUrl = baseUrl.trimEnd('/')
        if (!cleanBaseUrl.endsWith("/v1") && !cleanBaseUrl.contains("/chat/completions")) {
            // If base url is like https://api.openai.com, append /v1/chat/completions
            if (!cleanBaseUrl.endsWith("/v1")) {
                cleanBaseUrl = "$cleanBaseUrl/v1/chat/completions"
            } else {
                cleanBaseUrl = "$cleanBaseUrl/chat/completions"
            }
        } else if (cleanBaseUrl.endsWith("/v1")) {
            cleanBaseUrl = "$cleanBaseUrl/chat/completions"
        }

        // Construct JSON Payload
        val jsonPayload = JSONObject().apply {
            put("model", model)
            put("stream", true)
            put("temperature", temperature)
            put("max_tokens", maxTokens)

            val messagesArray = JSONArray()
            for (msg in messages) {
                val msgObj = JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                }
                messagesArray.put(msgObj)
            }
            put("messages", messagesArray)
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val requestBuilder = Request.Builder()
            .url(cleanBaseUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        // Custom headers (e.g. OpenRouter HTTP-Referer, X-Title, custom proxy keys)
        for ((key, value) in customHeaders) {
            if (key.isNotBlank() && value.isNotBlank()) {
                requestBuilder.addHeader(key, value)
            }
        }

        val request = requestBuilder.build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "Unknown error"
                emit(StreamEvent.Error("API Error (${response.code}): $errBody"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(StreamEvent.Error("Empty response body from AI provider"))
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
            val fullResponseBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.isEmpty() || currentLine.startsWith(":")) {
                    continue // SSE comment or heartbeat
                }

                if (currentLine.startsWith("data:")) {
                    val data = currentLine.removePrefix("data:").trim()
                    if (data == "[DONE]") {
                        break
                    }

                    try {
                        val json = JSONObject(data)
                        val choices = json.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val choice = choices.getJSONObject(0)
                            val delta = choice.optJSONObject("delta")
                            val content = delta?.optString("content", "") ?: ""
                            if (content.isNotEmpty()) {
                                fullResponseBuilder.append(content)
                                emit(StreamEvent.Chunk(content))
                            }
                        }
                    } catch (e: Exception) {
                        // Skip malformed chunk
                    }
                }
            }

            val finalOutput = fullResponseBuilder.toString()
            emit(StreamEvent.Complete(finalOutput))

        } catch (e: Exception) {
            emit(StreamEvent.Error("Network/Stream failure: ${e.localizedMessage ?: e.message}"))
        }
    }.flowOn(Dispatchers.IO)
}
