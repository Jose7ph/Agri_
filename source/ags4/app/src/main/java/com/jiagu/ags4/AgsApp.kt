package com.jiagu.ags4

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.sp.UserConfig
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.jni.SecurityChecker
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import xcrash.XCrash

class AgsApp : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val param = XCrash.InitParameters()
        param.setLogDir(getExternalFilesDir("tombstones")?.path)
        XCrash.init(this, param)
        SecurityChecker.check(this)
    }

    override fun onCreate() {
        super.onCreate()
        AgsUser.flavor = BuildConfig.FLAVOR
        AgsUser.firmPrefix = BuildConfig.FW_PREFIX
        // 用户信息
        val config = UserConfig(this)
        config.user?.let { AgsUser.setUser(it) }
        AgsNet.initialize(this)
        AgsNet.setSmsSender(BuildConfig.SMS_SENDER)
        registerReceiver()
        AptypeUtil.init(this)

        EventBus.getDefault().register(this)
    }

    @Subscribe
    fun onSortie(sync: Repo.SyncData) {
        sync()
    }

    private fun sync() {
        AgsDB.sync()
    }

    private lateinit var netReceiver: NetWorkStateReceiver
    private fun registerReceiver() {
        netReceiver = NetWorkStateReceiver()
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(netReceiver, filter)
    }

    inner class NetWorkStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (checkNetwork(context)) {
//                sync()
            }
            LogFileHelper.log("network connect:${AgsUser.netIsConnect} ${intent.action}")
        }
    }

    private fun checkNetwork(context: Context): Boolean {
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = manager.getNetworkCapabilities(manager.activeNetwork)
        AgsUser.netIsConnect = if (caps != null) {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else false
        return AgsUser.netIsConnect
    }
}