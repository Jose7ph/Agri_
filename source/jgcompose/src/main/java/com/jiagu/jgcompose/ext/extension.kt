package com.jiagu.jgcompose.ext

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.lifecycle.ViewModelProvider
import com.jiagu.jgcompose.dialog.DialogViewModel
import kotlin.math.abs

/**
 * 长按监听
 *
 * @param progress
 * @param done
 */
suspend fun PointerInputScope.longPressListener(
    progress: AwaitPointerEventScope.() -> Unit, done: AwaitPointerEventScope.() -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val pos0 = down.position
        var t0 = System.currentTimeMillis()
        var state = 0
        do {
            val event = withTimeoutOrNull(100) {
                awaitPointerEvent(pass = PointerEventPass.Initial)
            }
            if (event?.type == PointerEventType.Release) {
                break
            }
            if (event?.type == PointerEventType.Move) {
                val pt = event.changes.first().position
                if (state == 1) {
                    event.changes.forEach { it.consume() }
                } else {
                    val dist = abs(pt.x - pos0.x) + abs(pt.y - pos0.y)
                    if (dist > 40) return@awaitEachGesture
                }
            }
            val t = System.currentTimeMillis()
            when (state) {
                0 -> if (t - t0 > 300) {
                    state = 1
                    t0 = t
                }

                1 -> progress()
            }
        } while (event == null || event.changes.any { it.pressed })
        if (state == 0) progress()
        done()
    }
}

/**
 * Show dialog
 *
 * @param content
 */
fun Context.showDialog(
    content: (@Composable () -> Unit)? = null,
) {
    val activity = when (this) {
        is AppCompatActivity -> this
        is ComponentActivity -> this
        else -> return
    }
    val viewModel = ViewModelProvider(activity)[DialogViewModel::class.java]
    viewModel.showDialog(content = content)
}

/**
 * Hide dialog
 *
 */
fun Context.hideDialog() {
    val activity = when (this) {
        is AppCompatActivity -> this
        is ComponentActivity -> this
        else -> return
    }
    val viewModel = ViewModelProvider(activity)[DialogViewModel::class.java]
    viewModel.hideDialog()
}

/**
 * 没有点击效果的clickable
 *
 * @param onClick
 * @receiver
 * @return
 */
fun Modifier.noEffectClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    return composed {
        clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
    }
}

/**
 * 禁止自动获取焦点
 *
 * @return
 */
fun Modifier.disableAutoFocus(): Modifier {
    return composed {
        val focusRequester = remember { FocusRequester() }
        // 将三个操作合并到composed块中
        focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { false }
    }
}