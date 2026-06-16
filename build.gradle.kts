plugins {
    kotlin("jvm") version "2.2.20"
}

group = "com.github.khetaghub"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    // jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.0")

    // ini
    implementation("org.ini4j:ini4j:0.5.4")

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")

    // ssh
    implementation("com.hierynomus:sshj:0.39.0")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // test
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("io.mockk:mockk:1.13.11")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.13")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
