package com.jiagu.jgcompose.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

enum class WarnTypeEnum {
    WARN_TYPE_INFO, WARN_TYPE_WARN, WARN_TYPE_ERROR
}

/**
 * Card
 *
 * @param modifier 装饰器
 * @param image 图片
 * @param title 标题
 * @param content 内容
 * @param warnType 警告类型 用于显示不同卡片颜色
 * @receiver
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    image: Int?,
    title: String,
    content: @Composable ColumnScope.() -> Unit = {},
    warnType: WarnTypeEnum = WarnTypeEnum.WARN_TYPE_INFO
) {
    val containerColor: Color
    val contentColor: Color
    when (warnType) {
        WarnTypeEnum.WARN_TYPE_WARN -> {
            containerColor = MaterialTheme.colorScheme.tertiary
            contentColor = MaterialTheme.colorScheme.onTertiary
        }

        WarnTypeEnum.WARN_TYPE_ERROR -> {
            containerColor = MaterialTheme.colorScheme.error
            contentColor = MaterialTheme.colorScheme.onError
        }

        else -> {
            containerColor = MaterialTheme.colorScheme.onPrimary
            contentColor = Color.Black
        }
    }
    Surface(
        modifier = modifier.clip(MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                image?.let {
                    Image(
                        painter = painterResource(id = image),
                        contentDescription = "",
                        modifier = Modifier.fillMaxHeight(),
                        colorFilter = ColorFilter.tint(contentColor)
                    )
                }
                AutoScrollingText(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth(),
                    color = contentColor
                )

            }
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                content()
            }
        }
    }
}

@Preview(widthDp = 640, heightDp = 360)
@Composable
private fun CardPreview() {
    ComposeTheme {
        Column() {
            Card(
                modifier = Modifier
                    .width(240.dp)
                    .height(120.dp),
                image = R.drawable.lost,
//                image = R.drawable.lost,
                title = "Card",
//                warnType = WarnTypeEnum.WARN_TYPE_WARN,
                content = {
                    Text(text = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                }
            )
        }
    }
}