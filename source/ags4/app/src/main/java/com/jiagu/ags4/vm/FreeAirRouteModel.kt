package com.jiagu.ags4.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.work.IWorkBreakpoint
import com.jiagu.ags4.vm.work.IWorkEdit
import com.jiagu.ags4.vm.work.IWorkPage
import com.jiagu.ags4.vm.work.IWorkPlan
import com.jiagu.ags4.vm.work.WorkBreakpointImpl
import com.jiagu.ags4.vm.work.WorkEditImpl
import com.jiagu.ags4.vm.work.WorkPageImpl
import com.jiagu.ags4.vm.work.WorkPlanImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgTool
import kotlinx.coroutines.flow.MutableStateFlow

class FreeAirRouteModel(app: Application) : AndroidViewModel(app), IWorkPlan by WorkPlanImpl(),
    IWorkPage by WorkPageImpl(Block.TYPE_TRACK), IWorkEdit by WorkEditImpl(),
    IWorkBreakpoint by WorkBreakpointImpl() {

    val clearFlag = MutableStateFlow<Boolean>(false)

    var dronePosition: GeoHelper.LatLngAlt? = null

    fun processImuData(imuData: VKAg.IMUData) {
        //飞机位置
        dronePosition = GeoHelper.LatLngAlt(
            imuData.lat, imuData.lng, imuData.height.toDouble()
        )
        checkFlyModel(imuData.flyMode.toInt())
    }


    fun refresh(localBlockId: Long? = null) {
        exeTask {
            clearFlag.emit(true)
            dronePosition?.let {
                refreshList(
                    position = it, loadLocalComplete = {
                        //如果传入的localBlockId为空，且当前selectedBP不为空，则设置当前已选择的blockPlan
                        if (localBlockId == null) {
                            if (selectedBP != null) {
                                setBP(selectedBP!!)
                            }
                        } else {//如果传入的localBlockId不为空，则根据localBlockId查找对应的blockPlan，并设置为当前已选择的blockPlan
                            localBlocksListFlow.value.find { it.localBlockId == localBlockId }
                                ?.let { bp ->
                                    setBP(bp)
                                }
                        }
                    })
            }
        }
    }

    fun pushCanvasFlow() {
        exeTask {
            clearAndPushCanvasFlow()
        }
    }

    fun stopCanvasFlow() {
        exeTask {
            stopAndClearCanvasFlow()
            clearFlag.emit(true)
        }
    }

    fun setBP(blockPlan: BlockPlan) {
        exeTask {
            setBlockPlan(blockPlan)
            highlightBlocks.emit(listOf(blockPlan))
            //查询当前地块断点
            getBreakpoint()
        }
    }

    fun clearBP() {
        exeTask {
            clearBlockPlan()
            highlightBlocks.emit(listOf())
        }
    }

    fun deleteLocalBlock(localBlockId: Long, complete: () -> Unit) {
        exeTask {
            deleteBlock(localBlockId = localBlockId, complete = complete)
        }
    }

    fun renameLocalBlock(localBlockId: Long, name: String, complete: () -> Unit) {
        exeTask {
            renameBlock(localBlockId = localBlockId, name = name, complete = complete)
        }
    }

    private var preFlyMode = 0

    private fun checkFlyModel(mode: Int) {
        if (VKAgTool.isNavigation(mode)) preFlyMode = mode
        if (VKAgTool.isNavigation(preFlyMode) && mode != preFlyMode) {
            if (selectedLocalBlockId == null) {
                DroneModel.activeDrone?.getBreakPoint()
            } else {
                DroneModel.getBreakPoint(selectedLocalBlockId!!)
            }
            preFlyMode = mode
        }
    }

    fun getBreakpoint() {
        //优先判断本地数据库有没有，没有则判断架次有没有，都没有就是没有断点
        getLocalBreakpoint {
            if (it != null) {
                breakPoint = it
                return@getLocalBreakpoint
            }
            if (selectedBP?.additional?.bk != null) {
                breakPoint = selectedBP?.additional?.bk
                return@getLocalBreakpoint
            }
            breakPoint = null
        }
    }

    private fun getLocalBreakpoint(complete: (VKAg.BreakPoint?) -> Unit) {
        exeTask {
            selectedLocalBlockId?.let {
                getLocalBK(localBlockId = it, complete = { bk ->
                    complete(bk)
                })
            }
        }
    }

    fun deleteLocalBreakpoint() {
        exeTask {
            selectedLocalBlockId?.let {
                deleteLocalBK(localBlockId = it)
            }
            breakPoint = null
        }
    }
}