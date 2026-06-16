package com.github.khetaghub.keenetic.rci.utils

internal fun String?.toJsonField(name: String): String {
    return this?.let { ", \"$name\": \"${escapeJson(it)}\"" }.orEmpty()
}

internal fun StringBuilder.appendOptionalCliField(name: String, value: String?) {
    value?.let {
        append(" ")
        append(name)
        append(" ")
        append(it.toCliToken())
    }
}
