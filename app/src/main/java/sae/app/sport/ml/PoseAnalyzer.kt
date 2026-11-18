package sae.app.sport.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import sae.app.sport.model.LandmarkPoint
import sae.app.sport.model.RecordedPose
import java.io.Closeable

class PoseAnalyzer(context: Context, private val mode: Mode = Mode.LIVE) : Closeable {
    enum class Mode { LIVE, IMAGE }

    private var landmarker: PoseLandmarker? = null
    private var lastTimestamp = -1L

    var loadError: String? = null
        private set

    val isAvailable: Boolean get() = landmarker != null

    init {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .build()
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(if (mode == Mode.LIVE) RunningMode.VIDEO else RunningMode.IMAGE)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()
            landmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            landmarker = null
            loadError = e.message ?: e.javaClass.simpleName
        }
    }

    @Synchronized
    fun detect(bitmap: Bitmap): RecordedPose? {
        val lm = landmarker ?: return null
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = if (mode == Mode.LIVE) {
                var ts = SystemClock.uptimeMillis()
                if (ts <= lastTimestamp) ts = lastTimestamp + 1
                lastTimestamp = ts
                lm.detectForVideo(mpImage, ts)
            } else {
                lm.detect(mpImage)
            }
            val landmarks = result.landmarks().firstOrNull() ?: return null
            if (landmarks.size < 33) return null
            RecordedPose(
                landmarks = landmarks.map { l ->
                    LandmarkPoint(
                        x = l.x(),
                        y = l.y(),
                        z = l.z(),
                        visibility = l.visibility().orElse(1f)
                    )
                },
                aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
            )
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    override fun close() {
        try {
            landmarker?.close()
        } catch (_: Exception) {
        }
        landmarker = null
    }

    companion object {
        const val MODEL_ASSET = "pose_landmarker.task"
    }
}

fun ImageProxy.toDisplayBitmap(mirror: Boolean): Bitmap {
    val src = toBitmap()
    val rotation = imageInfo.rotationDegrees
    if (rotation == 0 && !mirror) return src
    val matrix = Matrix().apply {
        postRotate(rotation.toFloat())
        if (mirror) postScale(-1f, 1f)
    }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
}
