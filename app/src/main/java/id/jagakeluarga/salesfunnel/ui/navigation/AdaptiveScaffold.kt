package id.jagakeluarga.salesfunnel.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Bottom nav bar on compact width (phones in portrait), navigation rail
 * on medium/expanded width (phones landscape, foldables, tablets).
 */
@Composable
fun AdaptiveScaffold(
    windowSizeClass: WindowSizeClass,
    current: Destination,
    onNavigate: (Destination) -> Unit,
    content: @Composable (Destination) -> Unit,
) {
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    if (useRail) {
        Row(Modifier) {
            NavigationRail {
                Destination.entries.forEach { dest ->
                    NavigationRailItem(
                        selected = dest == current,
                        onClick = { onNavigate(dest) },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
            Box(Modifier.padding(16.dp)) {
                AnimatedContent(
                    targetState = current,
                    transitionSpec = {
                        val maju = targetState.ordinal > initialState.ordinal
                        val masuk = fadeIn(
                            initialAlpha = 0.88f,
                            animationSpec = tween(360, easing = FastOutSlowInEasing),
                        ) + slideInHorizontally(
                            initialOffsetX = { width -> if (maju) width else -width },
                            animationSpec = tween(360, easing = FastOutSlowInEasing),
                        )
                        val keluar = fadeOut(
                            targetAlpha = 0.88f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                        ) + slideOutHorizontally(
                            targetOffsetX = { width -> if (maju) -width else width },
                            animationSpec = tween(360, easing = FastOutSlowInEasing),
                        )
                        masuk togetherWith keluar
                    },
                    label = "menu_transition_rail",
                ) { destination -> content(destination) }
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    Destination.entries.forEach { dest ->
                        NavigationBarItem(
                            selected = dest == current,
                            onClick = { onNavigate(dest) },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AnimatedContent(
                    targetState = current,
                    transitionSpec = {
                        val maju = targetState.ordinal > initialState.ordinal
                        val masuk = fadeIn(
                            initialAlpha = 0.88f,
                            animationSpec = tween(360, easing = FastOutSlowInEasing),
                        ) + slideInHorizontally(
                            initialOffsetX = { width -> if (maju) width else -width },
                            animationSpec = tween(360, easing = FastOutSlowInEasing),
                        )
                        val keluar = fadeOut(
                            targetAlpha = 0.88f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                        ) + slideOutHorizontally(
                            targetOffsetX = { width -> if (maju) -width else width },
                            animationSpec = tween(360, easing = FastOutSlowInEasing),
                        )
                        masuk togetherWith keluar
                    },
                    label = "menu_transition_bottom",
                ) { destination -> content(destination) }
            }
        }
    }
}
