package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

class ResponseParser(
    objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
) {

    private val parsers: List<Parser<*>> = listOf(
        GetDomainGroupRoutingRulesCommandParser(objectMapper),
        GetDomainGroupsCommandParser(objectMapper),
        GetInterfacesCommandParser(objectMapper),
        GetVersionCommandParser(objectMapper)
    )

    @Suppress("UNCHECKED_CAST")
    fun <T> parse(
        commandType: RciCommandType,
        command: RciCommand<T>,
        response: String
    ): T {
        val suitableParsers = parsers.filter { (it as Parser<T>).suitable(commandType, command) }
        if (suitableParsers.isEmpty()) {
            throw KeeneticRciException("There are no parsers registered")
        }
        if (suitableParsers.size > 1) {
            throw KeeneticRciException("There are multiple parsers registered")
        }
        val parser = suitableParsers.first() as Parser<T>
        return when (commandType) {
            RciCommandType.HTTP -> parser.parseHttpResponse(commandType, command, response)
            RciCommandType.CLI -> parser.parseCliOutput(commandType, command, response)
        }
    }

}

sealed interface Parser<T> {

    fun suitable(commandType: RciCommandType, command: RciCommand<T>): Boolean

    fun parseHttpResponse(commandType: RciCommandType, command: RciCommand<T>, response: String): T

    fun parseCliOutput(commandType: RciCommandType, command: RciCommand<T>, response: String): T

}
