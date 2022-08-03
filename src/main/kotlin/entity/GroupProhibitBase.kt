package com.github.entity

import kotlinx.serialization.json.JsonNames

/**
 *
 * 群禁言词汇信息
 */
data class GroupProhibitBase(
    val content: String,        // 触发内容
    val reply: String?,          // 禁言后回复内容
    val description: String?,       // 禁言时间
    val prohibitNum: Int,       // 禁言时间
)
