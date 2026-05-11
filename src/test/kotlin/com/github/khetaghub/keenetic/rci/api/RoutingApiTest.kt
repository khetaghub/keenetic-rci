package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import com.github.khetaghub.keenetic.rci.utils.randomKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class RoutingApiTest {

    companion object {
        private const val DEFAULT_INTERFACE_NAME = "Wireguard0"

        private val deletableDomainGroupNames = linkedSetOf<String>()

        @AfterAll
        @JvmStatic
        fun clean() {
            val api = TestConfiguration.firstApiOrNull() ?: return

            deletableDomainGroupNames.forEach { name ->
                runCatching { api.routing().deleteDomainGroup(name) }
            }

            api.system().configurationSave()
        }

        // Deleting a DomainGroup also deletes dependent DomainGroupRoutingRule entries.
        @JvmStatic
        private fun newDomainGroup(markForCleanup: Boolean = false): DomainGroup {
            val id = randomKey()
            val domainGroup = DomainGroup(
                name = "test-$id",
                description = "test-$id",
                addresses = listOf(
                    "domain-1-$id",
                    "domain-2-$id",
                    "domain-3-$id",
                ),
            )

            if (markForCleanup) markForCleanup(domainGroup)

            return domainGroup
        }

        private fun markForCleanup(domainGroup: DomainGroup) {
            deletableDomainGroupNames += domainGroup.name
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getDomainGroupsList_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        api.routing().getDomainGroupsList()
    }

    @Test
    fun getDomainGroupsList_checkSameResponse() {
        TestConfiguration.checkSame { api ->
            api.routing().getDomainGroupsList()
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun addDomainGroup(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val domainGroup = newDomainGroup(markForCleanup = true)
        assertDoesNotThrow { api.routing().addDomainGroup(domainGroup) }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun deleteDomainGroup(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val domainGroup = newDomainGroup()

        api.routing().addDomainGroup(domainGroup)
        api.system().configurationSave()

        assertThat(api.domainGroupNames()).contains(domainGroup.name)

        api.routing().deleteDomainGroup(domainGroup.name)
        api.system().configurationSave()

        assertThat(api.domainGroupNames()).doesNotContain(domainGroup.name)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getDomainGroupRoutingRulesList_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        api.routing().getDomainGroupRoutingRulesList()
    }

    @Test
    fun getDomainGroupRoutingRulesList_checkSameResponse() {
        TestConfiguration.checkSame { api ->
            api.routing().getDomainGroupRoutingRulesList()
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun addDomainGroupRoutingRule(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val domainGroup = newDomainGroup(markForCleanup = true)
        val routingRule = domainGroup.routingRule()

        api.routing().addDomainGroup(domainGroup)
        api.routing().addDomainGroupRoutingRule(routingRule)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun deleteDomainGroupRoutingRule(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val domainGroup = newDomainGroup(markForCleanup = true)
        val routingRule = domainGroup.routingRule()

        api.routing().addDomainGroup(domainGroup)
        api.routing().addDomainGroupRoutingRule(routingRule)
        api.system().configurationSave()

        assertThat(api.routingRuleKeys()).contains(routingRule.key)

        api.routing().deleteDomainGroupRoutingRule(domainGroup.name, routingRule.interfaceName)
        api.system().configurationSave()

        assertThat(api.routingRuleKeys()).doesNotContain(routingRule.key)
    }

    private fun DomainGroup.routingRule(): DomainGroupRoutingRule =
        DomainGroupRoutingRule(
            groupName = name,
            interfaceName = DEFAULT_INTERFACE_NAME,
            auto = true,
            reject = false,
        )

    private fun KeeneticApi.domainGroupNames(): List<String> =
        routing()
            .getDomainGroupsList()
            .map { it.name }

    private fun KeeneticApi.routingRuleKeys(): List<Pair<String, String>> =
        routing()
            .getDomainGroupRoutingRulesList()
            .map { it.groupName to it.interfaceName }

    private val DomainGroupRoutingRule.key: Pair<String, String>
        get() = groupName to interfaceName
}
