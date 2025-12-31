package com.jiagu.ags4.scene.factory

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.buttonDisabled
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ImagePromptPopup
import kotlin.math.ceil

data class ImageItem(
    val title: String, val imageRes: Int, val identifier: Int
)

/**
 * 机型设置
 */
@Composable
fun FactorySettingsModel() {
    val context = LocalContext.current
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val titles = stringArrayResource(id = R.array.advanced_setting_model_type_titles).toList()
    val model = aptypeData?.getIntValue(VKAg.APTYPE_MODEL) ?: -1
    val imageItems = listOf(
        ImageItem(titles[0], R.drawable.plane42, 42),
        ImageItem(titles[1], R.drawable.plane41, 41),
        ImageItem(titles[2], R.drawable.plane62, 62),
        ImageItem(titles[3], R.drawable.plane61, 61),
        ImageItem(titles[4], R.drawable.plane82, 82),
        ImageItem(titles[5], R.drawable.plane81, 81),
        ImageItem(titles[6], R.drawable.plane63, 63),
        ImageItem(titles[7], R.drawable.plane64, 64),
        ImageItem(titles[8], R.drawable.plane83, 83),
        ImageItem(titles[9], R.drawable.plane84, 84),
        ImageItem(titles[10], R.drawable.plane46, 65),
        ImageItem(titles[11], R.drawable.plane48, 86),
        ImageItem(titles[12], R.drawable.plane66, 66),
        ImageItem(titles[13], R.drawable.plane67, 67),
        ImageItem(titles[14], R.drawable.plane68, 85),
    )
    //一行显示个数
    val itemsPerRow = 5
    //总共多少行
    val totalRows = ceil(imageItems.size / itemsPerRow.toDouble()).toInt()
    //图片大小
    val imageSize = 70.dp
    val lagerImageSize = 180.dp
    val borderColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        for (rowIndex in 0..totalRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val start = rowIndex * itemsPerRow
                val end = minOf(start + itemsPerRow, imageItems.size)
                for (i in start until end) {
                    Box(
                        modifier = Modifier
                            .size(imageSize + 10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = MaterialTheme.shapes.medium
                            )

                            .border(
                                width = 2.dp,
                                color = if (imageItems[i].identifier == model) { MaterialTheme.colorScheme.primary } else Color.Unspecified,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clip(shape = MaterialTheme.shapes.medium)
                            .clickable {
                                context.showDialog {
                                    ImagePromptPopup(
                                        image = imageItems[i].imageRes,
                                        imageSize = lagerImageSize,
                                        content = buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontStyle = FontStyle.Normal
                                                )
                                            ) {
                                                append(imageItems[i].title)
                                            }
                                            append("\n")
                                            withStyle(SpanStyle(color = Color.Red)) {
                                                append(stringResource(id = R.string.click_model_tip))
                                            }
                                        },
                                        onConfirm = {
                                            sendParameter(
                                                VKAg.APTYPE_MODEL,
                                                imageItems[i].identifier.toFloat()
                                            )
                                            context.hideDialog()
                                        },
                                        onDismiss = { context.hideDialog() })
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        //小图标
                        Image(
                            painter = painterResource(id = imageItems[i].imageRes),
                            contentDescription = "image",
                            colorFilter = ColorFilter.tint(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                blendMode = BlendMode.Modulate
                            ),
                            modifier = Modifier.size(imageSize)
                        )
                        //选中角标
                        if (imageItems[i].identifier == model) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 20.dp, y = 20.dp)
                            ) {
                                Canvas(modifier = Modifier.size(40.dp)) {
                                    drawRoundRect(color = borderColor,
                                        cornerRadius = CornerRadius(50f))
                                }
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "check",
                                    modifier = Modifier
                                        .size(15.dp)
                                        .offset(x = 2.5.dp, y = 2.5.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    //若不满一行则填充空白
                    if (end - start < itemsPerRow) {
                        repeat(itemsPerRow - (end - start)) {
                            Spacer(modifier = Modifier)
                        }
                    }

                }
            }
        }
    }
}