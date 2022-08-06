package com.github

import com.github.Command.CommandManager
import com.github.Data.PluginData
import com.github.Event.*
import com.github.Timming.TimerManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.info
import java.util.*

object AtrfanSession : KotlinPlugin(
    JvmPluginDescription(
        id = "com.github.atrfanSession",
        name = "atrfanSession",
        version = "1.0-SNAPSHOT",
    ) {
        author("atrfan")
    }
) {
    override fun onEnable() {
        logger.info { "atrfanSession loaded" }

        PluginData.reload()
        logger.info("数据加载完毕")

        if(PluginData.path == "") logger.warning("回复数据来源未指定，请设置相应数据")
        else logger.info("回复数据文件为${PluginData.path}")

        CommandManager.trusted
        CommandManager.register()
        logger.info("指令相关部分已加载完毕")



        // 新人入群欢迎
        GlobalEventChannel.subscribeAlways<MemberJoinEvent> {
            GroupEventManager.welcomeNewMember(this)
        }

        logger.info("入群欢迎已开启")

        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            GroupEventManager.muteGroupContact(this)                       // 违禁词禁言
            GroupEventManager.prohibit(this)                  // 快速禁言
            GroupEventManager.setGroupWelcomeManager(this)          // 设置入群欢迎
        }

        logger.info("群相关会话已开启")

        GlobalEventChannel.subscribeAlways<MessageEvent> {
            var flag = MessageEventManager.queryGroupProhibit(this)                     // 违禁词查询
            if (!flag) flag = MessageEventManager.deleteGroupProhibit(this)             // 违禁词删除
            if (!flag) flag = MessageEventManager.addGroupProhibit(this)                // 违禁词添加
            if (!flag) MessageEventManager.replySpecific(this)                          // 特定消息回复
            MessageEventManager.modifyGreetGroup(this)
            MessageEventManager.operateStudy(this)                                      // 学习功能
            MessageEventManager.showStudy(this)                                         // 查看记忆
            MessageEventManager.operateBlacklist(this)                                  // 黑名单相关操作
        }

        logger.info("会话监听已准备完毕")

        // 定时器时间
        TimerManager.Morning()      // 早上的问候
        TimerManager.Evening()      // 晚上的问候

        logger.info("定时器已准备完毕")
    }

    override fun onDisable() {
        CommandManager.unregister()
        logger.info("atrfanSession已被卸载，感谢使用")
    }
}