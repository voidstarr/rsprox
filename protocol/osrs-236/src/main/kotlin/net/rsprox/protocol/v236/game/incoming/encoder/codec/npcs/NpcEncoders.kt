package net.rsprox.protocol.v236.game.incoming.encoder.codec.npcs

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc6
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpNpc1Encoder : ProxyMessageEncoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC1

    override fun encode(
        message: OpNpc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // index: g2Alt1
        buf.p2Alt1(message.index)
        // controlKey: g1Alt1
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpNpc2Encoder : ProxyMessageEncoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC2

    override fun encode(
        message: OpNpc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // index: g2
        buf.p2(message.index)
        // controlKey: g1Alt2
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpNpc3Encoder : ProxyMessageEncoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC3

    override fun encode(
        message: OpNpc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // index: g2Alt1
        buf.p2Alt1(message.index)
        // controlKey: g1Alt1
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpNpc4Encoder : ProxyMessageEncoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC4

    override fun encode(
        message: OpNpc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // index: g2Alt1
        buf.p2Alt1(message.index)
        // controlKey: g1Alt2
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpNpc5Encoder : ProxyMessageEncoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC5

    override fun encode(
        message: OpNpc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // index: g2Alt1
        buf.p2Alt1(message.index)
        // controlKey: g1Alt3
        buf.p1Alt3(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpNpc6Encoder : ProxyMessageEncoder<OpNpc6> {
    override val prot: ClientProt = GameClientProt.OPNPC6

    override fun encode(
        message: OpNpc6,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // id: g2Alt1
        buf.p2Alt1(message.id)
        return buf.buffer
    }
}

public class OpNpcTEncoder : ProxyMessageEncoder<OpNpcT> {
    override val prot: ClientProt = GameClientProt.OPNPCT

    override fun encode(
        message: OpNpcT,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // index: g2Alt2
        buf.p2Alt2(message.index)
        // selectedSub: g2Alt3
        buf.p2Alt3(message.selectedSub)
        // selectedCombinedId: gCombinedIdAlt3 -> p4Alt3
        buf.p4Alt3(message.selectedCombinedId)
        // controlKey: g1Alt1
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        // selectedObj: g2Alt2
        buf.p2Alt2(message.selectedObj)
        return buf.buffer
    }
}
