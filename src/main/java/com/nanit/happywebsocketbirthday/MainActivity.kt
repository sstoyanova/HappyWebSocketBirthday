package com.nanit.happywebsocketbirthday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nanit.happywebsocketbirthday.presentation.birthday.BirthdayScreen
import com.nanit.happywebsocketbirthday.presentation.ipsetup.IpAddressSetupScreen
import com.nanit.happywebsocketbirthday.ui.theme.HappyWebSocketBirthdayTheme
import dagger.hilt.android.AndroidEntryPoint

// Define your routes
object AppDestinations {
    const val IP_ADDRESS_ROUTE = "ip_address"
    const val BABY_INFO_ROUTE = "baby_info"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HappyWebSocketBirthdayTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = AppDestinations.IP_ADDRESS_ROUTE
                    ) {
                        composable(AppDestinations.IP_ADDRESS_ROUTE) {
                            IpAddressSetupScreen(
                                onNavigateToBabyInfo = {
                                    navController.navigate(AppDestinations.BABY_INFO_ROUTE)
                                }
                            )
                        }
                        composable(
                            route = AppDestinations.BABY_INFO_ROUTE
                        ) {
                            // Display the BabyInfoScreen with the data
                            BirthdayScreen()
                        }
                    }
                }
            }
        }
    }
}