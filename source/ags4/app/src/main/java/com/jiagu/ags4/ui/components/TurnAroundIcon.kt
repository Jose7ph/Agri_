package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.ComposeTheme

@Composable
fun TurnAroundIcon() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(modifier = Modifier.padding(horizontal = 12.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.default_rotate_left),
                contentDescription = "rotate_left",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            Image(
                painter = painterResource(R.drawable.default_rotate_right),
                contentDescription = "rotate_right",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Image(
            painter = painterResource(R.drawable.default_drone),
            contentDescription = "drone",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxHeight(0.5f)
        )
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun TurnAroundIconPreview() {
    ComposeTheme {
        Column {
            TurnAroundIcon()
        }
    }
}