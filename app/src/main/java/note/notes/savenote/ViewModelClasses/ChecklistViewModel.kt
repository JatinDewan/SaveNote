package note.notes.savenote.ViewModelClasses

import note.notes.savenote.Database.CheckList
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
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
import note.notes.savenote.Database.Note
import note.notes.savenote.Utils.CheckStringUtil
import note.notes.savenote.Utils.URNG
import note.notes.savenote.Utils.DateUtils

class ChecklistViewModel (
    primaryViewModel: PrimaryViewModel,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    private val primaryView = primaryViewModel
    private val checkStringUtil = CheckStringUtil()
    private val keyGen = URNG()

    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()
    var temporaryChecklist = mutableStateListOf<CheckList>()

    @OptIn(SavedStateHandleSaveableApi::class)
    var checklistEntry by savedStateHandle.saveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    init { updateState() }

    fun checklistUncheckedUpdater():List<CheckList> {
        return temporaryChecklist.filter { entry -> entry.strike == 0 }.toMutableStateList()
    }

    fun checklistCheckedUpdater():List<CheckList> {
        return temporaryChecklist.filter { entry -> entry.strike == 1}.toMutableStateList()
    }

    private fun toDoList(note: List<CheckList>?) {
        if (note != null) temporaryChecklist = note.toMutableStateList()
    }

    fun header(header: String?) {
        if(header != null) _uiState.update { currentState -> currentState.copy(header = header) }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun confirmDeleteAllChecked() {
        viewModelScope.launch{ temporaryChecklist.removeIf { it.strike == 1 } }
    }

    fun completedNote(completedNote: Boolean) {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(completedNotes = completedNote) }
        }
    }

    fun unCheckCompleted() {
        viewModelScope.launch{ temporaryChecklist.forEach { if (it.strike == 1) it.strike = 0 } }
    }

    fun update(newEntry: TextFieldValue) = viewModelScope.launch{ checklistEntry = newEntry }

    fun pullUp(isVisible: Boolean) {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(isVisible = isVisible) }
        }
    }

    private fun uid(uid: Int?) {
        if (uid != null) _uiState.update { currentState -> currentState.copy(uid = uid) }
    }

    fun dragRestriction(fromIndex: Int): Boolean {
        return fromIndex <= checklistUncheckedUpdater().lastIndex + 3 && fromIndex >= 3
    }

    fun updateShowCompleted(boolean: Boolean) {
        viewModelScope.launch { primaryView.sharedPref.saveShowCompleted(boolean) }
    }

    private fun updateState() {
        viewModelScope.launch {
            primaryView.sharedPref.getCompleted.collect{
                _uiState.update {  currentState -> currentState.copy( showCompleted = it ) }
            }
        }
    }

    fun editChecklistEntry(entryKey: Int) {
        _uiState.update { currentState -> currentState.copy(checklistKey = entryKey) }
    }

    private fun checklistChecker(note: Note) {
        _uiState.update { currentState -> currentState.copy(fullChecklist = note) }
    }

    fun reArrange(reArrange: Boolean) {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(reArrange = reArrange) }
        }
    }

    fun clearChecklistEdit() {
        viewModelScope.launch{
            _uiState.update { currentState -> currentState.copy(checklistKey = null) }
        }
    }

    fun deleteEntry(location: Int) {
        viewModelScope.launch { temporaryChecklist.removeAt(location) }
    }

    fun visibleCheck(isVisible:Boolean, function:() -> Unit) = if(isVisible) function() else { }

    fun deleteOrComplete(checkList: CheckList){
        if(uiState.value.checklistKey == checkList.key){
            deleteEntry(temporaryChecklist.indexOf(checkList))
        } else {
            entryEditOrAdd(
                entry = checkList.note,
                strike = 1,
                location = temporaryChecklist.indexOf(checkList),
                key = checkList.key,
                deletable = true
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onBackPress(navController: NavController){
        if(uiState.value.isVisible) pullUp(false) else
            if(uiState.value.reArrange) reArrange(false) else returnAndSaveChecklist(navController)
    }



    fun onMoveIndexer(fromKey: Any?, toKey: Any?){
        viewModelScope.launch {
            var fromIndex = -1
            var toIndex = -1
            for (i in 0 until temporaryChecklist.size) {
                val item = temporaryChecklist[i]
                if (item.key == fromKey) {
                    fromIndex = i
                    if (toIndex >= 0) break
                } else if (item.key == toKey) {
                    toIndex = i
                    if (fromIndex >= 0) break
                }
            }
            if (fromIndex >= 0 && toIndex >= 0) {
                temporaryChecklist.add(toIndex, temporaryChecklist.removeAt(fromIndex))
            }
        }
    }

    fun iconSelectionTest(iconSelectionOne: Boolean, iconSelectionTwo: Boolean): Int {
        return when {
            iconSelectionOne -> { note.notes.savenote.R.drawable.switch_vertical_01 }

            iconSelectionTwo -> { note.notes.savenote.R.drawable.x_close }

            else -> { note.notes.savenote.R.drawable.circle }
        }
    }

    fun clearValuesChecklist() {
        viewModelScope.launch{
            temporaryChecklist.clear()
            _uiState.update { currentState ->
                currentState.copy(
                    uid = 0,
                    header = "",
                    checklistEntryNumber = null,
                    checklistKey = null,
                    fullChecklist = null,
                )
            }
            update(TextFieldValue(""))
        }
    }


    fun createBlankChecklist() {
        viewModelScope.launch{
            primaryView.notesRepositoryImp.insertNote(
                Note(
                    null,
                    null,
                    null,
                    null,
                    ArrayList(),
                    null
                )
            )
            primaryView.notesRepositoryImp.getNote().collect {
                if(it.isNotEmpty()) {
                    if(
                        it.last().note.isNullOrEmpty() &&
                        it.last().header.isNullOrEmpty() &&
                        it.last().checkList.isNullOrEmpty()
                    ){
                        navigateToChecklist(it.last())
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun editOrDeleteChecklist(){
        val headerCheck = uiState.value.fullChecklist?.header != checkStringUtil.checkString(uiState.value.header)
        val checklistCheck = uiState.value.fullChecklist?.checkList != temporaryChecklist
        viewModelScope.launch{
            when {
                uiState.value.header.isEmpty() && temporaryChecklist.isEmpty() -> {
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
                headerCheck || checklistCheck -> {
                    primaryView.notesRepositoryImp.editNote(
                        Note(
                            uiState.value.uid,
                            checkStringUtil.checkString(uiState.value.header),
                            null,
                            DateUtils().current,
                            ArrayList(temporaryChecklist),
                            uiState.value.category
                        )
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveChecklistEdit(){
        val headerCheck = uiState.value.fullChecklist?.header != checkStringUtil.checkString(uiState.value.header)
        val checklistCheck = uiState.value.fullChecklist?.checkList != temporaryChecklist
        if(headerCheck || checklistCheck) {
            viewModelScope.launch {
                primaryView.notesRepositoryImp.editNote(
                    Note(
                        uiState.value.uid,
                        checkStringUtil.checkString(uiState.value.header),
                        null,
                        DateUtils().current,
                        ArrayList(temporaryChecklist),
                        uiState.value.category
                    )
                )
            }
        }
    }

    fun navigateToChecklist(note: Note) {
        viewModelScope.launch {
            checklistChecker(note)
            toDoList(note.checkList)
            header(checkStringUtil.replaceNull(note.header))
            uid(note.uid)
            category(note.category)
        }
    }

    private fun category(category:String?) {
        _uiState.update { currentState -> currentState.copy(category = category) }
    }

    fun addChecklistEntry() {
        viewModelScope.launch{
            if(checklistEntry.text.isNotEmpty())  {
                temporaryChecklist.add(
                    CheckList(
                        checklistEntry.text,
                        0,
                        keyGen.numGen(temporaryChecklist)!!
                    )
                )
                checklistEntry = TextFieldValue("")
            }
        }
    }

    fun shareChecklist(): Intent {
        val listEntries = checklistUncheckedUpdater().joinToString { "\n○ ${it.note}" }.replace(",","")
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${uiState.value.header}\n$listEntries")
                type = "text/plain"
            },
            null
        )
    }

    fun entryEditOrAdd(entry: String, location: Int, strike: Int, key: Int, deletable:Boolean) {
        viewModelScope.launch{
            try {
                if(entry.isEmpty()){
                    if (deletable){ temporaryChecklist.removeAt(location) }
                } else {
                    temporaryChecklist[location] = CheckList(entry, strike, key)
                }
            } catch (_:IndexOutOfBoundsException) {
                Log.ERROR
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun returnAndSaveChecklist(navController: NavController){
        completedNote(false)
        reArrange(false)
        viewModelScope.launch {
            editOrDeleteChecklist()
            update(TextFieldValue(""))
            delay(100)
            navController.navigate(route = PageNav.AllNotes.name)
        }
    }
}