package net.rsprox.protocol.v235.game.incoming.encoder.codec.resumed

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.resumed.ResumePCountDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePNameDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePObjDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePStringDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePauseButton
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.incoming.decoder.prot.GameClientProt

public class ResumePCountDialogEncoder : ProxyMessageEncoder<ResumePCountDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_COUNTDIALOG

    override fun encode(
        message: ResumePCountDialog,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p4(message.count)
        return buf.buffer
    }
}

public class ResumePNameDialogEncoder : ProxyMessageEncoder<ResumePNameDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_NAMEDIALOG

    override fun encode(
        message: ResumePNameDialog,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class ResumePObjDialogEncoder : ProxyMessageEncoder<ResumePObjDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_OBJDIALOG

    override fun encode(
        message: ResumePObjDialog,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2(message.obj)
        return buf.buffer
    }
}

public class ResumePStringDialogEncoder : ProxyMessageEncoder<ResumePStringDialog> {
    override val prot: ClientProt = GameClientProt.RESUME_P_STRINGDIALOG

    override fun encode(
        message: ResumePStringDialog,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.string)
        return buf.buffer
    }
}

public class ResumePauseButtonEncoder : ProxyMessageEncoder<ResumePauseButton> {
    override val prot: ClientProt = GameClientProt.RESUME_PAUSEBUTTON

    override fun encode(
        message: ResumePauseButton,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt3(message.sub)
        buf.p4Alt1(message.combinedId)
        return buf.buffer
    }
}
