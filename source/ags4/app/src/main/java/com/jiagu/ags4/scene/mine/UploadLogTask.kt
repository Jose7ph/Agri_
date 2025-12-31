package com.jiagu.ags4.scene.mine

import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.model.AppLog
import com.jiagu.api.viewmodel.ProgressTask
import java.io.File

class UploadLogTask(val file: File, val log: AppLog) : ProgressTask() {
    override suspend fun start(): Pair<Boolean, String?> {
        try {
            if (!AgsUser.netIsConnect) {
                throw Throwable(getString(R.string.err_network))
            }
            postProgress(getString(R.string.uploading))
            AgsNet.uploadAppLog(file, log)
            file.delete()//删除压缩文件
            return true to getString(R.string.upload_success)
        } catch (e: Throwable) {
            return false to "${e.message}"
        }
    }

}

class UploadFcuLogTask(val droneId: String, val time: Long, val file: File) : ProgressTask() {
    override suspend fun start(): Pair<Boolean, String?> {
        try {
            if (!AgsUser.netIsConnect) {
                throw Throwable(getString(R.string.err_network))
            }
            postProgress(getString(R.string.uploading))
            AgsNet.uploadLog(droneId, time, file)
            return true to getString(R.string.upload_success)
        } catch (e: Throwable) {
            return false to "${e.message}"
        }
    }
}