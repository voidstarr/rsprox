dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)

    implementation(rootProject.libs.rsprot.buffer)
    implementation(rootProject.libs.rsprot.protocol)

    implementation(projects.protocol)

    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
}
