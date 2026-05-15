package com.github.khetaghub.keenetic.rci.utils

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import java.util.*

object KeeneticUtils {

    fun decodeEscapedHexToUtf8(input: String): String {
        val bytes = mutableListOf<Byte>()

        var i = 0
        while (i < input.length) {
            if (
                i + 3 < input.length &&
                input[i] == '\\' &&
                input[i + 1] == 'x'
            ) {
                val hex = input.substring(i + 2, i + 4)
                bytes += hex.toInt(16).toByte()
                i += 4
            } else {
                bytes += input[i].code.toByte()
                i++
            }
        }

        return bytes.toByteArray().toString(Charsets.UTF_8)
    }

}

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
