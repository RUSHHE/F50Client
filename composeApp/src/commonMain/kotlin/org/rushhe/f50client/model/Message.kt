package org.rushhe.f50client.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("content") val content: String,
    @SerialName("date") val date: String,
    @SerialName("draft_group_id") val draftGroupId: String,
    @SerialName("id") val id: Long,
    @SerialName("number") val number: String,
    @SerialName("tag") val tag: String
)