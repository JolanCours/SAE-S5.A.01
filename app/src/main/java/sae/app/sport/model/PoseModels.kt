package sae.app.sport.model

data class LandmarkPoint(
    val x: Float,
    val y: Float,
    val z: Float,
    val visibility: Float? = null
)

data class RecordedPose(
    val landmarks: List<LandmarkPoint>
)

data class MovementSequence(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val poses: List<RecordedPose>
)
