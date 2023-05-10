package note.notes.savenote.Composables.Components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.Utils.ColourUtil
import note.notes.savenote.ui.theme.UniversalFamily
import note.notes.savenote.R
import note.notes.savenote.ViewModelClasses.PrimaryViewModel


private object NoRippleThemes : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleAlpha(0.0f,0.0f,0.0f,0.0f)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MoreOptionsChecklist(
    dismiss: Boolean,
    share:() -> Unit,
    reArrange:() -> Unit,
    unCheckCompleted:() -> Unit,
    confirmDelete:() -> Unit,
    expandedIsFalse:() -> Unit,
    showCompleted:() -> Unit,
    showCompletedBoolean: Boolean,
    reArrangeBoolean: Boolean,
    colour: ColourUtil,
    modifier: Modifier = Modifier
) {
    val showCompletedBackgroundColour: Color by animateColorAsState(
        animationSpec = tween(300),
        targetValue =
        colour.colourSelection(
            !showCompletedBoolean,
            MaterialTheme.colors.onSecondary,
            MaterialTheme.colors.primary
        )
    )

    val reArrangeBackgroundColour: Color by animateColorAsState(
        animationSpec = tween(300),
        targetValue =
        colour.colourSelection(
            !reArrangeBoolean,
            MaterialTheme.colors.onSecondary,
            MaterialTheme.colors.primary
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ){
        if (dismiss){
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.background.copy(alpha = 0.8f))
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = { expandedIsFalse() })
                    }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 7.dp),
            contentAlignment = Alignment.TopEnd
        ){
            CompositionLocalProvider(LocalRippleTheme provides NoRippleThemes) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(50)) + fadeIn(tween(50)),
                        exit = scaleOut(tween(250)) + fadeOut(tween(250))
                    ) {
                        OptionsEntries(
                            buttonFunction = { share() },
                            entryLabel = R.string.SHARELIST,
                            entryIcon = R.drawable.share_03,
                            backgroundColour = MaterialTheme.colors.onSecondary,
                            iconBackgroundColour = MaterialTheme.colors.onSecondary
                        )
                    }

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(100)) + fadeIn(tween(100)),
                        exit = scaleOut(tween(200)) + fadeOut(tween(200))
                    ) {
                        OptionsEntries(
                            buttonFunction = { reArrange() },
                            entryLabel = R.string.REARRANGE,
                            entryIcon = R.drawable.switch_vertical_01,
                            backgroundColour = reArrangeBackgroundColour,
                            iconBackgroundColour = reArrangeBackgroundColour
                        )
                    }

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(150)) + fadeIn(tween(150)),
                        exit = scaleOut(tween(150)) + fadeOut(tween(150))
                    ) {

                        OptionsEntries(
                            buttonFunction = { showCompleted() },
                            entryLabel = if(showCompletedBoolean) R.string.HIDECOMPLETED else R.string.SHOWCOMPLETED,
                            entryIcon = R.drawable.eye,
                            backgroundColour = showCompletedBackgroundColour,
                            iconBackgroundColour = showCompletedBackgroundColour
                        )
                    }

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(200)) + fadeIn(tween(200)),
                        exit = scaleOut(tween(100)) + fadeOut(tween(100))
                    ) {1
                        OptionsEntries(
                            buttonFunction = { unCheckCompleted() },
                            entryLabel = R.string.UNCHECKALLENTRIES,
                            entryIcon = R.drawable.circle,
                            backgroundColour = MaterialTheme.colors.onSecondary,
                            iconBackgroundColour = MaterialTheme.colors.onSecondary
                        )
                    }

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(250)) + fadeIn(tween(250)),
                        exit = scaleOut(tween(50)) + fadeOut(tween(50))
                    ) {
                        var confirm by remember { mutableStateOf(false) }
                        val confirmDeleteColour: Color by animateColorAsState(
                            animationSpec = tween(300),
                            targetValue =
                            colour.colourSelection(
                                !confirm,
                                MaterialTheme.colors.onSecondary,
                                MaterialTheme.colors.onError
                            )
                        )
                        IconButton(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            onClick = { confirm = if(!confirm) true else { confirmDelete(); false } }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Card(
                                    backgroundColor = confirmDeleteColour
                                ) {
                                    AnimatedContent(
                                        targetState = !confirm,
                                        transitionSpec = {
                                            slideIntoContainer(AnimatedContentScope.SlideDirection.Right, tween(200)) with
                                                    slideOutOfContainer(AnimatedContentScope.SlideDirection.Right)
                                        }
                                    ){ animateSelected ->
                                        Text(
                                            modifier = Modifier.padding(
                                                vertical = 5.dp,
                                                horizontal = 10.dp
                                            ),
                                            text = if (animateSelected)
                                                stringResource(id = R.string.DELETECURRENTCHECKLIST) else stringResource(id = R.string.taptoconfirm),
                                            color = MaterialTheme.colors.background,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = UniversalFamily,
                                            style = TextStyle(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                )
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Card(
                                    backgroundColor = confirmDeleteColour,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        tint = MaterialTheme.colors.background,
                                        imageVector = ImageVector.vectorResource(id = R.drawable.trash_02),
                                        contentDescription = stringResource(R.string.check),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MoreOptionsMain(
    dismiss: Boolean,
    backUp:() -> Unit,
    rateApp:() -> Unit,
    expandedIsFalse:() -> Unit,
    help:() -> Unit,
    primaryViewModel: PrimaryViewModel,
    modifier: Modifier = Modifier
) {

    val primaryUiState by primaryViewModel.uiState.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ){
        if (dismiss){
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.background.copy(alpha = 0.8f))
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { expandedIsFalse() }
                        )
                    }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 55.dp),
            contentAlignment = Alignment.TopEnd
        ){
            CompositionLocalProvider(LocalRippleTheme provides NoRippleThemes) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(50)) + fadeIn(tween(50)),
                        exit = scaleOut(tween(250)) + fadeOut(tween(250))
                    ){
                        IconButton(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            onClick = { primaryViewModel.updateCurrentPageView(primaryUiState.sortByView) }
                        ) {
                            Column(horizontalAlignment = Alignment.End){
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Card(
                                        backgroundColor = MaterialTheme.colors.onBackground
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(
                                                    top = 5.dp,
                                                    bottom = 5.dp,
                                                    start = 10.dp,
                                                ),
                                                text = stringResource(id = R.string.sort),
                                                color = MaterialTheme.colors.onSecondary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = UniversalFamily,
                                                style = TextStyle(
                                                    platformStyle = PlatformTextStyle(
                                                        includeFontPadding = false
                                                    )
                                                )
                                            )

                                            AnimatedContent(
                                                targetState = primaryUiState.sortByView,
                                                transitionSpec = {
                                                    slideIntoContainer(
                                                        AnimatedContentScope.SlideDirection.Down,
                                                        tween(200)
                                                    ) with slideOutOfContainer(
                                                        AnimatedContentScope.SlideDirection.Down,
                                                        tween(200)
                                                    )+ fadeOut(tween(200))
                                                }
                                            ){
                                                Text(
                                                    modifier = Modifier.padding(end = 10.dp),
                                                    text = stringResource(id = primaryViewModel.sortByString()),
                                                    color = MaterialTheme.colors.primary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = UniversalFamily,
                                                    style = TextStyle(
                                                        platformStyle = PlatformTextStyle(
                                                            includeFontPadding = false
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Card(
                                        backgroundColor = MaterialTheme.colors.onSecondary,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            tint = MaterialTheme.colors.background,
                                            imageVector = ImageVector.vectorResource(id = R.drawable.sort_by),
                                            contentDescription = stringResource(R.string.check),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }



                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(50)) + fadeIn(tween(50)),
                        exit = scaleOut(tween(250)) + fadeOut(tween(250))
                    ) {
                        OptionsEntries(
                            buttonFunction = { backUp() },
                            entryLabel = R.string.backup,
                            entryIcon = R.drawable.server_01,
                            backgroundColour = MaterialTheme.colors.onSecondary,
                            iconBackgroundColour = MaterialTheme.colors.onSecondary,
                        )
                    }

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(100)) + fadeIn(tween(100)),
                        exit = scaleOut(tween(200)) + fadeOut(tween(200))
                    ) {
                        OptionsEntries(
                            buttonFunction = { rateApp() },
                            entryLabel = R.string.rateapp,
                            entryIcon = R.drawable.thumbs_up,
                            backgroundColour = MaterialTheme.colors.onSecondary,
                            iconBackgroundColour = MaterialTheme.colors.onSecondary,
                        )
                    }

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(150)) + fadeIn(tween(150)),
                        exit = scaleOut(tween(150)) + fadeOut(tween(150))
                    ) {

                        OptionsEntries(
                            buttonFunction = { help() },
                            entryLabel = R.string.Help,
                            entryIcon = R.drawable.help_circle,
                            backgroundColour = MaterialTheme.colors.onSecondary,
                            iconBackgroundColour = MaterialTheme.colors.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptionsEntries(
    buttonFunction:() -> Unit,
    entryLabel: Int,
    backgroundColour: Color,
    iconBackgroundColour: Color,
    entryIcon: Int,
){
    IconButton(
        modifier = Modifier.padding(horizontal = 10.dp),
        onClick = { buttonFunction() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Card(
                backgroundColor = backgroundColour
            ){
                Text(
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
                    text = stringResource(id = entryLabel),
                    color = MaterialTheme.colors.background,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = UniversalFamily,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Card(
                backgroundColor = iconBackgroundColour,
                shape = RoundedCornerShape(10.dp)
            ){
                Icon(
                    tint = MaterialTheme.colors.background ,
                    imageVector = ImageVector.vectorResource(id = entryIcon ),
                    contentDescription = stringResource(R.string.check),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
        }
    }
}