package com.github.khetaghub.keenetic.rci.command

enum class RciCommandType {
    HTTP, CLI
}

/** Marker for a typed SDK command. The type parameter is the parsed result type. */
sealed interface RciCommand<T>

/** Command representation for Keenetic HTTP RCI batch requests. */
sealed interface HttpBatchCommand<T> : RciCommand<T> {
    val httpRequestBody: String
}

/** CLI representation can be a single command or several commands executed in order. */
sealed interface CliCommandView {
    data class Single(val command: String) : CliCommandView
    data class Sequential(val commands: List<String>) : CliCommandView
}

/** Command representation for NDMS CLI execution over SSH. */
sealed interface CliCommand<T> : RciCommand<T> {
    val cliCommand: CliCommandView
}
