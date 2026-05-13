package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import kotlin.reflect.KClass

interface ResponseParser {

    fun <T> parse(
        commandType: RciCommandType,
        command: RciCommand<T>,
        response: String
    ): T

}

class DefaultResponseParser @JvmOverloads constructor(
    objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
) : ResponseParser {

    private val parsers: Map<KClass<out RciCommand<*>>, Parser<*>> = listOf(
        GetDomainGroupRoutingRulesCommandParser(objectMapper),
        GetDomainGroupsCommandParser(objectMapper),
        GetLastChangeCommandParser(objectMapper),
        GetInterfacesCommandParser(objectMapper),
        GetVersionCommandParser(objectMapper)
    ).associateByUniqueCommandClass()

    override fun <T> parse(
        commandType: RciCommandType,
        command: RciCommand<T>,
        response: String
    ): T {
        val parser = parserFor(command, commandType)
        return when (commandType) {
            RciCommandType.HTTP -> parser.parseHttpResponse(commandType, command, response)
            RciCommandType.CLI -> parser.parseCliOutput(commandType, command, response)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> parserFor(command: RciCommand<T>, commandType: RciCommandType): Parser<T> {
        return parsers[command::class] as? Parser<T>
            ?: throw KeeneticRciException(
                "There is no parser registered for command=${command::class.qualifiedName}, transport=$commandType"
            )
    }

    private fun List<Parser<*>>.associateByUniqueCommandClass(): Map<KClass<out RciCommand<*>>, Parser<*>> {
        val result = linkedMapOf<KClass<out RciCommand<*>>, Parser<*>>()

        forEach { parser ->
            val previous = result.put(parser.commandClass, parser)

            if (previous != null) {
                throw KeeneticRciException(
                    "There are multiple parsers registered for command=${parser.commandClass.qualifiedName}"
                )
            }
        }

        return result
    }

}

sealed interface Parser<T> {

    val commandClass: KClass<out RciCommand<T>>

    fun parseHttpResponse(commandType: RciCommandType, command: RciCommand<T>, response: String): T

    fun parseCliOutput(commandType: RciCommandType, command: RciCommand<T>, response: String): T

}
