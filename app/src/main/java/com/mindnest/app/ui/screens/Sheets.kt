package com.mindnest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindnest.app.data.Folder
import com.mindnest.app.ui.DeleteFolderState
import com.mindnest.app.ui.EditorState
import com.mindnest.app.ui.FolderDialogState
import com.mindnest.app.ui.FolderEmojis
import com.mindnest.app.ui.MoveTarget
import com.mindnest.app.ui.NestViewModel
import com.mindnest.app.ui.components.EmojiBadge
import com.mindnest.app.ui.flattenTree
import com.mindnest.app.ui.theme.FolderColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Capture / edit
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(vm: NestViewModel, state: EditorState) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val focus = remember { FocusRequester() }
    val folders by vm.allFolders.collectAsState()
    val tree = remember(folders) { flattenTree(folders) }
    var capturedInRow by remember { mutableIntStateOf(0) }
    var closing by remember { mutableStateOf(false) }
    val isNew = state.original == null
    val canSave = state.text.isNotBlank()

    fun closeAnd(action: () -> Unit) {
        closing = true
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    LaunchedEffect(Unit) {
        delay(120)
        runCatching { focus.requestFocus() }
    }

    ModalBottomSheet(
        // Swiping away or tapping outside still saves — a thought is never lost.
        onDismissRequest = { if (!closing) vm.commitEditor() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isNew) "Capture a thought" else "Edit idea",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (capturedInRow > 0) {
                    Text(
                        "$capturedInRow captured ✨",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            TextField(
                value = state.text,
                onValueChange = { v -> vm.updateEditor { copy(text = v) } },
                placeholder = { Text("Dump it here — don't overthink it…") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                minLines = 4,
                maxLines = 10,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus),
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "Where does it go?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))

            val selectedIndex = if (state.folderId == null) 0 else 1 + tree.indexOfFirst { it.folder.id == state.folderId }
            val rowState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0))
            LazyRow(
                state = rowState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp),
            ) {
                item(key = "inbox") {
                    FilterChip(
                        selected = state.folderId == null,
                        onClick = { vm.updateEditor { copy(folderId = null) } },
                        label = { Text("📥  Inbox") },
                    )
                }
                items(tree, key = { it.folder.id }) { node ->
                    FilterChip(
                        selected = state.folderId == node.folder.id,
                        onClick = { vm.updateEditor { copy(folderId = node.folder.id) } },
                        label = { Text("${node.folder.emoji}  ${node.path}", maxLines = 1) },
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isNew) {
                    IconToggleButton(
                        checked = state.pinned,
                        onCheckedChange = { checked -> vm.updateEditor { copy(pinned = checked) } },
                    ) {
                        Icon(
                            if (state.pinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (state.pinned) "Unpin" else "Pin to top",
                            tint = if (state.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { closeAnd { vm.deleteFromEditor() } }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (isNew) {
                    TextButton(
                        enabled = canSave,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            vm.commitEditor(keepOpen = true)
                            capturedInRow++
                        },
                    ) { Text("Save + next") }
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    enabled = canSave || !isNew,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        closeAnd { vm.commitEditor() }
                    },
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isNew) "Save" else "Done")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Move / sort
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveSheet(vm: NestViewModel, target: MoveTarget) {
    val folders by vm.allFolders.collectAsState()
    val excluded = remember(folders, target) {
        if (target is MoveTarget.MoveFolder) vm.subtreeIds(target.folder) else emptySet()
    }
    val tree = remember(folders, excluded) { flattenTree(folders).filter { it.folder.id !in excluded } }
    val currentLocation = when (target) {
        is MoveTarget.MoveIdea -> target.idea.folderId
        is MoveTarget.MoveFolder -> target.folder.parentId
    }

    ModalBottomSheet(
        onDismissRequest = vm::closeMover,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.imePadding().padding(bottom = 16.dp)) {
            Text(
                when (target) {
                    is MoveTarget.MoveIdea -> "Sort into…"
                    is MoveTarget.MoveFolder -> "Move “${target.folder.name}” to…"
                },
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            if (target is MoveTarget.MoveIdea) {
                Text(
                    target.idea.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.heightIn(max = 520.dp)) {
                item(key = "root") {
                    val isIdea = target is MoveTarget.MoveIdea
                    DestinationRow(
                        emoji = if (isIdea) "📥" else "🏠",
                        colorIndex = 7,
                        label = if (isIdea) "Inbox" else "Top level",
                        depth = 0,
                        selected = currentLocation == null,
                    ) {
                        when (target) {
                            is MoveTarget.MoveIdea -> vm.moveIdea(target.idea, null)
                            is MoveTarget.MoveFolder -> vm.moveFolder(target.folder, null)
                        }
                    }
                }
                items(tree, key = { it.folder.id }) { node ->
                    DestinationRow(
                        emoji = node.folder.emoji,
                        colorIndex = node.folder.colorIndex,
                        label = node.folder.name,
                        depth = node.depth,
                        selected = currentLocation == node.folder.id,
                    ) {
                        when (target) {
                            is MoveTarget.MoveIdea -> vm.moveIdea(target.idea, node.folder.id)
                            is MoveTarget.MoveFolder -> vm.moveFolder(target.folder, node.folder.id)
                        }
                    }
                }
                item(key = "new") { NewFolderRow(onCreate = vm::createFolderAndMove) }
            }
        }
    }
}

@Composable
private fun DestinationRow(
    emoji: String,
    colorIndex: Int,
    label: String,
    depth: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (20 + depth * 22).dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
    ) {
        EmojiBadge(emoji, colorIndex, size = 38)
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Text("current", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NewFolderRow(onCreate: (String) -> Unit) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }

    if (!editing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { editing = true }
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(14.dp))
            Text("New folder…", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            placeholder = { Text("Folder name") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onCreate(name) }),
            trailingIcon = {
                IconButton(enabled = name.isNotBlank(), onClick = { onCreate(name) }) {
                    Icon(Icons.Rounded.Check, contentDescription = "Create and move")
                }
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .focusRequester(focus),
        )
    }
}

// ---------------------------------------------------------------------------
// Folder long-press actions
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderActionsSheet(vm: NestViewModel, folder: Folder) {
    ModalBottomSheet(
        onDismissRequest = { vm.showFolderActions(null) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            ) {
                EmojiBadge(folder.emoji, folder.colorIndex, size = 40)
                Spacer(Modifier.width(14.dp))
                Text(folder.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            ActionRow(Icons.AutoMirrored.Rounded.OpenInNew, "Open") {
                vm.showFolderActions(null)
                vm.goTo(folder.id)
            }
            ActionRow(Icons.Rounded.Edit, "Edit name & look") { vm.editFolder(folder) }
            ActionRow(Icons.Rounded.CreateNewFolder, "New folder inside") { vm.newFolder(folder.id) }
            ActionRow(Icons.Rounded.DriveFileMove, "Move") { vm.openMover(MoveTarget.MoveFolder(folder)) }
            ActionRow(Icons.Rounded.Delete, "Delete", destructive = true) { vm.requestDeleteFolder(folder) }
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ListItem(
        headlineContent = { Text(label, color = color, style = MaterialTheme.typography.bodyLarge) },
        leadingContent = { Icon(icon, contentDescription = null, tint = color) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    )
}

// ---------------------------------------------------------------------------
// Create / edit folder
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderDialog(vm: NestViewModel, state: FolderDialogState) {
    val existing = state.existing
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var emoji by rememberSaveable { mutableStateOf(existing?.emoji ?: FolderEmojis.first()) }
    var color by rememberSaveable { mutableIntStateOf(existing?.colorIndex ?: FolderColors.indices.random()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(100)
        runCatching { focus.requestFocus() }
    }
    val parentName = vm.folderById(state.parentId)?.name

    AlertDialog(
        onDismissRequest = vm::dismissFolderDialog,
        title = {
            Text(
                when {
                    existing != null -> "Edit folder"
                    parentName != null -> "New folder in $parentName"
                    else -> "New folder"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Name") },
                    leadingIcon = { Text(emoji, fontSize = 20.sp) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank()) vm.saveFolder(name, emoji, color)
                    }),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus),
                )
                Spacer(Modifier.height(16.dp))
                Text("Icon", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FolderEmojis.forEach { e ->
                        val selected = e == emoji
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) FolderColors[color].copy(alpha = 0.30f)
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                                )
                                .clickable { emoji = e },
                        ) { Text(e, fontSize = 20.sp) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Colour", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FolderColors.forEachIndexed { i, c ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .then(
                                    if (i == color) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable { color = i },
                        ) {
                            if (i == color) {
                                Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = { vm.saveFolder(name, emoji, color) }) {
                Text(if (existing != null) "Save" else "Create")
            }
        },
        dismissButton = {
            TextButton(onClick = vm::dismissFolderDialog) { Text("Cancel") }
        },
    )
}

// ---------------------------------------------------------------------------
// Delete non-empty folder
// ---------------------------------------------------------------------------

@Composable
fun DeleteFolderDialog(vm: NestViewModel, state: DeleteFolderState) {
    val parts = buildList {
        if (state.subfolders > 0) add(if (state.subfolders == 1) "1 folder" else "${state.subfolders} folders")
        if (state.ideas > 0) add(if (state.ideas == 1) "1 idea" else "${state.ideas} ideas")
    }.joinToString(" and ")

    AlertDialog(
        onDismissRequest = vm::cancelDelete,
        title = { Text("Delete “${state.folder.name}”?") },
        text = { Text("This also removes $parts inside it. You can undo straight after.") },
        confirmButton = {
            Button(
                onClick = vm::confirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = vm::cancelDelete) { Text("Keep it") } },
    )
}
