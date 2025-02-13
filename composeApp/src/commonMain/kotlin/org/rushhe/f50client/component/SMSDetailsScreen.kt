package org.rushhe.f50client.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rushhe.f50client.model.Message
import org.rushhe.f50client.model.MessageGroup
import org.rushhe.f50client.utils.formatSMSDate
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun SMSDetailsScreen(
    smsGroup: MessageGroup,
    showBackButton: Boolean,
    popBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            SMSDetailsTopAppBar(personName = smsGroup.number, showBackButton, popBack = popBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        SMSDetailsList(
            messageGroup = smsGroup,
            snackbarHostState = snackbarHostState,
            innerPadding = innerPadding
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMSDetailsTopAppBar(personName: String, showBackButton: Boolean, popBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(personName) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { popBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun SMSDetailsList(
    messageGroup: MessageGroup,
    snackbarHostState: SnackbarHostState,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // 添加内容填充
            verticalArrangement = Arrangement.spacedBy(8.dp) // 设置垂直间距
        ) {
            //  原List按id升序，应倒序显示最新消息
            items(items = messageGroup.messageList.asReversed(), key = { it.id }) { message ->
                MessageCard(
                    message = message,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun MessageCard(
    message: Message,
    snackbarHostState: SnackbarHostState,
) {
    val decodedContent = decodeMessageContent(message.content)
    var showMenu by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column {
        Text(
            text = message.date.formatSMSDate("yyyy-MM-dd HH:mm"),
            style = MaterialTheme.typography.bodySmall,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .combinedClickable(onLongClick = {
                    showMenu = true
                }, onClick = {}), // 切割卡片，防止阴影溢出
            shape = MaterialTheme.shapes.extraLarge, // 设置圆角
        ) {
            Text(
                text = decodedContent,
                modifier = Modifier.padding(16.dp), // 添加文本内边距
                style = MaterialTheme.typography.bodyLarge, // 设置文本样式
                color = MaterialTheme.colorScheme.onSurface // 设置文本颜色
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("复制") },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(decodedContent))
                        showMenu = false
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("已复制到剪贴板")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
private fun decodeMessageContent(encodedContent: String): String {
    return runCatching {
        Base64.decode(encodedContent).decodeToString()
    }.getOrElse {
        "无法显示消息内容"
    }
}