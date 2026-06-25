package com.aichat.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionSpec

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
            }
        }
        configureOpenAiClient()
    }
}
