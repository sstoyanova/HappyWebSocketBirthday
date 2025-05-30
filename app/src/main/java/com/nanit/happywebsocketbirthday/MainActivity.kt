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
import androidx.navigation.toRoute
import com.nanit.happywebsocketbirthday.presentation.birthday.BirthdayScreen
import com.nanit.happywebsocketbirthday.presentation.ipsetup.IpSetupScreen
import com.nanit.happywebsocketbirthday.ui.theme.HappyWebSocketBirthdayTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

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
                        startDestination = IpSetupScreen
                    ) {
                        composable<IpSetupScreen> {
                            IpSetupScreen(
                                onNavigateToBabyInfo = { name, dateOfBirth, theme ->
                                    navController.navigate(
                                        BirthdayScreenRoute(
                                            name = name,
                                            dateOfBirth = dateOfBirth,
                                            theme = theme
                                        )
                                    )
                                })
                        }
                        composable<BirthdayScreenRoute> {
                            val arguments = it.toRoute<BirthdayScreenRoute>()
                            val name = arguments.name
                            val dateOfBirth = arguments.dateOfBirth
                            val theme = arguments.theme
                            BirthdayScreen(name, dateOfBirth, theme)
                        }
                    }
                }
            }
        }
    }
}

@Serializable
object IpSetupScreen

@Serializable
data class BirthdayScreenRoute(
    val name: String,
    val dateOfBirth: Long,
    val theme: String
)