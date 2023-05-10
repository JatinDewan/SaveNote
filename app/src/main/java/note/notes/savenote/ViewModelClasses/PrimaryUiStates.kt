package note.notes.savenote.ViewModelClasses

import note.notes.savenote.Database.Note
import androidx.compose.runtime.mutableStateListOf

data class PrimaryUiState (
    val allEntries: List<Note> = mutableStateListOf(),
    val favoriteEntries: List<Note> = mutableStateListOf(),
    val searchEntries: List<Note> = mutableStateListOf(),
    val searchQuery: String = "",
    val showSearchBar: Boolean = false,
    val collapseButton: Boolean = false,
    val confirmDelete: Boolean = false,
    val currentPage: Boolean = true,
    val sortByView: Int = 0,
    val showBackup: Boolean = false,
    val dropDown: Boolean = false,
    val showSortBy: Boolean = false,
)