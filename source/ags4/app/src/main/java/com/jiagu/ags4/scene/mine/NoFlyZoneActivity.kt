package com.jiagu.ags4.scene.mine

import android.os.Bundle
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jiagu.ags4.MapActivity
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.NoFlyZoneInfo
import com.jiagu.api.ext.toast


class NoFlyZoneActivity : MapActivity() {
    private var noFlyBlocks: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noFlyBlocks = intent.getStringExtra("noFlyBlocks")!!
        if (noFlyBlocks.isEmpty()) {
            toast(getString(R.string.no_data))
        } else {
            val boundary = NoFlyZoneInfo.convertBoundary(noFlyBlocks)

            val name = "no fly zone"
            canvas.drawBlock(name, boundary, false, 0x4DE12C2C)
            canvas.fit(listOf(name))
        }
    }

    @Composable
    override fun ContentPage() {
        val navController = rememberNavController()
        CompositionLocalProvider() {
            NavHost(navController = navController,
                startDestination = "no_fly_blocks",
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(tween(0))
                },
                exitTransition = {
                    fadeOut(tween(0))
                },
                popExitTransition = {
                    fadeOut(tween(0))
                },
                popEnterTransition = {
                    fadeIn(tween(0))
                }) {
                composable("no_fly_blocks") { NoFlyBlocks { finish() } }
            }
        }
    }

}