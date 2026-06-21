package com.example.app_lock

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_lock.ui.AppListScreen
import com.example.app_lock.ui.AppLockViewModel
import com.example.app_lock.ui.PinScreen
import com.example.app_lock.ui.theme.App_lockTheme
import com.example.app_lock.utils.PermissionUtils

class MainActivity : ComponentActivity() {
    private val viewModel: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App_lockTheme {
                val isInitialized by viewModel.isInitialized.collectAsState()
                val securityPin by viewModel.securityPin.collectAsState()
                
                if (!isInitialized) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    MainContent(viewModel, securityPin)
                }
            }
        }
    }

    @Composable
    private fun MainContent(viewModel: AppLockViewModel, securityPin: String?) {
        val navController = rememberNavController()
        var showPermissionDialog by remember { mutableStateOf(false) }

        // Check for accessibility permission on resume
        DisposableEffect(Unit) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    if (!PermissionUtils.isAccessibilityServiceEnabled(this@MainActivity)) {
                        showPermissionDialog = true
                    }
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = { Text("This app requires Accessibility Service permission to detect when locked apps are opened. Please enable it in settings.") },
                confirmButton = {
                    TextButton(onClick = {
                        showPermissionDialog = false
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }) {
                        Text("Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        NavHost(
            navController = navController,
            startDestination = if (securityPin == null) "setup_pin" else "enter_pin"
        ) {
            composable("setup_pin") {
                PinScreen(
                    isSetup = true,
                    onPinConfirmed = { pin ->
                        viewModel.setSecurityPin(pin)
                        navController.navigate("app_list") {
                            popUpTo("setup_pin") { inclusive = true }
                        }
                        true
                    }
                )
            }
            composable("enter_pin") {
                PinScreen(
                    isSetup = false,
                    onPinConfirmed = { pin ->
                        if (pin == securityPin) {
                            navController.navigate("app_list") {
                                popUpTo("enter_pin") { inclusive = true }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )
            }
            composable("app_list") {
                AppListScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        navController.navigate("setup_pin")
                    }
                )
            }
        }
    }
}
