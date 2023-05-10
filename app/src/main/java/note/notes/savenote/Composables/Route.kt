package note.notes.savenote.Composables

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.delay
import note.notes.savenote.Utils.ColourUtil
import note.notes.savenote.Utils.DateUtils
import note.notes.savenote.Utils.keyboardAsState
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ViewModelClasses.PrimaryViewModel

enum class PageNav {
    AddNote,
    AllNotes,
    AddChecklist
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MainComposable(
    primaryViewModel: PrimaryViewModel = viewModel(factory = PrimaryViewModel.Factory),
    checklistViewModel: ChecklistViewModel = viewModel(initializer = { ChecklistViewModel(primaryViewModel, SavedStateHandle()) }),
    notesViewModel: NotesViewModel = viewModel(initializer = { NotesViewModel(primaryViewModel,
        SavedStateHandle()
    ) }),
    loaded:() -> Unit
) {
    val primaryUiState by primaryViewModel.uiState.collectAsState()
    val navController = rememberAnimatedNavController()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val colorUtil = ColourUtil()
    val scope = rememberCoroutineScope()
    val focusRequester = FocusRequester()
    val keyboard = keyboardAsState()
    val date = DateUtils()
    val context = LocalContext.current

    LaunchedEffect(primaryViewModel.isReady){
        primaryViewModel.deleteAllEmpty()
        delay(1000)
        if(primaryViewModel.isReady) {
            loaded()
        }
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = PageNav.AllNotes.name
    ) {
        composable(
            route = PageNav.AllNotes.name,
            enterTransition = {
                when (initialState.destination.route) {
                    PageNav.AddNote.name -> fadeIn(tween(300))
                    PageNav.AddChecklist.name -> fadeIn(tween(300))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    PageNav.AddNote.name -> fadeOut(tween(0))
                    PageNav.AddChecklist.name -> fadeOut(tween(0))
                    else -> null
                }
            }
        ) {
            BackHandler(true) {}
            AllNotesView(
                navController = navController,
                primaryViewModel = primaryViewModel,
                notesViewModel = notesViewModel,
                colour = colorUtil,
                allEntries = primaryUiState.allEntries.sortedWith(primaryViewModel.compareLastEdit()),
                favoriteEntries = primaryUiState.favoriteEntries,
                date = date,
                checklistView = checklistViewModel,
                keyboardController = keyboardController!!,
                focusRequester = focusRequester,
                context = context,
                focusManager = focusManager,
                navigateNewNote = {
                    notesViewModel.clearNote()
                    notesViewModel.createBlankNote()
                    navController.navigate(route = PageNav.AddNote.name)
                },
                navigateNewChecklist = {
                    checklistViewModel.clearValuesChecklist()
                    checklistViewModel.createBlankChecklist()
                    navController.navigate(route = PageNav.AddChecklist.name)
                }
            )

        }

        composable(
            route = PageNav.AddNote.name,
            enterTransition = {
                when (initialState.destination.route) {
                    PageNav.AllNotes.name ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(300,0))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    PageNav.AllNotes.name ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(300,0))
                    else -> null
                }
            }
        ) {
            if (keyboardController != null) {
                NoteComposer(
                    notesView = notesViewModel,
                    navController = navController,
                    focusRequester = focusRequester,
                    coroutineScope = scope,
                    focusManager = focusManager,
                    context = context,
                    keyboard = keyboard
                )
            }
        }
        composable(
            route = PageNav.AddChecklist.name,
            enterTransition = {
                when (initialState.destination.route) {
                    PageNav.AllNotes.name ->
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(300,0))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    PageNav.AllNotes.name ->
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(300,0))
                    else -> null
                }
            }
        ) {
            if (keyboardController != null) {
                ChecklistComposer(
                    navController = navController,
                    coroutineScope = scope,
                    colour = colorUtil,
                    focusManager = focusManager,
                    checklistViewModel = checklistViewModel,
                    focusRequester = focusRequester,
                    keyboard = keyboard,
                    context = context
                )
            }
        }
    }
}