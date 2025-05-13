package com.nanit.happywebsocketbirthday.presentation.ipsetup

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun IpSetupScreen(
    uiState: IpSetupScreenState,
    onIpPortChange: (String) -> Unit, // Callback for IP/Port changes
    onConnectClick: () -> Unit, // Callback for the Connect button click
    onSendMessageClick: () -> Unit, // Callback for the SendMessage button click
) {
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
        Text(uiState.connectionStatusText, Modifier.padding(bottom = 16.dp))
        Text(uiState.messageStatusText, Modifier.padding(bottom = 16.dp))
        Text(uiState.babyInfoStatusText)

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
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
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
            onClick = { onConnectClick() },
            modifier = Modifier.padding(top = 32.dp),
            enabled = uiState.validationResult.isValid && uiState.ipPort.isNotEmpty() && !uiState.isLoading
        ) {
            Text(stringResource(R.string.connect))
        }

        Button(
            onClick = {
                onSendMessageClick()
            },
            modifier = Modifier.padding(top = 16.dp),
            enabled = uiState.isConnected
        ) {
            Text(stringResource(R.string.send_message))
        }
    }
}

// Original IpAddressSetupScreen that uses HiltViewModel
@Composable
fun IpSetupScreen(
    onNavigateToBabyInfo: (name: String, dateOfBirth: Long, theme: String) -> Unit,
    viewModel: IpSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Use DisposableEffect to manage the ViewModel's state based on composable lifecycle
    DisposableEffect(Unit) {
        onDispose {
            // When the composable leaves the composition (e.g., navigating away)
            viewModel.resetNavigationTriggerState() // Reset the state
        }
    }

    // Use LaunchedEffect to trigger navigation when babyInfoReceived is true
    LaunchedEffect(key1 = state.babyInfo) {
        state.babyInfo?.let { babyInfo ->
            onNavigateToBabyInfo(babyInfo.name, babyInfo.dateOfBirth, babyInfo.theme)
        }
    }

    IpSetupScreen(
        uiState = state,
        onIpPortChange = { newValue ->
            // When the IP/Port text changes, simply trigger the onIpPortChanged function in the ViewModel
            viewModel.onIpPortChanged(newValue)
        },
        onConnectClick = {
            // When the button is clicked, simply trigger the onConnectClick function in the ViewModel
            viewModel.onConnectClick()
        },
        onSendMessageClick = {
            viewModel.onSendMessageClick()
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
        connectionStatusText = "Not connected",
        messageStatusText = "Send message",
        babyInfoStatusText = "No baby info received yet"
    )

    IpSetupScreen(
        uiState = sampleUiState,
        onIpPortChange = {}, //empty lambdas for preview
        onConnectClick = {},
        onSendMessageClick = {}
    )

}