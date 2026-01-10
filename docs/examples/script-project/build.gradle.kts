import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "2.2.10"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("net.rsprox:scripting-api:1.0.5")
}

kotlin {
    jvmToolchain(11)
}

val rsproxScriptsDir = File(System.getProperty("user.home"), ".rsprox/scripts")

tasks.register<Copy>("installToRSProxScripts") {
    group = "rsprox"
    description = "Copy the built script jar into ~/.rsprox/scripts/"
    dependsOn(tasks.named("jar"))
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(rsproxScriptsDir)
}

tasks.named<Jar>("jar") {
    finalizedBy("installToRSProxScripts")
}
