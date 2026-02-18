package com.espaciotiago.platziflixandroid.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.espaciotiago.platziflixandroid.di.AppModule
import com.espaciotiago.platziflixandroid.presentation.courses.detail.screen.CourseDetailScreen
import com.espaciotiago.platziflixandroid.presentation.courses.detail.viewmodel.CourseDetailViewModel
import com.espaciotiago.platziflixandroid.presentation.courses.screen.CourseListScreen
import com.espaciotiago.platziflixandroid.presentation.courses.viewmodel.CourseListViewModel

private const val ROUTE_COURSES = "courses"
private const val ROUTE_COURSE_DETAIL = "courses/{slug}"
private const val ARG_SLUG = "slug"

/**
 * Root navigation graph for the PlatziFlixAndroid app
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_COURSES
    ) {
        composable(ROUTE_COURSES) {
            val viewModel = viewModel<CourseListViewModel> {
                AppModule.provideCourseListViewModel()
            }
            CourseListScreen(
                viewModel = viewModel,
                onCourseClick = { course ->
                    navController.navigate("courses/${course.slug}")
                }
            )
        }

        composable(
            route = ROUTE_COURSE_DETAIL,
            arguments = listOf(navArgument(ARG_SLUG) { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString(ARG_SLUG) ?: return@composable
            val viewModel = viewModel<CourseDetailViewModel>(key = slug) {
                AppModule.provideCourseDetailViewModel(slug)
            }
            CourseDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
