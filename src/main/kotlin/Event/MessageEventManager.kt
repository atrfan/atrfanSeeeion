package com.github.Event

import com.github.Data.PluginData
import com.github.entity.GroupProhibitBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

object MessageEventManager {

    lateinit var base: GroupProhibitBase

    suspend fun replySpecific(event: MessageEvent) {
        val message = event.message.contentToString()
        val file = File("data.json")
        val stream = file.inputStream()
        val content = stream.readBytes().toString(Charset.defaultCharset())
        val obj = JSONObject(content)
        try {
            val array = obj.getJSONArray(message)
            val index = (Random().nextInt(array.length()))
            event.subject.sendMessage(array.get(index).toString())
        } catch (ignore: Exception) {
        } finally {
            withContext(Dispatchers.IO) {
                stream.close()
            }
        }
    }

    /**
     *
     * 添加违禁词，格式为：+prohibit: 违禁词 禁言时间
     */
    suspend fun addGroupProhibit(event: MessageEvent) {
        val content = event.message.contentToString()
        if (!Pattern.matches("\\+prohibit[:：](\\S)+ (\\d)+\\S", content)) {
            return
        }
        val str = content.split(" ".toRegex()).toTypedArray()

        val unit = str[2].substring(str[2].length - 1)
        val inputTime = str[2].substring(0, str[2].length - 1).toInt()
        var time = 60
        val description: String
        when (unit) {
            "s" -> {
                time = inputTime
                description = "禁言：" + inputTime + "秒"
            }
            "m" -> {
                time = inputTime * 60
                description = "禁言：" + inputTime + "分钟"
            }
            "h" -> {
                time = inputTime * 60 * 60
                description = "禁言：" + inputTime + "小时"
            }
            "d" -> {
                time = inputTime * 60 * 60 * 24
                description = "禁言：" + inputTime + "天"
            }
            else -> {
                time = 60
                description = "禁言1分钟"
            }
        }

        val base = GroupProhibitBase(str[1], "违法天条！被", description, time)
        event.subject.sendMessage(
            PluginData.operateGroupProhibitMessage(true, base)
        )
    }

    /**
     *
     * 删除违禁词，格式：-prohibit：违禁词
     */
    suspend fun deleteGroupProhibit(event: MessageEvent) {
        val input = event.message.contentToString()
        if (!Pattern.matches("-prohibit[:：](\\S)+", input)) {
            return
        }
        val str = input.split("[:：]".toRegex()).toTypedArray()
        val content = str[1]

        val base = GroupProhibitBase(content, null, null, 0)
        event.subject.sendMessage(
            PluginData.operateGroupProhibitMessage(false, base)
        )
    }

    /**
     *
     * 查询违禁词，格式：让我康康违禁词
     */
    suspend fun queryGroupProhibit(event: MessageEvent) {
        if (event.message.contentToString() != "让我康康违禁词") {
            return
        }
        val message = MessageChainBuilder().append("下列为所有违禁词：\n").append("违禁词\t禁言时间\t\n")
        for ((_, base) in PluginData.groupProhibitMessage) {
            message.append("${base.content}\t${base.description}\n")
        }
        event.subject.sendMessage(message.build())
    }

    /**
     *
     * 违禁词禁言
     */

    suspend fun muteGroupContact(event: MessageEvent) {
        val qq = event.sender.id
        val member = event.bot.getGroup(event.subject.id)!![qq]!!
        val content = event.message.contentToString()
        for((key,value) in PluginData.groupProhibitMessage){
            if(content.contains(key)){
                base = value
            }
        }
        try {
            member.mute(base.prohibitNum)       // 禁言
            //撤回
            event.source.recall()
        } catch (e: Exception) {
            if (e is PermissionDeniedException) {
                event.subject.sendMessage("Σ(っ °Д °;)っ,咱好像没有权限撤回那条消息欸")
            } else {
                e.printStackTrace()
            }
            event.subject.sendMessage(
                buildMessageChain {
                    +At(event.sender.id)
                    +PlainText(base.reply + base.description)
                }
            )
        }
    }
}