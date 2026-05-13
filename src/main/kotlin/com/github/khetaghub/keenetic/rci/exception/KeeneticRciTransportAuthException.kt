package com.github.khetaghub.keenetic.rci.exception

/** Signals authentication or authorization failures reported by the transport. */
open class KeeneticRciTransportAuthException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
