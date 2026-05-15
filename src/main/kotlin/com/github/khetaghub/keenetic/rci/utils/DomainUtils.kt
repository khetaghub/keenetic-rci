package com.github.khetaghub.keenetic.rci.utils

import java.net.IDN

enum class DomainType {
    DOMAIN,
    IDN,
    PUNYCODE,
}

object DomainUtils {

    fun detectType(domain: String): DomainType {
        val normalized = domain.trim()

        require(normalized.isNotEmpty()) {
            "Domain is blank"
        }

        require(isValid(normalized)) {
            "Invalid domain: $domain"
        }

        return when {
            isPunycode(normalized) -> DomainType.PUNYCODE
            containsUnicode(normalized) -> DomainType.IDN
            else -> DomainType.DOMAIN
        }
    }

    fun isValid(domain: String): Boolean {
        val normalized = domain.trim()

        if (normalized.isEmpty()) {
            return false
        }

        return try {
            val ascii = IDN.toASCII(
                normalized,
                IDN.USE_STD3_ASCII_RULES,
            )

            ascii.length in 1..253 &&
                    ascii.split(".").all(::isValidLabel)
        } catch (_: Exception) {
            false
        }
    }

    fun toAscii(domain: String): String {
        require(isValid(domain)) {
            "Invalid domain: $domain"
        }

        return IDN.toASCII(
            domain.trim(),
            IDN.USE_STD3_ASCII_RULES,
        )
    }

    fun toUnicode(domain: String): String {
        require(isValid(domain)) {
            "Invalid domain: $domain"
        }

        return IDN.toUnicode(domain.trim())
    }

    private fun isPunycode(domain: String): Boolean {
        return domain
            .split(".")
            .any { it.startsWith("xn--", ignoreCase = true) }
    }

    private fun containsUnicode(domain: String): Boolean {
        return domain.any { it.code > 127 }
    }

    private fun isValidLabel(label: String): Boolean {
        return label.isNotEmpty() &&
                label.length <= 63 &&
                !label.startsWith("-") &&
                !label.endsWith("-") &&
                label.all { ch ->
                    ch.isLetterOrDigit() || ch == '-'
                }
    }

}