package com.jiagu.jgcompose_demo.ui.fragment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.rocker.DiagonalDirectionRocker
import com.jiagu.jgcompose.rocker.Rocker
import com.jiagu.jgcompose.rocker.RockerText
import com.jiagu.jgcompose.rocker.StraightDirectionRocker
import com.jiagu.jgcompose.theme.ComposeTheme

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun AllRockerPreviews() {
    ComposeTheme {
        FlowRow(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Basic rocker component preview
            Rocker(
                rockerText = RockerText("L", "R", "T", "B"),
                rockerTitle = "L",
                rockerSize = 160.dp,
                rockerValues = floatArrayOf(0.5f, -0.3f, 0.0f, 0.7f)
            )
            
            // Directional rocker preview
            Column(
                modifier = Modifier.size(210.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                StraightDirectionRocker(
                    modifier = Modifier.size(150.dp),
                    onTopClick = {},
                    onTopClickFinish = {},
                    onLeftClick = {},
                    onLeftClickFinish = {},
                    onRightClick = {},
                    onRightClickFinish = {},
                    onBottomClick = {},
                    onBottomClickFinish = {},
                )
            }
            
            // Diagonal direction rocker preview
            Column(
                modifier = Modifier.size(210.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DiagonalDirectionRocker(
                    modifier = Modifier.size(150.dp),
                    onTopClick = {},
                    onTopClickFinish = {},
                    onLeftClick = {},
                    onLeftClickFinish = {},
                    onRightClick = {},
                    onRightClickFinish = {},
                    onBottomClick = {},
                    onBottomClickFinish = {},
                    onTopLeftClick = {},
                    onTopLeftClickFinish = {},
                    onTopRightClick = {},
                    onTopRightClickFinish = {},
                    onBottomLeftClick = {},
                    onBottomLeftClickFinish = {},
                    onBottomRightClick = {},
                    onBottomRightClickFinish = {},
                )
            }
        }
    }
}