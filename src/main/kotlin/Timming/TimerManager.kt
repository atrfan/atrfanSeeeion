package com.github.Timming

import com.github.Data.PluginData
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import java.util.*

object TimerManager {

    const val morning = "早上好呀米娜桑，准备好迎接新的一天了吗"

    const val evening = "晚安啦米纳斯，早点休息别再熬夜啦"

    fun Morning() {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 7
        cal[Calendar.MINUTE] = 30
        cal[Calendar.SECOND] = 0
        var date = cal.time //第一次执行定时任务的时间   每天早上7点30
        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (date.before(Date())) {
            date = addDay(date, 1)
        }
        //0替换成cal.getTime();
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() = runBlocking {
                sendGreetMessage(morning)
            }
        }, date, 1000 * 60 * 60 * 24L)
    }

    fun Evening() {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 22
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        var date = cal.time //第一次执行定时任务的时间   每天早上7点30
        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (date.before(Date())) {
            date = addDay(date, 1)
        }
        //0替换成cal.getTime();
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() = runBlocking {
                sendGreetMessage(evening)
            }
        }, date, 1000 * 60 * 60 * 24L)
    }

    suspend fun sendGreetMessage(message: String) {
        val bot = Bot.getInstance(PluginData.bot)
        for (group in PluginData.greetGroup) {
            bot.getGroup(group)?.sendMessage(message)
        }
    }

    private fun addDay(date: Date, num: Int): Date? {
        val startDT = Calendar.getInstance()
        startDT.time = date
        startDT.add(Calendar.DAY_OF_MONTH, num)
        return startDT.time
    }
}