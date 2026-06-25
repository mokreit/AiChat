package com.aichat.data.api

import io.ktor.client.HttpClient

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient {
        configureOpenAiClient()
    }
}
