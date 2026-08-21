package com.example.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PixelDAgentLogo
import com.example.ui.components.TermsDialog
import com.example.ui.localization.AppStrings
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DragonUiState
import com.example.ui.viewmodel.DragonViewModel
import com.example.ui.viewmodel.NavigationTab

@Composable
fun DragonMainScreen(
    viewModel: DragonViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFa = uiState.language.equals("fa", ignoreCase = true)
    val layoutDirection = if (isFa) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Show Toast when needed
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Terms Acceptance Dialog
    if (!uiState.hasAcceptedTerms && !uiState.isInitializing) {
        TermsDialog(
            onAccept = { viewModel.acceptTerms() },
            isDark = uiState.isDarkMode,
            isFa = isFa
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Screen Content - precisely calculated with system insets + floating bar clearances
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = statusBarTop + 60.dp,
                        bottom = navBarBottom + 74.dp
                    )
            ) {
                when (uiState.currentTab) {
                    NavigationTab.AGENT_WORKSPACE -> AgentWorkspaceScreen(viewModel = viewModel, uiState = uiState)
                    NavigationTab.CHAT_ASSISTANT -> ChatAssistantScreen(viewModel = viewModel, uiState = uiState)
                    NavigationTab.MEMORY_BANK -> MemoryBankScreen(viewModel = viewModel, uiState = uiState)
                    NavigationTab.API_PROVIDERS -> ApiProvidersScreen(viewModel = viewModel, uiState = uiState)
                    NavigationTab.SETTINGS_ABOUT -> SettingsAboutScreen(viewModel = viewModel, uiState = uiState)
                }
            }

            // Floating Minimal Liquid Glass Top Bar (Clean branding & active model status)
            FloatingLiquidGlassTopBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                uiState = uiState
            )

            // Floating Apple-Style Liquid Glass Bottom Navigation Bar (No background block)
            FloatingLiquidGlassBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                currentTab = uiState.currentTab,
                isDark = uiState.isDarkMode,
                lang = uiState.language,
                onTabSelect = { viewModel.selectTab(it) }
            )
        }
    }
}

/**
 * Ultra-clean Apple-style Liquid Glass Top Bar
 * Minimal, uncluttered floating bar with bold pixel "D agent" logo and status chip.
 * Language and Theme toggles moved exclusively into Settings per user instruction.
 */
@Composable
private fun FloatingLiquidGlassTopBar(
    modifier: Modifier = Modifier,
    uiState: DragonUiState
) {
    val isDark = uiState.isDarkMode
    val glassBg = if (isDark) GlassDarkBg else GlassLightBg
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (isDark) Color(0x40000000) else Color(0x10000000),
                ambientColor = Color.Transparent
            ),
        shape = RoundedCornerShape(22.dp),
        color = glassBg,
        border = BorderStroke(1.dp, glassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Bold Retro Pixel Art "D agent" Branding (Always LTR)
            PixelDAgentLogo(
                pixelSize = 2.8.dp,
                pixelColor = MaterialTheme.colorScheme.onSurface,
                accentColor = MaterialTheme.colorScheme.primary
            )

            // Right: Subtle Active Model Chip & Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (uiState.isGenerating) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Running",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else {
                    val modelName = uiState.selectedModel.ifEmpty { "Default" }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, glassBorder)
                    ) {
                        Text(
                            text = modelName.substringAfterLast("/"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple-style Liquid Glass Floating Bottom Navigation Bar
 * Pure translucent floating island with no black backplane or clipping issues.
 */
@Composable
private fun FloatingLiquidGlassBottomBar(
    modifier: Modifier = Modifier,
    currentTab: NavigationTab,
    isDark: Boolean,
    lang: String,
    onTabSelect: (NavigationTab) -> Unit
) {
    val glassBg = if (isDark) GlassDarkBg else GlassLightBg
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = if (isDark) Color(0x55000000) else Color(0x14000000),
                ambientColor = Color.Transparent
            ),
        shape = RoundedCornerShape(26.dp),
        color = glassBg,
        border = BorderStroke(1.dp, glassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.AGENT_WORKSPACE,
                onClick = { onTabSelect(NavigationTab.AGENT_WORKSPACE) },
                icon = Icons.Outlined.Code,
                selectedIcon = Icons.Filled.Code,
                label = AppStrings.get("nav_code_agent", lang)
            )

            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.CHAT_ASSISTANT,
                onClick = { onTabSelect(NavigationTab.CHAT_ASSISTANT) },
                icon = Icons.Outlined.ChatBubbleOutline,
                selectedIcon = Icons.Filled.ChatBubble,
                label = AppStrings.get("nav_chat", lang)
            )

            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.MEMORY_BANK,
                onClick = { onTabSelect(NavigationTab.MEMORY_BANK) },
                icon = Icons.Outlined.Psychology,
                selectedIcon = Icons.Filled.Psychology,
                label = AppStrings.get("nav_memory", lang)
            )

            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.API_PROVIDERS,
                onClick = { onTabSelect(NavigationTab.API_PROVIDERS) },
                icon = Icons.Outlined.Key,
                selectedIcon = Icons.Filled.Key,
                label = AppStrings.get("nav_api_keys", lang)
            )

            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.SETTINGS_ABOUT,
                onClick = { onTabSelect(NavigationTab.SETTINGS_ABOUT) },
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings,
                label = AppStrings.get("nav_settings", lang)
            )
        }
    }
}

@Composable
private fun LiquidGlassNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    val itemBgColor by animateColorAsState(
        targetValue = if (selected) primaryColor.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "nav_item_bg"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) primaryColor else mutedColor,
        animationSpec = tween(durationMillis = 200),
        label = "nav_icon_color"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(itemBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = iconColor
            ),
            maxLines = 1
        )
    }
}
