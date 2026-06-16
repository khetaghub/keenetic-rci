package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.ProviderDnsIpVersion
import com.github.khetaghub.keenetic.rci.api.ProviderDnsState
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

/** Reads whether provider-provided DNS servers are used on a provider interface. */
class GetProviderDnsCommand(
    val interfaceName: String,
) : HttpBatchCommand<ProviderDnsState>, CliCommand<ProviderDnsState> {

    override val httpRequestBody = """
        [
          {
            "show": {
              "sc": {
                "interface": {}
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("show running-config")

}

/** Enables or ignores provider-provided DNS servers on a provider interface. */
class SetProviderDnsCommand(
    interfaceName: String,
    ipVersion: ProviderDnsIpVersion,
    enabled: Boolean,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = providerDnsHttpRequestBody(
        interfaceName = interfaceName,
        ipVersion = ipVersion,
        enabled = enabled,
    )

    override val cliCommand = providerDnsCliCommand(
        interfaceName = interfaceName,
        ipVersion = ipVersion,
        ipv4Command = if (enabled) {
            "ip name-servers"
        } else {
            "ip no name-servers"
        },
        rootIpv6Command = if (enabled) {
            "interface ${interfaceName.toCliToken()} ipv6 name-servers"
        } else {
            "interface ${interfaceName.toCliToken()} ipv6 no name-servers"
        },
    )

}

private fun providerDnsHttpRequestBody(
    interfaceName: String,
    ipVersion: ProviderDnsIpVersion,
    enabled: Boolean,
): String {
    val commands = when (ipVersion) {
        ProviderDnsIpVersion.IPV4 -> listOf(providerDnsHttpCommand(interfaceName, "ip", enabled))
        ProviderDnsIpVersion.IPV6 -> listOf(providerDnsHttpCommand(interfaceName, "ipv6", enabled))
        ProviderDnsIpVersion.ALL -> listOf(
            providerDnsHttpCommand(interfaceName, "ip", enabled),
            providerDnsHttpCommand(interfaceName, "ipv6", enabled),
        )
    }

    return commands.joinToString(
        separator = ",\n",
        prefix = "[\n",
        postfix = "\n]",
    )
}

private fun providerDnsHttpCommand(
    interfaceName: String,
    protocol: String,
    enabled: Boolean,
): String {
    return """
          {
            "interface": {
              "name": "${escapeJson(interfaceName)}",
              "$protocol": {
                "name-servers": $enabled
              }
            }
          }
    """.trimIndent()
}

private fun providerDnsCliCommand(
    interfaceName: String,
    ipVersion: ProviderDnsIpVersion,
    ipv4Command: String,
    rootIpv6Command: String,
): CliCommandView {
    val interfaceToken = interfaceName.toCliToken()

    return when (ipVersion) {
        ProviderDnsIpVersion.IPV4 -> CliCommandView.Contextual(
            listOf(
                "interface $interfaceToken",
                ipv4Command,
            )
        )

        ProviderDnsIpVersion.IPV6 -> CliCommandView.Single(rootIpv6Command)

        ProviderDnsIpVersion.ALL -> CliCommandView.Contextual(
            listOf(
                "interface $interfaceToken",
                ipv4Command,
                "exit",
                rootIpv6Command,
            )
        )
    }
}
