package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.button.BackButton
import com.jiagu.jgcompose.button.ComboImageListButton
import com.jiagu.jgcompose.button.ComboListButton
import com.jiagu.jgcompose.button.CountdownButton
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.button.GroupButtons
import com.jiagu.jgcompose.button.GroupImageButton
import com.jiagu.jgcompose.button.ImageHorizontalButton
import com.jiagu.jgcompose.button.ImageVerticalButton
import com.jiagu.jgcompose.button.RadioButton
import com.jiagu.jgcompose.button.SliderSwitchButton
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllButtonsPreview() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.size(40.dp)) {
                BackButton() {}
            }

            ComboListButton(
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp),
                index = 0,
                value = "ListButton",
                items = listOf("Item 1", "Item 2", "Item 3"),
                onConfirm = { _ -> },
            )
            ComboImageListButton(
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp),
                index = 1,
                value = "ImageButton",
                items = listOf("Item 1", "Item 2", "Item 3"),
                images = listOf(R.drawable.eye_close, R.drawable.lost, R.drawable.eye_open),
                onConfirm = { _ -> },
            )
            CountdownButton(
                modifier = Modifier.size(100.dp, 30.dp),
                title = "Countdown",
                waitTime = 5f,
                onClick = {}
            )
            GroupButton(
                modifier = Modifier
                    .width(200.dp)
                    .height(40.dp),
                items = listOf("A", "B", "C"),
                indexes = listOf(0, 1, 2),
                number = 0,
            ) { _, _ ->
            }
            GroupButtons(
                modifier = Modifier
                    .width(200.dp)
                    .height(40.dp),
                items = listOf("A", "B", "C"),
                indexes = listOf(0, 1, 2),
                numbers = listOf(0, 1),
            ) {}
            GroupImageButton(
                modifier = Modifier
                    .width(200.dp)
                    .height(60.dp),
                items = listOf("Img 1", "Img 2", "Img 3"),
                indexes = listOf(0, 1, 2),
                number = 0,
                images = listOf(R.drawable.lost, R.drawable.lost, R.drawable.lost)
            ) { _, _ ->
            }
            ImageVerticalButton(
                modifier = Modifier
                    .width(100.dp)
                    .height(60.dp),
                text = "Vertical",
                image = R.drawable.lost,
            ) {}
            ImageHorizontalButton(
                modifier = Modifier
                    .width(150.dp)
                    .height(60.dp),
                text = "Horizontal",
                image = R.drawable.lost,
            ) {}
            Column {
                RadioButton(isSelected = true) {}
                RadioButton(isSelected = false) {}
            }
            SliderSwitchButton(
                sliderWidth = 150.dp, height = 30.dp, buttonName = "Slider", buttonWidth = 60.dp
            ) {}
            SwitchButton(
                defaultChecked = true,
            ) {}
            TopBarBottom(text = "TopBar") {}
        }
    }
}
