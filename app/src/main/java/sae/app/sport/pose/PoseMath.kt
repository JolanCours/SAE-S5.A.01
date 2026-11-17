package sae.app.sport.pose

import sae.app.sport.model.LandmarkPoint
import sae.app.sport.model.RecordedPose
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object PoseMath {
    const val NOSE = 0
    const val L_SHOULDER = 11
    const val R_SHOULDER = 12
    const val L_ELBOW = 13
    const val R_ELBOW = 14
    const val L_WRIST = 15
    const val R_WRIST = 16
    const val L_HIP = 23
    const val R_HIP = 24
    const val L_KNEE = 25
    const val R_KNEE = 26
    const val L_ANKLE = 27
    const val R_ANKLE = 28
    const val LANDMARK_COUNT = 33

    val KEY_JOINTS = listOf(
        NOSE, L_SHOULDER, R_SHOULDER, L_ELBOW, R_ELBOW, L_WRIST, R_WRIST,
        L_HIP, R_HIP, L_KNEE, R_KNEE, L_ANKLE, R_ANKLE
    )

    val BONES: List<Pair<Int, Int>> = listOf(
        L_SHOULDER to L_ELBOW, L_ELBOW to L_WRIST,
        R_SHOULDER to R_ELBOW, R_ELBOW to R_WRIST,
        L_HIP to L_KNEE, L_KNEE to L_ANKLE,
        R_HIP to R_KNEE, R_KNEE to R_ANKLE,
        L_SHOULDER to L_HIP, R_SHOULDER to R_HIP,
        L_SHOULDER to R_SHOULDER, L_HIP to R_HIP
    )

    val EXTRA_BONES: List<Pair<Int, Int>> = listOf(
        L_ANKLE to 31, R_ANKLE to 32, L_ANKLE to 29, R_ANKLE to 30,
        L_WRIST to 19, R_WRIST to 20
    )

    val FOLLOWERS: Map<Int, List<Int>> = mapOf(
        NOSE to (1..10).toList(),
        L_WRIST to listOf(17, 19, 21),
        R_WRIST to listOf(18, 20, 22),
        L_ANKLE to listOf(29, 31),
        R_ANKLE to listOf(30, 32)
    )

    private val MIRROR_PAIRS = listOf(
        1 to 4, 2 to 5, 3 to 6, 7 to 8, 9 to 10, 11 to 12, 13 to 14, 15 to 16,
        17 to 18, 19 to 20, 21 to 22, 23 to 24, 25 to 26, 27 to 28, 29 to 30, 31 to 32
    )

    private const val ANGLE_DEAD_ZONE_DEG = 10f
    private const val BONE_TOLERANCE_DEG = 80f
    private const val POSITION_DEAD_ZONE = 0.08f
    private const val POSITION_TOLERANCE = 0.55f
    private const val MIN_VISIBILITY = 0.45f

    data class MatchResult(
        val score: Float,
        val boneScores: List<Float?>,
        val bodyVisible: Boolean
    )

    fun isValid(pose: RecordedPose?): Boolean =
        pose != null && pose.landmarks.size >= LANDMARK_COUNT

    private fun LandmarkPoint.vis(): Float = visibility ?: 1f

    private class V(val x: Float, val y: Float)

    private fun RecordedPose.p(i: Int): V {
        val l = landmarks[i]
        return V(l.x * aspect, l.y)
    }

    private fun mid(a: V, b: V) = V((a.x + b.x) / 2f, (a.y + b.y) / 2f)
    private fun dist(a: V, b: V): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun frame(p: RecordedPose): Triple<V, Float, V> {
        val hip = mid(p.p(L_HIP), p.p(R_HIP))
        val sh = mid(p.p(L_SHOULDER), p.p(R_SHOULDER))
        val shoulderWidth = dist(p.p(L_SHOULDER), p.p(R_SHOULDER))
        val scale = max(dist(hip, sh), shoulderWidth * 0.8f).coerceAtLeast(1e-3f)
        return Triple(hip, scale, sh)
    }

    private fun angleDiffDeg(a1: Float, a2: Float): Float {
        var d = abs(a1 - a2) % (2 * PI).toFloat()
        if (d > PI) d = (2 * PI).toFloat() - d
        return d * 180f / PI.toFloat()
    }

    private fun visibleMax(errors: List<Float?>): Float = errors.filterNotNull().maxOrNull() ?: 0f

    fun compare(live: RecordedPose, target: RecordedPose): MatchResult {
        if (!isValid(live) || !isValid(target)) {
            return MatchResult(0f, BONES.map { null }, bodyVisible = false)
        }

        val (lc, ls, _) = frame(live)
        val (tc, ts, _) = frame(target)

        val boneErrors = BONES.map { (a, b) ->
            val vis = min(live.landmarks[a].vis(), live.landmarks[b].vis())
            if (vis < MIN_VISIBILITY) return@map null
            val la = live.p(a); val lb = live.p(b)
            val ta = target.p(a); val tb = target.p(b)
            val angLive = atan2(lb.y - la.y, lb.x - la.x)
            val angTarget = atan2(tb.y - ta.y, tb.x - ta.x)
            val lenLive = dist(la, lb) / ls
            val lenTarget = dist(ta, tb) / ts
            val reliability = (min(lenLive, lenTarget) / 0.35f).coerceIn(0.3f, 1f)
            val dirErr = ((angleDiffDeg(angLive, angTarget) - ANGLE_DEAD_ZONE_DEG) / BONE_TOLERANCE_DEG)
                .coerceIn(0f, 1f) * reliability
            val ratio = min(lenLive, lenTarget) / max(max(lenLive, lenTarget), 1e-3f)
            val lenErr = ((1f - ratio) - 0.15f).coerceAtLeast(0f) / 0.85f
            (dirErr + 0.7f * lenErr).coerceIn(0f, 1f)
        }

        var posErrSq = 0f
        var posCount = 0
        var maxErr = visibleMax(boneErrors)
        for (j in KEY_JOINTS) {
            if (live.landmarks[j].vis() < MIN_VISIBILITY) continue
            val l = live.p(j); val t = target.p(j)
            val lx = (l.x - lc.x) / ls; val ly = (l.y - lc.y) / ls
            val tx = (t.x - tc.x) / ts; val ty = (t.y - tc.y) / ts
            val d = sqrt((lx - tx) * (lx - tx) + (ly - ty) * (ly - ty))
            val e = ((d - POSITION_DEAD_ZONE) / POSITION_TOLERANCE).coerceIn(0f, 1f)
            maxErr = max(maxErr, e)
            posErrSq += e * e
            posCount++
        }

        val boneScores = boneErrors.map { it?.let { e -> 1f - e } }
        val visible = boneErrors.filterNotNull()
        val bodyVisible = visible.size >= 8 && posCount >= 9
        if (visible.isEmpty() || posCount == 0) {
            return MatchResult(0f, boneScores, bodyVisible = false)
        }
        val boneMse = visible.sumOf { (it * it).toDouble() }.toFloat() / visible.size
        val posMse = posErrSq / posCount
        val rms = sqrt(0.6f * boneMse + 0.4f * posMse)
        val base = 1f - (0.75f * rms + 0.25f * maxErr)
        val coverage = visible.size / BONES.size.toFloat()
        val raw = base * (0.5f + 0.5f * coverage)
        return MatchResult(raw.coerceIn(0f, 1f), boneScores, bodyVisible)
    }

    fun alignTo(target: RecordedPose, live: RecordedPose?): RecordedPose {
        if (!isValid(target) || live == null || !isValid(live)) return target
        val (lc, ls, _) = frame(live)
        val (tc, ts, _) = frame(target)
        val k = ls / ts
        val pts = target.landmarks.mapIndexed { i, l ->
            val t = target.p(i)
            val x = (t.x - tc.x) * k + lc.x
            val y = (t.y - tc.y) * k + lc.y
            l.copy(x = x / live.aspect, y = y)
        }
        return RecordedPose(pts, live.aspect)
    }

    fun mirror(pose: RecordedPose): RecordedPose {
        if (!isValid(pose)) return pose
        val src = pose.landmarks
        val out = src.map { it.copy(x = 1f - it.x) }.toMutableList()
        for ((a, b) in MIRROR_PAIRS) {
            out[a] = src[b].copy(x = 1f - src[b].x)
            out[b] = src[a].copy(x = 1f - src[a].x)
        }
        return pose.copy(landmarks = out)
    }

    fun moveJoint(pose: RecordedPose, joint: Int, newX: Float, newY: Float): RecordedPose {
        if (!isValid(pose)) return pose
        val old = pose.landmarks[joint]
        val nx = newX.coerceIn(0f, 1f)
        val ny = newY.coerceIn(0f, 1f)
        val dx = nx - old.x
        val dy = ny - old.y
        val out = pose.landmarks.toMutableList()
        out[joint] = old.copy(x = nx, y = ny, visibility = 1f)
        FOLLOWERS[joint]?.forEach { f ->
            val p = out[f]
            out[f] = p.copy(x = (p.x + dx).coerceIn(0f, 1f), y = (p.y + dy).coerceIn(0f, 1f))
        }
        return pose.copy(landmarks = out)
    }

    fun bounds(pose: RecordedPose): FloatArray {
        if (!isValid(pose)) return floatArrayOf(0f, 0f, 1f, 1f)
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        for (i in KEY_JOINTS + listOf(29, 30, 31, 32, 19, 20)) {
            val l = pose.landmarks[i]
            val x = l.x * pose.aspect
            minX = min(minX, x); maxX = max(maxX, x)
            minY = min(minY, l.y); maxY = max(maxY, l.y)
        }
        return floatArrayOf(minX, minY, maxX, maxY)
    }
}
