package com.achmadichzan.rangkum.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.achmadichzan.rangkum.presentation.screen.DetailChatScreen
import com.achmadichzan.rangkum.presentation.screen.MainScreen
import com.achmadichzan.rangkum.presentation.viewmodel.ChatViewModel
import com.achmadichzan.rangkum.presentation.viewmodel.ViewModelFactory

@Composable
fun AppNavigation(onStartOverlaySession: (Long) -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            MainScreen(
                onStartSession = { sessionId ->
                    if (sessionId == -1L) {
                        onStartOverlaySession(sessionId)
                    } else {
                        navController.navigate(DetailRoute(sessionId))
                    }
                }
            )
        }

        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            val sessionId = route.sessionId
            val factory = remember { ViewModelFactory(context) }
            val chatViewModel: ChatViewModel = viewModel(factory = factory)

            LaunchedEffect(sessionId) {
                chatViewModel.loadHistorySession(sessionId)
            }

            DetailChatScreen(
                viewModel = chatViewModel,
                onBackClick = { navController.popBackStack() },
                onStartRecordingClick = {
                    navController.popBackStack()
                    onStartOverlaySession(sessionId)
                }
            )
        }
    }
}