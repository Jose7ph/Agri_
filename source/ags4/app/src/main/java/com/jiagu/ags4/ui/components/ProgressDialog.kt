package com.jiagu.ags4.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jiagu.api.viewmodel.ProgressModel

@Composable
fun ProgressTaskDialog(vm: ProgressModel) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val progress by vm.progress.observeAsState(0)
        when (progress) {
            is ProgressModel.ProgressMessage -> {
            }
            is ProgressModel.ProgressNotice -> {
            }
            is ProgressModel.ProgressResult -> {
            }
            else -> {}
        }
    }
}
