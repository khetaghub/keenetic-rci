package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule
import com.github.khetaghub.keenetic.rci.command.GetDomainGroupRoutingRulesCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType

class GetDomainGroupRoutingRulesCommandParser(
    private val objectMapper: ObjectMapper,
) : Parser<List<DomainGroupRoutingRule>> {

    override fun suitable(
        commandType: RciCommandType,
        command: RciCommand<List<DomainGroupRoutingRule>>,
    ): Boolean = command is GetDomainGroupRoutingRulesCommand

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<List<DomainGroupRoutingRule>>,
        response: String,
    ): List<DomainGroupRoutingRule> {
        val root = objectMapper.readTree(response)
        return root.findDomainGroupRoutesNode()
            ?.takeIf(JsonNode::isArray)
            ?.map { routeNode ->
                DomainGroupRoutingRule(
                    groupName = routeNode.path("group").asText(),
                    interfaceName = routeNode.path("interface").asText(),
                    auto = routeNode.path("auto").asBoolean(false),
                    reject = routeNode.path("reject").asBoolean(false),
                )
            }
            ?.filter { it.groupName.isNotBlank() || it.interfaceName.isNotBlank() }
            .orEmpty()
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<List<DomainGroupRoutingRule>>,
        response: String,
    ): List<DomainGroupRoutingRule> {
        val result = mutableListOf<DomainGroupRoutingRule>()

        var insideDnsProxy = false

        for (rawLine in response.lines()) {
            val line = rawLine.trim()

            when {
                line == "dns-proxy" -> {
                    insideDnsProxy = true
                }

                line == "!" -> {
                    insideDnsProxy = false
                }

                insideDnsProxy && line.startsWith("route object-group ") -> {
                    val parts = line.split(Regex("\\s+"))

                    // route object-group <groupName> <interfaceName> [auto] [reject]
                    if (parts.size >= 4) {
                        val flags = parts.drop(4).toSet()

                        result += DomainGroupRoutingRule(
                            groupName = parts[2],
                            interfaceName = parts[3],
                            auto = "auto" in flags,
                            reject = "reject" in flags,
                        )
                    }
                }
            }
        }

        return result
    }

    private fun JsonNode.findDomainGroupRoutesNode(): JsonNode? {
        if (has("route")) {
            return path("route")
        }

        if (isObject) {
            fields().forEach { (_, child) ->
                child.findDomainGroupRoutesNode()?.let { return it }
            }
        }

        if (isArray) {
            forEach { child ->
                child.findDomainGroupRoutesNode()?.let { return it }
            }
        }

        return null
    }
}