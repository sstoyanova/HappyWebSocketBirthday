package com.nanit.happywebsocketbirthday

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nanit.happywebsocketbirthday.ui.theme.HappyWebSocketBirthdayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HappyWebSocketBirthdayTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    IpAddressLayout()
                }
            }
        }
    }

    @Composable
    fun IpAddressLayout(initialValue: String = "", initialLoading: Boolean = false) {
        var ipPort by remember { mutableStateOf(initialValue) }
        var loading by remember { mutableStateOf(initialLoading) }
        var isError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        val context = LocalContext.current
        val isValidInput = !isError && ipPort.isNotEmpty()

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                //A text box that displays the string value you pass here.
                value = ipPort,
                //The lambda callback that's triggered when the user enters text in the text box.
                onValueChange = {
                    ipPort = it
                    val validationResult = isValidIpPortFormat(it)
                    isError = !validationResult.isValid
                    errorMessage = validationResult.errorMessage
                }, // Updates the state when user types
                label = { Text("IP : Port") },
                placeholder = { Text("e.g., 192.168.1.1:8080") },
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(text = errorMessage)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.padding(top = 64.dp, bottom = 32.dp)
            )
            if (loading) {
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
                    if (isValidInput) {
                        loading = true
                        connectToServer(ipPort, onConnectionFinished = {
                            loading = false
                        })
                    } else {
                        Toast.makeText(
                            context,
                            "Please enter a valid IP:Port first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.padding(top = 32.dp, bottom = 64.dp),
                enabled = isValidInput
            ) {
                Text("Connect")
            }
        }
    }

    private fun connectToServer(ipAddress: String, onConnectionFinished: () -> Unit) {
        Log.d("buttonClicked", "Connecting to $ipAddress")
        // Simulate a network request
        Thread {
            Thread.sleep(2000)
            Log.d("buttonClicked", "Finished Connecting to $ipAddress")
            onConnectionFinished()
        }.start()
    }

    @Preview(showBackground = true)
    @Composable
    fun IpAddressConfigPreview() {
        HappyWebSocketBirthdayTheme {
            IpAddressLayout("10.0.2.10:8080", true)
        }
    }
}