package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.card.Card
import com.jiagu.jgcompose.card.CardFrame
import com.jiagu.jgcompose.card.CardFrameContentRow
import com.jiagu.jgcompose.card.CardFrameSwitchButtonRow
import com.jiagu.jgcompose.card.CardFrameTitleButtonRow
import com.jiagu.jgcompose.card.CardFrameTitleCounterRow
import com.jiagu.jgcompose.card.CardFrameTitleSliderCounterRow
import com.jiagu.jgcompose.card.ParameterCard
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllCardsPreview() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .width(240.dp)
                    .height(120.dp),
                image = R.drawable.lost,
                title = "Card",
                content = {
                    Text(text = "This is the content of the card.")
                }
            )
            CardFrame(modifier = Modifier.width(300.dp), title = "Card Frame", titleRightContent = {
                Button(onClick = {}) {
                    Text("Button")
                }
            }, content = {
                CardFrameContentRow(title = "Content", text = "Details")
                CardFrameSwitchButtonRow(title = "Switch", defaultChecked = true) {}
                CardFrameTitleButtonRow(title = "Title Button", text = "Click", onClick = {})
                CardFrameTitleSliderCounterRow(
                    title = "Slider:",
                    number = 50f,
                    step = 1f,
                    fraction = 0,
                    onValueChange = {},
                    onValueChangeFinished = {}
                )
                CardFrameTitleCounterRow(
                    title = "Counter",
                    min = 0f,
                    max = 100f,
                    number = 25f,
                    fraction = 0,
                    step = 1f,
                    onValueChange = {}
                )
            })
            ParameterCard(
                title = "Parameter", text = "Value", modifier = Modifier
                    .width(120.dp)
                    .height(60.dp)
            )
        }
    }
}
