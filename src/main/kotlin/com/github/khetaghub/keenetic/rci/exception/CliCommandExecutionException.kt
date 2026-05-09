package com.github.khetaghub.keenetic.rci.exception

class CliCommandExecutionException(
    val exitCode: Int,
    val command: String,
    message: String,
    cause: Throwable? = null,
) : KeeneticRciException(
    message,
    cause
)