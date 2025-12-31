package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.bluetooth.BluetoothList
import com.jiagu.jgcompose.theme.ComposeTheme

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun BluetoothListPreview() {
    ComposeTheme {
        Column(modifier = Modifier.padding(10.dp)) {
            BluetoothList(
                buttonName = "search",
                bluetoothList = listOf(),
                onItemClick = { _ -> },
                searching = false,
                onSearchClick = {})

        }
    }
}