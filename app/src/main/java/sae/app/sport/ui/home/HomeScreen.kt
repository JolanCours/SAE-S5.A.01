package sae.app.sport.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onCreateMovement: () -> Unit,
    onTrainMovement: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SAE Sport - MediaPipe", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onCreateMovement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Créer un mouvement", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onTrainMovement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("S'entraîner / Reproduire", style = MaterialTheme.typography.titleMedium)
        }
    }
}
