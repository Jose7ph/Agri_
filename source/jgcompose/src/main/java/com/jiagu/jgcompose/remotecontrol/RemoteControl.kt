package com.jiagu.jgcompose.remotecontrol

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.rocker.Rocker
import com.jiagu.jgcompose.rocker.RockerText
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * Rocker type enum
 * 注意：需要跟Drone中
 * OPER_JAPAN = 0
 * OPER_US = 1
 * OPER_CN = 2
 * 值保持一致
 *
 * @property rockerType
 * @constructor Create empty Rocker type enum
 */
private enum class RockerTypeEnum(val rockerType: Int) {
    JP(0), US(1), CN(2)
}

/**
 * Remote control
 *
 * @param modifier 装饰器
 * @param rockerType 摇杆类型
 * @param rockerSize 摇杆大小
 * @param rockerValues 摇杆值
 * @param onRockerMode 点击摇杆模式
 * @param onCalibration 摇杆校准
 */
@Composable
fun RemoteControl(
    modifier: Modifier = Modifier,
    rockerType: Int,
    rockerSize: Dp = 120.dp,
    rockerValues: FloatArray?,
    onRockerMode: (Int) -> Unit,
    onCalibration: () -> Unit
) {
    val context = LocalContext.current
    //摇杆操作文本
    val rockerTexts = getRockerText(rockerType, context)
    Column {
        //摇杆模式
        RockerMode(rockerType = rockerType, onClick = onRockerMode)
        //摇杆图
        Box(
            modifier = modifier
                .border(
                    width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.medium
                )
                .background(Color.White, MaterialTheme.shapes.medium)
                .padding(vertical = 10.dp, horizontal = 30.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Rocker(
                    rockerTitle = "L",
                    rockerSize = rockerSize,
                    rockerText = rockerTexts[0],
                    rockerValues = rockerValues
                )
                Rocker(
                    rockerTitle = "R",
                    rockerSize = rockerSize,
                    rockerText = rockerTexts[1],
                    rockerValues = rockerValues,
                    isLeft = false
                )
            }
            Button(modifier = Modifier
                .padding(bottom = 10.dp)
                .width(120.dp)
                .height(40.dp)
                .align(Alignment.BottomCenter),
                contentPadding = PaddingValues(0.dp),
                shape = MaterialTheme.shapes.medium,
                onClick = {
                        onCalibration()
                }) {
                AutoScrollingText(
                    text = stringResource(id = R.string.rocker_calibration),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

/**
 * 摇杆模式
 *
 */
@Composable
private fun RockerMode(rockerType: Int, onClick: (Int) -> Unit) {
    //顺序不能变
    val rockerModes = listOf(
        stringResource(id = R.string.hand_jp),
        stringResource(id = R.string.hand_us),
        stringResource(id = R.string.hand_cn)
    )
    val indexes = listOf(
        RockerTypeEnum.JP.rockerType,
        RockerTypeEnum.US.rockerType,
        RockerTypeEnum.CN.rockerType,
    )
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = R.string.rocker_mode) + ":",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray
            )
        }
        GroupButton(
            modifier = Modifier
                .weight(1f)
                .height(30.dp),
            items = rockerModes,
            number = rockerType,
            indexes = indexes
        ) { index, _ ->
            onClick(index)
        }
    }
}


/**
 * 摇杆操作文本
 *
 * @param rockerType 摇杆类型
 * @param context
 * @return
 */
private fun getRockerText(rockerType: Int, context: Context): List<RockerText> {
    return when (rockerType) {
        RockerTypeEnum.JP.rockerType -> {
            val leftRockerText = RockerText(
                left = context.getString(R.string.turn_left),
                right = context.getString(R.string.turn_right),
                top = context.getString(R.string.forward),
                bottom = context.getString(R.string.back)
            )
            val rightRockerText = RockerText(
                left = context.getString(R.string.shift_left),
                right = context.getString(R.string.shift_right),
                top = context.getString(R.string.rise),
                bottom = context.getString(R.string.descend)
            )
            listOf(leftRockerText, rightRockerText)
        }

        RockerTypeEnum.CN.rockerType -> {
            val leftRockerText = RockerText(
                left = context.getString(R.string.shift_left),
                right = context.getString(R.string.shift_right),
                top = context.getString(R.string.forward),
                bottom = context.getString(R.string.back)
            )
            val rightRockerText = RockerText(
                left = context.getString(R.string.turn_left),
                right = context.getString(R.string.turn_right),
                top = context.getString(R.string.rise),
                bottom = context.getString(R.string.descend)
            )
            listOf(leftRockerText, rightRockerText)
        }

        RockerTypeEnum.US.rockerType -> {
            val leftRockerText = RockerText(
                left = context.getString(R.string.turn_left),
                right = context.getString(R.string.turn_right),
                top = context.getString(R.string.rise),
                bottom = context.getString(R.string.descend)
            )
            val rightRockerText = RockerText(
                left = context.getString(R.string.shift_left),
                right = context.getString(R.string.shift_right),
                top = context.getString(R.string.forward),
                bottom = context.getString(R.string.back)
            )
            listOf(leftRockerText, rightRockerText)
        }

        else -> {
            listOf(
                RockerText("L", "R", "T", "B"), RockerText("L", "R", "T", "B")
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun RemoteControlPreview() {
    ComposeTheme {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
        ) {
            RemoteControl(
                modifier = Modifier.fillMaxSize(), rockerType = RockerTypeEnum.JP.rockerType,
                onRockerMode = {}, onCalibration = {}, rockerValues = null
            )
        }
    }
}