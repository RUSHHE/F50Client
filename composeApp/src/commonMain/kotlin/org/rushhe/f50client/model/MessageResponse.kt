package org.rushhe.f50client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    @SerialName("messages") val messages: List<Message>
)