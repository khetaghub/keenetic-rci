package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.FailSafe
import com.github.khetaghub.keenetic.rci.api.LastChange
import com.github.khetaghub.keenetic.rci.command.GetLastChangeCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

class GetLastChangeCommandParser(
    private val objectMapper: ObjectMapper
) : Parser<LastChange> {

    override val commandClass = GetLastChangeCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<LastChange>,
        response: String
    ): LastChange {
        val root = objectMapper.readTree(response)

        if (!root.isArray || root.isEmpty) {
            throw KeeneticRciException("Expected non-empty array")
        }

        val node = root[0]
            .path("show")
            .path("last-change")
        val lastChange = objectMapper.treeToValue(node, LastChange::class.java)
        val newAction = lastChange.failSafe.action?.ifEmpty { null }
        return lastChange.copy(failSafe = lastChange.failSafe.copy(action = newAction))
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<LastChange>,
        response: String
    ): LastChange {
        val root = mutableMapOf<String, String>()
        val failSafe = mutableMapOf<String, String>()

        var currentSection: String? = null

        response
            .lineSequence()
            .map(String::trimEnd)
            .filter(String::isNotBlank)
            .forEach { line ->

                val separatorIndex = line.indexOf(':')

                if (separatorIndex == -1) {
                    return@forEach
                }

                val key = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1).trim()

                // Only fail-safe is represented as a section.
                if (key == "fail-safe" && value.isEmpty()) {
                    currentSection = "fail-safe"
                    return@forEach
                }

                when (currentSection) {
                    "fail-safe" -> failSafe[key] = value
                    else -> root[key] = value
                }
            }

        return LastChange(
            date = root["date"]
                ?: throw KeeneticRciException("Invalid date"),

            agent = root["agent"]
                ?: throw KeeneticRciException("Invalid agent"),

            user = root["user"]
                ?: throw KeeneticRciException("Invalid user"),

            checksum = root["checksum"]
                ?: throw KeeneticRciException("Invalid checksum"),

            easyconfig = root["easyconfig"]
                ?.toKeeneticBooleanOrNull()
                ?: throw KeeneticRciException("Invalid easyconfig"),

            failSafe = FailSafe(
                action = failSafe["action"]
                    ?.takeIf(String::isNotEmpty),

                unsaved = failSafe["unsaved"]
                    ?.toKeeneticBooleanOrNull()
                    ?: throw KeeneticRciException("Invalid unsaved"),

                timeLeft = failSafe["time-left"]
                    ?.toIntOrNull()
                    ?: throw KeeneticRciException("Invalid time-left"),

                rollback = failSafe["rollback"]
                    ?.toKeeneticBooleanOrNull()
                    ?: throw KeeneticRciException("Invalid rollback"),

                blocked = failSafe["blocked"]
                    ?.toKeeneticBooleanOrNull()
                    ?: throw KeeneticRciException("Invalid blocked")
            )
        )
    }

    private fun String.toKeeneticBooleanOrNull(): Boolean? =
        when (trim().lowercase()) {
            "yes", "true" -> true
            "no", "false" -> false
            else -> null
        }

}
