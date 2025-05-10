package com.nanit.happywebsocketbirthday.di

import android.content.Context
import android.content.SharedPreferences
import com.nanit.happywebsocketbirthday.data.BabyRepositoryImpl
import com.nanit.happywebsocketbirthday.data.network.WebSocketClient
import com.nanit.happywebsocketbirthday.domain.repository.BabyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("baby_info_prefs", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
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


    @Provides
    @Singleton
    fun provideBabyRepository(
        webSocketClient: WebSocketClient,
        sharedPreferences: SharedPreferences
    ): BabyRepository { // Provide the domain interface
        return BabyRepositoryImpl(
            webSocketClient,
            sharedPreferences
        ) // Return the data layer implementation
    }
}