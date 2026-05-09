package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.transport.http.HttpTransport
import com.github.khetaghub.keenetic.rci.transport.http.KeeneticHttpRci
import com.github.khetaghub.keenetic.rci.parser.ResponseParser
import com.github.khetaghub.keenetic.rci.transport.ssh.KeeneticSshRci
import com.github.khetaghub.keenetic.rci.transport.ssh.SshTransport

interface KeeneticApi {

    fun system(): SystemApi

    companion object {
        fun create(
            transport: KeeneticTransport,
            responseParser: ResponseParser = ResponseParser(),
        ): KeeneticApi {
            return when (transport) {
                is HttpTransport -> KeeneticHttpRci(transport, responseParser)
                is SshTransport -> KeeneticSshRci(transport, responseParser)
                else -> throw KeeneticRciException("Unsupported transport: ${transport::class.qualifiedName}")
            }
        }
    }

}
