package com.skyrenegades.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onPlayClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SKY RENEGADES",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Outlaws of the upper sky",
            color = Color(0xFF9AA5B1),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        Button(onClick = onPlayClicked) {
            Text("PLAY", fontSize = 18.sp)
        }
    }
}
