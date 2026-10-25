package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Punto único de configuración HTTP. La URL base se define con la propiedad de Gradle
 * `vinilos.apiBaseUrl` (ver `gradle.properties`).
 */
object VinilosApi {

    internal val json = Json { ignoreUnknownKeys = true }

    private val userAgent = "Vinilos/${BuildConfig.VERSION_NAME} (Android)"

    /**
     * Cliente compartido por Retrofit y Coil. Se envía un User-Agent propio porque algunos
     * orígenes de carátulas (p. ej. upload.wikimedia.org) rechazan con 403 el de OkHttp.
     * Incluye fallback automático entre 10.0.2.2 y 127.0.0.1 (ADB reverse) para entornos
     * donde el cortafuegos de Windows bloquea la interfaz virtual del emulador.
     */
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request().newBuilder()
                    .header("User-Agent", userAgent)
                    .build()
                try {
                    chain.proceed(originalRequest)
                } catch (e: IOException) {
                    val url = originalRequest.url
                    val fallbackHost = when (url.host) {
                        "10.0.2.2" -> "127.0.0.1"
                        "127.0.0.1" -> "10.0.2.2"
                        else -> null
                    }
                    if (fallbackHost != null) {
                        val fallbackUrl = url.newBuilder().host(fallbackHost).build()
                        val fallbackRequest = originalRequest.newBuilder().url(fallbackUrl).build()
                        chain.proceed(fallbackRequest)
                    } else {
                        throw e
                    }
                }
            }
            .build()
    }

    val service: VinilosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(VinilosApiService::class.java)
    }
}
