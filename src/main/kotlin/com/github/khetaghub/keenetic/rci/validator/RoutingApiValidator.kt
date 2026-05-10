package com.github.khetaghub.keenetic.rci.validator

import com.github.khetaghub.keenetic.rci.api.DomainGroup
import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

internal object RoutingApiValidator {

    fun validateAddDomainGroup(domainGroup: DomainGroup) {
        requireNotBlank("name", domainGroup.name)
        requireNotBlank("description", domainGroup.description)
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
