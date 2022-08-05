package com.github.entity

data class SessionBase(
    val type: String,           // 匹配规则，有：精确、模糊、首部、尾部四种
    val key: String,            // 触发的词条，可以为字符也可以为图片
    val values: ArrayList<String>,       // 回复的词条，可为字符也可为图片
    val regStr: String                    // 匹配用的正则表达式
)
