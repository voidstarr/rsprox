package net.rsprox.protocol.v235.game.incoming.encoder.codec.buttons

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.buttons.*
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.incoming.decoder.prot.GameClientProt

public class If1ButtonEncoder : ProxyMessageEncoder<If1Button> {
    override val prot: ClientProt = GameClientProt.IF_BUTTON

    override fun encode(
        message: If1Button,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p4(message.combinedId)
        return buf.buffer
    }
}

public class IfButtonDEncoder : ProxyMessageEncoder<IfButtonD> {
    override val prot: ClientProt = GameClientProt.IF_BUTTOND

    override fun encode(
        message: IfButtonD,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // selectedObj: g2Alt3
        buf.p2Alt3(message.selectedObj)
        // selectedCombinedId: gCombinedIdAlt1 -> p4Alt1
        buf.p4Alt1(message.selectedCombinedId)
        // targetSub: g2Alt3
        buf.p2Alt3(message.targetSub)
        // selectedSub: g2Alt2
        buf.p2Alt2(message.selectedSub)
        // targetObj: g2Alt1
        buf.p2Alt1(message.targetObj)
        // targetCombinedId: gCombinedIdAlt3 -> p4Alt3
        buf.p4Alt3(message.targetCombinedId)
        return buf.buffer
    }
}

public class IfButtonTEncoder : ProxyMessageEncoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun encode(
        message: IfButtonT,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // selectedSub: g2Alt2
        buf.p2Alt2(message.selectedSub)
        // targetCombinedId: gCombinedIdAlt1 -> p4Alt1
        buf.p4Alt1(message.targetCombinedId)
        // targetObj: g2Alt3
        buf.p2Alt3(message.targetObj)
        // targetSub: g2Alt3
        buf.p2Alt3(message.targetSub)
        // selectedCombinedId: gCombinedId -> p4
        buf.p4(message.selectedCombinedId)
        // selectedObj: g2
        buf.p2(message.selectedObj)
        return buf.buffer
    }
}

public class IfButtonXEncoder : ProxyMessageEncoder<If3Button> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONX

    override fun encode(
        message: If3Button,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // combinedId: gCombinedId -> p4
        buf.p4(message.combinedId)
        // sub: g2
        buf.p2(message.sub)
        // obj: g2
        buf.p2(message.obj)
        // op: g1
        buf.p1(message.op)
        return buf.buffer
    }
}

public class IfSubOpEncoder : ProxyMessageEncoder<IfSubOp> {
    override val prot: ClientProt = GameClientProt.IF_SUBOP

    override fun encode(
        message: IfSubOp,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // combinedId: gCombinedId -> p4
        buf.p4(message.combinedId)
        // sub: g2
        buf.p2(message.sub)
        // obj: g2
        buf.p2(message.obj)
        // op: g1
        buf.p1(message.op)
        // subop: g1
        buf.p1(message.subop)
        return buf.buffer
    }
}

public class IfRunScriptEncoder : ProxyMessageEncoder<IfRunScript> {
    override val prot: ClientProt = GameClientProt.IF_RUNSCRIPT

    override fun encode(
        message: IfRunScript,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // script: g4Alt1
        buf.p4Alt1(message.script)
        // combinedId: gCombinedId -> p4
        buf.p4(message.combinedId)
        // obj: g2Alt1
        buf.p2Alt1(message.obj)
        // sub: g2Alt2
        buf.p2Alt2(message.sub)
        // bytes: toByteArray
        // We write the remaining bytes, assuming message.bytes captured all payload
        // The decoder does `buffer.buffer.toByteArray()`.
        buf.pdata(Unpooled.wrappedBuffer(message.bytes))
        return buf.buffer
    }
}
