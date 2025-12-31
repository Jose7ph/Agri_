package com.jiagu.ags4.scene.factory

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jiagu.ags4.Constants.TYPE_PARAM_DRONE
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.DroneParam
import com.jiagu.api.ext.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

val LocalFactoryModel = compositionLocalOf<FactoryModel> { error("No FactoryModel provided") }

class FactoryModel(application: Application) : AndroidViewModel(application) {

    val context = getApplication<Application>()

    //高级设置默认菜单
    var selectedMenuId: FactoryTypeEnum by mutableStateOf(FactoryTypeEnum.FACTORY_TYPE_MODEL)

    //参数列表
    var paramList = mutableStateListOf<DroneParam>()

    //参数上传下载按钮状态控制
    var uploadAndDownloadEnabled by mutableStateOf(true)

    companion object {
        private const val PARAM_EXT = ".param"
    }

    fun getParamFile(fileName: String): File {
        val file = context.getExternalFilesDir("param")
        return File(file, "${fileName}${PARAM_EXT}")
    }

    /**
     * 上传参数->导入
     * 下载参数->保存
     */
    fun uploadParam(complete: (Boolean) -> Unit) {
        viewModelScope.launch {
            AgsNet.getDroneParams(TYPE_PARAM_DRONE).networkFlow {
                context.toast(it)
                complete(false)
            }.collectLatest { params ->
                //清除记录
                paramList.clear()
                if (params.isEmpty()) {
                    context.toast(context.getString(R.string.ver_param_not_found))
                    complete(true)
                    return@collectLatest
                }
                params.forEach {
                    paramList.add(it)
                }
                complete(true)
            }
        }
    }

    /**
     * 删除参数
     *
     * @param paramId 参数id
     */
    fun deleteTemplate(paramId: Long) {
        viewModelScope.launch {
            AgsNet.deleteDroneParam(paramId).networkFlow {
                context.toast(it)
            }.collectLatest {
                //删除记录
                paramList.removeIf { param ->
                    param.paramId == paramId
                }
                context.toast(context.getString(R.string.success))
            }
        }
    }

    /**
     * 上传参数到FCU
     *
     * @param paramId 参数id
     * @param complete 成功回调
     */
    fun uploadParamToFCU(paramId: Long, complete: (DroneParam) -> Unit) {
        viewModelScope.launch {
            AgsNet.getDroneParam(paramId).networkFlow {
                context.toast(it)
            }.collectLatest { droneParam ->
                complete(droneParam)
            }
        }
    }
}

/**
 * 菜单类型enum
 */
enum class FactoryTypeEnum {
    FACTORY_TYPE_MODEL, //机型设置
    FACTORY_TYPE_INSTALL, //安装设置
    FACTORY_TYPE_PARAMETER, //参数设置
    FACTORY_TYPE_RC, //遥控器设置
    FACTORY_TYPE_MOTOR, //电机设置
}