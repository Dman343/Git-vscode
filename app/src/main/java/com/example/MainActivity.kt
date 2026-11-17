package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WorkspaceSettingsRepository
import com.example.ui.WorkspaceSettingsScreen
import com.example.ui.WorkspaceSettingsViewModel
import com.example.ui.theme.VSCodeConfigTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = WorkspaceSettingsRepository(applicationContext)
        val factory = WorkspaceSettingsViewModel.Factory(repository)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val vm: WorkspaceSettingsViewModel = viewModel(factory = factory)
            val uiState by vm.uiState.collectAsStateWithLifecycle()

            VSCodeConfigTheme(darkTheme = uiState.isDarkTheme) {
                WorkspaceSettingsScreen(
                    viewModel = vm,
                    widthSizeClass = windowSizeClass.widthSizeClass
                )
            }
        }
    }
}
