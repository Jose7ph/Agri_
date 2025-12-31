package com.jiagu.jgcompose.container

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.bar.SimpleTopBar
import com.jiagu.jgcompose.ext.disableAutoFocus

@Composable
fun MainContent(
    title: String,
    backgroundColor: Color = Color.White,
    breakAction: () -> Unit,
    spaceBy: Dp = 0.dp,
    height: Dp = 36.dp,
    barAction: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    var breakActionEnabled by remember {
        mutableStateOf(true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .disableAutoFocus(),
        verticalArrangement = Arrangement.spacedBy(spaceBy)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            SimpleTopBar(
                breakAction = {
                    if (breakActionEnabled) {
                        breakAction()
                    }
                    breakActionEnabled = false
                }, title = title
            ) {
                barAction()
            }
        }
        content()
    }
}