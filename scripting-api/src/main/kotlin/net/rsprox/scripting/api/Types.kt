package net.rsprox.scripting.api

/**
 * Lightweight connection metadata exposed to scripts.
 */
public data class ConnectionInfo(
    public val id: String,
    public val localAddress: String?,
    public val remoteAddress: String?,
    public val side: ConnectionSide,
)

public enum class ConnectionSide {
    CLIENT,
    SERVER,
}

/**
 * Fired when the proxy successfully switches a session into game packet decoding.
 */
public data class LoginInfo(
    public val revision: Int,
    public val members: Boolean,
    public val localPlayerIndex: Int,
)

/**
 * Fired when the server instructs the client to hop worlds.
 */
public data class WorldTransfer(
    public val originalHost: String,
    public val worldId: Int,
    public val properties: Int,
    public val redirectedHost: String,
)
