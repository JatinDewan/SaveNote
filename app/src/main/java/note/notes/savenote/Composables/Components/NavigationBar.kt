package note.notes.savenote.Composables.Components

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import note.notes.savenote.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.Utils.ColourUtil
import note.notes.savenote.ViewModelClasses.PrimaryViewModel
import note.notes.savenote.ui.theme.UniversalFamily

@Composable
fun TopNavigationChecklist(
    backButton: () -> Unit,
    moreOptions: () -> Unit,
    header: String,
    showHeader: Boolean,
    colour: ColourUtil,
    moreOptionsOpened: Boolean
) {

    val iconColour: Color by animateColorAsState(
        animationSpec = tween(300),
        targetValue =
        colour.colourSelection(
            !moreOptionsOpened,
            MaterialTheme.colors.primary,
            MaterialTheme.colors.onSecondary
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopAppBar(
            title = {
                AnimatedVisibility(
                    visible = showHeader,
                    enter = slideInVertically(
                        tween(150, easing = EaseIn),
                        initialOffsetY = {fullHeight -> fullHeight}
                    ) + fadeIn(tween(150, easing = EaseIn)),
                    exit = slideOutVertically(
                        tween(150, easing = EaseOut),
                        targetOffsetY = {fullHeight -> fullHeight}
                    ) + fadeOut(tween(150, easing = EaseOut))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 15.dp)
                    ){
                        Text(
                            text = header,
                            color = MaterialTheme.colors.onSecondary,
                            fontFamily = UniversalFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            backgroundColor = MaterialTheme.colors.background,
            elevation = if(showHeader) 10.dp else 0.dp,
            navigationIcon = {
                IconButton(
                    onClick = { backButton() }
                ) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.arrow_ios_back),
                        contentDescription = stringResource(R.string.backbutton),
                        tint = MaterialTheme.colors.primary
                    )
                }
            },
            actions = {
                Card(
                    modifier = Modifier
                        .height(35.dp)
                        .padding(end = 10.dp),
                    elevation = 5.dp,
                    shape = RoundedCornerShape(20),
                    backgroundColor = MaterialTheme.colors.secondary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.13f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.weight(1f),
                            onClick = { moreOptions() }
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.dots_vertical),
                                contentDescription = stringResource(R.string.deletenote),
                                tint = iconColour
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun TopNavigationNote(
    backButton: () -> Unit,
    share:() -> Unit,
    showHeader: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopAppBar(
            title = {
                AnimatedVisibility(
                    visible = showHeader,
                    enter = slideInVertically(tween(150, easing = EaseIn)) { fullHeight -> fullHeight } + fadeIn(
                        tween(150, easing = EaseIn)
                    ),
                    exit = slideOutVertically(tween(150, easing = EaseOut)) { fullHeight -> fullHeight } + fadeOut(
                        tween(150, easing = EaseOut)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 15.dp)
                    ){
                        Text(
                            text = "",
                            color = MaterialTheme.colors.onSecondary,
                            fontFamily = UniversalFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            backgroundColor = MaterialTheme.colors.background,
            elevation = if(showHeader) 10.dp else 0.dp,
            navigationIcon = {
                IconButton(onClick = { backButton() }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.arrow_ios_back),
                        contentDescription = stringResource(R.string.backbutton),
                        tint = MaterialTheme.colors.primary
                    )
                }
            },
            actions = {
                Card(
                    modifier = Modifier
                        .height(35.dp)
                        .padding(end = 10.dp),
                    elevation = 5.dp,
                    shape = RoundedCornerShape(20),
                    backgroundColor = MaterialTheme.colors.secondary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.13f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            modifier = Modifier.weight(1f),
                            onClick = { share() }
                        ) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = ImageVector.vectorResource(R.drawable.share_03),
                                contentDescription = stringResource(R.string.deletenote),
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
            }
        )
    }
}

@SuppressLint("Recycle")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TopNavigationBarHome(
    startedScrolling: Boolean,
    colour: ColourUtil,
    header: String,
    search:() -> Unit,
    primaryViewModel: PrimaryViewModel,
    focusRequester: FocusRequester,
    moreOptions: () -> Unit

) {
    val primaryView by primaryViewModel._uiState.collectAsState()

    val borderColour: Color by animateColorAsState(
        animationSpec = tween(200),
        targetValue =
        colour.colourSelection(
            !startedScrolling,
            MaterialTheme.colors.background,
            MaterialTheme.colors.onBackground
        )
    )

    val searchBarColour: Color by animateColorAsState(
        animationSpec = tween(50,250),
        targetValue =
        colour.colourSelection(
            !primaryView.showSearchBar,
            MaterialTheme.colors.background,
            MaterialTheme.colors.secondary
        )
    )

    val optionsOpenColor: Color by animateColorAsState(
        animationSpec = tween(300),
        targetValue =
        colour.colourSelection(
            primaryView.dropDown,
            MaterialTheme.colors.onSecondary,
            MaterialTheme.colors.primary
        )
    )

    Card(
        border = BorderStroke(1.dp, borderColour),
        elevation = if(startedScrolling) 10.dp else 0.dp,
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(15)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedVisibility(
                visible = !primaryView.showSearchBar,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150))
            ){
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = header,
                    color = MaterialTheme.colors.onSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = UniversalFamily,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedContent(
                targetState = primaryView.showSearchBar,
                transitionSpec = {
                    slideInHorizontally(tween(200,200)) { fullWidth -> fullWidth } + fadeIn(tween(200,200)) with
                            slideOutHorizontally(tween(200,500)) { fullWidth -> fullWidth } + fadeOut(
                        tween(200,500)
                    )
                }
            ){animationTarget ->
                Card(
                    backgroundColor = searchBarColour,
                    shape = RoundedCornerShape(0.dp),
                    elevation = 0.dp
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            modifier = Modifier.width(35.dp),
                            onClick = { search() }
                        ) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                imageVector = ImageVector.vectorResource(
                                    primaryViewModel.iconSelection(
                                        boolean = animationTarget,
                                        iconOne = R.drawable.x_close,
                                        iconTwo = R.drawable.search_lg
                                    )
                                ),
                                contentDescription = stringResource(R.string.deletenote),
                                tint = MaterialTheme.colors.primary
                            )
                        }
                        if(animationTarget){
                            BasicTextField(
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                singleLine = true,
                                cursorBrush =
                                Brush.horizontalGradient(
                                    0f to MaterialTheme.colors.primary,
                                    1f to MaterialTheme.colors.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.79f)
                                    .focusRequester(focusRequester)
                                    .padding(end = 10.dp),
                                textStyle = TextStyle(
                                    color = MaterialTheme.colors.surface,
                                    fontSize = 16.sp,
                                    fontFamily = UniversalFamily
                                ),
                                value = primaryView.searchQuery,
                                onValueChange = {
                                    primaryViewModel.searchQuery(it)
                                },
                                decorationBox = { innerTextField ->
                                    AnimatedVisibility(
                                        visible = primaryView.searchQuery.isEmpty(),
                                        enter = fadeIn(tween(200)),
                                        exit = fadeOut(tween(200))
                                    ){
                                        Text(
                                            text = stringResource(R.string.searchnotes),
                                            color = MaterialTheme.colors.onSurface,
                                            fontFamily = UniversalFamily
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                        AnimatedVisibility(
                            visible = primaryView.showSearchBar,
                            enter = scaleIn(tween(100,100)),
                            exit = scaleOut(tween(0,0))
                        ) {
                            Divider(modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
            Row(
                modifier = Modifier.wrapContentSize(Alignment.CenterEnd,true)
            ){

                ViewChanger(
                    isVisible = primaryView.currentPage,
                    updateState = { primaryViewModel.updateCurrentPageView(!primaryView.currentPage) },
                )

                IconButton(
                    modifier = Modifier
                        .width(35.dp),
                    onClick = { moreOptions() }
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.dots_vertical),
                        contentDescription = stringResource(R.string.deletenote),
                        tint = optionsOpenColor
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ViewChanger(
    isVisible: Boolean,
    updateState: () -> Unit,
){
    IconButton(
        modifier = Modifier
            .width(35.dp),
        onClick = { updateState() }
    ){
        AnimatedContent(targetState = isVisible) { showIcon ->
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector = ImageVector.vectorResource(
                    if (!showIcon) R.drawable.grid_01 else R.drawable.rows_01
                ),
                contentDescription = stringResource(R.string.backbutton),
                tint = MaterialTheme.colors.primary
            )
        }
    }
}


@Composable
fun BottomPopUpBar(
    primaryViewModel: PrimaryViewModel,
    colour: ColourUtil
) {
    val primaryView by primaryViewModel._uiState.collectAsState()

    val selectAllIndicator: Color by animateColorAsState(
        animationSpec = tween(150),
        targetValue =
        colour.colourSelection(
            primaryViewModel.temporaryEntryHold.containsAll(primaryView.allEntries + primaryView.favoriteEntries),
            MaterialTheme.colors.surface,
            MaterialTheme.colors.onSecondary
        )
    )

    Column {
        Divider(color = MaterialTheme.colors.onBackground)
        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .padding(start = 10.dp)
                .background(MaterialTheme.colors.background),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Card(
                modifier = Modifier.height(38.dp),
                elevation = 0.dp,
                shape = RoundedCornerShape(15),
                backgroundColor = MaterialTheme.colors.secondary,
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = { primaryViewModel.confirmDelete(true) }
                    ) {
                        BoxWithConstraints {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ){
                                Icon(
                                    modifier = Modifier.size(23.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.trash_02),
                                    contentDescription = stringResource(R.string.deletenote),
                                    tint = MaterialTheme.colors.onSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .size(30.dp)
                                    .padding(
                                        bottom = 5.dp,
                                        end = 13.dp
                                    ),
                                contentAlignment = Alignment.BottomEnd
                            ){
                                Card(
                                    modifier = Modifier.size(15.dp),
                                    backgroundColor = MaterialTheme.colors.onError,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()){
                                        Text(
                                            modifier = Modifier.align(Alignment.Center),
                                            text = primaryViewModel.containerSize(),
                                            color = MaterialTheme.colors.onSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = UniversalFamily,
                                            maxLines = 1,
                                            fontSize = 8.sp,
                                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = { primaryViewModel.selectAllNotes() }
                    ) {
                        Icon(
                            modifier = Modifier.size(23.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.check_done_02),
                            contentDescription = stringResource(R.string.deletenote),
                            tint = selectAllIndicator
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Card(
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(15),
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.secondary,
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
            ) {
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        primaryViewModel.favouriteSelected("favourite")
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(23.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.pin_01),
                        contentDescription = stringResource(R.string.deletenote),
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }
}