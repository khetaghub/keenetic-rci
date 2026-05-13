package com.github.khetaghub.keenetic.rci.exception

/** Base runtime exception for SDK-level Keenetic RCI failures. */
open class KeeneticRciException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
