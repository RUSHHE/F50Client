package org.rushhe.f50client.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.rushhe.f50client.R

actual class NotificationHelper(private val context: Context) {
    init {
        // Android需要创建通知渠道
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "sms"
            val channelName = "短信"
            val channelDescription = "用于推送最新收到的短信"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    actual fun sendAppNotification(id: Int, title: String, body: String) {
        try {
            // 构建通知
            val notification = NotificationCompat.Builder(context, "sms")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build()

            // 发送通知
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            // 如果没有权限，会抛出 SecurityException
            e.printStackTrace()
        }
    }

    actual fun clearAppNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    actual fun CheckAppNotificationPermission() {
        var showAlert by remember { mutableStateOf(false) }

        // 创建权限状态对象
        val notificationPermission = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )

        val lifecycleOwner = LocalLifecycleOwner.current

        // 在生命周期变化时处理权限请求
        DisposableEffect(key1 = lifecycleOwner, effect = {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    ON_START -> {
                        // 如果未授予权限，则启动权限请求
                        if (!notificationPermission.status.isGranted) {
                            notificationPermission.launchPermissionRequest()
                        }

                        // 如果未授予权限且不需要显示请求，表示用户明确拒绝，则显示 AlertDialog
                        if (!notificationPermission.status.isGranted && !notificationPermission.status.shouldShowRationale) {
                            showAlert = true
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

        // 当权限未被授予且需要显示请求时，显示 AlertDialog
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
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 从其他context启动时，必须添加这个标志
                            }
                        } else {
                            // 低版本使用通用设置
                            Intent(Settings.ACTION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 从其他context启动时，必须添加这个标志
                            }
                        }

                        context.startActivity(intent)
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