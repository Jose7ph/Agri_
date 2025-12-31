package com.jiagu.ags4.utils

import android.content.Context
import com.jiagu.tools.math.WktHelper
import java.io.File

object RegionTool {
    var region: WktHelper.CountryBoundary? = null
    fun init(ctx: Context) {
        val dir = ctx.getExternalFilesDir("fcu")
        val file = File(dir, "region.wkt")
        if (!file.exists()) {
            return
        }
        region = WktHelper.getBoundary(file)
    }

    fun save(ctx: Context, wkt: String) {
        val dir = ctx.getExternalFilesDir("fcu")
        val file = File(dir, "region.wkt")
        file.writeText(wkt)
        region = WktHelper.getBoundary(wkt)
    }

    fun checkInside(lat: Double, lng: Double): Boolean {
        return true
    }
}