package com.editstudio.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.editstudio.app.domain.AspectRatio
import com.editstudio.app.domain.FrameRate
import com.editstudio.app.domain.VideoResolution
import com.editstudio.app.ui.editor.EditorScreen
import com.editstudio.app.ui.editor.EditorViewModel
import com.editstudio.app.ui.home.HomeScreen
import com.editstudio.app.ui.home.HomeViewModel
import com.editstudio.app.ui.store.StoreScreen
import com.editstudio.app.ui.store.StoreViewModel
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioDarkBg
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.StudioSurface
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Projects : Screen("projects", "Projects", Icons.Default.Folder)
    data object Store : Screen("store", "Store", Icons.Default.ShoppingBag)
    data object Editor : Screen("editor/{projectId}", "Editor", Icons.Default.AddCircle) {
        fun createRoute(projectId: String) = "editor/$projectId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isEditorActive = currentRoute?.startsWith("editor/") == true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = StudioDarkBg,
        bottomBar = {
            if (!isEditorActive) {
                NavigationBar(
                    containerColor = StudioSurface,
                    contentColor = TextPrimary
                ) {
                    val bottomItems = listOf(Screen.Projects, Screen.Store)
                    bottomItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudioCyberCyan,
                                selectedTextColor = StudioCyberCyan,
                                indicatorColor = StudioNeonPurple.copy(alpha = 0.25f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Projects.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Projects.route) {
                val homeViewModel = remember { HomeViewModel(context) }
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenProject = { projectId ->
                        navController.navigate(Screen.Editor.createRoute(projectId))
                    }
                )
            }

            composable(Screen.Store.route) {
                val storeViewModel = remember { StoreViewModel(context) }
                val homeViewModel = remember { HomeViewModel(context) }
                StoreScreen(
                    viewModel = storeViewModel,
                    onUseTemplate = { item ->
                        val newId = homeViewModel.createNewProject(
                            name = item.name,
                            aspectRatio = AspectRatio.PORTRAIT_9_16,
                            resolution = VideoResolution.FHD_1080P,
                            frameRate = FrameRate.FPS_30
                        )
                        navController.navigate(Screen.Editor.createRoute(newId))
                    }
                )
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                val editorViewModel = remember(projectId) {
                    EditorViewModel(context, projectId)
                }
                EditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
