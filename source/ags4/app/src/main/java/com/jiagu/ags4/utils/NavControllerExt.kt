package com.jiagu.ags4.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions

val LocalNavController = compositionLocalOf<NavController> { error("No NavController provided") }

fun NavController.goto(dest: String, popupTo: String? = null) {
    if (popupTo != null) {
        val option = NavOptions.Builder().setPopUpTo(popupTo, true).build()
        navigate(dest, navOptions = option)
    } else {
        navigate(dest)
    }
}

fun NavController.popToRoot() {
    // 如果当前已经在起始路由（WORK_MANUAL），则不做任何操作
    if (currentDestination?.route == graph.startDestinationRoute) {
        return
    }
    // 否则，清空整个栈并导航到起始路由（WORK_MANUAL）
    navigate(graph.startDestinationRoute!!) {
        popUpTo(graph.startDestinationRoute!!) {
            inclusive = true  // 清空整个栈（包括起始路由本身）
        }
        launchSingleTop = true  // 避免重复创建实例
    }
}

fun <T : ViewModel> NavController.getViewModel(
    route: String,
    clazz: Class<T>,
    factory: ViewModelProvider.Factory? = null,
): T {
    val entry = getBackStackEntry(route)
    val provider = factory?.let { ViewModelProvider(entry, it) } ?: ViewModelProvider(entry)
    return provider[clazz]
}