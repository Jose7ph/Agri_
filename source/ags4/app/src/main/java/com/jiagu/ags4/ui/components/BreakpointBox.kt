package com.jiagu.ags4.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText

@Composable
fun BreakpointBox(
    modifier: Modifier = Modifier,
    title: String,
    breakpointList: List<String>,
    defaultClickNumber: Int = -1,
    onSelect: (Int) -> Unit = {},
) {
    var clickNumber by remember { mutableStateOf(defaultClickNumber) }
    LaunchedEffect(defaultClickNumber) {
        if(defaultClickNumber != clickNumber){
            clickNumber = defaultClickNumber
        }
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        //title
        Box(modifier = Modifier.padding(top = 4.dp), contentAlignment = Alignment.Center) {
            AutoScrollingText(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        //breakpoint
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.extraSmall)
        ) {
            for ((i, v) in breakpointList.withIndex()) {
                val clickTextColor =
                    if (clickNumber == i) MaterialTheme.colorScheme.onPrimary else Color.Black
                val clickBackgroundColor =
                    if (clickNumber == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(color = clickBackgroundColor)
                        .clickable {
                            clickNumber = if (clickNumber == i) {
                                -1
                            } else {
                                i
                            }
                            onSelect(clickNumber)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //icon
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(20.dp)
                            .clip(shape = CircleShape)
                            .background(Color.Red)
                            .border(
                                width = 0.1.dp,
                                color = Color.LightGray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val text = when (i) {
                            0 -> "BK"
                            else -> "$i"
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium,
                            color = clickTextColor
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = v,
                            style = MaterialTheme.typography.bodySmall,
                            color = clickTextColor
                        )
                    }
                }
            }
        }
    }
}
