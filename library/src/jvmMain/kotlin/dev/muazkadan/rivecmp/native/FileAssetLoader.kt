package dev.muazkadan.rivecmp.native

import java.net.URI
import kotlin.concurrent.atomics.incrementAndFetch

/**
 * Base class for asset loading. Overload [loadContents] to customize the loading process.
 *
 * This allows you to choose how assets, i.e. images, fonts, and audio, are loaded when referenced
 * by a Rive file. This is especially useful for referenced assets which you may want to load once
 * and supply to multiple Rive files, e.g. for expensive font files.
 *
 */
public abstract class FileAssetLoader : NativeObject(NULL_POINTER) {

    init {
        // Make the corresponding C++ object.
        cppPointer = constructor()
        refs.incrementAndFetch()
        assert(cppPointer != NULL_POINTER)
    }

    /* C++ constructor */
    protected external fun constructor(): Long

    /* Destructor gets called on [dispose()] */
    external override fun cppDelete(pointer: Long)

    private external fun cppRef(pointer: Long)

    /**
     * Override to customize the asset loading process.
     *
     * @param asset The [FileAsset] being loaded. This contains metadata about the asset, e.g. its
     *    name and CDN URL when hosted by Rive.
     * @param inBandBytes The embedded bytes that were included in the Rive file. This will be empty
     *    if the asset was marked as "Referenced" or "Hosted" in the Rive editor.
     * @return true if the asset was loaded, false refuse loading. Returning false can be useful
     *    when using multiple [FileAssetLoader]s in a [FallbackAssetLoader] and you want to delegate
     *    loading to the next loader.
     */
    public abstract fun loadContents(asset: FileAsset, inBandBytes: ByteArray): Boolean

    override fun acquire(): Int {
        cppRef(cppPointer)
        return super.acquire()
    }
}

public class FallbackAssetLoader(
    private val loaders: List<FileAssetLoader>,
) : FileAssetLoader() {

    init {
        loaders.forEach { loader ->
            loader.acquire()
            dependencies.add(loader)
        }
    }

    override fun loadContents(asset: FileAsset, inBandBytes: ByteArray): Boolean =
        loaders.any { loader -> loader.loadContents(asset, inBandBytes) }
}

public class CDNAssetLoader : FileAssetLoader() {

    override fun loadContents(asset: FileAsset, inBandBytes: ByteArray): Boolean {
        if (inBandBytes.isNotEmpty()) return asset.decode(inBandBytes)

        val url = asset.cdnUrl
        if (url.isEmpty()) return false

        return try {
            val connection = URI(url).toURL().openConnection().apply {
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            asset.decode(connection.getInputStream().use { it.readBytes() })
        }
        catch (e: Throwable) {
            RiveLog.e("CDNAssetLoader", "Failed to load CDN asset $url: ${e.message}")
            false
        }
    }
}
