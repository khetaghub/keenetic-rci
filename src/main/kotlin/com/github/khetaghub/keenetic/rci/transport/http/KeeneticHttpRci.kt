package com.github.khetaghub.keenetic.rci.transport.http

import com.github.khetaghub.keenetic.rci.api.InterfaceApi
import com.github.khetaghub.keenetic.rci.api.KeeneticApi
import com.github.khetaghub.keenetic.rci.api.RoutingApi
import com.github.khetaghub.keenetic.rci.api.SystemApi
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

class KeeneticHttpRci(
    transport: HttpTransport,
    responseParser: ResponseParser,
) : KeeneticApi {

    private val interfacesApi: InterfaceApi = InterfaceHttpApi(transport, responseParser)
    private val routingApi: RoutingApi = RoutingHttpApi(transport, responseParser)
    private val systemApi: SystemApi = SystemHttpApi(transport, responseParser)

    override fun interfaces(): InterfaceApi = interfacesApi
    override fun routing(): RoutingApi = routingApi
    override fun system(): SystemApi = systemApi

}
