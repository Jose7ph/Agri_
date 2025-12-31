package com.jiagu.ags4.scene.device

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.ui.theme.DarkAlpha
import com.jiagu.device.vkprotocol.NewWarnTool

/**
 * 警告信息
 */
@Composable
fun DeviceWarningBox(
    modifier: Modifier = Modifier,
    warnInfoList: List<NewWarnTool.WarnStringData> = emptyList(),
    onClose: () -> Unit
) {
    Row(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .background(
                    color = DarkAlpha,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                )
                .width(25.dp)
                .height(50.dp)
                .clickable {
                    onClose()
                },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = DarkAlpha,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                ), contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(warnInfoList) { index, warn ->
                        val color = when (warn.warnType) {
                            NewWarnTool.WARN_TYPE_ERROR -> MaterialTheme.colorScheme.error
                            NewWarnTool.WARN_TYPE_WARN -> MaterialTheme.colorScheme.tertiary
                            else -> androidx.compose.ui.graphics.Color.White
                        }
                        val contentColor = when (warn.warnType) {
                            NewWarnTool.WARN_TYPE_ERROR -> MaterialTheme.colorScheme.onError
                            NewWarnTool.WARN_TYPE_WARN -> MaterialTheme.colorScheme.onTertiary
                            else -> androidx.compose.ui.graphics.Color.Black
                        }
                        Surface(
                            modifier = Modifier,
                            color = color,
                            shape = MaterialTheme.shapes.small,
                            contentColor = contentColor
                        ) {
                            Text(
                                text = warn.warnString,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}