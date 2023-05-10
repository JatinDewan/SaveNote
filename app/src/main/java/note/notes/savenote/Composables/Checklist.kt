package note.notes.savenote.Composables

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import note.notes.savenote.Composables.Components.MoreOptionsChecklist
import note.notes.savenote.Composables.Components.TopNavigationChecklist
import note.notes.savenote.Database.CheckList
import note.notes.savenote.Utils.*
import note.notes.savenote.ViewModelClasses.ChecklistViewModel
import note.notes.savenote.ui.theme.UniversalFamily
import note.notes.savenote.R
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistComposer(
    checklistViewModel: ChecklistViewModel,
    colour: ColourUtil,
    navController: NavController,
    coroutineScope: CoroutineScope,
    focusManager: FocusManager,
    focusRequester: FocusRequester,
    keyboard: State<Keyboard>,
    context: Context
) {

    val checklistUiState by checklistViewModel.uiState.collectAsState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateLifecycle by lifecycleOwner.lifecycle.observeAsState()

    val state = rememberReorderableLazyListState(
        canDragOver = { from, _ -> checklistViewModel.dragRestriction(from.index) },
        onMove = { from, to -> checklistViewModel.onMoveIndexer(from.key, to.key) }
    )
    val showButton by remember { derivedStateOf { state.listState.firstVisibleItemIndex > 1 } }

    if(keyboard.value == Keyboard.Closed) {
        LaunchedEffect(!checklistUiState.reArrange) {
            focusManager.clearFocus()
            checklistViewModel.clearChecklistEdit()
        }
    }

    LaunchedEffect(stateLifecycle) {
        if(checklistViewModel.checklistEntry.text.isNotEmpty()) {
            if (stateLifecycle == Lifecycle.Event.ON_RESUME) {
                focusRequester.requestFocus()
            }
        }

        if (stateLifecycle == Lifecycle.Event.ON_PAUSE) {
            checklistViewModel.saveChecklistEdit()
        }
    }

    BackHandler(
        onBack = { checklistViewModel.onBackPress(navController) }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopNavigationChecklist(
                backButton = {
                    checklistViewModel.pullUp(false)
                    focusManager.clearFocus()
                    checklistViewModel.returnAndSaveChecklist(navController)
                },
                moreOptions = {
                    focusManager.clearFocus()
                    coroutineScope.launch { checklistViewModel.pullUp(!checklistUiState.isVisible) }
                },
                header = checklistUiState.header,
                showHeader = showButton,
                colour = colour,
                moreOptionsOpened = checklistUiState.isVisible
            )
        }
    ) { padding ->
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            BoxWithConstraints{
                LazyColumn(
                    state = state.listState,
                    horizontalAlignment = Alignment.Start,
                    contentPadding = PaddingValues(vertical = 5.dp),
                    flingBehavior = maxScrollFlingBehavior(),
                    modifier = Modifier
                        .padding(padding)
                        .background(MaterialTheme.colors.background)
                        .reorderable(state)
                ) {
                    stickyHeader {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    coroutineScope.launch {
                                        checklistViewModel.addChecklistEntry()
                                        delay(50)
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            ),
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
                            value = checklistUiState.header,
                            onValueChange = { checklistViewModel.header(it) },
                            decorationBox = { innerTextField ->
                                if (checklistUiState.header.isEmpty()) {
                                    Text(
                                        text = stringResource(note.notes.savenote.R.string.title),
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = UniversalFamily
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    stickyHeader { }

                    items(
                        items = checklistViewModel.checklistUncheckedUpdater(),
                        key = { notes -> notes.key },
                    ) { items ->
                        Spacer(modifier = Modifier.height(6.dp))
                        org.burnoutcrew.reorderable.ReorderableItem(state, key = items.key) { isDragging ->
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                            ){
                                CheckList(
                                    checkList = CheckList(
                                        items.note,
                                        items.strike,
                                        items.key
                                    ),
                                    editEntry = checklistUiState.checklistKey == items.key,
                                    checklistViewModel = checklistViewModel,
                                    isDragging = isDragging,
                                    colour = colour,
                                    modifier = Modifier.detectReorder(state),
                                    reArrange = checklistUiState.reArrange,
                                    coroutineScope = coroutineScope,
                                    focusManager = focusManager
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    item(key = 4759084375890743) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .bringIntoViewRequester(bringIntoViewRequester)
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                        ) {
                            IconButton(
                                enabled = false,
                                interactionSource = remember { MutableInteractionSource() },
                                modifier = Modifier
                                    .align(Alignment.Top)
                                    .size(30.dp),
                                onClick = { }
                            ) {
                                Icon(
                                    tint = MaterialTheme.colors.primaryVariant,
                                    imageVector = ImageVector.vectorResource(id = note.notes.savenote.R.drawable.plus),
                                    contentDescription = stringResource(note.notes.savenote.R.string.check),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            BasicTextField(
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        coroutineScope.launch {
                                            checklistViewModel.addChecklistEntry()
                                            delay(50)
                                            bringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                ),
                                maxLines = 15,
                                onTextLayout = {
                                    coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                                },
                                cursorBrush = Brush.horizontalGradient(
                                    0f to MaterialTheme.colors.primary,
                                    1f to MaterialTheme.colors.primary
                                ),
                                modifier = Modifier
                                    .padding(5.dp)
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (it.isFocused)
                                            checklistViewModel.editChecklistEntry(0)
                                    },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colors.surface,
                                    fontSize = 15.sp,
                                    fontFamily = UniversalFamily
                                ),
                                value = checklistViewModel.checklistEntry,
                                onValueChange = { checklistViewModel.update(it) },
                                decorationBox = { innerTextField ->
                                    AnimatedVisibility(
                                        visible = checklistViewModel.checklistEntry.text.isEmpty(),
                                        enter = fadeIn(tween(100)),
                                        exit = fadeOut(tween(100))
                                    ) {
                                        Text(
                                            text = stringResource(note.notes.savenote.R.string.addEntry),
                                            color = MaterialTheme.colors.onSurface,
                                            fontFamily = UniversalFamily,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    items(
                        items = checklistViewModel.checklistCheckedUpdater(),
                        key = { notes -> notes.key }
                    ) { items ->
                        if(checklistUiState.showCompleted){
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                            ) {
                                CheckListCompleted(
                                    checkList = CheckList(
                                        items.note,
                                        items.strike,
                                        items.key
                                    ),
                                    checklistViewModel = checklistViewModel,
                                    collapseList = {
                                        checklistViewModel.completedNote(
                                            false
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(10.dp)) }
                }

                MoreOptionsChecklist(
                    dismiss = checklistUiState.isVisible,
                    share = { context.startActivity(checklistViewModel.shareChecklist()) },
                    reArrange = {
                        checklistViewModel.reArrange(!checklistUiState.reArrange)
                        checklistViewModel.pullUp(false)
                    },
                    unCheckCompleted = {
                        checklistViewModel.unCheckCompleted()
                        checklistViewModel.pullUp(false)
                    },
                    confirmDelete = {
                        checklistViewModel.confirmDeleteAllChecked()
                        checklistViewModel.pullUp(false)
                    },
                    expandedIsFalse = { checklistViewModel.pullUp(false) },
                    showCompleted = {
                        checklistViewModel.updateShowCompleted(!checklistUiState.showCompleted)
                    },
                    colour = colour,
                    showCompletedBoolean = checklistUiState.showCompleted,
                    reArrangeBoolean = checklistUiState.reArrange
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun CheckList(
    checkList: CheckList,
    editEntry: Boolean,
    isDragging: Boolean,
    checklistViewModel: ChecklistViewModel,
    colour: ColourUtil,
    reArrange: Boolean,
    coroutineScope: CoroutineScope,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
){
    val myUiState by checklistViewModel.uiState.collectAsState()
    var updateEntry by rememberSaveable { mutableStateOf(checkList.note) }
    val localBringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }

    val entryBackground: Color by animateColorAsState(
        animationSpec = tween(150),
        targetValue =
        colour.colourSelection(
            editEntry || isDragging,
            MaterialTheme.colors.secondary,
            MaterialTheme.colors.background
        )
    )

    AnimatedContent(
        targetState = isDragging,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) with
                    fadeOut(animationSpec = tween(200))
        }
    ) {target ->
        Card(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = entryBackground,
            elevation = 0.dp
        ) {
            Row {
                AnimatedVisibility(
                    visible = reArrange,
                    content = {
                        IconButton(
                            enabled = !editEntry,
                            modifier = modifier
                                .align(Alignment.Top)
                                .size(30.dp),
                            onClick = {},
                        ) {
                            Icon(
                                tint = MaterialTheme.colors.onSurface,
                                imageVector = Icons.Outlined.DragIndicator,
                                contentDescription = stringResource(R.string.check),
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.Top)
                            .size(30.dp),
                        onClick = { checklistViewModel.deleteOrComplete(checkList) },
                    ) {
                        Icon(
                            tint = MaterialTheme.colors.primary.copy(0.85f),
                            imageVector = ImageVector.vectorResource(
                                id = checklistViewModel.iconSelectionTest(
                                    iconSelectionOne = target,
                                    iconSelectionTwo = myUiState.checklistKey == checkList.key
                                )
                            ),
                            contentDescription = stringResource(R.string.check),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    checklistViewModel.editChecklistEntry(checkList.key)
                                }
                                if (!it.isFocused) {
                                    if (updateEntry.isEmpty()) {
                                        checklistViewModel.entryEditOrAdd(
                                            entry = updateEntry,
                                            strike = 0,
                                            location = checklistViewModel.temporaryChecklist.indexOf(
                                                checkList
                                            ),
                                            key = checkList.key,
                                            deletable = true
                                        )
                                    }
                                }
                            },
                        cursorBrush =
                        Brush.horizontalGradient(
                            0f to MaterialTheme.colors.primary,
                            1f to MaterialTheme.colors.primary
                        ),
                        textStyle = TextStyle(
                            color = MaterialTheme.colors.surface,
                            fontSize = 15.sp,
                            fontFamily = UniversalFamily,
                        ),
                        value = updateEntry,
                        onValueChange = {
                            updateEntry = it
                            checklistViewModel.entryEditOrAdd(
                                entry = updateEntry,
                                strike = 0,
                                location = checklistViewModel.temporaryChecklist.indexOf(checkList),
                                key = checkList.key,
                                deletable = false
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                checklistViewModel.clearChecklistEdit()
                                checklistViewModel.entryEditOrAdd(
                                    entry = updateEntry,
                                    strike = 0,
                                    location =
                                    checklistViewModel.temporaryChecklist.indexOf(checkList),
                                    key = checkList.key,
                                    deletable = true
                                )
                            }
                        ),
                        onTextLayout = {
                            checklistViewModel.visibleCheck(
                                isVisible = myUiState.checklistKey == checkList.key,
                                function = {
                                    coroutineScope.launch{
                                        delay(200)
                                        localBringIntoViewRequester.bringIntoView()
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CheckListCompleted(
    checkList: CheckList,
    checklistViewModel: ChecklistViewModel,
    collapseList: () -> Unit,
    modifier: Modifier = Modifier,
){
    val size = SizeUtils()
    val firstIndex = checklistViewModel.checklistCheckedUpdater().indexOf(checkList) == 0
    val lastIndex = checklistViewModel.checklistCheckedUpdater().indexOf(checkList) ==
            checklistViewModel.checklistCheckedUpdater().lastIndex

    Card(
        backgroundColor = MaterialTheme.colors.secondary,
        shape = RoundedCornerShape(
            topStart = size.DPISelection(firstIndex, 15.dp, 0.dp ),
            topEnd = size.DPISelection(firstIndex, 15.dp, 0.dp ),
            bottomStart = size.DPISelection(lastIndex, 15.dp, 0.dp ),
            bottomEnd = size.DPISelection(lastIndex, 15.dp, 0.dp )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    start = 5.dp,
                    end = 5.dp,
                    top = if(firstIndex) 7.dp else 5.dp,
                    bottom =  if(lastIndex) 7.dp else 5.dp

                ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .size(30.dp),
                    onClick = {
                        checklistViewModel.entryEditOrAdd(
                            entry = checkList.note,
                            strike = 0,
                            location = checklistViewModel.temporaryChecklist.indexOf(
                                checkList
                            ),
                            key = checkList.key,
                            deletable = false
                        )
                        checklistViewModel.visibleCheck(
                            isVisible = checklistViewModel.checklistCheckedUpdater().isEmpty(),
                            function = { collapseList() }
                        )
                    },
                ) {
                    Icon(
                        tint = MaterialTheme.colors.primary.copy(0.85f),
                        imageVector = ImageVector.vectorResource(id = R.drawable.check_circle),
                        contentDescription = stringResource(R.string.check),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth(),
                    text = checkList.note,
                    color = MaterialTheme.colors.onSurface,
                    fontFamily = UniversalFamily,
                    fontSize = 15.sp,
                    style = TextStyle(textDecoration = TextDecoration.LineThrough)
                )
            }
            if(!lastIndex) Divider(color = MaterialTheme.colors.background)
        }
    }
}