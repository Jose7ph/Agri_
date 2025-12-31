package com.jiagu.ags4.utils

/**
 * 保护模式类型枚举
 */
enum class ProtectionType {
    FIXED_UNLOCK,    // 固定解-解锁保护 (低4位)
    FIXED_LOST,      // 固定解-丢失保护 (4位)
    DIRECTION_UNLOCK, // 测向-解锁保护 (4位)
    DIRECTION_LOST   // 测向-丢失保护 (高4位)
}

/**
 * 解析保护模式值
 * @param value 16位的short值
 * @param type 保护类型
 * @return 解析后的值 (0-15)
 */
fun parseProtectionMode(value: Int, type: ProtectionType): Int {
    val intValue = value.toInt() and 0xFFFF // 确保是正数

    return when (type) {
        ProtectionType.FIXED_UNLOCK -> {
            // 低4位 (0-3位)
            intValue and 0x000F
        }
        ProtectionType.FIXED_LOST -> {
            // 4-7位
            (intValue and 0x00F0) ushr 4
        }
        ProtectionType.DIRECTION_UNLOCK -> {
            // 8-11位
            (intValue and 0x0F00) ushr 8
        }
        ProtectionType.DIRECTION_LOST -> {
            // 高4位 (12-15位)
            (intValue and 0xF000) ushr 12
        }
    }
}

/**
 * 设置保护模式值
 * @param originalValue 原始的16位short值
 * @param type 保护类型
 * @param newValue 要设置的新值 (0-15)
 * @return 设置后的short值
 */
fun setProtectionMode(originalValue: Int, type: ProtectionType, newValue: Int): Short {
    var intValue = originalValue.toInt() and 0xFFFF
    val clampedValue = newValue and 0x0F // 确保值在0-15范围内

    intValue = when (type) {
        ProtectionType.FIXED_UNLOCK -> {
            // 清除低4位，然后设置新值
            (intValue and 0xFFF0) or clampedValue
        }
        ProtectionType.FIXED_LOST -> {
            // 清除4-7位，然后设置新值
            (intValue and 0xFF0F) or (clampedValue shl 4)
        }
        ProtectionType.DIRECTION_UNLOCK -> {
            // 清除8-11位，然后设置新值
            (intValue and 0xF0FF) or (clampedValue shl 8)
        }
        ProtectionType.DIRECTION_LOST -> {
            // 清除高4位，然后设置新值
            (intValue and 0x0FFF) or (clampedValue shl 12)
        }
    }

    return intValue.toShort()
}