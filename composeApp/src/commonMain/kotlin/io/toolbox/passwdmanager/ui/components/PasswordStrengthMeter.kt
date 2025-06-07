package io.toolbox.passwdmanager.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.toolbox.passwdmanager.utils.PasswordStrength

@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier
) {
    var strength by remember { mutableStateOf(0.0) }
    var crackTime by remember { mutableStateOf("calculating...") }
    
    LaunchedEffect(password) {
        strength = PasswordStrength.calculateStrength(password)
        crackTime = PasswordStrength.estimateCrackTime(password)
    }
    
    val animatedStrength by animateFloatAsState(
        targetValue = strength.toFloat(),
        label = "strength"
    )
    
    val strengthColor = when {
        strength < 30 -> Color.Red
        strength < 60 -> Color(0xFFFFA500) // Orange
        strength < 80 -> Color(0xFFFFD700) // Yellow
        else -> Color.Green
    }
    
    Column(modifier = modifier) {
        // Strength bar
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(animatedStrength / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(strengthColor)
            )
        }
        
        // Crack time estimation
        Text(
            text = "Estimated crack time: $crackTime",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 