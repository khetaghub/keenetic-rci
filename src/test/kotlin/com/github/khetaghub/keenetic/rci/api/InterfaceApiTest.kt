package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class InterfaceApiTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getList_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val interfaces = api.interfaces().getList()

        assertThat(interfaces)
            .allMatch { it::class == BaseInterface::class }
    }

    @Test
    fun getList_checkSameResponse() {
        TestConfiguration.checkSame { api ->
            api.interfaces()
                .getList()
                .sortedBy { it.index }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getProviderList_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.interfaces().getProviderList() }
    }

    @Test
    fun getProviderList_checkSameResponse() {
        TestConfiguration.checkSame { api ->
            api.interfaces()
                .getProviderList()
                .map { it.copy(uptime = null) }
                .sortedBy { it.index }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getWireguardList_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.interfaces().getWireguardList() }
    }

    @Test
    fun getWireguardList_checkSameResponse() {
        TestConfiguration.checkSame { api ->
            api.interfaces()
                .getWireguardList()
                .map { it.copy(uptime = null, peers = emptyList()) }
                .sortedBy { it.index }
        }
    }

}
