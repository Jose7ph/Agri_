package com.jiagu.ags4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    val resultMap = mutableMapOf<Int, (Intent?) -> Unit>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            resultMap[requestCode]?.invoke(data)
        }
        resultMap.remove(requestCode)
    }

}
