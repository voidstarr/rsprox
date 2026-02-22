package net.rsprox.protocol.v236.game.incoming.encoder.codec.social

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.social.FriendListAdd
import net.rsprox.protocol.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.game.incoming.model.social.IgnoreListAdd
import net.rsprox.protocol.game.incoming.model.social.IgnoreListDel
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class FriendListAddEncoder : ProxyMessageEncoder<FriendListAdd> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_ADD

    override fun encode(
        message: FriendListAdd,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class FriendListDelEncoder : ProxyMessageEncoder<FriendListDel> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_DEL

    override fun encode(
        message: FriendListDel,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class IgnoreListAddEncoder : ProxyMessageEncoder<IgnoreListAdd> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_ADD

    override fun encode(
        message: IgnoreListAdd,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class IgnoreListDelEncoder : ProxyMessageEncoder<IgnoreListDel> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_DEL

    override fun encode(
        message: IgnoreListDel,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        return buf.buffer
    }
}
