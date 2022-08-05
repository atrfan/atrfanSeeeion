package com.github.Data

import com.alibaba.fastjson.JSON
import com.github.entity.GroupProhibitBase
import com.github.entity.SessionBase
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import org.json.JSONObject

object PluginData : AutoSavePluginData("atrfanSessionData") {
    @ValueDescription("机器人主人")
    var master: Long by value(1311489434L)

    @ValueDescription("机器人QQ")
    val bot: Long by value(1832557686L)

    @ValueDescription("群欢迎词")
    val groupWelcomeMessage: MutableList<String> by value()

    @ValueDescription("群禁言词汇")
    val groupProhibitMessage: MutableMap<String, String> by value()

    @ValueDescription("特定词语回复")
    val specReply: MutableMap<String, String> by value()

    @ValueDescription("发送问候消息的群聊")
    val greetGroup: MutableList<Long> by value()

    @ValueDescription("bot拥有的群列表")
    val groupData: MutableMap<Long, String> by value()

    fun operateGroupProhibitMessage(aod: Boolean, base: GroupProhibitBase): MessageChain {
        return if (aod) {
            if (groupProhibitMessage.contains(base.content)) {
                buildMessageChain { +PlainText("该禁言词已经存在了，请勿重复添加") }
            } else {
                groupProhibitMessage[base.content] = JSONObject(base).toString()
                buildMessageChain { +PlainText("${base.content} 禁言词添加成功") }
            }
        } else {
            if (!groupProhibitMessage.contains(base.content)) {
                buildMessageChain { +PlainText("未找到该禁言词") }
            } else {
                groupProhibitMessage.remove(base.content)
                buildMessageChain { +PlainText("${base.content} 禁言词删除成功") }
            }
        }
    }

    fun operateSpecReply(aod: Boolean, base: SessionBase): MessageChain {
        return if (aod) {
            if (specReply.containsKey(base.regStr)) {
                val jsonString = specReply[base.regStr]
                val session = JSON.parseObject(jsonString, SessionBase::class.java)
                if (session.values.contains(base.values[0])) {
                    buildMessageChain { +PlainText("ATRI可是高性能机器人，不需要再次学习已经会的东西哦") }
                } else {
                    session.values.add(base.values[0])
                    specReply[base.regStr] = JSON.toJSONString(session)
                    buildMessageChain { +PlainText("ATRI已经学会了哦，下次就看我表演吧") }
                }
            } else {
                specReply[base.regStr] = JSON.toJSONString(base)
                buildMessageChain { +PlainText("ATRI已经学会了哦，下次就看我表演吧") }
            }
        } else {
            if (specReply.containsKey(base.regStr)) {
                val jsonString = specReply[base.regStr]
                val session = JSON.parseObject(jsonString, SessionBase::class.java)
                if (session.values.contains(base.values[0])) {
                    session.values.remove(base.values[0])
                    specReply[base.regStr] = JSON.toJSONString(session)
                    buildMessageChain { +PlainText("听从主人的命令，ATRI成功的将其遗忘了哦") }
                } else {
                    if (base.values[0] == "all") {
                        session.values.clear()
                        specReply[base.regStr] = JSON.toJSONString(session)
                        buildMessageChain { +PlainText("听从主人的命令，ATRI成功的将其遗忘了哦") }
                    } else {
                        buildMessageChain { +PlainText("在ATRI的记忆里面好像没有这个欸，是不是你记错了呢？") }
                    }
                }
            } else {
                buildMessageChain { +PlainText("在ATRI的记忆里面好像没有这个欸，是不是你记错了呢？") }
            }
        }
    }

    fun operateGreetGroup(aod: Boolean, qq: Long): MessageChain {
        return if (aod) {
            if (greetGroup.contains(qq)) {
                buildMessageChain { +PlainText("群 <${groupData[qq]}> 已经存在了，请勿重复添加") }
            } else {
                greetGroup.add(qq)
                buildMessageChain { +PlainText("<${groupData[qq]}> 添加到问候群聊成功") }
            }
        } else {
            if (greetGroup.contains(qq)) {
                greetGroup.remove(qq)
                buildMessageChain { +PlainText("群 <${groupData[qq]}> 移除成功！") }
            } else {
                buildMessageChain { +PlainText("群 <${groupData[qq]}> 未在列表中哦，请核对后再尝试吧") }
            }
        }
    }

    fun loadGroupData(bot: Bot) {
        val groups = bot.groups
        for (group in groups) {
            if (!groupData.containsKey(group.id)) {
                groupData[group.id] = group.name
            }
        }
    }

}