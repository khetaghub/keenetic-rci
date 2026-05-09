package com.github.khetaghub.keenetic.rci.command

enum class RciCommandType {
    HTTP, CLI
}

sealed interface RciCommand<T>

//------------------ HTTP Command ------------------

sealed interface HttpBatchCommand<T> : RciCommand<T> {
    val httpRequestBody: String
}

//------------------ SSH Command -------------------

sealed interface CliCommandView {
    data class Single(val command: String) : CliCommandView
    data class Sequential(val commands: List<String>) : CliCommandView
}

sealed interface CliCommand<T> : RciCommand<T> {
    val cliCommand: CliCommandView
}
