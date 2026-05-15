package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.DomainGroup
import com.github.khetaghub.keenetic.rci.command.GetDomainGroupsCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.utils.KeeneticUtils

class GetDomainGroupsCommandParser(
    private val objectMapper: ObjectMapper,
) : Parser<List<DomainGroup>> {

    override val commandClass = GetDomainGroupsCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<List<DomainGroup>>,
        response: String,
    ): List<DomainGroup> {
        val root = objectMapper.readTree(response)

        val payload = when {
            root.isArray -> root.firstOrNull()
            else -> root
        } ?: throw KeeneticRciException("Unable to parse domain groups")

        val fqdnNode = payload
            .path("show")
            .path("sc")
            .path("object-group")
            .path("fqdn")

        if (fqdnNode.isMissingNode || fqdnNode.isNull || !fqdnNode.isObject) {
            throw KeeneticRciException("Unable to parse domain groups")
        }

        return fqdnNode.fields().asSequence()
            .map { (name, groupNode) ->
                DomainGroup(
                    name = name,
                    description = groupNode.path("description")
                        .takeIf { !it.isMissingNode && !it.isNull }
                        ?.asText()
                        .orEmpty(),
                    addresses = groupNode.path("include")
                        .takeIf(JsonNode::isArray)
                        ?.mapNotNull { includeNode ->
                            includeNode.path("address")
                                .takeIf { !it.isMissingNode && !it.isNull }
                                ?.asText()
                                ?.trim()
                                ?.takeIf(String::isNotBlank)
                        }
                        .orEmpty(),
                )
            }
            .toList()
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<List<DomainGroup>>,
        response: String,
    ): List<DomainGroup> {

        data class MutableDomainGroup(
            var description: String = "",
            val addresses: MutableList<String> = mutableListOf(),
        )

        val groups = linkedMapOf<String, MutableDomainGroup>()

        var currentGroupName: String? = null

        response.lineSequence().forEach { rawLine ->

            val line = rawLine.trim()

            when {

                line.isBlank() || line == "!" -> Unit

                line.startsWith("object-group fqdn ") -> {
                    val name = line
                        .removePrefix("object-group fqdn ")
                        .trim()

                    currentGroupName = name

                    groups.getOrPut(name) {
                        MutableDomainGroup()
                    }
                }

                line.startsWith("description ") -> {
                    val groupName = currentGroupName ?: return@forEach

                    val description = line
                        .removePrefix("description ")
                        .trim()
                        .removeSurrounding("\"")
                        .let(KeeneticUtils::decodeEscapedHexToUtf8)

                    groups[groupName]?.description = description
                }

                line.startsWith("include ") -> {
                    val groupName = currentGroupName ?: return@forEach

                    val address = line
                        .removePrefix("include ")
                        .trim()

                    if (address.isNotBlank()) {
                        groups[groupName]?.addresses?.add(address)
                    }
                }
            }
        }

        return groups
            .filter { (_, group) ->
                group.description.isNotBlank() ||
                        group.addresses.isNotEmpty()
            }
            .map { (name, group) ->
                DomainGroup(
                    name = name,
                    description = group.description,
                    addresses = group.addresses.toList(),
                )
            }
    }
}
