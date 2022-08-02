package com.github.Data

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object PluginData: AutoSavePluginData("atrfanSessionData") {

    @ValueDescription("群欢迎词")
    val groupWelcomeMessage: MutableList<String> by value()

    @ValueDescription("机器人主人")
    var master: Long by value(1311489434L)
}