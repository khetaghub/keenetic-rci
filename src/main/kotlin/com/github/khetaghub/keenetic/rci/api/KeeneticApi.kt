package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.parser.ResponseParser
import com.github.khetaghub.keenetic.rci.api.impl.DefaultKeeneticApi
import com.github.khetaghub.keenetic.rci.api.impl.RciCommandExecutor
import com.github.khetaghub.keenetic.rci.transport.HttpTransport
import com.github.khetaghub.keenetic.rci.transport.SshTransport
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.parser.DefaultResponseParser
import com.github.khetaghub.keenetic.rci.validator.DefaultResponseValidator
import com.github.khetaghub.keenetic.rci.validator.ResponseValidator

/**
 * Main entry point for the SDK.
 *
 * The API is split into domain-specific facades so callers do not have to work
 * with raw NDMS RCI commands directly.
 */
interface KeeneticApi {

    /**
     * Executes a transport-specific raw command and returns the unparsed device response.
     *
     * With [HttpTransport] the argument must be a ready-to-send JSON body for `/rci/`.
     * With [SshTransport] the argument must be a raw NDMS CLI command such as `show version`.
     *
     * Use this method when the SDK does not yet provide a typed facade for the required operation.
     */
    fun executeRaw(rawCommand: String): String

    /** Operations for Keenetic interfaces. */
    fun interfaces(): InterfaceApi

    /** Operations for IPv4, IPv6, and DNS routes, plus FQDN groups. */
    fun routing(): RoutingApi

    /** System-level operations. */
    fun system(): SystemApi

    companion object {
        /**
         * Creates an API facade for the selected transport.
         *
         * HTTP transport sends JSON RCI requests to `/rci/`, while SSH transport
         * executes equivalent CLI commands and parses their output.
         */
        fun create(
            transport: KeeneticTransport,
            responseParser: ResponseParser = DefaultResponseParser(),
            responseValidator: ResponseValidator = DefaultResponseValidator(),
        ): KeeneticApi {
            val commandType = when (transport) {
                is HttpTransport -> RciCommandType.HTTP
                is SshTransport -> RciCommandType.CLI
                else -> throw KeeneticRciException("Unsupported transport: ${transport::class.qualifiedName}")
            }

            return DefaultKeeneticApi(
                executor = RciCommandExecutor(
                    transport = transport,
                    commandType = commandType,
                    responseParser = responseParser,
                    responseValidator = responseValidator,
                )
            )
        }
    }

}
