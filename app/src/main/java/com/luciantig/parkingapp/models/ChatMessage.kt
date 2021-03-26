package com.luciantig.parkingapp.models

import java.util.*

data class ChatMessage(
    val text: String,
    val timestamp: Date,
    val senderId: String,
    val recipientId: String,
    val senderName: String
)