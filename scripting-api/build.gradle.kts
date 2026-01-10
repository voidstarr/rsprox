plugins {
    `java-library`
}

dependencies {
    api(platform(rootProject.libs.netty.bom))
    api(rootProject.libs.netty.buffer)

    api(rootProject.libs.rsprot.buffer)
    api(rootProject.libs.rsprot.protocol)

    api(projects.protocol)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
