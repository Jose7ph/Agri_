package com.jiagu.ags4.ui.components

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.jiagu.ags4.R
import com.jiagu.api.widget.BaseArrayAdapter
import razerdp.basepopup.BasePopupWindow
import java.lang.ref.WeakReference

object PopupMenuHolder {
    var currentPopupMenu: WeakReference<PopupMenu>? = null
}

class PopupMenu(ctx: Context, val x: Int, val y: Int, val w: Int, val h: Int) :
    BasePopupWindow(ctx),
    AdapterView.OnItemClickListener {

    init {
        setContentView(R.layout.combox_popup)
        setBackgroundColor(Color.TRANSPARENT)
        setFitSize(false)
        PopupMenuHolder.currentPopupMenu = WeakReference(this)
    }

    private var listener: (Int) -> Unit = {}
    fun setListener(l: (Int) -> Unit) {
        listener = l
    }

    private lateinit var list: ListView
    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)

        list = contentView as ListView
        list.onItemClickListener = this
    }

    fun show(adapter: BaseAdapter) {
        list.adapter = adapter
        show()
    }

    fun show(data: List<String>) {
        val adapter = PopupListAdapter(context, data)
        list.adapter = adapter
        show()
    }

    private fun show() {
        offsetX = x
//    if (sh - (y + height) > 300) {
//        offsetY = sh - y
//        popupGravity = Gravity.BOTTOM
//    } else {
        offsetY = y + h + 1
        popupGravity = Gravity.TOP
//    }
        setMaxWidth(w)
        Log.d("yuhang", "show: $offsetX, $offsetY")
//    pop.setMaxHeight(300)
        showPopupWindow()
    }

    private inner class PopupListAdapter(ctx: Context, items: List<String>) :
        BaseArrayAdapter<String, ViewHolder>(ctx, R.layout.item_popupmenu, items) {
        override fun bindView(data: String, position: Int, vh: ViewHolder) {
            vh.position = position
            vh.text.text = data
        }

        override fun createViewHolder(view: View): ViewHolder {
            return ViewHolder(view)
        }
    }

    inner class ViewHolder(v: View) {
        var position = 0
        val text = v.findViewById<TextView>(R.id.text)
    }

    override fun onItemClick(parent: AdapterView<*>?, v: View?, index: Int, id: Long) {
        listener.invoke(index)
        dismiss()
    }

    companion object {
        // x, y, width, height of parent widget
        fun showPopupMenu(
            ctx: Context,
            items: List<String>,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            onClick: (Int) -> Unit
        ) {
            val pop = PopupMenu(ctx, x, y, width, height)
            pop.setListener(onClick)
            pop.show(items)
        }

        fun showPopupMenu(
            ctx: Context,
            adapter: BaseAdapter,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            onClick: (Int) -> Unit
        ) {
            val pop = PopupMenu(ctx, x, y, width, height)
            pop.setListener(onClick)
            pop.show(adapter)
        }
    }
}