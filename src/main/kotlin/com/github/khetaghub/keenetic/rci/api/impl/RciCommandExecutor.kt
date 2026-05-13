package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.KeeneticTransport
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.parser.ResponseParser
import com.github.khetaghub.keenetic.rci.validator.ResponseValidator

internal class RciCommandExecutor(
    private val transport: KeeneticTransport,
    private val commandType: RciCommandType,
    private val responseParser: ResponseParser,
    private val responseValidator: ResponseValidator,
) {

    fun <T> execute(command: RciCommand<T>): T {
        val response = transport.execute(command)
        responseValidator.validate(commandType, command, response)
        return responseParser.parse(commandType, command, response)
    }

    fun executeWithoutResponse(command: RciCommand<Unit>) {
        val response = transport.execute(command)
        responseValidator.validate(commandType, command, response)
    }

    fun executeRaw(rawCommand: String): String {
        val response = transport.execute(rawCommand)
        return response
    }

}
