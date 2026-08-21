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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CodeSandboxView
import com.example.ui.components.PixelBetaBadge
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DragonUiState
import com.example.ui.viewmodel.DragonViewModel
import com.example.ui.viewmodel.UiMessage

@Composable
fun ChatAssistantScreen(
    viewModel: DragonViewModel,
    uiState: DragonUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lang = uiState.language
    val isFa = lang.equals("fa", ignoreCase = true)
    val isDark = uiState.isDarkMode
    val glassBg = if (isDark) GlassDarkBg else GlassLightBg
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var previewHtmlCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.chatMessages.size, uiState.isGenerating) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    if (previewHtmlCode != null) {
        CodeSandboxView(
            htmlContent = previewHtmlCode!!,
            onClose = { previewHtmlCode = null },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.chatMessages.isEmpty()) {
                item {
                    ChatEmptyPlaceholder(isFa = isFa, onSelectPrompt = { prompt ->
                        inputText = prompt
                        viewModel.sendChatMessage(prompt)
                    })
                }
            }

            items(uiState.chatMessages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
                    ) {
                        Text(
                            text = if (isUser) (if (isFa) "شما" else "You") else "Dragon Agent",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (!isUser) {
                            PixelBetaBadge(
                                pixelSize = 1.1.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

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
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            )

                            // Render code blocks with copy & sandbox actions
                            if (msg.extractedBlocks.isNotEmpty()) {
                                for (block in msg.extractedBlocks) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
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
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(block.code))
                                                            viewModel.showToast(AppStrings.get("copied", lang))
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.ContentCopy,
                                                            contentDescription = "Copy",
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.shareCodeSnippet(context, block.filename ?: "code.txt", block.code)
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Share,
                                                            contentDescription = "Share",
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }

                                                    if (block.language.equals("html", ignoreCase = true) || block.filename?.endsWith(".html") == true) {
                                                        IconButton(
                                                            onClick = { previewHtmlCode = block.code },
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
                                                text = block.code,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isGenerating) {
                item {
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
                            text = if (isFa) "در حال پاسخ..." else "Thinking...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
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
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            AppStrings.get("chat_hint", lang),
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
            val canSend = inputText.isNotBlank() && !uiState.isGenerating
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
                        val text = inputText.trim()
                        inputText = ""
                        viewModel.sendChatMessage(text)
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

@Composable
private fun ChatEmptyPlaceholder(
    isFa: Boolean,
    onSelectPrompt: (String) -> Unit
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
                    text = if (isFa) "گفتگو و مشاوره هوشمند" else "AI Chat & Consultation",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                PixelBetaBadge(
                    pixelSize = 1.2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = if (isFa)
                    "هر سؤالی در مورد برنامه‌نویسی دارید بپرسید، یا درخواست نمونه کد و توضیح مفاهیم بدهید."
                else
                    "Ask coding questions, explore algorithms, or discuss software design with instant code formatting and sandbox previews.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SuggestionChip(
                    onClick = {
                        onSelectPrompt(
                            if (isFa) "تفاوت Promise و Async/Await در جاوااسکریپت چیست؟"
                            else "Explain the difference between Promise and Async/Await in JavaScript"
                        )
                    },
                    label = { Text(if (isFa) "Async/Await" else "JS Promises", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )

                SuggestionChip(
                    onClick = {
                        onSelectPrompt(
                            if (isFa) "یک انیمیشن CSS زیبا برای دکمه بنویس"
                            else "Write a clean CSS button ripple animation"
                        )
                    },
                    label = { Text(if (isFa) "انیمیشن دکمه" else "Button Animation", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}
