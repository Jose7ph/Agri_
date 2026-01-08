package com.jiagu.ags4.scene.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.jgcompose.container.MainContent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.jiagu.ags4.ihattys.ble.IhattysBleManager
import com.jiagu.ags4.ihattys.ble.IhattysBlePermissions


@Composable
fun DeviceNewCardDemoScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current

    var bleRunning by remember { mutableStateOf(IhattysBleManager.isRunning(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val ok = result.values.all { it }
        if (ok) {
            val newState = IhattysBleManager.toggle(context)
            bleRunning = newState
            Toast.makeText(
                context,
                if (newState) "IHATTYS BLE started broadcasting" else "IHATTYS BLE stopped",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(context, "BLE permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    MainContent(
        title = "new card demo",
        barAction = {},
        breakAction = { navController.popBackStack() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            NewCardDemoCard(
                onGrpc = {
                    // TODO: put your gRPC logic here
                },
                onBle = {
                    // ✅ Press once: start, press again: stop
                    if (!IhattysBlePermissions.hasAll(context)) {
                        permissionLauncher.launch(IhattysBlePermissions.required())
                        return@NewCardDemoCard
                    }

                    val newState = IhattysBleManager.toggle(context)
                    bleRunning = newState

                    Toast.makeText(
                        context,
                        if (newState) "IHATTYS BLE started broadcasting" else "IHATTYS BLE stopped",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}


@Composable
fun NewCardDemoCard(
    onGrpc: () -> Unit,
    onBle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "new card",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Body -> buttons directly (no popup, no extra button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onGrpc
                ) {
                    Text("gRPC")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onBle
                ) {
                    Text("BLE")
                }
            }
        }
    }
}
