package com.nanit.happywebsocketbirthday.di

import com.nanit.happywebsocketbirthday.data.BabyRepositoryImpl
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.ALL // Set the desired log level
            }
            install(WebSockets)
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    // Provide the WebSocketClient
    @Provides
    @Singleton
    fun provideWebSocketClient(client: HttpClient): WebSocketClient {
        return WebSocketClient(client)
    }

    // Provide Clock for age calculation
    @Provides
    @Singleton
    fun provideClock(): Clock {
        return Clock.System
    }

    // Provide TimeZone for age calculation
    @Provides
    @Singleton
    fun provideTimeZone(): TimeZone {
        return TimeZone.currentSystemDefault()
    }


    @Provides
    @Singleton
    fun provideBabyRepository(
        webSocketClient: WebSocketClient
    ): BabyRepository { // Provide the domain interface
        return BabyRepositoryImpl(
            webSocketClient
        ) // Return the data layer implementation
    }
}