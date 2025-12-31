package com.jiagu.ags4.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jiagu.ags4.bean.Region
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import java.util.IdentityHashMap

object CacheModel {

    private val _region = MutableLiveData<List<Address>>()
    val region: LiveData<List<Address>> = _region

    class Address(
        val code: Int,
        var name: String,
        var child: MutableList<Address> = mutableListOf(),
        var parent: Address? = null
    ) {
        override fun toString() = "$name($code)"
    }

    private fun buildAddress(regions: List<Region>): List<Address> {
        val provinces = mutableListOf<Address>()
        val map = mutableMapOf<Int, Address>()
        for (r in regions) {
            val addr = map[r.c] ?: Address(r.c, r.n).apply { map[code] = this }
            addr.name = r.n
            if (r.p == 0) {
                provinces.add(addr)
            } else {
                val parent = map[r.p] ?: Address(r.p, "").apply { map[code] = this }
                addr.parent = parent
                parent.child.add(addr)
            }
        }
        // simplify hierachy
        for (p in provinces) {
            if (p.child.size == 1) {
                val child = p.child[0]
                p.child.clear()
                p.child.addAll(child.child)
                for (c in p.child) {
                    c.parent = p
                }
            }
        }
        return provinces
    }

    private suspend fun loadAddress(
        flow: Flow<List<Region>>,
        ok: suspend (List<Address>) -> Unit,
        fail: (String) -> Unit
    ) {
        flow.transform { emit(buildAddress(it)) }.flowOn(Dispatchers.IO)
            .networkFlow(fail)
            .collectLatest(ok)
    }

    private var loading = false
    suspend fun loadCountry(fail: (String) -> Unit) {
        if (loading || region.value != null) return
        loading = true
        loadAddress(AgsNet.getRegionList().networkFlow {
            loading = false
            fail(it)
        }, {
            _region.postValue(it)
            loading = false
        }) {
            loading = false
            fail(it)
        }
    }

    fun convertAddressList(): List<com.jiagu.jgcompose.picker.Address> {
        val addressList = mutableListOf<com.jiagu.jgcompose.picker.Address>()
        region.value?.let { oldList ->
            if (oldList.isNotEmpty()) {
                repeat(oldList.size) {
                    val region = oldList[it]
                    addressList.add(
                        region.deepCopy()
                    )
                }
            }
        }
        return addressList.toList()

    }

    fun Address.deepCopy(): com.jiagu.jgcompose.picker.Address {
        val cache = IdentityHashMap<Address, com.jiagu.jgcompose.picker.Address>()

        fun recursiveCopy(source: Address): com.jiagu.jgcompose.picker.Address {
            // 检查缓存，避免循环引用导致的无限递归
            cache[source]?.let { return it }

            // 创建目标对象并存入缓存
            val target = com.jiagu.jgcompose.picker.Address(
                code = source.code,
                name = source.name,
                child = mutableListOf(),
                parent = null
            )
            cache[source] = target

            // 递归拷贝子节点
            target.child.addAll(source.child.map { child ->
                recursiveCopy(child).apply {
                    parent = target  // 设置子节点的 parent 引用
                }
            })

            return target
        }

        return recursiveCopy(this)
    }
}