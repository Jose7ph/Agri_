package com.jiagu.ags4.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.components.ImageButton
import com.jiagu.ags4.ui.components.SplitButton
import com.jiagu.ags4.ui.components.StatusTag
import com.jiagu.ags4.ui.components.TagType
import com.jiagu.jgcompose.button.GroupButton


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun Preview() {
    val textModifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ComposeTheme{
        Row(modifier = Modifier.padding(8.dp)) {
            Column {
                Text(text = "headlineSmall", style = typography.headlineSmall, modifier = textModifier, color = colorScheme.primary)
                Text(text = "titleLarge", style = typography.titleLarge, modifier = textModifier)
                Text(text = "titleMedium", style = typography.titleMedium, modifier = textModifier)
                Text(text = "titleSmall", style = typography.titleSmall, modifier = textModifier)
                Text(text = "bodyLarge", style = typography.bodyLarge, modifier = textModifier)
                Text(text = "bodyMedium", style = typography.bodyMedium, modifier = textModifier)
                Text(text = "bodySmall", style = typography.bodySmall, modifier = textModifier)
                Text(text = "labelLarge", style = typography.labelLarge, modifier = textModifier)
                Text(text = "labelMedium", style = typography.labelMedium, modifier = textModifier)
                Text(text = "labelSmall", style = typography.labelSmall, modifier = textModifier)
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    StatusTag(type = TagType.PRIMARY, title = "primary")
                    StatusTag(type = TagType.SECONDARY, title = "secondary")
                    StatusTag(type = TagType.TERTIARY, title = "tertiary")
                    StatusTag(type = TagType.ERROR, title = "error")
                }
                Row {
                    SplitButton(modifier = Modifier
                        .width(242.dp)
                        .background(color = BlackAlpha, shape = shapes.medium), leftButton = {
                        ImageButton(
                            title = "Button1",
                            image = R.drawable.default_lost,
                            onClick = {},
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(120.dp)
                                .height(60.dp)
                        )
                    }, rightButton = {
                        ImageButton(
                            title = "Button1",
                            image = R.drawable.default_lost,
                            onClick = {},
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(120.dp)
                                .height(60.dp)
                        )
                    })
                }
                Row {
                    ComposeTheme {
                        Surface(shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                            GroupButton(
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(48.dp),
                                items = listOf("item1", "item2", "item3"),
                                indexes = listOf(1, 2, 3),
                                number = 1
                            ) {_,_->

                            }
                        }

                    }
                }

                Row {
//                    BlockCard(modifier = Modifier
//                        .width(200.dp)
//                        .height(120.dp),
//                        Block(1, "地块1", listOf(), DoubleArray(0), 12.3f, 1L)
//                    )
                }

            }
        }
    }
}