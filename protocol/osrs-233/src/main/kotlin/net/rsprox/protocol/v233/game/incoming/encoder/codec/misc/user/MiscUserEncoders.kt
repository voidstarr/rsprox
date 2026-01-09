package net.rsprox.protocol.v233.game.incoming.encoder.codec.misc.user

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.misc.user.BugReport
import net.rsprox.protocol.game.incoming.model.misc.user.ClickWorldMap
import net.rsprox.protocol.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.game.incoming.model.misc.user.CloseModal
import net.rsprox.protocol.game.incoming.model.misc.user.HiscoreRequest
import net.rsprox.protocol.game.incoming.model.misc.user.IfCrmViewClick
import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.game.incoming.model.misc.user.MoveMinimapClick
import net.rsprox.protocol.game.incoming.model.misc.user.OculusLeave
import net.rsprox.protocol.game.incoming.model.misc.user.SendSnapshot
import net.rsprox.protocol.game.incoming.model.misc.user.SetChatFilterSettings
import net.rsprox.protocol.game.incoming.model.misc.user.SetHeading
import net.rsprox.protocol.game.incoming.model.misc.user.Teleport
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

public class BugReportEncoder : ProxyMessageEncoder<BugReport> {
    override val prot: ClientProt = GameClientProt.BUG_REPORT

    override fun encode(
        message: BugReport,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.description)
        buf.p1Alt3(message.type)
        buf.pjstr(message.instructions)
        return buf.buffer
    }
}

public class ClickWorldMapEncoder : ProxyMessageEncoder<ClickWorldMap> {
    override val prot: ClientProt = GameClientProt.CLICKWORLDMAP

    override fun encode(
        message: ClickWorldMap,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p4((message.level shl 28) or (message.x shl 14) or message.z)
        return buf.buffer
    }
}

public class ClientCheatEncoder : ProxyMessageEncoder<ClientCheat> {
    override val prot: ClientProt = GameClientProt.CLIENT_CHEAT

    override fun encode(
        message: ClientCheat,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.command)
        return buf.buffer
    }
}

public class CloseModalEncoder : ProxyMessageEncoder<CloseModal> {
    override val prot: ClientProt = GameClientProt.CLOSE_MODAL

    override fun encode(
        message: CloseModal,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        return buf.buffer
    }
}

public class HiscoreRequestEncoder : ProxyMessageEncoder<HiscoreRequest> {
    override val prot: ClientProt = GameClientProt.HISCORE_REQUEST

    override fun encode(
        message: HiscoreRequest,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.requestId)
        buf.p1(message.type)
        buf.pjstr(message.name)
        return buf.buffer
    }
}

public class IfCrmViewClickEncoder : ProxyMessageEncoder<IfCrmViewClick> {
    override val prot: ClientProt = GameClientProt.IF_CRMVIEW_CLICK

    override fun encode(
        message: IfCrmViewClick,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p4Alt3(message.behaviour1)
        buf.p4Alt1(message.behaviour3)
        buf.p4Alt2(message.behaviour2)
        buf.p2Alt3(message.sub)
        buf.p4(message.combinedId)
        buf.p4Alt1(message.crmServerTarget)
        return buf.buffer
    }
}

public class MoveGameClickEncoder : ProxyMessageEncoder<MoveGameClick> {
    override val prot: ClientProt = GameClientProt.MOVE_GAMECLICK

    override fun encode(
        message: MoveGameClick,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.keyCombination)
        buf.p2Alt3(message.x)
        buf.p2Alt3(message.z)
        return buf.buffer
    }
}

public class MoveMinimapClickEncoder : ProxyMessageEncoder<MoveMinimapClick> {
    override val prot: ClientProt = GameClientProt.MOVE_MINIMAPCLICK

    override fun encode(
        message: MoveMinimapClick,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.keyCombination)
        buf.p2Alt3(message.x)
        buf.p2Alt3(message.z)
        buf.p1(message.minimapWidth)
        buf.p1(message.minimapHeight)
        buf.p2(message.cameraAngleY)
        buf.p1(57) // checkpoint1
        buf.p1(0) // checkpoint2
        buf.p1(0) // checkpoint3
        buf.p1(89) // checkpoint4
        buf.p2(message.fineX)
        buf.p2(message.fineZ)
        buf.p1(63) // checkpoint5
        return buf.buffer
    }
}

public class OculusLeaveEncoder : ProxyMessageEncoder<OculusLeave> {
    override val prot: ClientProt = GameClientProt.OCULUS_LEAVE

    override fun encode(
        message: OculusLeave,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        return buf.buffer
    }
}

public class SendSnapshotEncoder : ProxyMessageEncoder<SendSnapshot> {
    override val prot: ClientProt = GameClientProt.SEND_SNAPSHOT

    override fun encode(
        message: SendSnapshot,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.pjstr(message.name)
        buf.p1(message.ruleId)
        buf.p1(if (message.mute) 1 else 0)
        return buf.buffer
    }
}

public class SetChatFilterSettingsEncoder : ProxyMessageEncoder<SetChatFilterSettings> {
    override val prot: ClientProt = GameClientProt.SET_CHATFILTERSETTINGS

    override fun encode(
        message: SetChatFilterSettings,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.publicChatFilter)
        buf.p1(message.privateChatFilter)
        buf.p1(message.tradeChatFilter)
        return buf.buffer
    }
}

public class SetHeadingEncoder : ProxyMessageEncoder<SetHeading> {
    override val prot: ClientProt = GameClientProt.SET_HEADING

    override fun encode(
        message: SetHeading,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1Alt1(message.heading)
        return buf.buffer
    }
}

public class TeleportEncoder : ProxyMessageEncoder<Teleport> {
    override val prot: ClientProt = GameClientProt.TELEPORT

    override fun encode(
        message: Teleport,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt3(message.x)
        buf.p1Alt2(message.level)
        buf.p4(message.oculusSyncValue)
        buf.p2Alt2(message.z)
        return buf.buffer
    }
}

