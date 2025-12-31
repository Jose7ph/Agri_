package com.jiagu.ags4

import android.Manifest
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.db.AgsDB.syncNoFlyZone
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.AgsNet.process
import com.jiagu.ags4.repo.net.model.AppUIConfig
import com.jiagu.ags4.repo.net.model.Group
import com.jiagu.ags4.repo.net.model.ServiceResult
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.repo.sp.UserConfig
import com.jiagu.ags4.scene.device.DeviceManagementActivity
import com.jiagu.ags4.scene.factory.FactoryActivity
import com.jiagu.ags4.scene.login.LoginActivity
import com.jiagu.ags4.scene.mine.MineActivity
import com.jiagu.ags4.scene.work.MapVideoActivity
import com.jiagu.ags4.ui.components.EasyDataButtons
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.getPrimaryButtonColors
import com.jiagu.ags4.utils.V9Util.canGnssUpgrade
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.utils.initUnit
import com.jiagu.ags4.utils.registerPhone
import com.jiagu.ags4.utils.startActivity
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.GimbalModel
import com.jiagu.ags4.vm.WhiteList
import com.jiagu.ags4.vm.task.MainCheckTask
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.PackageHelper
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.api.viewmodel.runJob
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.controller.eav.registerEavController
import com.jiagu.device.controller.siyi.registerSiYiController
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.button.ImageHorizontalButton
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ListSelectionPopup
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.splash.SplashScreen
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.Locale
import android.graphics.Color as AndroidColor

class MainActivity : BaseComponentActivity() {
    private val mainVm: MainVm by viewModels()

    //开始作业
    var buttonEnabled by mutableStateOf(true)
    var deviceUseSaveFlag by mutableStateOf(false)

    //用于控制需要在lazyinit执行之后执行方法的地方
    var initEnd by mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        buttonEnabled = true
        DroneModel.activeDrone?.getSortieList()
        //获取团队 todo 权限
        if (initEnd) {
            mainVm.getWorkGroups()
        }
    }

    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            if (mainVm.initialized.value) {
                Greeting()
            } else {
                SplashScreen(uri = uri2) {
                    mainVm.done()
                }
                LaunchedEffect(Unit) {
                    delay(2600)  // 3秒延迟
                    if (!mainVm.initialized.value) {
                        mainVm.done()
                    }

                }
            }

        }
    }

    private var uri: Uri? = null
    private var uri2: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        findManufacturerUri()

        super.onCreate(savedInstanceState)
        requestPermissionsOrFinish {
            lazyInit()
            startProgress(MainCheckTask()) { _, _ ->
//                checkUpgrade(DroneModel.verData.value)
                false
            }
//            startProgress(UpgradeAppTask()) { success, msg ->
//                true
//            }
        }
        addObserver()
        LogFileHelper.log(
            "APP: ${PackageHelper.getAppVersionName(applicationContext)}(${
                PackageHelper.getAppVersionCode(
                    applicationContext
                )
            })"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        DroneModel.close()
    }

    private fun addObserver() {
        progressModel.progress.observe(this) {
            if (it == null) hideDialog()
            when (it) {
                is ProgressModel.ProgressMessage -> {
                    showDialog {
                        ScreenPopup(content = {
                            Box(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = it.text, style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }, showCancel = true, showConfirm = false, onDismiss = {
                            progressModel.next(0)
                            hideDialog()
                        })
                    }
                }

                is ProgressModel.ProgressNotice -> {
                    showDialog {
                        ScreenPopup(
                            width = 480.dp,
                            content = {
                                Box(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxWidth()
                                ) {
                                    LazyColumn(modifier = Modifier.heightIn(40.dp, 200.dp)) {
                                        if (it.content == null) {
                                            item {
                                                Text(
                                                    text = it.title,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                        it.content?.let { content ->
                                            item {
                                                MarkdownText(
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .fillMaxWidth(),
                                                    markdown = content,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            }
                                        }
                                    }


                                }
                            },
                            showCancel = true,
                            showConfirm = true,
                            onDismiss = { progressModel.next(0) },
                            onConfirm = { progressModel.next(1) })
                    }
                }

                is ProgressModel.ProgressResult -> {
                    val processed = taskComplete?.invoke(it.success, it.msg) ?: false
                    if (!processed && it.msg != null) {
                        if (it.msg!!.contains("apk")) installApk(it.msg!!)
                        else toast(it.msg!!)
                    }
                    hideDialog()
                    progressModel.done()
                }
            }
        }
        DroneModel.verData.observe(this) {
            //飞机id不为空 && 有网络
            if (it.serial.isNotEmpty() && AgsUser.netIsConnect && !deviceUseSaveFlag) {
                //先修改保存标识 防止多次触发保存/更新
                deviceUseSaveFlag = true
                mainVm.saveDeviceUseRecord(
                    deviceId = it.serial,
                    appVersion = PackageHelper.getAppVersionName(applicationContext)
                        .toString() + "-" + PackageHelper.getAppVersionCode(applicationContext),
                ) { result ->
                    //保存失败 设置为false
                    if (!result) {
                        deviceUseSaveFlag = false
                    }
                }
            }
        }
    }

    private fun findManufacturerUri() {
        val dir = File(Environment.getExternalStorageDirectory(), "ags4Config")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file1 = File(dir, "bg.png")
        if (file1.exists()) {
            uri = FileProvider.getUriForFile(this, "com.jiagu.ags4.fileprovider", file1)
            Log.d("yuhang", "onCreate: $uri")
        }
        val file2 = File(dir, "splash.png")
        if (file2.exists()) {
            uri2 = FileProvider.getUriForFile(this, "com.jiagu.ags4.fileprovider", file2)
            Log.d("yuhang", "onCreate: $uri2")
        }
        val file3 = File(dir, "splash.mp4")
        if (file3.exists()) {
            uri2 = FileProvider.getUriForFile(this, "com.jiagu.ags4.fileprovider", file3)
            Log.d("yuhang", "onCreate: $uri2")
        }
        val themeFile = File(dir, "theme.json")
        if (themeFile.exists()) {
            try {
                val json = themeFile.readText()
                Gson().fromJson(json, AppUIConfig::class.java).apply {
                    if (theme != null) {
                        AppConfig(this@MainActivity).primaryColor =
                            Color(AndroidColor.parseColor(theme))
                    }

                }
            } catch (e: Throwable) {
                e.printStackTrace()
                themeFile.delete()
            }

        }
    }

    @Composable
    fun Greeting() {
        val context = LocalContext.current
        val connectionState = DroneModel.droneConnectionState.observeAsState(false)
        val buttonHeight = 70.dp
        val buttonWidth = 160.dp
        val imageSize = 24.dp
        var showEasyData by rememberSaveable {
            mutableIntStateOf(0)
        }
        val isService = AgsUser.userInfo?.isService() == true
        // GPS强制升级
        DroneModel.deviceList.observe(this) {
            it.forEach { device ->
                var ver = 0
                try {
                    ver = device.swId.toInt()
                } catch (e: Throwable) {
                    e.printStackTrace()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
                if (device.devType == VKAgCmd.DEVINFO_GNSS && canGnssUpgrade(ver)) {
                    context.showDialog {
                        ScreenPopup(
                            width = 480.dp,
                            content = {
                                Box(
                                    modifier = Modifier
                                        .padding(20.dp)
                                        .fillMaxWidth()
                                ) {
                                    LazyColumn(modifier = Modifier.heightIn(40.dp, 200.dp)) {
                                        item {
                                            Text(
                                                text = "GNSS " + stringResource(R.string.force_upgrade),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                            },
                            showConfirm = true,
                            onConfirm = {
                                // 跳转到升级页面
                                context.hideDialog()
                                checkLogin { startActivity(DeviceManagementActivity::class.java) }
                            },
                            showCancel = true,
                            onDismiss = {
                                context.hideDialog()
                            }
                        )
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.onPrimary
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (showEasyData == 10) {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp, top = 10.dp)
                            .align(Alignment.TopStart)
                            .zIndex(1f)
                    ) {
                        EasyDataButtons()
                    }
                }
                if (uri == null) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.main_title),
                        contentDescription = "background",
                        contentScale = ContentScale.FillBounds
                    )
                } else {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = uri,
                        contentDescription = "background",
                        contentScale = ContentScale.FillBounds
                    )
                }
                val auth = AgsUser.userInfo?.canTuneAdvParam() ?: false

                //我的
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .width(buttonWidth)
                        .align(Alignment.TopEnd)
                        .background(color = BlackAlpha, shape = MaterialTheme.shapes.medium)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ImageHorizontalButton(
                        text = AgsUser.userInfo?.userName
                            ?: stringResource(id = R.string.mine_title),
                        image = R.drawable.default_mine_avatar,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = {
                            buttonEnabled = false
                            checkLogin {
                                checkLogin { startActivity(MineActivity::class.java) }
                            }
                        },
                        enabled = buttonEnabled,
                        fontStyle = MaterialTheme.typography.titleMedium,
                        imageSize = imageSize,
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                    //团队
                    if (isService && mainVm.teams.isNotEmpty()) {
                        Box(modifier = Modifier.noEffectClickable {
                            showDialog {
                                ChooseTeamPopup()
                            }
                        }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AutoScrollingText(
                                        text = mainVm.curTeamName.ifEmpty {
                                            stringResource(
                                                R.string.team_not_selected
                                            )
                                        },
                                        modifier = Modifier,
                                        textAlign = TextAlign.Center,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            //设备管理
                            Box(
                                modifier = Modifier
                                    .width(buttonWidth)
                                    .height(buttonHeight)
                                    .clip(MaterialTheme.shapes.medium),
                            ) {
                                ImageHorizontalButton(
                                    text = stringResource(id = R.string.device_management),
                                    image = R.drawable.default_device_management,
                                    textColor = MaterialTheme.colorScheme.onPrimary,
                                    onClick = {
                                        buttonEnabled = false
                                        checkLogin { startActivity(DeviceManagementActivity::class.java) }
                                    },
                                    enabled = buttonEnabled,
                                    fontStyle = MaterialTheme.typography.titleMedium,
                                    imageSize = imageSize,
                                    colors = ButtonDefaults.buttonColors().copy(
                                        containerColor = BlackAlpha
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (auth) {
                                //工厂模式
                                Box(
                                    modifier = Modifier
                                        .width(buttonWidth)
                                        .height(buttonHeight)
                                        .clip(MaterialTheme.shapes.medium),
                                ) {
                                    ImageHorizontalButton(
                                        text = stringResource(id = R.string.factory_mode),
                                        image = R.drawable.default_factory_mode,
                                        textColor = MaterialTheme.colorScheme.onPrimary,
                                        onClick = {
                                            buttonEnabled = false
                                            checkLogin {
                                                startActivity(FactoryActivity::class.java)
                                            }
                                        },
                                        enabled = buttonEnabled,
                                        fontStyle = MaterialTheme.typography.titleMedium,
                                        imageSize = imageSize,
                                        colors = ButtonDefaults.buttonColors().copy(
                                            containerColor = BlackAlpha
                                        ),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .height(150.dp)
                                .width(buttonWidth + 90.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(
                                modifier = Modifier
                                    .width(buttonWidth + 70.dp)
                                    .align(Alignment.End),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(showEasyData < 10 && connectionState.value) {
                                            showEasyData++
                                        }) {
                                    AutoScrollingText(
                                        text = stringResource(id = if (connectionState.value) R.string.main_device_connected else R.string.main_device_disconnected),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = if (connectionState.value) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .height(buttonHeight)
                                    .width(buttonWidth + 70.dp)
                                    .align(Alignment.End)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Button(
                                    onClick = {
                                        checkStartWork {
                                            buttonEnabled = false
                                            startActivity(MapVideoActivity::class.java)
                                        }
                                    },
                                    colors = getPrimaryButtonColors(),
                                    enabled = buttonEnabled,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.go_work),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun checkStartWork(complete: () -> Unit) {
        //检查团队
        checkTeamSelected {
            LogFileHelper.log("checkStartWork -> checkTeamSelected : success")
            complete()
        }

    }

    private fun checkTeamSelected(onSuccess: () -> Unit) {
        //团队判断
        val isService = AgsUser.userInfo?.isService() == true
        //是运营人 && 当前有团队
        if (isService && mainVm.teams.isNotEmpty()) {
            // 当前团队未选择 弹出选择框
            if (AgsUser.workGroup == null || DroneModel.groupId == null) {
                showDialog {
                    ChooseTeamPopup {
                        onSuccess()
                    }
                }
            } else {
                onSuccess()
            }
        } else {
            onSuccess()
        }
    }

    private fun lazyInit() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("yuhang", "lazyInit started")
                WhiteList.initWhiteList(application)
                val t0 = System.currentTimeMillis()
                val dir = getExternalFilesDir("applog")
                LogFileHelper.initialize(dir, "ags4")
                AgsDB.initialize(this@MainActivity)
                val config = DeviceConfig(applicationContext)
                if (config.rtspurl.isNotBlank()) {
                    ControllerFactory.rtspUrl = config.rtspurl
                }
                if (config.rtspurl2.isNotBlank()) {
                    ControllerFactory.rtspUrl2 = config.rtspurl2
                }
                ControllerFactory.registerPhone()
                ControllerFactory.registerEavController()
                ControllerFactory.registerSiYiController()
                ControllerFactory.dumpModel()
                DroneModel.bindController()
                DroneModel.connectLastDevice(this@MainActivity.application)
                GimbalModel.initGimbal(this@MainActivity)
                UnitHelper.initUnit(this@MainActivity)
                val t1 = System.currentTimeMillis()
                if (uri2.toString().endsWith("png") && t1 - t0 < 3000) {
                    delay(3000 - t0 + t1)
                }
            }
            Log.d("yuhang", "connected ${DroneModel.droneConnectionState.value}")
            Log.d("yuhang", "lazyInit done")
            if (uri2.toString().endsWith("png")) {
                mainVm.done()
            }
            //检查实名认证
            mainVm.checkIdentity()
            mainVm.sync()
            //同步禁飞区
            syncNoFlyZone()
            //获取团队
            mainVm.getWorkGroups()
            initEnd = true
        }
    }

    private fun requestPermissionsOrFinish(block: () -> Unit) {
        val permissions = mutableListOf(
            Manifest.permission.INTERNET,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        requestPermissions(*permissions.toTypedArray()) { granted ->
            if (granted) block()
            else finish()
        }
    }

    private var permissionListener: ((Boolean) -> Unit)? = null
    private fun requestPermissions(vararg permissions: String, block: (Boolean) -> Unit) {
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            Log.d("yuhang", "requestPermissions: passed")
            block(true)
        } else {
            permissionListener = block
            EasyPermissions.requestPermissions(this, "权限不满足要求", 54321, *permissions)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionListener?.let { it(EasyPermissions.hasPermissions(this, *permissions)) }
    }

    private fun checkLogin(block: () -> Unit) {
        if (AgsUser.userInfo == null) startActivity(LoginActivity::class.java)
        else block()
    }


    @Composable
    private fun ChooseTeamPopup(onConfirm: () -> Unit = {}) {
        val userConfig = UserConfig(this)
        ListSelectionPopup(
            title = stringResource(R.string.current_work_team),
            list = mainVm.teams,
            defaultIndexes = if (mainVm.curTeamIndex != -1) listOf(
                mainVm.curTeamIndex
            ) else listOf(),
            item = { team ->
                AutoScrollingText(
                    text = team.groupName,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            itemPaddingValues = PaddingValues(
                horizontal = 60.dp, vertical = 4.dp
            ),
            onDismiss = {
                mainVm.curTeamName = ""
                mainVm.curTeamIndex = -1
                userConfig.group = 0
                DroneModel.groupId = null
                AgsUser.workGroup = null
                onConfirm()
                hideDialog()
            },
            onConfirm = { idx, value ->
                val changeIndex = idx[0]
                val changeTeam = value[0]
                mainVm.curTeamName = changeTeam.groupName
                mainVm.curTeamIndex = changeIndex
                userConfig.group = changeTeam.groupId
                DroneModel.groupId = changeTeam.groupId
                AgsUser.workGroup = changeTeam
                onConfirm()
                hideDialog()
            })
    }
}

class MainVm(app: Application) : AndroidViewModel(app) {
    val context = getApplication<Application>()
    val initialized = mutableStateOf(false)
    val teams = mutableStateListOf<Group>()
    var curTeamName by mutableStateOf("")
    var curTeamIndex by mutableIntStateOf(-1)

    //false 需要实名认证
    var verifyState by mutableStateOf(true)
    fun done() {
        initialized.value = true
    }

    fun sync() {
        viewModelScope.launch {
            AgsDB.sync()
        }
    }

    //保存/更新设备使用记录
    fun saveDeviceUseRecord(deviceId: String, appVersion: String, complete: (Boolean) -> Unit) {
        viewModelScope.launch {
            var userId: Long? = null
            if (AgsUser.userInfo != null) {
                userId = AgsUser.userInfo!!.userId
            }
            AgsNet.saveDeviceUseRecord(
                deviceId = deviceId, userId = userId, appVersion = appVersion
            ).networkFlow {
                Log.e("zhy", "saveDeviceUseRecord error,$it")
                complete(false)
            }.collectLatest {
                complete(true)
            }
        }
    }

    //用户实名认证
    fun identityVerify(
        idcardNum: String, contactName: String, complete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            AgsNet.identityVerify(
                idcardNum = idcardNum, contactName = contactName
            ).networkFlow {
                val serviceResult =
                    Gson().fromJson<ServiceResult<Any>>(it, ServiceResult::class.java)
                complete(false, serviceResult.msg)
            }.collectLatest {
                complete(it, "")
            }
        }
    }

    //检查实名认证
    fun checkIdentity() {
        //没联网不检查
        if (!AgsUser.netIsConnect) {
            return
        }
        val user = AgsUser.userInfo
        //判断设备当前语言 中文才需要认证
        val language = Locale.getDefault().language
        //不是中文 || 没有登录  则默认通过
        if (language != "zh" || user == null) {
            return
        }
        //已登录 && 中文
        val userId = user.userId.toString()
        //判断本地是否已经存储当前用户的实名状态
        val userConfig = UserConfig(context)
        //本地文件不存在当前id或未实名，则通过网络查询
        if (!userConfig.getIdentity(userId)) {
            runJob {
                AgsNet.checkVerify().networkFlow { Log.e("zhy", "checkIdentity error, $it") }
                    .collectLatest {
                        verifyState = it.verifyState
                        //已实名则保存至本地文件 下次不需要再次检查
                        if (it.verifyState) {
                            userConfig.setIdentity(userId)
                        }
                    }
            }
        }
    }

    //获取团队
    fun getWorkGroups() {
        val userConfig = UserConfig(context)
        exeTask {
            teams.clear()
            process(work = {
                AgsNet.getGroups(0, 10000).networkFlow {
                    Log.e("zhy", "getWorkGroups error, $it ")
                }.collectLatest {
                    val groups = it.list.map { team ->
                        Group(team.groupId, team.groupName)
                    }
                    AgsDB.saveGroupInfo(groups)
                    teams.addAll(groups)
                }
            }, local = {
                teams.addAll(AgsDB.getGroups())
            })
            val teamWithIndex = teams.withIndex().firstOrNull { (_, group) ->
                group.groupId == userConfig.group
            }
            if (teamWithIndex != null) {
                DroneModel.groupId = teamWithIndex.value.groupId
                AgsUser.workGroup = teamWithIndex.value
                curTeamName = teamWithIndex.value.groupName
                curTeamIndex = teamWithIndex.index
            } else {
                DroneModel.groupId = null
                AgsUser.workGroup = null
                curTeamName = ""
                curTeamIndex = -1
            }
        }
    }

}
