package net.rsprox.protocol.v233.game.incoming.encoder.codec.worldentities

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity6
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntityT
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

public class OpWorldEntity1Encoder : ProxyMessageEncoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY1

    override fun encode(
        message: OpWorldEntity,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt3(if (message.controlKey) 1 else 0)
        buf.p2(message.index)
        return buf.buffer
    }
}

public class OpWorldEntity2Encoder : ProxyMessageEncoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY2

    override fun encode(
        message: OpWorldEntity,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        buf.p2Alt3(message.index)
        return buf.buffer
    }
}

public class OpWorldEntity3Encoder : ProxyMessageEncoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY3

    override fun encode(
        message: OpWorldEntity,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt3(message.index)
        buf.p1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpWorldEntity4Encoder : ProxyMessageEncoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY4

    override fun encode(
        message: OpWorldEntity,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt2(message.index)
        buf.p1(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpWorldEntity5Encoder : ProxyMessageEncoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY5

    override fun encode(
        message: OpWorldEntity,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt3(message.index)
        buf.p1Alt2(if (message.controlKey) 1 else 0)
        return buf.buffer
    }
}

public class OpWorldEntity6Encoder : ProxyMessageEncoder<OpWorldEntity6> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY6

    override fun encode(
        message: OpWorldEntity6,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt1(message.id)
        return buf.buffer
    }
}

public class OpWorldEntityTEncoder : ProxyMessageEncoder<OpWorldEntityT> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITYT

    override fun encode(
        message: OpWorldEntityT,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt1(message.selectedSub)
        buf.p4Alt3(message.selectedCombinedId)
        buf.p2Alt3(message.index)
        buf.p1(if (message.controlKey) 1 else 0)
        buf.p2Alt3(message.selectedObj)
        return buf.buffer
    }
}

