package com.jiagu.ags4

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.jgcompose.dialog.DialogViewModel
import com.jiagu.tools.map.IMapCanvas


abstract class MapActivity : MapBaseActivity() {
    private val dialogVM: DialogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<ComposeView>(R.id.fragment).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ComposeTheme {
                    ContentPage()
                    val dialogState by dialogVM.dialogState.collectAsState()
                    if (dialogState.isVisible) {
                        dialogState.content?.let { it() }
                    }
                }
            }
        }
    }

    override fun setContentView() = setContentView(R.layout.activity_map)

    @Composable
    abstract fun ContentPage()

    override fun onResume() {
        super.onResume()
        canvas.onResume()
    }

    override fun onPause() {
        super.onPause()
        canvas.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        canvas.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        canvas.onSaveInstanceState(outState)
    }

    private var clickListener: IMapCanvas.MapClickListener? = null
    private var mapChangeListener: IMapCanvas.MapChangeListener? = null
    private var markListener: IMapCanvas.MapMarkerSelectListener? = null
    private var markDragListener: IMapCanvas.MarkerDragListener? = null
    fun clearMapListener() {
        clickListener?.let { canvas.removeClickListener(it) }
        markListener?.let { canvas.removeMarkClickListener(it) }
        markDragListener?.let { canvas.removeMarkerDragListener(it) }
        mapChangeListener?.let { canvas.removeChangeListener(it) }
    }

    fun addMapClickListener(l: IMapCanvas.MapClickListener) {
        clickListener = l
        canvas.addClickListener(l)
    }

    fun addMapChangeListener(l: IMapCanvas.MapChangeListener) {
        mapChangeListener = l;
        canvas.addChangeListener(l)
    }

    fun addMarkClickListener(l: IMapCanvas.MapMarkerSelectListener) {
        markListener = l
        canvas.addMarkClickListener(l)
    }

    fun addMarkDragListener(l: IMapCanvas.MarkerDragListener) {
        markDragListener = l
        canvas.addMarkerDragListener(l)
    }
}