package org.rushhe.f50client.helper

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import platform.Foundation.NSURL
import platform.UIKit.UIScene
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

actual class NotificationHelper {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter().apply {
        setDelegate(NotificationDelegate())
    }

    /**
     * 通知中心代理，用于在前台显示通知
     */
    class NotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            listOf(
                UNAuthorizationOptionAlert, // 显示通知
                UNAuthorizationOptionSound, // 播放声音
                UNAuthorizationOptionBadge, // 显示角标
            ).forEach {
                withCompletionHandler(it)
            }
        }
    }

    actual fun sendAppNotification(id: Int, title: String, body: String) {
        // 创建通知内容
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }

        // 创建触发器，通知将在1秒后显示
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 1.0,
            repeats = false
        )

        // 创建通知请求
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id.toString(),
            content = content,
            trigger = trigger
        )

        // 发送通知
        notificationCenter.addNotificationRequest(request) { error ->
            // TODO: 用其他方式处理错误
            error?.let { println("iOS通知发送失败: ${it.localizedDescription}") }
        }
    }

    actual fun clearAppNotification() {
        notificationCenter.apply {
            removeAllPendingNotificationRequests()
            removeAllDeliveredNotifications()
        }
    }

    @Composable
    actual fun CheckAppNotificationPermission() {
        var showAlert by remember { mutableStateOf(false) }

        val lifecycleOwner = LocalLifecycleOwner.current

        // 在生命周期变化时处理权限请求
        DisposableEffect(key1 = lifecycleOwner, effect = {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    ON_START -> {
                        // 获取通知权限的状态
                        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
                            when (settings?.authorizationStatus) {
                                // 用户未确定，则请求通知权限
                                UNAuthorizationStatusNotDetermined -> {
                                    listOf(
                                        UNAuthorizationOptionAlert,
                                        UNAuthorizationOptionSound,
                                        UNAuthorizationOptionBadge
                                    ).forEach {
                                        notificationCenter.requestAuthorizationWithOptions(
                                            options = it,
                                            completionHandler = { granted, error ->
                                                if (!granted) {
                                                    println(error)
                                                }
                                            }
                                        )
                                    }
                                }

                                // 用户拒绝了通知权限，则显示提示对话框
                                UNAuthorizationStatusDenied -> {
                                    showAlert = true
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        })

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                confirmButton = {
                    TextButton(onClick = {
                        showAlert = false
                    }) {
                        Text("取消")
                    }

                    TextButton(onClick = {
                        showAlert = false
                        // 打开设置页面
                        UIScene().openURL(
                            url = NSURL(string = "App-prefs://"),
                            options = null,
                            completionHandler = {},
                        )
                    }) {
                        Text("确认")
                    }
                },
                text = {
                    Text("通知权限被拒绝，请前往设置中打开通知权限")
                }
            )
        }
    }
}