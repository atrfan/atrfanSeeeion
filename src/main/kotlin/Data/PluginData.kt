package com.github.Data

import com.github.entity.GroupProhibitBase
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import org.json.JSONObject

object PluginData : AutoSavePluginData("atrfanSessionData") {
    @ValueDescription("机器人主人")
    var master: Long by value(1311489434L)

    val bot: Long by value(1832557686L)

    @ValueDescription("群欢迎词")
    val groupWelcomeMessage: MutableList<String> by value()

    @ValueDescription("群禁言词汇")
    val groupProhibitMessage: MutableMap<String, String> by value()

    @ValueDescription("发送早安的群聊")
    val goodMorningGroup: MutableList<Long> by value()

    @ValueDescription("发送晚安的群聊")
    val goodEveningGroup: MutableList<Long> by value()

    @ValueDescription("bot拥有的群列表")
    val groupData: MutableMap<Long,String> by value()

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

    fun operateGreetGroup(aod: Boolean,isMorning: Boolean,qq: Long): MessageChain{
        return if(aod){
            if(isMorning){
                if(goodMorningGroup.contains(qq)){
                    buildMessageChain { +PlainText("群 <${groupData[qq]}> 已经存在了，请勿重复添加") }
                } else{
                    goodMorningGroup.add(qq)
                    buildMessageChain { +PlainText("<${groupData[qq]}> 添加到早上好群聊成功") }
                }
            } else {
                if(goodEveningGroup.contains(qq)){
                    buildMessageChain { +PlainText("群 <${groupData[qq]}> 已经存在了，请勿重复添加") }
                } else{
                    goodEveningGroup.add(qq)
                    buildMessageChain { +PlainText("<${groupData[qq]}> 添加到晚上好群聊成功") }
                }
            }
        } else {
            if(isMorning){
                if(goodMorningGroup.contains(qq)){
                    goodMorningGroup.remove(qq)
                    buildMessageChain { +PlainText("群 <${groupData[qq]}> 移除成功！") }
                } else{
                    buildMessageChain { +PlainText("群 <${groupData[qq]}> 未在列表中哦，请核对后再尝试吧") }
                }
            } else {
                if(goodEveningGroup.contains(qq)){
                    goodEveningGroup.remove(qq)
                    buildMessageChain { +PlainText("群 <${groupData[qq]}> 移除成功！") }
                } else{
                    buildMessageChain { +PlainText("群 <${groupData[qq]}> 未在列表中哦，请核对后再尝试吧") }
                }
            }
        }
    }

    fun loadGroupData(bot: Bot){
        val groups = bot.groups
        for (group in groups) {
            if(!groupData.containsKey(group.id)){
                groupData[group.id] = group.name
            }
        }
    }

}