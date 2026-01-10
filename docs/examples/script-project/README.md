# RSProx Jar Script Example

This is a minimal example of a user script packaged as a JAR and loaded by RSProx via `ServiceLoader`.

## Build

1) Publish RSProx's scripting API to Maven local:

- From the RSProx repo root: `./gradlew :scripting-api:publishToMavenLocal`

2) Build this example jar:

- From this folder: `gradle jar`

  Or using the RSProx repo's Gradle wrapper:

  `../../../gradlew jar`

The jar will be at `build/libs/rsprox-jar-script.jar`.

This also copies the jar into `~/.rsprox/scripts/` automatically.

## Install

If you built with `../../../gradlew jar`, the jar is already installed to `~/.rsprox/scripts/`.

Otherwise, you can install it explicitly:

- `gradle installToRSProxScripts`

  Or:

  `../../../gradlew installToRSProxScripts`

Then restart RSProx.

## Notes

- Add more scripts by implementing `net.rsprox.proxy.plugin.RSProxScript`.
- Register additional entrypoints by adding more lines to:
  `src/main/resources/META-INF/services/net.rsprox.proxy.plugin.RSProxScript`
