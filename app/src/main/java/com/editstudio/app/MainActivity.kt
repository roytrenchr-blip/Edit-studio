package com.editstudio.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.editstudio.app.navigation.AppNavigation
import com.editstudio.app.ui.theme.EditStudioTheme
import com.editstudio.app.ui.theme.NavyBg

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EditStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NavyBg
                ) {
                    val permissionsLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { _ ->
                        // Permissions granted/denied handled gracefully
                    }

                    LaunchedEffect(Unit) {
                        val needed = mutableListOf<String>()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                needed.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            needed.add(Manifest.permission.RECORD_AUDIO)
                        }
                        if (needed.isNotEmpty()) {
                            permissionsLauncher.launch(needed.toTypedArray())
                        }
                    }

                    AppNavigation()
                }
            }
        }
    }
}
