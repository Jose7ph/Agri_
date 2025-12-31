package com.jiagu.ags4.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.ComposeTheme

var enhancedModeLeftIconColor = Color(0xFFFF8000)
var enhancedModeLeftBackgroundColor = Color(0x78FF8000)

var enhancedModeRightIconColor = Color(0xFF0080FF)
var enhancedModeRightBackgroundColor = Color(0x780080FF)

@Composable
fun TranslationIcon(
    modifier: Modifier = Modifier,
    isLeft: Boolean = true,
) {
    Row(modifier = modifier.background(color = if(isLeft) enhancedModeLeftBackgroundColor else enhancedModeRightBackgroundColor),
        verticalAlignment = Alignment.CenterVertically) {
        if (isLeft) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_work_ab_left2),
                    contentDescription = "left arrow",
                    colorFilter = ColorFilter.tint(color = enhancedModeLeftIconColor),
                    modifier = Modifier.fillMaxHeight(0.7f)
                )
            }
            VerticalDivider(
                thickness = 3.dp, modifier = Modifier
                    .fillMaxHeight(),
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Spacer(modifier = Modifier.weight(1f))
            VerticalDivider(
                thickness = 3.dp, modifier = Modifier
                    .fillMaxHeight(),
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_work_ab_right2),
                    contentDescription = "right arrow",
                    colorFilter = ColorFilter.tint(color = enhancedModeRightIconColor),
                    modifier = Modifier.fillMaxHeight(0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun TranslationIconPreview() {
    ComposeTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TranslationIcon(
                modifier = Modifier
                    .width(120.dp)
                    .height(60.dp),
            )
            TranslationIcon(
                modifier = Modifier
                    .width(120.dp)
                    .height(60.dp),
                isLeft = false
            )
        }
    }
}