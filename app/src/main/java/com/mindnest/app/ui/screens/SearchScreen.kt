package com.mindnest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mindnest.app.data.FolderWithCounts
import com.mindnest.app.data.Idea
import com.mindnest.app.ui.NestViewModel
import com.mindnest.app.ui.components.EmptyState
import com.mindnest.app.ui.components.FolderTile
import com.mindnest.app.ui.components.IdeaCard
import com.mindnest.app.ui.components.SectionLabel
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchScreen(vm: NestViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val allFolders by vm.allFolders.collectAsState()
    val ideas by remember(q) {
        if (q.isEmpty()) flowOf(emptyList<Idea>()) else vm.searchIdeas(q)
    }.collectAsState(initial = emptyList())
    val folders = remember(q, allFolders) {
        if (q.isEmpty()) emptyList() else allFolders.filter { it.name.contains(q, ignoreCase = true) }
    }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        ) {
            IconButton(onClick = vm::back) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search ideas and folders") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = ListPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                q.isEmpty() -> fullWidth("hint") {
                    EmptyState("🔎", "Find anything", "Type a word you remember — it searches every idea and folder.")
                }
                folders.isEmpty() && ideas.isEmpty() -> fullWidth("none") {
                    EmptyState("🤔", "No matches", "Nothing contains “$q”. Try a shorter word?")
                }
            }
            if (folders.isNotEmpty()) {
                fullWidth("f-label") { SectionLabel("Folders") }
                folders.forEach { f ->
                    item(key = "f" + f.id) {
                        FolderTile(
                            item = FolderWithCounts(f, ideaCount = 0, folderCount = 0),
                            subtitle = vm.locationLabel(f.parentId).let { if (f.parentId == null) "Top level" else "in $it" },
                            onClick = { vm.goTo(f.id) },
                            onLongClick = { vm.showFolderActions(f) },
                        )
                    }
                }
            }
            if (ideas.isNotEmpty()) {
                fullWidth("i-label") { SectionLabel("Ideas") }
                ideas.forEach { idea ->
                    fullWidth("i" + idea.id) {
                        IdeaCard(
                            idea = idea,
                            subtitle = vm.locationLabel(idea.folderId),
                            onClick = { vm.edit(idea) },
                        )
                    }
                }
            }
        }
    }
}
