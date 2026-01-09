package net.rsprox.protocol

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.session.Session

public interface ProxyMessageEncoder<in T : IncomingGameMessage> {
    public val prot: ClientProt

    public fun encode(
        message: T,
        session: Session,
    ): ByteBuf
}

