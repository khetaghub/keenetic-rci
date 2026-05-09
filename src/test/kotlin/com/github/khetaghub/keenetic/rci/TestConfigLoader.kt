package com.github.khetaghub.keenetic.rci

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object TestConfigLoader {

    private val mapper = ObjectMapper(YAMLFactory())
        .registerKotlinModule()

    fun load(): TestConfig =
        requireNotNull(
            javaClass.classLoader.getResource("keenetic-rci-config.yaml")
        ) {
            "keenetic-rci-config.yaml not found"
        }.let { url ->
            mapper.readValue(url, TestConfig::class.java)
        }
}

@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy::class)
data class TestConfig(

    @JsonProperty("keenetic-rci")
    val keeneticRci: KeeneticRciConfig,
)

@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy::class)
data class KeeneticRciConfig(
    val transport: TransportConfig,
)

@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy::class)
data class TransportConfig(
    val http: HttpTransportConfig,
    val ssh: SshTransportConfig,
)

@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy::class)
data class HttpTransportConfig(
    val enabled: Boolean,
    val baseUrl: String,
    val username: String,
    val password: String,
)

@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy::class)
data class SshTransportConfig(
    val enabled: Boolean,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
)