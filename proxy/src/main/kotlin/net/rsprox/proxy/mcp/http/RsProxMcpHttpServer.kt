package net.rsprox.proxy.mcp.http

import com.github.michaelbull.logging.InlineLogger
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import net.rsprox.proxy.mcp.RsProxMcpServer
import net.rsprox.proxy.mcp.transcript.TranscriptStore
import java.io.Closeable

public class RsProxMcpHttpServer(
    private val bindAddress: String,
    private val port: Int,
    private val transcriptStore: TranscriptStore,
) : Closeable {
    private var ktorServer: EmbeddedServer<*, *>? = null

    public val isRunning: Boolean
        get() = ktorServer != null

    public fun start() {
        check(ktorServer == null) { "MCP HTTP server already running" }

        val embedded = embeddedServer(CIO, host = bindAddress, port = port) {
            mcp {
                RsProxMcpServer(transcriptStore = transcriptStore).createSdkServer()
            }
        }

        embedded.start(wait = false)
        ktorServer = embedded
        logger.info { "MCP HTTP server listening on $bindAddress:$port" }
    }

    public fun stop() {
        val s = ktorServer
        ktorServer = null

        runCatching {
            s?.stop(gracePeriodMillis = 200, timeoutMillis = 1000)
        }
    }

    public override fun close() {
        stop()
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
