package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.agent.DragonToolAction
import com.example.ui.components.CodeEditorView
import com.example.ui.components.CodeSandboxView
import com.example.ui.components.FileExplorerView
import com.example.ui.components.PixelBetaBadge
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DragonUiState
import com.example.ui.viewmodel.DragonViewModel
import com.example.ui.viewmodel.UiMessage

enum class WorkspaceViewMode {
    AGENT_STREAM,
    CODE_EDITOR,
    FILES_TREE,
    SANDBOX_PREVIEW
}

@Composable
fun AgentWorkspaceScreen(
    viewModel: DragonViewModel,
    uiState: DragonUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = uiState.language
    val isFa = lang.equals("fa", ignoreCase = true)
    val isDark = uiState.isDarkMode
    val glassBg = if (isDark) GlassDarkBg else GlassLightBg
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    var viewMode by remember { mutableStateOf(WorkspaceViewMode.AGENT_STREAM) }
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new messages arrive
    LaunchedEffect(uiState.agentMessages.size, uiState.isGenerating) {
        if (uiState.agentMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.agentMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Workspace Mode Selector Bar - Apple-Style Liquid Glass with soft rounded corners
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = if (isDark) Color(0x30000000) else Color(0x0C000000),
                    ambientColor = Color.Transparent
                ),
            shape = RoundedCornerShape(18.dp),
            color = glassBg,
            border = BorderStroke(1.dp, glassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Project Name & Status
                Column {
                    Text(
                        text = uiState.currentProject?.name ?: "Dragon Web App",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    )
                    if (uiState.lastExecutedToolsCount > 0) {
                        Text(
                            text = if (isFa) "${uiState.lastExecutedToolsCount} تغییر در فایل‌ها اعمال شد"
                            else "${uiState.lastExecutedToolsCount} file ops synced",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Clean Mode Switcher Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = viewMode == WorkspaceViewMode.AGENT_STREAM,
                        onClick = { viewMode = WorkspaceViewMode.AGENT_STREAM },
                        label = { Text(AppStrings.get("workspace_stream", lang), fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    FilterChip(
                        selected = viewMode == WorkspaceViewMode.CODE_EDITOR,
                        onClick = { viewMode = WorkspaceViewMode.CODE_EDITOR },
                        label = { Text(AppStrings.get("workspace_editor", lang), fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    FilterChip(
                        selected = viewMode == WorkspaceViewMode.SANDBOX_PREVIEW,
                        onClick = {
                            viewModel.toggleSandboxPreview(true)
                            viewMode = WorkspaceViewMode.SANDBOX_PREVIEW
                        },
                        label = { Text(AppStrings.get("workspace_preview", lang), fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(30.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Main Content Area based on ViewMode
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (viewMode) {
                WorkspaceViewMode.AGENT_STREAM -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Agent Messages Stream
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (uiState.agentMessages.isEmpty()) {
                                item {
                                    AgentMinimalGreeting(
                                        isFa = isFa,
                                        onQuickPrompt = { prompt ->
                                            promptInput = prompt
                                            viewModel.sendAgentPrompt(prompt)
                                        }
                                    )
                                }
                            }

                            items(uiState.agentMessages, key = { it.id }) { msg ->
                                AgentMessageItem(
                                    message = msg,
                                    isDark = isDark,
                                    isFa = isFa,
                                    onOpenFile = { path ->
                                        val file = uiState.projectFiles.firstOrNull { it.path == path }
                                        if (file != null) {
                                            viewModel.selectFile(file)
                                            viewMode = WorkspaceViewMode.CODE_EDITOR
                                        }
                                    },
                                    onOpenSandbox = {
                                        viewModel.toggleSandboxPreview(true)
                                        viewMode = WorkspaceViewMode.SANDBOX_PREVIEW
                                    }
                                )
                            }

                            if (uiState.isGenerating) {
                                item {
                                    AgentThinkingRow(isFa = isFa)
                                }
                            }
                        }

                        // Apple-Style Liquid Glass Input Area with SEPARATE Circular Floating Action Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Main Input Container (Floating Liquid Glass Pill)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        spotColor = if (isDark) Color(0x35000000) else Color(0x10000000),
                                        ambientColor = Color.Transparent
                                    ),
                                shape = RoundedCornerShape(24.dp),
                                color = glassBg,
                                border = BorderStroke(1.dp, glassBorder)
                            ) {
                                TextField(
                                    value = promptInput,
                                    onValueChange = { promptInput = it },
                                    placeholder = {
                                        Text(
                                            AppStrings.get("workspace_prompt_hint", lang),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 2.dp),
                                    maxLines = 4
                                )
                            }

                            // 2. Separate Floating Liquid Glass Circular Send Button
                            val canSend = promptInput.isNotBlank() && !uiState.isGenerating
                            val sendBtnBg = if (canSend) {
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.25f else 0.18f)
                            } else {
                                glassBg
                            }
                            val sendBtnBorder = if (canSend) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            } else {
                                glassBorder
                            }
                            val sendIconColor = if (canSend) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            }

                            Surface(
                                onClick = {
                                    if (canSend) {
                                        val text = promptInput.trim()
                                        promptInput = ""
                                        viewModel.sendAgentPrompt(text)
                                    }
                                },
                                enabled = canSend,
                                shape = CircleShape,
                                color = sendBtnBg,
                                border = BorderStroke(1.2.dp, sendBtnBorder),
                                modifier = Modifier
                                    .size(46.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = CircleShape,
                                        spotColor = if (isDark) Color(0x35000000) else Color(0x12000000),
                                        ambientColor = Color.Transparent
                                    )
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (uiState.isGenerating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Send,
                                            contentDescription = "Send",
                                            tint = sendIconColor,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                WorkspaceViewMode.CODE_EDITOR -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // File Explorer Sidebar
                        FileExplorerView(
                            files = uiState.projectFiles,
                            selectedFile = uiState.selectedFile,
                            onSelectFile = { viewModel.selectFile(it) },
                            onCreateFile = { viewModel.createNewFile(it) },
                            onCreateFolder = { viewModel.createNewFolder(it) },
                            onDeleteFile = { viewModel.deleteFileOrFolder(it) },
                            onMoveFile = { src, dst -> viewModel.moveFileOrFolder(src, dst) },
                            onExportZip = { viewModel.exportProjectZip(context) },
                            modifier = Modifier.width(150.dp)
                        )

                        // Code Editor
                        CodeEditorView(
                            file = uiState.selectedFile,
                            content = uiState.selectedFileContent,
                            isModified = uiState.isEditorModified,
                            onContentChange = { viewModel.updateEditorContent(it) },
                            onSave = { viewModel.saveCurrentFile() },
                            onPreviewSandbox = {
                                viewModel.toggleSandboxPreview(true)
                                viewMode = WorkspaceViewMode.SANDBOX_PREVIEW
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                WorkspaceViewMode.FILES_TREE -> {
                    FileExplorerView(
                        files = uiState.projectFiles,
                        selectedFile = uiState.selectedFile,
                        onSelectFile = {
                            viewModel.selectFile(it)
                            viewMode = WorkspaceViewMode.CODE_EDITOR
                        },
                        onCreateFile = { viewModel.createNewFile(it) },
                        onCreateFolder = { viewModel.createNewFolder(it) },
                        onDeleteFile = { viewModel.deleteFileOrFolder(it) },
                        onMoveFile = { src, dst -> viewModel.moveFileOrFolder(src, dst) },
                        onExportZip = { viewModel.exportProjectZip(context) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                WorkspaceViewMode.SANDBOX_PREVIEW -> {
                    CodeSandboxView(
                        htmlContent = uiState.compiledSandboxHtml,
                        onClose = {
                            viewModel.toggleSandboxPreview(false)
                            viewMode = WorkspaceViewMode.AGENT_STREAM
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentMinimalGreeting(
    isFa: Boolean,
    onQuickPrompt: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isFa) "دراگون ایجنت" else "Dragon Agent",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                PixelBetaBadge(
                    pixelSize = 1.3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isFa) "(مهندس هوشمند کدنویسی)" else "(Autonomous Coding)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            Text(
                text = if (isFa)
                    "ایجنت فایل‌های پروژه را به صورت هوشمند ایجاد، ویرایش و مدیریت می‌کند و با بخش‌های حافظه هماهنگ است."
                else
                    "The agent autonomously plans, creates, and refactors project files with targeted memory segment recall and live sandbox testing.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SuggestionChip(
                    onClick = {
                        onQuickPrompt(
                            if (isFa) "یک برنامه ماشین حساب مدرن با HTML و CSS و JS بساز"
                            else "Build a clean calculator web app in HTML, CSS, and JS"
                        )
                    },
                    label = { Text(if (isFa) "ماشین حساب" else "Calculator App", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )

                SuggestionChip(
                    onClick = {
                        onQuickPrompt(
                            if (isFa) "یک برنامه تایمر پومودورو مدرن طراحی کن"
                            else "Create a modern Pomodoro Timer web app"
                        )
                    },
                    label = { Text(if (isFa) "تایمر پومودورو" else "Pomodoro Timer", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

@Composable
private fun AgentMessageItem(
    message: UiMessage,
    isDark: Boolean,
    isFa: Boolean,
    onOpenFile: (String) -> Unit,
    onOpenSandbox: () -> Unit
) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = if (isUser) (if (isFa) "شما" else "You") else "Dragon Agent",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            )
            if (!isUser) {
                PixelBetaBadge(
                    pixelSize = 1.1.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Bubble
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else
                    MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 1.0f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (message.content.isNotEmpty()) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    )
                }

                // Tool Actions
                if (message.toolActions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (action in message.toolActions) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${action.actionType.name.lowercase().replace("_", " ")}: ${action.path}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (action.path.isNotBlank()) {
                                        TextButton(
                                            onClick = { onOpenFile(action.path) },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                        ) {
                                            Text(if (isFa) "باز کردن" else "Open", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Extracted code preview
                if (message.extractedBlocks.isNotEmpty()) {
                    for (block in message.extractedBlocks) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = block.filename ?: block.language,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { clipboardManager.setText(AnnotatedString(block.code)) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ContentCopy,
                                                contentDescription = "Copy",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        if (block.language.equals("html", ignoreCase = true) || block.filename?.endsWith(".html") == true) {
                                            IconButton(
                                                onClick = onOpenSandbox,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.PlayArrow,
                                                    contentDescription = "Preview",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = block.code.take(150) + if (block.code.length > 150) "\n..." else "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentThinkingRow(isFa: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isFa) "در حال پردازش و تغییر فایل‌ها..." else "Thinking & executing code tools...",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}
