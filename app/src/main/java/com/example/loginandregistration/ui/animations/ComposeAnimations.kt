package com.example.loginandregistration.ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Standard animation durations for consistent timing across the app */
object AnimationDurations {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
}

/** Standard enter/exit transitions for screens */
object ScreenTransitions {
    val slideInFromRight: EnterTransition =
            slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec =
                            tween(
                                    durationMillis = AnimationDurations.NORMAL,
                                    easing = FastOutSlowInEasing
                            )
            ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.NORMAL))

    val slideOutToLeft: ExitTransition =
            slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec =
                            tween(
                                    durationMillis = AnimationDurations.NORMAL,
                                    easing = FastOutSlowInEasing
                            )
            ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.NORMAL))

    val slideInFromBottom: EnterTransition =
            slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec =
                            tween(
                                    durationMillis = AnimationDurations.NORMAL,
                                    easing = FastOutSlowInEasing
                            )
            ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.NORMAL))

    val slideOutToBottom: ExitTransition =
            slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec =
                            tween(
                                    durationMillis = AnimationDurations.NORMAL,
                                    easing = FastOutSlowInEasing
                            )
            ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.NORMAL))
}

/** Standard enter/exit transitions for list items */
object ListItemTransitions {
    val expandIn: EnterTransition =
            expandVertically(
                    animationSpec =
                            tween(
                                    durationMillis = AnimationDurations.FAST,
                                    easing = FastOutSlowInEasing
                            )
            ) + fadeIn(animationSpec = tween(durationMillis = AnimationDurations.FAST))

    val collapseOut: ExitTransition =
            shrinkVertically(
                    animationSpec =
                            tween(
                                    durationMillis = AnimationDurations.FAST,
                                    easing = FastOutSlowInEasing
                            )
            ) + fadeOut(animationSpec = tween(durationMillis = AnimationDurations.FAST))
}

/** Animated visibility wrapper with standard transitions */
@Composable
fun AnimatedListItem(
        visible: Boolean,
        modifier: Modifier = Modifier,
        content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
            visible = visible,
            enter = ListItemTransitions.expandIn,
            exit = ListItemTransitions.collapseOut,
            modifier = modifier,
            content = content
    )
}

/** Shimmer loading effect for skeleton screens */
@Composable
fun ShimmerLoadingAnimation(
        modifier: Modifier = Modifier,
        shimmerColors: List<Color> =
                listOf(
                        Color.LightGray.copy(alpha = 0.6f),
                        Color.LightGray.copy(alpha = 0.2f),
                        Color.LightGray.copy(alpha = 0.6f)
                )
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by
            transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1000f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                            ),
                    label = "shimmer_translate"
            )

    val brush =
            Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim - 1000f, translateAnim - 1000f),
                    end = Offset(translateAnim, translateAnim)
            )

    Box(modifier = modifier.background(brush))
}

/** Shimmer loading placeholder for list items */
@Composable
fun ShimmerListItem(modifier: Modifier = Modifier) {
    ShimmerLoadingAnimation(modifier = modifier.fillMaxWidth().height(80.dp))
}

/** Modifier extension for shimmer effect */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by
            transition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.9f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation =
                                            tween(
                                                    durationMillis = 1000,
                                                    easing = FastOutSlowInEasing
                                            ),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "shimmer_alpha"
            )

    this.background(Color.LightGray.copy(alpha = alpha))
}

/** Pulse animation for attention-grabbing elements */
fun Modifier.pulseAnimation(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by
            transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation =
                                            tween(
                                                    durationMillis = 800,
                                                    easing = FastOutSlowInEasing
                                            ),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "pulse_scale"
            )

    this.then(
            Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}

/** Helper to apply graphicsLayer transformations */
private fun Modifier.graphicsLayer(
        block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit
): Modifier {
    return this.then(androidx.compose.ui.Modifier.graphicsLayer(block))
}
