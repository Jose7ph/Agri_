package com.jiagu.ags4.scene.work

import android.graphics.Color
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.ags4.vm.LocatorModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.tools.map.IMapCanvas

fun MapVideoActivity.collectLocator(locatorModel: LocatorModel, locationModel: LocationModel) {
    addMapChangeListener(MapChangedListener(canvas, locationModel))
    val lineList = mutableListOf<String>()
    val locatorLine = "locator_line"
    collectFlow(locatorModel.locatorListFlow) {
        if (it.isEmpty()) {
            canvas.drawNumberMarker(listOf())
            repeat(lineList.size) {
                canvas.remove(lineList[it])
            }
            lineList.clear()
            droneCanvas.clearText()
        } else {
            for (i in it.indices) {
                val curPt = it[i].pt
                val next = if (i == it.size - 1) null else it[i + 1]
                next?.let {
                    val lineColor = getLineColor(next.level)
                    val curLineName = locatorLine + "${i}_${i + 1}"
                    if (!lineList.contains(curLineName)) {
                        lineList.add(curLineName)
                    }
                    canvas.drawLine(
                        name = curLineName,
                        pts = listOf(curPt, next.pt),
                        color = lineColor,
                        z = IMapCanvas.Z_HL_LINE,
                        width = IMapCanvas.Params.COMPLETION_WIDTH
                    )
                }
            }
        }
    }
    //收到点数据的时候触发
    val curLine = "cur_line"
    collectFlow(locationModel.location) {
        //至少有一个点 && 收到了新的部署null的location数据
        if (locatorModel.locatorList.isNotEmpty() && it != null) {
            val pt1 = locatorModel.locatorList.last().pt
            val pt2 = GeoHelper.LatLngAlt(it.lat, it.lng, it.alt)
            val pts = listOf(pt1, pt2)
            val level = locatorModel.processLevel(it.info.hdop)
//            val level = Random.nextInt(1, 4)
            val lineColor = getLineColor(level)
            droneCanvas.drawDistance(pts, false)
            canvas.drawLine(
                name = curLine, pts = pts, color = lineColor, z = IMapCanvas.Z_HL_LINE,
                width = IMapCanvas.Params.COMPLETION_WIDTH
            )
        }
    }
}

private fun getLineColor(level: Int): Int {
    return when {
        level <= 1 -> Color.RED
        level == 2 -> Color.YELLOW
        else -> Color.GREEN
    }
}