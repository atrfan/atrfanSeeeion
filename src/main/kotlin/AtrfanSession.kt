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

        GlobalEventChannel.subscribeAlways<MemberJoinEvent> {
            GroupEventManager.welcomeNewMember(this)
        }
        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            GroupEventManager.prohibit(this)
            GroupEventManager.setGroupWelcomeManager(this)
        }

        GlobalEventChannel.subscribeAlways<MessageEvent> {
            MessageEventManager.replySpecific(this)
        }
    }

    override fun onDisable() {
        logger.info("插件已被卸载，感谢使用")
    }
}