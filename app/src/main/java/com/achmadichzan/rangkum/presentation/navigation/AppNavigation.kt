package com.achmadichzan.rangkum.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.achmadichzan.rangkum.presentation.screen.DetailChatScreen
import com.achmadichzan.rangkum.presentation.screen.MainScreen
import com.achmadichzan.rangkum.presentation.viewmodel.ChatViewModel
import com.achmadichzan.rangkum.presentation.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavigation(onStartOverlaySession: (Long) -> Unit) {
    val context = LocalContext.current
    val navigator = rememberListDetailPaneScaffoldNavigator<DetailRoute>()
    val coroutineScope = rememberCoroutineScope()
    val backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange

    BackHandler(navigator.canNavigateBack()) {
        coroutineScope.launch {
            navigator.navigateBack(backNavigationBehavior)
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane(
                enterTransition = expandHorizontally(expandFrom = Alignment.CenterHorizontally),
                exitTransition = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally)
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
                enterTransition = scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)),

                exitTransition = scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            ) {
                navigator.currentDestination?.contentKey?.let { currentRoute ->
                    val sessionId = currentRoute.sessionId

                    val factory = remember { ViewModelFactory(context) }
                    val chatViewModel: ChatViewModel = viewModel(
                        key = sessionId.toString(),
                        factory = factory
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
                } ?:

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pilih percakapan dari daftar", color = Color.Gray)
                }
            }
        }
    )
}