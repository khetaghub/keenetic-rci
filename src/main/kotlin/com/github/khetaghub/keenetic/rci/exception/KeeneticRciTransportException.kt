package com.github.khetaghub.keenetic.rci.exception

/** Signals transport-level failures while sending requests to a Keenetic device. */
open class KeeneticRciTransportException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
