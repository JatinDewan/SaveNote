package note.notes.savenote.Composables

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import note.notes.savenote.Composables.Components.TopNavigationNote
import note.notes.savenote.Utils.Keyboard
import note.notes.savenote.Utils.observeAsState
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ui.theme.UniversalFamily

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteComposer(
    navController: NavController,
    notesView: NotesViewModel,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    coroutineScope: CoroutineScope,
    context : Context,
    keyboard: State<Keyboard>
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val myUiState by notesView.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateLifecycle by lifecycleOwner.lifecycle.observeAsState()
    var keyboardRefocusState:Keyboard? by rememberSaveable { mutableStateOf(null) }

    if(keyboard.value == Keyboard.Closed) {
        LaunchedEffect(Unit) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(stateLifecycle) {
        if(notesView.noteEntry.text.isNotEmpty()) {
            if(stateLifecycle == Lifecycle.Event.ON_RESUME) {
                if(keyboardRefocusState == Keyboard.Opened){
                    focusRequester.requestFocus()
                }
            }
        }

        if(stateLifecycle == Lifecycle.Event.ON_PAUSE) {
            keyboardRefocusState = keyboard.value
            notesView.saveNoteEdit()
        }
    }

    Scaffold(
        topBar = {
            TopNavigationNote(
                backButton = {
                    focusManager.clearFocus()
                    notesView.returnAndSaveNote(navController)
                },
                share = { context.startActivity(notesView.shareNote()) },
                showHeader = false
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(),
                onTextLayout = { coroutineScope.launch { bringIntoViewRequester.bringIntoView() } },
                cursorBrush =
                Brush.horizontalGradient(
                    0f to MaterialTheme.colors.primary,
                    1f to MaterialTheme.colors.primary
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onSecondary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = UniversalFamily,
                ),
                value = myUiState.header,
                onValueChange = { notesView.header(it) },
                decorationBox = { innerTextField ->
                    if (myUiState.header.isEmpty()) {
                        Text(
                            text = stringResource(note.notes.savenote.R.string.title),
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = UniversalFamily,
                        )
                    }
                    innerTextField()
                }

            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = MaterialTheme.colors.secondary)

            Spacer(modifier = Modifier.height(10.dp))

            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(bottom = 10.dp),
                cursorBrush =
                Brush.horizontalGradient(
                    0f to MaterialTheme.colors.primary,
                    1f to MaterialTheme.colors.primary
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colors.surface,
                    fontSize = 15.sp,
                    fontFamily = UniversalFamily
                ),
                value = notesView.noteEntry,
                onValueChange = { notesView.update(it) },
                decorationBox = { innerTextField ->
                    AnimatedVisibility(
                        visible = notesView.noteEntry.text.isEmpty(),
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(100))
                    ) {
                        Text(
                            text = stringResource(note.notes.savenote.R.string.note),
                            color = MaterialTheme.colors.onSurface,
                            fontFamily = UniversalFamily,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
        BackHandler(onBack = { notesView.returnAndSaveNote(navController) })
    }
}