package com.nanit.happywebsocketbirthday.data.network

import android.util.Log
import com.nanit.happywebsocketbirthday.data.model.ApiBabyInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.net.ConnectException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketClient @Inject constructor(
    private val client: HttpClient
) {
    fun connectAndReceiveBabyInfo(ipAddress: String, message: String): Flow<ApiBabyInfo?> = flow {
        val (ip, port) = ipAddress.split(":")
        val url = "ws://$ip:$port/nanit"

        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = ip,
                port = port.toInt(),
                path = "/nanit"
            ) {
                Log.d("WebSocketClient", "WebSocket connection established to: $url")
                send(Frame.Text(message))
                Log.d("WebSocketClient", "Message sent: $message")

                while (isActive) {
                    when (val frame = incoming.receive()) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            Log.d("WebSocketClient", "Received message: $receivedText")
                            val apiBabyInfo = try {
                                ApiBabyInfo.fromJson(receivedText)
                            } catch (e: Exception) {
                                Log.e("WebSocketClient", "Error decoding JSON: ${e.message}")
                                null
                            }
                            emit(apiBabyInfo)
                            if (apiBabyInfo != null) {
                                break
                            }
                        }
                        else -> {
                            Log.w("WebSocketClient", "Received non-text frame")
                        }
                    }
                }
            }
        } catch (e: ConnectException) {
            Log.e("WebSocketClient", "Connection failed: ${e.message}")
            emit(null)
        } catch (e: Exception) {
            Log.e("WebSocketClient", "General exception: ${e.message}")
            emit(null)
        } finally {
            Log.d("WebSocketClient", "WebSocket connection closed")
        }
    }
}