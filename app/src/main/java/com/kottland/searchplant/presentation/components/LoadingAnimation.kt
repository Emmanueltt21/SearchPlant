package com.kottland.searchplant.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Beautiful loading animation for plant scanning
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )
    
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            )
        ),
        label = "sweep"
    )
    
    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Plant-themed loading animation with multiple circles
 */
@Composable
fun PlantLoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    val infiniteTransition = rememberInfiniteTransition(label = "plant_loading")
    
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )
    
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )
    
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale3"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle
        Box(
            modifier = Modifier
                .size(size * scale1)
                .clip(CircleShape)
                .then(
                    Modifier.size(size * 0.8f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = this.size.minDimension / 2
                )
            }
        }
        
        // Middle circle
        Box(
            modifier = Modifier
                .size(size * scale2 * 0.6f)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.5f),
                    radius = this.size.minDimension / 2
                )
            }
        }
        
        // Inner circle
        Box(
            modifier = Modifier
                .size(size * scale3 * 0.3f)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = tertiaryColor.copy(alpha = 0.7f),
                    radius = this.size.minDimension / 2
                )
            }
        }
    }
}

/**
 * Pulsing dots loading animation
 */
@Composable
fun PulsingDotsAnimation(
    modifier: Modifier = Modifier,
    dotSize: Dp = 12.dp,
    dotSpacing: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val dots = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600,
                    delayMillis = index * 200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_$index"
        )
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { animatedScale ->
            Canvas(
                modifier = Modifier.size(dotSize)
            ) {
                drawCircle(
                    color = color.copy(alpha = animatedScale.value),
                    radius = (dotSize.toPx() / 2) * animatedScale.value
                )
            }
        }
    }
}

/**
 * Scanning line animation for camera preview
 */
@Composable
fun ScanningLineAnimation(
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    lineWidth: Dp = 2.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            )
        ),
        label = "offset"
    )
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val lineY = size.height * offsetY
        
        drawLine(
            color = lineColor,
            start = androidx.compose.ui.geometry.Offset(0f, lineY),
            end = androidx.compose.ui.geometry.Offset(size.width, lineY),
            strokeWidth = lineWidth.toPx(),
            cap = StrokeCap.Round
        )
    }
}