package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.picker.ImageListPicker
import com.jiagu.jgcompose.picker.ListPicker
import com.jiagu.jgcompose.picker.RollPicker
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllPickersPreview() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // DatePicker and DateRangePicker are dialogs, so they are not suitable for a combined preview.
            // You can preview them individually in their respective files.

            ListPicker(
                rowItemNum = 3,
                selectedIndexes = listOf(0),
                items = listOf("Item 1", "Item 2", "Item 3"),
                onConfirm = {},
                onCancel = {}
            )

            ImageListPicker(
                rowItemNum = 3,
                selectedIndexes = listOf(0),
                items = listOf("Image 1", "Image 2", "Image 3"),
                images = listOf(R.drawable.lost, R.drawable.lost, R.drawable.lost),
                onConfirm = {},
                onCancel = {}
            )

            // ListRollPicker, ImageListRollPicker, and RegionPicker are also dialogs.

            RollPicker(
                value = "Item 2",
                onValueChange = { _, _ -> },
                list = listOf("Item 1", "Item 2", "Item 3"),
                item = { _, item ->
                    Text(text = item)
                }
            )
        }
    }
}
