package com.duolearning.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duolearning.app.ui.navigation.Routes
import com.duolearning.app.ui.screens.home.HomeScreen
import com.duolearning.app.ui.screens.language.LanguagePickerScreen
import com.duolearning.app.ui.screens.learn.LessonScreen
import com.duolearning.app.ui.screens.practice.PracticeScreen
import com.duolearning.app.ui.screens.progress.ProgressScreen
import com.duolearning.app.ui.screens.settings.OfflinePacksScreen
import com.duolearning.app.ui.screens.settings.SettingsScreen
import com.duolearning.app.ui.screens.translate.SavedTranslationsScreen
import com.duolearning.app.ui.screens.translate.TranslateScreen
import com.duolearning.app.ui.theme.DuoGreen
import com.duolearning.app.ui.theme.DuoGrey

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Learn", Icons.Filled.School),
    Tab(Routes.TRANSLATE, "Translate", Icons.Filled.Translate),
    Tab(Routes.PRACTICE, "Practice", Icons.Filled.Style),
    Tab(Routes.PROGRESS, "Progress", Icons.Filled.Insights)
)

@Composable
fun DuoApp() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBar = currentRoute in tabs.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                DuoBottomBar(nav, currentRoute)
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStartLesson = { course, lessonId -> nav.navigate(Routes.lesson(course, lessonId)) },
                    onOpenTranslate = { nav.switchTab(Routes.TRANSLATE) },
                    onOpenPractice = { nav.switchTab(Routes.PRACTICE) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    onChooseCourse = { nav.navigate(Routes.languagePicker("learn")) }
                )
            }
            composable(Routes.TRANSLATE) {
                TranslateScreen(
                    onOpenSaved = { nav.navigate(Routes.SAVED) },
                    onPickLanguage = { mode -> nav.navigate(Routes.languagePicker(mode)) },
                    onStartLesson = { course, lessonId -> nav.navigate(Routes.lesson(course, lessonId)) }
                )
            }
            composable(Routes.PRACTICE) {
                PracticeScreen(
                    onStartLesson = { course, lessonId -> nav.navigate(Routes.lesson(course, lessonId)) },
                    onOpenSaved = { nav.navigate(Routes.SAVED) },
                    onChooseCourse = { nav.navigate(Routes.languagePicker("learn")) }
                )
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(onOpenSettings = { nav.navigate(Routes.SETTINGS) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenOfflinePacks = { nav.navigate(Routes.OFFLINE_PACKS) },
                    onChooseNative = { nav.navigate(Routes.languagePicker("native")) },
                    onChooseCourse = { nav.navigate(Routes.languagePicker("learn")) }
                )
            }
            composable(Routes.OFFLINE_PACKS) {
                OfflinePacksScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.SAVED) {
                SavedTranslationsScreen(
                    onBack = { nav.popBackStack() },
                    onPractice = { nav.switchTab(Routes.PRACTICE) }
                )
            }
            composable(
                route = Routes.LANGUAGE_PICKER,
                arguments = listOf(navArgument(Routes.ARG_MODE) {
                    type = NavType.StringType; defaultValue = "learn"
                })
            ) { entry ->
                LanguagePickerScreen(
                    mode = entry.arguments?.getString(Routes.ARG_MODE) ?: "learn",
                    onBack = { nav.popBackStack() },
                    onPicked = { nav.popBackStack() }
                )
            }
            composable(
                route = Routes.LESSON,
                arguments = listOf(
                    navArgument(Routes.ARG_COURSE) { type = NavType.StringType },
                    navArgument(Routes.ARG_LESSON_ID) { type = NavType.StringType }
                )
            ) { entry ->
                LessonScreen(
                    course = entry.arguments?.getString(Routes.ARG_COURSE).orEmpty(),
                    lessonId = entry.arguments?.getString(Routes.ARG_LESSON_ID).orEmpty(),
                    onExit = { nav.popBackStack() },
                    onNextLesson = { course, lessonId ->
                        nav.navigate(Routes.lesson(course, lessonId)) {
                            popUpTo(Routes.LESSON) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DuoBottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { nav.switchTab(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DuoGreen,
                    selectedTextColor = DuoGreen,
                    unselectedIconColor = DuoGrey,
                    unselectedTextColor = DuoGrey,
                    indicatorColor = DuoGreen.copy(alpha = 0.14f)
                )
            )
        }
    }
}

/** Tab switching that keeps a single copy of each tab and restores state. */
private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
