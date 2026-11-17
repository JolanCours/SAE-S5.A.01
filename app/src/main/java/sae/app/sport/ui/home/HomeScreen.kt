package sae.app.sport.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sae.app.sport.model.Exercise
import sae.app.sport.model.formatDuration
import sae.app.sport.ui.components.AppIcon
import sae.app.sport.ui.components.DuoCard
import sae.app.sport.ui.components.DuoPressable
import sae.app.sport.ui.components.PoseThumbnail
import sae.app.sport.ui.components.SectionHeader
import sae.app.sport.ui.components.SimpleIcon
import sae.app.sport.ui.theme.AppTheme
import sae.app.sport.ui.theme.Green
import sae.app.sport.ui.theme.GreenDark
import sae.app.sport.ui.theme.cardColors

@Composable
fun HomeScreen(
    builtIns: List<Exercise>,
    custom: List<Exercise>,
    onOpen: (Exercise) -> Unit,
    onCreate: () -> Unit
) {
    val c = AppTheme.colors
    Box(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 1000.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(bottom = 4.dp)) {
                    Text("Prêt à bouger ?", style = MaterialTheme.typography.headlineLarge, color = c.text)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Choisis un exercice et imite les poses devant la caméra.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textMuted
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                CreateCard(onCreate)
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader("Exercices de base", Modifier.padding(top = 8.dp))
            }
            items(builtIns, key = { it.id }) { ex ->
                ExerciseCard(ex, onClick = { onOpen(ex) })
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader("Mes exercices", Modifier.padding(top = 8.dp))
            }
            if (custom.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DuoCard {
                        Text(
                            "Tu n'as pas encore créé d'exercice. Enregistre tes propres poses avec la caméra et enchaîne-les comme tu veux !",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(custom, key = { it.id }) { ex ->
                    ExerciseCard(ex, onClick = { onOpen(ex) })
                }
            }
        }
    }
}

@Composable
private fun CreateCard(onCreate: () -> Unit) {
    val c = AppTheme.colors
    DuoCard(onClick = onCreate) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            DuoPressable(
                onClick = null,
                face = Green,
                shadow = GreenDark,
                shape = CircleShape,
                fillWidth = false,
                modifier = Modifier.size(width = 52.dp, height = 56.dp),
                minHeight = 52.dp
            ) {
                AppIcon(SimpleIcon.PLUS, Color.White, size = 28.dp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Créer mon exercice", style = MaterialTheme.typography.titleLarge, color = c.text)
                Text(
                    "Enregistre tes poses, règle les temps et les pauses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMuted
                )
            }
            AppIcon(SimpleIcon.CHEVRON_RIGHT, c.textMuted, size = 22.dp)
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (face, shadow) = cardColors(exercise.colorIndex)
    val firstPose = exercise.steps.firstOrNull { it.pose != null && it.isPose }?.let {
        exercise.steps.filter { s -> s.pose != null && s.isPose }.getOrNull(1)?.pose ?: it.pose
    }
    DuoPressable(
        onClick = onClick,
        modifier = modifier.height(184.dp),
        face = face,
        shadow = shadow,
        shape = RoundedCornerShape(20.dp),
        depth = 5.dp,
        contentAlignment = Alignment.TopStart
    ) {
        Box(Modifier.fillMaxSize()) {
            PoseThumbnail(
                firstPose,
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = 90.dp, height = 120.dp),
                color = Color.White.copy(alpha = 0.28f),
                padding = 0.06f
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(exercise.emoji, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            exercise.safeDifficulty.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    exercise.name.ifBlank { "Sans nom" },
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${exercise.poseStepCount} pose${if (exercise.poseStepCount > 1) "s" else ""} · ~${formatDuration(exercise.estimatedDurationSec())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
