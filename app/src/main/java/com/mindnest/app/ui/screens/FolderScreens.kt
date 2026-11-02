package com.mindnest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindnest.app.ui.MoveTarget
import com.mindnest.app.ui.NestViewModel
import com.mindnest.app.ui.components.Breadcrumbs
import com.mindnest.app.ui.components.EmojiBadge
import com.mindnest.app.ui.components.EmptyState
import com.mindnest.app.ui.components.IdeaCard
import com.mindnest.app.ui.components.ScreenHeader
import com.mindnest.app.ui.components.SectionLabel
import com.mindnest.app.ui.components.SwipeToDelete

@Composable
fun FolderScreen(vm: NestViewModel, folderId: Long) {
    val allFolders by vm.allFolders.collectAsState()
    val folder = allFolders.firstOrNull { it.id == folderId }
    val subfolders by remember(folderId) { vm.childFolders(folderId) }.collectAsState(initial = emptyList())
    val ideas by remember(folderId) { vm.ideasIn(folderId) }.collectAsState(initial = emptyList())
    var menuOpen by remember { mutableStateOf(false) }

    if (folder == null) {
        // Still loading, or the folder was just deleted.
        Box(Modifier.fillMaxSize())
        return
    }
    val path = remember(allFolders, folderId) { vm.pathTo(folderId) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = folder.name,
            onBack = vm::back,
            leading = { EmojiBadge(folder.emoji, folder.colorIndex, size = 40) },
            actions = {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Folder options")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit name & look") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                            onClick = { menuOpen = false; vm.editFolder(folder) },
                        )
                        DropdownMenuItem(
                            text = { Text("New folder inside") },
                            leadingIcon = { Icon(Icons.Rounded.CreateNewFolder, null) },
                            onClick = { menuOpen = false; vm.newFolder(folder.id) },
                        )
                        DropdownMenuItem(
                            text = { Text("Move folder") },
                            leadingIcon = { Icon(Icons.Rounded.DriveFileMove, null) },
                            onClick = { menuOpen = false; vm.openMover(MoveTarget.MoveFolder(folder)) },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete folder") },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                            onClick = { menuOpen = false; vm.requestDeleteFolder(folder) },
                        )
                    }
                }
            },
        )
        Breadcrumbs(path = path, onNavigate = vm::goTo)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = ListPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            fullWidth("sub-label") {
                SectionLabel("Folders") {
                    TextButton(onClick = { vm.newFolder(folderId) }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New")
                    }
                }
            }
            folderTiles(subfolders, vm)

            if (ideas.isNotEmpty()) {
                fullWidth("ideas-label") { SectionLabel("Ideas") }
            }
            ideas.forEach { idea ->
                fullWidth("i" + idea.id) {
                    SwipeToDelete(onDelete = { vm.deleteIdea(idea) }) {
                        IdeaCard(idea = idea, onClick = { vm.edit(idea) })
                    }
                }
            }

            if (ideas.isEmpty() && subfolders.isEmpty()) {
                fullWidth("empty") {
                    EmptyState(
                        emoji = folder.emoji,
                        title = "Nothing here yet",
                        body = "Tap Capture to drop a thought straight into “${folder.name}”.",
                    )
                }
            }
        }
    }
}

@Composable
fun InboxScreen(vm: NestViewModel) {
    val ideas by remember { vm.ideasIn(null) }.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Inbox",
            subtitle = "Capture now, sort later",
            onBack = vm::back,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            contentPadding = ListPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (ideas.isEmpty()) {
                fullWidth("empty") {
                    EmptyState(
                        emoji = "🎉",
                        title = "Inbox zero!",
                        body = "Everything is sorted. New thoughts you capture from Home land here first.",
                    )
                }
            }
            ideas.forEach { idea ->
                item(key = idea.id) {
                    SwipeToDelete(onDelete = { vm.deleteIdea(idea) }) {
                        IdeaCard(
                            idea = idea,
                            onClick = { vm.edit(idea) },
                            trailing = {
                                FilledTonalButton(
                                    onClick = { vm.openMover(MoveTarget.MoveIdea(idea)) },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
                                    modifier = Modifier.size(width = 86.dp, height = 34.dp),
                                ) {
                                    Text("Sort", style = MaterialTheme.typography.labelLarge)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
