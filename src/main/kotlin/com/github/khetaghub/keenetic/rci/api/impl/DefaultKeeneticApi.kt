package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.InterfaceApi
import com.github.khetaghub.keenetic.rci.api.KeeneticApi
import com.github.khetaghub.keenetic.rci.api.RoutingApi
import com.github.khetaghub.keenetic.rci.api.SystemApi

internal class DefaultKeeneticApi(
    executor: RciCommandExecutor,
) : KeeneticApi {

    private val interfacesApi: InterfaceApi = DefaultInterfaceApi(executor)
    private val routingApi: RoutingApi = DefaultRoutingApi(executor)
    private val systemApi: SystemApi = DefaultSystemApi(executor)

    override fun interfaces(): InterfaceApi = interfacesApi
    override fun routing(): RoutingApi = routingApi
    override fun system(): SystemApi = systemApi

}
