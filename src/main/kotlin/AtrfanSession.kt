package com.github

import com.github.Event.*
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.info

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
        logger.info { "Plugin loaded" }

        // 新人入群欢迎
        GlobalEventChannel.subscribeAlways<MemberJoinEvent> {
            GroupEventManager.welcomeNewMember(this)
        }

        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            GroupEventManager.prohibit(this)                  // 快速禁言
            GroupEventManager.setGroupWelcomeManager(this)          // 设置入群欢迎
        }

        GlobalEventChannel.subscribeAlways<MessageEvent> {

            var flag = MessageEventManager.queryGroupProhibit(this)                     // 违禁词查询
            if (!flag) flag = MessageEventManager.deleteGroupProhibit(this)             // 违禁词删除
            if (!flag) flag = MessageEventManager.addGroupProhibit(this)                // 违禁词添加
            if (!flag) MessageEventManager.muteGroupContact(this)                       // 违禁词禁言
            if (!flag) MessageEventManager.replySpecific(this)                          // 特定消息回复
        }
    }

    override fun onDisable() {
        logger.info("插件已被卸载，感谢使用")
    }
}