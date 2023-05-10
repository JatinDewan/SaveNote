package note.notes.savenote.ViewModelClasses

import note.notes.savenote.Database.Note
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import note.notes.savenote.Composables.PageNav
import note.notes.savenote.Utils.CheckStringUtil
import note.notes.savenote.Utils.DateUtils

class NotesViewModel(
    primaryViewModel: PrimaryViewModel,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    private val primaryView = primaryViewModel
    private val checkStringUtil = CheckStringUtil()



    fun shareNote(): Intent {
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${uiState.value.header}\n${noteEntry.text}")
                type = "text/plain"
            }, null
        )
    }

    fun navigateToNote(note: Note){
        viewModelScope.launch {
            noteChecker(note)
            uid(note.uid)
            header(checkStringUtil.replaceNull(note.header))
            update(TextFieldValue(checkStringUtil.replaceNull(note.note)))
            category(note.category)
        }
    }

    private fun category(category:String?) {
        _uiState.update { currentState ->
            currentState.copy(
                category = category
            )
        }
    }

    fun confirmDelete(confirmDelete: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmDelete = confirmDelete
            )
        }
    }

    fun header(header: String?) {
        if(header != null){
            _uiState.update { currentState ->
                currentState.copy(header = header)
            }
        }
    }

    fun uid(uid: Int?) {
        if(uid != null) {
            _uiState.update { currentState ->
                currentState.copy( uid = uid )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun returnAndSaveNote(navController: NavController) {
        viewModelScope.launch {
            editOrDeleteNote()
            delay(100)
            navController.navigate(route = PageNav.AllNotes.name)
        }
    }

    fun clearNote(){
        viewModelScope.launch{
            _uiState.update { currentState ->
                currentState.copy(
                    uid = 0,
                    header = "",
                )
            }
            update(TextFieldValue(""))
        }
    }

    fun noteChecker(note: Note) {
        _uiState.update { currentState ->
            currentState.copy(
                fullNote = note
            )
        }
    }

    fun createBlankNote() {
        viewModelScope.launch{
            primaryView.notesRepositoryImp.insertNote(
                Note(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )

            primaryView.notesRepositoryImp.getNote().collect {

                if(it.isNotEmpty()) {
                    if(it.last().note.isNullOrEmpty() && it.last().header.isNullOrEmpty() &&
                        it.last().checkList.isNullOrEmpty()
                    ){
                        navigateToNote(it.last())
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveNoteEdit(){
        val headerCheck =
            uiState.value.fullNote?.header != uiState.value.header && uiState.value.header.isNotEmpty()
        val noteCheck =
            uiState.value.fullNote?.note != noteEntry.text && noteEntry.text.isNotEmpty()

        if(headerCheck || noteCheck) {
            viewModelScope.launch {
                primaryView.notesRepositoryImp.editNote(
                    Note(
                        uiState.value.uid,
                        checkStringUtil.checkString(uiState.value.header),
                        checkStringUtil.checkString(noteEntry.text),
                        DateUtils().current,
                        null,
                        uiState.value.category
                    )
                )
            }
        }
    }
    @OptIn(SavedStateHandleSaveableApi::class)
    var noteEntry by savedStateHandle.saveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    fun update(newEntry: TextFieldValue) = viewModelScope.launch{ noteEntry = newEntry }

    @RequiresApi(Build.VERSION_CODES.O)
    fun editOrDeleteNote() {
        val headerCheck = uiState.value.fullNote?.header != checkStringUtil.checkString(uiState.value.header)
        val noteCheck = uiState.value.fullNote?.note != checkStringUtil.checkString(noteEntry.text)
        viewModelScope.launch{
            when {
                uiState.value.header.isEmpty() && noteEntry.text.isEmpty() -> {
                    primaryView.notesRepositoryImp.deleteNote(
                        Note(
                            uiState.value.uid,
                            null,
                            null,
                            null,
                            null,
                            null

                        )
                    )
                }

                headerCheck || noteCheck -> {
                    primaryView.notesRepositoryImp.editNote(
                        Note(
                            uiState.value.uid,
                            checkStringUtil.checkString(uiState.value.header),
                            checkStringUtil.checkString(noteEntry.text),
                            DateUtils().current,
                            null,
                            uiState.value.category
                        )
                    )
                }
            }
        }
    }
}