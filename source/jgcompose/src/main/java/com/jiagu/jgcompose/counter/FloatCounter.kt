package com.jiagu.jgcompose.counter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.longPressListener
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.theme.LocalExtendedColors
import com.jiagu.jgcompose.utils.ImeVisible
import com.jiagu.jgcompose.utils.Validator
import com.jiagu.jgcompose.utils.toString
import kotlin.math.roundToInt

enum class ClickStatusEnum {
    TAP_START, TAP_END,
}

/**
 * 单位转换
 *
 * @param1 转换方法 用于显示数据
 * @param2 还原方法 用于返回数据
 */
typealias ConverterPair = Pair<(Float) -> String, (String) -> Float>

/**
 * float counter
 *
 * @param modifier
 * @param number 当前值
 * @param min 最小值
 * @param max 最大值
 * @param step 步幅
 * @param fraction 小数位数 若不保留小数则传0
 * @param enabled 是否可操作
 * @param textColor 文本色
 * @param converterPair 单位转换
 * @param forceStep 数值根据step强制进行转换 默认否
 * @param onValueChange 值变化回调
 */
@Composable
fun FloatCounter(
    modifier: Modifier = Modifier,
    number: Float,
    min: Float,
    max: Float,
    step: Float,
    fraction: Int,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    converterPair: ConverterPair? = null,
    forceStep: Boolean = false,
    onValueChange: (Float) -> Unit
) {
    val converterMin by remember {
        mutableFloatStateOf(converterPair?.first?.invoke(min)?.replace(",", ".")?.toFloat() ?: 0f)
    }
    val converterMax by remember {
        mutableFloatStateOf(converterPair?.first?.invoke(max)?.replace(",", ".")?.toFloat() ?: 0f)
    }
    val converterFlag by remember {
        mutableStateOf(converterPair != null)
    }
    var converterText by remember {
        mutableStateOf(converterPair?.first?.invoke(number) ?: "0")
    }

    //文本值
    var text by remember {
        mutableStateOf(number.toString(fraction))
    }
    //减号按钮状态
    var minusEnabled by remember {
        mutableStateOf(number > min && enabled)
    }
    //加号按钮状态
    var plusEnabled by remember {
        mutableStateOf(number < max && enabled)
    }
    //软键盘状态
    var imeState by remember {
        mutableStateOf(false)
    }
    //用于处理当counter按钮点击后变成最小值，Enabled变成false从而同时触发两次onValueChange回调的情况
    var onButtonClickValueChangeFlag by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(onButtonClickValueChangeFlag) {
        if (onButtonClickValueChangeFlag) {
            onButtonClickValueChangeFlag = false
        }
    }
    //用于处理当父组件值与组件内值不一致时，统一更新组件数据，值以父组件为准
    LaunchedEffect(key1 = number) {
        if (text.replace(",", ".").toFloat() != number) {
//            Log.d("zhy", "number not same...............: ")
            text = number.toString(fraction)
            if (converterFlag) converterText = converterPair?.first?.invoke(number) ?: "0"
            minusEnabled = number > min && enabled
            plusEnabled = number < max && enabled
        }
    }
    //软键盘状态为关闭(false)时，触发数据更新，用于处理通过状态栏关闭和正常关闭软键盘的情况
    ImeVisible { isVisible ->
        //因为当组件更新时 ImeVisible，也会重组触发回调，为了防止数据更新频繁增加ime的状态比对
        //若当前记录的状态跟ime的状态一直则不进行更新操作
        if (imeState == isVisible) {
            return@ImeVisible
        }
        //状态不一致说明当前打开/关闭了软键盘，将当前的状态记录到imeState中
        imeState = isVisible
        //imeState关闭，开始处理数据
        if (!imeState) {
            //有单位转换的情况
            if (converterFlag) {
                //原始值
                val v = converterPair?.first?.invoke(number)?.replace(",", ".")?.toFloat()
                //格式校验
                //空字符串显示最小值
                if (text.isEmpty()) {
                    converterText = converterMin.toString(fraction)
                }
                //校验数值格式是否正确
                if (!Validator.checkNumerical(converterText)) {
                    converterText = number.toString(fraction)
                }
                //处理 ， 如葡萄牙语
                var convertNum = converterText.replace(",", ".").toFloat()
                //当前值格式化之后跟原始值比较 数据不一致则触发回调
                if (v != convertNum) {
//                    Log.d("zhy", "convert change............")
                    //按钮状态处理
                    convertNum = when {
                        //current <= min,加号可以点击，减号禁点
                        convertNum <= converterMin -> {
                            minusEnabled = false
                            plusEnabled = true
                            converterMin
                        }
                        //current <= min,减号可以点击，加号禁点
                        convertNum >= converterMax -> {
                            minusEnabled = true
                            plusEnabled = false
                            converterMax
                        }
                        //current 不变,加号可以点击，减号可以点击
                        else -> {
                            minusEnabled = true
                            plusEnabled = true
                            convertNum
                        }
                    }
                    converterText = convertNum.toString(fraction)
                    val num = converterPair?.second?.invoke(converterText.replace(",", "."))
                    onValueChange(num!!)
                }
            } else {//无单位转换的情况
                //格式校验
                //空字符串显示最小值
                if (text.isEmpty()) {
                    text = min.toString(fraction)
                }
                //校验数值格式是否正确
                if (!Validator.checkNumerical(text)) {
                    text = number.toString(fraction)
                }
                //处理 ， 如葡萄牙语
                var num = text.replace(",", ".").toFloat()
                //当前值格式化之后跟原始值比较 数据不一致则触发回调
                if (number != num) {
//                    Log.d("zhy", "change............")
                    //数值范围校验
                    //按钮状态处理
                    num = when {
                        //current <= min,加号可以点击，减号禁点
                        num <= min -> {
                            minusEnabled = false
                            plusEnabled = true
                            min
                        }
                        //current <= min,减号可以点击，加号禁点
                        num >= max -> {
                            minusEnabled = true
                            plusEnabled = false
                            max
                        }
                        //current 不变,加号可以点击，减号可以点击
                        else -> {
                            minusEnabled = true
                            plusEnabled = true
                            num
                        }
                    }
                    //修改当前组件上显示的文本
                    text = num.toString(fraction)
                    //触发回调方法
                    onValueChange(num)
                }
            }
        }
    }

    Row(modifier = modifier) {
        CounterButton(
            image = R.drawable.minus, enabled = enabled && minusEnabled
        ) { clickType ->
            //处理 ， 如葡萄牙语
            var num = text.replace(",", ".")
                .ifEmpty { "0" }
                .toFloat()
            when (clickType) {
                //长按中数据修改，同时判断按钮状态，current <= min时，修改按钮状态为false，同时直接触发更新回调
                ClickStatusEnum.TAP_START -> {
                    num -= step
                    if (forceStep) {
                        num = ((num * 2.0).roundToInt() / 2.0).toFloat()
                    }
                    //处理值越界情况
                    if (num < min) {
                        num = min
                    }
                    text = num.toString(fraction)
                    if (converterFlag) {
                        converterText = converterPair!!.first.invoke(num)
                    }
                    //按钮状态判断
                    if (num <= min) {
                        minusEnabled = false
                    } else {
                        minusEnabled = true
                        plusEnabled = true
                    }
                    //长按时，数据到达min、max时，按钮状态false之后，不会获取到长按松开的回调，
                    //当按钮状态变化为false时，直接触发回调更新数据
                    if (!minusEnabled) {
//                        Log.d("zhy", " mius TAP_Start: change:${num}")
                        if (!onButtonClickValueChangeFlag) {
                            onValueChange(num)
                            onButtonClickValueChangeFlag = true
                        }
                    }
                }
                //松开触发(必定会触发)
                ClickStatusEnum.TAP_END -> {
//                    Log.d("zhy", " mius TAP_END: change:${num}")
                    if (!onButtonClickValueChangeFlag) {
                        onValueChange(num)
                        onButtonClickValueChangeFlag = true
                    }
                }
            }
        }
        NormalTextField(
            modifier = Modifier.weight(1f),
            text = if (converterFlag) converterText else text,
            onValueChange = {
                if (converterFlag) converterText = it else text = it
            },
            enabled = enabled,
            showClearIcon = false,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
            borderColor = Color.Transparent,
            textStyle = textStyle.copy(
                textAlign = TextAlign.Center
            ),
        )
        CounterButton(
            image = R.drawable.plus, enabled = plusEnabled && enabled, isMinus = false
        ) { clickType ->
            //处理 ， 如葡萄牙语
            var num = text.replace(",", ".")
                .ifEmpty { "0" }
                .toFloat()
            when (clickType) {
                //长按中数据修改，同时判断按钮状态，current <= min时，修改按钮状态为false，同时直接触发更新回调
                ClickStatusEnum.TAP_START -> {
                    num += step
                    if (forceStep) {
                        num = ((num * 2.0).roundToInt() / 2.0).toFloat()
                    }
                    //处理值越界情况
                    if (num > max) {
                        num = max
                    }
                    text = num.toString(fraction)
                    if (converterFlag) {
                        converterText = converterPair!!.first.invoke(num)
                    }
                    //按钮状态判断
                    if (num >= max) {
                        plusEnabled = false
                    } else {
                        minusEnabled = true
                        plusEnabled = true
                    }
                    //长按时，数据到达min、max时，按钮状态false之后，不会获取到长按松开的回调，
                    //当按钮状态变化为false时，直接触发回调更新数据
                    if (!plusEnabled) {
//                        Log.d("zhy", " plus TAP_start: change:${num}")
                        if (!onButtonClickValueChangeFlag) {
                            onValueChange(num)
                            onButtonClickValueChangeFlag = true
                        }
                    }
                }
                //松开触发(必定会触发)
                ClickStatusEnum.TAP_END -> {
//                    Log.d("zhy", " plus TAP_END: change:${num}")
                    if (!onButtonClickValueChangeFlag) {
                        onValueChange(num)
                        onButtonClickValueChangeFlag = true
                    }
                }
            }

        }
    }
}

/**
 * 在float counter 技术上增加值变更弹窗提示
 *
 * @param modifier
 * @param number 当前值
 * @param min 最小值
 * @param max 最大值
 * @param step 步幅
 * @param fraction 小数位数 若不保留小数则传0
 * @param enabled 是否可操作
 * @param textColor 文本色
 * @param converterPair 单位转换
 * @param forceStep 数值根据step强制进行转换 默认否
 * @param onConfirm 确定回调
 * @param onDismiss 取消回调
 */
@Composable
fun FloatChangeAskCounter(
    modifier: Modifier = Modifier,
    number: Float,
    min: Float,
    max: Float,
    step: Float,
    fraction: Int,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    converterPair: ConverterPair? = null,
    forceStep: Boolean = false,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit = {},
) {
    val context = LocalContext.current
    //原值 用于还原数据用
    var oldValue by remember {
        mutableFloatStateOf(number)
    }

    val converterMin by remember {
        mutableFloatStateOf(converterPair?.first?.invoke(min)?.replace(",", ".")?.toFloat() ?: 0f)
    }
    val converterMax by remember {
        mutableFloatStateOf(converterPair?.first?.invoke(max)?.replace(",", ".")?.toFloat() ?: 0f)
    }
    val converterFlag by remember {
        mutableStateOf(converterPair != null)
    }
    var converterText by remember {
        mutableStateOf(converterPair?.first?.invoke(number) ?: "0")
    }
    //文本值
    var text by remember {
        mutableStateOf(number.toString(fraction))
    }
    //减号按钮状态
    var minusEnabled by remember {
        mutableStateOf(number > min && enabled)
    }
    //加号按钮状态
    var plusEnabled by remember {
        mutableStateOf(number < max && enabled)
    }
    //软键盘状态
    var imeState by remember {
        mutableStateOf(false)
    }
    //值修改弹窗 num：最新值
    val showAsk = { num: Float ->
        context.showDialog {
            PromptPopup(content = stringResource(id = R.string.confirm_change), onConfirm = {
                oldValue = num
                onConfirm(num)
                when {
                    num <= min -> {
                        minusEnabled = false
                        plusEnabled = true
                    }

                    num >= max -> {
                        minusEnabled = true
                        plusEnabled = false
                    }

                    else -> {
                        minusEnabled = true
                        plusEnabled = true
                    }
                }
                context.hideDialog()
            }, onDismiss = {
                text = oldValue.toString(fraction)
                converterText = converterPair?.first?.invoke(oldValue) ?: "0"
                onDismiss()
                when {
                    oldValue <= min -> {
                        minusEnabled = false
                        plusEnabled = true
                    }

                    oldValue >= max -> {
                        minusEnabled = true
                        plusEnabled = false
                    }

                    else -> {
                        minusEnabled = true
                        plusEnabled = true
                    }
                }
                context.hideDialog()
            })
        }
    }
    //用于处理当父组件值与组件内值不一致时，统一更新组件数据，值以父组件为准
    LaunchedEffect(key1 = number) {
        if (text.replace(",", ".").toFloat() != number) {
            oldValue = number
            text = number.toString(fraction)
            if (converterFlag) converterText = converterPair?.first?.invoke(number) ?: "0"
            minusEnabled = number > min && enabled
            plusEnabled = number < max && enabled
        }
    }
    //软键盘状态为关闭(false)时，触发数据更新，用于处理通过状态栏关闭和正常关闭软键盘的情况
    ImeVisible { isVisible ->
        //因为当组件更新时 ImeVisible，也会重组触发回调，为了防止数据更新频繁增加ime的状态比对
        //若当前记录的状态跟ime的状态一直则不进行更新操作
        if (imeState == isVisible) {
            return@ImeVisible
        }
        //状态不一致说明当前打开/关闭了软键盘，将当前的状态记录到imeState中
        imeState = isVisible
        //imeState关闭，开始处理数据
        if (!imeState) {
            //有单位转换的情况
            if (converterFlag) {
                //原始值
                val v = converterPair?.first?.invoke(number)?.replace(",", ".")?.toFloat()
                //格式校验
                //空字符串显示最小值
                if (text.isEmpty()) {
                    converterText = converterMin.toString(fraction)
                }
                //校验数值格式是否正确
                if (!Validator.checkNumerical(converterText)) {
                    converterText = number.toString(fraction)
                }
                //处理 ， 如葡萄牙语
                var convertNum = converterText.replace(",", ".").toFloat()
                //当前值格式化之后跟原始值比较 数据不一致则触发回调
                if (v != convertNum) {
                    //按钮状态处理
                    convertNum = when {
                        //current <= min,加号可以点击，减号禁点
                        convertNum <= converterMin -> {
                            converterMin
                        }
                        //current <= min,减号可以点击，加号禁点
                        convertNum >= converterMax -> {
                            converterMax
                        }
                        //current 不变,加号可以点击，减号可以点击
                        else -> {
                            convertNum
                        }
                    }
                    converterText = convertNum.toString(fraction)
                    val num =
                        converterPair?.second?.invoke(converterText.replace(",", ".")) ?: 0f
                    showAsk(num)
                }
            } else {//无单位转换的情况
                //格式校验
                //空字符串显示最小值
                if (text.isEmpty()) {
                    text = min.toString(fraction)
                }
                //校验数值格式是否正确
                if (!Validator.checkNumerical(text)) {
                    text = number.toString(fraction)
                }
                //处理 ， 如葡萄牙语
                var num = text.replace(",", ".").toFloat()
                //当前值格式化之后跟原始值比较 数据不一致则触发回调
                if (number != num) {
                    //数值范围校验
                    //按钮状态处理
                    num = when {
                        //current <= min,加号可以点击，减号禁点
                        num <= min -> {
                            min
                        }
                        //current <= min,减号可以点击，加号禁点
                        num >= max -> {
                            max
                        }
                        //current 不变,加号可以点击，减号可以点击
                        else -> {
                            num
                        }
                    }
                    //修改当前组件上显示的文本
                    text = num.toString(fraction)
                    //触发回调方法
                    showAsk(num)
                }
            }
        }
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CounterButton(
            image = R.drawable.minus, enabled = enabled && minusEnabled
        ) { clickType ->
            //处理 ， 如葡萄牙语
            var num = text.replace(",", ".").toFloat()
            when (clickType) {
                //长按中数据修改，同时判断按钮状态，current <= min时，修改按钮状态为false，同时直接触发更新回调
                ClickStatusEnum.TAP_START -> {
                    num -= step
                    if (forceStep) {
                        num = (Math.round(num * 2.0) / 2.0).toFloat()
                    }
                    text = num.toString(fraction)
                    if (converterFlag) {
                        converterText = converterPair!!.first.invoke(num)
                    }
                    //按钮状态判断
                    if (num <= min) {
                        minusEnabled = false
                    } else {
                        minusEnabled = true
                        plusEnabled = true
                    }
                    //长按时，数据到达min、max时，按钮状态false之后，不会获取到长按松开的回调，
                    //当按钮状态变化为false时，直接触发回调更新数据
                    if (!minusEnabled) {
                        showAsk(num)
                    }
                }
                //松开触发(必定会触发)
                ClickStatusEnum.TAP_END -> {
                    showAsk(num)
                }
            }
        }
        NormalTextField(
            modifier = Modifier.weight(1f),
            text = if (converterFlag) converterText else text,
            onValueChange = {
                if (converterFlag) converterText = it else text = it
            },
            enabled = enabled,
            showClearIcon = false,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
            borderColor = Color.Transparent,
            textStyle = textStyle.copy(
                textAlign = TextAlign.Center
            )
        )
        CounterButton(
            image = R.drawable.plus, enabled = enabled && plusEnabled, isMinus = false
        ) { clickType ->
            //处理 ， 如葡萄牙语
            var num = text.replace(",", ".").toFloat()
            when (clickType) {
                //长按中数据修改，同时判断按钮状态，current <= min时，修改按钮状态为false，同时直接触发更新回调
                ClickStatusEnum.TAP_START -> {
                    num += step
                    if (forceStep) {
                        num = (Math.round(num * 2.0) / 2.0).toFloat()
                    }
                    text = num.toString(fraction)
                    //处理值越界情况
                    if (num > max) {
                        num = max
                    }
                    if (converterFlag) {
                        converterText = converterPair!!.first.invoke(num)
                    }
                    //按钮状态判断
                    if (num >= max) {
                        plusEnabled = false
                    } else {
                        minusEnabled = true
                        plusEnabled = true
                    }
                    //长按时，数据到达min、max时，按钮状态false之后，不会获取到长按松开的回调，
                    //当按钮状态变化为false时，直接触发回调更新数据
                    if (!plusEnabled) {
                        showAsk(num)
                    }
                }
                //松开触发(必定会触发)
                ClickStatusEnum.TAP_END -> {
                    showAsk(num)
                }
            }

        }
    }
}

/**
 * Counter button
 *
 * @param image 按钮图片
 * @param enabled 按钮状态
 * @param isMinus 是否减号 用于控制shape
 * @param onClick 点击事件
 */
@Composable
private fun CounterButton(
    image: Int,
    enabled: Boolean = true,
    isMinus: Boolean = true,
    onClick: (ClickStatusEnum) -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val buttonColor =
        if (enabled) MaterialTheme.colorScheme.primary else extendedColors.buttonDisabled
    Box(modifier = Modifier
        .fillMaxHeight()
        .background(
            color = buttonColor, shape = if (isMinus) RoundedCornerShape(
                topStart = 4.dp, bottomStart = 4.dp
            ) else RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
        )
        .then(if (enabled) {
            Modifier.pointerInput(Unit) {
                longPressListener(progress = {
                    onClick(ClickStatusEnum.TAP_START)
                }, done = {
                    onClick(ClickStatusEnum.TAP_END)
                })
            }
        } else {
            Modifier
        })) {
        Image(
            painter = painterResource(id = image), contentDescription = "symbol"
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun FloatCounterPreview() {
    ComposeTheme {
        Column() {
            var num by remember {
                mutableStateOf(100f)
            }
            FloatCounter(
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp), //建议宽度至少=4*height
                number = num,
                min = 0f,
                max = 20f,
                step = 1f,
                fraction = 1,
            ) {
                num = it
            }
        }
    }
}