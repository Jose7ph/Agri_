import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.ui.theme.ComposeTheme

@Composable
fun SignalIndicator(
    signal: Int,
    barCount: Int = 4,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier
    ) {
        repeat(barCount) {
            SignalBar(
                fraction = ((it + 1) * (1.0 / barCount)).toFloat(),
                isActive = it < signal,
                activeColor
            )
        }
    }
}

@Composable
fun SignalBar(
    fraction: Float = 1f,
    isActive: Boolean,
    activeColor: Color,
) {
    val color: Color = if (isActive) activeColor else Color.LightGray
    Row(modifier = Modifier.fillMaxHeight()) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight(fraction)
                .background(color = color)
                .align(Alignment.Bottom)
        )
    }
}


@Preview
@Composable
fun testSignalIndicator() {
    ComposeTheme {
        Column(modifier = Modifier.padding(10.dp)) {
            SignalIndicator(2, barCount = 5, modifier = Modifier.height(20.dp))
        }
    }
}