package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.BaseInterface
import com.github.khetaghub.keenetic.rci.api.InterfaceApi
import com.github.khetaghub.keenetic.rci.api.InternetProviderInterface
import com.github.khetaghub.keenetic.rci.api.WireguardInterface
import com.github.khetaghub.keenetic.rci.command.DeleteInterfaceCommand
import com.github.khetaghub.keenetic.rci.command.GetInterfacesCommand
import com.github.khetaghub.keenetic.rci.command.GetWireguardAscCommand

internal class DefaultInterfaceApi(
    private val executor: RciCommandExecutor,
) : InterfaceApi {

    override fun getList(): List<BaseInterface> {
        return executor.execute(GetInterfacesCommand())
    }

    override fun getProviderList(): List<InternetProviderInterface> {
        val interfaces = getDetailedList()
        val isp = interfaces.filterIsInstance<InternetProviderInterface>()
        return isp
    }

    override fun getWireguardList(): List<WireguardInterface> {
        val interfaces = getDetailedList()
        val wireguards = interfaces.filterIsInstance<WireguardInterface>()
        if (wireguards.isEmpty()) {
            return emptyList()
        }

        val ascByInterfaceName = executor.execute(GetWireguardAscCommand())
        if (ascByInterfaceName.isEmpty()) {
            return wireguards
        }

        return wireguards.map { wireguard ->
            if (wireguard.asc.isEmpty()) {
                wireguard.copy(asc = ascByInterfaceName[wireguard.name].orEmpty())
            } else {
                wireguard
            }
        }
    }

    override fun delete(interfaceName: String) {
        executor.executeWithoutResponse(DeleteInterfaceCommand(interfaceName))
    }

    private fun getDetailedList(): List<BaseInterface> {
        return executor.execute(GetInterfacesCommand(detailed = true))
    }

}
