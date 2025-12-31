package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.utils.arrayToMapBlock3D
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.utils.mapBlockToArray
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.IWorkMapMarker
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.WorkMapMarkerImpl
import com.jiagu.ags4.vm.work.IWorkEdit
import com.jiagu.ags4.vm.work.IWorkPlan
import com.jiagu.ags4.vm.work.WorkEditImpl
import com.jiagu.ags4.vm.work.WorkPlanImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D
import com.jiagu.api.math.Polygon2D
import com.jiagu.api.model.MapBlock
import com.jiagu.api.model.MapBlock3D
import com.jiagu.api.model.MapRing
import com.jiagu.api.model.MathBlock
import com.jiagu.api.model.MathRing
import com.jiagu.tools.math.GeometryHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.locationtech.jts.geom.GeometryFactory

class BlockDivisionModel(app: Application) : AndroidViewModel(app),
    IWorkEdit by WorkEditImpl(),
    IWorkMapMarker by WorkMapMarkerImpl(), IWorkPlan by WorkPlanImpl() {

    private val converter = GeoHelper.GeoCoordConverter()
    private val factory = GeometryFactory()

    val divisionPoints = mutableStateListOf<GeoHelper.LatLngAlt>()
    val divisionPointsFlow = MutableStateFlow<MapRing>(emptyList())

    val divisionBlocks = mutableStateListOf<MutableList<MathBlock>>()
    val divisionBlocksFlow = MutableStateFlow<List<MapBlock>>(emptyList())


    fun initEditBlockPlan(blockPlan: BlockPlan) {
        val boundary = blockPlan.boundary
        divisionBlocks.add(mutableListOf(map2math(boundary)))
        exeTask {
            setBlockPlan(blockPlan)
            divisionBlocksFlow.emit(listOf(boundary))
        }
    }

    fun addDivisionPoint(pt: GeoHelper.LatLngAlt) {
        exeTask {
            if (divisionPoints.size < 2) {
                divisionPoints.add(pt)
                divisionPointsFlow.emit(divisionPoints.toList())
            }else{
                sliceBlock()
            }
        }
    }

    fun sliceBlock() {
        if (divisionBlocks.size > 20 || divisionPoints.size < 2) {
            return
        }
        val pt1 = divisionPoints[0]
        val pt2 = divisionPoints[1]
        slice(pt1, pt2) {
            divisionPoints.clear()
            exeTask {
                divisionPointsFlow.emit(divisionPoints.toList())
            }
        }
    }

    fun clearDivisionBlocks() {
        divisionPoints.clear()
        if (divisionBlocks.size > 1) {
            divisionBlocks.removeAt(divisionBlocks.lastIndex)
        }
        exeTask {
            divisionPointsFlow.emit(divisionPoints.toList())
            divisionBlocksFlow.emit(maths2maps(divisionBlocks.last()))
        }
    }

    fun saveAs(complete: () -> Unit) {
        exeTask {
            val blocks = toBlocks(selectedBP!!.blockName, selectedBP!!.groupId)
            exeTask {
                Repo.uploadBlocks(blocks).collect {
                    complete()
                }
            }
        }
    }

    fun markerSelect(mIdx: Int) {
        if (mIdx < 0) {
            return
        }
        val point = divisionPoints[mIdx]
        changeSelectedMarkerIndex(mIdx)
        showMarkerPanel = true
        initMarker(point)
    }

    fun updatePoint(pt: GeoHelper.LatLngAlt) {
        if (selectedMarkerIndex < 0 || selectedMarkerIndex >= divisionPoints.size) return
        divisionPoints[selectedMarkerIndex] = pt
        exeTask {
            divisionPointsFlow.emit(divisionPoints.toList())
        }
    }

    fun removePointByIndex() {
        divisionPoints.removeAt(selectedMarkerIndex)
        exeTask {
            divisionPointsFlow.emit(divisionPoints.toList())
        }
        clearMarker()
    }

    private fun toBlocks(name: String, groupId: Long?): List<Block> {
        val out = mutableListOf<Block>()
        val maps = divisionBlocksFlow.value
        val maths = divisionBlocks.last()
        for ((i, map) in maps.withIndex()) {
            val block = buildBlock("${name}-${i + 1}", map, maths[i], groupId)
            out.add(block)
        }
        return out
    }

    private fun buildBlock(name: String, map: MapBlock, math: MathBlock, groupId: Long?): Block {
        val boundary = mapBlockToArray(map)
        val area = calcDivisionArea(math)
        val block = Block(
            Block.TYPE_BLOCK,
            name,
            arrayToMapBlock3D(boundary, null),
            doubleArrayOf(),
            area.toFloat(),
            groupId
        )
        block.createTime = System.currentTimeMillis()
        return block
    }

    private fun calcDivisionArea(mathBlock: MathBlock): Double {
        var area = 0.0
        for ((i, ring) in mathBlock.withIndex()) {
            val poly = Polygon2D(ring)
            val a = poly.area()
            if (i == 0) {
                area += a
            } else {
                area -= a
            }
        }
        return area / 666.66667
    }

    private fun slice(p1: GeoHelper.LatLng, p2: GeoHelper.LatLng, complete: () -> Unit) {
        exeTask {
            withContext(Dispatchers.Default) {
                val pt1 = converter.convertLatLng(p1)
                val pt2 = converter.convertLatLng(p2)
                val blks = mutableListOf<MathBlock>()
                for (b in divisionBlocks.last()) {
                    val sub = GeometryHelper.slicePolygon(factory, pt1, pt2, b)
                    blks.addAll(sub)
                }
                if (blks.size > divisionBlocks.last().size) {
                    divisionBlocks.add(blks)
                    divisionBlocksFlow.emit(maths2maps(blks))
                }
                complete()
            }
        }
    }

    private fun maths2maps(mathBlocks: List<MathBlock>): List<MapBlock> {
        val out = mutableListOf<MapBlock>()
        mathBlocks.forEach { out.add(math2map(it)) }
        return out
    }

    private fun math2map(mathBlock: MathBlock): MapBlock {
        val out = mutableListOf<MapRing>()
        mathBlock.forEach {
            val ring = mutableListOf<GeoHelper.LatLng>()
            converter.convertPoint(it, ring)
            out.add(ring)
        }
        return out
    }

    private fun map2math(mapBlock: MapBlock3D): MathBlock {
        val out = mutableListOf<MathRing>()
        mapBlock.forEach {
            val ring = mutableListOf<Point2D>()
            converter.convertLatLng(it, ring)
            out.add(ring)
        }
        return out
    }
}