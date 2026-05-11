package com.github.khetaghub.keenetic.rci.exception

open class KeeneticRciTransportException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)