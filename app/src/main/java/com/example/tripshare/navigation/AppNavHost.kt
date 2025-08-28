package com.example.tripshare.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tripshare.screens.login.LoginScreen
import com.example.tripshare.screens.register.RegisterScreen
import com.example.tripshare.ui.screens.AddRideScreen
import com.example.tripshare.ui.screens.DriverDashboardScreen
import com.example.tripshare.ui.screens.MyBookingsScreen
import com.example.tripshare.ui.screens.PassengerDashboardScreen
import com.example.tripshare.ui.screens.RoleSelectionScreen
import com.example.tripshare.ui.screens.UpdateRideScreen
import com.example.tripshare.ui.theme.screens.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // Splash -> Register
        composable(ROUTE_SPLASH) {
            SplashScreen {
                navController.navigate(ROUTE_REGISTER) {
                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                }
            }
        }

        // Auth Screens
        composable(ROUTE_REGISTER) { RegisterScreen(navController) }
        composable(ROUTE_LOGIN) { LoginScreen(navController) }
        composable(ROUTE_ROLE) { RoleSelectionScreen(navController) }

        // Driver Screens
        composable(ROUTE_DRIVER) { DriverDashboardScreen(navController) }
        composable(ROUTE_ADD_RIDE) { AddRideScreen(navController) }

        // Passenger Screens
        composable(
            route = "$ROUTE_PASSENGER?userId={userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = "passenger"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "passenger"
            PassengerDashboardScreen(navController = navController, userId = userId)
        }

        // My Bookings
        composable(
            route = "$ROUTE_BOOKINGS?userId={userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = "passenger"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "passenger"
            MyBookingsScreen(navController = navController, userId = userId)
        }

        // Update Ride
        composable(
            route = "$ROUTE_UPDATE_RIDE/{rideId}",
            arguments = listOf(
                navArgument("rideId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId") ?: return@composable
            UpdateRideScreen(navController = navController, rideId = rideId)
        }

        // Organiser Dashboard (placeholder)
        composable(ROUTE_ORGANISER) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Organiser Dashboard Coming Soon")
            }
        }
    }
}
