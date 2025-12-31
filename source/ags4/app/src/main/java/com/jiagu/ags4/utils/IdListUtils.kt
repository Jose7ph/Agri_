package com.jiagu.ags4.utils

import com.jiagu.device.vkprotocol.VKAg

/**
 * idList 过滤
 *
 * @param idListData idlistdata
 * @param filterNum 过滤的编号 VKBase.DEVINFO_开头的
 * @return 分组的数据
 */
fun filterDeviceByTypes(
    idListData: List<VKAg.IDListData>?,
    filterNum: List<Short>? = null
): MutableMap<Short, MutableList<VKAg.IDListData>> {
    val filterMap = mutableMapOf<Short, MutableList<VKAg.IDListData>>()
    idListData?.let {
        it.forEach { dev ->
            //filterNum为空直接分组数据
            if (filterNum.isNullOrEmpty()) {
                if (!filterMap.containsKey(dev.devType)) {
                    filterMap[dev.devType] = mutableListOf(dev)
                } else {
                    filterMap[dev.devType]!!.add(dev)
                }
            } else {
                //filterNum不为空仅分组需要过滤的数据
                if (filterNum.contains(dev.devType)) {
                    if (!filterMap.containsKey(dev.devType)) {
                        filterMap[dev.devType] = mutableListOf(dev)
                    } else {
                        filterMap[dev.devType]!!.add(dev)
                    }
                }

            }
        }
    }
    return filterMap
}