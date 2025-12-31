package com.jiagu.jgcompose.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

@Composable
fun InformationPopup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
    onConfirm: () -> Unit,
) {
    ScreenPopup(
        onConfirm = onConfirm,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //弹窗标题
                AutoScrollingText(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.titleSmall
                )
                Column(
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        .padding(top = 5.dp, bottom = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    content()
                }
            }
        },
        showCancel = false
    )
}

@Composable
fun InformationCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    titleStyle: TextStyle = MaterialTheme.typography.bodySmall,
    titleColor: Color = Color.Gray,
    contentStyle: TextStyle = MaterialTheme.typography.titleSmall,
    contentColor: Color = Color.Black,
) {
    Column(
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AutoScrollingText(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = titleStyle,
                textAlign = TextAlign.Center,
                color = titleColor
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            AutoScrollingText(
                text = content,
                modifier = Modifier.fillMaxWidth(),
                style = contentStyle,
                textAlign = TextAlign.Center,
                color = contentColor
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun InformationPopupPreview() {
    ComposeTheme {
        Column {
            InformationPopup(title = "Info", content = {
                Row {
                    InformationCard(
                        modifier = Modifier.weight(1f),
                        title = "title1",
                        content = "content1"
                    )
                    InformationCard(
                        modifier = Modifier.weight(1f),
                        title = "title1",
                        content = "content1"
                    )
                }
                Row {
                    InformationCard(
                        modifier = Modifier.weight(1f),
                        title = "title1",
                        content = "content1"
                    )
                    InformationCard(
                        modifier = Modifier.weight(1f),
                        title = "title1",
                        content = "content1"
                    )
                }
            }, onConfirm = {})
        }
    }
}