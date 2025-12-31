package com.jiagu.jgcompose.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 *  蓝牙列表
 *
 * @param modifier 装饰器
 * @param buttonName 按钮名称
 * @param bluetoothList 蓝牙列表
 * @param searching 是否正在检索
 * @param onItemClick 点击列表回调 p1:蓝牙名称
 * @param onSearchClick 点击搜索回调
 */
@SuppressLint("MissingPermission")
@Composable
fun BluetoothList(
    modifier: Modifier = Modifier,
    buttonName: String,
    bluetoothList: List<BluetoothDevice>?,
    searching: Boolean,
    onItemClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(modifier = modifier
        .fillMaxSize()
        .border(
            width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.medium
        )
        .clickable(false) {}) {
        Button(
            onClick = {
                onSearchClick()
            },
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
                .height(30.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium,
            enabled = !searching,
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                AutoScrollingText(
                    text = buttonName,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (searching) Color.Black else MaterialTheme.colorScheme.onPrimary
                )
                if (searching) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 10.dp)
                            .size(16.dp), color = Color.Black, strokeWidth = 1.dp
                    )
                }
            }
        }
        if (bluetoothList.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.no_data),
                    color = Color.LightGray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(bluetoothList.size) {
                    val bluetoothDevice = bluetoothList[it]
                    val name = bluetoothDevice.name?.trim() ?: "null"
                    val address = bluetoothDevice.address
                    Box(modifier = Modifier
                        .clickable {
                            onItemClick(address)
                        }
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center) {
                        Text(
                            text = "$name($address)",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (it != bluetoothList.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp)
                                .padding(top = 10.dp)
                        )
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun BluetoothListPreview() {
    ComposeTheme {
        Column(modifier = Modifier.padding(10.dp)) {
            BluetoothList(
                buttonName = "检索",
                bluetoothList = listOf(),
                onItemClick = { _ -> },
                searching = false,
                onSearchClick = {})

        }
    }
}