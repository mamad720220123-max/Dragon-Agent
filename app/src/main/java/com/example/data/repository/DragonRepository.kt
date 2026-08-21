package com.example.data.repository

import android.content.Context
import com.example.data.agent.*
import com.example.data.crypto.CryptoManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DragonRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val apiProviderDao = db.apiProviderDao()
    private val memoryDao = db.memoryDao()
    private val chatDao = db.chatDao()
    private val workspaceDao = db.workspaceDao()
    private val settingsDao = db.settingsDao()
    private val aiClient = AiStreamClient()

    // -------------------------------------------------------------
    // INITIALIZATION & SEEDING
    // -------------------------------------------------------------
    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        // 1. Seed API Providers if none exist
        val providers = apiProviderDao.getAllProvidersList()
        if (providers.isEmpty()) {
            val defaultOpenAi = ApiProviderEntity(
                name = "OpenAI Official",
                baseUrl = "https://api.openai.com/v1",
                apiKeyEncrypted = CryptoManager.encrypt(""),
                isDefault = true,
                tokenLimit = 500000L,
                tokensUsed = 0L,
                modelsJson = "[\"gpt-4o\", \"gpt-4o-mini\", \"o3-mini\", \"gpt-4-turbo\"]",
                selectedModel = "gpt-4o",
                temperature = 0.7f,
                maxTokens = 4096
            )
            val openRouter = ApiProviderEntity(
                name = "OpenRouter AI",
                baseUrl = "https://openrouter.ai/api/v1",
                apiKeyEncrypted = CryptoManager.encrypt(""),
                isDefault = false,
                tokenLimit = 1000000L,
                tokensUsed = 0L,
                modelsJson = "[\"anthropic/claude-3.5-sonnet\", \"deepseek/deepseek-chat\", \"meta-llama/llama-3.3-70b-instruct\", \"google/gemini-2.0-flash-001\", \"qwen/qwen-2.5-coder-32b-instruct\"]",
                selectedModel = "anthropic/claude-3.5-sonnet",
                temperature = 0.7f,
                maxTokens = 4096
            )
            val groq = ApiProviderEntity(
                name = "Groq High-Speed",
                baseUrl = "https://api.groq.com/openai/v1",
                apiKeyEncrypted = CryptoManager.encrypt(""),
                isDefault = false,
                tokenLimit = 500000L,
                tokensUsed = 0L,
                modelsJson = "[\"llama-3.3-70b-versatile\", \"deepseek-r1-distill-llama-70b\", \"qwen-qwq-32b\"]",
                selectedModel = "llama-3.3-70b-versatile",
                temperature = 0.6f,
                maxTokens = 4096
            )
            val deepseek = ApiProviderEntity(
                name = "DeepSeek API",
                baseUrl = "https://api.deepseek.com/v1",
                apiKeyEncrypted = CryptoManager.encrypt(""),
                isDefault = false,
                tokenLimit = 500000L,
                tokensUsed = 0L,
                modelsJson = "[\"deepseek-chat\", \"deepseek-reasoner\"]",
                selectedModel = "deepseek-chat",
                temperature = 0.7f,
                maxTokens = 4096
            )
            val ollama = ApiProviderEntity(
                name = "Local Ollama / Custom",
                baseUrl = "http://10.0.2.2:11434/v1",
                apiKeyEncrypted = CryptoManager.encrypt("ollama"),
                isDefault = false,
                tokenLimit = 10000000L,
                tokensUsed = 0L,
                modelsJson = "[\"qwen2.5-coder\", \"llama3.3\", \"deepseek-r1\", \"mistral\"]",
                selectedModel = "qwen2.5-coder",
                temperature = 0.7f,
                maxTokens = 4096
            )

            val openAiId = apiProviderDao.insertProvider(defaultOpenAi)
            apiProviderDao.insertProvider(openRouter)
            apiProviderDao.insertProvider(groq)
            apiProviderDao.insertProvider(deepseek)
            apiProviderDao.insertProvider(ollama)
            apiProviderDao.setDefaultProvider(openAiId)
        }

        // 2. Seed Default Workspace Project if none exist
        val projects = workspaceDao.getAllProjectsFlow().firstOrNull() ?: emptyList()
        if (projects.isEmpty()) {
            val projectId = workspaceDao.insertProject(
                WorkspaceProjectEntity(
                    name = "Dragon Web App",
                    description = "Starter HTML/CSS/JS sandbox project",
                    isCurrent = true
                )
            )

            // Seed starter files
            val starterHtml = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Dragon Agent Demo</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <div class="card">
                        <div class="logo">🐉</div>
                        <h1>Dragon Agent</h1>
                        <p class="subtitle">Next-Generation Autonomous AI Software Engineer</p>
                        <div class="stats-badge">Live Sandbox Active</div>
                        <button id="actionBtn">Click for Magic</button>
                        <div id="output"></div>
                    </div>
                    <script src="app.js"></script>
                </body>
                </html>
            """.trimIndent()

            val starterCss = """
                * {
                    box-sizing: border-box;
                    margin: 0;
                    padding: 0;
                    font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
                }
                body {
                    background: radial-gradient(circle at top, #1a1e2d, #090b10);
                    color: #f1f5f9;
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    padding: 20px;
                }
                .card {
                    background: rgba(22, 26, 36, 0.85);
                    border: 1px solid rgba(255, 51, 75, 0.3);
                    backdrop-filter: blur(12px);
                    border-radius: 20px;
                    padding: 32px;
                    text-align: center;
                    max-width: 400px;
                    width: 100%;
                    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5), 0 0 20px rgba(255, 51, 75, 0.15);
                }
                .logo {
                    font-size: 54px;
                    margin-bottom: 12px;
                    animation: float 3s ease-in-out infinite;
                }
                h1 {
                    font-size: 26px;
                    font-weight: 700;
                    color: #ffffff;
                    margin-bottom: 6px;
                }
                .subtitle {
                    color: #94a3b8;
                    font-size: 14px;
                    margin-bottom: 20px;
                }
                .stats-badge {
                    display: inline-block;
                    background: rgba(0, 229, 255, 0.1);
                    color: #00e5ff;
                    border: 1px solid rgba(0, 229, 255, 0.3);
                    border-radius: 12px;
                    padding: 4px 12px;
                    font-size: 12px;
                    margin-bottom: 24px;
                }
                button {
                    background: linear-gradient(135deg, #ff334b, #ff7a30);
                    color: white;
                    border: none;
                    padding: 12px 24px;
                    font-size: 15px;
                    font-weight: 600;
                    border-radius: 12px;
                    cursor: pointer;
                    width: 100%;
                    transition: transform 0.2s, box-shadow 0.2s;
                }
                button:active {
                    transform: scale(0.97);
                }
                #output {
                    margin-top: 16px;
                    font-size: 14px;
                    color: #00e676;
                    min-height: 24px;
                }
                @keyframes float {
                    0%, 100% { transform: translateY(0); }
                    50% { transform: translateY(-8px); }
                }
            """.trimIndent()

            val starterJs = """
                document.getElementById('actionBtn').addEventListener('click', () => {
                    const output = document.getElementById('output');
                    const quotes = [
                        "⚡ Dragon Agent generated real-time code!",
                        "🛡️ All memory segments are encrypted locally.",
                        "🚀 Ready to build full-stack web applications.",
                        "✨ User changes are automatically synchronized."
                    ];
                    const random = quotes[Math.floor(Math.random() * quotes.length)];
                    output.innerText = random;
                });
            """.trimIndent()

            insertOrUpdateFile(projectId, "index.html", starterHtml, "html")
            insertOrUpdateFile(projectId, "style.css", starterCss, "css")
            insertOrUpdateFile(projectId, "app.js", starterJs, "javascript")
        }

        // 3. Seed starter Segmented AI Memory
        val memories = memoryDao.getActiveMemoriesList()
        if (memories.isEmpty()) {
            memoryDao.insertMemory(
                MemoryEntity(
                    title = "System Identity & Security",
                    category = "ARCHITECTURE",
                    contentEncrypted = CryptoManager.encrypt("The application is Dragon Agent, an autonomous high-security AI coding agent with segmented encrypted on-device memory and multi-tool workspace manipulation."),
                    importance = 5
                )
            )
            memoryDao.insertMemory(
                MemoryEntity(
                    title = "Code Quality & Modular Architecture",
                    category = "TECH_STACK",
                    contentEncrypted = CryptoManager.encrypt("Write clean, modern, modular code. When providing code blocks, ensure filenames and clear comments are included."),
                    importance = 4
                )
            )
            memoryDao.insertMemory(
                MemoryEntity(
                    title = "Developer Preferences",
                    category = "USER_PREFS",
                    contentEncrypted = CryptoManager.encrypt("Prefers concise responses, robust error handling, and clean modern aesthetics."),
                    importance = 3
                )
            )
        }
    }

    // -------------------------------------------------------------
    // TERMS & SETTINGS
    // -------------------------------------------------------------
    suspend fun hasAcceptedTerms(): Boolean = withContext(Dispatchers.IO) {
        val value = settingsDao.getSetting("terms_accepted")
        value == "true"
    }

    suspend fun setTermsAccepted(accepted: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(AppSettingsEntity("terms_accepted", accepted.toString()))
    }

    suspend fun isDarkMode(): Boolean = withContext(Dispatchers.IO) {
        val value = settingsDao.getSetting("app_theme_dark")
        value == "true" // Defaults to false (Light Mode)
    }

    suspend fun setDarkMode(dark: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(AppSettingsEntity("app_theme_dark", dark.toString()))
    }

    suspend fun getLanguage(): String = withContext(Dispatchers.IO) {
        val value = settingsDao.getSetting("app_language")
        value ?: "en" // Defaults to English
    }

    suspend fun setLanguage(lang: String) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(AppSettingsEntity("app_language", lang))
    }

    // -------------------------------------------------------------
    // API PROVIDER REPOSITORY
    // -------------------------------------------------------------
    fun getProvidersFlow(): Flow<List<ApiProviderEntity>> = apiProviderDao.getAllProvidersFlow()

    suspend fun getProvider(id: Long): ApiProviderEntity? = withContext(Dispatchers.IO) {
        apiProviderDao.getProviderById(id)
    }

    suspend fun getDefaultProvider(): ApiProviderEntity? = withContext(Dispatchers.IO) {
        apiProviderDao.getDefaultProvider() ?: apiProviderDao.getAllProvidersList().firstOrNull()
    }

    suspend fun saveProvider(provider: ApiProviderEntity, rawApiKey: String): Long = withContext(Dispatchers.IO) {
        val encryptedKey = if (rawApiKey.isNotEmpty()) {
            CryptoManager.encrypt(rawApiKey)
        } else {
            provider.apiKeyEncrypted
        }
        val entity = provider.copy(apiKeyEncrypted = encryptedKey)
        if (entity.id == 0L) {
            val id = apiProviderDao.insertProvider(entity)
            if (entity.isDefault) {
                apiProviderDao.setDefaultProvider(id)
            }
            id
        } else {
            apiProviderDao.updateProvider(entity)
            if (entity.isDefault) {
                apiProviderDao.setDefaultProvider(entity.id)
            }
            entity.id
        }
    }

    suspend fun setDefaultProvider(id: Long) = withContext(Dispatchers.IO) {
        apiProviderDao.setDefaultProvider(id)
    }

    suspend fun updateProviderSelectedModel(providerId: Long, model: String) = withContext(Dispatchers.IO) {
        apiProviderDao.updateSelectedModel(providerId, model)
    }

    suspend fun deleteProvider(id: Long) = withContext(Dispatchers.IO) {
        apiProviderDao.deleteProvider(id)
    }

    fun getDecryptedApiKey(provider: ApiProviderEntity): String {
        return CryptoManager.decrypt(provider.apiKeyEncrypted)
    }

    // -------------------------------------------------------------
    // MEMORY MANAGEMENT (SEGMENTED ENCRYPTED RECALL)
    // -------------------------------------------------------------
    fun getMemoriesFlow(): Flow<List<MemoryEntity>> = memoryDao.getAllMemoriesFlow()

    suspend fun getActiveMemories(): List<MemoryEntity> = withContext(Dispatchers.IO) {
        memoryDao.getActiveMemoriesList()
    }

    suspend fun getMemoriesByCategory(category: String): List<MemoryEntity> = withContext(Dispatchers.IO) {
        memoryDao.getMemoriesByCategory(category)
    }

    suspend fun saveMemory(
        id: Long = 0,
        title: String,
        category: String,
        content: String,
        importance: Int = 3
    ): Long = withContext(Dispatchers.IO) {
        val encrypted = CryptoManager.encrypt(content)
        val entity = MemoryEntity(
            id = id,
            title = title,
            category = category.uppercase().trim(),
            contentEncrypted = encrypted,
            importance = importance,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        if (id == 0L) {
            memoryDao.insertMemory(entity)
        } else {
            memoryDao.updateMemory(entity)
            id
        }
    }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemory(id)
    }

    suspend fun clearAllMemories() = withContext(Dispatchers.IO) {
        memoryDao.clearAllMemories()
    }

    fun getDecryptedMemoryContent(memory: MemoryEntity): String {
        return CryptoManager.decrypt(memory.contentEncrypted)
    }

    // -------------------------------------------------------------
    // CHAT SESSIONS & MESSAGES
    // -------------------------------------------------------------
    fun getChatSessionsFlow(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessionsFlow()

    fun getMessagesFlow(sessionId: Long): Flow<List<ChatMessageEntity>> = chatDao.getMessagesFlow(sessionId)

    suspend fun createChatSession(title: String, mode: String = "CHAT", providerId: Long = 0, modelId: String = ""): Long = withContext(Dispatchers.IO) {
        chatDao.insertSession(
            ChatSessionEntity(
                title = title,
                mode = mode,
                providerId = providerId,
                modelId = modelId
            )
        )
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        chatDao.clearMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun saveMessage(
        sessionId: Long,
        role: String,
        content: String,
        toolCallsJson: String = "",
        isStreaming: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        val encrypted = CryptoManager.encrypt(content)
        val id = chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                role = role,
                contentEncrypted = encrypted,
                toolCallsJson = toolCallsJson,
                isStreaming = isStreaming
            )
        )
        chatDao.touchSession(sessionId, content.take(30).ifEmpty { "Chat Session" })
        id
    }

    suspend fun updateStreamingMessage(messageId: Long, sessionId: Long, role: String, content: String, isStreaming: Boolean) = withContext(Dispatchers.IO) {
        val encrypted = CryptoManager.encrypt(content)
        chatDao.updateMessage(
            ChatMessageEntity(
                id = messageId,
                sessionId = sessionId,
                role = role,
                contentEncrypted = encrypted,
                isStreaming = isStreaming
            )
        )
    }

    fun getDecryptedMessageContent(message: ChatMessageEntity): String {
        return CryptoManager.decrypt(message.contentEncrypted)
    }

    // -------------------------------------------------------------
    // WORKSPACE & PROJECT FILE SYSTEM
    // -------------------------------------------------------------
    fun getProjectsFlow(): Flow<List<WorkspaceProjectEntity>> = workspaceDao.getAllProjectsFlow()

    fun getProjectFilesFlow(projectId: Long): Flow<List<ProjectFileEntity>> = workspaceDao.getProjectFilesFlow(projectId)

    suspend fun getCurrentProject(): WorkspaceProjectEntity? = withContext(Dispatchers.IO) {
        workspaceDao.getCurrentProject() ?: workspaceDao.getAllProjectsFlow().firstOrNull()?.firstOrNull()
    }

    suspend fun createProject(name: String, description: String = ""): Long = withContext(Dispatchers.IO) {
        val id = workspaceDao.insertProject(
            WorkspaceProjectEntity(
                name = name,
                description = description,
                isCurrent = true
            )
        )
        workspaceDao.setCurrentProject(id)
        id
    }

    suspend fun setCurrentProject(projectId: Long) = withContext(Dispatchers.IO) {
        workspaceDao.setCurrentProject(projectId)
    }

    suspend fun deleteProject(projectId: Long) = withContext(Dispatchers.IO) {
        val files = workspaceDao.getProjectFilesList(projectId)
        for (f in files) {
            workspaceDao.deleteFile(f.id)
        }
        workspaceDao.deleteProject(projectId)
    }

    suspend fun insertOrUpdateFile(
        projectId: Long,
        path: String,
        content: String,
        language: String = detectLanguage(path),
        isManualUserEdit: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        val cleanPath = path.trim().trimStart('/')
        val existing = workspaceDao.getFileByPath(projectId, cleanPath)
        val encrypted = CryptoManager.encrypt(content)
        val name = cleanPath.substringAfterLast('/')
        val parent = if (cleanPath.contains('/')) cleanPath.substringBeforeLast('/') else ""

        if (existing != null) {
            val newVersion = existing.version + 1
            // Snapshot previous version
            workspaceDao.insertVersion(
                FileVersionEntity(
                    fileId = existing.id,
                    versionNumber = existing.version,
                    contentEncrypted = existing.contentEncrypted,
                    changeDescription = if (isManualUserEdit) "User manual edit" else "Agent AI modification"
                )
            )

            val updated = existing.copy(
                contentEncrypted = encrypted,
                language = language,
                lastModified = System.currentTimeMillis(),
                version = newVersion,
                manualUserModified = isManualUserEdit
            )
            workspaceDao.updateFile(updated)
            existing.id
        } else {
            // Also ensure parent folders exist
            ensureParentFolders(projectId, parent)

            val file = ProjectFileEntity(
                projectId = projectId,
                path = cleanPath,
                name = name,
                isDirectory = false,
                parentPath = parent,
                contentEncrypted = encrypted,
                language = language,
                version = 1,
                manualUserModified = isManualUserEdit
            )
            workspaceDao.insertFile(file)
        }
    }

    suspend fun createFolder(projectId: Long, folderPath: String): Long = withContext(Dispatchers.IO) {
        val cleanPath = folderPath.trim().trim('/')
        if (cleanPath.isEmpty()) return@withContext 0L
        val existing = workspaceDao.getFileByPath(projectId, cleanPath)
        if (existing != null) return@withContext existing.id

        val name = cleanPath.substringAfterLast('/')
        val parent = if (cleanPath.contains('/')) cleanPath.substringBeforeLast('/') else ""
        ensureParentFolders(projectId, parent)

        val folder = ProjectFileEntity(
            projectId = projectId,
            path = cleanPath,
            name = name,
            isDirectory = true,
            parentPath = parent,
            contentEncrypted = "",
            language = "folder"
        )
        workspaceDao.insertFile(folder)
    }

    private suspend fun ensureParentFolders(projectId: Long, parentPath: String) {
        if (parentPath.isEmpty()) return
        val parts = parentPath.split('/')
        var cur = ""
        for (part in parts) {
            cur = if (cur.isEmpty()) part else "$cur/$part"
            val existing = workspaceDao.getFileByPath(projectId, cur)
            if (existing == null) {
                val p = if (cur.contains('/')) cur.substringBeforeLast('/') else ""
                workspaceDao.insertFile(
                    ProjectFileEntity(
                        projectId = projectId,
                        path = cur,
                        name = part,
                        isDirectory = true,
                        parentPath = p,
                        contentEncrypted = "",
                        language = "folder"
                    )
                )
            }
        }
    }

    suspend fun deleteFileOrFolder(projectId: Long, path: String) = withContext(Dispatchers.IO) {
        val cleanPath = path.trim().trim('/')
        workspaceDao.deleteFileOrFolder(projectId, cleanPath, "$cleanPath/%")
    }

    suspend fun moveFileOrFolder(projectId: Long, sourcePath: String, destinationPath: String) = withContext(Dispatchers.IO) {
        val src = sourcePath.trim().trim('/')
        val dst = destinationPath.trim().trim('/')
        val file = workspaceDao.getFileByPath(projectId, src) ?: return@withContext

        val dstName = dst.substringAfterLast('/')
        val dstParent = if (dst.contains('/')) dst.substringBeforeLast('/') else ""
        ensureParentFolders(projectId, dstParent)

        val updated = file.copy(
            path = dst,
            name = dstName,
            parentPath = dstParent,
            lastModified = System.currentTimeMillis()
        )
        workspaceDao.updateFile(updated)

        // If it's a folder, move all children
        if (file.isDirectory) {
            val allFiles = workspaceDao.getProjectFilesList(projectId)
            for (f in allFiles) {
                if (f.path.startsWith("$src/")) {
                    val rel = f.path.removePrefix("$src/")
                    val newChildPath = "$dst/$rel"
                    val newChildParent = if (newChildPath.contains('/')) newChildPath.substringBeforeLast('/') else ""
                    workspaceDao.updateFile(
                        f.copy(
                            path = newChildPath,
                            name = newChildPath.substringAfterLast('/'),
                            parentPath = newChildParent,
                            lastModified = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    suspend fun replaceCodeInFile(projectId: Long, path: String, targetContent: String, replacementContent: String): Boolean = withContext(Dispatchers.IO) {
        val file = workspaceDao.getFileByPath(projectId, path.trim().trim('/')) ?: return@withContext false
        val currentCode = CryptoManager.decrypt(file.contentEncrypted)

        if (currentCode.contains(targetContent)) {
            val newCode = currentCode.replace(targetContent, replacementContent)
            insertOrUpdateFile(projectId, file.path, newCode, file.language, isManualUserEdit = false)
            true
        } else {
            false
        }
    }

    fun getDecryptedFileContent(file: ProjectFileEntity): String {
        return CryptoManager.decrypt(file.contentEncrypted)
    }

    // -------------------------------------------------------------
    // SMART SEGMENTED CONTEXT BUILDER (MEMORY + WORKSPACE SYNC)
    // -------------------------------------------------------------
    suspend fun buildAgentContextPrompt(projectId: Long, promptQuery: String = ""): Pair<String, List<String>> = withContext(Dispatchers.IO) {
        val activeMemories = memoryDao.getActiveMemoriesList()
        val allFiles = workspaceDao.getProjectFilesList(projectId)

        val memoryBuilder = StringBuilder()
        val recalledMemoryTitles = mutableListOf<String>()

        // Segment memories into categories to load relevant parts and reduce token usage
        if (activeMemories.isNotEmpty()) {
            val categorized = activeMemories.groupBy { it.category.uppercase() }
            memoryBuilder.append("\n=== SEGMENTED LONG-TERM ENCRYPTED MEMORY ===\n")

            for ((category, mems) in categorized) {
                memoryBuilder.append("\n[SECTION: $category]\n")
                for (m in mems) {
                    val decrypted = CryptoManager.decrypt(m.contentEncrypted)
                    memoryBuilder.append("• ${m.title} (Importance: ${m.importance}/5): $decrypted\n")
                    recalledMemoryTitles.add(m.title)
                    memoryDao.updateRecalledTime(m.id)
                }
            }
        }

        // Workspace Tree
        val workspaceBuilder = StringBuilder()
        workspaceBuilder.append("\n=== CURRENT PROJECT WORKSPACE FILES ===\n")
        val userModifiedNotices = StringBuilder()

        for (f in allFiles) {
            if (f.isDirectory) {
                workspaceBuilder.append("📁 ${f.path}/\n")
            } else {
                workspaceBuilder.append("📄 ${f.path} (v${f.version})\n")
                if (f.manualUserModified) {
                    val decrypted = CryptoManager.decrypt(f.contentEncrypted)
                    userModifiedNotices.append("\n⚠️ [USER MANUAL EDIT DETECTED]: The user directly edited `${f.path}`:\n```${f.language}\n$decrypted\n```\n")
                    workspaceDao.updateFile(f.copy(manualUserModified = false))
                }
            }
        }

        // Action Protocol Instructions
        val actionProtocol = """
=== DRAGON AGENT PROTOCOL ===
You are Dragon Agent, an elite autonomous software engineering AI. You have full tool control over the project workspace and memory.
To create, edit, move, or delete files, use the following exact XML action tags in your response:

1. Create / Write Complete File:
<dragon_action type="create_file" path="path/to/file.ext">
... full code content here ...
</dragon_action>

2. Create Folder:
<dragon_action type="create_folder" path="path/to/folder" />

3. Replace Targeted Code in Existing File:
<dragon_action type="replace_code" path="path/to/file.ext">
<<<<<<< SEARCH
[exact lines to replace]
=======
[new replacement lines]
>>>>>>>
</dragon_action>

4. Move / Rename File:
<dragon_action type="move_file" source="old/path.ext" destination="new/path.ext" />

5. Delete File:
<dragon_action type="delete_file" path="path/to/file.ext" />

When modifying code, prioritize writing production-ready, beautiful, bug-free implementations.
All changes are applied immediately to encrypted local storage and live preview.
        """.trimIndent()

        val fullSystemPrompt = buildString {
            append("You are Dragon Agent, an autonomous software development and code creation system.\n")
            append(actionProtocol)
            append("\n")
            append(memoryBuilder)
            append("\n")
            append(workspaceBuilder)
            if (userModifiedNotices.isNotEmpty()) {
                append("\n")
                append(userModifiedNotices)
            }
        }

        Pair(fullSystemPrompt, recalledMemoryTitles)
    }

    suspend fun buildChatAssistantPrompt(userPrompt: String = ""): String = withContext(Dispatchers.IO) {
        val activeMemories = memoryDao.getActiveMemoriesList()
        val memBuilder = StringBuilder()

        if (activeMemories.isNotEmpty()) {
            val highPriority = activeMemories.filter { it.importance >= 3 }
            if (highPriority.isNotEmpty()) {
                memBuilder.append("\n=== ACTIVE MEMORY CONTEXT ===\n")
                for (m in highPriority) {
                    val decrypted = CryptoManager.decrypt(m.contentEncrypted)
                    memBuilder.append("• [${m.category}] ${m.title}: $decrypted\n")
                }
            }
        }

        buildString {
            append("You are Dragon Agent AI, an expert programming consultant, software architect, and conversational companion.\n")
            append("Provide insightful, concise, and structured answers. Format code snippets with clear language tags and filenames when relevant.\n")
            if (memBuilder.isNotEmpty()) {
                append(memBuilder)
            }
        }
    }

    // -------------------------------------------------------------
    // EXPORT / BACKUP (ZIP & JSON)
    // -------------------------------------------------------------
    suspend fun exportProjectZip(projectId: Long): ByteArray = withContext(Dispatchers.IO) {
        val files = workspaceDao.getProjectFilesList(projectId)
        val byteOut = ByteArrayOutputStream()
        val zipOut = ZipOutputStream(byteOut)

        for (file in files) {
            if (file.isDirectory) {
                val entry = ZipEntry("${file.path}/")
                zipOut.putNextEntry(entry)
                zipOut.closeEntry()
            } else {
                val content = CryptoManager.decrypt(file.contentEncrypted)
                val entry = ZipEntry(file.path)
                zipOut.putNextEntry(entry)
                zipOut.write(content.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
            }
        }
        zipOut.finish()
        zipOut.close()
        byteOut.toByteArray()
    }

    suspend fun exportMemoriesJson(): String = withContext(Dispatchers.IO) {
        val list = memoryDao.getActiveMemoriesList()
        val jsonArray = JSONArray()
        for (m in list) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("category", m.category)
                put("content", CryptoManager.decrypt(m.contentEncrypted))
                put("importance", m.importance)
                put("createdAt", m.createdAt)
            }
            jsonArray.put(obj)
        }
        jsonArray.toString(2)
    }

    suspend fun importMemoriesJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "Imported Memory")
                val category = obj.optString("category", "GENERAL_FACTS")
                val content = obj.optString("content", "")
                val importance = obj.optInt("importance", 3)
                if (content.isNotEmpty()) {
                    saveMemory(0, title, category, content, importance)
                    count++
                }
            }
        } catch (e: Exception) {
            // handle error
        }
        count
    }

    // -------------------------------------------------------------
    // LIVE HTML/CSS/JS COMPILER FOR SANDBOX
    // -------------------------------------------------------------
    suspend fun compileSandboxHtml(projectId: Long): String = withContext(Dispatchers.IO) {
        val files = workspaceDao.getProjectFilesList(projectId)
        val htmlFile = files.firstOrNull { it.name.endsWith(".html") }
        val cssFiles = files.filter { it.name.endsWith(".css") }
        val jsFiles = files.filter { it.name.endsWith(".js") }

        var html = if (htmlFile != null) {
            CryptoManager.decrypt(htmlFile.contentEncrypted)
        } else {
            """
                <!DOCTYPE html>
                <html>
                <head><style>body{background:#12151e;color:#fff;font-family:sans-serif;padding:20px;text-align:center;}</style></head>
                <body><h3>🐉 Dragon Agent Sandbox</h3><p>No HTML file found. Create index.html to preview.</p></body>
                </html>
            """.trimIndent()
        }

        // Inline CSS
        val combinedCss = StringBuilder()
        for (css in cssFiles) {
            combinedCss.append("\n/* ${css.path} */\n")
            combinedCss.append(CryptoManager.decrypt(css.contentEncrypted))
        }

        // Inline JS
        val combinedJs = StringBuilder()
        for (js in jsFiles) {
            combinedJs.append("\n// ${js.path}\n")
            combinedJs.append(CryptoManager.decrypt(js.contentEncrypted))
        }

        // Inject CSS before </head> or at start
        val injectedStyle = "<style>$combinedCss</style>"
        val injectedScript = "<script>$combinedJs</script>"

        if (html.contains("</head>", ignoreCase = true)) {
            html = html.replace("</head>", "$injectedStyle\n</head>")
        } else {
            html = "$injectedStyle\n$html"
        }

        if (html.contains("</body>", ignoreCase = true)) {
            html = html.replace("</body>", "$injectedScript\n</body>")
        } else {
            html = "$html\n$injectedScript"
        }

        html
    }

    private fun detectLanguage(path: String): String {
        return when (path.substringAfterLast('.', "").lowercase()) {
            "html", "htm" -> "html"
            "css" -> "css"
            "js", "javascript" -> "javascript"
            "ts", "typescript" -> "typescript"
            "json" -> "json"
            "kt", "kts" -> "kotlin"
            "py" -> "python"
            "java" -> "java"
            "md", "markdown" -> "markdown"
            "xml" -> "xml"
            "sql" -> "sql"
            "sh", "bash" -> "shell"
            else -> "plaintext"
        }
    }
}
