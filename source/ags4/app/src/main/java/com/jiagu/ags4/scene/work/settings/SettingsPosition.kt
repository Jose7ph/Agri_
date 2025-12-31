package com.jiagu.ags4.scene.work.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.api.helper.GeoHelper
import com.jiagu.jgcompose.container.MainContent

class TestPoint(
    lat: Double,
    lng: Double,
): GeoHelper.LatLng(lat, lng) {
    var index by mutableStateOf(0)

    var lat: Double by mutableStateOf(lat)
    var lng: Double by mutableStateOf(lng)
}
@Composable
fun PositionSettings(moveToDronePosition: () -> Unit, finish: () -> Unit = {}) {
    MainContent(
        title = stringResource(id = R.string.aircraft_position),
        backgroundColor = Color.Transparent,
        breakAction = { finish() }) {

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 40.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            moveToDronePosition()
                        }
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.default_flight_location),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}