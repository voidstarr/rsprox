package net.rsprox.protocol.v233.game.incoming.encoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprox.protocol.MessageEncoderRepository
import net.rsprox.protocol.v233.game.incoming.encoder.codec.buttons.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.clan.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.events.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.friendchat.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.locs.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.messaging.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.misc.client.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.misc.user.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.npcs.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.objs.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.players.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.resumed.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.social.*
import net.rsprox.protocol.v233.game.incoming.encoder.codec.worldentities.*

public object ClientMessageEncoderRepository {
    public fun build(huffmanCodec: HuffmanCodec): MessageEncoderRepository {
        val encoders = listOf(
            // Buttons
            If1ButtonEncoder(),
            IfButtonDEncoder(),
            IfButtonTEncoder(),
            IfSubOpEncoder(),
            IfButtonXEncoder(),
            IfRunScriptEncoder(),

            // Clan
            AffinedClanSettingsAddBannedFromChannelEncoder(),
            AffinedClanSettingsSetMutedFromChannelEncoder(),
            ClanChannelFullRequestEncoder(),
            ClanChannelKickUserEncoder(),
            ClanSettingsFullRequestEncoder(),

            // Events
            EventAppletFocusEncoder(),
            EventCameraPositionEncoder(),
            EventKeyboardEncoder(),
            EventMouseClickV1Encoder(),
            EventMouseClickV2Encoder(),
            EventMouseMoveEncoder(),
            EventMouseScrollEncoder(),
            EventNativeMouseMoveEncoder(),

            // Friend Chat
            FriendChatJoinLeaveEncoder(),
            FriendChatKickEncoder(),
            FriendChatSetRankEncoder(),

            // Locs
            OpLoc1Encoder(),
            OpLoc2Encoder(),
            OpLoc3Encoder(),
            OpLoc4Encoder(),
            OpLoc5Encoder(),
            OpLoc6Encoder(),
            OpLocTEncoder(),

            // Messaging
            MessagePrivateEncoder(huffmanCodec),
            MessagePublicEncoder(huffmanCodec),

            // Misc Client
            ConnectionTelemetryEncoder(),
            DetectModifiedClientEncoder(),
            IdleEncoder(),
            MapBuildCompleteEncoder(),
            MembershipPromotionEligibilityEncoder(),
            NoTimeoutEncoder(),
            RSevenStatusEncoder(),
            ReflectionCheckReplyEncoder(),
            SendPingReplyEncoder(),
            SoundJingleEndEncoder(),
            WindowStatusEncoder(),

            // Misc User
            BugReportEncoder(),
            ClickWorldMapEncoder(),
            ClientCheatEncoder(),
            CloseModalEncoder(),
            HiscoreRequestEncoder(),
            IfCrmViewClickEncoder(),
            MoveGameClickEncoder(),
            MoveMinimapClickEncoder(),
            OculusLeaveEncoder(),
            SendSnapshotEncoder(),
            SetChatFilterSettingsEncoder(),
            SetHeadingEncoder(),
            TeleportEncoder(),

            // Npcs
            OpNpc1Encoder(),
            OpNpc2Encoder(),
            OpNpc3Encoder(),
            OpNpc4Encoder(),
            OpNpc5Encoder(),
            OpNpc6Encoder(),
            OpNpcTEncoder(),

            // Objs
            OpObj1Encoder(),
            OpObj2Encoder(),
            OpObj3Encoder(),
            OpObj4Encoder(),
            OpObj5Encoder(),
            OpObj6Encoder(),
            OpObjTEncoder(),

            // Players
            OpPlayer1Encoder(),
            OpPlayer2Encoder(),
            OpPlayer3Encoder(),
            OpPlayer4Encoder(),
            OpPlayer5Encoder(),
            OpPlayer6Encoder(),
            OpPlayer7Encoder(),
            OpPlayer8Encoder(),
            OpPlayerTEncoder(),

            // Resumed
            ResumePCountDialogEncoder(),
            ResumePNameDialogEncoder(),
            ResumePObjDialogEncoder(),
            ResumePStringDialogEncoder(),
            ResumePauseButtonEncoder(),

            // Social
            FriendListAddEncoder(),
            FriendListDelEncoder(),
            IgnoreListAddEncoder(),
            IgnoreListDelEncoder(),

            // World Entities
            OpWorldEntity1Encoder(),
            OpWorldEntity2Encoder(),
            OpWorldEntity3Encoder(),
            OpWorldEntity4Encoder(),
            OpWorldEntity5Encoder(),
            OpWorldEntity6Encoder(),
            OpWorldEntityTEncoder(),
        )
        return MessageEncoderRepository(encoders.associateBy { it.prot })
    }
}
