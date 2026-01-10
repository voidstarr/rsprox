package net.rsprox.proxy.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.michaelbull.logging.InlineLogger
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.jar.JarFile
import java.util.stream.Collectors
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

public data class Script(
    val id: String,
    var name: String,
    val jarPath: Path,
    val implementationClassName: String,
    var enabled: Boolean = false,
)

/**
 * Loads user scripts from JARs in the scripts directory.
 *
 * Scripts are discovered via Java's Service Provider mechanism using
 * `META-INF/services/net.rsprox.proxy.plugin.RSProxScript`.
 */
public class ScriptManager(private val scriptsDir: Path) : AutoCloseable {
    public val scripts: CopyOnWriteArrayList<Script> = CopyOnWriteArrayList()

    private val mapper = jacksonObjectMapper()
    private val stateFile: Path = scriptsDir.resolve("_state.json")
    private val enabledState: MutableMap<String, Boolean> = linkedMapOf()

    private val classLoadersByJar: MutableMap<Path, URLClassLoader> = linkedMapOf()
    private val instancesById: ConcurrentHashMap<String, RSProxScript> = ConcurrentHashMap()
    private val contextsById: ConcurrentHashMap<String, ProxyScriptContext> = ConcurrentHashMap()

    init {
        if (!scriptsDir.exists()) {
            Files.createDirectories(scriptsDir)
        }
        loadState()
        val stateChangedOnDiscover = loadScriptsFromDirectory()
        if (stateChangedOnDiscover) saveState()

        // Enable scripts that were saved as enabled.
        // If a script fails to load, disable it instead of crashing startup.
        var changed = false
        scripts.filter { it.enabled }.forEach { script ->
            try {
                enableScript(script, save = false)
            } catch (t: Throwable) {
                logger.error(t) { "Unable to enable script '${script.id}'" }
                script.enabled = false
                enabledState[script.id] = false
                changed = true
            }
        }
        if (changed) saveState()
    }

    public fun reload() {
        val enabledIds = scripts.filter { it.enabled }.map { it.id }.toSet()
        close()

        if (!scriptsDir.exists()) {
            Files.createDirectories(scriptsDir)
        }

        loadState()
        for (id in enabledIds) {
            enabledState[id] = true
        }
        loadScriptsFromDirectory()

        scripts.filter { it.enabled }.forEach { script ->
            runCatching {
                enableScript(script, save = false)
            }.onFailure { t ->
                logger.error(t) { "Unable to re-enable script '${script.id}'" }
                script.enabled = false
                enabledState[script.id] = false
            }
        }
        saveState()
    }

    public fun enableScript(script: Script, save: Boolean = true) {
        if (contextsById.containsKey(script.id)) return

        val loader = classLoadersByJar[script.jarPath]
            ?: throw IllegalStateException("Jar not loaded: ${script.jarPath}")

        val instance = instantiate(script, loader)

        val context = ProxyScriptContext()
        val log = ProxyScriptLogger(script.name)

        try {
            instance.start(context, log)
            instancesById[script.id] = instance
            contextsById[script.id] = context
            context.activate()
            script.enabled = true
            enabledState[script.id] = true
            if (save) saveState()
        } catch (t: Throwable) {
            runCatching { context.close() }
            runCatching { instance.close() }
            instancesById.remove(script.id)
            contextsById.remove(script.id)
            script.enabled = false
            enabledState[script.id] = false
            if (save) saveState()
            throw RuntimeException("Failed to enable script '${script.name}': ${t.message}", t)
        }
    }

    public fun disableScript(script: Script, save: Boolean = true) {
        val context = contextsById.remove(script.id)
        val instance = instancesById.remove(script.id)

        runCatching { context?.close() }
        runCatching { instance?.close() }

        script.enabled = false
        enabledState[script.id] = false
        if (save) saveState()
    }

    public fun toggleScript(script: Script) {
        if (script.enabled) disableScript(script) else enableScript(script)
    }

    override fun close() {
        for (script in scripts.toList()) {
            if (script.enabled) {
                runCatching { disableScript(script, save = false) }
            }
        }
        for (loader in classLoadersByJar.values) {
            runCatching { loader.close() }
        }
        classLoadersByJar.clear()
        instancesById.clear()
        contextsById.clear()
        scripts.clear()
    }

    private fun instantiate(script: Script, loader: ClassLoader): RSProxScript {
        val clazz = Class.forName(script.implementationClassName, true, loader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        if (instance !is RSProxScript) {
            throw IllegalStateException("${script.implementationClassName} does not implement RSProxScript")
        }

        // Update display name from the script implementation if provided.
        runCatching {
            val displayName = instance.name
            if (displayName.isNotBlank() && displayName != script.name) {
                script.name = displayName
            }
        }

        return instance
    }

    private fun loadState() {
        enabledState.clear()
        try {
            if (stateFile.exists()) {
                val loaded: Map<String, Boolean> = mapper.readValue(stateFile.toFile())
                enabledState.putAll(loaded)
            }
        } catch (t: Throwable) {
            logger.error(t) { "Failed reading script state" }
        }
    }

    private fun saveState() {
        try {
            mapper.writeValue(stateFile.toFile(), enabledState)
        } catch (t: Throwable) {
            logger.error(t) { "Failed writing script state" }
        }
    }

    private fun loadScriptsFromDirectory(): Boolean {
        scripts.clear()
        classLoadersByJar.clear()

        var stateChanged = false
        for (jar in listJarFiles()) {
            val loader = URLClassLoader(arrayOf(jar.toUri().toURL()), parentClassLoader)
            classLoadersByJar[jar] = loader

            val (discovered, changed) = discoverScripts(jar)
            stateChanged = stateChanged || changed
            scripts.addAll(discovered)
        }

        scripts.sortWith(compareBy({ it.jarPath.name }, { it.name }, { it.implementationClassName }))
        return stateChanged
    }

    private fun discoverScripts(jar: Path): Pair<List<Script>, Boolean> {
        val jarName = jar.name
        val result = mutableListOf<Script>()
        var stateChanged = false

        val implementations = readServiceImplementations(jar)
        for (impl in implementations) {
            val id = "$jarName:$impl"
            val enabled =
                if (enabledState.containsKey(id)) {
                    enabledState[id] ?: false
                } else {
                    // New scripts default to enabled unless explicitly disabled.
                    enabledState[id] = true
                    stateChanged = true
                    true
                }
            val displayName = impl.substringAfterLast('.')
            result += Script(
                id = id,
                name = displayName,
                jarPath = jar,
                implementationClassName = impl,
                enabled = enabled,
            )
        }

        return result to stateChanged
    }

    private fun readServiceImplementations(jar: Path): List<String> {
        val result = mutableListOf<String>()
        try {
            JarFile(jar.toFile()).use { jf ->
                val entry = jf.getJarEntry(SERVICE_FILE) ?: return emptyList()
                jf.getInputStream(entry).bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
                    for (raw in lines) {
                        val line = raw.substringBefore('#').trim()
                        if (line.isBlank()) continue
                        result += line
                    }
                }
            }
        } catch (t: Throwable) {
            logger.error(t) { "Failed reading service file from ${jar.name}" }
            return emptyList()
        }
        return result
    }

    private fun listJarFiles(): List<Path> {
        if (!scriptsDir.exists() || !scriptsDir.isDirectory()) return emptyList()
        return Files.list(scriptsDir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .filter { it.fileName.toString().endsWith(JAR_SUFFIX) }
                .sorted { a, b -> a.fileName.toString().compareTo(b.fileName.toString()) }
                .collect(Collectors.toList())
        }
    }

    private companion object {
        private val logger = InlineLogger()
        private const val JAR_SUFFIX: String = ".jar"
        private const val SERVICE_FILE: String = "META-INF/services/net.rsprox.proxy.plugin.RSProxScript"
        private val parentClassLoader: ClassLoader = ScriptManager::class.java.classLoader
    }
}
