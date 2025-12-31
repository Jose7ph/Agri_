package com.jiagu.ags4.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.TemplateParam
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.work.BlockParam
import com.jiagu.ags4.vm.work.IWorkParameter
import com.jiagu.ags4.vm.work.IWorkParameterTemplate
import com.jiagu.ags4.vm.work.WorkParameterImpl
import com.jiagu.ags4.vm.work.WorkParameterTemplateImpl


class ManualModel(app: Application) : AndroidViewModel(app),
    IWorkParameter by WorkParameterImpl(),
    IWorkParameterTemplate by WorkParameterTemplateImpl() {
    val context = getApplication<Application>()

    fun removeTemplate(paramId: Long, complete: (Boolean) -> Unit) {
        exeTask {
            deleteTemplate(paramId = paramId, complete = complete)
        }
    }

    fun saveTemplateData(name: String, complete: (Boolean) -> Unit) {
        exeTask {
            val templateData = buildTemplatePlan()
            saveTemplateData(
                name = name,
                type = Constants.TYPE_PARAM_PLAN,
                templateData = templateData,
                complete = complete
            )
        }
    }

    fun getTemplateParamList(complete: (Boolean, List<TemplateParam>?) -> Unit) {
        exeTask {
            getTemplateParamList(type = Constants.TYPE_PARAM_PLAN, complete = complete)
        }
    }

    fun setTemplate(templateParam: TemplateParam, complete: (Boolean, List<() -> Unit>?) -> Unit) {
        exeTask {
            val blockParam =
                BlockParam(
                    blockType = Block.TYPE_BLOCK,
                    rotationalSpeed = rotationalSpeed,
                    sprayOrSeedMu = sprayOrSeedMu,
                    pumpOrValveSize = pumpOrValveSize,
                    mode = mode
                )
            setTemplate(
                blockParam = blockParam,
                templateParam = templateParam,
            ) { success, bp ->
                val commands = bp?.let { buildCommands(it) }
                complete(success, commands)
            }
        }
    }

    private fun buildCommands(blockParam: BlockParam): List<() -> Unit> {
        val commands = mutableListOf<() -> Unit>()
        commands.add { AptypeUtil.setPumpMode(blockParam.mode) }
        commands.add { AptypeUtil.setPumpAndValve(blockParam.pumpOrValveSize.toFloat()) }
        commands.add { AptypeUtil.setSprayMu(blockParam.sprayOrSeedMu) }
        commands.add { AptypeUtil.setCenAndSeedSpeed(blockParam.rotationalSpeed.toFloat()) }
        return commands
    }
}