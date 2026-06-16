package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.*

internal class DefaultKeeneticApi(
    private val executor: RciCommandExecutor,
) : KeeneticApi {

    private val configurationApi: ConfigurationApi = DefaultConfigurationApi(executor)
    private val interfacesApi: InterfaceApi = DefaultInterfaceApi(executor)
    private val routingApi: RoutingApi = DefaultRoutingApi(executor)
    private val systemApi: SystemApi = DefaultSystemApi(executor)
    private val wireguardApi: WireguardApi = DefaultWireguardApi(executor)
    private val dnsApi: DnsApi = DefaultDnsApi(executor)

    override fun executeRaw(rawCommand: String): String = executor.executeRaw(rawCommand)

    override fun configuration(): ConfigurationApi = configurationApi

    override fun interfaces(): InterfaceApi = interfacesApi

    override fun routing(): RoutingApi = routingApi

    override fun system(): SystemApi = systemApi

    override fun wireguard(): WireguardApi = wireguardApi

    override fun dns(): DnsApi = dnsApi

}
