package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.Interface
import com.github.khetaghub.keenetic.rci.api.InterfaceApi
import com.github.khetaghub.keenetic.rci.command.GetInterfacesCommand

internal class DefaultInterfaceApi(
    private val executor: RciCommandExecutor,
) : InterfaceApi {

    override fun getInterfacesList(): List<Interface> {
        return executor.execute(GetInterfacesCommand())
    }

}
