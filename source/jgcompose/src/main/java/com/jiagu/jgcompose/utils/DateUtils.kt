package com.jiagu.jgcompose.utils

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class DateUtils {
    companion object {
        private val calendar: Calendar

        init {
            calendar = GregorianCalendar()
            calendar.time = Date()
        }

        fun getDayCountOfMonth(year: Int, month: Int): Int {
            if (month == 2) {
                //判断年是不是闰年
                if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                    return 29
                } else {
                    return 28
                }
            } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                return 30
            } else return 31
        }

        @SuppressLint("SimpleDateFormat")
        fun getDateTimeString(
            date: Date = Date(),
            pattern: String = "yyyy-MM-dd HH:mm:ss",
        ): String {
            val sdf = SimpleDateFormat(pattern)
            return sdf.format(date)
        }

        @SuppressLint("SimpleDateFormat")
        fun getDateString(date: Date = Date(), pattern: String = "yyyy-MM-dd"): String {
            val sdf = SimpleDateFormat(pattern)
            return sdf.format(date)
        }

        @SuppressLint("SimpleDateFormat")
        fun stringToDate(dateString: String, pattern: String = "yyyy-MM-dd"): Date? {
            val sdf = SimpleDateFormat(pattern)
            var date: Date? = null
            try {
                date = sdf.parse(dateString)
            } catch (e: Exception) {
                Log.e("DateUtil", ("dateFormat -> stringToDate" + e.message))
            }
            return date
        }

        @SuppressLint("SimpleDateFormat")
        fun stringToDateTime(dateString: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): Date? {
            val sdf = SimpleDateFormat(pattern)
            var date: Date? = null
            try {
                date = sdf.parse(dateString)
            } catch (e: Exception) {
                Log.e("DateUtil", ("dateFormat -> stringToDateTime" + e.message))
            }
            return date
        }

        /**
         * 获取当前日期
         */
        fun getDate(cal: Calendar = calendar): Date {
            return cal.time
        }

        /**
         * 获取年
         */
        fun getYear(cal: Calendar = calendar): Int {
            return cal.get(Calendar.YEAR)
        }

        /**
         * 获取月
         */
        fun getMonth(cal: Calendar = calendar): Int {
            //  月份从0开始，需要手动 +1
            return cal.get(Calendar.MONTH) + 1
        }

        /**
         * 获取日
         */
        fun getDayOfMonth(cal: Calendar = calendar): Int {
            return cal.get(Calendar.DAY_OF_MONTH)
        }

        /**
         * 获取时
         */
        fun getHour(cal: Calendar = calendar): Int {
            return cal.get(Calendar.HOUR_OF_DAY)
        }

        /**
         * 获取分
         */
        fun getMinute(cal: Calendar = calendar): Int {
            return cal.get(Calendar.MINUTE)
        }

        /**
         * 获取秒
         */
        fun getSecond(cal: Calendar = calendar): Int {
            return cal.get(Calendar.SECOND)
        }

        /**
         * 日期 + 天
         */
        fun plusDays(days: Int = 1): Date {
            calendar.add(Calendar.DATE, days)
            return calendar.time
        }

        /**
         * 日期 - 天
         */
        fun minusDays(days: Int = 1): Date {
            calendar.add(Calendar.DATE, -days)
            return calendar.time
        }

        /**
         * 获取日历
         */
        fun getCalendar(dateStr: String): Calendar {
            return if (dateStr.isNotBlank()) {
                getCalendar(stringToDate(dateStr) ?: getDate())
            } else {
                getCalendar(getDate())
            }
        }

        /**
         * 获取日历
         */
        fun getCalendar(date: Date): Calendar {
            val newCalendar = GregorianCalendar()
            newCalendar.time = date
            return newCalendar
        }

        /**
         * 获取半年前
         */
        fun getHalfYearAgoCalendar(date: Date): Date {
            // 创建一个新的 Calendar 实例以避免修改原始日历
            val newCalendar = GregorianCalendar()
            newCalendar.time = date
            newCalendar.add(Calendar.MONTH, -6)
            return newCalendar.time
        }

        /**
         * 获取一个月前
         */
        fun getOneMonthAgoCalendar(date: Date): Date {
            // 创建一个新的 Calendar 实例以避免修改原始日历
            val newCalendar = GregorianCalendar()
            newCalendar.time = date
            newCalendar.add(Calendar.MONTH, -1)
            return newCalendar.time
        }

        /**
         * 获取一周前
         */
        fun getOneWeekAgoCalendar(date: Date): Date {
            // 创建一个新的 Calendar 实例以避免修改原始日历
            val newCalendar = GregorianCalendar()
            newCalendar.time = date
            newCalendar.add(Calendar.DATE, -7)
            return newCalendar.time
        }

    }
}
