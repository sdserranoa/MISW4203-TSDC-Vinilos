package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

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
     */
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", userAgent)
                        .build()
                )
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
