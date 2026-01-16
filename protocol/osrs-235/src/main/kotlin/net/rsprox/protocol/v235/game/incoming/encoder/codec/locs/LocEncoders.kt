package net.rsprox.protocol.v235.game.incoming.encoder.codec.locs

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6
import net.rsprox.protocol.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.incoming.decoder.prot.GameClientProt

public class OpLoc1Encoder : ProxyMessageEncoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC1

    override fun encode(
        message: OpLoc,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt2(message.z)
        buf.p2(message.x)
        buf.p1Alt3(if (message.controlKey) 1 else 0)
        buf.p2Alt1(message.id)
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
        buf.p1Alt1(if (message.controlKey) 1 else 0)
        buf.p2(message.x)
        buf.p2Alt2(message.id)
        buf.p2(message.z)
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
        buf.p2(message.z)
        buf.p2Alt1(message.id)
        buf.p2(message.x)
        buf.p1Alt1(if (message.controlKey) 1 else 0)
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
        buf.p1(if (message.controlKey) 1 else 0)
        buf.p2(message.x)
        buf.p2(message.z)
        buf.p2Alt2(message.id)
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
        buf.p2(message.x)
        buf.p2Alt2(message.z)
        buf.p2(message.id)
        buf.p1Alt1(if (message.controlKey) 1 else 0)
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
        buf.p2Alt1(message.id)
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
        buf.p2(message.selectedSub)
        buf.p1(if (message.controlKey) 1 else 0)
        buf.p2(message.selectedObj)
        buf.p4(message.selectedCombinedId)
        buf.p2Alt2(message.x)
        buf.p2Alt2(message.z)
        buf.p2(message.id)
        return buf.buffer
    }
}
