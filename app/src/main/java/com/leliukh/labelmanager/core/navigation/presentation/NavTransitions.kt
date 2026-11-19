package com.leliukh.labelmanager.core.navigation.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

private const val PUSH_DURATION_MS = 380
private const val PARALLAX_DIVISOR = 4
private val PushEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

/**
 * Push: the new screen slides in from the end while the old one drifts a quarter of
 * the way back and fades, which reads as depth rather than a flat swap.
 */
fun <T : Any> pushTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
    {
        slideInHorizontally(tween(PUSH_DURATION_MS, easing = PushEasing)) { it } togetherWith
                slideOutHorizontally(tween(PUSH_DURATION_MS, easing = PushEasing)) {
                    -it / PARALLAX_DIVISOR
                } + fadeOut(tween(PUSH_DURATION_MS))
    }

/**
 * The reverse of [pushTransition], used for back navigation.
 */
fun <T : Any> popTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
    {
        slideInHorizontally(tween(PUSH_DURATION_MS, easing = PushEasing)) {
            -it / PARALLAX_DIVISOR
        } + fadeIn(tween(PUSH_DURATION_MS)) togetherWith
                slideOutHorizontally(tween(PUSH_DURATION_MS, easing = PushEasing)) { it }
    }

/**
 * [popTransition] driven by the predictive back gesture, so the previous screen follows the
 * finger.
 */
fun <T : Any> predictivePopTransition():
        AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform =
    { _ ->
        popTransition<T>().invoke(this)
    }
