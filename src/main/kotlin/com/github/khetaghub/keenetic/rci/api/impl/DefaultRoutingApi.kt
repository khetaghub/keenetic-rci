package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.DomainGroup
import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule
import com.github.khetaghub.keenetic.rci.api.RoutingApi
import com.github.khetaghub.keenetic.rci.command.AddDomainGroupCommand
import com.github.khetaghub.keenetic.rci.command.AddDomainGroupRoutingRuleCommand
import com.github.khetaghub.keenetic.rci.command.DeleteDomainGroupCommand
import com.github.khetaghub.keenetic.rci.command.DeleteDomainGroupRoutingRuleCommand
import com.github.khetaghub.keenetic.rci.command.GetDomainGroupRoutingRulesCommand
import com.github.khetaghub.keenetic.rci.command.GetDomainGroupsCommand
import com.github.khetaghub.keenetic.rci.validator.RoutingApiValidator

internal class DefaultRoutingApi(
    private val executor: RciCommandExecutor,
) : RoutingApi {

    override fun getDomainGroupsList(): List<DomainGroup> {
        return executor.execute(GetDomainGroupsCommand())
    }

    override fun addDomainGroup(domainGroup: DomainGroup) {
        RoutingApiValidator.validateAddDomainGroup(domainGroup)
        executor.executeWithoutResponse(AddDomainGroupCommand(domainGroup))
    }

    override fun deleteDomainGroup(domainGroupName: String) {
        RoutingApiValidator.validateDeleteDomainGroup(domainGroupName)
        executor.executeWithoutResponse(DeleteDomainGroupCommand(domainGroupName))
    }

    override fun getDomainGroupRoutingRulesList(): List<DomainGroupRoutingRule> {
        return executor.execute(GetDomainGroupRoutingRulesCommand())
    }

    override fun addDomainGroupRoutingRule(dgRoutingRule: DomainGroupRoutingRule) {
        RoutingApiValidator.validateAddDomainGroupRoutingRule(dgRoutingRule)
        executor.executeWithoutResponse(AddDomainGroupRoutingRuleCommand(dgRoutingRule))
    }

    override fun deleteDomainGroupRoutingRule(domainGroupName: String, interfaceName: String) {
        RoutingApiValidator.validateDeleteDomainGroupRoutingRule(domainGroupName, interfaceName)
        executor.executeWithoutResponse(DeleteDomainGroupRoutingRuleCommand(domainGroupName, interfaceName))
    }

}
