package com.example.tripshare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tripshare.navigation.ROUTE_DRIVER
import com.example.tripshare.navigation.ROUTE_LOGIN
import com.example.tripshare.navigation.ROUTE_ORGANISER
import com.example.tripshare.navigation.ROUTE_PASSENGER



@Composable
fun RoleSelectionScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Your Role",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Driver Button
            Button(
                onClick = { navController.navigate(ROUTE_DRIVER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Driver", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Passenger Button
            Button(
                onClick = { navController.navigate(ROUTE_PASSENGER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Passenger", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Organiser Button
            Button(
                onClick = { navController.navigate(ROUTE_ORGANISER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Organiser", fontSize = 18.sp)
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun RoleSelectionPreview() {
    RoleSelectionScreen(rememberNavController())
}
