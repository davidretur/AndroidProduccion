package com.example.shownotification

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shownotification.Service.NotificationService
import com.example.shownotification.ui.theme.ShowNotificationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShowNotificationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val notificationService = NotificationService(applicationContext)
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding
                        ),
                        contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                                val permissionState = rememberPermissionState(
                                    permission = android.Manifest.permission.POST_NOTIFICATIONS
                                )
                                if (!permissionState.status.isGranted){
                                    OutlinedButton(onClick = { permissionState.launchPermissionRequest()}) {
                                        Text(text = "Allow Notification",
                                            fontSize = 22.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.size(16.dp))
                            Button(onClick = {notificationService.showNotificastion()}) {
                                Text(text = "Show Notification",
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
