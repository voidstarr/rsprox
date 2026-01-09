package net.rsprox.proxy.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.IdentityHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

public data class Script(
    var name: String,
    var code: String,
    var enabled: Boolean = false,
    internal var filePath: Path? = null,
)

public class ScriptManager(private val scriptsDir: Path) {
    public val scripts: CopyOnWriteArrayList<Script> = CopyOnWriteArrayList()

    private val contexts = IdentityHashMap<Script, ScriptContext>()
    private val scriptingSupport = ScriptingSupport()
    private val mapper = jacksonObjectMapper()

    private val stateFile: Path = scriptsDir.resolve("_state.json")
    private val enabledState: MutableMap<String, Boolean> = linkedMapOf()

    init {
        var scriptsDirCreated = false
        if (!scriptsDir.exists()) {
            Files.createDirectories(scriptsDir)
            scriptsDirCreated = true
        }
        loadState()
        loadScriptsFromDirectory()

        if (scriptsDirCreated) {
            createDefaults()
        }

        // Enable scripts that were saved as enabled.
        // If a script no longer compiles/loads, disable it instead of crashing startup.
        var changed = false
        scripts.filter { it.enabled }.forEach { script ->
            try {
                enableScript(script, save = false)
            } catch (e: Exception) {
                e.printStackTrace()
                script.enabled = false
                changed = true
            }
        }
        if (changed) {
            saveState()
        }
    }

    private fun createDefaults() {
        addScript(
            "Public Chat Yeet",
            """
            import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic

            /*
             * Public Chat Yeet
             * ---------------
             * What this does:
             * - Rewrites outgoing public chat messages so the client sends "yeet".
             *
             * How it works:
             * - Hooks a single client->server packet by its prot name ("MESSAGE_PUBLIC").
             * - Decodes the packet into a strongly-typed model (MessagePublic).
             * - Creates a new MessagePublic and re-encodes it back into the packet.
             *
             * Notes:
             * - This modifies what your client sends to the server.
             * - If the prot name changes between revisions, the hook simply won't fire.
             *
             * Try customizing:
             * - Change the replacement text.
             * - Add conditions (e.g. only rewrite when it starts with "/" or equals a phrase).
             */

            context.onClientPacket("MESSAGE_PUBLIC") { packet ->
                try {
                    val message = packet.decode<MessagePublic>()
                    if (message.message != "yeet") {
                        val newMessage = MessagePublic(
                            message.type,
                            message.colour,
                            message.effect,
                            "yeet",
                            message.pattern,
                            message.clanType
                        )
                        packet.encode(newMessage)
                        log.info("Public chat rewritten to yeet!")
                    }
                } catch (e: Exception) {
                    log.error("Failed to rewrite public chat", e)
                }
            }
            """.trimIndent(),
        )

        addScript(
            "Walk to Tile",
            """
            import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick

            /*
             * Walk to Tile
             * ------------
             * What this does:
             * - Forces your walk-click target to a fixed coordinate (x=3200, z=3200).
             *
             * How it works:
             * - Hooks outgoing MOVE_GAMECLICK.
             * - Decodes it to MoveGameClick, replaces fields, then re-encodes.
             *
             * Try customizing:
             * - Change the destination coordinates.
             * - Add a condition (e.g. only rewrite when holding a key combination).
             */

            context.onClientPacket("MOVE_GAMECLICK") { packet ->
                try {
                    val walk = packet.decode<MoveGameClick>()
                    if (walk.x != 3200 || walk.z != 3200) {
                         val newWalk = MoveGameClick(3200, 3200, walk.keyCombination)
                         packet.encode(newWalk)
                         log.info("Walk target rewritten to 3200, 3200")
                    }
                } catch (e: Exception) {
                    log.error("Failed to rewrite walk target", e)
                }
            }
            """.trimIndent(),
        )

        addScript(
            "Packet Logger (Client + Server)",
            """
            /*
             * Packet Logger (Client + Server)
             * -------------------------------
             * What this does:
             * - Prints a log line when a few selected packets pass through.
             *
             * How it works:
             * - Uses packet listeners (non-dropping): onClientPacket / onServerPacket.
             * - Listeners observe packets; they do not change or drop them.
             *
             * Try customizing:
             * - Change the prot name strings.
             * - Log more packet metadata.
             */

            context.onClientPacket("MESSAGE_PUBLIC") { packet ->
                log.info("C->S ${'$'}{packet.prot} size=${'$'}{packet.payload.readableBytes()}")
            }

            context.onClientPacket("MOVE_GAMECLICK") { packet ->
                log.info("C->S ${'$'}{packet.prot} size=${'$'}{packet.payload.readableBytes()}")
            }

            context.onServerPacket("MESSAGE_GAME") { packet ->
                log.info("S->C ${'$'}{packet.prot} size=${'$'}{packet.payload.readableBytes()}")
            }
            """.trimIndent(),
        )

        addScript(
            "Block URL Opens",
            """
            /*
             * Block URL Opens
             * --------------
             * What this does:
             * - Drops server->client URL_OPEN packets so the client never sees them.
             *
             * How it works:
             * - Uses a transformer (dropping/modifying): onServerTransform.
             * - Return null to drop a packet.
             *
             * Notes:
             * - Transformers run before listeners.
             * - If the prot name changes, the drop rule won't trigger.
             */

            context.onServerTransform { packet ->
                if (packet.prot.toString() == "URL_OPEN") {
                    log.info("Blocked URL_OPEN")
                    null
                } else {
                    packet
                }
            }
            """.trimIndent(),
        )

        addScript(
            "Prefix Private Messages",
            """
            import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate

            /*
             * Prefix Private Messages
             * ----------------------
             * What this does:
             * - Adds a prefix to outgoing private messages before they're sent.
             *
             * How it works:
             * - Hooks the client->server MESSAGE_PRIVATE packet.
             * - Decodes it to MessagePrivate, builds a new MessagePrivate, and encodes it.
             *
             * Try customizing:
             * - Prefix only certain recipients.
             * - Only prefix when a toggle is enabled (see the Tick/Timer examples).
             */

            context.onClientPacket("MESSAGE_PRIVATE") { packet ->
                try {
                    val pm = packet.decode<MessagePrivate>()
                    val newPm = MessagePrivate(pm.name, "[rsprox] ${'$'}{pm.message}")
                    packet.encode(newPm)
                } catch (e: Exception) {
                    log.error("Failed to prefix private message", e)
                }
            }
            """.trimIndent(),
        )

        addScript(
            "Examples: Lifecycle + Login",
            """
            /*
             * Examples: Lifecycle + Login
             * -----------------------------
             * What this does:
             * - Demonstrates the basic lifecycle hooks for a script.
             * - Logs enable/disable and login/logout events.
             *
             * Useful for:
             * - Verifying your scripting setup works.
             * - Copy/pasting as a starter template.
             */

            context.onEnable {
                log.info("script enabled")
            }

            context.onDisable {
                log.info("script disabled")
            }

            context.onLogin { login ->
                log.info("login: ${'$'}login")
            }

            context.onLogout {
                log.info("logout")
            }
            """.trimIndent(),
        )

        addScript(
            "Examples: Connection Logger",
            """
            /*
             * Examples: Connection Logger
             * -----------------------------
             * What this does:
             * - Logs when the client-side and server-side game channels connect/disconnect.
             * - Also logs world-transfer events.
             *
             * Useful for:
             * - Understanding when packets start flowing.
             * - Debugging disconnects.
             */

            context.onClientConnected { info ->
                log.info("client connected: ${'$'}info")
            }

            context.onClientDisconnected { info ->
                log.info("client disconnected: ${'$'}info")
            }

            context.onServerConnected { info ->
                log.info("server connected: ${'$'}info")
            }

            context.onServerDisconnected { info ->
                log.info("server disconnected: ${'$'}info")
            }

            context.onWorldChanged { transfer ->
                log.info("world change: ${'$'}transfer")
            }
            """.trimIndent(),
        )

        addScript(
            "Examples: Tick Counter",
            """
            /*
             * Examples: Tick Counter
             * ------------------------
             * What this does:
             * - Demonstrates the 600ms game-tick hook.
             * - Prints once every 10 ticks so it doesn't spam.
             *
             * Notes:
             * - This is a simple way to build periodic logic without dealing with packets.
             */

            var ticks = 0
            context.onTick {
                ticks += 1
                if (ticks % 10 == 0) {
                    log.info("ticks=${'$'}ticks")
                }
            }
            """.trimIndent(),
        )

        addScript(
            "Examples: Rate-Limited Packet Names",
            """
            /*
             * Examples: Rate-Limited Packet Names
             * ------------------------------------
             * What this does:
             * - Logs packet prot names for both directions, but with a simple rate limit.
             *
             * How it works:
             * - Uses the catch-all listeners (onClientPacket / onServerPacket).
             * - Counts packets and logs only the first N per second.
             *
             * Try customizing:
             * - Increase/decrease maxPerSecond.
             * - Filter by prot name prefix or exact match.
             */

            val maxPerSecond = 5
            var c2s = 0
            var s2c = 0

            // Reset counters every second.
            context.every(1000) {
                c2s = 0
                s2c = 0
            }

            context.onClientPacket { packet ->
                if (c2s >= maxPerSecond) return@onClientPacket
                c2s += 1
                log.info("C->S ${'$'}{packet.prot}")
            }

            context.onServerPacket { packet ->
                if (s2c >= maxPerSecond) return@onServerPacket
                s2c += 1
                log.info("S->C ${'$'}{packet.prot}")
            }
            """.trimIndent(),
        )
    }

    private fun loadState() {
        enabledState.clear()
        try {
            if (stateFile.exists()) {
                val loaded: Map<String, Boolean> = mapper.readValue(stateFile.toFile())
                enabledState.putAll(loaded)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveState() {
        try {
            mapper.writeValue(stateFile.toFile(), enabledState)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadScriptsFromDirectory() {
        scripts.clear()
        for (path in listScriptFiles()) {
            try {
                val code = Files.readString(path)
                val name = path.name.removeSuffix(SCRIPT_SUFFIX)
                val enabled = enabledState[path.name] ?: false
                scripts.add(Script(name, code, enabled, filePath = path))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    public fun addScript(name: String, code: String): Script {
        val normalizedName = normalizeBaseName(name, fallback = "Script")
        val filePath = createUniqueScriptFile(normalizedName)
        writeScriptFile(filePath, code)

        enabledState[filePath.name] = false
        saveState()

        val script = Script(filePath.name.removeSuffix(SCRIPT_SUFFIX), code, enabled = false, filePath = filePath)
        scripts.add(script)
        return script
    }

    public fun updateScript(script: Script, name: String, code: String) {
        val wasEnabled = script.enabled
        if (wasEnabled) {
            disableScript(script)
        }

        val oldPath = requireNotNull(script.filePath) { "Script has no backing file" }
        val oldFileName = oldPath.name
        val normalizedName = normalizeBaseName(name, fallback = script.name)

        val desired = scriptsDir.resolve("$normalizedName$SCRIPT_SUFFIX")
        val newPath =
            if (desired == oldPath) {
                oldPath
            } else {
                createUniqueScriptFile(normalizedName, reserved = oldPath)
            }

        if (newPath != oldPath) {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING)
            enabledState[newPath.name] = enabledState.remove(oldFileName) ?: wasEnabled
        }

        writeScriptFile(newPath, code)

        script.name = newPath.name.removeSuffix(SCRIPT_SUFFIX)
        script.code = code
        script.filePath = newPath

        // Restore enabled state if it was enabled, but don't save yet as enableScript handles it
        if (wasEnabled) {
            enableScript(script)
        } else {
            saveState()
        }
    }

    public fun removeScript(script: Script) {
        disableScript(script)
        val path = script.filePath
        if (path != null) {
            try {
                Files.deleteIfExists(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            enabledState.remove(path.name)
        }
        scripts.remove(script)
        saveState()
    }

    public fun enableScript(script: Script, save: Boolean = true) {
        if (contexts.containsKey(script)) return

        val context = ProxyScriptContext()
        try {
            if (script.code.isBlank()) {
                contexts[script] = context
            } else {
                scriptingSupport.evaluate(script.code, context, script.name)
                contexts[script] = context
            }
            context.activate()
            script.enabled = true

            val path = requireNotNull(script.filePath) { "Script has no backing file" }
            enabledState[path.name] = true
            if (save) saveState()
        } catch (e: Exception) {
            throw RuntimeException("Failed to enable script '${script.name}': ${e.message}", e)
        }
    }

    public fun disableScript(script: Script, save: Boolean = true) {
        val context = contexts.remove(script)
        context?.close()
        script.enabled = false

        val path = script.filePath
        if (path != null) {
            enabledState[path.name] = false
        }
        if (save) saveState()
    }

    public fun toggleScript(script: Script) {
        if (script.enabled) {
            disableScript(script)
        } else {
            enableScript(script)
        }
    }

    private fun listScriptFiles(): List<Path> {
        if (!scriptsDir.exists() || !scriptsDir.isDirectory()) return emptyList()
        return Files.list(scriptsDir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .filter { it.fileName.toString().endsWith(SCRIPT_SUFFIX) }
                .sorted { a, b -> a.fileName.toString().compareTo(b.fileName.toString()) }
                .collect(Collectors.toList())
        }
    }

    private fun writeScriptFile(path: Path, code: String) {
        Files.writeString(path, code, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun normalizeBaseName(name: String, fallback: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return fallback

        // Keep names user-friendly (spaces are fine), but strip characters that don't belong in a filename.
        val cleaned =
            trimmed
                .replace("/", "_")
                .replace("\\\\", "_")
                .replace(":", "-")
                .replace("\u0000", "")
                .trim()

        return if (cleaned.isEmpty()) fallback else cleaned
    }

    private fun createUniqueScriptFile(baseName: String, reserved: Path? = null): Path {
        val normalizedBase = normalizeBaseName(baseName, fallback = "Script")
        var candidate = scriptsDir.resolve("$normalizedBase$SCRIPT_SUFFIX")
        var i = 2
        while (candidate.exists() && candidate != reserved) {
            candidate = scriptsDir.resolve("$normalizedBase ($i)$SCRIPT_SUFFIX")
            i++
        }
        return candidate
    }

    private companion object {
        private const val SCRIPT_SUFFIX: String = ".rsprox.kts"
    }
}
