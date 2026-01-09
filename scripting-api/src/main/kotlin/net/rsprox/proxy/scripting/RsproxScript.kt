package net.rsprox.proxy.scripting

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import net.rsprox.proxy.plugin.ScriptContext
import net.rsprox.scripting.api.ScriptLogger

/**
 * Rsprox script definition.
 *
 * Using a dedicated script definition (custom file extension + typed receivers/properties)
 * enables IDE features like completion and highlighting when editing scripts.
 */
@KotlinScript(
    fileExtension = "rsprox.kts",
    compilationConfiguration = RsproxScriptCompilationConfiguration::class,
)
public abstract class RsproxScript

public class RsproxScriptCompilationConfiguration : ScriptCompilationConfiguration(
    {
        // The project uses a Java 11 toolchain; scripts must match or inline calls will fail.
        compilerOptions.append("-jvm-target=11")

        jvm {
            // Scripts need access to the RSProx runtime + protocol model classes.
            dependenciesFromCurrentContext(wholeClasspath = true)
        }

        // Available both as an explicit variable (`context`) and as an implicit receiver (`this`).
        providedProperties("context" to ScriptContext::class)
        implicitReceivers(ScriptContext::class)

        // Logging facility provided by the host (GUI can display it).
        providedProperties("log" to ScriptLogger::class)

        defaultImports(
            // Core rsprox scripting surface
            "net.rsprox.proxy.plugin.ScriptContext",

            // Convenience extensions + event types
            "net.rsprox.scripting.api.*",

            // Packet wrappers
            "net.rsprox.proxy.client.ClientPacket",
            "net.rsprox.proxy.server.ServerPacket",
        )
    },
)
