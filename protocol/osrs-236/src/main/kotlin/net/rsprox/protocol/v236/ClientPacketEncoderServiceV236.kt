package net.rsprox.protocol.v236

import io.netty.buffer.ByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprox.protocol.ClientPacketEncoder
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.encoder.prot.ClientMessageEncoderRepository

public class ClientPacketEncoderServiceV236(
    huffmanCodec: HuffmanCodec,
) : ClientPacketEncoder {
    private val repository = ClientMessageEncoderRepository.build(huffmanCodec)

    override fun encode(
        prot: ClientProt,
        message: IncomingGameMessage,
        session: Session,
    ): ByteBuf {
        return repository
            .getEncoder(prot)
            .encode(message, session)
    }
}
