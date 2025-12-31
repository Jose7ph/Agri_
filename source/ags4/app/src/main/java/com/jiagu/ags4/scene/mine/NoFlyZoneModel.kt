package com.jiagu.ags4.scene.mine

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.paging.PagingData
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.model.NoFlyZoneInfo
import com.jiagu.jgcompose.paging.Paging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

val LocalNoFlyZoneModel = compositionLocalOf<NoFlyZoneModel> {
    error("No NoFlyZoneModel provided")
}

class NoFlyZoneModel(app: Application) : AndroidViewModel(app) {
    var noFlyZonePageList by mutableStateOf<Flow<PagingData<NoFlyZoneInfo>>>(emptyFlow())

    var detailAddress by mutableStateOf("")

    var noflyType by mutableStateOf(NoFlyTypeEnum.NO_FLY_TYPE_ALL)

    fun refresh() {
        val noFlyZonePage = NoFlyZonePage(noflyType.key, detailAddress)
        noFlyZonePageList = noFlyZonePage.load()
    }

    class NoFlyZonePage(
        noflyType: Int,
        detailAddress: String
    ) : Paging<NoFlyZoneInfo>(pageSize = 20, api = { params ->
        AgsNet.getNoflyList(
            currentPage = params.key ?: 1,
            pageSize = params.loadSize,
            noflyType = noflyType,
            detailAddress = detailAddress,
        )
    })
}

enum class NoFlyTypeEnum(val key: Int, val value: Int) {
    NO_FLY_TYPE_ALL(key = 0, value = R.string.block_filter_all),
    NO_FLY_TYPE_OTHER(key = 1, value = R.string.other),
    NO_FLY_TYPE_CIVIL_AVIATION(key = 2, value = R.string.civil_aviation)
}
