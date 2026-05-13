package com.github.khetaghub.keenetic.rci.validator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticNdmsException

data class NdmsError(
    val controller: String,
    val code: Long,
    val message: String?
)

interface ResponseValidator {

    fun validate(commandType: RciCommandType, command: RciCommand<*>, response: String)

}

class DefaultResponseValidator @JvmOverloads constructor(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
) : ResponseValidator {

    private val cliErrorRegex = Regex(
        pattern = """([\p{L}\p{N}_]+(?:::[\p{L}\p{N}_]+)*)\s+error\[(\d+)](?:\s*:\s*(.*))?""",
        options = setOf(RegexOption.DOT_MATCHES_ALL)
    )

    override fun validate(commandType: RciCommandType, command: RciCommand<*>, response: String) {
        val errors: List<NdmsError> = when (commandType) {
            RciCommandType.HTTP -> findHttpResponse(command, response)
            RciCommandType.CLI -> findCliOutput(command, response)
        }
        if (errors.isNotEmpty()) {
            throw KeeneticNdmsException(errors)
        }
    }

    private fun findHttpResponse(command: RciCommand<*>, response: String): List<NdmsError> {
        val root = try {
            objectMapper.readTree(response)
        } catch (e: Exception) {
            throw KeeneticNdmsException(
                "NDMS command failed: invalid JSON response for ${command::class.simpleName}. Response: $response",
                e
            )
        }

        return root.findNdmsErrors()
            .mapNotNull { error ->
                val code = error.get("code")?.asLongOrNull() ?: return@mapNotNull null

                NdmsError(
                    controller = error.get("ident")?.takeIf { it.isTextual }?.asText()
                        ?: command::class.simpleName
                        ?: "Unknown",
                    code = code,
                    message = error.get("message")?.takeIf { it.isTextual }?.asText()?.trim()
                )
            }
    }

    private fun findCliOutput(command: RciCommand<*>, response: String): List<NdmsError> {
        val match = cliErrorRegex.find(response) ?: return emptyList()

        return listOf(
            NdmsError(
                controller = match.groupValues[1],
                code = match.groupValues[2].toLong(),
                message = match.groups[3]?.value?.trim()
            )
        )
    }

    private fun JsonNode.findNdmsErrors(): List<JsonNode> {
        val result = mutableListOf<JsonNode>()

        fun walk(node: JsonNode) {
            if (node.isObject) {
                val status = node.get("status")

                if (status != null && status.isTextual && status.asText() == "error") {
                    result.add(node)
                }

                node.fields().forEachRemaining { (_, child) ->
                    walk(child)
                }
            } else if (node.isArray) {
                node.forEach { child ->
                    walk(child)
                }
            }
        }

        walk(this)

        return result
    }

    private fun JsonNode.asLongOrNull(): Long? =
        when {
            isIntegralNumber -> asLong()
            isTextual -> asText().toLongOrNull()
            else -> null
        }

}
