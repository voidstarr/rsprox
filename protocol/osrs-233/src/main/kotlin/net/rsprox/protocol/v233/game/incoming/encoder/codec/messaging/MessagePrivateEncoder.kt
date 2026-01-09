package net.rsprox.protocol.v233.game.incoming.encoder.codec.messaging

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

public class MessagePrivateEncoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageEncoder<MessagePrivate> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PRIVATE

    override fun encode(
        message: MessagePrivate,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        huffmanCodec.encode(buf, message.message)
        return buf.buffer
    }
}

