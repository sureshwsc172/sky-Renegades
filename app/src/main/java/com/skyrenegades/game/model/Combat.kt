package com.skyrenegades.game.model

enum class EnemyType(val health: Int, val baseSpeed: Float, val size: Float, val scoreValue: Int) {
    DRONE(health = 1, baseSpeed = 5f, size = 44f, scoreValue = 5),
    GUNSHIP(health = 3, baseSpeed = 3f, size = 64f, scoreValue = 15),
    TURRET(health = 2, baseSpeed = 1.5f, size = 56f, scoreValue = 10),
    BOSS(health = 25, baseSpeed = 1.2f, size = 140f, scoreValue = 200)
}

data class Enemy(
    var x: Float,
    var y: Float,
    val type: EnemyType,
    var health: Int = type.health,
    var lastShotFrame: Int = 0,
    var horizontalDir: Float = 1f
)

data class PlayerBullet(
    var x: Float,
    var y: Float
)

data class EnemyBullet(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 8f
)
