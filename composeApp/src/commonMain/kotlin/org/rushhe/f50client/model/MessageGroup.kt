package org.rushhe.f50client.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageGroup(
    val number: String,
    val messageList: List<Message>,
)