package com.jiagu.ags4.vm.task

import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.voice.VoiceService
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.tools.vm.VoiceTask

class MainCheckTask : ProgressTask() {
    override suspend fun start(): Pair<Boolean, String?> {
        val upgradeAppTask = UpgradeAppTask()
        val (success, msg) = startChild(upgradeAppTask)
        startChild(VoiceTask())
        if (AppConfig(context).voice == 1) {
            VoiceService.start(context)
        }
        if (success) {
            return true to msg
        }
        return true to null
    }
}