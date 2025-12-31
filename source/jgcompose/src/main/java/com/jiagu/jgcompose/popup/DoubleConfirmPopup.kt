package com.jiagu.jgcompose.popup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.button.SliderSwitchButton
import com.jiagu.jgcompose.theme.ComposeTheme

@Composable
fun DoubleConfirmPopup(
    title: String? = null,
    content: @Composable () -> Unit = {},
    titleColor: Color = Color.Black,
    sliderTitle: String = stringResource(id = R.string.slider_tip),
    buttonName: String = stringResource(id = R.string.confirm),
    cancelText: Int = R.string.cancel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ScreenPopup(
        width = 360.dp, content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                title?.let {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = titleColor
                        )
                    }
                }
                content()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SliderSwitchButton(
                        sliderTitle = sliderTitle,
                        buttonName = buttonName,
                        sliderWidth = 220.dp,
                        buttonWidth = 100.dp,
                        height = 40.dp,
                        onSuccess = { success ->
                            if (success) onConfirm()
                        }
                    )
                }
            }
        },
        showCancel = true, showConfirm = false, cancelText = cancelText, onDismiss = onDismiss
    )

}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DoubleConfirmPopupPreview() {
    ComposeTheme {
        Column {
            DoubleConfirmPopup(
                title = "DoubleConfirmPopup",
                content = {},
                onConfirm = {},
                onDismiss = {})
        }
    }
}