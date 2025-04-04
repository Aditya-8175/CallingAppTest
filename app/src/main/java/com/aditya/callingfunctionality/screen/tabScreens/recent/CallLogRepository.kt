package com.aditya.callingfunctionality.screen.tabScreens.recent

import android.content.Context
import com.aditya.callingfunctionality.component.CallLogHelper
import com.aditya.callingfunctionality.component.CallLogItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
      private val callLogHelper = CallLogHelper(context)
    suspend fun getGroupedCallLogs(): Map<String, List<CallLogItem>> = withContext(Dispatchers.IO) {
        val allLogs = callLogHelper.getCallLogs()
        val duplicateLogs = allLogs.groupBy { it.number }
            .mapNotNull { (_, logs) -> logs.maxByOrNull { it.date } }

        val todayList = mutableListOf<CallLogItem>()
        val yesterdayList = mutableListOf<CallLogItem>()
        val groupedByDate = mutableMapOf<String, MutableList<CallLogItem>>()

        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)

        val dateFormatter = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.getDefault())

        duplicateLogs.forEach { log ->
            cal.time = log.date
            val logYear = cal.get(Calendar.YEAR)
            val logDay = cal.get(Calendar.DAY_OF_YEAR)

            when {
                logYear == todayYear && logDay == todayDay -> todayList.add(log)
                logYear == todayYear && logDay == todayDay - 1 -> yesterdayList.add(log)
                else -> {
                    val formattedDate = dateFormatter.format(log.date)
                    groupedByDate.getOrPut(formattedDate) { mutableListOf() }.add(log)
                }
            }
        }

        val map = mutableMapOf<String, List<CallLogItem>>()
        if (todayList.isNotEmpty()) map["Today"] = todayList
        if (yesterdayList.isNotEmpty()) map["Yesterday"] = yesterdayList
        map.putAll(groupedByDate)

        map.toMap()
    }

}

