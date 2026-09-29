package sae.app.sport.ui.creation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import sae.app.sport.data.MovementRepository
import sae.app.sport.ml.PoseAnalyzer
import sae.app.sport.model.MovementSequence
import sae.app.sport.model.RecordedPose

enum class CreationStep {
    CONFIG, COUNTDOWN_CAPTURE, EDIT_POSES, SAVE
}

@Composable
fun CreationFlowScreen(
    repository: MovementRepository,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(CreationStep.CONFIG) }
    var movementName by remember { mutableStateOf("") }
    var totalPosesCount by remember { mutableStateOf(3) }
    
    var currentPoseIndex by remember { mutableStateOf(0) }
    val capturedPoses = remember { mutableStateListOf<RecordedPose>() }
    
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

    when (step) {
        CreationStep.CONFIG -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Créer un nouveau mouvement", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = movementName,
                    onValueChange = { movementName = it },
                    label = { Text("Nom du mouvement") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Nombre de poses : $totalPosesCount")
                Slider(
                    value = totalPosesCount.toFloat(),
                    onValueChange = { totalPosesCount = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (movementName.isNotBlank()) {
                            capturedPoses.clear()
                            currentPoseIndex = 0
                            step = CreationStep.COUNTDOWN_CAPTURE
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = movementName.isNotBlank() && hasCameraPermission
                ) {
                    Text("Commencer la capture")
                }
                if (!hasCameraPermission) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Permission caméra requise", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        CreationStep.COUNTDOWN_CAPTURE -> {
            CountdownCaptureScreen(
                poseIndex = currentPoseIndex,
                totalPoses = totalPosesCount,
                onPoseCaptured = { pose ->
                    capturedPoses.add(pose)
                    if (currentPoseIndex + 1 < totalPosesCount) {
                        currentPoseIndex++
                    } else {
                        step = CreationStep.EDIT_POSES
                        currentPoseIndex = 0
                    }
                }
            )
        }
        CreationStep.EDIT_POSES -> {
            if (capturedPoses.isNotEmpty() && currentPoseIndex < capturedPoses.size) {
                PoseEditorScreen(
                    pose = capturedPoses[currentPoseIndex],
                    poseIndex = currentPoseIndex,
                    totalPoses = capturedPoses.size,
                    onPoseUpdated = { updatedPose ->
                        capturedPoses[currentPoseIndex] = updatedPose
                    },
                    onNext = {
                        if (currentPoseIndex + 1 < capturedPoses.size) {
                            currentPoseIndex++
                        } else {
                            step = CreationStep.SAVE
                        }
                    },
                    onPrev = {
                        if (currentPoseIndex > 0) {
                            currentPoseIndex--
                        }
                    }
                )
            }
        }
        CreationStep.SAVE -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sauvegarder le mouvement", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Nom : $movementName")
                Text("Nombre de poses : ${capturedPoses.size}")
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val seq = MovementSequence(name = movementName, poses = capturedPoses.toList())
                        repository.saveMovement(seq)
                        onFinished()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enregistrer")
                }
            }
        }
    }
}

@Composable
fun CountdownCaptureScreen(
    poseIndex: Int,
    totalPoses: Int,
    onPoseCaptured: (RecordedPose) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var countdown by remember { mutableStateOf(5) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val poseAnalyzer = remember { PoseAnalyzer(context) }

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(poseIndex) {
        countdown = 5
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    val pose = poseAnalyzer.analyzeBitmap(bitmap) ?: RecordedPose(emptyList())
                    onPoseCaptured(pose)
                }

                override fun onError(exception: ImageCaptureException) {
                    onPoseCaptured(RecordedPose(emptyList()))
                }
            }
        ) ?: run {
            onPoseCaptured(RecordedPose(emptyList()))
        }
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = CameraXPreview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {}
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            poseAnalyzer.close()
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
            Text(
                text = "Pose ${poseIndex + 1} / $totalPoses",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.extraLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (countdown > 0) "$countdown" else "📸",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
            }

            Button(onClick = {
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                            val bitmap = image.toBitmap()
                            image.close()
                            val pose = poseAnalyzer.analyzeBitmap(bitmap) ?: RecordedPose(emptyList())
                            onPoseCaptured(pose)
                        }
                        override fun onError(exception: ImageCaptureException) {
                            onPoseCaptured(RecordedPose(emptyList()))
                        }
                    }
                ) ?: onPoseCaptured(RecordedPose(emptyList()))
            }) {
                Text("Capturer maintenant")
            }
        }
    }
}

@Composable
fun PoseEditorScreen(pose: RecordedPose, poseIndex: Int, totalPoses: Int, onPoseUpdated: (RecordedPose) -> Unit, onNext: () -> Unit, onPrev: () -> Unit) {
    val landmarks = remember(pose) { pose.landmarks.toMutableStateList() }
    var selectedLandmarkIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Édition de la pose ${poseIndex + 1} / $totalPoses", style = MaterialTheme.typography.titleLarge)
        Text("Glissez les points (bones/landmarks) avec le tactile pour ajuster la pose.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.DarkGray)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()
                            selectedLandmarkIndex = landmarks.indices.minByOrNull { i ->
                                val lm = landmarks[i]
                                val lx = lm.x * canvasWidth
                                val ly = lm.y * canvasHeight
                                val dx = lx - offset.x
                                val dy = ly - offset.y
                                dx * dx + dy * dy
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            selectedLandmarkIndex?.let { idx ->
                                val canvasWidth = size.width.toFloat()
                                val canvasHeight = size.height.toFloat()
                                val lm = landmarks[idx]
                                val newX = (lm.x * canvasWidth + dragAmount.x).coerceIn(0f, canvasWidth) / canvasWidth
                                val newY = (lm.y * canvasHeight + dragAmount.y).coerceIn(0f, canvasHeight) / canvasHeight
                                landmarks[idx] = lm.copy(x = newX, y = newY)
                                onPoseUpdated(RecordedPose(landmarks.toList()))
                            }
                        },
                        onDragEnd = {
                            selectedLandmarkIndex = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val connections = listOf(
                    11 to 12, 11 to 13, 13 to 15, 12 to 14, 14 to 16,
                    11 to 23, 12 to 24, 23 to 24,
                    23 to 25, 25 to 27, 24 to 26, 26 to 28
                )

                for ((u, v) in connections) {
                    if (u < landmarks.size && v < landmarks.size) {
                        val p1 = landmarks[u]
                        val p2 = landmarks[v]
                        drawLine(
                            color = Color.Green,
                            start = Offset(p1.x * canvasWidth, p1.y * canvasHeight),
                            end = Offset(p2.x * canvasWidth, p2.y * canvasHeight),
                            strokeWidth = 4f
                        )
                    }
                }

                landmarks.forEachIndexed { i, lm ->
                    val color = if (i == selectedLandmarkIndex) Color.Red else Color.Cyan
                    drawCircle(
                        color = color,
                        radius = if (i == selectedLandmarkIndex) 16f else 10f,
                        center = Offset(lm.x * canvasWidth, lm.y * canvasHeight)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onPrev, enabled = poseIndex > 0) {
                Text("Précédent")
            }
            Button(onClick = onNext) {
                Text(if (poseIndex + 1 < totalPoses) "Suivant" else "Terminer")
            }
        }
    }
}
