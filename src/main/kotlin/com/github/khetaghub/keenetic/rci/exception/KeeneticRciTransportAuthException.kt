package com.github.khetaghub.keenetic.rci.exception

open class KeeneticRciTransportAuthException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)