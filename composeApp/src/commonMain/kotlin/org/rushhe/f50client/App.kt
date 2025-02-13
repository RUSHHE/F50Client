package org.rushhe.f50client

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.rushhe.f50client.component.HomeScreen
import org.rushhe.f50client.component.SMSDetailsScreen
import org.rushhe.f50client.component.SMSListRoute
import org.rushhe.f50client.component.SettingsScreen
import org.rushhe.f50client.model.MessageGroup
import org.rushhe.f50client.viewmodel.DarkMode
import org.rushhe.f50client.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
@Preview
fun App() {
    val settingsViewModel: SettingsViewModel = koinViewModel()

    val coroutineScope = rememberCoroutineScope()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val navigator = rememberListDetailPaneScaffoldNavigator<MessageGroup>()

    val darkMode by settingsViewModel.darkMode.collectAsState()

    val isDarkTheme = when (darkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.OPEN -> true
        DarkMode.CLOSE -> false
    }

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch {
            navigator.navigateBack()
        }
    }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = it.contentDescription
                            )
                        },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it }
                    )
                }
            }
        ) {
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen()
                AppDestinations.SMS -> {
                    ListDetailPaneScaffold(
                        directive = navigator.scaffoldDirective,
                        value = navigator.scaffoldValue,
                        listPane = {
                            SMSListRoute { person ->
                                coroutineScope.launch {
                                    navigator.navigateTo(
                                        ListDetailPaneScaffoldRole.Detail,
                                        person
                                    )
                                }
                            }
                        },
                        detailPane = {
                            // 如果 detailPane 需要展示内容，确保处理可能的 null 值
                            navigator.currentDestination?.contentKey?.let { person ->
                                SMSDetailsScreen(
                                    smsGroup = person,
                                    showBackButton = navigator.canNavigateBack(),
                                    popBack = {
                                        coroutineScope.launch {
                                            navigator.navigateBack(BackNavigationBehavior.PopLatest)
                                        }
                                    }
                                )
                            }
                        }
                    )
                }

                AppDestinations.SETTINGS -> SettingsScreen()
            }
        }
    }
}

// Android平台下，需要使用BackHandler来处理返回事件，等待https://youtrack.jetbrains.com/issue/CMP-4419/Support-BackHandler-PredictiveBackHandler的移植
@Composable
expect fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)