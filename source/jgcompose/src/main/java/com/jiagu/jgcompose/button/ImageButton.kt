package com.jiagu.jgcompose.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors

@Composable
fun ImageVerticalButton(
    modifier: Modifier = Modifier,
    text: String,
    image: Int,
    colors: ButtonColors=ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = LocalExtendedColors.current.buttonDisabled
    ),
    textColor: Color = Color.White,
    fontStyle: TextStyle = MaterialTheme.typography.titleSmall,
    shape: Shape = MaterialTheme.shapes.small,
    imageSize: Dp = 30.dp,
    imageColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        colors = colors,
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image",
                    modifier = Modifier.size(imageSize),
                    colorFilter = ColorFilter.tint(imageColor)
                )
            }
            AutoScrollingText(
                text = text,
                color = textColor,
                style = fontStyle,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ImageHorizontalButton(
    modifier: Modifier = Modifier,
    text: String,
    image: Int,
    colors: ButtonColors=ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = LocalExtendedColors.current.buttonDisabled
    ),
    textColor: Color = Color.White,
    fontStyle: TextStyle = MaterialTheme.typography.titleSmall,
    shape: Shape = MaterialTheme.shapes.small,
    imageSize: Dp = 30.dp,
    imageColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        colors = colors,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Image",
                    modifier = Modifier.size(imageSize),
                    colorFilter = ColorFilter.tint(imageColor)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = text,
                    color = textColor,
                    style = fontStyle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ImageButtonPreview() {
    ComposeTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ImageVerticalButton(
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp),
                text = "image button",
                image = R.drawable.lost,
            ) {}
            ImageHorizontalButton(
                modifier = Modifier
                    .width(150.dp)
                    .height(60.dp),
                text = "image button",
                image = R.drawable.lost,
            ) {}
        }
    }
}