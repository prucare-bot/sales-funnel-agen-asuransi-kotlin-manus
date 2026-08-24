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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    onQuickSearch: () -> Unit = {},
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
            Column(Modifier.fillMaxSize()) {
                if (current != Destination.BERANDA) GlobalHeader(current, onNavigate, onQuickSearch)
                Box(Modifier.padding(16.dp).weight(1f)) {
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
            topBar = { if (current != Destination.BERANDA) GlobalHeader(current, onNavigate, onQuickSearch) },
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


@Composable
private fun GlobalHeader(
    current: Destination,
    onNavigate: (Destination) -> Unit,
    onQuickSearch: () -> Unit,
) {
    val dateText = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date()) }
    val colors = androidx.compose.material3.MaterialTheme.colorScheme
    TopAppBar(
        modifier = Modifier.fillMaxWidth().background(
            Brush.horizontalGradient(listOf(colors.primary, colors.secondary))
        ),
        title = {
            Column {
                Text(dateText, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(current.label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        },
        actions = {
            IconButton(onClick = onQuickSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Pencarian cepat")
            }
            IconButton(onClick = { onNavigate(Destination.BERANDA) }) {
                Icon(Icons.Filled.Home, contentDescription = "Beranda")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = colors.onPrimary,
            actionIconContentColor = colors.onPrimary,
        ),
    )
}
