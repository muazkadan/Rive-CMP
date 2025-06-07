package dev.muazkadan.rivecmp

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

@Stable
interface RiveCompositionSpec {
    suspend fun load(): RiveComposition

    companion object {

        @Stable
        fun url(
            url: String
        ): RiveCompositionSpec = RiveUrlCompositionSpec(url)

        @Stable
        fun byteArray(
            byteArray: ByteArray
        ): RiveCompositionSpec = RiveByteArrayCompositionSpec(byteArray)
    }
}

@Immutable
data class RiveUrlCompositionSpec(
    val url: String
) : RiveCompositionSpec {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RiveUrlCompositionSpec
        return url == other.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    override suspend fun load(): RiveComposition {
        return RiveComposition(this)
    }
}

@Immutable
data class RiveByteArrayCompositionSpec(
    val byteArray: ByteArray,
) : RiveCompositionSpec {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RiveByteArrayCompositionSpec
        return byteArray.contentEquals(other.byteArray)
    }

    override fun hashCode(): Int {
        val result = byteArray.contentHashCode()
        return result
    }

    override suspend fun load(): RiveComposition {
        return RiveComposition(this)
    }
}
