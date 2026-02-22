package net.rsprox.protocol.v236.game.incoming.encoder.codec.objs

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.objs.OpObj
import net.rsprox.protocol.game.incoming.model.objs.OpObj6
import net.rsprox.protocol.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class OpObj1Encoder : ProxyMessageEncoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ1

    override fun encode(
        message: OpObj,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // id: g2
        buf.p2(message.id)
        // x: g2Alt1
        buf.p2Alt1(message.x)
        // z: g2
        buf.p2(message.z)
        // controlKey: g1Alt3
        buf.p1Alt3(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpObj2Encoder : ProxyMessageEncoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ2

    override fun encode(
        message: OpObj,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // x: g2Alt2
        buf.p2Alt2(message.x)
        // controlKey: g1Alt1
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        // z: g2Alt1
        buf.p2Alt1(message.z)
        // id: g2Alt1
        buf.p2Alt1(message.id)
        return buf.buffer
    }
}

public class OpObj3Encoder : ProxyMessageEncoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ3

    override fun encode(
        message: OpObj,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // id: g2Alt1
        buf.p2Alt1(message.id)
        // z: g2Alt2
        buf.p2Alt2(message.z)
        // controlKey: g1Alt2
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        // x: g2
        buf.p2(message.x)
        return buf.buffer
    }
}

public class OpObj4Encoder : ProxyMessageEncoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ4

    override fun encode(
        message: OpObj,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // z: g2Alt1
        buf.p2Alt1(message.z)
        // controlKey: g1Alt2
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        // id: g2
        buf.p2(message.id)
        // x: g2
        buf.p2(message.x)
        return buf.buffer
    }
}

public class OpObj5Encoder : ProxyMessageEncoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ5

    override fun encode(
        message: OpObj,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // z: g2Alt3
        buf.p2Alt3(message.z)
        // controlKey: g1Alt2
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        // x: g2Alt1
        buf.p2Alt1(message.x)
        // id: g2Alt2
        buf.p2Alt2(message.id)
        return buf.buffer
    }
}

public class OpObj6Encoder : ProxyMessageEncoder<OpObj6> {
    override val prot: ClientProt = GameClientProt.OPOBJ6

    override fun encode(
        message: OpObj6,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // z: g2Alt1
        buf.p2Alt1(message.z)
        // id: g2Alt2
        buf.p2Alt2(message.id)
        // x: g2Alt2
        buf.p2Alt2(message.x)
        return buf.buffer
    }
}

public class OpObjTEncoder : ProxyMessageEncoder<OpObjT> {
    override val prot: ClientProt = GameClientProt.OPOBJT

    override fun encode(
        message: OpObjT,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // selectedCombinedId: gCombinedIdAlt2 -> p4Alt2
        buf.p4Alt2(message.selectedCombinedId)
        // z: g2Alt2
        buf.p2Alt2(message.z)
        // id: g2Alt3
        buf.p2Alt3(message.id)
        // controlKey: g1Alt2
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        // selectedObj: g2
        buf.p2(message.selectedObj)
        // x: g2Alt1
        buf.p2Alt1(message.x)
        // selectedSub: g2
        buf.p2(message.selectedSub)
        return buf.buffer
    }
}
