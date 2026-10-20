package sae.app.sport.ui.training

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import sae.app.sport.data.MovementRepository
import sae.app.sport.ml.PoseAnalyzer
import sae.app.sport.model.MovementSequence
import java.util.concurrent.Executors

@Composable
fun TrainingScreen(
    repository: MovementRepository,
    onBack: () -> Unit
) {
    var selectedMovement by remember { mutableStateOf<MovementSequence?>(null) }
    val movements = remember { repository.getAllMovements() }

    if (selectedMovement == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Choisir un mouvement", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = onBack) {
                    Text("Retour")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (movements.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun mouvement enregistré. Veuillez d'abord en créer un.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movements) { seq ->
                        Card(
                            onClick = { selectedMovement = seq },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(seq.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Nombre de poses : ${seq.poses.size}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    } else {
        ActiveTrainingSession(
            sequence = selectedMovement!!,
            onFinishSession = { selectedMovement = null }
        )
    }
}

@Composable
fun ActiveTrainingSession(
    sequence: MovementSequence,
    onFinishSession: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val poseAnalyzer = remember { PoseAnalyzer(context) }

    var currentPoseIndex by remember { mutableStateOf(0) }
    var isMatched by remember { mutableStateOf(false) }
    var sessionCompleted by remember { mutableStateOf(false) }

    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()
                        imageProxy.close()
                        
                        val livePose = poseAnalyzer.analyzeBitmap(bitmap)
                        if (livePose != null && currentPoseIndex < sequence.poses.size) {
                            val targetPose = sequence.poses[currentPoseIndex]
                            val matched = poseAnalyzer.comparePoses(livePose, targetPose, threshold = 0.35f)
                            if (matched) {
                                isMatched = true
                            }
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {}
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraExecutor.shutdown()
            poseAnalyzer.close()
        }
    }

    LaunchedEffect(isMatched) {
        if (isMatched) {
            delay(1000L)
            if (currentPoseIndex + 1 < sequence.poses.size) {
                currentPoseIndex++
                isMatched = false
            } else {
                sessionCompleted = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sequence.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                TextButton(onClick = onFinishSession) {
                    Text("Quitter", color = Color.White)
                }
            }

            if (sessionCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Green.copy(alpha = 0.8f), shape = MaterialTheme.shapes.medium)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉 Mouvement réussi !", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onFinishSession) {
                            Text("Terminer")
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isMatched) Color.Green.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pose ${currentPoseIndex + 1} / ${sequence.poses.size}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isMatched) "✓ Pose validée !" else "Reproduisez la pose...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}
