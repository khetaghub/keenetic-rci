package com.github.khetaghub.keenetic.rci.transport.ssh

import com.github.khetaghub.keenetic.rci.api.InterfaceApi
import com.github.khetaghub.keenetic.rci.api.KeeneticApi
import com.github.khetaghub.keenetic.rci.api.RoutingApi
import com.github.khetaghub.keenetic.rci.api.SystemApi
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

class KeeneticSshRci(
    transport: SshTransport,
    responseParser: ResponseParser,
) : KeeneticApi {

    private val interfaceApi: InterfaceApi = InterfaceSshApi(transport, responseParser)
    private val routingApi: RoutingApi = RoutingSshApi(transport, responseParser)
    private val systemApi: SystemApi = SystemSshApi(transport, responseParser)

    override fun interfaces(): InterfaceApi = interfaceApi
    override fun routing(): RoutingApi = routingApi
    override fun system(): SystemApi = systemApi

}
