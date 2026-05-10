package com.github.khetaghub.keenetic.rci.transport.http

import com.github.khetaghub.keenetic.rci.api.Interface
import com.github.khetaghub.keenetic.rci.api.InterfaceApi
import com.github.khetaghub.keenetic.rci.command.GetInterfacesCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

class InterfaceHttpApi(
    private val transport: HttpTransport,
    private val responseParser: ResponseParser,
) : InterfaceApi {

    override fun getInterfacesList(): List<Interface> {
        val command = GetInterfacesCommand()
        val response = transport.execute(command)
        val parsedResponse = responseParser.parse(RciCommandType.HTTP, command, response)
        return parsedResponse
    }

}
