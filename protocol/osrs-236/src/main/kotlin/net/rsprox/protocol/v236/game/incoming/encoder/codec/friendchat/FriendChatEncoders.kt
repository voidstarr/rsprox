package net.rsprox.protocol.v236.game.incoming.encoder.codec.friendchat

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatJoinLeave
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatKick
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatSetRank
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt

public class FriendChatJoinLeaveEncoder : ProxyMessageEncoder<FriendChatJoinLeave> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_JOIN_LEAVE

    override fun encode(
        message: FriendChatJoinLeave,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        if (message.name != null) {
            buf.pjstr(message.name!!)
        }
        return buf.buffer
    }
}

public class FriendChatKickEncoder : ProxyMessageEncoder<FriendChatKick> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_KICK

    override fun encode(
        message: FriendChatKick,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class FriendChatSetRankEncoder : ProxyMessageEncoder<FriendChatSetRank> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_SETRANK

    override fun encode(
        message: FriendChatSetRank,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // rank: g1Alt1
        buf.p1Alt1(message.rank)
        // name: gjstr
        buf.pjstr(message.name)
        return buf.buffer
    }
}
