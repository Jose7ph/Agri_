
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


@Composable
fun BreathingLightDemo() {
    var previousInt by remember { mutableStateOf(0) }
    var currentInt by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BreathingLight(modifier = Modifier.size(40.dp), currentInt, currentInt)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            previousInt = currentInt
            currentInt++
        }) {
            Text("Increase Int")
        }
    }
}

@Composable
fun BreathingLight(modifier: Modifier, value: Int, refreshKey: Int) {
    var isBright by remember { mutableStateOf(false) }
    LaunchedEffect(refreshKey) {
        if (refreshKey <= 0 && value <= 0) {
            isBright = false
            return@LaunchedEffect
        }
        isBright = true
        delay(280)
        isBright = false
    }

    val color by animateColorAsState(
        targetValue = if (isBright) Color.Green else Color.Gray
    )

    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            radius = size.minDimension / 2,
            center = center,
            style = Fill
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        BreathingLightDemo()
    }
}