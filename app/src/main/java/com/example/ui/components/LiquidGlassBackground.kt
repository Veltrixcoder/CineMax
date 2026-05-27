package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kashif_e.backdrop.backdrops.rememberLayerBackdrop
import com.kashif_e.backdrop.drawPlainBackdrop

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // We set up infinite animations for fluid motion of our neon shapes
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_motion")

    // Animating shape 1 (Cyan/Teal)
    val float1X = infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cyan_x"
    )
    val float1Y = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cyan_y"
    )

    // Animating shape 2 (Magenta/Purple)
    val float2X = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "magenta_x"
    )
    val float2Y = infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "magenta_y"
    )

    // Animating shape 3 (Royal Violet/Blue)
    val float3X = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "violet_x"
    )
    val float3Y = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "violet_y"
    )

    // Animating shape 4 (Amber/Pink)
    val float4X = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amber_x"
    )
    val float4Y = infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amber_y"
    )

    // The backdrop renders the animated layout to a layer internally
    val backdrop = rememberLayerBackdrop {
        val width = size.width
        val height = size.height

        // Circle 1: Cyan/Teal
        val center1 = Offset(width * float1X.value, height * float1Y.value)
        val r1 = width * 0.4f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00ADB5),
                    Color(0xFF00ADB5).copy(alpha = 0.4f),
                    Color.Transparent
                ),
                center = center1,
                radius = r1
            ),
            radius = r1,
            center = center1
        )

        // Circle 2: Fuchsia/Magenta
        val center2 = Offset(width * float2X.value, height * float2Y.value)
        val r2 = width * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF2E93),
                    Color(0xFFFF2E93).copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = center2,
                radius = r2
            ),
            radius = r2,
            center = center2
        )

        // Circle 3: Bright Royal Blue/Violet
        val center3 = Offset(width * float3X.value, height * float3Y.value)
        val r3 = width * 0.35f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF3F3B6C),
                    Color(0xFF3F3B6C).copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = center3,
                radius = r3
            ),
            radius = r3,
            center = center3
        )

        // Circle 4: Amber/Orange Highlights
        val center4 = Offset(width * float4X.value, height * float4Y.value)
        val r4 = width * 0.32f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF7E36),
                    Color(0xFFFF7E36).copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = center4,
                radius = r4
            ),
            radius = r4,
            center = center4
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Render backdrop behind our content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawPlainBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(0.dp) },
                    effects = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(55f, 55f, android.graphics.Shader.TileMode.CLAMP)
                        }
                    }
                )
        )
        
        // Main Screen overlay content fits over the blurred animated backdrop
        content()
    }
}
