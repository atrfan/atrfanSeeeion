package com.github.Command

import com.github.AtrfanSession
import com.github.AtrfanSession.logger
import com.github.Data.PluginData
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService

object CommandManager : CompositeCommand(
    AtrfanSession, primaryName = "session"
){
    val trusted: Permission by lazy {
        PermissionService.INSTANCE.register(AtrfanSession.permissionId("atrfanSession"), "atrfanSession的权限")
    }

    @SubCommand("啊吧")
    @Description("啊吧")
    suspend fun CommandSender.aba(){
        logger.info("啊吧啊吧")
        subject?.sendMessage("啊吧啊吧")
    }

    @SubCommand("path")
    @Description("设置对话回复的数据来源")
    suspend fun CommandSender.setPath(path: String){
        PluginData.path = path
        subject?.sendMessage("路径设置成功，当前路径为$path")
        logger.info("回复的数据来源被更改为$path")
    }

    @SubCommand("master")
    @Description("设置机器人主人")
    suspend fun CommandSender.setMaster(qq: Long){
        PluginData.master = qq
        subject?.sendMessage("主人设置完成，当前主人为$qq")
        logger.info("机器人主人被修改为$qq")
    }

    @SubCommand("bot")
    @Description("设置机器人的qq")
    suspend fun CommandSender.setBot(bot: Long){
        PluginData.bot = bot
        subject?.sendMessage("机器人设置完成，当前机器人为$bot")
        logger.info("机器人被修改为$bot")
    }
}