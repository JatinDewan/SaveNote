package note.notes.savenote.Composables

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import note.notes.savenote.Composables.Components.*
import note.notes.savenote.Database.Note
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ViewModelClasses.NotesViewModel
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import note.notes.savenote.ui.theme.UniversalFamily
import note.notes.savenote.R
import note.notes.savenote.Utils.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyStaggeredGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }

    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun AllNotesView(
    navController: NavController,
    primaryViewModel: PrimaryViewModel,
    notesViewModel: NotesViewModel,
    colour: ColourUtil,
    allEntries: List<Note>,
    favoriteEntries: List<Note>,
    date: DateUtils,
    checklistView: ChecklistViewModel,
    keyboardController: SoftwareKeyboardController,
    navigateNewNote:() -> Unit,
    navigateNewChecklist:() -> Unit,
    focusRequester: FocusRequester,
    context: Context,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {

    val primaryView by primaryViewModel.uiState.collectAsState()
    val gridState = rememberForeverLazyListState(key = "grid")
    val haptic = LocalHapticFeedback.current
    val scaffoldState = rememberScaffoldState()
    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
            uri -> primaryViewModel.backUpNotes(uri,context)
    }
    val restoreBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            uri -> primaryViewModel.restoreNotes(uri,context)
    }

    LaunchedEffect(gridState.isScrollInProgress) {
        if (gridState.isScrollInProgress) { primaryViewModel.collapse(false) }
    }


    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = primaryViewModel.temporaryEntryHold.isNotEmpty(),
                    enter = slideInVertically(tween(200)) { fullHeight -> fullHeight } + fadeIn(
                        tween(200)
                    ),
                    exit = slideOutVertically(tween(200)) { fullHeight -> fullHeight } + fadeOut(
                        tween(200)
                    )
                ) {
                    BottomPopUpBar(
                        primaryViewModel = primaryViewModel,
                        colour = colour
                    )
                }
            }

            AnimatedVisibility(
                visible = !primaryView.showSearchBar && !primaryView.dropDown,
                enter = slideInVertically(tween(delayMillis = 0)) { fullHeight -> fullHeight },
                exit = slideOutVertically(tween(delayMillis = 0)) { fullHeight -> fullHeight }
            ){
                ExpandableButton(
                    dismiss = primaryView.collapseButton,
                    expand = { primaryViewModel.collapse(true) },
                    expandedIsFalse = { primaryViewModel.collapse(false) },
                    colour = colour,
                    navigateNewChecklist = {
                        navigateNewChecklist()
                        primaryViewModel.collapse(false)
                    },
                    navigateNewNote = {
                        navigateNewNote()
                        primaryViewModel.collapse(false)
                    }
                )
            }

            BackupAndRestore(
                isVisible = primaryView.showBackup,
                backUp = { createBackup.launch("SaveNote_DB") },
                restore = { restoreBackup.launch(arrayOf("text/plain")) },
                dismiss = { primaryViewModel.showBackup(false) }
            )

            ConfirmDelete(
                popUp = primaryView.confirmDelete,
                cancel = { primaryViewModel.confirmDelete(false) },
                confirmDelete = { primaryViewModel.deleteSelected() },
                confirmMessage = stringResource(id = R.string.confirmDelete,"${primaryViewModel.temporaryEntryHold.size}")
            )
        }
    ){ padding ->

        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            AnimatedVisibility(
                visible = !primaryView.showSearchBar,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400))
            ){
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = { primaryViewModel.collapse(false) })
                        }
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    columns = StaggeredGridCells.Fixed(primaryViewModel.changeViewCards()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(
                        top = 45.dp,
                        bottom = 55.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    verticalItemSpacing = 10.dp,
                ) {

                    item {
                        AnimatedVisibility(visible = primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        AnimatedVisibility(visible = !primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(0.dp))
                        }
                    }

                    item {
                        AnimatedVisibility(visible = primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        AnimatedVisibility(visible = !primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(0.dp))
                        }
                    }

                    items(
                        items = favoriteEntries,
                        key = { it.uid!! }
                    ) { favoritesEntries ->
                        EntryCards(
                            primaryViewModel = primaryViewModel,
                            colour = colour,
                            onEditClick = {
                                primaryViewModel.cardFunctionSelection(
                                    boolean = favoritesEntries.checkList.isNullOrEmpty(),
                                    returnOperationOne = {
                                        primaryViewModel.cardFunctionSelection(
                                            boolean = primaryViewModel.temporaryEntryHold.isEmpty(),
                                            returnOperationOne = {
                                                notesViewModel.navigateToNote(favoritesEntries)
                                                navController.navigate(route = PageNav.AddNote.name)
                                            },
                                            returnOperationTwo = {
                                                primaryViewModel.deleteTally(
                                                    favoritesEntries
                                                )
                                            }
                                        )
                                    },
                                    returnOperationTwo = {
                                        primaryViewModel.cardFunctionSelection(
                                            boolean = primaryViewModel.temporaryEntryHold.isEmpty(),
                                            returnOperationOne = {
                                                checklistView.navigateToChecklist(
                                                    favoritesEntries
                                                )
                                                navController.navigate(route = PageNav.AddChecklist.name)
                                            },
                                            returnOperationTwo = {
                                                primaryViewModel.deleteTally(
                                                    favoritesEntries
                                                )
                                            }
                                        )
                                    }
                                )
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                primaryViewModel.deleteTally(favoritesEntries)
                            },
                            note = favoritesEntries,
                            date = date
                        )
                    }

                    items(
                        items = allEntries,
                        key = { it.uid!! }
                    ) { allEntries ->
                        /*keep animation on just one (this one for example) as update on favourites
                        * does not stay otherwise*/
                        AnimatedVisibility(visible = allEntries.category == null) {
                            EntryCards(
                                primaryViewModel = primaryViewModel,
                                colour = colour,
                                onEditClick = {
                                    primaryViewModel.cardFunctionSelection(
                                        boolean = !allEntries.checkList.isNullOrEmpty(),
                                        returnOperationTwo = {
                                            primaryViewModel.cardFunctionSelection(
                                                boolean = primaryViewModel.temporaryEntryHold.isEmpty(),
                                                returnOperationOne = {
                                                    notesViewModel.navigateToNote(allEntries)
                                                    navController.navigate(route = PageNav.AddNote.name)
                                                },
                                                returnOperationTwo = {
                                                    primaryViewModel.deleteTally(allEntries)
                                                }
                                            )
                                        },
                                        returnOperationOne = {
                                            primaryViewModel.cardFunctionSelection(
                                                boolean = primaryViewModel.temporaryEntryHold.isEmpty(),
                                                returnOperationOne = {
                                                    checklistView.navigateToChecklist(allEntries)
                                                    navController.navigate(route = PageNav.AddChecklist.name)
                                                },
                                                returnOperationTwo = {
                                                    primaryViewModel.deleteTally(allEntries)
                                                }
                                            )
                                        }
                                    )
                                },
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    primaryViewModel.deleteTally(allEntries)
                                },
                                note = allEntries,
                                date = date
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = primaryView.showSearchBar && primaryView.searchEntries.isNotEmpty(),
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400))
            ){
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    primaryViewModel.collapse(false)
                                }
                            )
                        }
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    columns = StaggeredGridCells.Fixed(primaryViewModel.changeViewCards()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(
                        top = 45.dp,
                        bottom = 55.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                    verticalItemSpacing = 10.dp,
                ) {

                    item {
                        AnimatedVisibility(visible = primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        AnimatedVisibility(visible = !primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(0.dp))
                        }
                    }

                    item {
                        AnimatedVisibility(visible = primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        AnimatedVisibility(visible = !primaryView.currentPage) {
                            Spacer(modifier = Modifier.height(0.dp))
                        }
                    }

                    items(
                        items = primaryView.searchEntries,
                        key = { it.uid!! }
                    ) { searchResults ->
                        EntryCardsSearch(
                            primaryViewModel = primaryViewModel,
                            colour = colour,
                            onEditClick = {
                                primaryViewModel.cardFunctionSelection(
                                    boolean = searchResults.checkList.isNullOrEmpty(),
                                    returnOperationTwo = {
                                        primaryViewModel.cardFunctionSelection(
                                            boolean = primaryViewModel.temporaryEntryHold.isEmpty(),
                                            returnOperationOne = {
                                                notesViewModel.navigateToNote(searchResults)
                                                navController.navigate(route = PageNav.AddNote.name)
                                            },
                                            returnOperationTwo = {
                                                primaryViewModel.deleteTally(searchResults)
                                            }
                                        )
                                    },
                                    returnOperationOne = {
                                        primaryViewModel.cardFunctionSelection(
                                            boolean = primaryViewModel.temporaryEntryHold.isEmpty(),
                                            returnOperationOne = {
                                                checklistView.navigateToChecklist(searchResults)
                                                navController.navigate(route = PageNav.AddChecklist.name)
                                            },
                                            returnOperationTwo = {
                                                primaryViewModel.deleteTally(searchResults)
                                            }
                                        )
                                    }
                                )
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                primaryViewModel.deleteTally(searchResults)
                            },
                            note = searchResults,
                            date = date
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = primaryView.showSearchBar &&
                    primaryView.searchEntries.isEmpty()&&
                    primaryView.searchQuery.isNotEmpty(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(50))
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = 10.dp,
                        vertical = 150.dp
                    )
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(
                    tint = MaterialTheme.colors.onSecondary,
                    imageVector = ImageVector.vectorResource(id = R.drawable.search_lg),
                    contentDescription = stringResource(R.string.check),
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(40.dp)
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = stringResource(id = R.string.matching),
                    fontSize = 20.sp,
                    fontFamily = UniversalFamily,
                    color = MaterialTheme.colors.onSecondary,
                )
            }
        }

        MoreOptionsMain(
            dismiss = primaryView.dropDown,
            backUp = {
                primaryViewModel.dropDown(false)
                primaryViewModel.showSortBy(false)
                primaryViewModel.showBackup(true)
            },
            rateApp = { TODO() },
            expandedIsFalse = { primaryViewModel.dropDown(false) },
            help = { primaryViewModel.help(context) },
            primaryViewModel = primaryViewModel
        )

        AnimatedVisibility(
            visible = gridState.isScrollingUp() ||
                    primaryView.showSearchBar ||
                    gridState.firstVisibleItemIndex <= 1 ||
                    primaryView.dropDown,
            enter = slideInVertically(tween(200,200))
                    + fadeIn(tween(200,200)),
            exit = slideOutVertically(tween(200, 2000))
                    + fadeOut(tween(200, 2000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 5.dp,
                        start = 10.dp,
                        end = 10.dp
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                TopNavigationBarHome(
                    startedScrolling = gridState.firstVisibleItemIndex > 1,
                    colour = colour,
                    primaryViewModel = primaryViewModel,
                    focusRequester = focusRequester,
                    header = stringResource(id = R.string.app_name),
                    search = {
                        primaryViewModel.requestFocus(focusRequester, focusManager)
                        primaryViewModel.clearSearch(primaryView.showSearchBar)
                        if(!primaryView.showSearchBar) primaryViewModel.showSearch.clear()
                    },
                    moreOptions = {
                        primaryViewModel.dropDown(!primaryView.dropDown)
                    }
                )
            }
        }

        BackHandler(
            onBack = {
                primaryViewModel.clearSearch(primaryView.showSearchBar)
                keyboardController.hide()
                primaryViewModel.dropDown(false)
                primaryViewModel.collapse(false)
                primaryViewModel.showBackup(false)
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@ExperimentalFoundationApi
@SuppressLint("NewApi")
@Composable
fun EntryCards(
    primaryViewModel: PrimaryViewModel,
    onEditClick: () -> Unit,
    onLongPress: () -> Unit,
    colour: ColourUtil,
    note: Note,
    date: DateUtils,
    modifier: Modifier = Modifier
) {
    val selected = primaryViewModel.temporaryEntryHold.contains(note)

    val backgroundColour: Color by animateColorAsState(
        animationSpec = tween(150),
        targetValue =
        colour.colourSelection(
            selected,
            MaterialTheme.colors.secondaryVariant,
            MaterialTheme.colors.secondary
        )
    )

    val dateColour: Color by animateColorAsState(
        animationSpec = tween(150),
        targetValue =
        colour.colourSelection(
            selected,
            MaterialTheme.colors.background,
            MaterialTheme.colors.onSurface
        )
    )

    Card(
        backgroundColor = backgroundColour,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { onEditClick() },
                    onLongClick = { onLongPress() }
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                note.header?.let {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colors.primaryVariant,
                        fontFamily = UniversalFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                if (note.checkList.isNullOrEmpty()) {
                    note.note?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colors.onSecondary,
                            fontFamily = UniversalFamily,
                            maxLines = 20,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        note.checkList.rangeFinder(10).forEach { checklistEntries ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    tint = MaterialTheme.colors.primary.copy(0.85f),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.circle),
                                    contentDescription = stringResource(R.string.check),
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(12.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = checklistEntries.note,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colors.onSecondary,
                                    fontFamily = UniversalFamily,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colors.onBackground)

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(
                            horizontal = 15.dp,
                            vertical = 5.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    note.date?.let {
                        Text(
                            text = date.dateTimeDisplay(it),
                            fontSize = 10.sp,
                            color = dateColour,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = UniversalFamily,
                            maxLines = 1,
                        )
                    }

                    AnimatedVisibility(
                        visible = note.category != null,
                        enter = scaleIn(),
                        exit = scaleOut()
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.pin_01),
                            contentDescription = stringResource(R.string.check),
                            tint = MaterialTheme.colors.primary.copy(0.8f)
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@ExperimentalFoundationApi
@SuppressLint("NewApi")
@Composable
fun EntryCardsSearch(
    primaryViewModel: PrimaryViewModel,
    onEditClick: () -> Unit,
    onLongPress: () -> Unit,
    colour: ColourUtil,
    note: Note,
    date: DateUtils,
    modifier: Modifier = Modifier
) {
    val selected = primaryViewModel.temporaryEntryHold.contains(note)
    val primaryView by primaryViewModel.uiState.collectAsState()

    val backgroundColour: Color by animateColorAsState(
        animationSpec = tween(150),
        targetValue =
        colour.colourSelection(
            selected,
            MaterialTheme.colors.secondaryVariant,
            MaterialTheme.colors.secondary
        )
    )

    val dateColour: Color by animateColorAsState(
        animationSpec = tween(150),
        targetValue =
        colour.colourSelection(
            selected,
            MaterialTheme.colors.background,
            MaterialTheme.colors.onSurface
        )
    )

    Card(
        backgroundColor = backgroundColour,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { onEditClick() },
                    onLongClick = { onLongPress() }
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                note.header?.let {
                    HighlightedText(
                        text = it,
                        selectedString = primaryView.searchQuery,
                        maxLines = 3,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                if (note.checkList.isNullOrEmpty()) {
                    note.note?.let {
                        HighlightedText(
                            text = it,
                            selectedString = primaryView.searchQuery,
                            maxLines = 20,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        note.checkList.rangeFinder(10).forEach { checklistEntries ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    tint = MaterialTheme.colors.primary.copy(0.85f),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.circle),
                                    contentDescription = stringResource(R.string.check),
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(12.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                HighlightedText(
                                    text = checklistEntries.note,
                                    selectedString = primaryView.searchQuery,
                                    maxLines = 3,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = MaterialTheme.colors.onBackground)

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(
                            horizontal = 15.dp,
                            vertical = 5.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    note.date?.let {
                        Text(
                            text = date.dateTimeDisplay(it),
                            fontSize = 10.sp,
                            color = dateColour,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = UniversalFamily,
                            maxLines = 1,
                        )
                    }

                    AnimatedVisibility(
                        visible = note.category != null,
                        enter = scaleIn(),
                        exit = scaleOut()
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.pin_01),
                            contentDescription = stringResource(R.string.check),
                            tint = MaterialTheme.colors.primary.copy(0.8f)
                        )
                    }
                }
            }
        }
    }
}