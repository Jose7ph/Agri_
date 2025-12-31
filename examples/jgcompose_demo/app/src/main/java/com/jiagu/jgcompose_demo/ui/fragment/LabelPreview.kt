package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.label.ComboListButtonLabel
import com.jiagu.jgcompose.label.ComboRollButtonLabel
import com.jiagu.jgcompose.label.DateLabel
import com.jiagu.jgcompose.label.DateRangeLabel
import com.jiagu.jgcompose.label.InputSearchLabel
import com.jiagu.jgcompose.label.Label
import com.jiagu.jgcompose.label.RegionSelectionLabel
import com.jiagu.jgcompose.picker.Address
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllLabelsPreview() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ComboRollButtonLabel(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp),
                labelName = "Roll",
                labelWidth = 60.dp,
                comboIndex = 0,
                comboItems = listOf("Item 1", "Item 2", "Item 3"),
                comboValue = "Item 1",
                onConfirm = { _, _ -> },
                onCancel = {}
            )
            ComboListButtonLabel(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp),
                labelName = "List",
                labelWidth = 60.dp,
                comboIndex = 0,
                comboItems = listOf("Item 1", "Item 2", "Item 3"),
                comboValue = "Item 1",
                onConfirm = {},
                onCancel = {}
            )
            DateLabel(
                modifier = Modifier
                    .width(200.dp)
                    .height(30.dp),
                labelWidth = 60.dp,
                labelName = "Date",
                onConfirm = {}
            )
            DateRangeLabel(
                modifier = Modifier
                    .width(300.dp)
                    .height(30.dp),
                labelWidth = 80.dp,
                labelName = "Date Range",
                onConfirm = { _, _, _, _, _ -> }
            )
            InputSearchLabel(
                modifier = Modifier
                    .width(250.dp)
                    .height(30.dp),
                labelName = "Search",
                labelWidth = 80.dp,
                onSearch = {},
                onInputChange = {}
            )
            Label(
                modifier = Modifier.height(30.dp),
                labelName = "Basic",
                labelWidth = 60.dp
            ) {}
            RegionSelectionLabel(
                modifier = Modifier
                    .width(250.dp)
                    .height(30.dp),
                labelWidth = 80.dp,
                labelName = "Region",
                regions = listOf(Address(1, "Region 1"), Address(2, "Region 2")),
                onConfirm = {}
            )
        }
    }
}
