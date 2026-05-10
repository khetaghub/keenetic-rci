package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.KeeneticTransport
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

internal class RciCommandExecutor(
    private val transport: KeeneticTransport,
    private val commandType: RciCommandType,
    private val responseParser: ResponseParser,
) {

    fun <T> execute(command: RciCommand<T>): T {
        val response = transport.execute(command)
        return responseParser.parse(commandType, command, response)
    }

    fun executeWithoutResponse(command: RciCommand<Unit>) {
        transport.execute(command)
    }

}
