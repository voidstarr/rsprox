package net.rsprox.proxy.util

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.rsprox.proxy.attributes.BINARY_BLOB
import net.rsprox.proxy.connection.ProxyConnectionContainer
import net.rsprox.proxy.plugin.ScriptTriggerBus
import net.rsprox.scripting.api.ConnectionInfo
import net.rsprox.scripting.api.ConnectionSide
import java.io.IOException

public class ChannelConnectionHandler(
    private val targetChannel: Channel,
    private val connections: ProxyConnectionContainer,
    private val side: ConnectionSide,
) : ChannelInboundHandlerAdapter() {
    private var firedActive = false

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        if (ctx.channel().isActive) {
            fireActiveOnce(ctx)
        }
        super.handlerAdded(ctx)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        fireActiveOnce(ctx)
        super.channelActive(ctx)
    }

    private fun fireActiveOnce(ctx: ChannelHandlerContext) {
        if (firedActive) return
        firedActive = true
        ScriptTriggerBus.fire(
            side,
            active = true,
            channel = ctx.channel(),
        )
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        ScriptTriggerBus.fire(
            side,
            active = false,
            channel = ctx.channel(),
        )
        if (targetChannel.isActive) {
            targetChannel.close()
        }
        val blob = targetChannel.attr(BINARY_BLOB).get() ?: return
        blob.close()
        connections.removeConnection(blob)
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(
        ctx: ChannelHandlerContext,
        cause: Throwable,
    ) {
        // Ignore IOExceptions as those tend to spam whenever something disconnects
        // Those exceptions are not very useful for us, but errors in our handling are.
        if (cause is IOException) {
            return
        }
        logger.error(cause) {
            "Exception in netty channel $ctx"
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}

private fun ScriptTriggerBus.fire(
    side: ConnectionSide,
    active: Boolean,
    channel: Channel,
) {
    val info =
        ConnectionInfo(
            id = channel.id().asShortText(),
            localAddress = channel.localAddress()?.toString(),
            remoteAddress = channel.remoteAddress()?.toString(),
            side = side,
        )
    when (side) {
        ConnectionSide.CLIENT -> {
            if (active) fireClientConnected(info) else fireClientDisconnected(info)
        }

        ConnectionSide.SERVER -> {
            if (active) fireServerConnected(info) else fireServerDisconnected(info)
        }
    }
}
