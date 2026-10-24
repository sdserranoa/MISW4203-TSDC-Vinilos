package com.example.misw4203_tsdc_vinilos

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi

class VinilosApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { VinilosApi.httpClient }))
            }
            .crossfade(true)
            .build()
}
