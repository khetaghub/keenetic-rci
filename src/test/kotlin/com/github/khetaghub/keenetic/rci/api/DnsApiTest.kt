package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.api.KeeneticTransport
import com.github.khetaghub.keenetic.rci.api.impl.DefaultDnsApi
import com.github.khetaghub.keenetic.rci.api.impl.RciCommandExecutor
import com.github.khetaghub.keenetic.rci.command.CliCommand
import com.github.khetaghub.keenetic.rci.command.CliCommandView
import com.github.khetaghub.keenetic.rci.command.GetPlainDnsCommand
import com.github.khetaghub.keenetic.rci.command.GetSecuredDnsCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.parser.ResponseParser
import com.github.khetaghub.keenetic.rci.validator.ResponseValidator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class DnsApiTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getList(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.dns().getList() }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getPlainList(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.dns().getPlainList() }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getSecuredList(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.dns().getSecuredList() }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun addAndDeletePlainDns(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val dns = PlainDns(
            address = "203.0.113.53",
            interfaceName = null,
        )

        try {
            api.dns().add(dns)

            assertThat(api.dns().getPlainList())
                .anyMatch { it.address == dns.address }
        } finally {
            runCatching { api.dns().deletePlainByAddress(dns.address) }
        }

        assertThat(api.dns().getPlainList())
            .noneMatch { it.address == dns.address }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun addAndDeleteDnsOverTls(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val dns = DnsOverTls(
            address = "203.0.113.54",
            tlsDomainName = "dns.example.com",
        )

        try {
            api.dns().add(dns)
            api.configuration().save()

            assertThat(api.dns().getSecuredList())
                .anyMatch { it is DnsOverTls && it.address == dns.address }
        } finally {
            runCatching { api.dns().deleteDnsOverTlsByAddress(dns.address) }
            runCatching { api.configuration().save() }
        }

        assertThat(api.dns().getSecuredList())
            .noneMatch { it is DnsOverTls && it.address == dns.address }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun addAndDeleteDnsOverHttps(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val dns = DnsOverHttps(
            address = "https://example.com/dns-query",
        )

        try {
            api.dns().add(dns)
            api.configuration().save()

            assertThat(api.dns().getSecuredList())
                .anyMatch { it is DnsOverHttps && it.address == dns.address }
        } finally {
            runCatching { api.dns().deleteDnsOverHttpsByAddress(dns.address) }
            runCatching { api.configuration().save() }
        }

        assertThat(api.dns().getSecuredList())
            .noneMatch { it is DnsOverHttps && it.address == dns.address }
    }

    @Test
    fun deleteAllPlain_deletesEveryConfiguredPlainDnsAddressOnce() {
        val executor = mockk<RciCommandExecutor>(relaxed = true)
        val commands = mutableListOf<RciCommand<Unit>>()
        every { executor.execute(any<GetPlainDnsCommand>()) } returns listOf(
            PlainDns(address = "1.1.1.1", interfaceName = null),
            PlainDns(address = "1.1.1.1", domain = "example.com", interfaceName = null),
            PlainDns(address = "9.9.9.9", interfaceName = null),
        )
        every { executor.executeWithoutResponse(any()) } answers {
            commands += firstArg<RciCommand<Unit>>()
        }

        DefaultDnsApi(executor).deleteAllPlain()

        verify(exactly = 1) { executor.execute(any<GetPlainDnsCommand>()) }
        assertThat(commands.cliCommands())
            .containsExactly(
                "no ip name-server 1.1.1.1",
                "no ip name-server 9.9.9.9",
            )
    }

    @Test
    fun deleteAllDnsOverTls_deletesOnlyTlsResolvers() {
        val executor = mockk<RciCommandExecutor>(relaxed = true)
        val commands = mutableListOf<RciCommand<Unit>>()
        every { executor.execute(any<GetSecuredDnsCommand>()) } returns listOf(
            DnsOverTls(address = "1.1.1.1", tlsDomainName = "cloudflare-dns.com"),
            DnsOverHttps(address = "https://1.1.1.1/dns-query"),
            DnsOverTls(address = "9.9.9.9", tlsDomainName = "dns.quad9.net"),
        )
        every { executor.executeWithoutResponse(any()) } answers {
            commands += firstArg<RciCommand<Unit>>()
        }

        DefaultDnsApi(executor).deleteAllDnsOverTls()

        verify(exactly = 1) { executor.execute(any<GetSecuredDnsCommand>()) }
        assertThat(commands.cliCommands())
            .containsExactly(
                "no dns-proxy tls upstream 1.1.1.1",
                "no dns-proxy tls upstream 9.9.9.9",
            )
    }

    @Test
    fun deleteAllDnsOverHttps_deletesOnlyHttpsResolvers() {
        val executor = mockk<RciCommandExecutor>(relaxed = true)
        val commands = mutableListOf<RciCommand<Unit>>()
        every { executor.execute(any<GetSecuredDnsCommand>()) } returns listOf(
            DnsOverTls(address = "1.1.1.1", tlsDomainName = "cloudflare-dns.com"),
            DnsOverHttps(address = "https://1.1.1.1/dns-query"),
            DnsOverHttps(address = "https://9.9.9.9/dns-query"),
        )
        every { executor.executeWithoutResponse(any()) } answers {
            commands += firstArg<RciCommand<Unit>>()
        }

        DefaultDnsApi(executor).deleteAllDnsOverHttps()

        verify(exactly = 1) { executor.execute(any<GetSecuredDnsCommand>()) }
        assertThat(commands.cliCommands())
            .containsExactly(
                "no dns-proxy https upstream https://1.1.1.1/dns-query",
                "no dns-proxy https upstream https://9.9.9.9/dns-query",
            )
    }

    @Test
    fun deleteAllSecured_deletesTlsAndHttpsResolversFromSingleSnapshot() {
        val executor = mockk<RciCommandExecutor>(relaxed = true)
        val commands = mutableListOf<RciCommand<Unit>>()
        every { executor.execute(any<GetSecuredDnsCommand>()) } returns listOf(
            DnsOverTls(address = "1.1.1.1", tlsDomainName = "cloudflare-dns.com"),
            DnsOverHttps(address = "https://1.1.1.1/dns-query"),
        )
        every { executor.executeWithoutResponse(any()) } answers {
            commands += firstArg<RciCommand<Unit>>()
        }

        DefaultDnsApi(executor).deleteAllSecured()

        verify(exactly = 1) { executor.execute(any<GetSecuredDnsCommand>()) }
        assertThat(commands.cliCommands())
            .containsExactly(
                "no dns-proxy tls upstream 1.1.1.1",
                "no dns-proxy https upstream https://1.1.1.1/dns-query",
            )
    }

    @Test
    fun deleteAll_deletesPlainAndSecuredResolvers() {
        val transport = RecordingTransport()
        val executor = RciCommandExecutor(
            transport = transport,
            commandType = RciCommandType.CLI,
            responseParser = StaticDnsResponseParser(
                plainDns = listOf(PlainDns(address = "8.8.8.8", interfaceName = null)),
                securedDns = listOf(
                    DnsOverTls(address = "1.1.1.1", tlsDomainName = "cloudflare-dns.com"),
                    DnsOverHttps(address = "https://1.1.1.1/dns-query"),
                ),
            ),
            responseValidator = NoopResponseValidator,
        )

        DefaultDnsApi(executor).deleteAll()

        assertThat(transport.commands.cliCommands().filter { it.startsWith("no ") })
            .containsExactly(
                "no ip name-server 8.8.8.8",
                "no dns-proxy tls upstream 1.1.1.1",
                "no dns-proxy https upstream https://1.1.1.1/dns-query",
            )
    }

    private fun List<RciCommand<*>>.cliCommands(): List<String> {
        return mapNotNull { command ->
            val cliCommand = (command as CliCommand<*>).cliCommand
            (cliCommand as? CliCommandView.Single)?.command
        }
    }

    private class RecordingTransport : KeeneticTransport {
        val commands = mutableListOf<RciCommand<*>>()

        override fun execute(command: RciCommand<*>): String {
            commands += command
            return ""
        }

        override fun execute(rawCommand: String): String = ""
    }

    private class StaticDnsResponseParser(
        private val plainDns: List<PlainDns>,
        private val securedDns: List<SecuredDns>,
    ) : ResponseParser {

        @Suppress("UNCHECKED_CAST")
        override fun <T> parse(commandType: RciCommandType, command: RciCommand<T>, response: String): T {
            return when (command) {
                is GetPlainDnsCommand -> plainDns
                is GetSecuredDnsCommand -> securedDns
                else -> error("Unexpected command")
            } as T
        }
    }

    private object NoopResponseValidator : ResponseValidator {
        override fun validate(commandType: RciCommandType, command: RciCommand<*>, response: String) = Unit
    }

}
