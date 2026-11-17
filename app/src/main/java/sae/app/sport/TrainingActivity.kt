package sae.app.sport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import sae.app.sport.ui.navigation.AppNavigation
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.SAESportTheme

class TrainingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAESportTheme {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.background)
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
