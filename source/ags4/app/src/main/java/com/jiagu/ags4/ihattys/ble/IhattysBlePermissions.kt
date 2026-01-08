package com.jiagu.ags4.ihattys.ble

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object IhattysBlePermissions {

    fun required(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // Pre-Android 12 permissions are normal/manifest permissions
            emptyArray()
        }
    }

    fun hasAll(context: Context): Boolean {
        val perms = required()
        if (perms.isEmpty()) return true
        return perms.all { p ->
            ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        }
    }
}
