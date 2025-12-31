package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.text.AutoScrollingText


@Composable
fun TeamDataRow(
    title: String,
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //title
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            //text
            AutoScrollingText(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TeamDataColumn(
    title: String,
    text: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //title
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            //text
            AutoScrollingText(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TeamTitleMenuRow(
    title: String, content: @Composable RowScope.() -> Unit = {}, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 10.dp)
            .noEffectClickable {
                onClick()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //title
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "$title arrow right",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun TeamTitleRow(
    title: String, text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //title
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AutoScrollingText(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray
            )
        }
    }
}