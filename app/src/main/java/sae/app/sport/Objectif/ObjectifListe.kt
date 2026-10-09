
package sae.app.sport.Objectif

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val OBJECTIFS_PAR_PAGE = 7

@Composable
fun ObjectifListe(
    onBack: () -> Unit
) {
    var objectifSelectionne by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var pageActuelle by rememberSaveable {
        mutableStateOf(0)
    }

    val objectifs = ObjectifRepository.objectifs
    val nombreDePages = maxOf(
        1,
        (objectifs.size + OBJECTIFS_PAR_PAGE - 1) / OBJECTIFS_PAR_PAGE
    )

    // Si la liste change, on conserve un numéro de page valide.
    val page = pageActuelle.coerceIn(0, nombreDePages - 1)

    if (objectifSelectionne != null) {
        BackHandler {
            objectifSelectionne = null
        }

        val objectif = objectifs.firstOrNull {
            it.nom == objectifSelectionne
        }

        if (objectif != null) {
            CreationExercice(
                objectif = objectif,
                onBack = {
                    objectifSelectionne = null
                }
            )
        } else {
            objectifSelectionne = null
        }

    } else {
        BackHandler {
            onBack()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre de la page
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFD6D6D6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "TYPE D'OBJECTIF",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            // Liste paginée des objectifs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val debut = page * OBJECTIFS_PAR_PAGE
                val fin = minOf(
                    debut + OBJECTIFS_PAR_PAGE,
                    objectifs.size
                )

                items(
                    items = objectifs.subList(debut, fin),
                    key = { it.nom }
                ) { objectif ->
                    OutlinedButton(
                        onClick = {
                            objectifSelectionne = objectif.nom
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp),
                        border = BorderStroke(
                            1.dp,
                            Color.LightGray
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = objectif.nom,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Navigation entre les pages
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (page > 0) {
                            pageActuelle = page - 1
                        }
                    },
                    enabled = page > 0
                ) {
                    Text("<")
                }

                Text(
                    text = "${page + 1} / $nombreDePages",
                    fontSize = 14.sp
                )

                Button(
                    onClick = {
                        if (page < nombreDePages - 1) {
                            pageActuelle = page + 1
                        }
                    },
                    enabled = page < nombreDePages - 1
                ) {
                    Text(">")
                }
            }

            // Retour à la page d'accueil
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Retour")
            }
        }
    }
}
