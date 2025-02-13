package org.rushhe.f50client.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.rushhe.f50client.viewmodel.DarkMode
import org.rushhe.f50client.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = koinViewModel()

    val routerAddress by viewModel.routerAddress.collectAsState()
    val pollingInterval by viewModel.pollingInterval.collectAsState()

    val darkMode by viewModel.darkMode.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            // 路由地址
            Box {
                var showDialog by remember { mutableStateOf(false) }

                SettingsItem(
                    title = "路由地址",
                    subTitle = routerAddress,
                    icon = Icons.Outlined.Router,
                    onClick = { showDialog = true }
                ) {
                    if (showDialog) {
                        RouterInputDialog(
                            title = "请输入路由地址",
                            initialValue = routerAddress,
                            onConfirm = { routerAddress ->
                                viewModel.setRouterAddress(routerAddress)
                                showDialog = false
                            },
                            onDismiss = {
                                showDialog = false
                            }
                        )
                    }
                }
            }

            // 论询设置
            Column {
                // 定义 expandSubItem 状态
                var expandSubItem by remember { mutableStateOf(pollingInterval > 0) }

                // 0表示关闭
                SettingsItem(
                    title = "论询设置",
                    icon = Icons.Outlined.Sync,
                    isChecked = expandSubItem,
                    onCheckChange = { onCheckChanged ->
                        expandSubItem = onCheckChanged
                    }
                ) {
                    // 如果展开，显示输入框
                    if (expandSubItem) {
                        TextField(
                            value = pollingInterval.toString(),
                            label = { Text("请输入论询间隔") },
                            trailingIcon = { Text("秒") },
                            onValueChange = { newValue ->
                                // 判断输入值，更新 ViewModel，如果为 0，则设置为 0
                                val newInterval = if (newValue.isEmpty()) {
                                    0
                                } else {
                                    newValue.toIntOrNull() ?: pollingInterval
                                }
                                viewModel.setPollingInterval(newInterval)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth() // 让输入框占满宽度
                        )
                    }
                }
            }

            // 深色模式设置
            SettingsItem(
                title = "深色模式",
                subTitle = darkMode.title,
                icon = Icons.Outlined.DarkMode,
                dropdownMenuItemContent = { onDismiss -> // 接收 onDismiss 参数
                    DarkMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = (mode == darkMode),
                                        onClick = {
                                            viewModel.setDarkMode(mode)
                                            onDismiss() // 关闭菜单
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(mode.title, style = MaterialTheme.typography.bodyLarge)
                                }
                            },
                            onClick = {
                                viewModel.setDarkMode(mode)
                                onDismiss() // 点击后关闭菜单
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subTitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp) // 设置固定大小
        )
        Spacer(modifier = Modifier.width(16.dp)) // 控制图标和文本间距
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 标题
            Text(title)
            // 副标题
            subTitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    content()
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    var isCheckedState by remember { mutableStateOf(isChecked) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isCheckedState = !isCheckedState
                onCheckChange(isCheckedState)
            }
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp) // 统一图标大小
            )
            Spacer(modifier = Modifier.width(16.dp)) // 控制图标和文本间距
            Text(title, modifier = Modifier.weight(1f)) // 让文本占据剩余空间
            Switch(
                checked = isCheckedState,
                onCheckedChange = { checked ->
                    isCheckedState = checked
                    onCheckChange(checked)
                }
            )
        }

        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subTitle: String,
    icon: ImageVector,
    dropdownMenuItemContent: @Composable ColumnScope.(onDismiss: () -> Unit) -> Unit // 新增 onDismiss 参数
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 标题
                Text(title)
                // 副标题
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dropdownMenuItemContent { expanded = false } // 传递关闭菜单的 Lambda
        }
    }
}

@Composable
fun RouterInputDialog(
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("输入路由地址") }
            )
        },
        confirmButton = {
            Row {
                TextButton(onClick = { text = "192.168.0.1" }) {
                    Text("默认")
                }
                TextButton(onClick = { onConfirm(text) }) {
                    Text("确认")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}