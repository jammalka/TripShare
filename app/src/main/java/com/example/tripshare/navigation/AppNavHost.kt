package com.example.tripshare.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tripshare.screens.register.RegisterScreen
import com.example.tripshare.ui.screens.AddRideScreen
import com.example.tripshare.ui.screens.DriverDashboardScreen
import com.example.tripshare.ui.screens.MyBookingsScreen
import com.example.tripshare.ui.screens.PassengerDashboardScreen
import com.example.tripshare.ui.screens.RoleSelectionScreen
import com.example.tripshare.ui.screens.UpdateRideScreen
import com.example.tripshare.ui.theme.screens.SplashScreen
import com.example.tripshare.ui.theme.screens.login.LoginScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(ROUTE_SPLASH) {
            SplashScreen {
                navController.navigate(ROUTE_REGISTER) {
                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                }
            }
        }

        composable(ROUTE_REGISTER) { RegisterScreen(navController) }
        composable(ROUTE_LOGIN) { LoginScreen(navController) }
        composable(ROUTE_ROLE) { RoleSelectionScreen(navController) }
        composable(ROUTE_DRIVER) { DriverDashboardScreen(navController) }
        composable(ROUTE_ADD_RIDE) { AddRideScreen(navController) }
        composable ( ROUTE_PASSENGER ){ PassengerDashboardScreen(navController=navController, userId = "passenger") }
        composable (ROUTE_BOOKINGS ){ MyBookingsScreen(navController=navController, userId = "My_Bookings") }
        composable(
            route = "$ROUTE_UPDATE_RIDE/{rideId}",
            arguments = listOf(navArgument("rideId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: return@composable
            UpdateRideScreen(navController = navController, rideId = rideId)
        }
    }
}
