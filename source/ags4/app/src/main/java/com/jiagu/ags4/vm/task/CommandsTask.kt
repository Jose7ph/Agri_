package com.jiagu.ags4.vm.task

import android.util.Log
import com.jiagu.ags4.R
import com.jiagu.ags4.vm.DroneObject
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.task.UploadTool
import kotlinx.coroutines.withTimeoutOrNull

class CommandsTask(
    val drone: DroneObject, private val commands: List<() -> Unit>, private val
    timeoutMillis: Long = 2000
) : ProgressTask() {

    override suspend fun start(): Pair<Boolean, String?> {
        var curIndex = 0
        var r: Pair<Boolean, String?>
        while (curIndex < commands.size) {
            Log.v("lee", "curIndex: $curIndex")

            // 为每个 CommandTask 添加超时控制
            r = withTimeoutOrNull(timeoutMillis) {
                startChild(UploadTool.CommandTask(drone, commands[curIndex]))
            } ?: (false to "Command timed out after $timeoutMillis milliseconds")

            // 判断执行结果
            if (r.first) {
                curIndex++
            } else {
                Log.v(
                    "lee",
                    "Command failed or timed out at index: $curIndex with message: ${r.second}"
                )
                break
            }
        }
        return (curIndex == commands.size) to if (curIndex == commands.size) null else context
            .getString(R.string.fail)
    }
}