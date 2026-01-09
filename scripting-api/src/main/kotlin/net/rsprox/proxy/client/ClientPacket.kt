package net.rsprox.proxy.client

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.proxy.plugin.RevisionDecoder

public class ClientPacket<out T : Prot>(
    public val prot: T,
    private val cipherMod: Int,
    private var _payload: ByteBuf,
) {
    public var revisionDecoder: RevisionDecoder? = null
    public val payload: ByteBuf
        get() = _payload
    private var _start: Int = payload.readerIndex()
    public val start: Int
        get() = _start

    public fun encode(
        allocator: ByteBufAllocator,
        mod: Boolean = true,
    ): ByteBuf {
        // Ensure we always transfer the data from the first byte that the packet began at
        payload.readerIndex(start)
        val actualSize = payload.readableBytes()
        // Allocate a buffer that can handle payload + opcode + var-short size
        val buf = allocator.buffer(actualSize + 3).toJagByteBuf()
        val modToUse = if (mod) this.cipherMod else 0
        buf.p1((prot.opcode + modToUse) and 0xff)
        val constantSize = prot.size
        if (constantSize == Prot.VAR_BYTE) {
            buf.p1(actualSize)
        } else if (constantSize == Prot.VAR_SHORT) {
            buf.p2(actualSize)
        }
        buf.pdata(payload)
        return buf.buffer
    }

    public inline fun <reified M : IncomingGameMessage> decode(): M {
        val decoder = this.revisionDecoder ?: error("Decoder not available")
        val session = Session(0, AttributeMap())
        val slice = payload.retainedSlice()
        val jagBuf = slice.toJagByteBuf()
        jagBuf.readerIndex(start)
        try {
            val message = decoder.decodeClientPacket(prot.opcode, jagBuf, session)
            if (message !is M) throw ClassCastException("Expected ${M::class.simpleName}, got ${message::class.simpleName}")
            return message
        } finally {
            slice.release()
        }
    }

    public fun encode(message: IncomingGameMessage) {
        val decoder = this.revisionDecoder ?: error("Decoder not available")
        val session = Session(0, AttributeMap())
        val p = prot
        if (p is ClientProt) {
            val buf = decoder.encodeClientPacket(p, message, session)
            replacePayload(buf)
        } else {
            throw IllegalStateException("Cannot encode message: protocol '${prot}' is not a ClientProt")
        }
    }

    public fun replacePayload(buf: ByteBuf) {
        val old = this._payload
        try {
            this._payload = buf
            this._start = buf.readerIndex()
        } finally {
            old.release()
        }
    }

    override fun toString(): String {
        return "Packet(" +
            "prot=$prot, " +
            "payload=$payload" +
            ")"
    }
}
