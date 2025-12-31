package com.jiagu.ags4.vm.work

import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.tools.v9sdk.RouteParameter
import com.jiagu.api.helper.GeoHelper
import com.jiagu.tools.v9sdk.RouteModel

interface IWorkRouteParameter {
    var curEdge: Int
    var curRidge: Float
    var curAngle: Int
    var poleRadius: Float
    var planType: Int
    var barrierSafeDist: Float
    var distances: MutableList<Float>
    var smartDist: Int
    var block: List<GeoHelper.LatLngAlt>
    var obstacles: List<List<GeoHelper.LatLngAlt>>

    fun initBlockAndObstacles(blockPlan: BlockPlan, complete: () -> Unit)
    fun updateRouteByPlanParam(
        param: PlanParamInfo,
        planType: Int,
        blockSize: Int,
        paramBarrierSize: Int,
    )

    fun offsetBlockAndObstacles(offsetLat: Double, offsetLng: Double, complete: () -> Unit)
    fun toRouteParameter(): RouteParameter
}

class WorkRouteParameterImpl : IWorkRouteParameter {
    override var distances = mutableListOf<Float>()
    override var smartDist = 0
    override var curEdge = 0
    override var curRidge = 3.5f
    override var curAngle = 0
    override var poleRadius = 5f
    override var planType = RouteModel.PLAN_BLOCK
    override var barrierSafeDist: Float = 2f
    override var block: List<GeoHelper.LatLngAlt> = emptyList()
    override var obstacles: List<List<GeoHelper.LatLngAlt>> = emptyList()

    override fun initBlockAndObstacles(blockPlan: BlockPlan, complete: () -> Unit) {
        block = blockPlan.boundary[0]
        obstacles = blockPlan.barriers
        complete()
    }

    override fun updateRouteByPlanParam(
        param: PlanParamInfo,
        planType: Int,
        blockSize: Int,
        paramBarrierSize: Int,
    ) {
        curEdge = param.curEdge
        curRidge = param.curRidge
        curAngle = param.curAngle
        poleRadius = param.curRadius
        this.planType = planType
        if (blockSize == paramBarrierSize) distances =
            param.edgeSafeDist.toMutableList()//防止保存的参数中，数组个数和地块的边不相等问题
        barrierSafeDist = param.barrierSafeDist
    }

    override fun offsetBlockAndObstacles(
        offsetLat: Double,
        offsetLng: Double,
        complete: () -> Unit,
    ) {
        block = block.map { item ->
            GeoHelper.LatLngAlt(
                item.latitude + offsetLat,
                item.longitude + offsetLng,
                item.altitude
            )
        }
        obstacles = obstacles.map { barrier ->
            barrier.map { point ->
                GeoHelper.LatLngAlt(
                    point.latitude + offsetLat,
                    point.longitude + offsetLng,
                    point.altitude
                )
            }
        }
        complete()
    }

    override fun toRouteParameter(): RouteParameter {
        return RouteParameter(
            curEdge,
            curRidge,
            curAngle,
            poleRadius,
            planType,
            barrierSafeDist,
            distances,
            smartDist,
        )
    }
}
