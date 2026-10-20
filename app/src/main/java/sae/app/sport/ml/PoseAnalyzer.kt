package sae.app.sport.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.framework.image.BitmapImageBuilder
import sae.app.sport.model.LandmarkPoint
import sae.app.sport.model.RecordedPose
import kotlin.math.sqrt

class PoseAnalyzer(private val context: Context) {
    private var poseLandmarker: PoseLandmarker? = null

    //todo val
    init {
        try {
            val baseOptions = com.google.mediapipe.tasks.core.BaseOptions.builder()
                .setModelAssetPath("pose_landmarker.task")
                .build()
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            poseLandmarker = null
            //e.printStackTrace()
        }
    }

    fun analyzeBitmap(bitmap: Bitmap): RecordedPose {
        val landmarker = poseLandmarker

        if (landmarker == null)
            return generateMockPose()

        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result: PoseLandmarkerResult = landmarker.detect(mpImage)
            val landmarks = result.landmarks().firstOrNull() ?: return generateMockPose()
            
            val points = landmarks.map { landmark ->
                LandmarkPoint(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z(),
                    visibility = 1f
                )
            }
            return RecordedPose(points)
        } catch (e: Exception) {
            return generateMockPose()
        }
    }

    private fun generateMockPose(): RecordedPose {
        val points = (0 until 33).map { i ->
            LandmarkPoint(x = 0.5f + (i % 5) * 0.05f,
                y = 0.3f + (i / 5) * 0.05f,
                z = 0f,
                visibility = 1f)
        }
        return RecordedPose(points)
    }

    fun comparePoses(current: RecordedPose, target: RecordedPose, threshold: Float = 0.3f): Boolean {
        if (current.landmarks.isEmpty() || target.landmarks.isEmpty()) return false
        val size = minOf(current.landmarks.size, target.landmarks.size)
        var totalDistance = 0f
        for (i in 0 until size) {
            val c = current.landmarks[i]
            val t = target.landmarks[i]
            val dx = c.x - t.x
            val dy = c.y - t.y
            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            totalDistance += dist
        }
        val avgDistance = totalDistance / size
        return avgDistance <= threshold
    }

    fun close() {
        try {
            //nullabel
            poseLandmarker?.close()
        } catch (e: Exception) {}
    }
}
