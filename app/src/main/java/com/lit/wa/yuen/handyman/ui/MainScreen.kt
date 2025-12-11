package com.lit.wa.yuen.handyman.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.lit.wa.yuen.handyman.viewmodels.AuthViewModel

// Data class to define the items in the bottom navigation bar
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Search : BottomNavItem("search", Icons.Default.Search, "Search")
    data object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}

@Composable
fun MainScreen(viewModel: AuthViewModel) {
    // List of navigation items
    val items = listOf(BottomNavItem.Search, BottomNavItem.Profile)

    // State to track the currently selected route (tab)
    var selectedItem by remember { mutableStateOf(BottomNavItem.Search.route) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = selectedItem == item.route
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = { selectedItem = item.route }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Content based on the selected tab
            when (selectedItem) {
                BottomNavItem.Search.route -> SearchScreen()
                BottomNavItem.Profile.route -> ProfileScreen(viewModel)
            }
        }
    }
}