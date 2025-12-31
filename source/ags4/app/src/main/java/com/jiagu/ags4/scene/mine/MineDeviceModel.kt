package com.jiagu.ags4.scene.mine

import android.app.Application
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.SortieItem
import com.jiagu.ags4.bean.TrackBrief
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.DeviceSortieInfo
import com.jiagu.ags4.repo.net.model.DroneDetail
import com.jiagu.ags4.repo.net.model.DroneDevice
import com.jiagu.ags4.repo.net.model.SelectOper
import com.jiagu.ags4.repo.net.model.SelectTeam
import com.jiagu.api.ext.toMillis
import com.jiagu.api.ext.toString
import com.jiagu.api.helper.GeoHelper
import com.jiagu.jgcompose.paging.Paging
import com.jiagu.jgcompose.picker.Address
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

val LocalMineDeviceModel = compositionLocalOf<MineDeviceModel> {
    error("No MineDeviceModel provided")
}

class MineDeviceModel(app: Application) : AndroidViewModel(app) {
    val mineDeviceLogged = MutableStateFlow("")

    //我的设备
    var droneId by mutableStateOf<String?>(null)

    var droneDeviceTotal by mutableStateOf(0)
    var droneDeviceList = mutableStateListOf<DroneDevice>()
    var droneWorkArea by mutableStateOf(0f)
    var droneFlyCount by mutableStateOf(0)
    var droneWorkTime by mutableStateOf(0L)
    var droneSearch by mutableStateOf("")
    val tracks = MutableLiveData<Map<Long, List<TrackBrief>>>()

    private fun getDroneList(
        isRefresh: Boolean, pageIndex: Int = 1, pageSize: Int = 20, complete: (Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            AgsNet.getDroneList(pageIndex, pageSize, droneSearch).networkFlow {
                mineDeviceLogged.value = it
            }.collectLatest {
                if (isRefresh) {
                    droneListClear()
                }
                droneWorkArea = it.stat.workArea
                droneFlyCount = it.stat.sortieCount
                droneWorkTime = it.stat.flightTime
                droneDeviceList.addAll(it.list)
                droneDeviceTotal = it.total
                complete(true)
            }
        }
    }

    fun droneListLoadMore(
        pageIndex: Int, complete: (Boolean) -> Unit,
    ) {
        getDroneList(false, pageIndex) {
            complete(it)
        }
    }

    private var droneListJob: Job? = null
    fun droneListRefresh(
        complete: (Boolean) -> Unit,
    ) {
        droneListJob?.cancel()
        droneListJob = viewModelScope.launch {
            getDroneList(true) {
                complete(it)
                droneListJob?.cancel()
            }
        }
    }

    fun droneListClear() {
        droneDeviceTotal = 0
        droneDeviceList.clear()
    }

    //飞机详情
    var droneDetailStartDate by mutableStateOf("")
    var droneDetailEndDate by mutableStateOf("")
    var droneDetailDateRange by mutableStateOf("")
    var selUserId by mutableStateOf<Long?>(null)
    var selUserName by mutableStateOf("")
    var selGroupId by mutableStateOf<Long?>(null)
    var selGroupName by mutableStateOf("")
    var droneDetailRegion by mutableStateOf<Address?>(null)
    var droneDetailWorkArea by mutableStateOf(0f)
    var droneDetailFlyCount by mutableStateOf(0)
    var droneDetailWorkTime by mutableStateOf("0")

    val teamList = mutableStateListOf<SelectTeam>()
    val personList = mutableStateListOf<SelectOper>()

    val droneDetailList = mutableStateListOf<SortieItem>()

    var droneDetailSelectedList = mutableStateListOf<SortieItem>()
    var droneDetail by mutableStateOf<DroneDetail?>(null)

    /**
     * 初始化drone详情
     */
    fun initDroneDetail() {
        droneDetailSelectedList.clear()
        droneDetailStartDate = ""
        droneDetailEndDate = ""
        droneDetailDateRange = ""
        selUserId = null
        selUserName = ""
        selGroupId = null
        selGroupName = ""
        droneDetailRegion = null
        droneDetail = null
        droneDetailWorkArea = 0f
        droneDetailFlyCount = 0
        droneDetailWorkTime = "0"
        //当前飞机的统计数据
        getFlyDetailHistoryStatist()
        if (selUserId == null) {
            getTeams()
            getPersons()
        }
        //当前飞机的信息
        getDroneDetail()
        droneDetailRefresh()
    }

    private fun getDroneDetail() {
        viewModelScope.launch {
            AgsNet.getDroneDetail(droneId!!).networkFlow {
                mineDeviceLogged.value = it
            }.collectLatest {
                droneDetail = it
            }
        }
    }

    var droneDetailPageList by mutableStateOf<Flow<PagingData<SortieItem>>>(emptyFlow())
    fun droneDetailRefresh() {
        val startTime =
            if (droneDetailStartDate.isEmpty()) null else droneDetailStartDate.toMillis()
        val endTime = if (droneDetailEndDate.isEmpty()) null else droneDetailEndDate.toMillis()
        droneId?.let {
            val droneDetailPage =
                DroneDetailPage(
                    startTime,
                    endTime,
                    it,
                    selGroupId,
                    selUserId,
                    droneDetailRegion
                )
            droneDetailPageList = droneDetailPage.load()
        }
    }

    class DroneDetailPage(
        private val startTime: Long? = null,
        private val endTime: Long? = null,
        val droneId: String,
        val selGroupId: Long? = null,
        val selUserId: Long? = null,
        val region: Address? = null,
    ) : Paging<SortieItem>(pageSize = 20, api = { params ->
        AgsNet.getFlyDetailHistory(
            droneId,
            params.key ?: 1,
            params.loadSize,
            selGroupId,
            selUserId,
            startTime,
            endTime,
            region?.code
        )
    })

    private fun getFlyDetailHistoryStatist() {
        val startTime =
            if (droneDetailStartDate.isEmpty()) null else droneDetailStartDate.toMillis("yyyy-MM-dd")
        val endTime =
            if (droneDetailEndDate.isEmpty()) null else droneDetailEndDate.toMillis("yyyy-MM-dd")
        viewModelScope.launch {
            AgsNet.getFlyDetailHistoryStatist(
                droneId!!, selGroupId, selUserId, startTime, endTime, droneDetailRegion?.code
            ).networkFlow {
                mineDeviceLogged.value = it
            }.collectLatest {
                droneDetailWorkTime = it.flightTime.toString(1)
                droneDetailFlyCount = it.sortieCount
                droneDetailWorkArea = it.sprayRange
            }
        }
    }

    fun getTeams() {
        val allTeam = getApplication<Application>().getString(R.string.all_team)
        viewModelScope.launch {
            AgsNet.getSelectTeams(droneId!!).networkFlow {
                mineDeviceLogged.value = it
            }.collectLatest {
                teamList.clear()
                teamList.add(
                    SelectTeam(
                        -1, allTeam
                    )
                )
                teamList.addAll(it)
            }
        }
    }

    fun getPersons() {
        val allPerson = getApplication<Application>().getString(R.string.all_person)
        viewModelScope.launch {
            AgsNet.getSelectPersons(droneId!!).networkFlow {
                mineDeviceLogged.value = it
            }.collectLatest {
                personList.clear()
                personList.add(
                    SelectOper(
                        -1, allPerson
                    )
                )
                personList.addAll(it)
            }
        }
    }

    /**
     * 更新设备名称/制造商标识
     * isMaker = true 更新制造商
     * isMaker = false 名称更新
     */
    fun updateDroneDetailsName(isMaker: Boolean, droneId: String, name: String) {
        viewModelScope.launch {
            if (isMaker) {
                AgsNet.updateZzDroneName(droneId, name).networkFlow {
                    mineDeviceLogged.value = it
                }.collectLatest {
                    getDroneDetail()
                }
            } else {
                AgsNet.updateDroneName(droneId, name).networkFlow {
                    mineDeviceLogged.value = it
                }.collectLatest {
                    getDroneDetail()
                }
            }

        }
    }

    /**
     * 更新设备名称/制造商标识
     */
    fun lockDrone(droneId: String, opr: Int) {
        viewModelScope.launch {
            AgsNet.lockOpr(droneId, opr).networkFlow {
                mineDeviceLogged.value = it
            }.collectLatest {
                getDroneDetail()
            }
        }
    }


    val trackInfo = mutableListOf<List<GeoHelper.LatLngAlt>>()
    fun getSortieDetailTrackData(sortieIds: LongArray) {
        tracks.postValue(emptyMap())
        viewModelScope.launch {
            val batchMaxSize = 50
            var idx = 0
            var batchId = 0L
            var count = sortieIds.size
            val batchDataMap = mutableMapOf<Long, List<TrackBrief>>()
            while (count > 0) {
                val actual = if (count > batchMaxSize) batchMaxSize else count
                val ids = sortieIds.toList().subList(idx, idx + actual)
                AgsNet.getTrackBrief(ids).networkFlow {
                    mineDeviceLogged.value = it
                }.collectLatest {
                    batchDataMap[batchId] = it
                    tracks.postValue(batchDataMap)
                    it.forEach { s ->
                        val pts = mutableListOf<GeoHelper.LatLngAlt>()
                        s.droneInfos?.forEach {
                            pts.add(
                                GeoHelper.LatLngAlt(
                                    it.lat,
                                    it.lng,
                                    it.height ?: 0.0
                                )
                            )
                        }
                        trackInfo.add(pts)
                    }
                }
                count -= actual
                idx += actual
                batchId++
            }
        }
    }

    var startTime by mutableStateOf(9999999999999L)
    var endTime by mutableStateOf(-1L)
    var totalArea by mutableStateOf(0f)
    var totalTime by mutableStateOf(0L)
    var totalDrug by mutableStateOf(0f)
    val workInfo = mutableListOf<DeviceSortieInfo>()
    fun getSortieInfo(sortieIds: LongArray) {
        viewModelScope.launch {
            AgsNet.getSortieInfo(sortieIds.toList()).networkFlow {
                mineDeviceLogged.value = it
            }.collectLatest {
                it.forEach { v ->
                    startTime = if (v.startTime < startTime) v.startTime else startTime
                    endTime = if (v.endTime > endTime) v.endTime else endTime
                    totalArea += v.sprayArea
                    totalTime += v.flightTime
                    totalDrug += v.sprayCapacity
                    workInfo.add(v)
                }
            }
        }
    }

    fun activateDevice(success: () -> Unit, fail: () -> Unit) {
        viewModelScope.launch {
            droneId?.let { droneId ->
                Log.v("lee", "activateDevice: $droneId")
                AgsNet.activeDrone(droneId).networkFlow {
                    fail()
                }.collectLatest {
                    success()
                }
            }
        }
    }
}

// 飞机状态 0 锁定  1  在线  2 离线  3 作业
enum class FlyStatus(val status: Int) {
    LOCK(0), ONLINE(1), OFFLINE(2), WORK(3)
}