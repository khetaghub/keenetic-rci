package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.api.impl.DefaultWireguardApi
import com.github.khetaghub.keenetic.rci.api.impl.RciCommandExecutor
import com.github.khetaghub.keenetic.rci.command.GetInterfacesCommand
import com.github.khetaghub.keenetic.rci.command.SetWireguardEnabledCommand
import com.github.khetaghub.keenetic.rci.utils.WireguardUtils
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class WireguardApiTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun importConfig(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val configAsString = javaClass.classLoader.getResource("wg/awg-1_5.conf")!!.readText()
        val config = WireguardUtils.parseConfig(configAsString)

        val interfaceName = api.wireguard().generateNextInterfaceName()
        api.wireguard().import(config, interfaceName)
        api.configuration().save()

        val createdWireguardInterface = api.interfaces().getList().firstOrNull { it.name == interfaceName }
        assertThat(createdWireguardInterface).isNotNull()

        api.interfaces().delete(interfaceName)
        api.configuration().save()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun importConfigFile(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val configUrl = javaClass.classLoader.getResource("wg/awg-1_5.conf")!!
        val configFile = WireguardUtils.parseFile(File(configUrl.file))

        val interfaceName = api.wireguard().generateNextInterfaceName()
        api.wireguard().import(configFile, interfaceName)
        api.configuration().save()

        val createdWireguardInterface = api.interfaces().getList().firstOrNull { it.name == interfaceName }
        assertThat(createdWireguardInterface).isNotNull()

        api.interfaces().delete(interfaceName)
        api.configuration().save()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getAscParams_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.wireguard().getAscParams() }
    }

    @Test
    fun generateNextInterfaceName_dontHaveInterfaces() {
        val executorMock = mockk<RciCommandExecutor>()
        every { executorMock.execute(any<GetInterfacesCommand>()) } returns emptyList()

        val wireguardApi = DefaultWireguardApi(executorMock)

        val interfaceName = wireguardApi.generateNextInterfaceName()

        assertThat(interfaceName).isEqualTo("Wireguard0")
    }

    @Test
    fun generateNextInterfaceName_haveInterfaces() {
        val interfaceMocks = List(5) { index ->
            mockk<Interface> {
                every { name } returns "Wireguard${index.inc() * 2}"
            }
        }

        val executorMock = mockk<RciCommandExecutor>()
        every { executorMock.execute(any<GetInterfacesCommand>()) } returns interfaceMocks

        val wireguardApi = DefaultWireguardApi(executorMock)

        val interfaceName = wireguardApi.generateNextInterfaceName()

        assertThat(interfaceName).isEqualTo("Wireguard11")
    }

    @Test
    fun setEnabled_executesCommand() {
        val executorMock = mockk<RciCommandExecutor>()
        every { executorMock.executeWithoutResponse(any<SetWireguardEnabledCommand>()) } just runs

        val wireguardApi = DefaultWireguardApi(executorMock)

        wireguardApi.setEnabled("Wireguard0", false)

        verify(exactly = 1) {
            executorMock.executeWithoutResponse(any<SetWireguardEnabledCommand>())
        }
    }

}
