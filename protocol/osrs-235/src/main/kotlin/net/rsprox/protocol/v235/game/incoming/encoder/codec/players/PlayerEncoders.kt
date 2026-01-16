package net.rsprox.protocol.v235.game.incoming.encoder.codec.players

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.incoming.decoder.prot.GameClientProt

public class OpPlayer1Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER1

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt1(message.index)
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpPlayer2Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER2

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2(message.index)
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpPlayer3Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER3

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        buf.p2Alt3(message.index)
        return buf.buffer
    }
}

public class OpPlayer4Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER4

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2(message.index)
        buf.p1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpPlayer5Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER5

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt3(message.index)
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpPlayer6Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER6

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt2(message.index)
        buf.p1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpPlayer7Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER7

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2(message.index)
        buf.p1Alt3(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpPlayer8Encoder : ProxyMessageEncoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER8

    override fun encode(
        message: OpPlayer,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        buf.p2Alt3(message.index)
        return buf.buffer
    }
}

public class OpPlayerTEncoder : ProxyMessageEncoder<OpPlayerT> {
    override val prot: ClientProt = GameClientProt.OPPLAYERT

    override fun encode(
        message: OpPlayerT,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt3(if (message.controlKey) 1 else 0)
        buf.p2Alt2(message.selectedSub)
        buf.p2Alt3(message.selectedObj)
        buf.p2Alt3(message.index)
        buf.p4Alt1(message.selectedCombinedId)
        return buf.buffer
    }
}
