package com.jiagu.ags4.scene.mine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Environment
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.jiagu.ags4.MapActivity
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.DeviceSortieInfo
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.SIMPLE_BAR_HEIGHT
import com.jiagu.ags4.utils.formatSecond
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.millisToDate
import com.jiagu.api.ext.millisToDateTime
import com.jiagu.api.ext.toNumber
import com.jiagu.api.ext.toString
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.KmlHelper
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper
import com.jiagu.tools.ext.UnitHelper.convertArea
import com.jiagu.tools.ext.UnitHelper.convertCapacity
import com.jiagu.tools.ext.UnitHelper.convertLength
import com.jiagu.tools.map.IMapCanvas
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SortieDetailListActivity : MapActivity() {
    companion object {
        const val EXTRA_SORTIE_IDS = "extra_sortie_ids"
    }

    var sortieIds: LongArray = longArrayOf()

    private var showDialog by mutableStateOf(false)
    private val emptyContent = @Composable {}
    private var dialogContent by mutableStateOf(emptyContent)

    private val mineDeviceModel: MineDeviceModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sortieIds = intent.getLongArrayExtra(EXTRA_SORTIE_IDS) ?: longArrayOf()

        mineDeviceModel.getSortieDetailTrackData(
            sortieIds = sortieIds,
        )
        mineDeviceModel.getSortieInfo(sortieIds)

        val batchIds = mutableListOf<Long>()
        mineDeviceModel.tracks.observe(this) { dataMap ->
            dataMap.forEach { (batchId, tracks) ->
                if (!batchIds.contains(batchId)) {
                    var index = 0
                    tracks.forEach { track ->
                        val pts = mutableListOf<GeoHelper.LatLngAlt>()
                        track.droneInfos?.forEach {
                            pts.add(GeoHelper.LatLngAlt(it.lat, it.lng, it.height ?: 0.0))
                        }
                        val color = pickColor(track.droneId)
                        canvas.drawPolyline(
                            "fly${batchId}-${index}",
                            pts,
                            color,
                            IMapCanvas.Params.TRACK_WIDTH
                        )
                        index++
                    }
                    canvas.fit()
                }
            }
        }
    }

    class DroneWork(val droneId: String, val color: Int) {
        var time = 0L
        var area = 0f
        var spray = 0f
        var count = 0
    }

    private val trackColor = intArrayOf(
        Color.parseColor("#30bfff"),
        Color.parseColor("#ffb231"),
        Color.parseColor("#693dff"),
        Color.parseColor("#ff543d"),
        Color.parseColor("#de3eff"),
        Color.parseColor("#4242ff"),
        Color.parseColor("#ff44b7"),
        Color.parseColor("#a43ffd"),
        Color.parseColor("#3ee3fc"),
        Color.parseColor("#53e9a3"),
        Color.parseColor("#ffb231"),
        Color.parseColor("#693dff"),
        Color.parseColor("#ff543d"),
    )

    private var colorIdx = 0
    private val droneWork = mutableMapOf<String, DroneWork>()
    private fun pickColor(droneId: String): Int {
        val w = droneWork[droneId]
        if (w == null) {
            val color = trackColor[colorIdx]
            colorIdx = (colorIdx + 1) % trackColor.size
            droneWork[droneId] = DroneWork(droneId, color)
            return color
        }
        return w.color
    }

    @Composable
    override fun ContentPage() {
        SortieDetailListPage()
        if (showDialog) {
            dialogContent()
        }
    }

    @Composable
    fun SortieDetailListPage() {
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, start = 10.dp)
                    .size(36.dp)
                    .clip(shape = CircleShape)
                    .background(color = MaterialTheme.colorScheme.onPrimary)
                    .align(Alignment.TopStart)
                    .clickable { finish() },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(vertical = 6.dp),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Black
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, start = 10.dp, end = 20.dp)
                    .width(60.dp)
                    .height(30.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color = MaterialTheme.colorScheme.onPrimary)
                    .align(Alignment.TopEnd)
                    .clickable {
                        showDialog {
                            ExportPopup(onDismiss = {
                                hideDialog()
                            })
                        }
                    }, contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.export),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 10.dp, top = SIMPLE_BAR_HEIGHT + 20.dp)
                    .height(180.dp)
                    .width(240.dp)
                    .align(Alignment.TopStart)
                    .background(BlackAlpha, shape = MaterialTheme.shapes.medium),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // 作业面积
                SortieDetailListRow(
                    title = stringResource(
                        id = R.string.flight_information_work_area,
                        UnitHelper.areaUnit(context)
                    ),
                    content = UnitHelper.transAreaWithUnit(context, mineDeviceModel.totalArea)
                )
                // 飞行架次
                SortieDetailListRow(
                    title = stringResource(id = R.string.sortie_detail_flight_count),
                    content = sortieIds.size.toString()
                )
                // 作业时长
                SortieDetailListRow(
                    title = stringResource(id = R.string.sortie_detail_work_time),
                    content = formatSecond(mineDeviceModel.totalTime, true)
                )
                // 作业药量
                SortieDetailListRow(
                    title = stringResource(
                        id = R.string.flight_information_total_dosage,
                        UnitHelper.capacityUnit()
                    ),
                    content = UnitHelper.transCapacity(mineDeviceModel.totalDrug)
                )
                // 开始时间
                SortieDetailListRow(
                    title = stringResource(id = R.string.dev_detail_start_time),
                    content = mineDeviceModel.startTime.millisToDateTime()
                )
                // 结束时间
                SortieDetailListRow(
                    title = stringResource(id = R.string.dev_detail_end_time),
                    content = mineDeviceModel.endTime.millisToDateTime()
                )
            }
        }
    }

    fun showDialog(content: @Composable () -> Unit) {
        dialogContent = content
        showDialog = true
    }

    fun hideDialog() {
        showDialog = false
    }

    private fun exportCSV(complete: (Boolean, String) -> Unit) {
        canvas.fit()
        lifecycleScope.launch {
            val dir = Environment.getExternalStorageDirectory()
            val name = System.currentTimeMillis().millisToDate("yyyyMMdd-HHmm")
            val csvName = "$name.csv"
            val jpgName = "$name.jpg"
            val csv = File(dir, csvName)
            val jpg = File(dir, jpgName)
            val total = processCSV(mineDeviceModel.workInfo, csv)
            canvas.screenshot(jpg, {
                val exportFilePath = "${dir.path}\n\n${csv.name}\n${jpg.name}"
                complete(
                    true,
                    getString(R.string.sortie_export_done, exportFilePath)
                )
            }, { addText(it, total) })
        }
    }

    private class Total(val area: Float, val spray: Float, val count: Int, val time: Long)

    private fun processCSV(log: List<DeviceSortieInfo>, file: File): Total {
        val unit = UnitHelper.areaUnit(applicationContext)
        val lengthUnit = UnitHelper.lengthUnit()
        val capacityUnit = UnitHelper.capacityUnit()
        try {
            val out = BufferedOutputStream(FileOutputStream(file))
            out.write(
                getString(
                    R.string.sortie_map_csv_title,
                    lengthUnit,
                    unit,
                    capacityUnit
                ).toByteArray()
            )
            out.write("\n".toByteArray())
            var totalArea = 0f
            var totalSpray = 0f
            var totalTime = 0L
            for (data in log) {
                val area = convertArea(data.sprayArea)
                val sprayWidth = convertLength(data.sprayWidth)
                val sprayCapacity = convertCapacity(data.sprayCapacity)
                val workTime = "${data.startTime.millisToDate("yyyy/MM/dd HH:mm")} ~ ${
                    data.endTime.millisToDate("yyyy/MM/dd HH:mm")
                }"
                val duration = data.endTime - data.startTime
                val workMode = when (data.jobMode) {
                    DroneModel.TYPE_MODE_MA -> getString(R.string.manual)
                    DroneModel.TYPE_MODE_AB -> "AB"
                    DroneModel.TYPE_MODE_AU -> getString(R.string.auto)
                    else -> ""
                }
                val workType = when (data.jobType) {
                    DroneModel.TYPE_SPRAY -> getString(R.string.device_weight_work_mode_spray)
                    DroneModel.TYPE_SEED -> getString(R.string.device_weight_work_mode_seed)
                    DroneModel.TYPE_LIFTING -> getString(R.string.lifting)
                    else -> ""
                }
                val row = "${data.userName}," +
                        "${sprayWidth.toString(2).toNumber()}," +
                        "${area.toString(2).toNumber()}," +
                        "${sprayCapacity.toString(2).toNumber()}," +
                        "${workTime}," +
                        "${duration / 1000}," +
                        "${workMode}," +
                        "${workType}," +
                        data.address +
                        "\n"
                out.write(row.toByteArray())
                totalArea += area
                totalSpray += data.sprayCapacity
                totalTime += duration
            }
            out.write("\n".toByteArray())
            out.write(
                getString(
                    R.string.sortie_map_csv_total_title,
                    unit,
                    capacityUnit
                ).toByteArray()
            )
            out.write("\n".toByteArray())
            val text = "${totalArea.toString(2).toNumber()},${
                totalSpray.toString(2).toNumber()
            },${totalTime / 1000}\n"
            out.write(text.toByteArray())
            out.close()
            return Total(totalArea, totalSpray, log.size, totalTime)
        } catch (e: IOException) {
            e.printStackTrace()
            return Total(0f, 0f, 0, 0)
        }
    }

    private fun addText(bitmap: Bitmap, data: Total) {
        val unit = UnitHelper.areaUnit(applicationContext)
        val capacityUnit = UnitHelper.capacityUnit()
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val texts = listOf(
            "${mineDeviceModel.startTime.millisToDate("yyyy-MM-dd")}~${
                mineDeviceModel.endTime.millisToDate(
                    "yyyy-MM-dd"
                )
            }",
            getString(R.string.sortie_map_title1, unit, data.area.toString(1)),
            getString(R.string.sortie_map_title2, capacityUnit, data.spray.toString(1)),
            getString(R.string.sortie_map_title3, data.count),
            getString(R.string.sortie_map_title4, (data.time / 3600000f).toString(1))
        )
        paint.textSize = 40f
        var maxW = 0f
        for (t in texts) {
            val w = paint.measureText(t)
            if (maxW < w) {
                maxW = w
            }
        }
        paint.color = Color.argb(120, 0, 0, 0)
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, maxW + 40, 280f, paint)
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL_AND_STROKE
        var y = 50f
        for (s in texts) {
            canvas.drawText(s, 20f, y, paint)
            y += 50
        }
    }


    private fun exportKml(complete: (Boolean, String) -> Unit) {
        if (mineDeviceModel.trackInfo.isEmpty()) return
//        showProgressDialog(getString(R.string.please_wait))
        lifecycleScope.launch {
            val kmlHelper = KmlHelper()
            val dir = Environment.getExternalStorageDirectory()
            val name = System.currentTimeMillis().millisToDate("yyyyMMddHHmm")
            val kml = File(dir, "$name.kml")
            val kmlData = mutableListOf<KmlHelper.Geometry>()
            for ((i, d) in mineDeviceModel.workInfo.withIndex()) {
                val kmlD = KmlHelper.Geometry(
                    "${d.droneId}-${d.startTime.millisToDate("yyyyMMddHHmm")}",
                    "track",
                    mineDeviceModel.trackInfo[i]
                )
                kmlData.add(kmlD)
            }
            kmlHelper.createKml(kml, kmlData) {
                val exportFilePath = "${dir.path}\n\n${kml.name}"
                complete(true, getString(R.string.sortie_export_done, exportFilePath))
            }
        }
    }

    /**
     * 导出弹窗
     */
    @Composable
    private fun ExportPopup(
        onDismiss: () -> Unit,
    ) {
        var step by remember {
            mutableIntStateOf(1)
        }
        //导出方式 1 CSV 2 KML
        var exportType by remember {
            mutableIntStateOf(1)
        }

        val defaultTitle = stringResource(id = R.string.select_export_method)
        val csvExportTitle = "CSV " + stringResource(id = R.string.export)
        val kmlExportTitle = "KML " + stringResource(id = R.string.export)
        var title by remember {
            mutableStateOf(defaultTitle)
        }

        title = when (step) {
            1 -> defaultTitle
            else -> {
                when (exportType) {
                    1 -> csvExportTitle
                    2 -> kmlExportTitle
                    else -> ""
                }
            }
        }
        var exportMsg by remember {
            mutableStateOf("")
        }
        val stepContent = @Composable {
            when (step) {
                1 -> {
                    //导出格式选择
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 80.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            RadioButton(selected = exportType == 1, onClick = {
                                exportType = 1
                            })
                            Text(text = "CSV")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            RadioButton(selected = exportType == 2, onClick = {
                                exportType = 2
                            })
                            Text(text = "KML")
                        }
                    }
                }

                2 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stringResource(id = R.string.exporting))
                    }
                }

                3 -> {
                    //导出结果
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 20.dp)
                    ) {
                        Text(text = exportMsg)
                    }
                }

                else -> {}
            }
        }


        ScreenPopup(
            width = 300.dp,
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    //title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                    }
                    stepContent()
                }

            },
            onConfirm = {
                when (step) {
                    1 -> {
                        step = 2
                        when (exportType) {
                            1 -> {
                                //CSV
                                exportCSV { _, message ->
                                    step = 3
                                    exportMsg = message
                                }
                            }

                            2 -> {
                                //KML
                                exportKml { _, message ->
                                    step = 3
                                    exportMsg = message
                                }
                            }
                        }
                    }

                    3 -> {
                        onDismiss()
                    }
                }
            },
            onDismiss = {
                onDismiss()
            },
            showConfirm = step != 2,
            showCancel = step != 2,
        )
    }

    /**
     * 架次明细row
     */
    @Composable
    private fun SortieDetailListRow(title: String, content: String) {
        val textSpaced = 20.dp
        val labelWidth = 80.dp
        val autoScrollingWidth = 120.dp
        val rowPadding = 12.dp
        Row(
            modifier = Modifier.padding(horizontal = rowPadding),
            horizontalArrangement = Arrangement.spacedBy(textSpaced),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                width = labelWidth,
                color = MaterialTheme.colorScheme.onPrimary
            )
            AutoScrollingText(
                text = content,
                width = autoScrollingWidth,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
