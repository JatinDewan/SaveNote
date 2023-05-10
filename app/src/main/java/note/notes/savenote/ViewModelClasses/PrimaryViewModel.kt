package note.notes.savenote.ViewModelClasses

import note.notes.savenote.Database.NewArrayConverter
import note.notes.savenote.Database.Note
import note.notes.savenote.Database.NotesRepositoryImp
import note.notes.savenote.Database.SharedPref
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import note.notes.savenote.R
import note.notes.savenote.SaveNoteApplication
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PrimaryViewModel constructor(
    val notesRepositoryImp: NotesRepositoryImp,
    val sharedPref: SharedPref,
): ViewModel() {

    val _uiState = MutableStateFlow(PrimaryUiState())
    val uiState: StateFlow<PrimaryUiState> = _uiState.asStateFlow()
    val temporaryEntryHold = mutableStateListOf<Note>()
    var showSearch = mutableStateListOf<Note>()
    var isReady = false
    init {
        updateList()
        updateSortByView()
        updatePage()
        isReady = true
    }

    fun changeViewCards(): Int = if(uiState.value.currentPage) 2 else 1

    fun iconSelection(boolean: Boolean, iconOne: Int, iconTwo: Int): Int {
        return if(boolean) iconOne else iconTwo
    }

    private fun showSearchBar(show: Boolean) {
        _uiState.update { currentState -> currentState.copy(showSearchBar = show) }
    }

    fun containerSize():String = if(temporaryEntryHold.size > 99) "99+" else temporaryEntryHold.size.toString()

    fun showBackup(boolean: Boolean){
        _uiState.update { currentState -> currentState.copy(showBackup = boolean) }
    }

    fun dropDown(boolean: Boolean){
        _uiState.update { currentState -> currentState.copy(dropDown = boolean) }
    }

    fun showSortBy(boolean: Boolean){
        _uiState.update { currentState -> currentState.copy(showSortBy = boolean) }
    }

    fun cardFunctionSelection(boolean: Boolean, returnOperationOne:() -> Unit, returnOperationTwo: () -> Unit) {
        if (boolean) returnOperationOne() else returnOperationTwo()
    }

    fun confirmDelete(confirmDelete: Boolean) {
        _uiState.update { currentState -> currentState.copy(confirmDelete = confirmDelete) }
    }

    fun searchQuery(query:String) {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(searchQuery = query) }
            searchNotes()
        }
    }

    fun collapse(collapse: Boolean) {
        _uiState.update { currentState -> currentState.copy(collapseButton = collapse) }
    }

    private fun updatePage(){
        viewModelScope.launch{
            sharedPref.getLayout.collect {
                _uiState.update { currentState -> currentState.copy(currentPage = it) }
            }
        }
    }

    fun updateCurrentPageView(page: Boolean){
        viewModelScope.launch {
            sharedPref.saveLayout(page)
            updatePage()
        }
    }

    private fun updateSortByView(){
        viewModelScope.launch{
            sharedPref.getView.collect {
                _uiState.update { currentState -> currentState.copy(sortByView = it) }
            }
        }
    }

    fun requestFocus(focusRequester: FocusRequester, focusManager: FocusManager) {
        viewModelScope.launch {
            showSearchBar(!_uiState.value.showSearchBar)
            delay(200)
            if (_uiState.value.showSearchBar) focusRequester.requestFocus() else focusManager.clearFocus()
        }
    }

    fun selectAllNotes() {
        viewModelScope.launch{
            when{
                temporaryEntryHold.containsAll(uiState.value.allEntries) &&
                        temporaryEntryHold.containsAll(uiState.value.favoriteEntries) ->
                    temporaryEntryHold.clear()
                else -> {
                    uiState.value.allEntries.forEach {
                        if (it !in temporaryEntryHold) temporaryEntryHold.add(it)
                    }
                    uiState.value.favoriteEntries.forEach {
                        if(it !in temporaryEntryHold) temporaryEntryHold.add(it)
                    }
                }
            }
        }
    }

    fun help(context: Context){
        val brand = Build.BRAND
        val device = Build.DEVICE
        val model = Build.MODEL
        try{
            val sendEmail = Intent(Intent.ACTION_SEND)
            sendEmail.type = "vnd.android.cursor.item/email"
            sendEmail.putExtra(Intent.EXTRA_EMAIL, arrayOf("savenoteapp@gmail.com"))
            sendEmail.putExtra(Intent.EXTRA_SUBJECT, "SaveNote - Help / Feedback - ($brand $device $model)")
            ContextCompat.startActivity(context, sendEmail, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email client installed", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Toast.makeText(context, "Please use a valid Email Client", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteAllEmpty() {
        val deleteEmpty = notesRepositoryImp.getNote().onEach {
            it.forEach { entry ->
                if (entry.note.isNullOrEmpty() && entry.checkList.isNullOrEmpty() &&
                    entry.header.isNullOrEmpty()
                ) {
                    notesRepositoryImp.deleteNote(entry)
                }
            }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            delay(2000)
            deleteEmpty.cancel()
        }
    }

    fun deleteTally(note: Note) {
        viewModelScope.launch{
            if (temporaryEntryHold.contains(note))
                temporaryEntryHold.remove(note) else
                temporaryEntryHold.add(note)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            notesRepositoryImp.deleteSelected(temporaryEntryHold)
            temporaryEntryHold.clear()
        }
    }
    fun sortByString(): Int{
        return when(uiState.value.sortByView) {
            1 -> R.string.lastEdit
            2 -> R.string.oldest
            else -> R.string.newest
        }
    }

    fun updateCurrentPageView(changeView: Int) {
        val returnNumber: Int = if(changeView > 2) 1 else changeView + 1

        viewModelScope.launch {
            sharedPref.saveSortView(returnNumber)
            updatePage()
        }
    }

    private fun isCategory(current: String?, category: String): String? {
        return when(current) {
            category -> null
            null -> category
            else  ->  current
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun favouriteSelected(category: String) {
        viewModelScope.launch {
            temporaryEntryHold.forEach {
                notesRepositoryImp.updateCategory(
                    isCategory(it.category,category),
                    it.uid,
                )
            }
            delay(10)
            temporaryEntryHold.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun compareLastEdit(): Comparator<Note> {
        return when(uiState.value.sortByView){
            1 -> compareBy<Note> { LocalDateTime.parse(it.date, DateTimeFormatter.ofPattern("dd MMMM yy, EEEE, HH:mm")) }.reversed()
            2 -> compareBy<Note> { it.uid }
            else -> compareBy<Note> { it.uid }.reversed()
        }
    }

    fun clearSearch(boolean: Boolean) {
        viewModelScope.launch{
            if (boolean){
                showSearchBar(false)
                delay(300)
                searchQuery("")
            }
        }
    }

    private fun searchNotes() {
        viewModelScope.launch {
            notesRepositoryImp.getNote().collect {
                if(uiState.value.searchQuery.isNotEmpty()){
                    _uiState.update { update ->
                        update.copy(
                            searchEntries = it.filter { note ->
                                note.note?.lowercase()?.contains(uiState.value.searchQuery.lowercase()) == true ||
                                        note.header?.lowercase()?.contains(uiState.value.searchQuery.lowercase()) == true ||
                                        finderChecklist(uiState.value.searchQuery.lowercase(), note)
                            }
                        )
                    }
                } else {
                    _uiState.update { update ->
                        update.copy(searchEntries = emptyList())
                    }
                }
            }
        }
    }

    private fun updateList() {
        viewModelScope.launch {
            notesRepositoryImp.getNote().collect {
                _uiState.update { update ->
                    update.copy(allEntries = it.filter { it.category == null })
                }
                _uiState.update { update ->
                    update.copy(favoriteEntries = it.filter { it.category != null })
                }
            }
        }
    }

    fun backUpNotes(uri: Uri?, context: Context){
        val collectList = (uiState.value.allEntries + uiState.value.favoriteEntries)
        val writeTo = uri?.let { context.contentResolver.openOutputStream(it,"rw") }

        try{
            viewModelScope.launch {
                writeTo?.bufferedWriter()?.use {
                    it.write("****FileVerificationTag****")
                    it.write("****CustomListSplitMethod****")
                    collectList.forEach { note ->
                        it.write(NewArrayConverter().fromString(note))
                        it.write("****CustomListSplitMethod****")
                    }
                }
            }
        } catch (e: Exception){
            Toast.makeText(context, "Invalid Location", Toast.LENGTH_SHORT).show()
        } finally {
            writeTo?.close()
        }
    }

    fun restoreNotes(uri: Uri?, context: Context) {
        val temporaryList = mutableListOf<Note>()
        val createStream = uri?.let { context.contentResolver.openInputStream(it) }
        try{
            val openReader = createStream?.bufferedReader().use { it?.readText() }
            if (openReader?.split("****CustomListSplitMethod****")?.first() == "****FileVerificationTag****") {
                viewModelScope.launch {
                    openReader.split("****CustomListSplitMethod****").drop(1).forEach { entries ->
                        val note = NewArrayConverter().toString(entries)
                        if (note !in uiState.value.allEntries + uiState.value.favoriteEntries) {
                            if (!note.note.isNullOrEmpty() || !note.checkList.isNullOrEmpty()) {
                                temporaryList.add(
                                    Note(
                                        uid = null,
                                        header = note.header,
                                        note = note.note,
                                        date = note.date,
                                        checkList = note.checkList,
                                        category = note.category
                                    )
                                )
                            }
                        }
                    }
                    notesRepositoryImp.insertAll(temporaryList)
                }
                Toast.makeText(context, "Transfer Successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Incompatible File Type", Toast.LENGTH_SHORT).show()
            }
        }catch(e: Exception){
            Toast.makeText(context, "Incompatible File Type", Toast.LENGTH_SHORT).show()
        } finally {
            createStream?.close()
        }
    }

    private fun finderChecklist (string: String, note: Note):Boolean {
        var checklistContainsEntry = false
        if (note.checkList != null) {
            note.checkList.forEach {
                if (it.note.lowercase().contains(string.lowercase())) {
                    checklistContainsEntry = true
                }
            }
        }
        return checklistContainsEntry
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val notesRepositoryImp = (this[APPLICATION_KEY] as SaveNoteApplication).notesRepositoryImp
                val sharedPref = (this[APPLICATION_KEY] as SaveNoteApplication).sharedPref
                PrimaryViewModel(
                    notesRepositoryImp = notesRepositoryImp,
                    sharedPref = sharedPref,
                )
            }
        }
    }
}