package com.jiagu.ags4

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.SparseArray
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.api.helper.PackageHelper
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.jgcompose.dialog.DialogViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseComponentActivity : ComponentActivity() {
    lateinit var navController: NavHostController
    private lateinit var controller: WindowInsetsControllerCompat
    val progressModel: ProgressModel by viewModels()
    var taskComplete: ((Boolean, String?) -> Boolean)? = null
    val dialogVM: DialogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // 修改全局的Density
        val displayMetrics = resources.displayMetrics
        when (ControllerFactory.deviceModel) {
            "H20","EAV-RC50" -> displayMetrics.density = 2.5f
        }
        displayMetrics.densityDpi = DisplayMetrics.DENSITY_XHIGH
        setContent {
            ComposeTheme {
                navController = rememberNavController()
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalProgressModel provides progressModel,
                ) {
                    Content()
                }
                val dialogState by dialogVM.dialogState.collectAsState()
                if (dialogState.isVisible) {
                    dialogState.content?.let { it() }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 修改全局的Density
        val displayMetrics = resources.displayMetrics
        when (ControllerFactory.deviceModel) {
            "H20","EAV-RC50" -> displayMetrics.density = 2.5f
        }
        displayMetrics.densityDpi = DisplayMetrics.DENSITY_XHIGH
    }

    @Composable
    abstract fun Content()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let { baseContext ->
            val overrideConfiguration = Configuration(baseContext.resources.configuration)
            overrideConfiguration.fontScale = 1.0f // 保持字体不缩放
            val context = baseContext.createConfigurationContext(overrideConfiguration)
            super.attachBaseContext(context)
        } ?: super.attachBaseContext(newBase)
    }

    fun startProgress(task: ProgressTask, block: ((Boolean, String?) -> Boolean)? = null) {
        taskComplete = block
        progressModel.start(task)
    }

    val resultMap = SparseArray<(Intent?) -> Unit>()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            resultMap[requestCode]?.invoke(data)
        }
        resultMap.remove(requestCode)
    }

    protected fun startActivityResult(intent: Intent, requestCode: Int, task: (Intent?)-> Unit) {
        resultMap[requestCode] = task
        startActivityForResult(intent, requestCode)
    }

    fun installApk(filename: String) {
        val auth = "${packageName}.fileprovider"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            val uri = Uri.parse("package:${packageName}")
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, uri)
            startActivityResult(intent, 18967) {
                PackageHelper.installApkFile(this, auth, filename)
            }
        } else {
            PackageHelper.installApkFile(this, auth, filename)
        }
    }

    protected fun<T: Any?> collectFlow(flow: Flow<T>, f: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                flow.collectLatest(f)
            }
        }
    }}