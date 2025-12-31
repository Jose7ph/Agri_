package com.jiagu.tools.map

import android.os.Bundle
import android.widget.FrameLayout

fun IMapCanvas.Companion.createGoogleMap(container: FrameLayout, state: Bundle?, autoLocate: Boolean, l: IMapCanvas.MapReadyListener): IMapCanvas {
    return GoogleMapCanvas(container, autoLocate, state, l)
}
