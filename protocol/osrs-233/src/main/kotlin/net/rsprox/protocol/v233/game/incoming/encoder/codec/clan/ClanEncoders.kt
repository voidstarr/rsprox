package net.rsprox.protocol.v233.game.incoming.encoder.codec.clan

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsAddBannedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelFullRequest
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelKickUser
import net.rsprox.protocol.game.incoming.model.clan.ClanSettingsFullRequest
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

public class AffinedClanSettingsAddBannedFromChannelEncoder : ProxyMessageEncoder<AffinedClanSettingsAddBannedFromChannel> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL

    override fun encode(
        message: AffinedClanSettingsAddBannedFromChannel,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.clanId)
        buf.p2(message.memberIndex)
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class AffinedClanSettingsSetMutedFromChannelEncoder : ProxyMessageEncoder<AffinedClanSettingsSetMutedFromChannel> {
    override val prot: ClientProt = GameClientProt.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL

    override fun encode(
        message: AffinedClanSettingsSetMutedFromChannel,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.clanId)
        buf.p2(message.memberIndex)
        buf.p1(if (message.muted) 1 else 0)
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class ClanChannelFullRequestEncoder : ProxyMessageEncoder<ClanChannelFullRequest> {
    override val prot: ClientProt = GameClientProt.CLANCHANNEL_FULL_REQUEST

    override fun encode(
        message: ClanChannelFullRequest,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt3(message.clanId)
        return buf.buffer
    }
}

public class ClanChannelKickUserEncoder : ProxyMessageEncoder<ClanChannelKickUser> {
    override val prot: ClientProt = GameClientProt.CLANCHANNEL_KICKUSER

    override fun encode(
        message: ClanChannelKickUser,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.clanId)
        buf.p2(message.memberIndex)
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class ClanSettingsFullRequestEncoder : ProxyMessageEncoder<ClanSettingsFullRequest> {
    override val prot: ClientProt = GameClientProt.CLANSETTINGS_FULL_REQUEST

    override fun encode(
        message: ClanSettingsFullRequest,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt3(message.clanId)
        return buf.buffer
    }
}

