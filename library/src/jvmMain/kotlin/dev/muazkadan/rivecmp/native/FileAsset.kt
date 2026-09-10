package dev.muazkadan.rivecmp.native


public sealed class FileAsset(address: Long) : NativeObject(address) {

    private external fun cppName(cppPointer: Long): String
    private external fun cppDecode(cppPointer: Long, bytes: ByteArray): Boolean
    private external fun cppCDNUrl(cppPointer: Long): String

    public val name: String by lazy { cppName(cppPointer) }
    public val cdnUrl: String by lazy { cppCDNUrl(cppPointer) }

    public fun decode(bytes: ByteArray): Boolean = cppDecode(cppPointer, bytes)
}

/**
 * A thin Kotlin wrapper for the underlying C++ image asset. Helpful to distinguish between various
 * [FileAsset] subclasses.
 */
public class ImageAsset(address: Long) : FileAsset(address)

/**
 * A thin Kotlin wrapper for the underlying C++ font asset. Helpful to distinguish between various
 * [FileAsset] subclasses.
 */
public class FontAsset(address: Long) : FileAsset(address)

/**
 * A thin Kotlin wrapper for the underlying C++ audio asset. Helpful to distinguish between various
 * [FileAsset] subclasses.
 */
public class AudioAsset(address: Long) : FileAsset(address)
