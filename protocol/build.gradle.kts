plugins {
    `java-library`
}

dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.rsprot.buffer)
    implementation(rootProject.libs.rsprot.compression)
    api(rootProject.libs.rsprot.protocol)
    implementation(rootProject.libs.rsprot.crypto)
    implementation(platform(rootProject.libs.log4j.bom))
    implementation(rootProject.libs.bundles.log4j)
    implementation(projects.cache.cacheApi)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
