package dev.muazkadan.rivecmp.utils

/**
 * Marks declarations that are still **experimental** in the Rive CMP library.
 * 
 * These APIs are in early development and may change significantly or be removed entirely
 * in future releases. They are not recommended for production use.
 */
@RequiresOptIn(
    message = "This API is experimental and may change or be removed in future versions. " +
            "Use with caution in production code.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class ExperimentalRiveCmpApi