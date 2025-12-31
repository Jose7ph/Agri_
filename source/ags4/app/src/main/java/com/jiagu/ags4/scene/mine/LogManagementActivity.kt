package com.jiagu.ags4.scene.mine

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BaseComponentActivity
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.AppLog
import com.jiagu.ags4.repo.net.model.LOG_TYPE_APP
import com.jiagu.ags4.repo.net.model.LOG_TYPE_CRASH
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.LocalLogModel
import com.jiagu.ags4.vm.LogFile
import com.jiagu.ags4.vm.LogModel
import com.jiagu.api.ext.toMillis
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.PackageHelper
import com.jiagu.api.helper.ZipHelper
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.task.LogV9Task
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.textfield.NormalTextField
import kotlinx.coroutines.launch
import java.io.File

class LogManagementActivity : BaseComponentActivity() {

    private val logVM: LogModel by viewModels()
    private var appLogFile: File? = null
    private var crashLogFile: File? = null
    private var siyiLogFile: File? = null
    private val appLogName = "applog"
    private val crashLogName = "tombstones"
    private val siyiLogName = "SLog/com.siyi.imagetransmission/"
    private var zipFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLogFile = this.getExternalFilesDir(appLogName)
        crashLogFile = this.getExternalFilesDir(crashLogName)
        siyiLogFile = File(Environment.getExternalStorageDirectory(), siyiLogName)
        logVM.getData()
        progressModel.progress.observe(this) {
            when (it) {
                is ProgressModel.ProgressMessage -> displayProgress(it.text)
                is ProgressModel.ProgressNotice -> showNoticeDialog(it.title, it.content)
                is ProgressModel.ProgressResult -> handleTaskResult(it.success, it.msg)
            }
        }
    }

    @Composable
    override fun Content() {
        CompositionLocalProvider(LocalLogModel provides logVM) {
            LogManagement() { finish() }
        }
    }


    fun uploadFile(item: LogFile) {
        val fileLog = when (logVM.curPage) {
            LogModel.LOG_APP -> appLogFile
            LogModel.LOG_APP_CRASH -> crashLogFile
//            LogModel.LOG_APP_SIYI -> siyiLogFile
            else -> null
        }

        val fileName = item.fileName

        val zipPath = ZipHelper.zipFile(fileLog!!.path, fileName)

        zipFile = File(zipPath)
        if (zipPath == null) {
            toast(applicationContext.getString(R.string.upload_fail))
            return
        }
        showDialog {
            var content by remember { mutableStateOf("") }
            ScreenPopup(content = {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_log_note),
                        style = MaterialTheme.typography.titleLarge
                    )
                    NormalTextField(
                        text = content,
                        onValueChange = {
                            content = it
                        },
                        modifier = Modifier.height(40.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        borderColor = MaterialTheme.colorScheme.outline
                    )
                }
            }, onConfirm = {
                lifecycleScope.launch {
                    logVM.taskType = 1
                    val appLog = AppLog(
                        if (logVM.curPage == LogModel.LOG_APP_CRASH) LOG_TYPE_CRASH
                        else LOG_TYPE_APP,
                        item.timestamp,
                        content,
                        item.fileName,
                        AgsUser.flavor,
                        Build.SUPPORTED_ABIS.joinToString(","),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT.toString(),
                        Build.MODEL,
                        Build.PRODUCT,
                        Build.MANUFACTURER,
                        PackageHelper.getAppVersionCode(applicationContext).toString(),
                        PackageHelper.getAppVersionName(applicationContext),
                        AgsUser.userInfo?.userId ?: 0
                    )
                    val uploadTask = UploadLogTask(zipFile!!, appLog)
                    progressModel.start(uploadTask)
                    hideDialog()
                }
            }, onDismiss = {
                zipFile!!.delete()
                hideDialog()
            })

        }
    }

    fun shareFile(item: LogFile) {
        val fileLog = when (logVM.curPage) {
            LogModel.LOG_APP -> appLogFile
            LogModel.LOG_APP_CRASH -> crashLogFile
//            LogModel.LOG_APP_SIYI -> siyiLogFile
            else -> null
        } ?: return
        val file = File(fileLog.path, item.fileName)
        val auth = "${this.packageName}.fileprovider"
        Log.v("shero", "file:${file.name} ${file.parent} auth:${auth}")
        PackageHelper.shareFile(this, auth, file.absolutePath, getString(R.string.share))
    }

    fun displayProgress(message: String) {
        if (logVM.taskType == 2) {
            logVM.taskProcess = message
        } else {
            toast(message)
        }


    }

    fun showNoticeDialog(title: String, content: String?) {
        toast("$title: $content")
    }

    fun handleTaskResult(success: Boolean, message: String?) {
        toast(
            message ?: applicationContext.getString(
                if (success) R.string.success
                else R.string.fail
            )
        )
        if (logVM.taskType == 1) {
            zipFile?.delete()
        } else if (logVM.taskType == 2) {
            logVM.refreshFcuLog(true)
        }
        logVM.taskType = 0
        hideDialog()
    }

    fun clearLogs() {
        when (logVM.curPage) {
            LogModel.LOG_APP -> clearAppLogs()
            LogModel.LOG_APP_CRASH -> clearCrashLogs()
//            LogModel.LOG_APP_SIYI -> clearSiyiLogs()
            LogModel.LOG_APP_FCU -> clearFcuLogs()
            else -> null
        }
    }

    private fun clearAppLogs() {
        showDialog {
            ScreenPopup(content = {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .height(DIALOG_WIDTH),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_clear_log),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
                onDismiss = {
                    hideDialog()
                },
                onConfirm = {
                    appLogFile?.apply {
                        list()?.forEach { fn ->
                            val file = File(this, fn)
                            file.delete()
                        }
                        logVM.getData()
                    }
                    hideDialog()

                })
        }
    }

    private fun clearCrashLogs() {
        showDialog {
            ScreenPopup(content = {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .height(DIALOG_WIDTH),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_clear_crash_log),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
                onDismiss = {
                    hideDialog()
                },
                onConfirm = {
                    crashLogFile?.apply {
                        list()?.forEach { fn ->
                            val file = File(this, fn)
                            file.delete()
                        }
                        logVM.getData()
                    }
                    hideDialog()

                })
        }
    }

    private fun clearSiyiLogs() {
        showDialog {
            ScreenPopup(content = {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .height(DIALOG_WIDTH),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_clear_siyi_log),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
                onDismiss = {
                    hideDialog()
                },
                onConfirm = {
                    siyiLogFile?.apply {
                        list()?.forEach { fn ->
                            val file = File(this, fn)
                            file.delete()
                        }
                        logVM.getData()
                    }
                    hideDialog()

                })
        }
    }

    private fun clearFcuLogs() {
        showDialog {
            ScreenPopup(content = {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .height(DIALOG_WIDTH),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_clear_fcu_log),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
                onDismiss = {
                    hideDialog()
                },
                onConfirm = {
                    val dir = this.getExternalFilesDir("log")
                    for (log in logVM.logList) {
                        if (log.downloaded) {
                            File(dir, "${log.file}.dat").delete()
                            if (log.fcIdx > 0) {
                                log.downloaded = false
                                log.isCheck = false
                            } else {
                                logVM.logList.remove(log)
                            }
                        }
                    }
                    hideDialog()
                })
        }

    }

    fun downloadFcuLog(name: String, index: Int) {
        showDialog {
            ScreenPopup(content = {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .height(DIALOG_WIDTH),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (logVM.taskType != 2) {
                        Text(
                            text = stringResource(id = R.string.log_download_confirm, name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else {
                        Text(
                            text = logVM.taskProcess,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                }
            },
                showConfirm = logVM.taskType != 2,
                onDismiss = {
                    if (logVM.taskType == 2) {
                        progressModel.cancel()
                        logVM.taskType = 0
                    }
                    hideDialog()
                },
                onConfirm = {
                    DroneModel.activeDrone?.let {
                        logVM.taskType = 2
                        startProgress(LogV9Task(it, index, "$name.dat")) { success, _ ->
                            if (success) logVM.refreshFcuLog(true)//下载完日志之后刷新列表
                            false
                        }
                    }

                })
        }
    }


    fun uploadFcuFile(name: String) {
        val file = File(this.getExternalFilesDir("log"), "${name}.dat")
        val strs = name.split("-")
        val time =
            if (strs.size == 3) strs[2].toMillis("yyyyMMddHHmm") else strs[1].toMillis("yyyyMMddHHmm")
        logVM.taskType = 1
        Log.v("lee", "uploadFcuFile: ${strs} $time ${file.absolutePath}")
        startProgress(buildUploadTask(strs[0], time, file))
    }

    fun shareFcuFile(name: String) {
        val file = File(this.getExternalFilesDir("log"), "${name}.dat")
        val auth = "${this.packageName}.fileprovider"
        PackageHelper.shareFile(this, auth, file.absolutePath, getString(R.string.share))
    }

    fun buildUploadTask(droneId: String, ts: Long, file: File): ProgressTask {
        return UploadFcuLogTask(droneId, ts, file)
    }

    companion object {
        private val DIALOG_WIDTH = 120.dp
    }

}