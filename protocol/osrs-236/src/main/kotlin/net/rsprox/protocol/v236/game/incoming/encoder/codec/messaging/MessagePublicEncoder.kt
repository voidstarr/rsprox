package net.rsprox.protocol.v236.game.incoming.encoder.codec.messaging

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class MessagePublicEncoder(
    private val huffmanCodec: HuffmanCodec,
) : ProxyMessageEncoder<MessagePublic> {
    override val prot: ClientProt = GameClientProt.MESSAGE_PUBLIC

    override fun encode(
        message: MessagePublic,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.type)
        buf.p1(message.colour)
        buf.p1(message.effect)
        if (message.pattern != null) {
            val patternArray = message.pattern!!.asByteArray()
            for (patternByte in patternArray) {
                buf.p1(patternByte.toInt())
            }
        }

        huffmanCodec.encode(buf, message.message)

        if (message.type == 3) { // CLAN_MAIN_CHANNEL_TYPE
            buf.p1(message.clanType)
        }
        return buf.buffer
    }
}
