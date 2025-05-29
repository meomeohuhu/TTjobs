package com.example.cvtt.Data

data class Message(
    val id: String = "",
    val senderId: String = "", // ID của người gửi (userId hoặc companyId)
    val receiverId: String = "", // ID của người nhận (companyId hoặc userId)
    val content: String = "",
    val timestamp: Long = 0L
)

data class ChatSummary(
    val companyId: String = "",
    val company: Company? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
)