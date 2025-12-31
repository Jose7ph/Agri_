package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.jgcompose.text.AutoScrollingText

enum class ImageButtonType {
    Vertical, Horizontal
}

@Composable
fun ImageButton(
    modifier: Modifier,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.primary,
    image: Int,
    type: ImageButtonType = ImageButtonType.Vertical,
    onClick: () -> Unit,
    fontSize: TextUnit = 15.sp,
    fontStyle: TextStyle = MaterialTheme.typography.titleSmall,
    imageSize: Dp = 20.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    colorFilter: ColorFilter? = null,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .padding(0.dp)
            .clickable(enabled) { onClick() },
    ) {
        if (type == ImageButtonType.Vertical) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image",
                    modifier = Modifier
                        .size(imageSize),
                    colorFilter = colorFilter
                )
                AutoScrollingText(
                    text = title,
                    color = textColor,
                    style = fontStyle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
                    .clickable(enabled) { onClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image",
                    modifier = Modifier.size(imageSize),
                    colorFilter = colorFilter
                )
                AutoScrollingText(
                    text = title,
                    color = textColor,
                    style = fontStyle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

    }
}

@Composable
fun SelectImageButton(
    modifier: Modifier,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.primary,
    image: Int,
    type: ImageButtonType = ImageButtonType.Vertical,
    onClick: () -> Unit,
    imageSize: Dp = 20.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    colorFilter: ColorFilter? = null,
    checked: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp)
) {
    Box(
        modifier = modifier
            .padding(0.dp)
            .clip(shape)
            .clickable { onClick() },
    ) {
        if (type == ImageButtonType.Vertical) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(imageSize)
                        .border(2.dp, if (checked) Color.Red else Color.Unspecified),
                    colorFilter = colorFilter
                )
                AutoScrollingText(
                    text = title,
                    color = textColor,
                    style = textStyle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image",
                    modifier = Modifier.size(imageSize),
                    colorFilter = colorFilter
                )
                Spacer(modifier = Modifier.width(20.dp))
                AutoScrollingText(
                    text = title,
                    color = textColor,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

    }
}

@Composable
fun SortButton(
    modifier: Modifier = Modifier,
    title: String,
    currentType: Int = 0,
    textColor: Color = MaterialTheme.colorScheme.primary,
    imageSize: Dp = 20.dp,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    onClick: (type: Int) -> Unit
) {
    var type by remember {
        mutableIntStateOf(currentType)
    }
    Box(
        modifier = modifier
            .clickable {
                if (type == 2) {
                    type = 0
                } else {
                    type++
                }
                onClick(type)
            },
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_sort_desc),
                contentDescription = "desc",
                colorFilter = if (type == 2) ColorFilter.tint(Color.Gray) else ColorFilter.tint(
                    textColor
                ),
                modifier = Modifier.size(imageSize),
            )
            Image(
                painter = painterResource(id = R.drawable.default_sort_asc),
                contentDescription = "asc",
                colorFilter = if (type == 1) ColorFilter.tint(Color.Gray) else ColorFilter.tint(
                    textColor
                ),
                modifier = Modifier.size(imageSize)
            )
            AutoScrollingText(
                text = title,
                color = textColor,
                style = style,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImageButtonPreview() {
    Column {

        ImageButton(
            title = "Button1",
            image = R.drawable.default_lost,
            type = ImageButtonType.Horizontal,
            onClick = {},
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(BlackAlpha)
        )
        ImageButton(
            title = "Button2",
            image = R.drawable.default_lost,
            type = ImageButtonType.Vertical,
            onClick = {},
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BlackAlpha),
        )
        SortButton(
            title = "sort", onClick = {}, modifier = Modifier
                .width(120.dp)
                .height(60.dp)
        )
    }
}
