package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.agent.*
import com.example.data.local.entities.*
import com.example.data.repository.DragonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class NavigationTab {
    AGENT_WORKSPACE,
    CHAT_ASSISTANT,
    MEMORY_BANK,
    API_PROVIDERS,
    SETTINGS_ABOUT
}

data class UiMessage(
    val id: Long,
    val role: String,
    val content: String,
    val timestamp: Long,
    val isStreaming: Boolean = false,
    val extractedBlocks: List<ExtractedCodeBlock> = emptyList(),
    val toolActions: List<DragonToolAction> = emptyList()
)

data class DragonUiState(
    val currentTab: NavigationTab = NavigationTab.AGENT_WORKSPACE,
    val hasAcceptedTerms: Boolean = false,
    val isInitializing: Boolean = true,
    val isDarkMode: Boolean = false, // Light mode by default
    val language: String = "en",     // English by default, Persian selectable
    
    // Providers & Models
    val providers: List<ApiProviderEntity> = emptyList(),
    val currentProvider: ApiProviderEntity? = null,
    val selectedModel: String = "",
    
    // Workspace
    val projects: List<WorkspaceProjectEntity> = emptyList(),
    val currentProject: WorkspaceProjectEntity? = null,
    val projectFiles: List<ProjectFileEntity> = emptyList(),
    val selectedFile: ProjectFileEntity? = null,
    val selectedFileContent: String = "",
    val isEditorModified: Boolean = false,
    val showSandboxPreview: Boolean = false,
    val compiledSandboxHtml: String = "",
    
    // Agent & Chat
    val agentSession: ChatSessionEntity? = null,
    val agentMessages: List<UiMessage> = emptyList(),
    val chatSession: ChatSessionEntity? = null,
    val chatMessages: List<UiMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingChunk: String = "",
    val recalledMemories: List<String> = emptyList(),
    val lastExecutedToolsCount: Int = 0,
    
    // Memory
    val memories: List<MemoryEntity> = emptyList(),
    
    // UI Feedback
    val toastMessage: String? = null
)

class DragonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DragonRepository(application)
    
    private val _uiState = MutableStateFlow(DragonUiState())
    val uiState: StateFlow<DragonUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            val termsAccepted = repository.hasAcceptedTerms()
            val isDark = repository.isDarkMode()
            val lang = repository.getLanguage()
            _uiState.update { 
                it.copy(
                    hasAcceptedTerms = termsAccepted,
                    isDarkMode = isDark,
                    language = lang,
                    isInitializing = false
                ) 
            }
            
            observeProviders()
            observeWorkspace()
            observeMemories()
            initializeSessions()
        }
    }

    fun setDarkMode(dark: Boolean) {
        _uiState.update { it.copy(isDarkMode = dark) }
        viewModelScope.launch {
            repository.setDarkMode(dark)
        }
    }

    fun toggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        setDarkMode(newMode)
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(language = lang) }
        viewModelScope.launch {
            repository.setLanguage(lang)
        }
    }

    private fun observeProviders() {
        viewModelScope.launch {
            repository.getProvidersFlow().collect { list ->
                val current = list.firstOrNull { it.isDefault } ?: list.firstOrNull()
                _uiState.update { state ->
                    state.copy(
                        providers = list,
                        currentProvider = current,
                        selectedModel = current?.selectedModel ?: ""
                    )
                }
            }
        }
    }

    private fun observeWorkspace() {
        viewModelScope.launch {
            repository.getProjectsFlow().collect { projects ->
                val current = projects.firstOrNull { it.isCurrent } ?: projects.firstOrNull()
                _uiState.update { it.copy(projects = projects, currentProject = current) }
                if (current != null) {
                    observeFiles(current.id)
                }
            }
        }
    }

    private fun observeFiles(projectId: Long) {
        viewModelScope.launch {
            repository.getProjectFilesFlow(projectId).collect { files ->
                _uiState.update { state ->
                    val selected = state.selectedFile?.let { sf -> files.firstOrNull { it.id == sf.id } }
                        ?: files.firstOrNull { !it.isDirectory }
                    val content = if (selected != null && !selected.isDirectory) {
                        repository.getDecryptedFileContent(selected)
                    } else ""
                    state.copy(
                        projectFiles = files,
                        selectedFile = selected,
                        selectedFileContent = content,
                        isEditorModified = false
                    )
                }
            }
        }
    }

    private fun observeMemories() {
        viewModelScope.launch {
            repository.getMemoriesFlow().collect { list ->
                _uiState.update { it.copy(memories = list) }
            }
        }
    }

    private fun initializeSessions() {
        viewModelScope.launch {
            // Find or create Agent session
            val agentSessionId = repository.createChatSession("Workspace Agent", "CODER_AGENT")
            val chatSessionId = repository.createChatSession("AI Chat Assistant", "CHAT")

            observeAgentMessages(agentSessionId)
            observeChatMessages(chatSessionId)
        }
    }

    private fun observeAgentMessages(sessionId: Long) {
        viewModelScope.launch {
            repository.getMessagesFlow(sessionId).collect { list ->
                val uiMessages = list.map { msg ->
                    val decrypted = repository.getDecryptedMessageContent(msg)
                    UiMessage(
                        id = msg.id,
                        role = msg.role,
                        content = decrypted,
                        timestamp = msg.timestamp,
                        isStreaming = msg.isStreaming,
                        extractedBlocks = AgentActionParser.extractCodeBlocks(decrypted),
                        toolActions = AgentActionParser.parseWorkspaceActions(decrypted)
                    )
                }
                _uiState.update { it.copy(agentMessages = uiMessages) }
            }
        }
    }

    private fun observeChatMessages(sessionId: Long) {
        viewModelScope.launch {
            repository.getMessagesFlow(sessionId).collect { list ->
                val uiMessages = list.map { msg ->
                    val decrypted = repository.getDecryptedMessageContent(msg)
                    UiMessage(
                        id = msg.id,
                        role = msg.role,
                        content = decrypted,
                        timestamp = msg.timestamp,
                        isStreaming = msg.isStreaming,
                        extractedBlocks = AgentActionParser.extractCodeBlocks(decrypted)
                    )
                }
                _uiState.update { it.copy(chatMessages = uiMessages) }
            }
        }
    }

    // -------------------------------------------------------------
    // NAVIGATION & TERMS
    // -------------------------------------------------------------
    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun acceptTerms() {
        viewModelScope.launch {
            repository.setTermsAccepted(true)
            _uiState.update { it.copy(hasAcceptedTerms = true) }
        }
    }

    // -------------------------------------------------------------
    // PROVIDER & MODEL SELECTION
    // -------------------------------------------------------------
    fun selectProvider(provider: ApiProviderEntity) {
        viewModelScope.launch {
            repository.setDefaultProvider(provider.id)
            _uiState.update {
                it.copy(
                    currentProvider = provider,
                    selectedModel = provider.selectedModel
                )
            }
        }
    }

    fun selectModel(model: String) {
        val provider = _uiState.value.currentProvider ?: return
        viewModelScope.launch {
            repository.updateProviderSelectedModel(provider.id, model)
            _uiState.update { it.copy(selectedModel = model) }
        }
    }

    fun saveProvider(provider: ApiProviderEntity, rawApiKey: String) {
        viewModelScope.launch {
            repository.saveProvider(provider, rawApiKey)
            showToast("API Provider '${provider.name}' saved securely.")
        }
    }

    fun deleteProvider(id: Long) {
        viewModelScope.launch {
            repository.deleteProvider(id)
            showToast("Provider removed.")
        }
    }

    // -------------------------------------------------------------
    // AGENT CODING & CHAT EXECUTION
    // -------------------------------------------------------------
    fun sendAgentPrompt(promptText: String) {
        if (promptText.isBlank()) return
        val provider = _uiState.value.currentProvider
        if (provider == null) {
            showToast("Please configure an API provider in API tab first.")
            return
        }

        val apiKey = repository.getDecryptedApiKey(provider)
        val project = _uiState.value.currentProject ?: return
        val currentMessages = _uiState.value.agentMessages

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }

            // 1. Build context with memory and project files
            val (systemPrompt, recalledTitles) = repository.buildAgentContextPrompt(project.id)
            _uiState.update { it.copy(recalledMemories = recalledTitles) }

            // 2. Save user message to DB
            val userMsgId = repository.saveMessage(
                sessionId = 1L, // Agent session
                role = "user",
                content = promptText
            )

            // 3. Create placeholder assistant streaming message
            val assistantMsgId = repository.saveMessage(
                sessionId = 1L,
                role = "assistant",
                content = "",
                isStreaming = true
            )

            val apiMessages = mutableListOf<ApiChatMessage>()
            apiMessages.add(ApiChatMessage("system", systemPrompt))
            for (m in currentMessages.takeLast(6)) {
                apiMessages.add(ApiChatMessage(m.role, m.content))
            }
            apiMessages.add(ApiChatMessage("user", promptText))

            val streamContentBuilder = StringBuilder()
            val aiClient = AiStreamClient()

            var executedActionsCount = 0

            aiClient.streamChat(
                baseUrl = provider.baseUrl,
                apiKey = apiKey,
                model = _uiState.value.selectedModel.ifEmpty { provider.selectedModel },
                messages = apiMessages,
                temperature = provider.temperature,
                maxTokens = provider.maxTokens
            ).collect { event ->
                when (event) {
                    is StreamEvent.Chunk -> {
                        streamContentBuilder.append(event.text)
                        val currentText = streamContentBuilder.toString()
                        // Immediate persistent update to DB so changes are never lost!
                        repository.updateStreamingMessage(assistantMsgId, 1L, "assistant", currentText, isStreaming = true)
                    }
                    is StreamEvent.Complete -> {
                        val fullResponse = event.fullText.ifEmpty { streamContentBuilder.toString() }
                        repository.updateStreamingMessage(assistantMsgId, 1L, "assistant", fullResponse, isStreaming = false)

                        // Parse and execute actions
                        val actions = AgentActionParser.parseWorkspaceActions(fullResponse)
                        executedActionsCount = applyAgentActions(project.id, actions)

                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                lastExecutedToolsCount = executedActionsCount
                            )
                        }
                        if (executedActionsCount > 0) {
                            showToast("Agent executed $executedActionsCount workspace operations & saved to encrypted storage.")
                        }
                    }
                    is StreamEvent.Error -> {
                        val errMsg = "⚠️ Error: ${event.message}"
                        repository.updateStreamingMessage(assistantMsgId, 1L, "assistant", errMsg, isStreaming = false)
                        _uiState.update { it.copy(isGenerating = false) }
                        showToast(event.message)
                    }
                }
            }
        }
    }

    fun sendChatMessage(promptText: String) {
        if (promptText.isBlank()) return
        val provider = _uiState.value.currentProvider
        if (provider == null) {
            showToast("Please configure an API provider in API tab first.")
            return
        }

        val apiKey = repository.getDecryptedApiKey(provider)
        val currentMessages = _uiState.value.chatMessages

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }

            // Recall active memories & build specialized chat assistant prompt
            val memPrompt = repository.buildChatAssistantPrompt(promptText)

            repository.saveMessage(2L, "user", promptText)
            val assistantMsgId = repository.saveMessage(2L, "assistant", "", isStreaming = true)

            val apiMessages = mutableListOf<ApiChatMessage>()
            apiMessages.add(ApiChatMessage("system", memPrompt))
            for (m in currentMessages.takeLast(6)) {
                apiMessages.add(ApiChatMessage(m.role, m.content))
            }
            apiMessages.add(ApiChatMessage("user", promptText))

            val streamBuilder = StringBuilder()
            val aiClient = AiStreamClient()

            aiClient.streamChat(
                baseUrl = provider.baseUrl,
                apiKey = apiKey,
                model = _uiState.value.selectedModel.ifEmpty { provider.selectedModel },
                messages = apiMessages,
                temperature = provider.temperature,
                maxTokens = provider.maxTokens
            ).collect { event ->
                when (event) {
                    is StreamEvent.Chunk -> {
                        streamBuilder.append(event.text)
                        repository.updateStreamingMessage(assistantMsgId, 2L, "assistant", streamBuilder.toString(), isStreaming = true)
                    }
                    is StreamEvent.Complete -> {
                        repository.updateStreamingMessage(assistantMsgId, 2L, "assistant", event.fullText.ifEmpty { streamBuilder.toString() }, isStreaming = false)
                        _uiState.update { it.copy(isGenerating = false) }
                    }
                    is StreamEvent.Error -> {
                        repository.updateStreamingMessage(assistantMsgId, 2L, "assistant", "⚠️ Error: ${event.message}", isStreaming = false)
                        _uiState.update { it.copy(isGenerating = false) }
                        showToast(event.message)
                    }
                }
            }
        }
    }

    private suspend fun applyAgentActions(projectId: Long, actions: List<DragonToolAction>): Int {
        var count = 0
        for (action in actions) {
            when (action.actionType) {
                DragonToolAction.ActionType.CREATE_FILE -> {
                    if (action.path.isNotBlank()) {
                        repository.insertOrUpdateFile(projectId, action.path, action.content, isManualUserEdit = false)
                        count++
                    }
                }
                DragonToolAction.ActionType.CREATE_FOLDER -> {
                    if (action.path.isNotBlank()) {
                        repository.createFolder(projectId, action.path)
                        count++
                    }
                }
                DragonToolAction.ActionType.DELETE_FILE -> {
                    if (action.path.isNotBlank()) {
                        repository.deleteFileOrFolder(projectId, action.path)
                        count++
                    }
                }
                DragonToolAction.ActionType.MOVE_FILE -> {
                    if (action.sourcePath.isNotBlank() && action.destinationPath.isNotBlank()) {
                        repository.moveFileOrFolder(projectId, action.sourcePath, action.destinationPath)
                        count++
                    }
                }
                DragonToolAction.ActionType.REPLACE_CODE -> {
                    if (action.path.isNotBlank() && action.targetContent.isNotBlank()) {
                        val replaced = repository.replaceCodeInFile(projectId, action.path, action.targetContent, action.replacementContent)
                        if (replaced) count++
                    }
                }
                DragonToolAction.ActionType.REWRITE_FILE -> {
                    if (action.path.isNotBlank()) {
                        repository.insertOrUpdateFile(projectId, action.path, action.content, isManualUserEdit = false)
                        count++
                    }
                }
            }
        }
        return count
    }

    // -------------------------------------------------------------
    // FILE EXPLORER & EDITOR
    // -------------------------------------------------------------
    fun selectFile(file: ProjectFileEntity) {
        if (file.isDirectory) return
        val content = repository.getDecryptedFileContent(file)
        _uiState.update {
            it.copy(
                selectedFile = file,
                selectedFileContent = content,
                isEditorModified = false
            )
        }
    }

    fun updateEditorContent(newContent: String) {
        _uiState.update {
            it.copy(
                selectedFileContent = newContent,
                isEditorModified = true
            )
        }
    }

    fun saveCurrentFile() {
        val file = _uiState.value.selectedFile ?: return
        val content = _uiState.value.selectedFileContent
        val project = _uiState.value.currentProject ?: return

        viewModelScope.launch {
            repository.insertOrUpdateFile(
                projectId = project.id,
                path = file.path,
                content = content,
                language = file.language,
                isManualUserEdit = true
            )
            _uiState.update { it.copy(isEditorModified = false) }
            showToast("Saved '${file.name}' to encrypted storage. Changes synced with AI context.")
        }
    }

    fun createNewFile(path: String, content: String = "") {
        val project = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.insertOrUpdateFile(project.id, path, content, isManualUserEdit = true)
            showToast("File '$path' created.")
        }
    }

    fun createNewFolder(path: String) {
        val project = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.createFolder(project.id, path)
            showToast("Folder '$path' created.")
        }
    }

    fun deleteFileOrFolder(path: String) {
        val project = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.deleteFileOrFolder(project.id, path)
            showToast("Deleted '$path'.")
        }
    }

    fun moveFileOrFolder(source: String, destination: String) {
        val project = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            repository.moveFileOrFolder(project.id, source, destination)
            showToast("Moved to '$destination'.")
        }
    }

    fun toggleSandboxPreview(show: Boolean) {
        val project = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            if (show) {
                val compiled = repository.compileSandboxHtml(project.id)
                _uiState.update { it.copy(showSandboxPreview = true, compiledSandboxHtml = compiled) }
            } else {
                _uiState.update { it.copy(showSandboxPreview = false) }
            }
        }
    }

    // -------------------------------------------------------------
    // MEMORY MANAGEMENT
    // -------------------------------------------------------------
    fun saveMemory(id: Long = 0, title: String, category: String, content: String, importance: Int = 3) {
        viewModelScope.launch {
            repository.saveMemory(id, title, category, content, importance)
            showToast("Memory saved securely.")
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
            showToast("Memory removed.")
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
            showToast("All memories cleared.")
        }
    }

    // -------------------------------------------------------------
    // EXPORT & SHARE
    // -------------------------------------------------------------
    fun exportProjectZip(context: Context) {
        val project = _uiState.value.currentProject ?: return
        viewModelScope.launch {
            try {
                val zipBytes = repository.exportProjectZip(project.id)
                val file = File(context.cacheDir, "${project.name.replace(" ", "_")}.zip")
                FileOutputStream(file).use { it.write(zipBytes) }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Project ZIP"))
            } catch (e: Exception) {
                showToast("Export failed: ${e.message}")
            }
        }
    }

    fun exportMemoriesJson(context: Context) {
        viewModelScope.launch {
            try {
                val json = repository.exportMemoriesJson()
                val file = File(context.cacheDir, "dragon_memories_backup.json")
                FileOutputStream(file).use { it.write(json.toByteArray()) }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Memories Backup"))
            } catch (e: Exception) {
                showToast("Export failed: ${e.message}")
            }
        }
    }

    fun importMemoriesFromJson(jsonString: String) {
        viewModelScope.launch {
            val count = repository.importMemoriesJson(jsonString)
            showToast("Imported $count memory entries.")
        }
    }

    fun shareCodeSnippet(context: Context, filename: String, code: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, filename)
            putExtra(Intent.EXTRA_TEXT, code)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share $filename"))
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
