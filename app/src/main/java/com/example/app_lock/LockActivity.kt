package com.example.app_lock

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.app_lock.data.LockManager
import com.example.app_lock.ui.AppLockViewModel
import com.example.app_lock.ui.PinScreen
import com.example.app_lock.ui.theme.App_lockTheme

class LockActivity : ComponentActivity() {
    private val viewModel: AppLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra("LOCKED_PACKAGE_NAME") ?: ""
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "Restricted App"
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })

        setContent {
            App_lockTheme {
                val securityPin by viewModel.securityPin.collectAsState()
                
                PinScreen(
                    isSetup = false,
                    onPinConfirmed = { pin ->
                        if (pin == securityPin) {
                            LockManager.unlockPackage(packageName)
                            finish()
                            true
                        } else {
                            false
                        }
                    },
                    title = "$appName is locked"
                )
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
