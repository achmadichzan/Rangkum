package com.achmadichzan.rangkum.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.achmadichzan.rangkum.presentation.screen.detail.DetailChatScreen
import com.achmadichzan.rangkum.presentation.screen.main.MainScreen
import com.achmadichzan.rangkum.presentation.viewmodels.ChatViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigation(onStartOverlaySession: (Long) -> Unit) {
    val navigator = rememberListDetailPaneScaffoldNavigator<DetailRoute>()
    val coroutineScope = rememberCoroutineScope()
    val backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange

    var activeSessionId by rememberSaveable { mutableStateOf<Long?>(null) }
    val currentRoute = navigator.currentDestination?.contentKey
    LaunchedEffect(currentRoute) {
        if (currentRoute != null) {
            activeSessionId = currentRoute.sessionId
        }
    }

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch {
            navigator.navigateBack(backNavigationBehavior)
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane(
                enterTransition = slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(500)
                ) + fadeIn(animationSpec = tween(500)),

                exitTransition = slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            ) {
                MainScreen(
                    onStartSession = { sessionId ->
                        if (sessionId == -1L) {
                            onStartOverlaySession(sessionId)
                        } else {
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    DetailRoute(sessionId)
                                )
                            }
                        }
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane(
                enterTransition = slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(500)
                ) + fadeIn(animationSpec = tween(500)),

                exitTransition = slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            ) {
                val sessionId = activeSessionId

                if (sessionId != null) {
                    val chatViewModel: ChatViewModel = koinViewModel(
                        key = sessionId.toString()
                    )

                    LaunchedEffect(sessionId) {
                        chatViewModel.loadHistorySession(sessionId)
                    }

                    DetailChatScreen(
                        viewModel = chatViewModel,
                        onBackClick = {
                            if (navigator.canNavigateBack()) {
                                coroutineScope.launch {
                                    navigator.navigateBack(backNavigationBehavior)
                                }
                            }
                        },
                        onStartRecordingClick = {
                            if (navigator.canNavigateBack()) {
                                coroutineScope.launch {
                                    navigator.navigateBack(backNavigationBehavior)
                                }
                            }
                            onStartOverlaySession(sessionId)
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Pilih percakapan dari daftar", color = Color.Gray)
                    }
                }
            }
        }
    )
}