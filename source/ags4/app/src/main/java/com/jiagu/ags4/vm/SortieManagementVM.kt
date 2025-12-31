package com.jiagu.ags4.vm

import android.app.Application
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.api.ext.toMillis
import com.jiagu.jgcompose.paging.Paging
import com.jiagu.jgcompose.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

val LocalSortieManagementVM = compositionLocalOf<SortieManagementVM> {
    error("No SortieManagementVM provided")
}

class SortieManagementVM(app: Application) : AndroidViewModel(app) {
    val defaultStartTime = "2000-01-01 00:00:00"
    var startTime by mutableStateOf(defaultStartTime)
    var endTime by mutableStateOf("")
    var sortieType by mutableStateOf("")

    val showDetailMap = mutableStateMapOf<String, Boolean>()
    val sortieDateInfoDetailsMap = mutableStateMapOf<String, List<UserSortieCountDetail>>()
    //key 日期 value 当天下选择的架次数量
    val selectedAllChildes = mutableStateMapOf<String, Set<Long>>()

    fun toggleShowDetails(date: String, complete: (List<UserSortieCountDetail>) -> Unit = {}) {

        if (showDetailMap[date] != true) {
            viewModelScope.launch {
                val startTime = DateUtils.stringToDate(date)?.time!!
                val endTime = startTime + 24 * 60 * 60 * 1000 - 1
                AgsNet.getDateUserSortieByDay(
                    UserSortieQueryParams(
                        listOf(AgsUser.userInfo?.userId.toString()),
                        startTime.toString(),
                        endTime.toString(),
                        0,
                        0,
                        sortieType
                    )
                ).networkFlow {
                    Log.e("lee", "load land block datas error:${it} ")
                }.collectLatest {
                    sortieDateInfoDetailsMap[date] = it
                    showDetailMap[date] = true
                    complete(it)
                }
            }
        }
    }

    fun clearSortie(){
        showDetailMap.clear()
        sortieDateInfoDetailsMap.clear()
        selectedAllChildes.clear()
    }

    var sortieInfoPageList by mutableStateOf<Flow<PagingData<UserSortieCount>>>(emptyFlow())

    fun refresh() {
        clearSortie()
        val st = if (startTime.isNotBlank()) startTime.toMillis().toString() else ""
        val et = if (endTime.isNotBlank()) endTime.toMillis().toString() else ""
        val sortieInfoPage =
            SortieInfoPage(st, et, sortieType)
        sortieInfoPageList = sortieInfoPage.load()
    }

    class SortieInfoPage(
        private val startTime: String,
        private val endTime: String,
        sortieType: String
    ) : Paging<UserSortieCount>(pageSize = 20, api = { params ->
        AgsNet.getDateUserSortieCountPage(
            UserSortieQueryParams(
                listOf(AgsUser.userInfo?.userId.toString()),
                startTime,
                endTime,
                params.key ?: 1,
                params.loadSize,
                sortieType
            )
        )
    })
}