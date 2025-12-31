package com.jiagu.ags4.scene.work.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * 实时参数
 */
@Composable
fun RealTimeParam(onClose: () -> Unit) {
    val paramNames = stringArrayResource(id = R.array.rt_param_names)
    val radarStatus = stringArrayResource(id = R.array.radar_status)
    val imuData by DroneModel.imuData.observeAsState(initial = null)
    val context = LocalContext.current
    //电池参数
    val batteryParams = getBatteryParams(paramNames, imuData, context)
    //飞行参数
    val flyParams = getFlyParams(paramNames, imuData)
    //雷达参数
    val radarParams = getRadarParams(paramNames, imuData, radarStatus)
    //报警参数
    val warnParams = getWarnParams(paramNames, imuData)
    //作业参数
    val jobParams = getJobParams(paramNames, imuData)
    //GPS RTK参数
    val rtkOrGPSParams = getRTKOrGPSParams(paramNames, imuData)
    //水泵参数
    val pumpParams = getPumpParams(paramNames, imuData)
    MainContent(
        title = stringResource(id = R.string.real_time_parameters),
        breakAction = { onClose() }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //飞行参数
            item {
                GenDataRow(flyParams)
            }
            //电池参数
            item {
                GenDataRow(batteryParams)
            }
            //雷达参数
            item {
                GenDataRow(radarParams)
            }
            //报警参数
            item {
                GenDataRow(warnParams)
            }
            //作业参数
            item {
                GenDataRow(jobParams)
            }
            //GPS RTK参数
            item {
                GenDataRow(rtkOrGPSParams)
            }
            //水泵参数
            item {
                GenDataRow(pumpParams)
            }
        }
    }
}

/**
 * 飞行参数
 */
private fun getFlyParams(
    paramNames: Array<String>,
    imuData: VKAg.IMUData?
): List<RealTimeParam> {
    val realTimeParams = mutableListOf<RealTimeParam>()
    realTimeParams.add(RealTimeParam(paramNames[0], "-")) //航向角
    realTimeParams.add(RealTimeParam(paramNames[1], "-")) //横滚角
    realTimeParams.add(RealTimeParam(paramNames[2], "-")) //俯仰角
    realTimeParams.add(RealTimeParam(paramNames[3], "-")) //高度
    realTimeParams.add(RealTimeParam(paramNames[22], "-")) //温度
    realTimeParams.add(RealTimeParam(paramNames[4], "-")) //垂直速度
    realTimeParams.add(RealTimeParam(paramNames[5], "-")) //水平速度
    realTimeParams.add(RealTimeParam(paramNames[6], "-")) //海拔
    realTimeParams.add(RealTimeParam(paramNames[7], "-")) //目标距离
    realTimeParams.add(RealTimeParam(paramNames[8], "-")) //离家距离
    realTimeParams.add(RealTimeParam(paramNames[12], "-")) //目标航点
    realTimeParams.add(RealTimeParam(paramNames[14], "-")) //飞行模式
    realTimeParams.add(RealTimeParam(paramNames[15], "-")) //返航原因
    realTimeParams.add(RealTimeParam(paramNames[16], "-")) //悬停原因
    realTimeParams.add(RealTimeParam(paramNames[20], "-")) //AB点状态
    realTimeParams.add(RealTimeParam(paramNames[21], "-")) //飞行时间
    realTimeParams.add(RealTimeParam(paramNames[24], "-")) //空中标志
    realTimeParams.add(RealTimeParam(paramNames[37], "-")) //手动状态
    realTimeParams.add(RealTimeParam(paramNames[11], "-")) //锁定状态
    realTimeParams.add(RealTimeParam(paramNames[34], "-")) //锁定原因

    imuData?.apply {
        realTimeParams[0].value = yaw.toString(1)
        realTimeParams[1].value = roll.toString(1)
        realTimeParams[2].value = pitch.toString(1)
        realTimeParams[3].value = height.toString(1)
        realTimeParams[4].value = temperature.toString()
        realTimeParams[5].value = vvel.toString(1)
        realTimeParams[6].value = hvel.toString(1)
        realTimeParams[7].value = alt.toString(1)
        realTimeParams[8].value = MuBiaoJuLi.toString(0)
        realTimeParams[9].value = LiJiaJuLi.toString()
        realTimeParams[10].value = target.toString()
        realTimeParams[11].value = flyMode.toString()
        realTimeParams[12].value = returnReason.toString()
        realTimeParams[13].value = hoverReason.toString()
        realTimeParams[14].value = ABStatus.toString()
        realTimeParams[15].value = flyTime.toString()
        realTimeParams[16].value = airFlag.toString()
        realTimeParams[17].value = manual.toString()
        realTimeParams[18].value = lock.toString()
        realTimeParams[19].value = lockReason.toString()
    }
    return realTimeParams
}


/**
 * 雷达参数
 */
private fun getRadarParams(
    paramNames: Array<String>,
    imuData: VKAg.IMUData?,
    radarStatus: Array<String>
): List<RealTimeParam> {
    val realTimeParams = mutableListOf<RealTimeParam>()
    realTimeParams.add(RealTimeParam(paramNames[18], "-")) //雷达状态
    realTimeParams.add(RealTimeParam(paramNames[28], "-")) //前避障状态
    realTimeParams.add(RealTimeParam(paramNames[29], "-")) //前障碍距离
    realTimeParams.add(RealTimeParam(paramNames[30], "-")) //后避障状态
    realTimeParams.add(RealTimeParam(paramNames[31], "-")) //后障碍距离

    imuData?.apply {
        realTimeParams[0].value = LeiDaLianJieZhuangTai.toString()
        realTimeParams[1].value = radarStatus(BiZhang1Status, radarStatus)
        realTimeParams[2].value = BiZhang1JuLi.toString(1)
        realTimeParams[3].value = radarStatus(BiZhang2Status, radarStatus)
        realTimeParams[4].value = BiZhang2JuLi.toString(1)
    }
    return realTimeParams
}

/**
 * 报警参数
 */
private fun getWarnParams(
    paramNames: Array<String>,
    imuData: VKAg.IMUData?
): List<RealTimeParam> {
    val realTimeParams = mutableListOf<RealTimeParam>()
    realTimeParams.add(RealTimeParam(paramNames[17], "-")) //报警原因
    realTimeParams.add(RealTimeParam(paramNames[33], "-")) //报警标志

    imuData?.apply {
        realTimeParams[0].value = alertReason.toString()
        realTimeParams[1].value = warningFlag.toString()
    }
    return realTimeParams
}

/**
 * 电池参数
 */
private fun getBatteryParams(
    paramNames: Array<String>,
    imuData: VKAg.IMUData?,
    context: Context
): List<RealTimeParam> {
    val realTimeParams = mutableListOf<RealTimeParam>()
    realTimeParams.add(RealTimeParam(paramNames[9], "-")) //电压
    realTimeParams.add(RealTimeParam(paramNames[10], "-")) //容量
    imuData?.apply {
        when (energyType) {
            VKAg.TYPE_SMART_BATTERY -> {
                realTimeParams[0].value = "--"
                realTimeParams[1].title = context.getString(R.string.battery_capacity)
                realTimeParams[1].value = capacity.toString()
            }

            VKAg.TYPE_ENGINE -> {
                realTimeParams[0].value = energy.toString(1)
                realTimeParams[1].title = context.getString(R.string.fuel_capacity)
                realTimeParams[1].value = capacity.toString()
            }

            else -> {
                realTimeParams[0].value = energy.toString(1)
                realTimeParams[1].value = "--"
            }
        }
    }
    return realTimeParams
}

/**
 * 作业参数
 */
private fun getJobParams(
    paramNames: Array<String>,
    imuData: VKAg.IMUData?
): List<RealTimeParam> {
    val realTimeParams = mutableListOf<RealTimeParam>()
    realTimeParams.add(RealTimeParam(paramNames[25], "-")) //执行状态
    realTimeParams.add(RealTimeParam(paramNames[26], "-")) //作业亩数
    realTimeParams.add(RealTimeParam(paramNames[27], "-")) //已喷药量
    realTimeParams.add(RealTimeParam(paramNames[32], "-")) //断药标记

    imuData?.apply {
        realTimeParams[0].value = execFlag.toString()
        realTimeParams[1].value = ZuoYeMuShu.toString(1)
        realTimeParams[2].value = YiYongYaoLiang.toString(1)
        realTimeParams[3].value = drugFlag.toString()
    }
    return realTimeParams
}

/**
 * GPS RTK参数
 */
private fun getRTKOrGPSParams(
    paramNames: Array<String>,
    imuData: VKAg.IMUData?,
): List<RealTimeParam> {
    val realTimeParams = mutableListOf<RealTimeParam>()
    realTimeParams.add(RealTimeParam(paramNames[13], "-")) //星数
    realTimeParams.add(RealTimeParam(paramNames[23], "-")) //GPS状态

    imuData?.apply {
        realTimeParams[0].value = GPSNum.toString()
        realTimeParams[1].value = GPSStatus.toString()
    }
    return realTimeParams
}

/**
 * 水泵参数
 */
private fun getPumpParams(
    paramNames: Array<String>,
    imuData: VKAg.IMUData?
): List<RealTimeParam> {
    val realTimeParams = mutableListOf<RealTimeParam>()
    realTimeParams.add(RealTimeParam(paramNames[35], "-")) //水泵开关
    realTimeParams.add(RealTimeParam(paramNames[36], "-")) //水泵值
    realTimeParams.add(RealTimeParam(paramNames[38], "-")) //液位
    realTimeParams.add(RealTimeParam(paramNames[19], "-")) //流速

    imuData?.apply {
        realTimeParams[0].value = pump.toString()
        realTimeParams[1].value = pumpValue.toString()
        realTimeParams[2].value = waterLevel.toString()
        realTimeParams[3].value = waterLevel.toString()
    }
    return realTimeParams
}

@Composable
fun ParamBox(
    realTimeParam: RealTimeParam,
    boxWidth: Dp,
) {
    Row(
        modifier = Modifier
            .width(boxWidth)
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(boxWidth / 2)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                ), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                modifier = Modifier.fillMaxWidth(),
                text = realTimeParam.title,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Box(
            modifier = Modifier
                .width(boxWidth / 2)
                .fillMaxHeight()
                .align(Alignment.CenterVertically), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                modifier = Modifier.fillMaxWidth(),
                text = realTimeParam.value,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black
            )
        }
    }
}

class RealTimeParam(var title: String, var value: String)

private fun radarStatus(status: Int, radarStatus: Array<String>): String {
    return when {
        status and 1 == 0 -> radarStatus[0]
        status and 2 == 0 -> radarStatus[1]
        status and 4 == 0 -> radarStatus[2]
        status and 8 == 0 -> radarStatus[3]
        else -> radarStatus[4]
    }
}

@Composable
private fun GenDataRow(params: List<RealTimeParam>) {
    val boxWidth = 140.dp
    val rowDataCount = 4
    val rowList = params.chunked(rowDataCount)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 6.dp)
    ) {
        repeat(rowList.size) { rowIndex ->
            val rowDataList = rowList[rowIndex]
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                repeat(rowDataList.size) { data ->
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(boxWidth)
                            .background(
                                color = Color.White, shape = MaterialTheme.shapes.extraSmall
                            )
                    ) {
                        ParamBox(
                            realTimeParam = rowDataList[data], boxWidth = boxWidth
                        )
                    }
                    //不是最后一个添加行间距
                    if (data != rowDataList.size - 1) {
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    //1行数据不满填充空数据
                    if (rowDataList.size < rowDataCount && data == rowDataList.size - 1) {
                        repeat(rowDataCount - rowDataList.size) {
                            Spacer(modifier = Modifier.width(boxWidth))
                            //不是最后一个添加行间距
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                }
            }
        }
    }
}

