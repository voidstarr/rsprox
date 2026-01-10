package net.rsprox.proxy.plugin

import net.rsprox.scripting.api.ScriptLogger

/**
 * Entry-point interface for user scripts distributed as JARs.
 *
 * Implementations are discovered via Java's [java.util.ServiceLoader].
 *
 * To register a script, the JAR must contain:
 * - `META-INF/services/net.rsprox.proxy.plugin.RSProxScript`
 *   with the fully-qualified implementation class name(s), one per line.
 *
 * The proxy will create a [ScriptContext] for each enabled script, call [start],
 * then activate the context (triggering any `context.onEnable { ... }` handlers).
 */
public interface RSProxScript : AutoCloseable {
    /** Display name as shown in logs/UI. */
    public val name: String

    /** Called when the script is enabled. */
    public fun start(
        context: ScriptContext,
        log: ScriptLogger,
    )

    /** Called after the script is disabled for additional cleanup. */
    override fun close() {
        // default no-op
    }
}
