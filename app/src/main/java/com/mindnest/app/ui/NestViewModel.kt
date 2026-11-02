package com.mindnest.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.mindnest.app.data.Folder
import com.mindnest.app.data.FolderWithCounts
import com.mindnest.app.data.Idea
import com.mindnest.app.data.NestDatabase
import com.mindnest.app.ui.theme.FolderColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data object Inbox : Screen
    data object Search : Screen
    data class FolderView(val id: Long) : Screen
}

/** Where a new capture from this screen should land. */
fun Screen.captureTarget(): Long? = (this as? Screen.FolderView)?.id

data class EditorState(
    val original: Idea? = null,
    val folderId: Long? = null,
    val text: String = "",
    val pinned: Boolean = false,
)

sealed interface MoveTarget {
    data class MoveIdea(val idea: Idea) : MoveTarget
    data class MoveFolder(val folder: Folder) : MoveTarget
}

data class FolderDialogState(val existing: Folder? = null, val parentId: Long? = null)

data class DeleteFolderState(val folder: Folder, val subfolders: Int, val ideas: Int)

data class UiMessage(val text: String, val undo: (() -> Unit)? = null)

data class TreeNode(val folder: Folder, val depth: Int, val path: String)

/** Folders flattened in tree order (parents before children), with depth for indentation. */
fun flattenTree(all: List<Folder>): List<TreeNode> {
    val byParent = all.groupBy { it.parentId }
    val out = mutableListOf<TreeNode>()
    fun walk(parentId: Long?, depth: Int, prefix: String) {
        byParent[parentId].orEmpty().sortedBy { it.name.lowercase() }.forEach { f ->
            val path = if (prefix.isEmpty()) f.name else "$prefix › ${f.name}"
            out += TreeNode(f, depth, path)
            walk(f.id, depth + 1, path)
        }
    }
    walk(null, 0, "")
    return out
}

val FolderEmojis = listOf(
    "📁", "💡", "🚀", "🎨", "📚", "🎵", "🛒", "✈️",
    "🏠", "💼", "🧠", "❤️", "🌱", "🎮", "🍳", "💰",
    "🏋️", "📝", "🔧", "🎁", "🌙", "⭐", "🧩", "📷",
)

class NestViewModel(app: Application) : AndroidViewModel(app) {
    private val db = NestDatabase.get(app)
    private val dao = db.dao()

    // ---------- Observed data ----------

    val allFolders: StateFlow<List<Folder>> =
        dao.allFolders().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val inboxCount: StateFlow<Int> =
        dao.inboxCount().stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val totalIdeas: StateFlow<Int> =
        dao.totalIdeas().stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun childFolders(parentId: Long?): Flow<List<FolderWithCounts>> = dao.childFolders(parentId)
    fun ideasIn(folderId: Long?): Flow<List<Idea>> = dao.ideasIn(folderId)
    fun searchIdeas(query: String): Flow<List<Idea>> = dao.searchIdeas(query)

    // ---------- UI state ----------

    val backStack = mutableStateListOf<Screen>(Screen.Home)
    var editor by mutableStateOf<EditorState?>(null)
        private set
    var mover by mutableStateOf<MoveTarget?>(null)
        private set
    var folderDialog by mutableStateOf<FolderDialogState?>(null)
        private set
    var folderActions by mutableStateOf<Folder?>(null)
        private set
    var pendingDelete by mutableStateOf<DeleteFolderState?>(null)
        private set
    var spark by mutableStateOf<Idea?>(null)
        private set

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 8)
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    private fun say(text: String, undo: (() -> Unit)? = null) {
        _messages.tryEmit(UiMessage(text, undo))
    }

    // ---------- Navigation ----------

    fun open(screen: Screen) {
        if (backStack.last() != screen) backStack.add(screen)
    }

    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    /** Jump to a folder (or Home when null) with a clean, predictable back stack. */
    fun goTo(folderId: Long?) {
        val path = folderId?.let { pathTo(it) }.orEmpty()
        backStack.clear()
        backStack.add(Screen.Home)
        path.forEach { backStack.add(Screen.FolderView(it.id)) }
    }

    fun folderById(id: Long?): Folder? = id?.let { fid -> allFolders.value.firstOrNull { it.id == fid } }

    fun pathTo(folderId: Long): List<Folder> {
        val byId = allFolders.value.associateBy { it.id }
        val out = ArrayDeque<Folder>()
        var current = byId[folderId]
        var guard = 0
        while (current != null && guard++ < 64) {
            out.addFirst(current)
            current = current.parentId?.let(byId::get)
        }
        return out.toList()
    }

    fun locationLabel(folderId: Long?): String =
        if (folderId == null) "Inbox" else pathTo(folderId).joinToString(" › ") { it.name }

    // ---------- Capture & edit ----------

    fun startCapture(folderId: Long?, prefill: String = "") {
        editor = EditorState(folderId = folderId, text = prefill)
    }

    fun edit(idea: Idea) {
        editor = EditorState(original = idea, folderId = idea.folderId, text = idea.text, pinned = idea.pinned)
    }

    fun updateEditor(transform: EditorState.() -> EditorState) {
        editor = editor?.transform()
    }

    /**
     * Saves whatever is in the editor. Called on Save *and* when the sheet is dismissed,
     * so a half-written thought is never lost.
     */
    fun commitEditor(keepOpen: Boolean = false) {
        val st = editor ?: return
        val text = st.text.trim()
        val original = st.original
        val changed = original == null ||
            text != original.text || st.folderId != original.folderId || st.pinned != original.pinned

        if (text.isNotEmpty() && changed) {
            val now = System.currentTimeMillis()
            val idea = Idea(
                id = original?.id ?: 0,
                folderId = st.folderId,
                text = text,
                pinned = st.pinned,
                createdAt = original?.createdAt ?: now,
                updatedAt = now,
            )
            viewModelScope.launch {
                dao.upsertIdea(idea)
                if (original != null && spark?.id == original.id) spark = idea
            }
            if (original == null && !keepOpen) say("Captured to ${locationLabel(st.folderId)} ✨")
        }

        editor = if (keepOpen) EditorState(folderId = st.folderId) else null
    }

    fun discardEditor() {
        editor = null
    }

    fun deleteFromEditor() {
        val original = editor?.original
        editor = null
        if (original != null) deleteIdea(original)
    }

    fun deleteIdea(idea: Idea) {
        if (spark?.id == idea.id) spark = null
        viewModelScope.launch {
            dao.deleteIdea(idea)
            say("Idea deleted") {
                viewModelScope.launch { runCatching { dao.upsertIdea(idea) } }
            }
        }
    }

    fun togglePin(idea: Idea) {
        viewModelScope.launch { dao.setPinned(idea.id, !idea.pinned) }
    }

    fun moveIdea(idea: Idea, folderId: Long?, folderName: String? = null) {
        mover = null
        if (idea.folderId == folderId) return
        viewModelScope.launch {
            dao.moveIdea(idea.id, folderId, System.currentTimeMillis())
            val label = folderName ?: locationLabel(folderId)
            say("Moved to $label") {
                viewModelScope.launch { runCatching { dao.upsertIdea(idea) } }
            }
        }
    }

    // ---------- Folders ----------

    fun newFolder(parentId: Long?) {
        folderActions = null
        folderDialog = FolderDialogState(parentId = parentId)
    }

    fun editFolder(folder: Folder) {
        folderActions = null
        folderDialog = FolderDialogState(existing = folder, parentId = folder.parentId)
    }

    fun dismissFolderDialog() {
        folderDialog = null
    }

    fun saveFolder(name: String, emoji: String, colorIndex: Int) {
        val st = folderDialog ?: return
        folderDialog = null
        val clean = name.trim().ifEmpty { return }
        viewModelScope.launch {
            if (st.existing != null) {
                dao.updateFolder(st.existing.copy(name = clean, emoji = emoji, colorIndex = colorIndex))
            } else {
                dao.insertFolder(Folder(parentId = st.parentId, name = clean, emoji = emoji, colorIndex = colorIndex))
            }
        }
    }

    fun showFolderActions(folder: Folder?) {
        folderActions = folder
    }

    /** The folder plus everything below it, parents first (so it can be re-inserted in order). */
    private fun subtree(root: Folder): List<Folder> {
        val byParent = allFolders.value.groupBy { it.parentId }
        val out = mutableListOf<Folder>()
        val queue = ArrayDeque(listOf(root))
        while (queue.isNotEmpty()) {
            val f = queue.removeFirst()
            out += f
            queue.addAll(byParent[f.id].orEmpty())
        }
        return out
    }

    fun subtreeIds(root: Folder): Set<Long> = subtree(root).map { it.id }.toSet()

    fun requestDeleteFolder(folder: Folder) {
        folderActions = null
        viewModelScope.launch {
            val tree = subtree(folder)
            val ideas = dao.ideasInFolders(tree.map { it.id })
            if (tree.size == 1 && ideas.isEmpty()) {
                deleteFolderNow(folder)
            } else {
                pendingDelete = DeleteFolderState(folder, tree.size - 1, ideas.size)
            }
        }
    }

    fun cancelDelete() {
        pendingDelete = null
    }

    fun confirmDelete() {
        val st = pendingDelete ?: return
        pendingDelete = null
        deleteFolderNow(st.folder)
    }

    private fun deleteFolderNow(folder: Folder) {
        viewModelScope.launch {
            val tree = subtree(folder)
            val ids = tree.map { it.id }.toSet()
            val ideas = dao.ideasInFolders(ids.toList())
            backStack.removeAll { it is Screen.FolderView && it.id in ids }
            if (backStack.isEmpty()) backStack.add(Screen.Home)
            if (spark?.folderId?.let { it in ids } == true) spark = null
            dao.deleteFolder(folder) // children & ideas cascade
            say("Deleted “${folder.name}”") {
                viewModelScope.launch {
                    runCatching {
                        db.withTransaction {
                            dao.insertFolders(tree)
                            dao.insertIdeas(ideas)
                        }
                    }
                }
            }
        }
    }

    fun moveFolder(folder: Folder, parentId: Long?, parentName: String? = null) {
        mover = null
        if (folder.parentId == parentId) return
        if (parentId != null && parentId in subtreeIds(folder)) {
            say("A folder can't go inside itself")
            return
        }
        viewModelScope.launch {
            dao.moveFolder(folder.id, parentId)
            val label = parentName ?: if (parentId == null) "top level" else locationLabel(parentId)
            say("Moved to $label") {
                viewModelScope.launch { runCatching { dao.moveFolder(folder.id, folder.parentId) } }
            }
        }
    }

    // ---------- Move sheet ----------

    fun openMover(target: MoveTarget) {
        folderActions = null
        mover = target
    }

    fun closeMover() {
        mover = null
    }

    /** "Oh, this needs a new folder" — create it and file the item in one go. */
    fun createFolderAndMove(name: String) {
        val target = mover ?: return
        val clean = name.trim().ifEmpty { return }
        mover = null
        viewModelScope.launch {
            val id = dao.insertFolder(
                Folder(name = clean, emoji = "📁", colorIndex = FolderColors.indices.random()),
            )
            when (target) {
                is MoveTarget.MoveIdea -> moveIdea(target.idea, id, clean)
                is MoveTarget.MoveFolder -> moveFolder(target.folder, id, clean)
            }
        }
    }

    // ---------- Rediscover ----------

    fun shuffleSpark() {
        viewModelScope.launch {
            spark = dao.randomIdea(excludeId = spark?.id ?: -1L) ?: dao.randomIdea(-1L)
        }
    }
}
