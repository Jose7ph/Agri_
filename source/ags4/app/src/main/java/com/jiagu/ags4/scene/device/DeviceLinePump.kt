package com.jiagu.ags4.scene.device

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.task.WaterLinePumpCalibrationTask
import com.jiagu.ags4.vm.task.WaterLinePumpChartTask
import com.jiagu.ags4.vm.task.WaterLinePumpConfigTask
import com.jiagu.api.ext.toString
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.chart.PumpLineChart
import com.jiagu.jgcompose.chart.PumpLineChartData
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper
import com.jiagu.tools.utils.NumberUtil
import java.math.RoundingMode
import java.util.Locale




/**
 * 喷洒器
 */
@Composable
fun DeviceLinePump() {
    val navController = LocalNavController.current
    val progressModel = LocalProgressModel.current
    MainContent(title = stringResource(id = R.string.setting_line_pump),
        barAction = {},
        breakAction = {
            navController.popBackStack()
        }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { DeviceSprayerFlow() }
//                item { DeviceSprayerLinePumpK() }
                item {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    )  {
                        Box(modifier = Modifier.weight(1f)) { DeviceSprayerLinePump() }
                        Box(modifier = Modifier.weight(1f)) { DeviceSprayerCentrifugal() }
                    }
                }
            }
            VerticalDivider(
                thickness = 1.dp, modifier = Modifier, color = Color.Gray
            )
            LazyColumn(
                modifier = Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeviceSprayerLinePumpAction(progressModel)//水泵校准
//                        DeviceSprayerFlowAction()//流量计校准
                    }
                }
            }
        }
    }
}


