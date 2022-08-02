package com.github.Event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.MessageEvent
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.util.*

object MessageEventManager {

    suspend fun replySpecific(event: MessageEvent) {
        val message = event.message.contentToString()
        val file = File("data.json")
        val stream = file.inputStream()
        val content = stream.readBytes().toString(Charset.defaultCharset())
        val obj = JSONObject(content)
        try {
            val array = obj.getJSONArray(message)
            val index = (Random().nextInt(array.length()))
            event.subject.sendMessage(array.get(index).toString())
        } catch (ignore: Exception) {
        } finally {
            withContext(Dispatchers.IO) {
                stream.close()
            }
        }
    }
}