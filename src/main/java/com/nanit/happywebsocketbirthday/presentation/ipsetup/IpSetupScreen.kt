package com.nanit.happywebsocketbirthday.presentation.ipsetup

import android.widget.Toast
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.ValidationResult
import com.nanit.happywebsocketbirthday.isValidIpPortFormat

@Composable
fun IpSetupScreen(
    uiState: IpSetupScreenState,
    onIpPortChange: (String) -> Unit, // Callback for IP/Port changes
    onConnectClick: () -> Unit, // Callback for the Connect button click
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Hello Nanit Team!",
            Modifier.padding(
                top = 32.dp,
                bottom = 16.dp
            ),
            fontSize = 21.sp
        )
        if (uiState.babyInfoReceived) {
            Text("Baby info received! ${uiState.babyInfo}")
        } else {
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage + " Try again.")
            } else {
                Text("Waiting for IP setup and will fetch baby info automatically.")
            }
        }

        OutlinedTextField(
            //A text box that displays the string value you pass here.
            value = uiState.ipPort,
            //The lambda callback that's triggered when the user enters text in the text box.
            onValueChange = { newValue ->
                onIpPortChange(newValue) // Call the callback to handle the change
            }, // Updates the state when user types
            label = { Text(stringResource(R.string.ip_port_label)) },
            placeholder = { Text(stringResource(R.string.ip_port_placeholder)) },
            isError = !uiState.validationResult.isValid, // Use validationResult from state
            supportingText = {
                if (!uiState.validationResult.isValid) { // Use validationResult from state
                    Text(text = uiState.validationResult.errorMessage)
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
                if (uiState.validationResult.isValid && uiState.ipPort.isNotEmpty()) {
                    onConnectClick() // Call the callback for the connect button click
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_enter_a_valid_ip_port_first),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.padding(top = 32.dp, bottom = 64.dp),
            enabled = uiState.validationResult.isValid && uiState.ipPort.isNotEmpty() && !uiState.isLoading
        ) {
            Text(stringResource(R.string.connect))
        }
    }
}

// Original IpAddressSetupScreen that uses HiltViewModel
@Composable
fun IpSetupScreen(
    onNavigateToBabyInfo: () -> Unit,
    viewModel: IpSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // Use DisposableEffect to manage the ViewModel's state based on composable lifecycle
    DisposableEffect(Unit) {
        onDispose {
            // When the composable leaves the composition (e.g., navigating away)
            viewModel.resetNavigationTriggerState() // Reset the state
        }
    }

    // Use LaunchedEffect to trigger navigation when babyInfoReceived is true
    LaunchedEffect(key1 = uiState.babyInfoReceived) {
        if (uiState.babyInfoReceived) {
            onNavigateToBabyInfo() // Trigger navigation
        }
    }
    IpSetupScreen(
        uiState = uiState,
        onIpPortChange = { newValue ->
            viewModel.updateIpPort(newValue)
            // Trigger validation in the ViewModel when the IP/Port changes
            viewModel.updateValidationResult(isValidIpPortFormat(newValue)) // Ensure isValidIpPortFormat is accessible
        },
        onConnectClick = {
            // When the button is clicked, trigger the connection logic in the ViewModel
            // The ViewModel will handle validation before connecting
            val validationResult =
                isValidIpPortFormat(uiState.ipPort) // Re-validate before connecting
            viewModel.updateValidationResult(validationResult) // Update validation state

            if (validationResult.isValid && uiState.ipPort.isNotEmpty()) {
                viewModel.connectToWebSocket(uiState.ipPort)
            } else {
                // The state-driven composable handles the Toast for invalid input
            }
        }
    )
}

@Preview(
    showBackground = true,
//    widthDp = 361,
//    heightDp = 592
) // showBackground is helpful to see the composable against a background
@Composable
fun IpAddressSetupScreenPreview() {
    val sampleUiState = IpSetupScreenState(
        ipPort = "192.168.1.1:8080", // Sample IP and port
        validationResult = ValidationResult(true), // Assume valid for preview
        isLoading = false,
        babyInfoReceived = false, // Assume no info received initially
        errorMessage = null,
        babyInfo = null // No baby info yet in this state
    )

    MaterialTheme {
        Surface {
            IpSetupScreen(
                uiState = sampleUiState,
                onIpPortChange = {}, //empty lambdas for preview
                onConnectClick = {},
            )
        }
    }
}