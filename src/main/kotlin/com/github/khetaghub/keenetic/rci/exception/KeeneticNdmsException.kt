package com.github.khetaghub.keenetic.rci.exception

import com.github.khetaghub.keenetic.rci.validator.NdmsError

class KeeneticNdmsException : KeeneticRciException {

    constructor(
        errors: List<NdmsError>
    ) : super(
        message = buildMessage(errors)
    )

    constructor(
        message: String,
        cause: Throwable? = null
    ) : super(
        message = message,
        cause = cause
    )

    constructor(
        exitCode: Int,
        command: String,
        output: String? = null,
        cause: Throwable? = null,
    ) : this(
        message = buildString {
            appendLine("CLI command failed")
            appendLine("command: $command")
            appendLine("exitCode: $exitCode")

            if (!output.isNullOrBlank()) {
                appendLine("stdout:")
                appendLine(output.trim())
            }
        },
        cause = cause
    )

    companion object {

        private fun buildMessage(
            errors: List<NdmsError>
        ): String = buildString {

            appendLine("NDMS returned ${errors.size} error(s):")

            errors.forEachIndexed { index, error ->
                append(index + 1)
                append(". ")

                append("[")
                append(error.controller)
                append("] ")

                append("code=")
                append(error.code)

                error.message?.let {
                    append(": ")
                    append(it)
                }

                appendLine()
            }
        }
    }
}