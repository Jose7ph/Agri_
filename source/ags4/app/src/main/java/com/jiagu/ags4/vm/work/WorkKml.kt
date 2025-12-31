package com.jiagu.ags4.vm.work

import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.utils.WorkUtils
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.KmlHelper
import com.jiagu.api.model.MapBlock
import com.jiagu.api.model.MapRing
import java.io.File


interface IWorkKml {
    var kmlBlockName: String?
    var kmlBlock: Pair<MapBlock, Array<DoubleArray>>?
    var kmlTrack: Pair<MapRing, DoubleArray>?
    fun loadKml(file: File, complete: (Boolean, Int?) -> Unit)
    fun clearKml()
}

class WorkKmlImpl : IWorkKml {
    override var kmlBlockName: String? = null
    override var kmlBlock: Pair<MapBlock, Array<DoubleArray>>? = null
    override var kmlTrack: Pair<MapRing, DoubleArray>? = null

    @Suppress("UNCHECKED_CAST")
    override fun loadKml(file: File, complete: (Boolean, Int?) -> Unit) {
        val kml = KmlHelper()
        if (kml.loadKmlFile(file) && kml.geometries.isNotEmpty()) {
            val geometry = kml.geometries[0]
            kmlBlockName = geometry.name
            when (geometry.type) {
                "block" -> {
                    val block = geometry.data as List<List<GeoHelper.LatLngAlt>>
                    kmlBlock = WorkUtils.mapBlock2Arrays(block)
                    complete(true, Block.TYPE_BLOCK)
                }

                "track" -> {
                    val track = geometry.data as List<GeoHelper.LatLngAlt>
                    kmlTrack = WorkUtils.mapRing2Arrays(track)
                    complete(true, Block.TYPE_TRACK)
                }
            }
        } else {
            complete(false, null)
        }
    }

    override fun clearKml() {
        kmlBlock = null
        kmlTrack = null
    }
}