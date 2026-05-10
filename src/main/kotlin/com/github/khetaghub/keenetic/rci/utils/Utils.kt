package com.github.khetaghub.keenetic.rci.utils

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import java.util.*

internal fun escapeJson(value: String): String = buildString {
    value.forEach { ch ->
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(ch)
        }
    }
}

internal fun String.toCliToken(): String {
    if (isBlank()) throw KeeneticRciException("CLI token must not be blank")
    if (contains('\n') || contains('\r')) throw KeeneticRciException("CLI token must not contain line breaks")
    if (contains('"')) throw KeeneticRciException("CLI token must not contain quotes")
    return this
}

internal fun String.toCliString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

internal fun randomKey(length: Int = 12): String = UUID.randomUUID()
    .toString()
    .replace("-", "")
    .take(length)
