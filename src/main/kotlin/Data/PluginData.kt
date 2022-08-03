package com.github.Data

import com.github.entity.GroupProhibitBase
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import org.json.JSONObject

object PluginData : AutoSavePluginData("atrfanSessionData") {

    @ValueDescription("群欢迎词")
    val groupWelcomeMessage: MutableList<String> by value()

    @ValueDescription("机器人主人")
    var master: Long by value(1311489434L)

    @ValueDescription("群禁言词汇")
    val groupProhibitMessage: MutableMap<String, String> by value()

    fun operateGroupProhibitMessage(aod: Boolean, base: GroupProhibitBase): MessageChain {
        return if (aod) {
            groupProhibitMessage[base.content] = JSONObject(base).toString()
            buildMessageChain { +PlainText("${base.content} 禁言词添加成功") }
        } else {
            if (!groupProhibitMessage.contains(base.content)) {
                buildMessageChain { +PlainText("未找到该禁言词") }
            } else {
                groupProhibitMessage.remove(base.content)
                buildMessageChain { +PlainText("${base.content} 禁言词删除成功") }
            }
        }
    }
}