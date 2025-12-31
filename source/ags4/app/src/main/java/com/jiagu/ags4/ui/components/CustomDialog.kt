package com.jiagu.ags4.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.jiagu.ags4.R
import com.jiagu.jgcompose.textfield.NormalTextField

@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    title: String? = null,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
    action: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (title != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                ) {
                    content()
                }
                action()
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String? = null,
    content: String? = null,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val systemUiController = rememberSystemUiController()
    CustomDialog(modifier,onDismissRequest = {
        systemUiController.isSystemBarsVisible = false
        onCancel()
    }, title = title, properties = DialogProperties(dismissOnClickOutside = false), content = {
        if (!content.isNullOrEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = content,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(
                onClick = {
                    systemUiController.isSystemBarsVisible = false
                    onCancel()
                },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
            ) {
                Text(stringResource(id = R.string.cancel))
            }
            TextButton(
                onClick = {
                    systemUiController.isSystemBarsVisible = false
                    onConfirm()
                },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        }
    }
}

@Composable
fun InputDialog(
    title: String? = null,
    content: String = "",
    placeHolder: String = "",
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    CustomDialog(modifier,onDismissRequest = {
        systemUiController.isSystemBarsVisible = false
        onCancel()
    }, title = title, properties = DialogProperties(dismissOnClickOutside = false), content = {
        NormalTextField(
            text = content,
            onValueChange = onValueChanged,
            modifier = Modifier.height(30.dp),
            hint = placeHolder,
            textStyle = MaterialTheme.typography.bodyMedium,
        )
    }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(
                onClick = {
                    systemUiController.isSystemBarsVisible = false
                    onCancel()
                },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
            ) {
                Text(stringResource(id = R.string.cancel))
            }
            TextButton(
                onClick = {
                    systemUiController.isSystemBarsVisible = false
                    onConfirm()
                },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        }
    }
}
