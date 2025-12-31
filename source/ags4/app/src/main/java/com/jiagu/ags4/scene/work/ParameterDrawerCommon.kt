package com.jiagu.ags4.scene.work

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.TemplateParam
import com.jiagu.ags4.ui.theme.buttonDisabled
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.button.GroupImageButton
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.InputPopup
import com.jiagu.jgcompose.popup.ListSelectionPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.ConverterPair

@Composable
fun TemplateSelect(
    templateName: String,
    enabled: Boolean = true,
    onClick: ((List<TemplateParam>) -> Unit) -> Unit,
    onConfirm: (TemplateParam) -> Unit,
    onSave: (String) -> Unit,
    onDeleteTemplate: (Long, () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.height(30.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    color = if (enabled) Color.White else buttonDisabled,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .clickable(enabled = enabled) {
                    onClick { templateParamList ->
                        context.showDialog {
                            val dataList = remember { templateParamList.toMutableStateList() }
                            ListSelectionPopup(
                                defaultIndexes = listOf(),
                                list = dataList,
                                item = { templateParam ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            AutoScrollingText(
                                                text = templateParam.name,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.clickable {
                                                onDeleteTemplate(templateParam.id) {
                                                    dataList.remove(templateParam)
                                                }
                                            })
                                    }
                                },
                                onConfirm = { idx, value ->
                                    if (value.isNotEmpty()) {
                                        onConfirm(value[0])
                                    }
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    context.hideDialog()
                                })
                        }

                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AutoScrollingText(
                    text = templateName,
                    color = if (enabled) Color.Black else Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (enabled) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
            }
        }
        //另存为
        if (enabled) {
            Button(
                onClick = {
                    context.showDialog {
                        InputPopup(
                            title = stringResource(R.string.save_job_parameter_template),
                            hint = stringResource(id = R.string.template_name),
                            onConfirm = {
                                onSave(it)
                                context.hideDialog()
                            },
                            onDismiss = {
                                context.hideDialog()
                            })
                    }
                },
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.extraSmall,
                contentPadding = PaddingValues(0.dp)
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.save_as),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

//参数抽屉行文本
@Composable
fun ParameterDrawerGlobalRowText(
    modifier: Modifier = Modifier,
    text: String,
    width: Dp = Dp.Infinity,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Color.Black,
) {
    AutoScrollingText(
        text = text,
        width = width,
        modifier = modifier,
        style = style,
        textAlign = textAlign,
        color = color
    )
}

/**
 * 计数器行
 */
@Composable
fun ParameterDrawerCounterRow(
    title: String? = null,
    content: String = "",
    min: Float = 0f,
    max: Float = 100f,
    step: Float = 1f,
    fraction: Int = 1,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    defaultNumber: Float = min,
    editEnabled: Boolean = true,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    converter: ConverterPair? = null,
    forceStep: Boolean = false,
    onChange: (Float) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart
        ) {
            ParameterDrawerTextRow(
                textColor = textColor,
                title = title ?: "",
                content = content,
                style = style,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp), contentAlignment = Alignment.Center
        ) {
            FloatCounter(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.extraSmall
                    ),
                number = defaultNumber,
                min = min,
                max = max,
                step = step,
                fraction = fraction,
                enabled = editEnabled,
                converterPair = converter,
                forceStep = forceStep
            ) {
                onChange(it)
            }
        }
    }
}

/**
 * 单选按钮行
 */
@Composable
fun ParameterDrawerGroupButtonRow(
    title: String,
    defaultNumber: Int = 0,
    names: List<String>,
    values: List<Int>,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    enabled: Boolean = true,
    onChange: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart
        ) {
            ParameterDrawerGlobalRowText(
                text = title,
                style = style,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp), contentAlignment = Alignment.Center
        ) {
            GroupButton(
                modifier = Modifier.fillMaxSize(),
                items = names,
                indexes = values,
                number = defaultNumber,
                enabled=enabled,
                onClick = { idx, _ ->
                    onChange(idx)
                })
        }
    }
}

/**
 * 单选按钮图片行
 */
@Composable
fun ParameterDrawerGroupImageButtonRow(
    title: Int,
    defaultNumber: Int = 0,
    names: List<String>,
    indexes: List<Int>,
    images: List<Int>,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    onChange: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            ParameterDrawerGlobalRowText(
                text = stringResource(id = title),
                style = style,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            GroupImageButton(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                indexes = indexes,
                items = names,
                number = defaultNumber,
                images = images,
            ) { idx, _ ->
                onChange(idx)
            }
        }
    }
}

/**
 * 文本按钮parameterDrawer
 */
@Composable
fun ParameterDrawerTextButton(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        ParameterDrawerGlobalRowText(
            text = text,
            style = style,
            modifier = Modifier.fillMaxWidth(),
            textAlign = textAlign,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * 普通文本行
 */
@Composable
fun ParameterDrawerTextRow(
    modifier: Modifier = Modifier,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: String = "",
    style: TextStyle = MaterialTheme.typography.titleSmall,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            ParameterDrawerGlobalRowText(
                text = title,
                style = style,
                modifier = Modifier.fillMaxWidth(),
                color = textColor
            )
        }
        if (content != "") {
            Box(
                modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center
            ) {
                ParameterDrawerGlobalRowText(
                    text = content,
                    style = style,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    color = textColor
                )
            }
        }
    }
}