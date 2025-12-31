package com.jiagu.jgcompose.splash

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Composable
fun SplashScreen(uri: Uri?, onVideoEnded: () -> Unit) {
    Surface {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val modifier = Modifier.fillMaxSize()
            if (uri != null && uri.toString().endsWith("png")) {
                AsyncImage(
                    modifier = modifier,
                    model = uri,
                    contentDescription = "logo",
                    contentScale = ContentScale.Fit
                )
            } else {
                VideoPlayerScreen(uri) { onVideoEnded() }
            }

        }
    }
}

@Composable
fun VideoPlayerScreen(uri: Uri?, onVideoEnded: () -> Unit) {
    val context = LocalContext.current
    var hasEndedCalled = remember { false }
    
    fun safeCallOnVideoEnded() {
        if (!hasEndedCalled) {
            hasEndedCalled = true
            onVideoEnded()
        }
    }

    val exoPlayer = remember {
        try {
            ExoPlayer.Builder(context).build().apply {
                val fromUri = if (uri != null && uri.toString().endsWith("mp4")) {
                    uri
                } else {
                    Uri.parse("asset:///splash.mp4")
                }
                try {
                    setMediaItem(MediaItem.fromUri(fromUri))
                    prepare()
                    playWhenReady = true
                } catch (e: Exception) {
                    Log.e("lee", "ExoPlayer prepare failed: ${e.message}")
                    safeCallOnVideoEnded()
                }
            }
        } catch (e: Exception) {
            Log.e("lee", "ExoPlayer init failed: ${e.message}")
            safeCallOnVideoEnded()
            null
        }
    }

    if (exoPlayer == null) {
        LaunchedEffect(Unit) {
            safeCallOnVideoEnded()
        }
        return
    }

    val playbackStateListener = remember {
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    Log.v("lee", "onPlaybackStateChanged: ended")
                    safeCallOnVideoEnded()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("lee", "onPlayerError: ${error.message}")
                safeCallOnVideoEnded()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // 使用 AndroidView 嵌入 PlayerView
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    // 启用控制栏
                    useController = false
                    // 设置控制栏自动隐藏的时间，0表示一直显示
//                        controllerShowTimeoutMs = 5000 // 默认5000ms，即5秒后隐藏
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }, modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
    }

    // 在 VideoPlayerScreen 函数中添加缓冲超时检测
    DisposableEffect(Unit) {
        try {
            exoPlayer.addListener(playbackStateListener)
        } catch (e: Exception) {
            Log.e("lee", "Add listener failed: ${e.message}")
            safeCallOnVideoEnded()
        }
        
        onDispose {
            try {
                if (!hasEndedCalled) {
                    safeCallOnVideoEnded()
                }
                Log.v("lee", "dispose")
                exoPlayer.removeListener(playbackStateListener)
                exoPlayer.release()
            } catch (e: Exception) {
                Log.e("lee", "Dispose failed: ${e.message}")
            }
        }
    }
}