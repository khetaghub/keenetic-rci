package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.Version
import com.github.khetaghub.keenetic.rci.command.GetVersionCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

class GetVersionCommandParser(private val objectMapper: ObjectMapper) : Parser<Version> {

    override fun suitable(commandType: RciCommandType, command: RciCommand<Version>): Boolean {
        return command is GetVersionCommand
    }

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<Version>,
        response: String
    ): Version {
        val root = objectMapper.readTree(response)
        val versionNode = root.path("show").path("version")
        if (versionNode.isMissingNode || versionNode.isNull) {
            throw KeeneticRciException("Unable to parse version")
        }
        return objectMapper.treeToValue(versionNode, Version::class.java)
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<Version>,
        response: String
    ): Version {
        val root = linkedMapOf<String, String>()

        val lineRegex = Regex("""^\s*([A-Za-z0-9_]+):\s*(.*)$""")

        response
            .lineSequence()
            .mapNotNull { lineRegex.matchEntire(it) }
            .forEach { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2].trim()

                if (value.isNotEmpty()) {
                    root[key] = value
                }
            }

        fun rootValue(key: String): String = root[key].orEmpty()

        return Version(
            release = rootValue("release"),
            sandbox = rootValue("sandbox"),
            title = rootValue("title"),
            arch = rootValue("arch"),
        )
    }

}
