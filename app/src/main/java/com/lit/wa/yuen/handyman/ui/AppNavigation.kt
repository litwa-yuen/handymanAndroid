package com.lit.wa.yuen.handyman.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lit.wa.yuen.handyman.models.Destinations
import com.lit.wa.yuen.handyman.viewmodels.AuthViewModel

@Composable
fun AppNavigation(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    // Use collectAsState to monitor the authentication state
    val state by viewModel.state.collectAsState()

    // Determine the start destination based on the initial state of the current user
    val startDestination = remember(state.currentUser) {
        if (state.currentUser != null) Destinations.HOME else Destinations.SIGN_IN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.SIGN_IN) {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate(Destinations.SIGN_UP) },
                onSignInSuccess = {
                    // Navigate to the main application screen and clear the back stack
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.SIGN_IN) { inclusive = true }
                    }
                },
                onNavigateToEmailSignIn = { navController.navigate(Destinations.EMAIL_SIGN_IN) }, // Navigates to the new Email login flow
                viewModel = viewModel
            )
        }

        composable(Destinations.SIGN_UP) {
            SignUpScreen(
                onNavigateToSignIn = { navController.navigate(Destinations.SIGN_IN) },
                onSignUpSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.SIGN_UP) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // New route for Email Sign-In Screen
        composable(Destinations.EMAIL_SIGN_IN) {
            EmailSignInScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignInSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.SIGN_IN) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // HOME destination launches MainScreen, which contains the bottom tabs (Search and Profile)
        composable(Destinations.HOME) {
            MainScreen(viewModel = viewModel)
        }

        // Removed Destinations.PROFILE route as Profile is now a tab inside MainScreen
    }

    // Global state change handler: ensures navigation is correct if the state changes
    // outside of a navigation event (e.g., initial auth check or sign-out from anywhere)
    LaunchedEffect(state.currentUser) {
        val currentRoute = navController.currentDestination?.route

        if (state.currentUser != null) {
            if (currentRoute != Destinations.HOME) {
                navController.navigate(Destinations.HOME) {
                    popUpTo(Destinations.SIGN_IN) { inclusive = true }
                }
            }
        } else {
            // User signed out, redirect to sign-in screen
            if (currentRoute != Destinations.SIGN_IN && currentRoute != Destinations.SIGN_UP && currentRoute != Destinations.EMAIL_SIGN_IN) {
                navController.navigate(Destinations.SIGN_IN) {
                    popUpTo(Destinations.HOME) { inclusive = true }
                }
            }
        }
    }
}