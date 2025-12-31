package com.jiagu.ags4.utils

import com.jiagu.tools.v9sdk.RouteModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapBlock
import com.jiagu.api.model.MapRing
import com.jiagu.device.controller.Checksum
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd

object WorkUtils {
    fun planType2VK2(type: Int): Int {
        val t = when (type) {
            RouteModel.PLAN_BLOCK -> VKAg.MISSION_UTYPE
            RouteModel.PLAN_EDGE -> VKAg.MISSION_EDGE
            RouteModel.PLAN_TREE -> VKAg.MISSION_LINE
            RouteModel.PLAN_POLE -> VKAg.MISSION_THROW
            RouteModel.PLAN_BLOCK_EDGE -> VKAg.MISSION_UTYPE_EDGE
            else -> VKAg.MISSION_UTYPE
        }
        return t + 100
    }

    fun vk2PlanType(vk: Int): Int {
        return when (vk) {
            VKAg.MISSION_UTYPE -> RouteModel.PLAN_BLOCK
            VKAg.MISSION_EDGE -> RouteModel.PLAN_EDGE
            VKAg.MISSION_LINE -> RouteModel.PLAN_TREE
            VKAg.MISSION_THROW -> RouteModel.PLAN_POLE
            VKAg.MISSION_UTYPE_EDGE -> RouteModel.PLAN_BLOCK_EDGE
            else -> RouteModel.PLAN_BLOCK
        }
    }

    fun getNaviId(track: List<RoutePoint>): Int {
        val list = mutableListOf<String>()
        for (t in track) {
            list.add("${t.latitude},${t.longitude}")
        }
        val data = list.joinToString("-").toByteArray()
        return Checksum.crc32_ccitt(data, 0, data.size).toInt()
    }

    fun getNaviTree(rp: RoutePoint): NaviTree {
        val pointPump = if (rp.pump) 1 else 0
        val naviPump = if (rp.wlMission == VKAgCmd.WL_MISSION_PUMP.toInt()) 1 else 0//航线类型-打开水泵
        val h = if (rp.heightType == 1) {
            rp.elevation
        } else {
            if (rp.height == 0f) 5f else rp.height
        }
        return NaviTree(
            naviPump, h, pointPump, if (rp.wpParam == 0) 5 else rp.wpParam, rp.heightType
        )
    }

    fun mapRing2Arrays(ring: List<GeoHelper.LatLngAlt>): Pair<MapRing, DoubleArray> {
        val p = mutableListOf<GeoHelper.LatLng>()
        val h = mutableListOf<Double>()
        for (pt in ring) {
            p.add(GeoHelper.LatLng(pt.latitude, pt.longitude))
            h.add(pt.altitude)
        }
        return p to h.toDoubleArray()
    }

    fun mapBlock2Arrays(block: List<List<GeoHelper.LatLngAlt>>): Pair<MapBlock, Array<DoubleArray>> {
        val out = mutableListOf<MapRing>()
        val alt = mutableListOf<DoubleArray>()
        for (poly in block) {
            val (p, h) = mapRing2Arrays(poly)
            out.add(p)
            alt.add(h)
        }
        return out to alt.toTypedArray()
    }

}

class NaviTree(
    var naviPump: Int,
    var pointHeight: Float,
    var pointPump: Int,
    var sprayTime: Int,
    var pointHeightType: Int,
)
