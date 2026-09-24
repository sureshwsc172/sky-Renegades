package com.skyrenegades.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skyrenegades.game.model.Enemy
import com.skyrenegades.game.model.EnemyBullet
import com.skyrenegades.game.model.EnemyType
import com.skyrenegades.game.model.PlayerBullet
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val BOSS_SCORE_THRESHOLD = 150
private const val PLAYER_MAX_HEALTH = 3
private const val PLAYER_FIRE_INTERVAL_FRAMES = 12 // auto-fire rate
private const val INVULNERABILITY_FRAMES = 60

@Composable
fun GameScreen(onGameOver: (Int) -> Unit) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    var playerX by remember { mutableFloatStateOf(0f) }
    val playerSize = with(LocalDensity.current) { 48.dp.toPx() }

    val enemies = remember { mutableStateListOf<Enemy>() }
    val playerBullets = remember { mutableStateListOf<PlayerBullet>() }
    val enemyBullets = remember { mutableStateListOf<EnemyBullet>() }

    var score by remember { mutableIntStateOf(0) }
    var playerHealth by remember { mutableIntStateOf(PLAYER_MAX_HEALTH) }
    var invulnerableFrames by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var elapsedFrames by remember { mutableIntStateOf(0) }
    var bossActive by remember { mutableStateOf(false) }
    var bossSpawned by remember { mutableStateOf(false) }

    LaunchedEffect(canvasWidth, canvasHeight) {
        if (canvasWidth == 0f || canvasHeight == 0f) return@LaunchedEffect
        playerX = canvasWidth / 2f

        while (!isGameOver) {
            delay(16L) // ~60fps
            elapsedFrames++
            if (invulnerableFrames > 0) invulnerableFrames--

            val playerTop = canvasHeight - playerSize - 32f
            val playerCenterX = playerX
            val playerCenterY = playerTop + playerSize / 2f

            // --- Spawning ---
            if (!bossActive) {
                if (score >= BOSS_SCORE_THRESHOLD && !bossSpawned) {
                    enemies.clear()
                    enemyBullets.clear()
                    enemies.add(
                        Enemy(
                            x = canvasWidth / 2f - EnemyType.BOSS.size / 2f,
                            y = -EnemyType.BOSS.size,
                            type = EnemyType.BOSS
                        )
                    )
                    bossActive = true
                    bossSpawned = true
                } else if (elapsedFrames % 50 == 0) {
                    val roll = Random.nextFloat()
                    val type = when {
                        roll < 0.5f -> EnemyType.DRONE
                        roll < 0.8f -> EnemyType.GUNSHIP
                        else -> EnemyType.TURRET
                    }
                    val x = Random.nextFloat() * (canvasWidth - type.size)
                    enemies.add(Enemy(x = x, y = -type.size, type = type))
                }
            }

            // --- Auto-fire player weapon ---
            if (elapsedFrames % PLAYER_FIRE_INTERVAL_FRAMES == 0) {
                playerBullets.add(PlayerBullet(x = playerCenterX, y = playerTop))
            }

            // --- Move player bullets ---
            playerBullets.forEach { it.y -= 18f }
            playerBullets.removeAll { it.y < -20f }

            // --- Move + behavior per enemy type ---
            for (e in enemies) {
                when (e.type) {
                    EnemyType.DRONE -> {
                        e.y += e.type.baseSpeed + (score / 40f)
                    }
                    EnemyType.GUNSHIP -> {
                        e.y += e.type.baseSpeed + (score / 50f)
                        if (elapsedFrames - e.lastShotFrame > 70) {
                            enemyBullets.add(EnemyBullet(x = e.x + e.type.size / 2f, y = e.y + e.type.size, vx = 0f, vy = 7f))
                            e.lastShotFrame = elapsedFrames
                        }
                    }
                    EnemyType.TURRET -> {
                        e.y += e.type.baseSpeed
                        e.x += e.horizontalDir * 2f
                        if (e.x < 0f || e.x + e.type.size > canvasWidth) {
                            e.horizontalDir *= -1f
                        }
                        if (elapsedFrames - e.lastShotFrame > 90) {
                            val dx = playerCenterX - (e.x + e.type.size / 2f)
                            val dy = playerCenterY - (e.y + e.type.size / 2f)
                            val angle = atan2(dy, dx)
                            val speed = 7f
                            enemyBullets.add(
                                EnemyBullet(
                                    x = e.x + e.type.size / 2f,
                                    y = e.y + e.type.size / 2f,
                                    vx = cos(angle) * speed,
                                    vy = sin(angle) * speed
                                )
                            )
                            e.lastShotFrame = elapsedFrames
                        }
                    }
                    EnemyType.BOSS -> {
                        if (e.y < 60f) {
                            e.y += 3f
                        } else {
                            e.x += e.horizontalDir * 2.5f
                            if (e.x < 0f || e.x + e.type.size > canvasWidth) {
                                e.horizontalDir *= -1f
                            }
                        }
                        if (elapsedFrames - e.lastShotFrame > 55 && e.y >= 60f) {
                            val originX = e.x + e.type.size / 2f
                            val originY = e.y + e.type.size
                            for (spread in -1..1) {
                                enemyBullets.add(
                                    EnemyBullet(
                                        x = originX,
                                        y = originY,
                                        vx = spread * 3.5f,
                                        vy = 7f
                                    )
                                )
                            }
                            e.lastShotFrame = elapsedFrames
                        }
                    }
                }
            }

            // --- Move enemy bullets ---
            enemyBullets.forEach {
                it.x += it.vx
                it.y += it.vy
            }
            enemyBullets.removeAll { it.y > canvasHeight + 20f || it.x < -20f || it.x > canvasWidth + 20f }

            // --- Player bullet vs enemy collision ---
            val bulletIterator = playerBullets.iterator()
            while (bulletIterator.hasNext()) {
                val b = bulletIterator.next()
                var hit = false
                for (e in enemies) {
                    if (b.x in e.x..(e.x + e.type.size) && b.y in e.y..(e.y + e.type.size)) {
                        e.health--
                        hit = true
                        break
                    }
                }
                if (hit) bulletIterator.remove()
            }

            // --- Remove dead enemies, award score ---
            val deadEnemies = enemies.filter { it.health <= 0 }
            for (dead in deadEnemies) {
                score += dead.type.scoreValue
                if (dead.type == EnemyType.BOSS) {
                    bossActive = false
                }
            }
            enemies.removeAll { it.health <= 0 }

            // --- Remove off-screen enemies (missed), small score for surviving them ---
            val passedEnemies = enemies.filter { it.y > canvasHeight && it.type != EnemyType.BOSS }
            for (passed in passedEnemies) {
                score += 1
            }
            enemies.removeAll { it.y > canvasHeight && it.type != EnemyType.BOSS }

            // --- Enemy bullet vs player collision ---
            if (invulnerableFrames == 0) {
                val pLeft = playerX - playerSize / 2f
                val pRight = playerX + playerSize / 2f
                val pTop = playerTop
                val pBottom = playerTop + playerSize

                val hitBullet = enemyBullets.firstOrNull { b ->
                    b.x in pLeft..pRight && b.y in pTop..pBottom
                }
                if (hitBullet != null) {
                    enemyBullets.remove(hitBullet)
                    playerHealth--
                    invulnerableFrames = INVULNERABILITY_FRAMES
                    if (playerHealth <= 0) isGameOver = true
                }
            }

            // --- Enemy body vs player collision ---
            if (invulnerableFrames == 0) {
                val pLeft = playerX - playerSize / 2f
                val pRight = playerX + playerSize / 2f
                val pTop = playerTop
                val pBottom = playerTop + playerSize

                for (e in enemies) {
                    val eLeft = e.x
                    val eRight = e.x + e.type.size
                    val eTop = e.y
                    val eBottom = e.y + e.type.size

                    val overlapsX = eLeft < pRight && eRight > pLeft
                    val overlapsY = eTop < pBottom && eBottom > pTop

                    if (overlapsX && overlapsY) {
                        playerHealth -= if (e.type == EnemyType.BOSS) PLAYER_MAX_HEALTH else 1
                        invulnerableFrames = INVULNERABILITY_FRAMES
                        if (playerHealth <= 0) isGameOver = true
                        break
                    }
                }
            }
        }
    }

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            delay(200)
            onGameOver(score)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10151C))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    playerX = (playerX + dragAmount.x).coerceIn(
                        playerSize / 2f,
                        canvasWidth - playerSize / 2f
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasWidth = size.width
            canvasHeight = size.height

            val lineSpacing = 120f
            val offset = (elapsedFrames * 6f) % lineSpacing
            var y = -lineSpacing + offset
            while (y < size.height) {
                drawRect(
                    color = Color(0xFF1B2733),
                    topLeft = Offset(0f, y),
                    size = Size(size.width, 4f)
                )
                y += lineSpacing
            }

            for (e in enemies) {
                val color = when (e.type) {
                    EnemyType.DRONE -> Color(0xFFE76F51)
                    EnemyType.GUNSHIP -> Color(0xFFF4A261)
                    EnemyType.TURRET -> Color(0xFF2A9D8F)
                    EnemyType.BOSS -> Color(0xFF9D0208)
                }
                drawRect(color = color, topLeft = Offset(e.x, e.y), size = Size(e.type.size, e.type.size))

                if (e.type == EnemyType.BOSS) {
                    val barWidth = e.type.size
                    val healthRatio = (e.health.toFloat() / EnemyType.BOSS.health).coerceIn(0f, 1f)
                    drawRect(
                        color = Color.DarkGray,
                        topLeft = Offset(e.x, e.y - 16f),
                        size = Size(barWidth, 8f)
                    )
                    drawRect(
                        color = Color(0xFF06D6A0),
                        topLeft = Offset(e.x, e.y - 16f),
                        size = Size(barWidth * healthRatio, 8f)
                    )
                }
            }

            for (b in playerBullets) {
                drawRect(
                    color = Color(0xFFFFD166),
                    topLeft = Offset(b.x - 4f, b.y - 12f),
                    size = Size(8f, 16f)
                )
            }

            for (b in enemyBullets) {
                drawRect(
                    color = Color(0xFFEF476F),
                    topLeft = Offset(b.x - 5f, b.y - 5f),
                    size = Size(10f, 10f)
                )
            }

            val playerTop = size.height - playerSize - 32f
            val playerColor = if (invulnerableFrames > 0 && (elapsedFrames / 4) % 2 == 0) {
                Color(0xFF00B4D8).copy(alpha = 0.3f)
            } else {
                Color(0xFF00B4D8)
            }
            drawRect(
                color = playerColor,
                topLeft = Offset(playerX - playerSize / 2f, playerTop),
                size = Size(playerSize, playerSize)
            )
        }

        Text(
            text = "Score: $score",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Text(
            text = "Health: ${"❤".repeat(playerHealth.coerceAtLeast(0))}",
            color = Color(0xFFEF476F),
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        if (bossActive) {
            Text(
                text = "⚠ BOSS INCOMING ⚠",
                color = Color(0xFFFFD166),
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}
