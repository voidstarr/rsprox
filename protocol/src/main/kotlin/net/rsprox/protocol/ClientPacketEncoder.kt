package net.rsprox.protocol

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.session.Session

public fun interface ClientPacketEncoder {
    public fun encode(
        prot: ClientProt,
        message: IncomingGameMessage,
        session: Session,
    ): ByteBuf
}
