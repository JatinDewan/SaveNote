package note.notes.savenote.Composables.Components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import note.notes.savenote.R

@Composable
fun BackupAndRestore(
    isVisible: Boolean,
    backUp:() -> Unit,
    restore:() -> Unit,
    dismiss:() -> Unit
){
    var show by remember { mutableStateOf(false) }
    val interactionSource = MutableInteractionSource()
    val scroll = rememberScrollState()

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ){
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { dismiss() }
                )
                .fillMaxSize()
                .background(MaterialTheme.colors.background.copy(0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                backgroundColor = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(0.9f),
                border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(id = R.string.instructions),
                                tint = MaterialTheme.colors.primary
                            )
                            Text(
                                text = stringResource(id = R.string.instructions),
                                color = MaterialTheme.colors.primaryVariant,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = stringResource(id = R.string.backupInformationOne),
                            color = MaterialTheme.colors.primaryVariant,
                            fontSize = 14.sp
                        )

                        AnimatedVisibility(
                            visible = show,
                            enter = expandVertically(tween(100)) + fadeIn(tween(100)),
                            exit = shrinkVertically(tween(100)) + fadeOut(tween(100))
                        ) {
                            Column(
                                modifier = Modifier
                                    .height(260.dp)
                                    .verticalScroll(scroll)
                            ){
                                Text(
                                    text = stringResource(id = R.string.backingup),
                                    color = MaterialTheme.colors.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(15.dp)
                                ){
                                    Instructions(
                                        instructionNumber = 1,
                                        instructionInformation = R.string.backingup1
                                    )

                                    Instructions(
                                        instructionNumber = 2,
                                        instructionInformation = R.string.backingup2
                                    )

                                    Instructions(
                                        instructionNumber = 3,
                                        instructionInformation = R.string.backingup3
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = stringResource(id = R.string.restoringit),
                                    color = MaterialTheme.colors.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(15.dp)
                                ) {
                                    Instructions(
                                        instructionNumber = 1,
                                        instructionInformation = R.string.restoringit1
                                    )

                                    Instructions(
                                        instructionNumber = 2,
                                        instructionInformation = R.string.restoringit2
                                    )

                                    Instructions(
                                        instructionNumber = 3,
                                        instructionInformation = R.string.restoringit3
                                    )

                                    Instructions(
                                        instructionNumber = 4,
                                        instructionInformation = R.string.restoringit4
                                    )
                                }
                            }
                        }

                        Text(
                            modifier = Modifier.clickable { show = !show },
                            text = if (show)
                                stringResource(id = R.string.showless) else
                                stringResource(id = R.string.showmore),
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 16.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BackupButton(
                            icon = ImageVector.vectorResource(id = R.drawable.database_01),
                            text = R.string.Backup,
                            onClick = { backUp() }
                        )

                        BackupButton(
                            icon = ImageVector.vectorResource(id = R.drawable.refresh_ccw_01),
                            text = R.string.Restore,
                            onClick = { restore() }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Instructions(
    instructionNumber: Int,
    instructionInformation: Int
) {
    Row(
        verticalAlignment = Alignment.Top
    ){
        Text(
            text = stringResource(id = R.string.Instructions, instructionNumber.toString()),
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = stringResource(id = instructionInformation),
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BackupButton(
    icon: ImageVector,
    text: Int,
    onClick:() -> Unit,
){
    Card(
        backgroundColor = MaterialTheme.colors.background,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.width(150.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground)
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = icon,
                contentDescription = stringResource(id = text),
                tint = MaterialTheme.colors.primary
            )
            Text(
                text = stringResource(id = text),
                color = MaterialTheme.colors.primaryVariant,
                fontSize = 14.sp
            )
        }
    }
}