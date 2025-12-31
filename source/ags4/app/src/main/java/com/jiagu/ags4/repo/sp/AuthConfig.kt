package com.jiagu.ags4.repo.sp

import android.content.Context
import android.util.Base64
import android.util.Log
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.api.helper.RSAHelper
import com.jiagu.device.vkprotocol.VKAuthTool
import java.io.File


class AuthConfig(context: Context) {
    private val pref = context.applicationContext.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun getSigunature(): ByteArray? {
        return if (sign.isNotBlank()) MemoryHelper.stringToByte(sign) else null
    }

    private fun parsePem(pem: File): String {
        val sb = StringBuilder()
        for (line in pem.readLines()) {
            if (line.startsWith("-----")) continue
            sb.append(line.replace(System.lineSeparator().toRegex(), ""))
        }
        return sb.toString()
    }

    // import private key in PEM format
    fun importKey(privPem: File, pubPem: File) {
        if (!privPem.exists()) {
            Log.e("yuhang", "no private pem file")
            LogFileHelper.log("no private pem file")
            return
        }
        if (!pubPem.exists()) {
            Log.e("yuhang", "no public pem file")
            LogFileHelper.log("no public pem file")
            return
        }
        try {
            val der = parsePem(privPem)
            val data = Base64.decode(der, Base64.DEFAULT)
            val key = RSAHelper.restorePrivateKey(data)
            if (key == null) {
                Log.e("yuhang", "import private key failed")
                LogFileHelper.log("import private key failed")
                return
            }
            VKAuthTool.setPrivateKey(key)

            val derPub = parsePem(pubPem)
            val dataPub = Base64.decode(derPub, Base64.DEFAULT)
            val pubKey = RSAHelper.restorePublicKey(dataPub)
            if (pubKey == null) {
                Log.e("yuhang", "import public key failed")
                LogFileHelper.log("import public key failed")
                return
            }
            VKAuthTool.setPublicKey(pubKey)
            VKAuthTool.setPublicKeyData(dataPub)
        } catch (e: Throwable) {
            Log.e("yuhang", "import key failed: $e")
            LogFileHelper.log("import key failed: $e")
            e.printStackTrace()
            return
        }
    }

    fun importSignature(signature: File) {
        if (!signature.exists()) {
            Log.e("yuhang", "no sign file")
            return
        }
        val lines = signature.readLines()
        sign = lines[0]
    }

    var sign: String
        get() = pref.getString("sign", "")!!
        set(value) = pref.edit().putString("sign", value).apply()
}