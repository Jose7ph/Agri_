package com.jiagu.jgcompose.dialog

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


data class DialogState(
    val isVisible: Boolean = false,
    val content: (@Composable () -> Unit)? = null
)


class DialogViewModel : ViewModel() {
    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState = _dialogState.asStateFlow()

    fun showDialog(
        content: (@Composable () -> Unit)? = null
    ) {
        _dialogState.value = DialogState(
            isVisible = true,
            content = content
        )
    }

    fun hideDialog() {
        _dialogState.value = DialogState()
    }
}