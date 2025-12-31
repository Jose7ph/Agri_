package com.jiagu.ags4.vm.work

import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import kotlinx.coroutines.flow.MutableStateFlow


interface IWorkBreakpoint {
    val breakPointFlow: MutableStateFlow<List<VKAg.BreakPoint>>
    var breakPoint: VKAg.BreakPoint?
    suspend fun clearBK()
    suspend fun setBK(bk: VKAg.BreakPoint?, complete: () -> Unit = {})
    suspend fun deleteLocalBK(localBlockId: Long)
    suspend fun saveLocalBK(localBlockId: Long, bk: VKAg.BreakPoint)
    suspend fun getLocalBK(localBlockId: Long, complete: (VKAg.BreakPoint?) -> Unit)
}

class WorkBreakpointImpl() : IWorkBreakpoint {
    override var breakPoint: VKAg.BreakPoint? = null
    override val breakPointFlow = MutableStateFlow<List<VKAg.BreakPoint>>(listOf())

    override suspend fun clearBK() {
        breakPointFlow.emit(emptyList())
        breakPoint = null
        DroneModel.bk = null
        DroneModel.breakPoint.postValue(null)
    }

    override suspend fun deleteLocalBK(localBlockId: Long) {
        Repo.deleteBreakpoint(localBlockId)
    }

    override suspend fun saveLocalBK(localBlockId: Long, bk: VKAg.BreakPoint) {
        Repo.saveBreakpoint(localBlockId, bk)
    }

    override suspend fun getLocalBK(localBlockId: Long, complete: (VKAg.BreakPoint?) -> Unit) {
        Repo.getBreakpoint(localBlockId, complete)
    }

    override suspend fun setBK(bk: VKAg.BreakPoint?, complete: () -> Unit) {
        bk?.let { bk ->
            breakPoint = bk
            breakPointFlow.emit(listOf(bk))
            DroneModel.bk = bk
            DroneModel.breakPoint.postValue(bk)
            complete()
        }
    }
}