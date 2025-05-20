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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    onDisconnectClick: () -> Unit, // Callback for the Disconnect button click
    onSendMessageClick: () -> Unit, // Callback for the SendMessage button click
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(36.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Hello Nanit Team!", Modifier.padding(bottom = 16.dp), fontSize = 21.sp)
        Text(uiState.connectionStatusText, Modifier.padding(bottom = 16.dp))
        Text(uiState.messageStatusText, Modifier.padding(bottom = 16.dp))
        Text(uiState.babyInfoStatusText)

        IpAddressInputField(
            ipPort = uiState.ipPort,
            onIpPortChange = onIpPortChange, // Pass the callback directly
            validationResult = uiState.validationResult,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
        )

        LoadingIndicatorOrSpacer(isLoading = uiState.isLoading)

        Button(
            onClick = { onConnectClick() },
            modifier = Modifier.padding(top = 32.dp),
            enabled = !uiState.isConnected && uiState.validationResult.isValid && uiState.ipPort.isNotEmpty() && !uiState.isLoading
        ) {
            Text(stringResource(R.string.connect))
        }

        Button(
            onClick = { onDisconnectClick() },
            modifier = Modifier.padding(top = 16.dp),
            enabled = uiState.isConnected
        ) {
            Text(stringResource(R.string.disconnect))
        }

        Button(
            onClick = { onSendMessageClick() },
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
    val keyboardController = LocalSoftwareKeyboardController.current // Get the controller here

    // Use LaunchedEffect to trigger navigation when babyInfoReceived is true
    LaunchedEffect(key1 = state.navigateToBabyInfoEvent) {
        state.navigateToBabyInfoEvent?.let { babyInfo ->
            onNavigateToBabyInfo(babyInfo.name, babyInfo.dateOfBirth, babyInfo.theme)
            viewModel.onBabyInfoNavigationHandled()
        }
    }

    IpSetupScreen(
        uiState = state,
        onIpPortChange = { newValue ->
            // When the IP/Port text changes, simply trigger the onIpPortChanged function in the ViewModel
            viewModel.onIpPortChanged(newValue)
        },
        onConnectClick = {
            keyboardController?.hide() // Hide keyboard
            viewModel.onConnectClick()
        },
        onDisconnectClick = {
            viewModel.onDisconnectClick()
        },
        onSendMessageClick = {
            viewModel.onSendMessageClick()
        }
    )
}

@Preview(showBackground = true)
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
        onDisconnectClick = {},
        onSendMessageClick = {}
    )
}

@Composable
fun IpAddressInputField(
    ipPort: String,
    onIpPortChange: (String) -> Unit,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier // Allow passing custom modifiers
) {
    OutlinedTextField(
        value = ipPort,
        onValueChange = onIpPortChange, // Directly use the passed lambda
        label = { Text(stringResource(R.string.ip_port_label)) },
        placeholder = { Text(stringResource(R.string.ip_port_placeholder)) },
        isError = !validationResult.isValid,
        supportingText = {
            if (!validationResult.isValid) {
                Text(text = validationResult.errorMessage)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done
        ),
        modifier = modifier // Apply the passed modifier
    )
}

@Composable
fun LoadingIndicatorOrSpacer(isLoading: Boolean) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 4.dp // Consider making this a parameter if it might vary
        )
    } else {
        Spacer(modifier = Modifier.height(64.dp))
    }
}