package dev.muazkadan.rivecmp.native

import kotlin.concurrent.atomics.incrementAndFetch

/**
 * [File]s are created in the Rive editor.
 *
 * This object has a counterpart in C++, which implements much of its functionality. The base
 * class's [cppPointer] keeps track of this relationship.
 *
 * You can export .riv from the editor and use this class to load them. [File]s may contain multiple
 * artboards.
 *
 * If the given file cannot be loaded this will throw a [RiveException]. The Rive [File] format is
 * evolving, and while we attempt to keep backwards (and forwards) compatibility where possible,
 * there are times when this is not possible.
 *
 * The Rive editor will always export your file in the latest runtime format.
 *
 * Important: If you create a [File] yourself using this constructor, you are responsible for
 * calling [release] when you are done with it, otherwise it will leak memory.
 *
 * @param bytes The bytes of the .riv file.
 * @param fileAssetLoader An optional [FileAssetLoader] to use when loading external assets (images,
 *    fonts, audio) referenced by this file. If it is not provided you will not be able to load
 *    external assets.
 */
public class File(
    bytes: ByteArray,
    public val fileAssetLoader: FileAssetLoader? = CDNAssetLoader(),
) : NativeObject(NULL_POINTER) {

    init {
        // Set the correct renderer type and make sure we make this a dependency.
        // In fact, when importing a file, the FileAssetLoader rcp is incremented and Kotlin
        // should do the same.
        fileAssetLoader?.let {
            it.acquire()
            dependencies.add(it)
        }

        cppPointer = import(
            bytes,
            bytes.size,
            fileAssetLoader?.cppPointer ?: NULL_POINTER,
        )
        refs.incrementAndFetch()
    }

    private external fun import(
        bytes: ByteArray,
        length: Int,
        fileAssetLoaderPointer: Long,
    ): Long

    private external fun cppArtboardByName(cppPointer: Long, name: String): Long
    private external fun cppArtboardByIndex(cppPointer: Long, index: Int): Long
    private external fun cppArtboardNameByIndex(cppPointer: Long, index: Int): String
    private external fun cppArtboardCount(cppPointer: Long): Int

    private external fun cppDefaultViewModelForArtboard(
        cppPointer: Long,
        artboardPointer: Long,
    ): Long

    external override fun cppDelete(pointer: Long)

    /** Get the first (i.e. the default) artboard in the file. */
    public val firstArtboard: Artboard
        @Throws(RiveException::class)
        get() = artboard(0)

    /**
     * Get the artboard called [name] in the file.
     *
     * If multiple [Artboard]s have the same [name] it will return the first match.
     */
    @Throws(RiveException::class)
    public fun artboard(name: String): Artboard {
        // Creates a new artboard instance.
        val artboardPointer = cppArtboardByName(cppPointer, name)
        if (artboardPointer == NULL_POINTER)
            throw ArtboardException(
                "Artboard \"$name\" not found. " +
                    "Available Artboards: ${artboardNames.map { "\"$it\"" }}",
            )

        val ab = Artboard(artboardPointer)
        dependencies.add(ab)
        return ab
    }

    /**
     * Get the artboard at a given [index] in the [File].
     *
     * This starts at 0.
     */
    @Throws(RiveException::class)
    public fun artboard(index: Int): Artboard {
        // Creates a new Artboard instance.
        val artboardPointer = cppArtboardByIndex(cppPointer, index)
        if (artboardPointer == NULL_POINTER)
            throw ArtboardException("No Artboard found at index $index.")

        val ab = Artboard(artboardPointer)
        dependencies.add(ab)
        return ab
    }

    /** Get the number of artboards in the file. Useful for index-based iteration. */
    public val artboardCount: Int
        get() = cppArtboardCount(cppPointer)

    /** Get the names of the artboards in the file. */
    public val artboardNames: List<String>
        get() = (0 until artboardCount).map { index ->
            cppArtboardNameByIndex(cppPointer, index)
        }

    /**
     * Get the default [ViewModel] for an [Artboard]. Usually this will be the ViewModel intended
     * for use with this artboard.
     *
     * @param artboard The artboard to get the default ViewModel for.
     * @return The default ViewModel for the artboard.
     * @throws ViewModelException If the default ViewModel is not found.
     */
    public fun defaultViewModelForArtboard(artboard: Artboard): ViewModel {
        val vmPointer = cppDefaultViewModelForArtboard(cppPointer, artboard.cppPointer)
        if (vmPointer == NULL_POINTER)
            throw ViewModelException("No default ViewModel found for artboard ${artboard.name}.")

        return ViewModel(vmPointer).also { dependencies.add(it) }
    }
}
