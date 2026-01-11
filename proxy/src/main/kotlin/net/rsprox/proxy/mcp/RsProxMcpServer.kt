package net.rsprox.proxy.mcp

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.resolver.HistoricCacheResolver
import net.rsprox.proxy.binary.BinaryBlob
import net.rsprox.proxy.cache.CachedCaches
import net.rsprox.proxy.cache.StatefulCacheProvider
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.config.FILTERS_DIRECTORY
import net.rsprox.proxy.config.SETTINGS_DIRECTORY
import net.rsprox.proxy.filters.DefaultPropertyFilterSetStore
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.proxy.mcp.transcript.TranscriptDirection
import net.rsprox.proxy.mcp.transcript.TranscriptStore
import net.rsprox.proxy.plugin.DecoderLoader
import net.rsprox.proxy.plugin.DecodingSession
import net.rsprox.proxy.settings.DefaultSettingSetStore
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.indexing.NopBinaryIndex
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.transcriber.MessageConsumer
import net.rsprox.transcriber.prot.GameClientProt
import net.rsprox.transcriber.prot.GameServerProt
import net.rsprox.transcriber.state.SessionState
import net.rsprox.transcriber.state.SessionTracker
import net.rsprox.transcriber.text.TextMessageConsumerContainer
import net.rsprox.transcriber.text.TextTranscriberProvider
import net.rsprox.proxy.util.NopSessionMonitor
import net.rsprox.proxy.util.TranscribeCallback
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.streams.toList
import kotlinx.serialization.json.JsonObject as KJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * MCP (Model Context Protocol) tool server for inspecting RSProx captures.
 *
 * This class defines the MCP tools; RSProx hosts it over HTTP via
 * [net.rsprox.proxy.mcp.http.RsProxMcpHttpServer] (no standalone stdio entrypoint in this repo).
 *
 * Exposed tools include:
 * - listing recent `.bin` capture files under [BINARY_PATH]
 * - reading capture header metadata
 * - tailing (or generating) the transcriber transcript with basic filtering
 * - optional live transcript queries when a [TranscriptStore] is provided
 */
public class RsProxMcpServer(
    transcriptStore: TranscriptStore? = null,
) {
    private val gson: Gson = Gson()
    private val transcriptStore: TranscriptStore? = transcriptStore

    private val cacheProvider = StatefulCacheProvider(CachedCaches(HistoricCacheResolver()))
    private val decoderLoader = DecoderLoader()
    private val filters = DefaultPropertyFilterSetStore.load(FILTERS_DIRECTORY)
    private val settings = DefaultSettingSetStore.load(SETTINGS_DIRECTORY)

    public fun createSdkServer(): Server {
        if (INITIALIZED.compareAndSet(false, true)) {
            HuffmanProvider.load()
        }

        val options =
            ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(listChanged = false),
                ),
            )

        return Server(
            serverInfo = Implementation(name = "rsprox-proxy-mcp", version = "0.1.0"),
            options = options,
            instructionsProvider = {
                "RSProx MCP server exposing tools for inspecting binary captures and (optionally) live transcript events."
            },
        ) {
            registerTools(this)
        }
    }

    private companion object {
        private val INITIALIZED = AtomicBoolean(false)
    }

    private fun registerTools(server: Server) {
        server.addTool(
            name = "binary_list",
            description = "List recent RSProx .bin capture files (newest first).",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("limit") {
                        put("type", "integer")
                        put("description", "Max files")
                        put("default", 25)
                        put("minimum", 1)
                        put("maximum", 500)
                    }
                },
            ),
        ) { request ->
            val args = toGsonObject(request.arguments)
            CallToolResult(content = listOf(TextContent(gson.toJson(binaryList(args)))))
        }

        server.addTool(
            name = "binary_header",
            description = "Read and return header info from a capture (defaults to latest).",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("path") {
                        put("type", "string")
                        put("description", "Relative or absolute path to .bin file")
                        put("nullable", true)
                    }
                },
            ),
        ) { request ->
            val args = toGsonObject(request.arguments)
            CallToolResult(content = listOf(TextContent(gson.toJson(binaryHeader(args)))))
        }

        server.addTool(
            name = "binary_transcript_tail",
            description = "Read the transcriber transcript (.txt) for a capture and return matching lines (defaults to latest).",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("path") {
                        put("type", "string")
                        put("description", "Relative or absolute path to .bin file")
                        put("nullable", true)
                    }
                    putJsonObject("limit") {
                        put("type", "integer")
                        put("description", "Max lines returned")
                        put("default", 400)
                        put("minimum", 1)
                        put("maximum", 50_000)
                    }
                    putJsonObject("contains") {
                        put("type", "string")
                        put("description", "Substring filter applied to transcript lines")
                        put("nullable", true)
                    }
                    putJsonObject("cycleStart") {
                        put("type", "integer")
                        put("description", "Only include cycles at/after this cycle")
                        put("nullable", true)
                        put("minimum", 0)
                    }
                    putJsonObject("cycleEnd") {
                        put("type", "integer")
                        put("description", "Only include cycles at/before this cycle")
                        put("nullable", true)
                        put("minimum", 0)
                    }
                },
            ),
        ) { request ->
            val args = toGsonObject(request.arguments)
            CallToolResult(content = listOf(TextContent(gson.toJson(binaryTranscriptTail(args)))))
        }

        if (transcriptStore != null) {
            server.addTool(
                name = "live_sessions",
                description = "List active/seen live transcript sessions (GUI-embedded MCP only).",
                inputSchema = ToolSchema(properties = buildJsonObject { }),
            ) { _ ->
                CallToolResult(content = listOf(TextContent(gson.toJson(liveSessions()))))
            }

            server.addTool(
                name = "live_recent",
                description = "Fetch recent live transcribed events from the running GUI (GUI-embedded MCP only).",
                inputSchema = ToolSchema(
                    properties = buildJsonObject {
                        putJsonObject("sinceSeq") {
                            put("type", "integer")
                            put("description", "Only return events with seq > sinceSeq")
                            put("default", 0)
                            put("minimum", 0)
                        }
                        putJsonObject("limit") {
                            put("type", "integer")
                            put("description", "Max events returned")
                            put("default", 200)
                            put("minimum", 1)
                            put("maximum", 5000)
                        }
                        putJsonObject("direction") {
                            put("type", "string")
                            put("description", "Packet direction filter")
                            put("default", "any")
                            put("enum", buildJsonArray {
                                add(JsonPrimitive("c2s"))
                                add(JsonPrimitive("s2c"))
                                add(JsonPrimitive("any"))
                            })
                        }
                        putJsonObject("contains") {
                            put("type", "string")
                            put("description", "Substring filter applied to transcript lines/prot")
                            put("nullable", true)
                        }
                        putJsonObject("cycleStart") {
                            put("type", "integer")
                            put("description", "Only include cycles at/after this cycle")
                            put("nullable", true)
                            put("minimum", 0)
                        }
                        putJsonObject("cycleEnd") {
                            put("type", "integer")
                            put("description", "Only include cycles at/before this cycle")
                            put("nullable", true)
                            put("minimum", 0)
                        }
                        putJsonObject("port") {
                            put("type", "integer")
                            put("description", "Only include events from this session port")
                            put("nullable", true)
                            put("minimum", 0)
                        }
                    },
                ),
            ) { request ->
                val args = toGsonObject(request.arguments)
                CallToolResult(content = listOf(TextContent(gson.toJson(liveRecent(args)))))
            }
        }
    }

    private fun toGsonObject(arguments: KJsonObject?): JsonObject {
        if (arguments == null) return JsonObject()
        return JsonParser.parseString(arguments.toString()).asJsonObject
    }

    private fun binaryList(arguments: JsonObject): Any {
        val limit = arguments["limit"]?.asInt ?: 25

        if (!Files.exists(BINARY_PATH)) {
            return mapOf(
                "binaryPath" to BINARY_PATH.toString(),
                "files" to emptyList<Any>(),
                "note" to "Binary directory does not exist",
            )
        }

        val files =
            Files.walk(BINARY_PATH).use { stream ->
                stream
                    .filter { Files.isRegularFile(it) && it.extension == "bin" }
                    .map { path ->
                        val mtime = Files.getLastModifiedTime(path).toMillis()
                        mapOf(
                            "path" to BINARY_PATH.relativize(path).toString(),
                            "name" to path.name,
                            "modified" to Instant.ofEpochMilli(mtime).toString(),
                            "modifiedEpochMillis" to mtime,
                            "size" to Files.size(path),
                        )
                    }
                    .sorted { a, b ->
                        val am = (a["modifiedEpochMillis"] as Long)
                        val bm = (b["modifiedEpochMillis"] as Long)
                        bm.compareTo(am)
                    }
                    .limit(limit.toLong())
                    .toList()
            }

        return mapOf(
            "binaryPath" to BINARY_PATH.toString(),
            "count" to files.size,
            "files" to files,
        )
    }

    private fun binaryHeader(arguments: JsonObject): Any {
        val pathArg = arguments["path"]?.takeIf { !it.isJsonNull }?.asString
        val file = resolveBinaryPathOrLatest(pathArg)
            ?: return mapOf("error" to "No .bin files found under $BINARY_PATH")

        val binary = BinaryBlob.decode(file, filters, settings)

        return mapOf(
            "path" to displayBinaryPath(file),
            "header" to mapOf(
                "revision" to binary.header.revision,
                "subRevision" to binary.header.subRevision,
                "clientType" to binary.header.clientType,
                "platformType" to binary.header.platformType,
                "timestamp" to Instant.ofEpochMilli(binary.header.timestamp).toString(),
                "worldId" to binary.header.worldId,
                "worldHost" to binary.header.worldHost,
                "worldFlags" to binary.header.worldFlags,
                "worldLocation" to binary.header.worldLocation,
                "worldActivity" to binary.header.worldActivity,
                "localPlayerIndex" to binary.header.localPlayerIndex,
                "clientName" to binary.header.clientName,
                "fileName" to binary.header.fileName(),
            ),
        )
    }

    private fun liveSessions(): Any {
        val store = transcriptStore ?: return mapOf("enabled" to false)
        return mapOf(
            "enabled" to true,
            "latestSeq" to store.latestSeq(),
            "sessions" to store.listSessions(),
        )
    }

    private fun liveRecent(arguments: JsonObject): Any {
        val store = transcriptStore ?: return mapOf("enabled" to false)

        val sinceSeq = arguments["sinceSeq"]?.asLong ?: 0L
        val limit = arguments["limit"]?.asInt ?: 200
        val directionArg = arguments["direction"]?.asString ?: "any"
        val contains = arguments["contains"]?.takeIf { !it.isJsonNull }?.asString
        val cycleStart = arguments["cycleStart"]?.takeIf { !it.isJsonNull }?.asInt
        val cycleEnd = arguments["cycleEnd"]?.takeIf { !it.isJsonNull }?.asInt
        val port = arguments["port"]?.takeIf { !it.isJsonNull }?.asInt

        val direction: TranscriptDirection? =
            when (directionArg.lowercase()) {
                "c2s" -> TranscriptDirection.C2S
                "s2c" -> TranscriptDirection.S2C
                "any" -> null
                else -> null
            }

        val events =
            store.snapshot(
                sinceSeqExclusive = sinceSeq,
                limit = limit,
                port = port,
                direction = direction,
                cycleStart = cycleStart,
                cycleEnd = cycleEnd,
                contains = contains,
            )

        return mapOf(
            "enabled" to true,
            "latestSeq" to store.latestSeq(),
            "sinceSeq" to sinceSeq,
            "returned" to events.size,
            "events" to events,
        )
    }

    private fun binaryTranscriptTail(arguments: JsonObject): Any {
        val pathArg = arguments["path"]?.takeIf { !it.isJsonNull }?.asString
        val limit = arguments["limit"]?.asInt ?: 400
        val contains = arguments["contains"]?.takeIf { !it.isJsonNull }?.asString
        val cycleStart = arguments["cycleStart"]?.takeIf { !it.isJsonNull }?.asInt
        val cycleEnd = arguments["cycleEnd"]?.takeIf { !it.isJsonNull }?.asInt

        val file = resolveBinaryPathOrLatest(pathArg)
            ?: return mapOf("error" to "No .bin files found under $BINARY_PATH")

        val transcript = file.parent.resolve(file.nameWithoutExtension + ".txt")
        if (Files.exists(transcript)) {
            val lines = Files.readAllLines(transcript)
            val filtered = filterTranscriptLines(lines, limit, contains, cycleStart, cycleEnd)
            return mapOf(
                "path" to displayBinaryPath(file),
                "transcriptPath" to displayBinaryPath(transcript),
                "fromFile" to true,
                "returned" to filtered.size,
                "lines" to filtered,
            )
        }

        val lines = transcribeBinaryToLines(file)
        val filtered = filterTranscriptLines(lines, limit, contains, cycleStart, cycleEnd)
        return mapOf(
            "path" to displayBinaryPath(file),
            "transcriptPath" to displayBinaryPath(transcript),
            "fromFile" to false,
            "returned" to filtered.size,
            "lines" to filtered,
        )
    }

    private fun filterTranscriptLines(
        lines: List<String>,
        limit: Int,
        contains: String?,
        cycleStart: Int?,
        cycleEnd: Int?,
    ): List<String> {
        val containsLc = contains?.lowercase()
        var cycle: Int? = null

        val matches =
            lines.asSequence().mapNotNull { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    val num = trimmed.substring(1, trimmed.length - 1).toIntOrNull()
                    if (num != null) cycle = num
                    return@mapNotNull null
                }

                val c = cycle
                val cycleOk =
                    (cycleStart == null || (c != null && c >= cycleStart)) &&
                        (cycleEnd == null || (c != null && c <= cycleEnd))

                if (!cycleOk) return@mapNotNull null
                if (containsLc != null && !line.lowercase().contains(containsLc)) return@mapNotNull null
                line
            }.toList()

        return if (matches.size <= limit) matches else matches.takeLast(limit)
    }

    private fun transcribeBinaryToLines(binaryPath: Path): List<String> {
        val binary = BinaryBlob.decode(binaryPath, filters, settings)
        cacheProvider.update(
            Js5MasterIndex.trimmed(
                binary.header.revision,
                binary.header.js5MasterIndex,
            ),
        )
        decoderLoader.load(cacheProvider)
        val latestPlugin = decoderLoader.getDecoder(binary.header.revision, cacheProvider)

        val session = DecodingSession(binary, latestPlugin)
        val provider = TextTranscriberProvider()

        val outLines = mutableListOf<String>()
        val consumer =
            object : MessageConsumer {
                private var lastCycle = -1

                override fun consume(
                    formatter: PropertyTreeFormatter,
                    cycle: Int,
                    property: RootProperty,
                ) {
                    if (cycle != lastCycle) {
                        if (lastCycle != -1) outLines += ""
                        lastCycle = cycle
                        outLines += "[$cycle]"
                    }
                    val result = formatter.format(property)
                    for (line in result) {
                        outLines += "    $line"
                    }
                }

                override fun close() {
                }
            }

        val container = TextMessageConsumerContainer(listOf(consumer))
        val sessionState = SessionState(binary.header.revision, settings)
        val runner =
            provider.provide(
                container,
                cacheProvider,
                NopSessionMonitor,
                filters,
                settings,
                NopBinaryIndex,
                sessionState,
            )
        val tracker = SessionTracker(sessionState, cacheProvider.get(), NopSessionMonitor)

        val revision = binary.header.revision
        val callback =
            object : TranscribeCallback {
                override fun indeterminate(note: String) {
                }

                override fun report(percent: Int, note: String) {
                }

                override fun isCancelled(): Boolean = false
            }

        for ((direction, prot, packet) in session.sequence(callback)) {
            try {
                when (direction) {
                    StreamDirection.CLIENT_TO_SERVER -> {
                        tracker.onClientPacket(packet, prot)
                        tracker.beforeTranscribe(packet)
                        runner.onClientProt(prot, packet, revision)
                        tracker.afterTranscribe(packet)
                    }
                    StreamDirection.SERVER_TO_CLIENT -> {
                        tracker.onServerPacket(packet, prot)
                        tracker.beforeTranscribe(packet)
                        runner.onServerPacket(prot, packet, revision)
                        tracker.afterTranscribe(packet)
                    }
                }
            } catch (_: NotImplementedError) {
                continue
            }
        }

        container.close()
        return outLines
    }

    private fun resolveBinaryPathOrLatest(path: String?): Path? {
        if (path != null) {
            val p = Path.of(path)
            if (Files.exists(p)) return p
            val underBinary = BINARY_PATH.resolve(path)
            if (Files.exists(underBinary)) return underBinary
            return null
        }

        if (!Files.exists(BINARY_PATH)) return null

        return Files.walk(BINARY_PATH).use { stream ->
            stream
                .filter { Files.isRegularFile(it) && it.extension == "bin" }
                .max { a, b ->
                    val am = Files.getLastModifiedTime(a).toMillis()
                    val bm = Files.getLastModifiedTime(b).toMillis()
                    am.compareTo(bm)
                }
                .orElse(null)
        }
    }

    private fun displayBinaryPath(path: Path): String {
        return try {
            if (path.startsWith(BINARY_PATH)) {
                BINARY_PATH.relativize(path).toString()
            } else {
                path.toString()
            }
        } catch (_: Throwable) {
            path.toString()
        }
    }

}
