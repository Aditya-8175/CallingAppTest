package com.surefy.connects.presentation.screen.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {

    Column (horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,  modifier = Modifier.fillMaxSize().background(Color(0xFFFFFFFF))) {

        Text(text = "", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}