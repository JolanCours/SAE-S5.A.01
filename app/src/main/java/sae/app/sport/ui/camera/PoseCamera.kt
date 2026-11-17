package sae.app.sport.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import sae.app.sport.ml.PoseAnalyzer
import sae.app.sport.ml.toDisplayBitmap
import sae.app.sport.model.RecordedPose
import sae.app.sport.ui.components.DuoButton
import sae.app.sport.ui.theme.AppTheme
import java.util.concurrent.Executors

data class LiveFrame(
    val pose: RecordedPose?,
    val aspect: Float,
    val timestamp: Long = SystemClock.uptimeMillis()
) {
    fun isFresh(now: Long = SystemClock.uptimeMillis()) = now - timestamp < 800
}

@Composable
fun PoseCameraPreview(
    modifier: Modifier = Modifier,
    frontCamera: Boolean = true,
    onFrame: (LiveFrame) -> Unit,
    onModelError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnFrame by rememberUpdatedState(onFrame)
    val currentOnError by rememberUpdatedState(onModelError)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner, frontCamera) {
        val executor = Executors.newSingleThreadExecutor()
        val analyzer = PoseAnalyzer(context, PoseAnalyzer.Mode.LIVE)
        if (!analyzer.isAvailable) {
            currentOnError("Modèle « ${PoseAnalyzer.MODEL_ASSET} » introuvable dans assets (${analyzer.loadError})")
        }
        var disposed = false
        var provider: ProcessCameraProvider? = null
        var analysis: ImageAnalysis? = null

        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (disposed) return@addListener
            val cameraProvider = try { future.get() } catch (e: Exception) { return@addListener }
            provider = cameraProvider

            val useFront = frontCamera && try {
                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            } catch (e: Exception) {
                false
            }
            val selector = if (useFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
            imageAnalysis.setAnalyzer(executor) { proxy ->
                try {
                    val bitmap = proxy.toDisplayBitmap(mirror = useFront)
                    val pose = analyzer.detect(bitmap)
                    currentOnFrame(LiveFrame(pose, bitmap.width.toFloat() / bitmap.height.toFloat()))
                } catch (_: Exception) {
                } finally {
                    proxy.close()
                }
            }
            analysis = imageAnalysis

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
            } catch (_: Exception) {
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true
            analysis?.clearAnalyzer()
            try {
                provider?.unbindAll()
            } catch (_: Exception) {
            }
            executor.execute { analyzer.close() }
            executor.shutdown()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

fun hasCameraPermission(context: android.content.Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

@Composable
fun CameraPermissionGate(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasCameraPermission(context)) }
    var asked by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        asked = true
    }
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }
    if (granted) {
        content()
    } else {
        Box(
            modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(max = 420.dp)
            ) {
                Text("📷", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(16.dp))
                Text(
                    "On a besoin de ta caméra",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppTheme.colors.text,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Elle sert à voir tes mouvements et à vérifier que tu fais bien la pose. Rien n'est enregistré ni envoyé.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.textMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                DuoButton(
                    text = if (asked) "Réessayer" else "Autoriser la caméra",
                    onClick = { launcher.launch(Manifest.permission.CAMERA) }
                )
            }
        }
    }
}
