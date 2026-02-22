package net.rsprox.protocol.v236.game.incoming.encoder.codec.locs

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6
import net.rsprox.protocol.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpLoc1Encoder : ProxyMessageEncoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC1

    override fun encode(
        message: OpLoc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // controlKey: g1Alt1
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        // z: g2Alt3
        buf.p2Alt3(message.z)
        // id: g2
        buf.p2(message.id)
        // x: g2Alt2
        buf.p2Alt2(message.x)
        return buf.buffer
    }
}

public class OpLoc2Encoder : ProxyMessageEncoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC2

    override fun encode(
        message: OpLoc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // x: g2
        buf.p2(message.x)
        // id: g2Alt3
        buf.p2Alt3(message.id)
        // z: g2
        buf.p2(message.z)
        // controlKey: g1
        buf.p1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpLoc3Encoder : ProxyMessageEncoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC3

    override fun encode(
        message: OpLoc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // id: g2Alt2
        buf.p2Alt2(message.id)
        // z: g2Alt1
        buf.p2Alt1(message.z)
        // x: g2
        buf.p2(message.x)
        // controlKey: g1Alt2
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpLoc4Encoder : ProxyMessageEncoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC4

    override fun encode(
        message: OpLoc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // z: g2
        buf.p2(message.z)
        // controlKey: g1
        buf.p1(if (message.controlKey) 1 else 0)
        // id: g2Alt1
        buf.p2Alt1(message.id)
        // x: g2Alt2
        buf.p2Alt2(message.x)
        return buf.buffer
    }
}

public class OpLoc5Encoder : ProxyMessageEncoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC5

    override fun encode(
        message: OpLoc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // id: g2Alt3
        buf.p2Alt3(message.id)
        // z: g2Alt1
        buf.p2Alt1(message.z)
        // controlKey: g1
        buf.p1(if (message.controlKey) 1 else 0)
        // x: g2Alt2
        buf.p2Alt2(message.x)
        return buf.buffer
    }
}

public class OpLoc6Encoder : ProxyMessageEncoder<OpLoc6> {
    override val prot: ClientProt = GameClientProt.OPLOC6

    override fun encode(
        message: OpLoc6,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // id: g2Alt3
        buf.p2Alt3(message.id)
        return buf.buffer
    }
}

public class OpLocTEncoder : ProxyMessageEncoder<OpLocT> {
    override val prot: ClientProt = GameClientProt.OPLOCT

    override fun encode(
        message: OpLocT,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // selectedSub: g2Alt3
        buf.p2Alt3(message.selectedSub)
        // x: g2Alt3
        buf.p2Alt3(message.x)
        // selectedCombinedId: gCombinedIdAlt2 -> p4Alt2
        buf.p4Alt2(message.selectedCombinedId)
        // controlKey: g1Alt1
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        // id: g2
        buf.p2(message.id)
        // z: g2
        buf.p2(message.z)
        // selectedObj: g2Alt2
        buf.p2Alt2(message.selectedObj)
        return buf.buffer
    }
}
