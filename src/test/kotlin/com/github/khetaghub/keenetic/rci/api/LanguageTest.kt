package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import com.github.khetaghub.keenetic.rci.utils.randomKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.IDN

class LanguageTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun addDomainGroup(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
//        val id = randomKey()
//        val domainGroup = DomainGroup(
//            name = "test-$id",
//            description = "Моя группа",
            addresses = listOf(
                "domain.com",
                "пример.рф",
                "xn--e1afmkfd.xn--p1ai",
            ),
//        )
//        api.routing().addDomainGroup(domainGroup)

        val toUnicode: String? = IDN.toUnicode("xn--e1afmkfd.xn--p1ai")

        val domainGroups = api.routing().getDomainGroupsList()
        val createdDomainGroup = domainGroups.first { it.description == "Моя группа" }
//        assertThat(createdDomainGroup.addresses)
//            .hasSize(domainGroup.addresses.size)
//            .containsExactlyInAnyOrder(*domainGroup.addresses.toTypedArray())
    }

}
