package com.jiagu.jgcompose.picker

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.screen.OperateButton
import com.jiagu.jgcompose.screen.ScreenColumn
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.utils.DateUtils.Companion.getCalendar
import com.jiagu.jgcompose.utils.DateUtils.Companion.getDayOfMonth
import com.jiagu.jgcompose.utils.DateUtils.Companion.getHalfYearAgoCalendar
import com.jiagu.jgcompose.utils.DateUtils.Companion.getMonth
import com.jiagu.jgcompose.utils.DateUtils.Companion.getOneMonthAgoCalendar
import com.jiagu.jgcompose.utils.DateUtils.Companion.getOneWeekAgoCalendar
import com.jiagu.jgcompose.utils.DateUtils.Companion.getYear
import com.jiagu.jgcompose.utils.DateUtils.Companion.stringToDate
import java.util.Date

//最小年份
private const val MIN_YEAR = 2000

//最大年份
private const val MAX_YEAR = 2100

/**
 * 日期选择器
 *
 * @param defaultDate 默认日期
 * @param onCancel 取消回调
 * @param onConfirm 确定回调
 * p1:选择的日期[yyyy-MM-dd]
 */
@Composable
fun DatePicker(
    defaultDate: String, onCancel: () -> Unit, onConfirm: (String) -> Unit
) {
    // 年
    var year by remember {
        mutableIntStateOf(getYear(getCalendar(defaultDate)))
    }
    //月
    var month by remember {
        mutableIntStateOf(getMonth(getCalendar(defaultDate)))
    }
    //日
    var day by remember {
        mutableIntStateOf(getDayOfMonth(getCalendar(defaultDate)))
    }
    ScreenColumn(content = {
        //日期选择
        DateWheel(
            modifier = Modifier,
            year = year,
            month = month,
            day = day,
            minYear = MIN_YEAR,
            maxYear = MAX_YEAR
        ) { index, value ->
            when (index) {
                0 -> year = value
                1 -> month = value
                2 -> day = value
            }
        }
    }, buttons = {
        //筛选按钮
        Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            //半年
            OperateButton(
                text = stringResource(id = R.string.now)
            ) {
                year = getYear(getCalendar(Date()))
                month = getMonth(getCalendar(Date()))
                day = getDayOfMonth(getCalendar(Date()))
            }
        }
    }, onCancel = onCancel, onConfirm = {
        //处理日期字符串 小于2位补0 时分秒开始时间默认00:00:00 结束时间默认23:59:59
        //检索开始时间更新
        val monthStr = if (month < 10) "0${month}" else "${month}"
        val dayStr = if (day < 10) "0${day}" else "${day}"
        val dateStr = "${year}-$monthStr-$dayStr"
        onConfirm(
            dateStr
        )
    })
}

/**
 * 日期范围选择
 *
 * @param defaultStartDate 初始开始时间
 * @param defaultEndDate 初始结束时间
 * @param onCancel 取消回调
 * @param onConfirm 确定回调
 * p1:日期范围str[yyyy-MM-dd ~ yyyy-MM-dd],
 * p2:开始日期str[yyyy-MM-dd],
 * p3:开始时间str[yyyy-MM-dd HH:mm:ss],
 * p4:结束日期str[yyyy-MM-dd],
 * p5:结束时间str[yyyy-MM-dd HH:mm:ss]
 */
@Composable
fun DateRangePicker(
    defaultStartDate: String,
    defaultEndDate: String,
    onCancel: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit,
) {
    //起始 年
    var startYear by remember {
        mutableIntStateOf(getYear(getCalendar(defaultStartDate)))
    }
    //起始 月
    var startMonth by remember {
        mutableIntStateOf(getMonth(getCalendar(defaultStartDate)))
    }
    //起始 日
    var startDay by remember {
        mutableIntStateOf(getDayOfMonth(getCalendar(defaultStartDate)))
    }
    //结束 年
    var endYear by remember {
        mutableIntStateOf(getYear(getCalendar(defaultEndDate)))
    }
    //结束 月
    var endMonth by remember {
        mutableIntStateOf(getMonth(getCalendar(defaultEndDate)))
    }
    //结束 日
    var endDay by remember {
        mutableIntStateOf(getDayOfMonth(getCalendar(defaultEndDate)))
    }

    //用于记录picker1的临时日期
    var startDateStr by remember {
        mutableStateOf("")
    }
    ScreenColumn(content = {
        //日期区间选择
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
        ) {
            DateWheel(
                modifier = Modifier.weight(1f),
                year = startYear,
                month = startMonth,
                day = startDay,
                minYear = MIN_YEAR,
                maxYear = MAX_YEAR
            ) { index, value ->
                when (index) {
                    0 -> startYear = value
                    1 -> startMonth = value
                    2 -> startDay = value
                }
            }
            VerticalDivider(thickness = 1.dp, color = Color.Blue)
            //结束日期范围校验
            startDateStr = "${startYear}-${startMonth}-${startDay}"
            val endDateStr = "${endYear}-${endMonth}-${endDay}"
            // 当前picker2 < picker1 重置当前picker2 = picker1
            if (endDateStr.isNotEmpty() && startDateStr.isNotEmpty() && (stringToDate(
                    endDateStr
                )!! < stringToDate(startDateStr))
            ) {
                endYear = startYear
                endMonth = startMonth
                endDay = startDay
            }
            //更新 picker2的列表范围
            DateWheel(
                modifier = Modifier.weight(1f),
                year = endYear,
                month = endMonth,
                day = endDay,
                minYear = startYear,
                maxYear = MAX_YEAR,
                startDate = startDateStr
            ) { index, value ->
                when (index) {
                    0 -> endYear = value
                    1 -> endMonth = value
                    2 -> endDay = value
                }
            }
        }
    }, buttons = {
        //筛选按钮
        Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            //半年
            OperateButton(
                text = stringResource(id = R.string.half_year)
            ) {
                val firstDate = getHalfYearAgoCalendar(Date())
                startYear = getYear(getCalendar(firstDate))
                startMonth = getMonth(getCalendar(firstDate))
                startDay = getDayOfMonth(getCalendar(firstDate))
                endYear = getYear(getCalendar(Date()))
                endMonth = getMonth(getCalendar(Date()))
                endDay = getDayOfMonth(getCalendar(Date()))
            }
            //一月
            OperateButton(
                text = stringResource(id = R.string.one_month)
            ) {
                val firstDate = getOneMonthAgoCalendar(Date())
                startYear = getYear(getCalendar(firstDate))
                startMonth = getMonth(getCalendar(firstDate))
                startDay = getDayOfMonth(getCalendar(firstDate))
                endYear = getYear(getCalendar(Date()))
                endMonth = getMonth(getCalendar(Date()))
                endDay = getDayOfMonth(getCalendar(Date()))
            }
            //一周
            OperateButton(
                text = stringResource(id = R.string.one_week)
            ) {
                val firstDate = getOneWeekAgoCalendar(Date())
                startYear = getYear(getCalendar(firstDate))
                startMonth = getMonth(getCalendar(firstDate))
                startDay = getDayOfMonth(getCalendar(firstDate))
                endYear = getYear(getCalendar(Date()))
                endMonth = getMonth(getCalendar(Date()))
                endDay = getDayOfMonth(getCalendar(Date()))
            }
        }
    }, onCancel = onCancel, onConfirm = {
        //处理日期字符串 小于2位补0 时分秒开始时间默认00:00:00 结束时间默认23:59:59
        //检索开始时间更新
        val startMonthStr = if (startMonth < 10) "0${startMonth}" else "${startMonth}"
        val startDayStr = if (startDay < 10) "0${startDay}" else "${startDay}"
        val startDate = "${startYear}-$startMonthStr-$startDayStr"
        val startTime = "$startDate 00:00:00"
        //检索结束时间更新
        val endMonthStr = if (endMonth < 10) "0${endMonth}" else "${endMonth}"
        val endDayStr = if (endDay < 10) "0${endDay}" else "${endDay}"
        val endDate = "${endYear}-$endMonthStr-$endDayStr"
        val endTime = "$endDate 23:59:59"
        //时间文本框内容更新
        val dateRange = "$startDate ~ $endDate"
        onConfirm(
            dateRange, startDate, startTime, endDate, endTime
        )
    })
}

/**
 * 时间选择器 - (开始-结束时间)
 */
@Composable
private fun DateWheel(
    modifier: Modifier = Modifier,
    year: Int,
    month: Int,
    day: Int,
    minYear: Int = 2000,
    maxYear: Int = 2100,
    startDate: String = "",
    onChange: (index: Int, value: Int) -> Unit
) {
    Box(
        modifier = modifier, Alignment.Center
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            var yearRange = minYear..maxYear

            var monthRange = 1..12

            val lastDay = getLastDay(year, month)
            var dateRange = 1..lastDay


            /**
             * 时间范围后面 <前面的问题
             */
            if (startDate.isNotEmpty()) {
                //上一个已选择日期
                val date = stringToDate(startDate)
                val canceler = getCalendar(date!!)
                //单独处理列表中 月/日 列表数据
                //年份相同 则起始月份为 startDate.month ~ 12
                if (year == getYear(canceler)) {
                    monthRange = getMonth(canceler)..12
                    //年份 && 月份 都相同 则起始日为 startDate.day ~ lastDay
                    if (month == getMonth(canceler)) {
                        dateRange = getDayOfMonth(canceler)..lastDay
                    }
                }
                yearRange = getYear(canceler)..MAX_YEAR
            }
            //  年
            RollPicker(modifier = Modifier.width(80.dp), value = year, item = { _, v ->
                Label(
                    text = v.toString(),
                )
            }, list = yearRange.toList(), onValueChange = { _, v ->
                onChange(0, v)
            })
            //  月
            RollPicker(modifier = Modifier.width(50.dp), value = month, item = { _, v ->
                Label(
                    text = v.toString(),
                )
            }, list = monthRange.toList(), onValueChange = { _, v ->
                onChange(1, v)
            })
            //  日
            RollPicker(modifier = Modifier.width(50.dp),
                value = if (day > lastDay) lastDay else day,
                item = { _, v ->
                    Label(
                        text = v.toString(),
                    )
                },
                list = dateRange.toList(),
                onValueChange = { _, v ->
                    onChange(2, v)
                })
        }

        // 中间两道横线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
        ) {
            HorizontalDivider(Modifier.padding(horizontal = 15.dp))
            HorizontalDivider(
                Modifier
                    .padding(horizontal = 15.dp)
                    .align(Alignment.BottomStart)
            )
        }
    }
}

/**
 * 根据年月, 获取天数
 */
private fun getLastDay(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> {
            // 百年: 四百年一闰年;  否则: 四年一闰年;
            if (year % 100 == 0) {
                if (year % 400 == 0) {
                    29
                } else {
                    28
                }
            } else {
                if (year % 4 == 0) {
                    29
                } else {
                    28
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DatePickerPreview() {
    ComposeTheme {
        var show1 by remember {
            mutableStateOf(false)
        }
        var show2 by remember {
            mutableStateOf(false)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Row {
                Button(onClick = { show1 = !show1 }) {
                    Text(text = "日期")
                }
                Button(onClick = { show2 = !show2 }) {
                    Text(text = "日期区间")
                }
            }
            if (show1) {
                DatePicker(defaultDate = "2000-01-01",
                    onCancel = { show1 = !show1 }) { date ->
                    Log.d("zhy", "date: ${date}")
                }
            }
            if (show2) {
                DateRangePicker(defaultStartDate = "2000-01-01",
                    defaultEndDate = "2022-01-01",
                    onCancel = {
                        show2 = !show2
                    }) { dateRange, startDate, startTime, endDate, endTime ->
                    Log.d(
                        "zhy",
                        "dateRange: ${dateRange}," + "startDate:${startDate}," + "startTime:${startTime}," + "endDate:${endDate}," + "endTime:${endTime}"
                    )
                }
            }
        }
    }
}
