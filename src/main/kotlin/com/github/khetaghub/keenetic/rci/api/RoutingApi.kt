package com.github.khetaghub.keenetic.rci.api

interface RoutingApi {

    fun getDomainGroupsList(): List<DomainGroup>

    fun addDomainGroup(domainGroup: DomainGroup)

    fun deleteDomainGroup(domainGroupName: String)

    fun getDomainGroupRoutingRulesList(): List<DomainGroupRoutingRule>

    fun addDomainGroupRoutingRule(dgRoutingRule: DomainGroupRoutingRule)

    fun deleteDomainGroupRoutingRule(domainGroupName: String, interfaceName: String)

}

data class DomainGroup(
    val name: String,
    val description: String,
    val addresses: List<String>,
)

data class DomainGroupRoutingRule(
    val groupName: String,
    val interfaceName: String,
    val auto: Boolean,
    val reject: Boolean,
)
