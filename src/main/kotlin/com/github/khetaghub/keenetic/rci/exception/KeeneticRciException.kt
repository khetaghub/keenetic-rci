package com.github.khetaghub.keenetic.rci.exception

open class KeeneticRciException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)