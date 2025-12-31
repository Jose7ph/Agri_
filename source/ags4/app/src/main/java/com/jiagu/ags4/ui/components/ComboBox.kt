package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.text.AutoScrollingText

@Composable
fun ComboBox(
    modifier: Modifier = Modifier,
    width: Dp,
    items: List<String>,
    leftIcons: List<Int>? = null,
    leftIconColor: Color? = null,
    selectedIndex: Int? = null,
    selectedValue: String,
    fontStyle: TextStyle = MaterialTheme.typography.titleSmall,
    fontColor: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Center,
    showIcon: Boolean = true,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    imageBackground: Int? = null,
    onImageClick: (Int) -> Unit={},
    onIndexChange: (Int) -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var position by remember { mutableStateOf(Offset.Zero) }
    val context = LocalContext.current
    var imageItems = listOf<ImageItem>()

    if (leftIcons != null) {
        imageItems = items.mapIndexed { index, s ->
            ImageItem(
                s,
                leftIcons[index],
                imageBackground,
                onImageClick = {
                    PopupMenuHolder.currentPopupMenu?.get()?.dismiss()
                    onImageClick(index)
                }
            )
        }
    }

    Column(modifier = modifier
        .clickable {
            if (imageItems.isEmpty()) {
                PopupMenu.showPopupMenu(
                    context,
                    items,
                    position.x.toInt(),
                    position.y.toInt(),
                    size.width,
                    size.height,
                    onIndexChange
                )
            } else {
                PopupMenu.showPopupMenu(
                    context,
                    PopupImageListAdapter(context, imageItems),
                    position.x.toInt(),
                    position.y.toInt(),
                    size.width,
                    size.height,
                    onIndexChange
                )
            }
        }
        .onGloballyPositioned {
            size = it.size; position = it.positionInRoot()
        }) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(width)
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val showLeftIcon =
                    leftIcons != null && selectedIndex != null && selectedIndex >= 0 && selectedIndex < leftIcons.size
                if (showLeftIcon) {
                    Image(
                        painter = painterResource(id = leftIcons!![selectedIndex!!]),
                        contentDescription = "icon",
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(24.dp),
                        colorFilter = if (leftIconColor != null) ColorFilter.tint(leftIconColor) else null
                    )
                }
                val textWidth: Dp = when {
                    showLeftIcon && showIcon -> width - 40.dp
                    (showLeftIcon && !showIcon) || (!showLeftIcon && showIcon) -> width - 20.dp
                    else -> width
                }
                AutoScrollingText(
                    text = selectedValue,
                    modifier = Modifier
                        .width(textWidth)
                        .padding(end = 4.dp),
                    style = fontStyle,
                    color = fontColor,
                    textAlign = textAlign
                )
            }
            if (showIcon) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd),
                    contentDescription = null,
                    tint = iconColor
                )
            }
        }
    }
}
