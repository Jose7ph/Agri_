package com.jiagu.ags4.vm.work

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.vm.DroneModel
import kotlinx.coroutines.flow.MutableStateFlow


interface IWorkPlan {
    val selectedBPFlow: MutableStateFlow<List<BlockPlan>>
    var selectedBP: BlockPlan?
    var selectedLocalBlockId: Long?
    var selectedBlockId: Long?
    var workPlan: Plan?
    var isNewPlan: Boolean
    suspend fun setBlockPlan(blockPlan: BlockPlan)
    suspend fun getLocalBlockPlan(complete: (BlockPlan?) -> Unit)
    suspend fun clearBlockPlan()
    suspend fun makePlan(plan: Plan, complete: (Plan) -> Unit)
    suspend fun savePlan(workPlan: Plan, complete: () -> Unit)
    suspend fun updatePlan(workPlan: Plan, complete: () -> Unit)
    suspend fun updatePlan2(workPlan: Plan, complete: () -> Unit)
    suspend fun blockWorking(localBlockId: Long, complete: () -> Unit)
    suspend fun blockFinish(localBlockId: Long)
    fun updateSelectedPlan(plan: Plan)
    fun updateBlockPlan(blockPlan: BlockPlan)
}

class WorkPlanImpl() : IWorkPlan {
    override val selectedBPFlow = MutableStateFlow<List<BlockPlan>>(listOf()) //canvas用
    override var selectedBP by mutableStateOf<BlockPlan?>(null)

    override var selectedLocalBlockId by mutableStateOf<Long?>(null)
    override var selectedBlockId by mutableStateOf<Long?>(null)
    override var workPlan: Plan? = null
    override var isNewPlan = true

    override suspend fun setBlockPlan(blockPlan: BlockPlan) {
        selectedBPFlow.emit(listOf(blockPlan))
        selectedBP = blockPlan
        selectedLocalBlockId = blockPlan.localBlockId
        selectedBlockId = blockPlan.blockId
        DroneModel.blockPlan.emit(blockPlan)
    }

    override suspend fun getLocalBlockPlan(complete: (BlockPlan?) -> Unit) {
        if(selectedLocalBlockId == null) complete(null)
        Repo.getBlockPlan(selectedLocalBlockId!!).collect {
            complete(it)
        }
    }

    override suspend fun clearBlockPlan() {
        selectedBPFlow.emit(emptyList())
        selectedBP = null
        selectedLocalBlockId = null
        selectedBlockId = null
    }

    override suspend fun makePlan(plan: Plan, complete: (Plan) -> Unit) {
        //判断新增plan还是更新plan
        if (selectedBP?.plan != null) {
            plan.planId = selectedBP!!.plan!!.planId
            plan.localPlanId = selectedBP!!.plan!!.localPlanId
            updatePlan(plan) {
                DroneModel.blockId = selectedBlockId ?: 0
                DroneModel.localBlockId = selectedLocalBlockId ?: 0
                DroneModel.planId = plan.planId
                DroneModel.localPlanId = plan.localPlanId
                complete(plan)
            }
        } else {
            savePlan(plan) {
                DroneModel.blockId = selectedBlockId ?: 0
                DroneModel.localBlockId = selectedLocalBlockId ?: 0
                DroneModel.planId = plan.planId
                DroneModel.localPlanId = plan.localPlanId
                complete(plan)
            }
        }
    }

    override suspend fun savePlan(workPlan: Plan, complete: () -> Unit) {
        Repo.savePlan(workPlan).collect {
            complete()
        }
    }

    override suspend fun updatePlan(workPlan: Plan, complete: () -> Unit) {
        Repo.updatePlan(workPlan).collect {
            complete()
        }
    }

    override suspend fun updatePlan2(
        workPlan: Plan,
        complete: () -> Unit,
    ) {
        Repo.updatePlan2(workPlan).collect {
            complete()
        }
    }

    override suspend fun blockWorking(localBlockId: Long, complete: () -> Unit) {
        Repo.blockWorking(localBlockId).collect {
            complete()
        }
    }

    override suspend fun blockFinish(localBlockId: Long) {
        Repo.workFinish(localBlockId)
    }

    override fun updateSelectedPlan(plan: Plan) {
        //保存 or 更新 plan之后 将当前选择的block的plan设置为当前plan
        selectedBP?.let {
            it.plan = plan
            selectedBPFlow.value = listOf(it)
        }
    }

    override fun updateBlockPlan(blockPlan: BlockPlan) {
        selectedBP = blockPlan
        selectedBPFlow.value = listOf(blockPlan)
    }
}