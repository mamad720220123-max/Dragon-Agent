package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_providers")
data class ApiProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val apiKeyEncrypted: String,
    val isDefault: Boolean = false,
    val tokenLimit: Long = 200000L,
    val tokensUsed: Long = 0L,
    val customHeadersJson: String = "{}",
    val modelsJson: String = "[\"gpt-4o\", \"gpt-4o-mini\", \"claude-3-5-sonnet\", \"deepseek-chat\", \"llama-3.3-70b\", \"qwen-2.5-coder-32b\"]",
    val selectedModel: String = "gpt-4o",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val systemPrompt: String = "You are Dragon Studio AI, an expert intelligent software engineering agent."
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Fact", // Fact, Preference, CodeStyle, ProjectContext
    val contentEncrypted: String,
    val importance: Int = 3, // 1 to 5
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastRecalledAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: String = "CHAT", // CHAT or CODER_AGENT
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val providerId: Long = 0,
    val modelId: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val role: String, // user, assistant, system, tool
    val contentEncrypted: String,
    val toolCallsJson: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

@Entity(tableName = "workspace_projects")
data class WorkspaceProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false
)

@Entity(tableName = "project_files")
data class ProjectFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val path: String, // e.g. "index.html", "css/style.css"
    val name: String,
    val isDirectory: Boolean = false,
    val parentPath: String = "", // e.g. "", "css"
    val contentEncrypted: String = "",
    val language: String = "plaintext",
    val lastModified: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val manualUserModified: Boolean = false
)

@Entity(tableName = "file_versions")
data class FileVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val versionNumber: Int,
    val contentEncrypted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val changeDescription: String = "Edit snapshot"
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
