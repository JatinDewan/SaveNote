package note.notes.savenote

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import note.notes.savenote.Composables.MainComposable
import note.notes.savenote.ui.theme.SaveNoteTheme

class SaveNote : ComponentActivity() {
    var loaded = true
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition{ loaded }
        super.onCreate(savedInstanceState)
        setContent {
            SaveNoteTheme {
                MainComposable(
                    loaded = { loaded = false }
                )
            }
        }
    }
}
