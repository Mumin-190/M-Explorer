package me.mumin.android.files.coil

import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import me.mumin.android.files.app.application

fun initializeCoil() {
    Coil.setImageLoader(
        ImageLoader.Builder(application)
            .memoryCache {
                coil.memory.MemoryCache.Builder(application)
                    .maxSizePercent(0.15) // Limit to 15% of available RAM
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(application.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Limit to 2% of free disk space
                    .build()
            }
            .components {
                add(AppIconApplicationInfoKeyer())
                add(AppIconApplicationInfoFetcherFactory(application))
                add(AppIconPackageNameKeyer())
                add(AppIconPackageNameFetcherFactory(application))
                add(PathAttributesKeyer())
                add(PathAttributesFetcher.Factory(application))
                add(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoderDecoder.Factory()
                    } else {
                        GifDecoder.Factory()
                    }
                )
                add(SvgDecoder.Factory(false))
            }
            .build()
    )
}
