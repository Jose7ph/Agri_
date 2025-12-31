package com.jiagu.jgcompose_demo

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jiagu.jgcompose_demo.ui.fragment.AllButtonsPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllCardsPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllChannelsPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllChartsPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllContainersPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllCountersPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllDrawersPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllIconsPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllLabelsPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllMotorsPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllPagingPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllPickersPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllProgressPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllRadarPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllRemoteControlPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllRockerPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllRtspPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllScreenPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllShadowPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllSliderPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllTextFieldPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllTextPreviews
import com.jiagu.jgcompose_demo.ui.fragment.AllVideoPreviews
import com.jiagu.jgcompose_demo.ui.fragment.BatteryPreview
import com.jiagu.jgcompose_demo.ui.fragment.BluetoothListPreview
import com.jiagu.jgcompose_demo.ui.fragment.PreviewSimpleTopBar
import com.jiagu.jgcompose_demo.ui.fragment.PromptPopupPreview
import com.jiagu.jgcompose_demo.ui.fragment.AllVideoPreviews
import com.jiagu.jgcompose_demo.ui.theme.Jgcompose_demoTheme

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    protected lateinit var controller: WindowInsetsControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        enableEdgeToEdge()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        setContent {
            navController = rememberNavController()
            Jgcompose_demoTheme {
                CompositionLocalProvider(
                    LocalNavController provides navController,
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp))
                        NavHost(
                            navController = navController,
                            startDestination = "index"
                        ) {
                            composable("index") {
                                Index()
                            }
                            composable("bar") {
                                PreviewSimpleTopBar()
                            }
                            composable("battery") {
                                BatteryPreview()
                            }
                            composable("bluetooth") {
                                BluetoothListPreview()
                            }
                            composable("button") {
                                AllButtonsPreview()
                            }
                            composable("card") {
                                AllCardsPreview()
                            }
                            composable("channel") {
                                AllChannelsPreview()
                            }
                            composable("chart") {
                                AllChartsPreview()
                            }
                            composable("container") {
                                AllContainersPreview()
                            }
                            composable("counter") {
                                AllCountersPreview()
                            }
                            composable("drawer") {
                                AllDrawersPreview()
                            }
                            composable("icon") {
                                AllIconsPreview()
                            }
                            composable("label") {
                                AllLabelsPreview()
                            }
                            composable("motor") {
                                AllMotorsPreview()
                            }
                            composable("paging") {
                                AllPagingPreviews()
                            }
                            composable("picker") {
                                AllPickersPreview()
                            }
                            composable("popup") {
                                PromptPopupPreview()
                            }
                            composable("progress") {
                                AllProgressPreviews()
                            }
                            composable("radar") {
                                AllRadarPreviews()
                            }
                            composable("remotecontrol") {
                                AllRemoteControlPreviews()
                            }
                            composable("rocker") {
                                AllRockerPreviews()
                            }
                            composable("rtsp") {
                                AllRtspPreviews()
                            }
                            composable("screen") {
                                AllScreenPreviews()
                            }
                            composable("shadow") {
                                AllShadowPreviews()
                            }
                            composable("slider") {
                                AllSliderPreviews()
                            }
                            composable("text") {
                                AllTextPreviews()
                            }
                            composable("textfield") {
                                AllTextFieldPreviews()
                            }
                            composable("video") {
                                AllVideoPreviews()
                            }

                        }

                        // 将返回按钮放在 Box 内部以实现正确定位
                        BackButton(
                            navController = navController,
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                    }
                }

            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        return super.dispatchTouchEvent(ev)
    }

    @Composable
    fun BackButton(navController: NavHostController, modifier: Modifier = Modifier) {
        // 使用 currentBackStackEntryAsState 监听导航堆栈变化
        val backStackEntry = navController.currentBackStackEntryAsState()

        // 判断是否应该显示返回按钮
        val shouldShowBackButton = backStackEntry.value?.destination?.route != "index"

        if (shouldShowBackButton) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape),
                shape = CircleShape
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp),
                    textAlign = TextAlign.Center // 水平居中
                )
            }
        }
    }


    @Composable
    fun Index() {
        val modules = listOf(
            "bar",
            "battery",
            "bluetooth",
            "button",
            "card",
            "channel",
            "chart",
            "container",
            "counter",
            "drawer",
            "icon",
            "label",
            "motor",
            "paging",
            "picker",
            "popup",
            "progress",
            "radar",
            "remotecontrol",
            "rocker",
            "rtsp",
            "screen",
            "shadow",
            "slider",
            "text",
            "textfield",
            "video"
        )

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 30.dp, horizontal = 20.dp),
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(modules.size) {
                Button(
                    modifier = Modifier
                        .height(50.dp)
                        .width(100.dp), onClick = {
                        navController.navigate(modules[it])
                    }) {
                    Text(text = modules[it])
                }
            }
        }
    }
}

val LocalNavController = compositionLocalOf<NavController> { error("No NavController provided") }

