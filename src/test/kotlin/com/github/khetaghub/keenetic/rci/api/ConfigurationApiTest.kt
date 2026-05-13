package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import com.github.khetaghub.keenetic.rci.api.impl.DefaultConfigurationApi
import com.github.khetaghub.keenetic.rci.api.impl.RciCommandExecutor
import com.github.khetaghub.keenetic.rci.command.SystemConfigurationFailSafeTimerCommand
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.utils.randomKey
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ConfigurationApiTest {

    @BeforeEach
    fun beforeEach() {
        // disable fail-safe if active
        runCatching { TestConfiguration.firstApiOrNull()!!.configuration().disableFailSafeTimer() }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun lastChange_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.configuration().lastChange() }
    }

    @Test
    fun lastChange_checkSameResponse() {
        TestConfiguration.checkSame { api -> api.configuration().lastChange() }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun failSafeTimer_togglesState(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        api.configuration().enableFailSafeTimer(60)
        val lcAfterEnabling = api.configuration().lastChange()
        assertThat(lcAfterEnabling.failSafe.action).isEqualTo(SystemConfigurationFailSafeTimerCommand.ACTIVE_PARAM_VALUE)

        api.configuration().disableFailSafeTimer()
        val lcAfterDisabling = api.configuration().lastChange()
        assertThat(lcAfterDisabling.failSafe.action).isNull()
    }

    @Test
    fun failSafeTimerInterval() {
        val executor = mockk<RciCommandExecutor>()
        every { executor.executeWithoutResponse(any()) } returns Unit
        val configurationApi = DefaultConfigurationApi(executor)

        assertThrows<KeeneticRciException>(
            "Fail-safe timer interval must be between 60 and 86400 seconds, but was 59."
        ) {
            configurationApi.enableFailSafeTimer(59)
        }

        assertDoesNotThrow {
            configurationApi.enableFailSafeTimer(60)
        }

        assertDoesNotThrow {
            configurationApi.enableFailSafeTimer(50_000)
        }

        assertDoesNotThrow {
            configurationApi.enableFailSafeTimer(86_400)
        }

        assertThrows<KeeneticRciException>(
            "Fail-safe timer interval must be between 60 and 86400 seconds, but was 86401."
        ) {
            configurationApi.enableFailSafeTimer(86_401)
        }
    }

    @Test
    @Disabled
    fun failSafe() {
//        val api = TestConfiguration.httpApi!!
        val api = TestConfiguration.sshApi!!

        api.configuration().enableFailSafeTimer(60)
        val id = randomKey()
        api.routing().addDomainGroup(
            DomainGroup(
                name = "test-$id",
                description = "test-$id",
                addresses = listOf(
                    "domain-1-$id",
                    "domain-2-$id",
                    "domain-3-$id",
                ),
            )
        )
        api.configuration().save()
        api.configuration().lastChange()
//        api.configuration().failSafeRollback()
    }

}
