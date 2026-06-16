package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.BaseInterface
import com.github.khetaghub.keenetic.rci.api.WireguardApi
import com.github.khetaghub.keenetic.rci.api.WireguardConfig
import com.github.khetaghub.keenetic.rci.api.WireguardConfigFile
import com.github.khetaghub.keenetic.rci.command.GetInterfacesCommand
import com.github.khetaghub.keenetic.rci.command.GetWireguardAscCommand
import com.github.khetaghub.keenetic.rci.command.SetWireguardEnabledCommand
import com.github.khetaghub.keenetic.rci.command.WireguardImportConfigCommand
import com.github.khetaghub.keenetic.rci.utils.WireguardUtils

internal class DefaultWireguardApi(
    private val executor: RciCommandExecutor,
) : WireguardApi {

    override fun import(
        configFile: WireguardConfigFile,
        interfaceName: String
    ) {
        val config = WireguardUtils.parseConfig(configFile.fileContent)
        val command = WireguardImportConfigCommand(config, interfaceName, configFile.fileName)
        executor.executeWithoutResponse(command)
    }

    override fun import(
        config: WireguardConfig,
        interfaceName: String,
        interfaceDescription: String?
    ) {
        val command = WireguardImportConfigCommand(config, interfaceName, interfaceDescription)
        executor.executeWithoutResponse(command)
    }

    override fun getAscParams(): Map<String, Map<String, String>> {
        val command = GetWireguardAscCommand()
        return executor.execute(command)
    }

    override fun generateNextInterfaceName(): String {
        val interfaces: List<BaseInterface> = executor.execute(GetInterfacesCommand())

        val lastInterfaceNumber = interfaces
            .asSequence()
            .map { it.name }
            .mapNotNull { name ->
                Regex("""^Wireguard(\d+)$""", RegexOption.IGNORE_CASE)
                    .matchEntire(name)
                    ?.groupValues
                    ?.get(1)
                    ?.toIntOrNull()
            }
            .maxOrNull()

        val nextInterfaceNumber = lastInterfaceNumber?.plus(1) ?: 0
        val nextInterfaceName = "Wireguard$nextInterfaceNumber"
        return nextInterfaceName
    }

    override fun setEnabled(interfaceName: String, enabled: Boolean) {
        executor.executeWithoutResponse(SetWireguardEnabledCommand(interfaceName, enabled))
    }

}
