package net.rsprox.proxy.plugin

import net.rsprox.proxy.client.ClientPacket
import net.rsprox.proxy.server.ServerPacket
import net.rsprot.protocol.Prot
import net.rsprox.scripting.api.ConnectionInfo
import net.rsprox.scripting.api.LoginInfo
import net.rsprox.scripting.api.WorldTransfer

/**
 * A disposable registration handle returned by script hooks.
 */
public interface Subscription : AutoCloseable {
    public fun cancel()

    override fun close() {
        cancel()
    }
}

/**
 * Script-facing API surface.
 *
 * This is intentionally an interface so the proxy host can provide an implementation
 * without exposing host internals as part of the scripting API.
 */
public interface ScriptContext : AutoCloseable {
    /**
     * Fires once after a script is evaluated and enabled.
     */
    public fun onEnable(handler: () -> Unit): Subscription

    /**
     * Fires when a script is disabled (before its packet hooks are torn down).
     */
    public fun onDisable(handler: () -> Unit): Subscription

    /**
     * Fires when the client-side game channel becomes active.
     */
    public fun onClientConnected(handler: (ConnectionInfo) -> Unit): Subscription

    public fun onClientDisconnected(handler: (ConnectionInfo) -> Unit): Subscription

    /**
     * Fires when the server-side game channel becomes active.
     */
    public fun onServerConnected(handler: (ConnectionInfo) -> Unit): Subscription

    public fun onServerDisconnected(handler: (ConnectionInfo) -> Unit): Subscription

    /**
     * Fires when the session switches into game decoding (successful login).
     */
    public fun onLogin(handler: (LoginInfo) -> Unit): Subscription

    /**
     * Fires when the session logs out (best-effort; typically derived from server logout packets).
     */
    public fun onLogout(handler: () -> Unit): Subscription

    /**
     * Fires when the server sends a world transfer instruction.
     */
    public fun onWorldChanged(handler: (WorldTransfer) -> Unit): Subscription

    /**
     * Runs every OSRS game tick (600ms).
     */
    public fun onTick(handler: () -> Unit): Subscription

    /**
     * Runs periodically at the given interval.
     */
    public fun every(periodMillis: Long, handler: () -> Unit): Subscription

    /**
     * Listen for all client->server packets.
     */
    public fun onClientPacket(handler: (ClientPacket<*>) -> Unit): Subscription

    public fun <T : Prot> onClientPacket(prot: T, handler: (ClientPacket<T>) -> Unit): Subscription

    /**
     * Listen for all server->client packets.
     */
    public fun onServerPacket(handler: (ServerPacket<*>) -> Unit): Subscription

    public fun <T : Prot> onServerPacket(prot: T, handler: (ServerPacket<T>) -> Unit): Subscription

    public fun onClientPacket(protName: String, handler: (ClientPacket<*>) -> Unit): Subscription

    public fun onServerPacket(protName: String, handler: (ServerPacket<*>) -> Unit): Subscription

    /**
     * Register a client->server packet transformer.
     *
     * Return the (possibly modified) packet to continue, or return null to drop it.
     */
    public fun onClientTransform(transformer: (ClientPacket<*>) -> ClientPacket<*>?): Subscription

    /**
     * Register a server->client packet transformer.
     *
     * Return the (possibly modified) packet to continue, or return null to drop it.
     */
    public fun onServerTransform(transformer: (ServerPacket<*>) -> ServerPacket<*>?): Subscription

    /**
     * Legacy API for older scripts / host integrations.
     */
    @Deprecated("Use close()", ReplaceWith("close()"))
    public fun cleanup() {
        close()
    }
}
