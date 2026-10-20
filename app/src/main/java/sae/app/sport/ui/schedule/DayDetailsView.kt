package sae.app.sport.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sae.app.sport.model.DailySession
import sae.app.sport.model.Exercise
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
@Composable
fun DayDetailsView(
    date: LocalDate,
    session: DailySession?,
    onAddExercise: (String, Int, Int) -> Unit,
    onToggleCompletion: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")),
                style = MaterialTheme.typography.titleLarge
            )
            
            if (session?.exercises?.isNotEmpty() == true) {
                IconButton(onClick = onToggleCompletion) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Validate Session",
                        tint = if (session.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (session == null || session.exercises.isEmpty()) {
            Text(
                text = "Aucun exercice prévu pour ce jour.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(session.exercises) { exercise ->
                    ExerciseItem(exercise)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajouter un exercice")
        }
    }

    if (showDialog) {
        AddExerciseDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, sets, reps ->
                onAddExercise(name, sets, reps)
                showDialog = false
            }
        )
    }
}

@Composable
fun ExerciseItem(exercise: Exercise) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${exercise.sets} séries x ${exercise.reps} reps")
        }
    }
}

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    // Valide seulement si le nom est rempli et si séries/répétitions sont > 0
    val setsInt = sets.toIntOrNull() ?: 0
    val repsInt = reps.toIntOrNull() ?: 0
    val isValid = name.isNotBlank() && setsInt > 0 && repsInt > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvel exercice") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'exercice") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = sets,
                    // On garde uniquement les chiffres, max 3 caractères
                    onValueChange = { new -> sets = new.filter { it.isDigit() }.take(3) },
                    label = { Text("Nombre de séries") },
                    singleLine = true,
                    // Affiche le clavier numérique
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { new -> reps = new.filter { it.isDigit() }.take(3) },
                    label = { Text("Nombre de répétitions") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, setsInt, repsInt) },
                enabled = isValid // bouton grisé tant que tout n'est pas valide
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

