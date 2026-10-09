
package sae.app.sport.Objectif

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreationExercice(
    objectif: Objectif,
    onBack: () -> Unit
) {
    var nom by remember(objectif.nom) {
        mutableStateOf("")
    }

    var typeExercice by remember(objectif.nom) {
        mutableStateOf(TypeExercice.TEMPS)
    }

    var valeur by remember(objectif.nom) {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titre de la page
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = Color(0xFFD6D6D6),
            border = BorderStroke(1.dp, Color(0xFF2196F3))
        ) {
            androidx.compose.foundation.layout.Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TYPE D'OBJECTIF SÉLECTIONNÉ",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        // Nom de l'objectif sélectionné
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = objectif.nom,
                    fontSize = 14.sp
                )
            }
        }

        Text(
            text = "Créer un exercice",
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
            fontSize = 18.sp
        )

        TextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom de l'exercice") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            text = "Type d'exercice",
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 20.dp, bottom = 4.dp)
        )

        // Les deux choix sont côte à côte
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = typeExercice == TypeExercice.TEMPS,
                    onClick = {
                        typeExercice = TypeExercice.TEMPS
                    }
                )
                Text("TEMPS")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = typeExercice == TypeExercice.REPETITIONS,
                    onClick = {
                        typeExercice = TypeExercice.REPETITIONS
                    }
                )
                Text("RÉPÉTITION")
            }
        }

        TextField(
            value = valeur,
            onValueChange = { valeur = it },
            label = {
                if (typeExercice == TypeExercice.TEMPS) {
                    Text("Temps en minutes")
                } else {
                    Text("Nombre de répétitions")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Le formulaire occupe l'espace disponible.
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = {
                val valeurInt = valeur.toIntOrNull()

                if (nom.isNotBlank() &&
                    valeurInt != null &&
                    valeurInt > 0
                ) {
                    val exercice = Exercice(
                        nom = nom.trim(),
                        type = typeExercice,
                        valeur = valeurInt
                    )

                    objectif.exercices.add(exercice)
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("DÉFINIR")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            Text("Retour")
        }
    }
}
