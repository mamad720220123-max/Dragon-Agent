package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.crypto.CryptoManager
import com.example.data.local.entities.ApiProviderEntity
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.DragonUiState
import com.example.ui.viewmodel.DragonViewModel
import org.json.JSONArray

@Composable
fun ApiProvidersScreen(
    viewModel: DragonViewModel,
    uiState: DragonUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = uiState.language
    val isFa = lang.equals("fa", ignoreCase = true)
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProvider by remember { mutableStateOf<ApiProviderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = AppStrings.get("api_providers_title", lang),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = if (isFa) "مدیریت کلیدهای رمزنگاری‌شده و مدل‌ها" else "Encrypted API keys & model endpoints",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.get("api_add", lang), fontSize = 12.sp)
                }
            }
        }

        // List of Providers
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.providers, key = { it.id }) { provider ->
                val isDefault = provider.isDefault
                val models = remember(provider.modelsJson) {
                    try {
                        val arr = JSONArray(provider.modelsJson)
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.getString(i))
                        }
                        list
                    } catch (e: Exception) {
                        listOf("gpt-4o")
                    }
                }

                val hasKey = CryptoManager.decrypt(provider.apiKeyEncrypted).isNotBlank()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.selectProvider(provider) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDefault)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title & Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = provider.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                if (isDefault) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = AppStrings.get("api_active", lang),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { editingProvider = provider },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                if (!isDefault) {
                                    IconButton(
                                        onClick = { viewModel.deleteProvider(provider.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Base URL
                        Text(
                            text = provider.baseUrl,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Key Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasKey)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error
                                    )
                            )
                            Text(
                                text = if (hasKey) AppStrings.get("api_key_set", lang) else AppStrings.get("api_key_empty", lang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Models Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (model in models.take(4)) {
                                val isModelSelected = provider.selectedModel == model
                                FilterChip(
                                    selected = isModelSelected,
                                    onClick = {
                                        viewModel.selectProvider(provider)
                                        viewModel.selectModel(model)
                                    },
                                    label = { Text(model, fontSize = 10.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Provider Dialog
    if (showAddDialog || editingProvider != null) {
        val isEdit = editingProvider != null
        var name by remember { mutableStateOf(editingProvider?.name ?: "") }
        var baseUrl by remember { mutableStateOf(editingProvider?.baseUrl ?: "https://api.openai.com/v1") }
        var apiKey by remember { mutableStateOf(if (editingProvider != null) CryptoManager.decrypt(editingProvider!!.apiKeyEncrypted) else "") }
        var modelsInput by remember {
            mutableStateOf(
                if (editingProvider != null) {
                    try {
                        val arr = JSONArray(editingProvider!!.modelsJson)
                        val l = mutableListOf<String>()
                        for (i in 0 until arr.length()) l.add(arr.getString(i))
                        l.joinToString(", ")
                    } catch (e: Exception) {
                        "gpt-4o, gpt-4o-mini"
                    }
                } else "gpt-4o, gpt-4o-mini, claude-3-5-sonnet, deepseek-chat"
            )
        }
        var selectedModel by remember { mutableStateOf(editingProvider?.selectedModel ?: "gpt-4o") }
        var tokenLimit by remember { mutableStateOf(editingProvider?.tokenLimit?.toString() ?: "500000") }
        var temperature by remember { mutableStateOf(editingProvider?.temperature ?: 0.7f) }
        var maxTokens by remember { mutableStateOf(editingProvider?.maxTokens?.toString() ?: "4096") }
        var setAsDefault by remember { mutableStateOf(editingProvider?.isDefault ?: false) }
        var showPassword by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingProvider = null
            },
            title = {
                Text(
                    text = if (isEdit) AppStrings.get("edit", lang) else AppStrings.get("api_add", lang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Provider Name") },
                        placeholder = { Text("OpenAI / Groq / Ollama") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key (Encrypted on Device)") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = modelsInput,
                        onValueChange = { modelsInput = it },
                        label = { Text("Models (Comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isFa) "انتخاب به عنوان پیش‌فرض" else "Set as Default",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = setAsDefault,
                            onCheckedChange = { setAsDefault = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && baseUrl.isNotBlank()) {
                            val modelsList = modelsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val modelsJson = JSONArray(modelsList).toString()
                            val selModel = if (modelsList.contains(selectedModel)) selectedModel else modelsList.firstOrNull() ?: "default"

                            val id = editingProvider?.id ?: 0L
                            val provider = ApiProviderEntity(
                                id = id,
                                name = name.trim(),
                                baseUrl = baseUrl.trim(),
                                apiKeyEncrypted = "",
                                modelsJson = modelsJson,
                                selectedModel = selModel,
                                isDefault = setAsDefault,
                                tokenLimit = tokenLimit.toLongOrNull() ?: 500000L,
                                temperature = temperature,
                                maxTokens = maxTokens.toIntOrNull() ?: 4096
                            )

                            viewModel.saveProvider(provider, apiKey.trim())
                            showAddDialog = false
                            editingProvider = null
                        }
                    }
                ) {
                    Text(AppStrings.get("save", lang))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        editingProvider = null
                    }
                ) {
                    Text(AppStrings.get("cancel", lang))
                }
            }
        )
    }
}
