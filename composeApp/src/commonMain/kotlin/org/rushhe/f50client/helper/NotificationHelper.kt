package org.rushhe.f50client.helper

import androidx.compose.runtime.Composable

expect class NotificationHelper {
    fun sendAppNotification(id: Int, title: String, body: String)

    fun clearAppNotification()

    @Composable
    fun CheckAppNotificationPermission()
}