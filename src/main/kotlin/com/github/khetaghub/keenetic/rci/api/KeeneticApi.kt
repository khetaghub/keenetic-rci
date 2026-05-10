package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.parser.ResponseParser
import com.github.khetaghub.keenetic.rci.api.impl.DefaultKeeneticApi
import com.github.khetaghub.keenetic.rci.api.impl.RciCommandExecutor
import com.github.khetaghub.keenetic.rci.transport.HttpTransport
import com.github.khetaghub.keenetic.rci.transport.SshTransport
import com.github.khetaghub.keenetic.rci.command.RciCommandType

interface KeeneticApi {

    fun interfaces(): InterfaceApi
    fun routing(): RoutingApi
    fun system(): SystemApi

    companion object {
        fun create(
            transport: KeeneticTransport,
            responseParser: ResponseParser = ResponseParser(),
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
                )
            )
        }
    }

}
