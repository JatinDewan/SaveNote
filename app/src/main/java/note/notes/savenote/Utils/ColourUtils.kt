package note.notes.savenote.Utils

import androidx.compose.ui.graphics.Color

class ColourUtil {
    fun colourSelection(returnColor: Boolean, optionOne: Color, optionTwo: Color): Color {
        return if(returnColor) optionOne else optionTwo
    }
}