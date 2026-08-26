package id.jagakeluarga.salesfunnel.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdaptiveScaffold(
    windowSizeClass: WindowSizeClass,
    current: Destination,
    onNavigate: (Destination) -> Unit,
    onQuickSearch: () -> Unit = {},
    headerSubtitle: String = "",
    content: @Composable (Destination) -> Unit,
) {
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val tabDestinations = Destination.entries.filter { it != Destination.SETTINGS }

    if (useRail) {
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                    .padding(vertical = 12.dp),
            ) {
                NavigationRail(containerColor = Color.Transparent) {
                    tabDestinations.forEach { dest ->
                        NavigationRailItem(
                            selected = dest == current,
                            onClick = { onNavigate(dest) },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = {
                                Text(
                                    dest.label,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                            ),
                        )
                    }
                }
            }
            Column(Modifier.fillMaxSize()) {
                GlobalHeader(current, headerSubtitle, onNavigate, onQuickSearch)
                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).weight(1f).fillMaxSize()) {
                    AnimatedMenuContent(current, "menu_transition_rail", content)
                }
            }
        }
    } else {
        Scaffold(
            topBar = { GlobalHeader(current, headerSubtitle, onNavigate, onQuickSearch) },
            bottomBar = { CozyBottomBar(current, onNavigate, tabDestinations) },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                AnimatedMenuContent(current, "menu_transition_bottom", content)
            }
        }
    }
}

@Composable
private fun AnimatedMenuContent(
    current: Destination,
    label: String,
    content: @Composable (Destination) -> Unit,
) {
    AnimatedContent(
        targetState = current,
        transitionSpec = {
            val maju = targetState.ordinal >= initialState.ordinal
            val masuk = fadeIn(tween(280, easing = FastOutSlowInEasing)) + slideInHorizontally(
                initialOffsetX = { width -> if (maju) width / 3 else -width / 3 },
                animationSpec = tween(320, easing = FastOutSlowInEasing),
            )
            val keluar = fadeOut(tween(220, easing = FastOutSlowInEasing)) + slideOutHorizontally(
                targetOffsetX = { width -> if (maju) -width / 4 else width / 4 },
                animationSpec = tween(280, easing = FastOutSlowInEasing),
            )
            masuk togetherWith keluar
        },
        label = label,
    ) { destination -> content(destination) }
}

@Composable
private fun CozyBottomBar(
    current: Destination,
    onNavigate: (Destination) -> Unit,
    tabDestinations: List<Destination>,
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.surface,
        tonalElevation = 0.dp,
    ) {
        tabDestinations.forEach { dest ->
            NavigationBarItem(
                selected = dest == current,
                onClick = { onNavigate(dest) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = {
                    Text(
                        dest.label,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
private fun GlobalHeader(
    current: Destination,
    headerSubtitle: String,
    onNavigate: (Destination) -> Unit,
    onQuickSearch: () -> Unit,
) {
    val dateText = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date()) }
    val colors = MaterialTheme.colorScheme
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(colors.primary, colors.secondary))),
        navigationIcon = {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = colors.onPrimary)
        },
        title = {
            Column {
                Text(
                    dateText,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    if (headerSubtitle.isBlank()) current.label else "${current.label} · $headerSubtitle",
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        },
        actions = {
            IconButton(onClick = onQuickSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Pencarian cepat")
            }
            IconButton(onClick = { onNavigate(Destination.BERANDA) }) {
                Icon(Icons.Filled.Home, contentDescription = "Beranda")
            }
            IconButton(onClick = { onNavigate(Destination.SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Pengaturan")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = colors.onPrimary,
            navigationIconContentColor = colors.onPrimary,
            actionIconContentColor = colors.onPrimary,
        ),
    )
}
