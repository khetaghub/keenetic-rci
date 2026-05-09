package com.github.khetaghub.keenetic.rci.transport.http

import com.github.khetaghub.keenetic.rci.api.SystemApi
import com.github.khetaghub.keenetic.rci.api.Version
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.command.GetVersionCommand
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

class SystemHttpApi(
    private val transport: HttpTransport,
    private val responseParser: ResponseParser,
) : SystemApi {

    override fun version(): Version {
        val command = GetVersionCommand()
        val response = transport.execute(command)
        val parsedResponse = responseParser.parse(RciCommandType.HTTP, command, response)
        return parsedResponse
    }

}
