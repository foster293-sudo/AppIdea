package com.mindnest.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindnest.app.ui.screens.CaptureSheet
import com.mindnest.app.ui.screens.DeleteFolderDialog
import com.mindnest.app.ui.screens.FolderActionsSheet
import com.mindnest.app.ui.screens.FolderDialog
import com.mindnest.app.ui.screens.FolderScreen
import com.mindnest.app.ui.screens.HomeScreen
import com.mindnest.app.ui.screens.InboxScreen
import com.mindnest.app.ui.screens.MoveSheet
import com.mindnest.app.ui.screens.SearchScreen

@Composable
fun NestApp(vm: NestViewModel) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = msg.text,
                actionLabel = if (msg.undo != null) "Undo" else null,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) msg.undo?.invoke()
        }
    }

    val screen = vm.backStack.last()
    BackHandler(enabled = vm.backStack.size > 1) { vm.back() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (screen !is Screen.Search) {
                ExtendedFloatingActionButton(
                    onClick = { vm.startCapture(screen.captureTarget()) },
                    icon = { Icon(Icons.Rounded.Lightbulb, contentDescription = null) },
                    text = { Text("Capture", style = MaterialTheme.typography.titleMedium) },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 14 }) togetherWith
                    fadeOut(tween(120))
            },
            label = "screen",
        ) { s ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                when (s) {
                    Screen.Home -> HomeScreen(vm)
                    Screen.Inbox -> InboxScreen(vm)
                    Screen.Search -> SearchScreen(vm)
                    is Screen.FolderView -> FolderScreen(vm, s.id)
                }
            }
        }
    }

    vm.editor?.let { CaptureSheet(vm, it) }
    vm.mover?.let { MoveSheet(vm, it) }
    vm.folderActions?.let { FolderActionsSheet(vm, it) }
    vm.folderDialog?.let { FolderDialog(vm, it) }
    vm.pendingDelete?.let { DeleteFolderDialog(vm, it) }
}
