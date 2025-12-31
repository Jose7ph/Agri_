package com.jiagu.ags4.utils

import android.content.Context
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.tools.ext.UnitHelper

// 获取喷洒亩用量标题（含单位）
fun Context.sprayTitle(): String {
    return getString(R.string.spray_mu, UnitHelper.sprayUnit(this))
}

// 获取播撒亩用量标题（含单位）
fun Context.seedTitle(): String {
    return getString(R.string.seed_mu, UnitHelper.seedUnit(this))
}

fun UnitHelper.initUnit(ctx: Context) {
    val app = AppConfig(ctx)
    setAreaUnit(app.areaUnit)
    setCapacityUnit(app.capacityUnit)
    setWeightUnit(app.weightUnit)
    setLengthUnit(app.lengthUnit)
}