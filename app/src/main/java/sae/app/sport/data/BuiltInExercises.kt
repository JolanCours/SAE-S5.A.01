package sae.app.sport.data

import sae.app.sport.model.Difficulty
import sae.app.sport.model.Exercise
import sae.app.sport.model.ExerciseStep
import sae.app.sport.model.LandmarkPoint
import sae.app.sport.model.RecordedPose
import sae.app.sport.model.StepType
import kotlin.math.sqrt

object BuiltInExercises {
    private const val W = 300f
    private const val H = 400f

    private class Pt(val x: Float, val y: Float) {
        operator fun plus(o: Pt) = Pt(x + o.x, y + o.y)
        operator fun minus(o: Pt) = Pt(x - o.x, y - o.y)
        operator fun times(k: Float) = Pt(x * k, y * k)
        fun len() = sqrt(x * x + y * y)
        fun norm(): Pt { val l = len().coerceAtLeast(1e-3f); return Pt(x / l, y / l) }
    }

    private fun pt(x: Number, y: Number) = Pt(x.toFloat(), y.toFloat())

    private class Body(
        val nose: Pt,
        val lSh: Pt, val rSh: Pt,
        val lEl: Pt, val rEl: Pt,
        val lWr: Pt, val rWr: Pt,
        val lHip: Pt, val rHip: Pt,
        val lKn: Pt, val rKn: Pt,
        val lAn: Pt, val rAn: Pt
    )

    private fun Body.toPose(): RecordedPose {
        val pts = arrayOfNulls<Pt>(33)
        val sw = (rSh - lSh).len()
        val u = sw * 0.18f
        val torso = ((lHip + rHip) * 0.5f - (lSh + rSh) * 0.5f).len()

        pts[0] = nose
        pts[1] = nose + pt(-0.25f * u, -0.55f * u)
        pts[2] = nose + pt(-0.5f * u, -0.6f * u)
        pts[3] = nose + pt(-0.8f * u, -0.55f * u)
        pts[4] = nose + pt(0.25f * u, -0.55f * u)
        pts[5] = nose + pt(0.5f * u, -0.6f * u)
        pts[6] = nose + pt(0.8f * u, -0.55f * u)
        pts[7] = nose + pt(-1.3f * u, -0.2f * u)
        pts[8] = nose + pt(1.3f * u, -0.2f * u)
        pts[9] = nose + pt(-0.35f * u, 0.6f * u)
        pts[10] = nose + pt(0.35f * u, 0.6f * u)
        pts[11] = lSh; pts[12] = rSh
        pts[13] = lEl; pts[14] = rEl
        pts[15] = lWr; pts[16] = rWr

        fun hand(el: Pt, wr: Pt, side: Float): Triple<Pt, Pt, Pt> {
            val d = (wr - el).norm()
            val perp = Pt(-d.y, d.x) * side
            val h = (wr - el).len() * 0.28f
            return Triple(
                wr + d * h + perp * (0.3f * h),
                wr + d * (1.1f * h) - perp * (0.15f * h),
                wr + d * (0.6f * h) - perp * (0.5f * h)
            )
        }
        hand(lEl, lWr, 1f).let { (p, i, t) -> pts[17] = p; pts[19] = i; pts[21] = t }
        hand(rEl, rWr, -1f).let { (p, i, t) -> pts[18] = p; pts[20] = i; pts[22] = t }

        pts[23] = lHip; pts[24] = rHip
        pts[25] = lKn; pts[26] = rKn
        pts[27] = lAn; pts[28] = rAn
        pts[29] = lAn + pt(0.03f * torso, 0.08f * torso)
        pts[30] = rAn + pt(-0.03f * torso, 0.08f * torso)
        pts[31] = lAn + pt(-0.12f * torso, 0.14f * torso)
        pts[32] = rAn + pt(0.12f * torso, 0.14f * torso)

        for ((a, b) in SIDE_PAIRS) {
            val t = pts[a]; pts[a] = pts[b]; pts[b] = t
        }
        return RecordedPose(
            landmarks = pts.map { p -> LandmarkPoint(p!!.x / W, p.y / H, 0f, 1f) },
            aspect = W / H
        )
    }

    private val SIDE_PAIRS = listOf(
        1 to 4, 2 to 5, 3 to 6, 7 to 8, 9 to 10, 11 to 12, 13 to 14, 15 to 16,
        17 to 18, 19 to 20, 21 to 22, 23 to 24, 25 to 26, 27 to 28, 29 to 30, 31 to 32
    )

    private val STANDING = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(108, 160), rEl = pt(192, 160),
        lWr = pt(104, 208), rWr = pt(196, 208),
        lHip = pt(133, 210), rHip = pt(167, 210),
        lKn = pt(132, 280), rKn = pt(168, 280),
        lAn = pt(132, 350), rAn = pt(168, 350)
    )

    private val T_POSE = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(80, 110), rEl = pt(220, 110),
        lWr = pt(40, 110), rWr = pt(260, 110),
        lHip = pt(133, 210), rHip = pt(167, 210),
        lKn = pt(132, 280), rKn = pt(168, 280),
        lAn = pt(132, 350), rAn = pt(168, 350)
    )

    private val ARMS_UP = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(114, 62), rEl = pt(186, 62),
        lWr = pt(110, 16), rWr = pt(190, 16),
        lHip = pt(133, 210), rHip = pt(167, 210),
        lKn = pt(132, 280), rKn = pt(168, 280),
        lAn = pt(132, 350), rAn = pt(168, 350)
    )

    private val STAR = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(88, 76), rEl = pt(212, 76),
        lWr = pt(58, 40), rWr = pt(242, 40),
        lHip = pt(133, 210), rHip = pt(167, 210),
        lKn = pt(112, 280), rKn = pt(188, 280),
        lAn = pt(92, 350), rAn = pt(208, 350)
    )

    private val FEET_TOGETHER = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(110, 160), rEl = pt(190, 160),
        lWr = pt(108, 208), rWr = pt(192, 208),
        lHip = pt(134, 210), rHip = pt(166, 210),
        lKn = pt(140, 280), rKn = pt(160, 280),
        lAn = pt(142, 350), rAn = pt(158, 350)
    )

    private val SQUAT = Body(
        nose = pt(150, 128),
        lSh = pt(120, 168), rSh = pt(180, 168),
        lEl = pt(98, 215), rEl = pt(202, 215),
        lWr = pt(140, 196), rWr = pt(160, 196),
        lHip = pt(128, 262), rHip = pt(172, 262),
        lKn = pt(104, 292), rKn = pt(196, 292),
        lAn = pt(120, 350), rAn = pt(180, 350)
    )

    private val HANDS_ON_HIPS_BASE = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(92, 162), rEl = pt(208, 162),
        lWr = pt(128, 204), rWr = pt(172, 204),
        lHip = pt(133, 210), rHip = pt(167, 210),
        lKn = pt(132, 280), rKn = pt(168, 280),
        lAn = pt(132, 350), rAn = pt(168, 350)
    )

    private val KNEE_LEFT_UP = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(92, 162), rEl = pt(208, 162),
        lWr = pt(128, 204), rWr = pt(172, 204),
        lHip = pt(133, 210), rHip = pt(167, 210),
        lKn = pt(128, 228), rKn = pt(168, 280),
        lAn = pt(124, 290), rAn = pt(168, 350)
    )

    private val TREE_LEFT = Body(
        nose = pt(150, 70),
        lSh = pt(120, 110), rSh = pt(180, 110),
        lEl = pt(112, 58), rEl = pt(188, 58),
        lWr = pt(146, 14), rWr = pt(154, 14),
        lHip = pt(135, 210), rHip = pt(167, 210),
        lKn = pt(92, 258), rKn = pt(168, 280),
        lAn = pt(156, 284), rAn = pt(168, 350)
    )

    private val WARRIOR_LEFT = Body(
        nose = pt(150, 86),
        lSh = pt(120, 126), rSh = pt(180, 126),
        lEl = pt(78, 126), rEl = pt(222, 126),
        lWr = pt(34, 126), rWr = pt(266, 126),
        lHip = pt(132, 226), rHip = pt(168, 226),
        lKn = pt(88, 282), rKn = pt(210, 286),
        lAn = pt(84, 350), rAn = pt(246, 350)
    )

    private fun Body.mirror(): Body {
        fun m(p: Pt) = Pt(W - p.x, p.y)
        return Body(
            nose = m(nose),
            lSh = m(rSh), rSh = m(lSh), lEl = m(rEl), rEl = m(lEl), lWr = m(rWr), rWr = m(lWr),
            lHip = m(rHip), rHip = m(lHip), lKn = m(rKn), rKn = m(lKn), lAn = m(rAn), rAn = m(lAn)
        )
    }

    val standingPose: RecordedPose by lazy { STANDING.toPose() }

    private fun step(
        id: String, name: String, body: Body, type: StepType,
        duration: Float = 3f, reps: Int = 1
    ) = ExerciseStep(id = id, type = type, pose = body.toPose(), durationSec = duration, reps = reps, name = name)

    private fun pause(id: String, sec: Float) =
        ExerciseStep(id = id, type = StepType.PAUSE, durationSec = sec, name = "Pause")

    val all: List<Exercise> by lazy {
        listOf(
            Exercise(
                id = "builtin_squats",
                name = "Squats",
                description = "Descends en squat puis remonte, le plus vite possible.",
                emoji = "🦵",
                colorIndex = 0,
                rounds = 10,
                difficulty = Difficulty.NORMAL,
                builtIn = true,
                steps = listOf(
                    step("sq_1", "Debout", STANDING, StepType.INSTANT),
                    step("sq_2", "Squat", SQUAT, StepType.INSTANT)
                )
            ),
            Exercise(
                id = "builtin_jacks",
                name = "Jumping jacks",
                description = "Saute en ouvrant bras et jambes en étoile.",
                emoji = "⭐",
                colorIndex = 1,
                rounds = 12,
                difficulty = Difficulty.EASY,
                builtIn = true,
                steps = listOf(
                    step("jj_1", "Fermé", FEET_TOGETHER, StepType.INSTANT),
                    step("jj_2", "Étoile", STAR, StepType.INSTANT)
                )
            ),
            Exercise(
                id = "builtin_knees",
                name = "Montées de genoux",
                description = "Mains sur les hanches, monte un genou puis l'autre.",
                emoji = "🏃",
                colorIndex = 2,
                rounds = 10,
                difficulty = Difficulty.EASY,
                builtIn = true,
                steps = listOf(
                    step("kn_1", "Genou gauche", KNEE_LEFT_UP, StepType.INSTANT),
                    step("kn_2", "Genou droit", KNEE_LEFT_UP.mirror(), StepType.INSTANT)
                )
            ),
            Exercise(
                id = "builtin_wakeup",
                name = "Réveil musculaire",
                description = "Bras en croix, bras levés, squats et chaise.",
                emoji = "☀️",
                colorIndex = 3,
                rounds = 2,
                difficulty = Difficulty.NORMAL,
                builtIn = true,
                steps = listOf(
                    step("wu_1", "Bras en croix", T_POSE, StepType.HOLD, duration = 8f),
                    step("wu_2", "Bras levés", ARMS_UP, StepType.HOLD, duration = 8f),
                    step("wu_3", "Squats", SQUAT, StepType.INSTANT, reps = 6),
                    step("wu_4", "Chaise", SQUAT, StepType.HOLD, duration = 10f),
                    pause("wu_5", 10f)
                )
            ),
            Exercise(
                id = "builtin_yoga",
                name = "Yoga express",
                description = "Arbre et guerrier, des deux côtés. Respire !",
                emoji = "🧘",
                colorIndex = 4,
                rounds = 1,
                difficulty = Difficulty.HARD,
                builtIn = true,
                steps = listOf(
                    step("yo_1", "Arbre (gauche)", TREE_LEFT, StepType.HOLD, duration = 15f),
                    pause("yo_2", 5f),
                    step("yo_3", "Arbre (droite)", TREE_LEFT.mirror(), StepType.HOLD, duration = 15f),
                    pause("yo_4", 5f),
                    step("yo_5", "Guerrier (gauche)", WARRIOR_LEFT, StepType.HOLD, duration = 15f),
                    pause("yo_6", 5f),
                    step("yo_7", "Guerrier (droite)", WARRIOR_LEFT.mirror(), StepType.HOLD, duration = 15f)
                )
            )
        )
    }

    val templates: List<Pair<String, RecordedPose>> by lazy {
        listOf(
            "Debout" to STANDING.toPose(),
            "Bras en croix" to T_POSE.toPose(),
            "Bras levés" to ARMS_UP.toPose(),
            "Étoile" to STAR.toPose(),
            "Squat" to SQUAT.toPose(),
            "Mains hanches" to HANDS_ON_HIPS_BASE.toPose(),
            "Genou levé" to KNEE_LEFT_UP.toPose(),
            "Arbre" to TREE_LEFT.toPose(),
            "Guerrier" to WARRIOR_LEFT.toPose()
        )
    }
}
