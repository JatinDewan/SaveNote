package note.notes.savenote.Composables.Components

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import note.notes.savenote.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleAlpha(0.0f,0.0f,0.0f,0.0f)
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun ExpandableButton(
    dismiss: Boolean,
    expand:() -> Unit,
    expandedIsFalse:() -> Unit,
    navigateNewNote:() -> Unit,
    navigateNewChecklist:() -> Unit,
    colour: ColourUtil,
    modifier: Modifier = Modifier
) {
    val backgroundColour: Color by animateColorAsState(
        animationSpec = tween(100),
        targetValue =
        colour.colourSelection(
            !dismiss,
            MaterialTheme.colors.primary,
            MaterialTheme.colors.onSecondary
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
                        detectTapGestures( onPress = { expandedIsFalse() } )
                    }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    end = 20.dp,
                    bottom = 20.dp
                ),
            contentAlignment = Alignment.BottomEnd
        ){
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {

                CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme){
                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(150)) + fadeIn(tween(150)),
                        exit = scaleOut(tween(150)) + fadeOut(tween(150))
                    ) {
                        ButtonEntries(
                            buttonFunction = { navigateNewChecklist() },
                            entryLabel = R.string.ListOnly,
                            entryIcon = R.drawable.dotpoints_01
                        )
                    }

                    AnimatedVisibility(
                        visible = dismiss,
                        enter = scaleIn(tween(100)) + fadeIn(tween(100)),
                        exit = scaleOut(tween(100)) + fadeOut(tween(100))
                    ) {
                        ButtonEntries(
                            buttonFunction = { navigateNewNote() },
                            entryLabel = R.string.note,
                            entryIcon = R.drawable.pencil_line
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(15.dp),
                        backgroundColor = backgroundColour,
                        modifier = Modifier.size(60.dp),
                        onClick = { if (!dismiss) expand() else expandedIsFalse() }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ){
                            RotatingIcon(
                                icon = ImageVector.vectorResource(id = R.drawable.plus),
                                boolean = dismiss,
                                modifier = Modifier
                                    .size(60.dp)
                                    .padding(10.dp)

                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ButtonEntries(
    buttonFunction:() -> Unit,
    entryLabel: Int,
    entryIcon: Int
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
                backgroundColor = MaterialTheme.colors.onSecondary
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
                backgroundColor = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(10.dp)
            ){
                Icon(
                    tint = MaterialTheme.colors.background,
                    imageVector = ImageVector.vectorResource(id = entryIcon),
                    contentDescription = stringResource(R.string.check),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(22.dp)
                )
            }
        }
    }
}

@Composable
fun RotatingIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    boolean: Boolean
) {
    val rotation = remember { Animatable(0f) }

    if(boolean){
        LaunchedEffect(Unit) {
            rotation.animateTo(
                targetValue = 45f,
                animationSpec = tween(150)
            )
        }
    }
    if(!boolean){
        LaunchedEffect(Unit) {
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(150)
            )
        }
    }

    Icon(
        imageVector = icon,
        tint = MaterialTheme.colors.background,
        contentDescription = null,
        modifier = modifier.rotate(rotation.value)
    )
}