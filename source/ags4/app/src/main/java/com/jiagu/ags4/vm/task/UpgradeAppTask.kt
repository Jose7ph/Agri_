package com.jiagu.ags4.vm.task

import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.vm.UpgradeHelper
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.tools.vm.DownloadTask

class UpgradeAppTask : ProgressTask() {
    override suspend fun start(): Pair<Boolean, String?> {
        val (code, version) = UpgradeHelper.checkAll(context)
        if (code > 0 && Config(context).autoCheckUpgrade) {
            val result = postNotice(getString(com.jiagu.v9sdk.R.string.main_found_new_version), version!!.content)
            if (result == 1) {
                val (success, msg) = startChild(
                    DownloadTask(
                        version.url,
                        getString(com.jiagu.v9sdk.R.string.main_new_station),
                        null,
                        "ags4.apk"
                    )
                )
                if (success && msg!!.endsWith("apk")) {
                    return true to msg
                }
            } else {
                return false to null
            }
        }
        return true to null
    }

}