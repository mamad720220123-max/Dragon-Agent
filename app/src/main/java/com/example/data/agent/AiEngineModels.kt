package com.example.data.agent

data class ApiChatRequest(
    val model: String,
    val messages: List<ApiChatMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 4096,
    val stream: Boolean = true
)

data class ApiChatMessage(
    val role: String,
    val content: String
)

sealed class StreamEvent {
    data class Chunk(val text: String) : StreamEvent()
    data class Complete(val fullText: String, val promptTokens: Int = 0, val completionTokens: Int = 0) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
}

data class ExtractedCodeBlock(
    val language: String,
    val filename: String?,
    val code: String
)

data class DragonToolAction(
    val actionType: ActionType,
    val path: String = "",
    val sourcePath: String = "",
    val destinationPath: String = "",
    val targetContent: String = "",
    val replacementContent: String = "",
    val content: String = ""
) {
    enum class ActionType {
        CREATE_FILE,
        CREATE_FOLDER,
        DELETE_FILE,
        MOVE_FILE,
        REPLACE_CODE,
        REWRITE_FILE
    }
}
