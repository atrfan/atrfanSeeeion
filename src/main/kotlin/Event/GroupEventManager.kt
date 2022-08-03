package com.github.Event

import com.github.AtrfanSession.logger
import com.github.Data.PluginData
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import java.util.*
import java.util.regex.Pattern


object GroupEventManager {

    /**
     * 禁言相关正则匹配
     */
    private val prohibitPattern = "(\\[mirai:at:\\d+\\] \\d+[s|d|h|m])"

    /**
     *
     * 添加迎新词语正则匹配
     */
    private val setMessagePattern = "[+-]?welcome(\\\\)?[:：](\\S+)?"

    suspend fun prohibit(event: GroupMessageEvent) {
        val subject = event.subject
        val code = event.message.serializeToMiraiCode()
        //检测格式
        if (!Pattern.matches(prohibitPattern, code)) {
            return
        }
        if (event.sender.id != PluginData.master && event.permission.level == 0) {
            subject.sendMessage("您暂时没有禁言他人的权限哦，请向我的master申请权限后再来吧")
        }

        //获取群友对象
        var qq: Long? = null
        for (s in event.message) {
            if (s is At) {
                qq = s.target
            }
        }
        if (qq == null) {
            subject.sendMessage("禁言失败，群里好像没有这个人呢！")
            return
        }
        val member = event.bot.getGroup(event.subject.id)!![qq]

        //获取参数
        val split = code.split(" ".toRegex()).toTypedArray()
        val param = split[1]
        //分解参数
        val type = param.substring(param.length - 1)
        val timeParam = param.substring(0, param.length - 1).toInt()
        if (timeParam == 0) {
            assert(member != null)
            member!!.unmute()
            subject.sendMessage("解禁成功！")
            return
        }

        //禁言时间计算
        var time = 0
        val messages = MessageChainBuilder().append("禁言成功!")
        when (type) {
            "s" -> {
                time = timeParam
                messages.append("禁言:" + timeParam + "秒")
            }
            "m" -> {
                time = timeParam * 60
                messages.append("禁言:" + timeParam + "分钟")
            }
            "h" -> {
                time = timeParam * 60 * 60
                messages.append("禁言:" + timeParam + "小时")
            }
            "d" -> {
                time = timeParam * 60 * 60 * 24
                messages.append("禁言:" + timeParam + "天")
            }
            else -> {}
        }
        assert(member != null)
        try {
            member!!.mute(time)
        } catch (e: Exception) {
            if (e is PermissionDeniedException) {
                subject.sendMessage("禁言失败,ATRI没有权力禁言ta欸")
            } else {
                e.printStackTrace()
            }
        }
        subject.sendMessage(messages.build())
    }

    suspend fun welcomeNewMember(event: MemberJoinEvent) {
        logger.info(event.member.id.toString() + "(" + event.member.nameCard + ")" + "入群")

        val welcomeMessage = PluginData.groupWelcomeMessage
        val message: MessageChain

        if (welcomeMessage.size == 0) {
            message = buildMessageChain {
                +At(event.member.id)
                +PlainText(" ")
                +PlainText("又有新的小伙伴加入了，欢迎！")
            }
            event.group.sendMessage(message)
            return
        }
        val index: Int = (Random().nextInt() % welcomeMessage.size)
        message = buildMessageChain {
            +At(event.member.id)
            +welcomeMessage[index].deserializeMiraiCode()
        }
        event.group.sendMessage(message)
    }

    suspend fun setGroupWelcomeManager(event: GroupMessageEvent) {
        val code = event.message.serializeToMiraiCode()
        if (!Pattern.matches(setMessagePattern, code)) {
            return
        }
        try {
            val str = code.split("[:：]".toRegex(),2).toTypedArray()
            if (str[0].startsWith("+")) {
                PluginData.groupWelcomeMessage.add(str[1])
                event.subject.sendMessage("添加欢迎词成功")
            } else if (str[0].startsWith("-")) {
                PluginData.groupWelcomeMessage.remove(str[1])
                event.subject.sendMessage("删除欢迎词成功")
            } else {
                val chain = MessageChainBuilder()
                chain.append("入群欢迎词如下：\n")
                for (welcome in PluginData.groupWelcomeMessage) {
                    chain.append(welcome.deserializeMiraiCode())
                        .append("\n")
                }
                event.subject.sendMessage(chain.build())
            }
        } catch (e: Exception) {
            event.subject.sendMessage("操作发生了错误，请联系我的master后再试")
            e.printStackTrace()
        }
    }


}