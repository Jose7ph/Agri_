package com.jiagu.ags4.vm.work

import androidx.lifecycle.MutableLiveData
import com.jiagu.ags4.utils.ParamTool
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.device.vkprotocol.IProtocol

interface IWorkSeedMaterial {
    val userData: MutableLiveData<IProtocol.UserData>
    fun startDataMonitor()
    fun stopDataMonitor()
    fun readDroneDataParam(data: IProtocol.UserData): String
}

class WorkSeedMaterialImpl() : IWorkSeedMaterial {
    override val userData = MutableLiveData<IProtocol.UserData>()

    private val monitor = { data: Any ->
        if (data is IProtocol.UserData) {
            userData.postValue(data)
        }
    }

    override fun startDataMonitor() {
        DroneModel.activeDrone?.startDataMonitor(monitor)
        DroneModel.activeDrone?.getUserData(1)
    }

    override fun stopDataMonitor() {//UserData
        DroneModel.activeDrone?.stopDataMonitor(monitor)
    }

    override fun readDroneDataParam(data: IProtocol.UserData): String {
        val r = MemoryHelper.MemoryReader(data.data, 0, data.data.size)
        val list = mutableListOf<IProtocol.IndexedParam>()
        list.add(IProtocol.IndexedParam(1, data.type))//0 ID 1个字节
        for (i in 0..5) {
            list.add(IProtocol.IndexedParam(1, r.readLEUShort()))
        }
        return ParamTool.indexedParamsToString(list)
    }
}