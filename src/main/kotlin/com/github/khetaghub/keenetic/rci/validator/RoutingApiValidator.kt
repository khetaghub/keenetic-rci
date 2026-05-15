package com.github.khetaghub.keenetic.rci.validator

import com.github.khetaghub.keenetic.rci.api.DomainGroup
import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.utils.DomainType
import com.github.khetaghub.keenetic.rci.utils.DomainUtils

internal object RoutingApiValidator {

    fun validateAddDomainGroup(domainGroup: DomainGroup) {
        requireNotBlank("name", domainGroup.name)
        requireNotBlank("description", domainGroup.description)

        val addressesWithType: Map<String, DomainType> = domainGroup.addresses.associateWith { DomainUtils.detectType(it) }
        TODO()
    }

    fun validateDeleteDomainGroup(domainGroupName: String) {
        requireNotBlank("domainGroupName", domainGroupName)
    }

    fun validateAddDomainGroupRoutingRule(dgRoutingRule: DomainGroupRoutingRule) {
        requireNotBlank("groupName", dgRoutingRule.groupName)
        requireNotBlank("interfaceName", dgRoutingRule.interfaceName)
    }

    fun validateDeleteDomainGroupRoutingRule(domainGroupName: String, interfaceName: String) {
        requireNotBlank("domainGroupName", domainGroupName)
        requireNotBlank("interfaceName", interfaceName)
    }

    private fun requireNotBlank(fieldName: String, value: String) {
        if (value.isBlank()) {
            throw KeeneticRciException("Field '$fieldName' must not be blank")
        }
    }

}
