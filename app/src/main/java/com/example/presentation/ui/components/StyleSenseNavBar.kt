package com.example.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.presentation.viewmodel.AppDestination

@Composable
fun StyleSenseNavBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("stylesense_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        NavigationBarItem(
            selected = currentDestination == AppDestination.LIVE_CAMERA || currentDestination == AppDestination.ONBOARDING || currentDestination == AppDestination.MANUAL_INPUT,
            onClick = { onNavigate(AppDestination.ONBOARDING) },
            icon = { Icon(Icons.Default.Face, contentDescription = "Face Biometrics") },
            label = { Text("Color Scan") },
            modifier = Modifier.testTag("nav_scan"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            selected = currentDestination == AppDestination.RESULT_PALETTE,
            onClick = { onNavigate(AppDestination.RESULT_PALETTE) },
            icon = { Icon(Icons.Default.ColorLens, contentDescription = "12-Season Palette") },
            label = { Text("Palette") },
            modifier = Modifier.testTag("nav_palette"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            selected = currentDestination == AppDestination.CAPSULE_WARDROBE,
            onClick = { onNavigate(AppDestination.CAPSULE_WARDROBE) },
            icon = { Icon(Icons.Default.AutoAwesomeMosaic, contentDescription = "Capsule Wardrobe") },
            label = { Text("Capsule") },
            modifier = Modifier.testTag("nav_capsule"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            selected = currentDestination == AppDestination.WARDROBE_CLOSET,
            onClick = { onNavigate(AppDestination.WARDROBE_CLOSET) },
            icon = { Icon(Icons.Default.Checkroom, contentDescription = "Digital Wardrobe") },
            label = { Text("Closet") },
            modifier = Modifier.testTag("nav_closet"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )

        NavigationBarItem(
            selected = currentDestination == AppDestination.OUTFIT_EVALUATION,
            onClick = { onNavigate(AppDestination.OUTFIT_EVALUATION) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Outfit Matcher") },
            label = { Text("Match Event") },
            modifier = Modifier.testTag("nav_match"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
