package com.github.khetaghub.keenetic.rci.transport.http

import com.github.khetaghub.keenetic.rci.api.KeeneticApi
import com.github.khetaghub.keenetic.rci.api.SystemApi
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

class KeeneticHttpRci(
    transport: HttpTransport,
    responseParser: ResponseParser,
) : KeeneticApi {

    private val systemApi: SystemApi = SystemHttpApi(transport, responseParser)

    override fun system(): SystemApi = systemApi

}
