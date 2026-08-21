package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiProviderDao {
    @Query("SELECT * FROM api_providers ORDER BY isDefault DESC, id ASC")
    fun getAllProvidersFlow(): Flow<List<ApiProviderEntity>>

    @Query("SELECT * FROM api_providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: Long): ApiProviderEntity?

    @Query("SELECT * FROM api_providers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultProvider(): ApiProviderEntity?

    @Query("SELECT * FROM api_providers ORDER BY isDefault DESC, id ASC")
    suspend fun getAllProvidersList(): List<ApiProviderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ApiProviderEntity): Long

    @Update
    suspend fun updateProvider(provider: ApiProviderEntity)

    @Query("UPDATE api_providers SET isDefault = CASE WHEN id = :selectedId THEN 1 ELSE 0 END")
    suspend fun setDefaultProvider(selectedId: Long)

    @Query("UPDATE api_providers SET selectedModel = :model WHERE id = :providerId")
    suspend fun updateSelectedModel(providerId: Long, model: String)

    @Query("UPDATE api_providers SET tokensUsed = tokensUsed + :tokens WHERE id = :providerId")
    suspend fun incrementTokenUsage(providerId: Long, tokens: Long)

    @Query("DELETE FROM api_providers WHERE id = :id")
    suspend fun deleteProvider(id: Long)
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY importance DESC, lastRecalledAt DESC")
    fun getAllMemoriesFlow(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE isActive = 1 ORDER BY importance DESC, lastRecalledAt DESC")
    suspend fun getActiveMemoriesList(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE isActive = 1 AND category = :category ORDER BY importance DESC, lastRecalledAt DESC")
    suspend fun getMemoriesByCategory(category: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    suspend fun getMemoryById(id: Long): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("UPDATE memories SET lastRecalledAt = :time WHERE id = :id")
    suspend fun updateRecalledTime(id: Long, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessionsFlow(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions WHERE mode = :mode ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestSessionByMode(mode: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET updatedAt = :time, title = :title WHERE id = :id")
    suspend fun touchSession(id: Long, title: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    // Messages
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesFlow(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesList(sessionId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearMessagesForSession(sessionId: Long)
}

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspace_projects ORDER BY isCurrent DESC, updatedAt DESC")
    fun getAllProjectsFlow(): Flow<List<WorkspaceProjectEntity>>

    @Query("SELECT * FROM workspace_projects WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentProject(): WorkspaceProjectEntity?

    @Query("SELECT * FROM workspace_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): WorkspaceProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: WorkspaceProjectEntity): Long

    @Update
    suspend fun updateProject(project: WorkspaceProjectEntity)

    @Query("UPDATE workspace_projects SET isCurrent = CASE WHEN id = :selectedId THEN 1 ELSE 0 END")
    suspend fun setCurrentProject(selectedId: Long)

    @Query("DELETE FROM workspace_projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    // Files
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY isDirectory DESC, path ASC")
    fun getProjectFilesFlow(projectId: Long): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY isDirectory DESC, path ASC")
    suspend fun getProjectFilesList(projectId: Long): List<ProjectFileEntity>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPath(projectId: Long, path: String): ProjectFileEntity?

    @Query("SELECT * FROM project_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): ProjectFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFileEntity): Long

    @Update
    suspend fun updateFile(file: ProjectFileEntity)

    @Query("DELETE FROM project_files WHERE id = :id")
    suspend fun deleteFile(id: Long)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND (path = :path OR path LIKE :folderPrefix)")
    suspend fun deleteFileOrFolder(projectId: Long, path: String, folderPrefix: String)

    // File Versions
    @Query("SELECT * FROM file_versions WHERE fileId = :fileId ORDER BY versionNumber DESC")
    fun getFileVersionsFlow(fileId: Long): Flow<List<FileVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: FileVersionEntity): Long
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)
}
