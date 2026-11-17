package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ImportedRepoDefaults
import com.example.data.SettingValue
import com.example.ui.components.JsonInspectorPanel
import com.example.ui.components.SettingCard
import com.example.ui.theme.LocalSemanticCodeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSettingsScreen(
    viewModel: WorkspaceSettingsViewModel,
    widthSizeClass: WindowWidthSizeClass
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val isWideLayout = widthSizeClass != WindowWidthSizeClass.Compact

    val copiedMessage = stringResource(R.string.copied_snackbar)
    val resetMessage = stringResource(R.string.reset_snackbar)
    val appliedMessage = stringResource(R.string.applied_snackbar)

    val onCopyJson: () -> Unit = {
        clipboardManager.setText(AnnotatedString(uiState.formattedJson))
        scope.launch {
            snackbarHostState.showSnackbar(copiedMessage)
        }
    }

    val onResetDefaults: () -> Unit = {
        viewModel.resetAllToRepoDefaults()
        scope.launch {
            snackbarHostState.showSnackbar(resetMessage)
        }
    }

    val onApplyRawJson: () -> Unit = {
        if (viewModel.applyRawJsonDraft()) {
            scope.launch {
                snackbarHostState.showSnackbar(appliedMessage)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = ImportedRepoDefaults.REPO_NAME,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::toggleTheme,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) {
                                Icons.Outlined.LightMode
                            } else {
                                Icons.Outlined.DarkMode
                            },
                            contentDescription = if (uiState.isDarkTheme) {
                                "Switch to light theme"
                            } else {
                                "Switch to dark theme"
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!isWideLayout) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == ConfigTab.SETTINGS,
                        onClick = { viewModel.selectTab(ConfigTab.SETTINGS) },
                        modifier = Modifier.testTag("nav_settings_tab"),
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == ConfigTab.SETTINGS) {
                                    Icons.Filled.Tune
                                } else {
                                    Icons.Outlined.Tune
                                },
                                contentDescription = stringResource(R.string.tab_controls)
                            )
                        },
                        label = { Text(stringResource(R.string.tab_controls)) }
                    )
                    NavigationBarItem(
                        selected = uiState.selectedTab == ConfigTab.RAW_JSON,
                        onClick = { viewModel.selectTab(ConfigTab.RAW_JSON) },
                        modifier = Modifier.testTag("nav_json_tab"),
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == ConfigTab.RAW_JSON) {
                                    Icons.Filled.Code
                                } else {
                                    Icons.Outlined.Code
                                },
                                contentDescription = stringResource(R.string.tab_json)
                            )
                        },
                        label = { Text(stringResource(R.string.tab_json)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1120.dp)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WorkspaceHeroCard(
                            isModified = uiState.isModifiedFromRepo,
                            settingsCount = uiState.entries.size,
                            onResetDefaults = onResetDefaults
                        )
                        uiState.entries.forEach { entry ->
                            SettingCard(
                                entry = entry,
                                onToggleBoolean = { newVal ->
                                    viewModel.toggleSetting(entry.key, newVal)
                                },
                                onResetToDefault = {
                                    viewModel.resetKeyToDefault(entry.key)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        JsonInspectorPanel(
                            formattedJson = uiState.formattedJson,
                            isEditing = uiState.isEditingRawJson,
                            draftJson = uiState.rawJsonDraft,
                            jsonError = uiState.rawJsonError,
                            onCopyJson = onCopyJson,
                            onStartEdit = viewModel::startEditingRawJson,
                            onDraftChange = viewModel::updateRawJsonDraft,
                            onApplyEdit = onApplyRawJson,
                            onCancelEdit = viewModel::cancelEditingRawJson
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                ) {
                    AnimatedVisibility(
                        visible = uiState.selectedTab == ConfigTab.SETTINGS,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item(key = "hero") {
                                WorkspaceHeroCard(
                                    isModified = uiState.isModifiedFromRepo,
                                    settingsCount = uiState.entries.size,
                                    onResetDefaults = onResetDefaults
                                )
                            }
                            items(
                                items = uiState.entries,
                                key = { it.key }
                            ) { entry ->
                                SettingCard(
                                    entry = entry,
                                    onToggleBoolean = { newVal ->
                                        viewModel.toggleSetting(entry.key, newVal)
                                    },
                                    onResetToDefault = {
                                        viewModel.resetKeyToDefault(entry.key)
                                    }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.selectedTab == ConfigTab.RAW_JSON,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            JsonInspectorPanel(
                                formattedJson = uiState.formattedJson,
                                isEditing = uiState.isEditingRawJson,
                                draftJson = uiState.rawJsonDraft,
                                jsonError = uiState.rawJsonError,
                                onCopyJson = onCopyJson,
                                onStartEdit = viewModel::startEditingRawJson,
                                onDraftChange = viewModel::updateRawJsonDraft,
                                onApplyEdit = onApplyRawJson,
                                onCancelEdit = viewModel::cancelEditingRawJson
                            )

                            if (uiState.isModifiedFromRepo) {
                                OutlinedButton(
                                    onClick = onResetDefaults,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("reset_all_json_tab_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RestartAlt,
                                        contentDescription = stringResource(R.string.action_reset_defaults),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.action_reset_defaults))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceHeroCard(
    isModified: Boolean,
    settingsCount: Int,
    onResetDefaults: () -> Unit
) {
    val codeColors = LocalSemanticCodeColors.current
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val surfaceElevated = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workspace_hero_card"),
        shape = MaterialTheme.shapes.large,
        color = surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.14f),
                            surface,
                            surfaceElevated.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val dotColor = primary.copy(alpha = 0.12f)
                val step = 28.dp.toPx()
                var x = step
                while (x < size.width) {
                    var y = step
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = 1.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                        y += step
                    }
                    x += step
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ImportedRepoDefaults.EXTENSION_NAMESPACE,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.workspace_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isModified) {
                        FilledTonalIconButton(
                            onClick = onResetDefaults,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("reset_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RestartAlt,
                                contentDescription = stringResource(R.string.action_reset_defaults)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = if (isModified) {
                        codeColors.modifiedBadge
                    } else {
                        codeColors.jsonBooleanTrue
                    }
                    val statusLabel = if (isModified) {
                        stringResource(R.string.status_modified)
                    } else {
                        stringResource(R.string.status_synced)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$settingsCount keys",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
