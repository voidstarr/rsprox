package net.rsprox.proxy.plugin

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ClientPacketEncoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.protocol.session.Session

public data class RevisionDecoder(
    public val revision: Int,
    private val clientPacketDecoder: ClientPacketDecoder,
    public val clientPacketEncoder: ClientPacketEncoder,
    private val serverPacketDecoder: ServerPacketDecoder,
    public val gameClientProtProvider: ProtProvider<ClientProt>,
    public val gameServerProtProvider: ProtProvider<ClientProt>,
) {
    public fun decodeClientPacket(
        opcode: Int,
        buffer: JagByteBuf,
        session: Session,
    ): IncomingMessage {
        return clientPacketDecoder.decode(
            opcode,
            buffer,
            session,
        )
    }

    public fun encodeClientPacket(
        prot: ClientProt,
        message: IncomingGameMessage,
        session: Session,
    ): ByteBuf {
        return clientPacketEncoder.encode(prot, message, session)
    }

    public fun decodeServerPacket(
        opcode: Int,
        buffer: JagByteBuf,
        session: Session,
    ): IncomingMessage {
        return serverPacketDecoder.decode(
            opcode,
            buffer,
            session,
        )
    }
}
