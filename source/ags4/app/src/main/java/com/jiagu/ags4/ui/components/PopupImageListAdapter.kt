package com.jiagu.ags4.ui.components

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.jiagu.ags4.R
import com.jiagu.api.widget.BaseArrayAdapter


class ImageItem(val text: String, val image: Int = 0, val backgroundColor: Int? = null, val onImageClick: (() -> Unit)? = null)

class PopupImageListAdapter(
    ctx: Context, imageItems: List<ImageItem>
) : BaseArrayAdapter<ImageItem, PopupImageListAdapter.ViewHolder>(
    ctx, R.layout.image_item_popupmenu, imageItems
) {
    override fun bindView(data: ImageItem, position: Int, vh: ViewHolder) {
        vh.position = position
        vh.text.text = data.text
        vh.image.setImageResource(data.image)
        if (data.backgroundColor != null) {
            vh.image.setColorFilter(data.backgroundColor)
        }
        data.onImageClick?.let {
            vh.image.setOnClickListener { it() }
        }
    }

    override fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    class ViewHolder(v: View) {
        var position = 0
        val text = v.findViewById<TextView>(R.id.text)
        val image = v.findViewById<ImageView>(R.id.image)
    }
}