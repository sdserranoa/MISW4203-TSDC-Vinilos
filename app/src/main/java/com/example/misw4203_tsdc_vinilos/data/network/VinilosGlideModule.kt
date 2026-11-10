package com.example.misw4203_tsdc_vinilos.data.network

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule
import com.bumptech.glide.request.RequestOptions
import java.io.InputStream

/**
 * Módulo de configuración de Glide para el almacenamiento en caché de imágenes (HU04).
 *
 * Implementa [GlideModule] para ser descubierto automáticamente a través del AndroidManifest,
 * configurando una estrategia de caching en dos niveles:
 * 1. Caché en Memoria (RAM): [LruResourceCache] calculado dinámicamente según la memoria disponible.
 * 2. Caché en Disco: [InternalCacheDiskCacheFactory] de 100 MB en almacenamiento interno privado.
 * 3. Estrategia de Disco: [DiskCacheStrategy.ALL], almacenando tanto los bytes originales recibidos de red
 *    como las resoluciones y transformaciones decodificadas para acelerar el renderizado en pantalla.
 * 4. Integración de Red: Sustituye el cargador por defecto por [OkHttpUrlLoader] conectado a
 *    [VinilosApi.httpClient], beneficiándose de User-Agent propio, certificados y normalización de URLs.
 */
@Suppress("DEPRECATION")
class VinilosGlideModule : GlideModule {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Cálculo del tamaño óptimo de memoria caché según las especificaciones del dispositivo
        val calculator = MemorySizeCalculator.Builder(context)
            .setMemoryCacheScreens(2f)
            .build()
        builder.setMemoryCache(LruResourceCache(calculator.memoryCacheSize.toLong()))

        // Asignación de 100 MB para el almacenamiento persistente en disco de carátulas y retratos
        val diskCacheSizeBytes = 100L * 1024L * 1024L
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(context, DISK_CACHE_DIR, diskCacheSizeBytes)
        )

        // Opciones por defecto para todas las solicitudes
        builder.setDefaultRequestOptions(
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
        )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Enrutamiento de peticiones remotas a través del cliente OkHttp compartido
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(VinilosApi.httpClient)
        )
    }

    companion object {
        const val DISK_CACHE_DIR = "vinilos_glide_cache"
    }
}
