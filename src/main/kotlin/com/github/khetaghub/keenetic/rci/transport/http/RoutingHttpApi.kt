package com.github.khetaghub.keenetic.rci.transport.http

import com.github.khetaghub.keenetic.rci.api.DomainGroup
import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule
import com.github.khetaghub.keenetic.rci.api.RoutingApi
import com.github.khetaghub.keenetic.rci.command.*
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

class RoutingHttpApi(
    private val transport: HttpTransport,
    private val responseParser: ResponseParser,
) : RoutingApi {

    override fun getDomainGroupsList(): List<DomainGroup> {
        val command = GetDomainGroupsCommand()
        val response = transport.execute(command)
        val parsedResponse = responseParser.parse(RciCommandType.HTTP, command, response)
        return parsedResponse
    }

    override fun addDomainGroup(domainGroup: DomainGroup) {
        val command = AddDomainGroupCommand(domainGroup)
        transport.execute(command)
    }

    override fun deleteDomainGroup(domainGroupName: String) {
        val command = DeleteDomainGroupCommand(domainGroupName)
        transport.execute(command)
    }

    override fun getDomainGroupRoutingRulesList(): List<DomainGroupRoutingRule> {
        val command = GetDomainGroupRoutingRulesCommand()
        val response = transport.execute(command)
        val parsedResponse = responseParser.parse(RciCommandType.HTTP, command, response)
        return parsedResponse
    }

    override fun addDomainGroupRoutingRule(dgRoutingRule: DomainGroupRoutingRule) {
        val command = AddDomainGroupRoutingRuleCommand(dgRoutingRule)
        transport.execute(command)
    }

    override fun deleteDomainGroupRoutingRule(domainGroupName: String, interfaceName: String) {
        val command = DeleteDomainGroupRoutingRuleCommand(domainGroupName, interfaceName)
        transport.execute(command)
    }

}
