package sae.app.sport.ui.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import sae.app.sport.data.BuiltInExercises
import sae.app.sport.model.RecordedPose
import sae.app.sport.ui.components.DuoButton
import sae.app.sport.ui.components.DuoCard
import sae.app.sport.ui.components.DuoStyle
import sae.app.sport.ui.components.PoseThumbnail
import sae.app.sport.ui.components.SectionHeader
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Blue
import sae.app.sport.ui.theme.CardPalette

@Composable
fun TemplatesDialog(onPick: (String, RecordedPose) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.surface)
                .padding(20.dp)
        ) {
            Text("Ajouter un modèle", style = MaterialTheme.typography.headlineSmall, color = c.text)
            Text(
                "Tu pourras ensuite ajuster la pose au doigt.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMuted
            )
            Spacer(Modifier.height(14.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 420.dp)
            ) {
                items(BuiltInExercises.templates) { (name, pose) ->
                    DuoCard(onClick = { onPick(name, pose) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            PoseThumbnail(pose, Modifier.size(width = 72.dp, height = 88.dp), color = Blue)
                            Text(name, style = MaterialTheme.typography.labelMedium, color = c.text, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            DuoButton("Fermer", onClick = onDismiss, style = DuoStyle.OUTLINE)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(state: EditorState, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.surface)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Réglages", style = MaterialTheme.typography.headlineSmall, color = c.text)
            Spacer(Modifier.height(16.dp))

            SectionHeader("Icône")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EditorState.EMOJIS.forEach { e ->
                    val sel = e == state.emoji
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) Blue.copy(alpha = 0.15f) else c.surfaceAlt)
                            .border(2.dp, if (sel) Blue else c.border, RoundedCornerShape(12.dp))
                            .clickable { state.emoji = e; state.markDirty() },
                        contentAlignment = Alignment.Center
                    ) { Text(e, style = MaterialTheme.typography.titleLarge) }
                }
            }
            Spacer(Modifier.height(16.dp))

            SectionHeader("Couleur")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CardPalette.forEachIndexed { i, (face, _) ->
                    val sel = i == state.colorIndex
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(face)
                            .border(if (sel) 4.dp else 0.dp, if (sel) c.text else Color.Transparent, CircleShape)
                            .clickable { state.colorIndex = i; state.markDirty() }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            SectionHeader("Difficulté (précision demandée)")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EditorState.DIFFICULTIES.forEach { d ->
                    DuoCard(
                        modifier = Modifier.weight(1f),
                        onClick = { state.difficulty = d; state.markDirty() },
                        selected = state.difficulty == d,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            d.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (state.difficulty == d) Blue else c.text,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            SectionHeader("Description")
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = state.description,
                onValueChange = { state.description = it.take(140); state.markDirty() },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.text),
                cursorBrush = SolidColor(c.text),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceAlt)
                    .border(2.dp, c.border, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                decorationBox = { inner ->
                    if (state.description.isEmpty()) {
                        Text("Ex. : échauffement du matin", style = MaterialTheme.typography.bodyLarge, color = c.textMuted)
                    }
                    inner()
                }
            )
            Spacer(Modifier.height(20.dp))
            DuoButton("OK", onClick = onDismiss)
        }
    }
}

@Composable
fun DiscardDialog(onKeepEditing: () -> Unit, onDiscard: () -> Unit) {
    val c = AppTheme.colors
    AlertDialog(
        onDismissRequest = onKeepEditing,
        containerColor = c.surface,
        title = { Text("Quitter sans enregistrer ?", style = MaterialTheme.typography.headlineSmall, color = c.text) },
        text = { Text("Tes modifications seront perdues.", style = MaterialTheme.typography.bodyLarge, color = c.textMuted) },
        confirmButton = {
            Column(Modifier.fillMaxWidth()) {
                DuoButton("Continuer l'édition", onClick = onKeepEditing)
                Spacer(Modifier.height(8.dp))
                DuoButton("Quitter", onClick = onDiscard, style = DuoStyle.DANGER)
            }
        }
    )
}
