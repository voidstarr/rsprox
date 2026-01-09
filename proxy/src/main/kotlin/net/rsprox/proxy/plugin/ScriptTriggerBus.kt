package net.rsprox.proxy.plugin

import net.rsprox.scripting.api.ConnectionInfo
import net.rsprox.scripting.api.LoginInfo
import net.rsprox.scripting.api.WorldTransfer
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Host-side trigger fanout for script events that are not packet-based.
 */
public object ScriptTriggerBus {
    private val clientConnected = CopyOnWriteArrayList<(ConnectionInfo) -> Unit>()
    private val clientDisconnected = CopyOnWriteArrayList<(ConnectionInfo) -> Unit>()
    private val serverConnected = CopyOnWriteArrayList<(ConnectionInfo) -> Unit>()
    private val serverDisconnected = CopyOnWriteArrayList<(ConnectionInfo) -> Unit>()
    private val login = CopyOnWriteArrayList<(LoginInfo) -> Unit>()
    private val worldChanged = CopyOnWriteArrayList<(WorldTransfer) -> Unit>()

    public fun addClientConnected(handler: (ConnectionInfo) -> Unit) {
        clientConnected.add(handler)
    }

    public fun removeClientConnected(handler: (ConnectionInfo) -> Unit) {
        clientConnected.remove(handler)
    }

    public fun addClientDisconnected(handler: (ConnectionInfo) -> Unit) {
        clientDisconnected.add(handler)
    }

    public fun removeClientDisconnected(handler: (ConnectionInfo) -> Unit) {
        clientDisconnected.remove(handler)
    }

    public fun addServerConnected(handler: (ConnectionInfo) -> Unit) {
        serverConnected.add(handler)
    }

    public fun removeServerConnected(handler: (ConnectionInfo) -> Unit) {
        serverConnected.remove(handler)
    }

    public fun addServerDisconnected(handler: (ConnectionInfo) -> Unit) {
        serverDisconnected.add(handler)
    }

    public fun removeServerDisconnected(handler: (ConnectionInfo) -> Unit) {
        serverDisconnected.remove(handler)
    }

    public fun addLogin(handler: (LoginInfo) -> Unit) {
        login.add(handler)
    }

    public fun removeLogin(handler: (LoginInfo) -> Unit) {
        login.remove(handler)
    }

    public fun addWorldChanged(handler: (WorldTransfer) -> Unit) {
        worldChanged.add(handler)
    }

    public fun removeWorldChanged(handler: (WorldTransfer) -> Unit) {
        worldChanged.remove(handler)
    }

    public fun fireClientConnected(info: ConnectionInfo) {
        for (h in clientConnected) h(info)
    }

    public fun fireClientDisconnected(info: ConnectionInfo) {
        for (h in clientDisconnected) h(info)
    }

    public fun fireServerConnected(info: ConnectionInfo) {
        for (h in serverConnected) h(info)
    }

    public fun fireServerDisconnected(info: ConnectionInfo) {
        for (h in serverDisconnected) h(info)
    }

    public fun fireLogin(info: LoginInfo) {
        for (h in login) h(info)
    }

    public fun fireWorldChanged(info: WorldTransfer) {
        for (h in worldChanged) h(info)
    }
}
