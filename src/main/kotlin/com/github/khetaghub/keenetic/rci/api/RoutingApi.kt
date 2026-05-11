package com.github.khetaghub.keenetic.rci.api

/** Routing operations for IPv4, IPv6, and DNS routes, plus DNS groups. */
interface RoutingApi {

    /** Returns configured FQDN domain groups. */
    fun getDomainGroupsList(): List<DomainGroup>

    /** Creates an FQDN domain group. */
    fun addDomainGroup(domainGroup: DomainGroup)

    /** Deletes an FQDN domain group by name. */
    fun deleteDomainGroup(domainGroupName: String)

    /** Returns DNS proxy routing rules bound to domain groups. */
    fun getDomainGroupRoutingRulesList(): List<DomainGroupRoutingRule>

    /** Adds a DNS proxy routing rule for a domain group. */
    fun addDomainGroupRoutingRule(dgRoutingRule: DomainGroupRoutingRule)

    /** Deletes a DNS proxy routing rule identified by domain group and interface. */
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
