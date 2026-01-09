package net.rsprox.proxy.plugin

import net.rsprox.proxy.client.ClientPacket
import net.rsprox.proxy.server.ServerPacket
import net.rsprot.protocol.Prot
import net.rsprox.scripting.api.ConnectionInfo
import net.rsprox.scripting.api.LoginInfo
import net.rsprox.scripting.api.WorldTransfer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Proxy-host implementation of the script-facing [ScriptContext] API.
 */
public class ProxyScriptContext : ScriptContext {
    private val subscriptions = mutableListOf<Subscription>()
    private val enableHandlers = mutableListOf<() -> Unit>()
    private val disableHandlers = mutableListOf<() -> Unit>()
    private var enabled = false

    public fun activate() {
        if (enabled) return
        enabled = true
        // Run enable hooks after script evaluation completes.
        val toRun = enableHandlers.toList()
        for (h in toRun) {
            runCatching { h() }
        }
    }

    override fun onEnable(handler: () -> Unit): Subscription {
        enableHandlers.add(handler)
        if (enabled) {
            runCatching { handler() }
        }
        return track(
            cancel = { enableHandlers.remove(handler) },
        )
    }

    override fun onDisable(handler: () -> Unit): Subscription {
        disableHandlers.add(handler)
        return track(
            cancel = { disableHandlers.remove(handler) },
        )
    }

    override fun onClientConnected(handler: (ConnectionInfo) -> Unit): Subscription {
        ScriptTriggerBus.addClientConnected(handler)
        return track(
            cancel = { ScriptTriggerBus.removeClientConnected(handler) },
        )
    }

    override fun onClientDisconnected(handler: (ConnectionInfo) -> Unit): Subscription {
        ScriptTriggerBus.addClientDisconnected(handler)
        return track(
            cancel = { ScriptTriggerBus.removeClientDisconnected(handler) },
        )
    }

    override fun onServerConnected(handler: (ConnectionInfo) -> Unit): Subscription {
        ScriptTriggerBus.addServerConnected(handler)
        return track(
            cancel = { ScriptTriggerBus.removeServerConnected(handler) },
        )
    }

    override fun onServerDisconnected(handler: (ConnectionInfo) -> Unit): Subscription {
        ScriptTriggerBus.addServerDisconnected(handler)
        return track(
            cancel = { ScriptTriggerBus.removeServerDisconnected(handler) },
        )
    }

    override fun onLogin(handler: (LoginInfo) -> Unit): Subscription {
        ScriptTriggerBus.addLogin(handler)
        return track(
            cancel = { ScriptTriggerBus.removeLogin(handler) },
        )
    }

    override fun onLogout(handler: () -> Unit): Subscription {
        // Derived from server logout packets. (Disconnect-only logouts can still be observed
        // via onClientDisconnected/onServerDisconnected if needed.)
        val s1 = onServerPacket("LOGOUT") { handler() }
        val s2 = onServerPacket("LOGOUT_WITH_REASON") { handler() }
        return track(
            cancel = {
                s1.cancel()
                s2.cancel()
            },
        )
    }

    override fun onWorldChanged(handler: (WorldTransfer) -> Unit): Subscription {
        ScriptTriggerBus.addWorldChanged(handler)
        return track(
            cancel = { ScriptTriggerBus.removeWorldChanged(handler) },
        )
    }

    override fun onTick(handler: () -> Unit): Subscription {
        return every(600L, handler)
    }

    override fun every(periodMillis: Long, handler: () -> Unit): Subscription {
        require(periodMillis > 0) { "periodMillis must be > 0" }
        val future = scheduler.scheduleAtFixedRate(
            { runCatching { handler() } },
            periodMillis,
            periodMillis,
            TimeUnit.MILLISECONDS,
        )
        return trackScheduled(future)
    }

    override fun onClientPacket(handler: (ClientPacket<*>) -> Unit): Subscription {
        val listener: (ClientPacket<*>) -> Unit = { packet ->
            handler(packet)
        }
        PacketTransforms.addClientPacketListener(listener)
        return track(
            cancel = { PacketTransforms.removeClientPacketListener(listener) },
        )
    }

    override fun <T : Prot> onClientPacket(prot: T, handler: (ClientPacket<T>) -> Unit): Subscription {
        val listener = PacketTransforms.onClientPacket(prot, handler)
        return track(
            cancel = { PacketTransforms.removeClientPacketListener(listener) },
        )
    }

    override fun onServerPacket(handler: (ServerPacket<*>) -> Unit): Subscription {
        val listener: (ServerPacket<*>) -> Unit = { packet ->
            handler(packet)
        }
        PacketTransforms.addServerPacketListener(listener)
        return track(
            cancel = { PacketTransforms.removeServerPacketListener(listener) },
        )
    }

    override fun <T : Prot> onServerPacket(prot: T, handler: (ServerPacket<T>) -> Unit): Subscription {
        val listener = PacketTransforms.onServerPacket(prot, handler)
        return track(
            cancel = { PacketTransforms.removeServerPacketListener(listener) },
        )
    }

    override fun onClientPacket(protName: String, handler: (ClientPacket<*>) -> Unit): Subscription {
        val listener: (ClientPacket<*>) -> Unit = { packet ->
            if (packet.prot.toString() == protName) {
                handler(packet)
            }
        }
        PacketTransforms.addClientPacketListener(listener)
        return track(
            cancel = { PacketTransforms.removeClientPacketListener(listener) },
        )
    }

    override fun onServerPacket(protName: String, handler: (ServerPacket<*>) -> Unit): Subscription {
        val listener: (ServerPacket<*>) -> Unit = { packet ->
            if (packet.prot.toString() == protName) {
                handler(packet)
            }
        }
        PacketTransforms.addServerPacketListener(listener)
        return track(
            cancel = { PacketTransforms.removeServerPacketListener(listener) },
        )
    }

    override fun onClientTransform(transformer: (ClientPacket<*>) -> ClientPacket<*>?): Subscription {
        PacketTransforms.addClientTransformer(transformer)
        return track(
            cancel = { PacketTransforms.removeClientTransformer(transformer) },
        )
    }

    override fun onServerTransform(transformer: (ServerPacket<*>) -> ServerPacket<*>?): Subscription {
        PacketTransforms.addServerTransformer(transformer)
        return track(
            cancel = { PacketTransforms.removeServerTransformer(transformer) },
        )
    }

    override fun close() {
        // Run disable hooks first (before tearing down registrations).
        val toRun = disableHandlers.toList().asReversed()
        disableHandlers.clear()
        for (h in toRun) {
            runCatching { h() }
        }

        // Cancel in reverse order (helpful if there are dependencies between hooks).
        val toCancel = subscriptions.toList().asReversed()
        subscriptions.clear()
        for (sub in toCancel) {
            runCatching { sub.cancel() }
        }
    }

    private fun track(cancel: () -> Unit): Subscription {
        var cancelled = false
        val sub =
            object : Subscription {
                override fun cancel() {
                    if (cancelled) return
                    cancelled = true
                    cancel()
                    subscriptions.remove(this)
                }
            }
        subscriptions.add(sub)
        return sub
    }

    private fun trackScheduled(future: ScheduledFuture<*>): Subscription {
        return track(
            cancel = { future.cancel(false) },
        )
    }

    private companion object {
        private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "rsprox-script-timer").apply { isDaemon = true }
        }
    }
}
