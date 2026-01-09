package net.rsprox.proxy.plugin

import net.rsprox.proxy.client.ClientPacket
import net.rsprox.proxy.server.ServerPacket
import net.rsprot.protocol.Prot
import java.util.concurrent.CopyOnWriteArrayList

public object PacketTransforms {
    private val clientTransformers = CopyOnWriteArrayList<(ClientPacket<*>) -> ClientPacket<*>?>()
    private val serverTransformers = CopyOnWriteArrayList<(ServerPacket<*>) -> ServerPacket<*>?>()
    private val clientPacketListeners = CopyOnWriteArrayList<(ClientPacket<*>) -> Unit>()
    private val serverPacketListeners = CopyOnWriteArrayList<(ServerPacket<*>) -> Unit>()

    public fun addClientTransformer(transformer: (ClientPacket<*>) -> ClientPacket<*>?) {
        clientTransformers.add(transformer)
    }

    public fun removeClientTransformer(transformer: (ClientPacket<*>) -> ClientPacket<*>?) {
        clientTransformers.remove(transformer)
    }

    public fun addServerTransformer(transformer: (ServerPacket<*>) -> ServerPacket<*>?) {
        serverTransformers.add(transformer)
    }

    public fun removeServerTransformer(transformer: (ServerPacket<*>) -> ServerPacket<*>?) {
        serverTransformers.remove(transformer)
    }

    public fun addClientPacketListener(listener: (ClientPacket<*>) -> Unit) {
        clientPacketListeners.add(listener)
    }

    public fun removeClientPacketListener(listener: (ClientPacket<*>) -> Unit) {
        clientPacketListeners.remove(listener)
    }

    public fun addServerPacketListener(listener: (ServerPacket<*>) -> Unit) {
        serverPacketListeners.add(listener)
    }

    public fun removeServerPacketListener(listener: (ServerPacket<*>) -> Unit) {
        serverPacketListeners.remove(listener)
    }

    public fun transformClient(packet: ClientPacket<*>): ClientPacket<*>? {
        var current = packet
        for (transformer in clientTransformers) {
            current = transformer(current) ?: return null
        }
        for (listener in clientPacketListeners) {
            listener(current)
        }
        return current
    }

    public fun transformServer(packet: ServerPacket<*>): ServerPacket<*>? {
        var current = packet
        for (transformer in serverTransformers) {
            current = transformer(current) ?: return null
        }
        for (listener in serverPacketListeners) {
            listener(current)
        }
        return current
    }

    public fun <T : Prot> onClientPacket(prot: T, handler: (ClientPacket<T>) -> Unit): (ClientPacket<*>) -> Unit {
        val listener: (ClientPacket<*>) -> Unit = { packet ->
            if (packet.prot == prot) {
                @Suppress("UNCHECKED_CAST")
                handler(packet as ClientPacket<T>)
            }
        }
        addClientPacketListener(listener)
        return listener
    }

    public fun <T : Prot> onServerPacket(prot: T, handler: (ServerPacket<T>) -> Unit): (ServerPacket<*>) -> Unit {
        val listener: (ServerPacket<*>) -> Unit = { packet ->
            if (packet.prot == prot) {
                @Suppress("UNCHECKED_CAST")
                handler(packet as ServerPacket<T>)
            }
        }
        addServerPacketListener(listener)
        return listener
    }
}
