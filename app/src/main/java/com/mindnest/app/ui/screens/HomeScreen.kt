package com.mindnest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mindnest.app.data.FolderWithCounts
import com.mindnest.app.ui.NestViewModel
import com.mindnest.app.ui.Screen
import com.mindnest.app.ui.components.CardShape
import com.mindnest.app.ui.components.EmptyState
import com.mindnest.app.ui.components.FolderTile
import com.mindnest.app.ui.components.IconAction
import com.mindnest.app.ui.components.SectionLabel
import com.mindnest.app.ui.components.cardBorder
import java.util.Calendar

/** Shared bottom padding so the floating Capture button never covers content. */
val ListPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp)

fun LazyGridScope.fullWidth(key: Any? = null, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

fun LazyGridScope.folderTiles(folders: List<FolderWithCounts>, vm: NestViewModel) {
    items(folders, key = { "f" + it.folder.id }) { item ->
        FolderTile(
            item = item,
            onClick = { vm.open(Screen.FolderView(item.folder.id)) },
            onLongClick = { vm.showFolderActions(item.folder) },
        )
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    in 18..22 -> "Good evening"
    else -> "Hello, night owl"
}

@Composable
fun HomeScreen(vm: NestViewModel) {
    val folders by remember { vm.childFolders(null) }.collectAsState(initial = emptyList())
    val inboxCount by vm.inboxCount.collectAsState()
    val total by vm.totalIdeas.collectAsState()
    val spark = vm.spark

    LaunchedEffect(total) {
        if (vm.spark == null && total >= 3) vm.shuffleSpark()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = ListPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        fullWidth("header") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        greeting(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("Mindnest", style = MaterialTheme.typography.headlineLarge)
                }
                IconAction(Icons.Rounded.Search, "Search") { vm.open(Screen.Search) }
            }
        }

        fullWidth("prompt") {
            Surface(
                onClick = { vm.startCapture(null) },
                shape = CardShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(20.dp)) {
                    Icon(
                        Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "What's on your mind?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        fullWidth("inbox") {
            Surface(
                onClick = { vm.open(Screen.Inbox) },
                shape = CardShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = cardBorder(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(18.dp)) {
                    Icon(
                        Icons.Rounded.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Inbox", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when (inboxCount) {
                                0 -> "All sorted — nice work ✨"
                                1 -> "1 thought waiting to be sorted"
                                else -> "$inboxCount thoughts waiting to be sorted"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (spark != null && total >= 3) {
            fullWidth("spark") {
                Surface(
                    onClick = { vm.edit(spark) },
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(start = 18.dp, end = 8.dp, top = 14.dp, bottom = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Remember this?",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f),
                            )
                            IconAction(Icons.Rounded.Shuffle, "Show another") { vm.shuffleSpark() }
                        }
                        Text(
                            spark.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        Text(
                            vm.locationLabel(spark.folderId),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
                        )
                    }
                }
            }
        }

        fullWidth("folders-label") {
            SectionLabel("Folders") {
                TextButton(onClick = { vm.newFolder(null) }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
        }

        if (folders.isEmpty()) {
            fullWidth("folders-empty") {
                EmptyState(
                    emoji = "🗂️",
                    title = "No folders yet",
                    body = "Folders are optional. Make one when a few thoughts start to belong together.",
                )
            }
        } else {
            folderTiles(folders, vm)
        }

        fullWidth("bottom-space") { Spacer(Modifier.height(8.dp)) }
    }
}
