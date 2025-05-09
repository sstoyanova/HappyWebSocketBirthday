package com.nanit.happywebsocketbirthday

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nanit.happywebsocketbirthday.presentation.BabyState
import com.nanit.happywebsocketbirthday.presentation.BabyViewModel
import com.nanit.happywebsocketbirthday.ui.theme.HappyWebSocketBirthdayTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: BabyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HappyWebSocketBirthdayTheme {
                IpAddressLayout()
            }
        }
    }

    @Composable
    fun IpAddressLayout() {
        val validationResult by remember { mutableStateOf(ValidationResult(true)) }
        val uiState: BabyState by viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current

        val isIpPortValid = validationResult.isValid && uiState.ipPort.isNotEmpty()
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 40.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                //A text box that displays the string value you pass here.
                value = uiState.ipPort,
                //The lambda callback that's triggered when the user enters text in the text box.
                onValueChange = { newValue ->
                    viewModel.updateIpPort(newValue) // Update the ipPort on the ViewModel
                    viewModel.updateValidationResult(isValidIpPortFormat(newValue)) // Update the validationResult on the ViewModel
                }, // Updates the state when user types
                label = { Text("IP : Port") },
                placeholder = { Text("e.g., 192.168.1.1:8080") },
                isError = !validationResult.isValid, // Use the isValid from the object
                supportingText = {
                    if (!validationResult.isValid) { // Check the object
                        Text(text = validationResult.errorMessage) // Use the error message
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.padding(top = 64.dp, bottom = 32.dp)
            )
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 4.dp
                )
            } else {
                //Spacer to maintain the layout.
                Spacer(modifier = Modifier.height(64.dp))
            }
            Button(
                onClick = {
                    if (isIpPortValid) {
                        viewModel.connectToWebSocket(uiState.ipPort)
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter a valid IP:Port first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.padding(top = 32.dp, bottom = 64.dp),
                enabled = isIpPortValid
            ) {
                Text("Connect")
            }
            // Display the BabyInfo or an appropriate message
            when {
                uiState.babyInfo != null -> {
                    Text("Name: ${uiState.babyInfo?.name ?: "N/A"}")
                    Text("DOB: ${uiState.babyInfo?.dob ?: "N/A"}")
                    Text("Theme: ${uiState.babyInfo?.theme ?: "N/A"}")
                }

                !uiState.isLoading && uiState.ipPort.isNotEmpty() -> {
                    if (!validationResult.isValid) {
                        // Error message already displayed in the text field
                    } else {
                        Text("No data received or error getting baby info.")
                    }
                }
            }
            LaunchedEffect(key1 = uiState.ipPort) {
                // Reset babyInfo when ipPort changes to avoid displaying old data
                viewModel.resetBabyInfo()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun IpAddressConfigPreview() {
        HappyWebSocketBirthdayTheme {
            IpAddressLayout()
        }
    }
}