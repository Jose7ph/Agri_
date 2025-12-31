package com.jiagu.ags4.scene.mine

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.work.RightButtonCommon
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.SIMPLE_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.api.ext.millisToDate
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

/**
 * 地块详情
 */
@Composable
fun LandBlockDetails(finish: () -> Unit = {}) {
    val context = LocalContext.current
    val activity = LocalActivity.current as BlockDetailActivity
    val block by activity.curBlockPlan.collectAsState()
    val navController = LocalNavController.current
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp)
                .size(36.dp)
                .clip(shape = CircleShape)
                .align(Alignment.TopStart)
                .clickable { finish() },
            color = Color.White
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                modifier = Modifier
                    .size(36.dp)
                    .padding(vertical = 6.dp),
                contentDescription = null,
                tint = Color.Black
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp, top = SIMPLE_BAR_HEIGHT + 20.dp)
                .height(150.dp)
                .width(240.dp)
                .align(Alignment.TopStart)
                .background(BlackAlpha, shape = MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            val textSpaced = 20.dp
            val labelWidth = 80.dp
            val autoScrollingWidth = 120.dp
            val rowPadding = 20.dp
            Row(
                modifier = Modifier.padding(horizontal = rowPadding),
                horizontalArrangement = Arrangement.spacedBy(textSpaced),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BlockText(
                    text = stringResource(id = R.string.land_name),
                    width = labelWidth
                )
                BlockText(
                    text = block?.blockName ?: "",
                    width = autoScrollingWidth
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = rowPadding),
                horizontalArrangement = Arrangement.spacedBy(textSpaced),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BlockText(
                    text = stringResource(id = R.string.land_area),
                    width = labelWidth
                )
                BlockText(
                    text = UnitHelper.transAreaWithUnit(context, block?.area ?: 0f),
                    width = autoScrollingWidth
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = rowPadding),
                horizontalArrangement = Arrangement.spacedBy(textSpaced),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BlockText(
                    text = stringResource(id = R.string.land_position),
                    width = labelWidth
                )
                BlockText(
                    text = block?.regionName ?: "",
                    width = autoScrollingWidth
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = rowPadding),
                horizontalArrangement = Arrangement.spacedBy(textSpaced),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BlockText(
                    text = stringResource(id = R.string.land_create_time),
                    width = labelWidth
                )
                BlockText(
                    text = block?.createTime?.millisToDate("yyyy-MM-dd") ?: stringResource(
                        id = R.string.na
                    ),
                    width = autoScrollingWidth
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            RightButtonCommon(
                text = stringResource(id = R.string.edit_block)
            ) {
                navController.navigate(BlockDetailPageUrlEnum.BLOCK_EDIT.url)
            }
            RightButtonCommon(
                text = stringResource(id = R.string.block_slice)
            ) {
                navController.navigate(BlockDetailPageUrlEnum.BLOCK_DIVISION.url)
            }
        }
    }
}

@Composable
private fun BlockText(text: String, width: Dp) {
    AutoScrollingText(
        text = text,
        width = width,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimary
    )
}
