package com.skyrenegades.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameOverScreen(
    score: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MISSION FAILED",
            color = Color(0xFFE63946),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Score: $score",
            color = Color.White,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onPlayAgain) {
            Text("PLAY AGAIN")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onHome) {
            Text("HOME")
        }
    }
}
