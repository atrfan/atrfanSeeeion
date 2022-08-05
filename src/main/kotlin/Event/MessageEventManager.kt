package com.github.Event

import com.alibaba.fastjson.JSON
import com.github.Data.PluginData
import com.github.entity.GroupProhibitBase
import com.github.entity.SessionBase
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

object MessageEventManager {
    suspend fun replySpecific(event: MessageEvent) {
        val message = event.message.serializeToMiraiCode()
        val file = File("data.json")
        val stream = file.inputStream()
        val content = stream.readBytes().toString(Charset.defaultCharset())
        val obj = JSONObject(content)
        try {
            val array = obj.getJSONArray(message)
            val arraylist = ArrayList<String>()
            for (value in array) {
                arraylist.add(value.toString())
                val base = SessionBase("精确", message, arraylist, message)
                val chain = PluginData.operateSpecReply(true, base)
                arraylist.remove(value.toString())
                if (chain == buildMessageChain { +PlainText("ATRI可是高性能机器人，不需要再次学习已经会的东西哦") }) break
            }
        } catch (ignore: Exception) {

        } finally {
            withContext(Dispatchers.IO) {
                stream.close()
            }
        }
        for ((key, value) in PluginData.specReply) {
            if (Pattern.matches(key, message)) {
                val base = JSON.parseObject(value, SessionBase::class.java)
                val index = Random().nextInt(base.values.size)
                event.subject.sendMessage(base.values[index].deserializeMiraiCode())
            }
        }
    }

    /**
     *
     * 添加违禁词，格式为：+prohibit: 违禁词 禁言时间
     */
    suspend fun addGroupProhibit(event: MessageEvent): Boolean {
        val content = event.message.contentToString()
        if (!Pattern.matches("\\+prohibit[:：]\\s(\\S)+\\s(\\d)+\\S", content)) {
            return false
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
        return true
    }

    /**
     *
     * 删除违禁词，格式：-prohibit： 违禁词
     */
    suspend fun deleteGroupProhibit(event: MessageEvent): Boolean {
        val input = event.message.contentToString()
        if (!Pattern.matches("-prohibit[:：]\\s(\\S)+", input)) {
            return false
        }
        val str = input.split(" ".toRegex()).toTypedArray()
        val content = str[1]

        val base = GroupProhibitBase(content, null, null, 0)
        event.subject.sendMessage(
            PluginData.operateGroupProhibitMessage(false, base)
        )
        return true
    }

    /**
     *
     * 查询违禁词，格式：让我康康违禁词
     */
    suspend fun queryGroupProhibit(event: MessageEvent): Boolean {
        if (event.message.contentToString() != "让我康康违禁词") {
            return false
        }
        val message = MessageChainBuilder().append("下列为所有违禁词：\n").append("违禁词\t禁言时间\t\n")
        for ((_, value) in PluginData.groupProhibitMessage) {
            val jsonObject = JSONObject(value)
            message.append("${jsonObject["content"]}\t${jsonObject["description"]}\n")
        }
        event.subject.sendMessage(message.build())
        return true
    }

    /**
     *
     * 违禁词禁言
     */

    suspend fun muteGroupContact(event: MessageEvent) {
        val qq = event.sender.id
        val member = event.bot.getGroup(event.subject.id)!![qq]!!
        val content = event.message.contentToString()
        var base = ""
        for ((key, value) in PluginData.groupProhibitMessage) {
            if (content.contains(key)) {
                base = value
            }
        }
        if (base == "") return
        try {
            val time = JSONObject(base)["prohibitNum"].toString().toInt()
            member.mute(time)       // 禁言
            //撤回
            event.source.recall()
        } catch (e: Exception) {
            if (e is PermissionDeniedException) {
                event.subject.sendMessage("Σ(っ °Д °;)っ,咱好像没有权限撤回那条消息并禁言ta欸")
                return
            } else {
                e.printStackTrace()
            }
            event.subject.sendMessage(
                buildMessageChain {
                    +At(event.sender.id)
                    +PlainText(JSONObject(base)["reply"].toString() + JSONObject(base)["description"])
                }
            )
        }
    }

    /**
     *
     * 早、晚安问候群聊添加或删除,
     * 只有master能使用
     * 指令：【+-】greet:[空格] 群号
     */
    suspend fun modifyGreetGroup(event: MessageEvent) {
        val qq = event.sender.id
        val content = event.message.contentToString()
        if (qq != PluginData.master || !Pattern.matches("[+-]greet[:：]\\s[\\d]+", content)) {
            return
        }
        val input = content.split("[:：]\\s".toRegex()).toTypedArray()
        val group = input[1].toLong()
        val bot = event.subject.bot
        if (!PluginData.groupData.containsKey(group)) {
            PluginData.loadGroupData(bot)
        }
        if (PluginData.groupData.containsKey(group)) {
            val message = PluginData.operateGreetGroup(input[0][0] == '+', group)
            event.subject.sendMessage(message)
        } else {
            event.subject.sendMessage("没有找到这个群欸，要不要确定群号后再来试一下呢?")
        }
    }


    /**
     * 特定内容学习
     * 指令：[学习|遗忘] [ 匹配规则 ] 触发内容 回复内容
     *
     */
    suspend fun operateStudy(event: MessageEvent) {
        val content = event.message.serializeToMiraiCode()
        if (!Pattern.matches("(学习|遗忘) (精确|模糊|头部|尾部) \\S+ \\S+", content) || event.sender.id != PluginData.master) {
            return
        }
        val input = content.split(" ".toRegex()).toTypedArray()
        val type = input[1]
        val key = input[2]
        val value = ArrayList<String>()
        value.add(input[3])
        var regStr = ""
        when (type) {
            "模糊" -> {
                regStr = ".*?$key.*?"
            }
            "精确" -> {
                regStr = key
            }
            "头部" -> {
                regStr = "$key.*?"
            }
            "尾部" -> {
                regStr = ".*?$key"
            }
        }
        val base = SessionBase(type, key, value, regStr)
        val message = try {
            PluginData.operateSpecReply(input[0] == "学习", base)
        } catch (e: Exception) {
            e.printStackTrace()
            buildMessageChain { +PlainText("记忆功能出现了一点点的问题，暂时不能提供服务了，请帮忙联系我的master进行维修") }
        }
        event.subject.sendMessage(message)
    }

    /**
     *
     * 查看目前学会的所有信息
     * 命令：查看记忆
     */
    suspend fun showStudy(event: MessageEvent) {
        val content = event.message.contentToString()
        if (content != "查看记忆") {
            return
        }
        if (event.sender.id != PluginData.master) {
            event.subject.sendMessage("哼~ 才不会让你知道我学会了多少知识呢，这可是只有master能知道的秘密")
            return
        }

        val message = MessageChainBuilder()
        for ((_, value) in PluginData.specReply) {
            val base = JSON.parseObject(value, SessionBase::class.java)
            message.append("匹配规则：${base.type}\n")
                .append("触发词条：")
                .append(base.key.deserializeMiraiCode())
                .append("\n")
                .append("回复内容：\n")
            for (reply in base.values) {
                message.append("\t")
                    .append(reply.deserializeMiraiCode())
                    .append("\n")
            }
            message.append("---------------------\n")
        }
        try {
            event.subject.sendMessage(message.build())
        } catch (e: Exception) {
            if (e is IllegalArgumentException) {
                event.subject.sendMessage("ATRI目前啥都不会哦，请添加后再来查询吧~")
            } else {
                e.printStackTrace()
            }
        }
    }
}