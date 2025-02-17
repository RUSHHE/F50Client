package org.rushhe.f50client.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.rushhe.f50client.helper.NotificationHelper
import org.rushhe.f50client.model.Message
import org.rushhe.f50client.model.MessageGroup
import org.rushhe.f50client.utils.formatSMSDate
import org.rushhe.f50client.viewmodel.SMSListUiState
import org.rushhe.f50client.viewmodel.SMSViewModel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSListRoute(
    smsSelected: (MessageGroup) -> Unit,
) {
    val viewModel: SMSViewModel = koinViewModel()
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    var isRefreshing by remember { mutableStateOf(false) }
    isRefreshing = !(networkState is SMSListUiState.Success || networkState is SMSListUiState.Error)

    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            viewModel.refresh()
        }
    }

    SMSListScreen(
        uiState = uiState,
        networkState = networkState,
        smsSelected = smsSelected,
        isRefreshing = isRefreshing,
        state = pullToRefreshState,
        onRefresh = onRefresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSListScreen(
    uiState: SMSListUiState,
    networkState: SMSListUiState,
    smsSelected: (MessageGroup) -> Unit,
    isRefreshing: Boolean,
    state: PullToRefreshState,
    onRefresh: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationHelper: NotificationHelper = koinInject<NotificationHelper>()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "信息") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // 调用检查通知权限的函数
        notificationHelper.CheckAppNotificationPermission()

        var showDialog by remember { mutableStateOf(false) }

        // 网络错误对话框
        if (networkState is SMSListUiState.Error && showDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("错误") },
                text = {
                    Text(text = networkState.message)
                },
                confirmButton = {
                    Row {
                        TextButton(onClick = { showDialog = false }) {
                            Text("确认")
                        }
                    }
                }
            )
        }

        LaunchedEffect(networkState) {
            when (networkState) {
                is SMSListUiState.Error -> {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "加载失败！",
                            actionLabel = "详细",
                            duration = SnackbarDuration.Short
                        )

                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                showDialog = true
                            }

                            SnackbarResult.Dismissed -> {

                            }
                        }
                    }
                }

                is SMSListUiState.Loading -> {

                }

                is SMSListUiState.Success -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "加载成功！",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }

        PullToRefreshBox(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is SMSListUiState.Error -> {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("错误") },
                        text = {

                        },
                        confirmButton = {
                            Row {
                                TextButton(onClick = { }) {
                                    Text("确认")
                                }
                                TextButton(onClick = {}) {
                                    Text("取消")
                                }
                            }
                        }
                    )
                }

                is SMSListUiState.Loading -> {}
                is SMSListUiState.Success -> {
                    SMSList(uiState.result, smsSelected)
                }
            }
        }
    }
}

@Composable
fun SMSList(messages: List<Message>, smsSelected: (MessageGroup) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // 根据号码对短信进行分组，再转为二元组数组，再降序排序
        items(
            items = messages.groupBy { it.number }.map { MessageGroup(it.key, it.value) }
                .sortedByDescending { it.messageList.maxBy { it1 -> it1.id }.id },
            key = { it.number } // 使用号码作为key提高性能
        ) { messageGroup ->
            SMSListItem(messageGroup, smsSelected)
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun SMSListItem(
    messageGroup: MessageGroup,
    smsSelected: (messageGroup: MessageGroup) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { smsSelected(messageGroup) })
            .padding(8.dp)
    ) {
        // 左侧的头像图标
        Icon(
            imageVector = Icons.Default.AccountCircle,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "头像",
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterVertically)
        )

        // 添加间距
        Spacer(modifier = Modifier.width(16.dp))

        // 中间内容区域
        Column(
            modifier = Modifier
                .weight(1f) // 占据剩余空间
        ) {
            // 电话号码和时间显示在同一行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 电话号码
                Text(
                    text = messageGroup.number,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f) // 电话号码占据剩余空间
                )

                // 显示最新消息的时间
                Text(
                    text = messageGroup.messageList.maxBy { it.id }.date.formatSMSDate("yyyy-MM-dd HH:mm:ss"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // 添加垂直间距
            Spacer(modifier = Modifier.height(4.dp))

            // 最新的短信内容
            Text(
                text = runCatching {
                    // 解码 Base64 字符串
                    val latestMessage = messageGroup.messageList.maxBy { it.id }
                    Base64.decode(latestMessage.content).decodeToString()
                }.getOrElse { "解码失败" }, // 处理可能的解码异常
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, // 限制为单行
                overflow = TextOverflow.Ellipsis // 超出部分用省略号表示
            )
        }
    }
}