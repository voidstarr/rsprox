package net.rsprox.protocol.v235.game.incoming.encoder.prot

import net.rsprot.compression.HuffmanCodec
import net.rsprox.protocol.MessageEncoderRepository
import net.rsprox.protocol.MessageEncoderRepositoryBuilder
import net.rsprox.protocol.v235.game.incoming.encoder.codec.buttons.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.clan.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.events.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.friendchat.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.locs.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.messaging.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.misc.client.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.misc.user.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.npcs.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.objs.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.players.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.resumed.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.social.*
import net.rsprox.protocol.v235.game.incoming.encoder.codec.worldentities.*

public object ClientMessageEncoderRepository {
    public fun build(huffmanCodec: HuffmanCodec): MessageEncoderRepository {
        val builder =
            MessageEncoderRepositoryBuilder().apply {
                // Buttons
                bind(If1ButtonEncoder())
                bind(IfButtonDEncoder())
                bind(IfButtonTEncoder())
                bind(IfSubOpEncoder())
                bind(IfButtonXEncoder())
                bind(IfRunScriptEncoder())

                // Clan
                bind(AffinedClanSettingsAddBannedFromChannelEncoder())
                bind(AffinedClanSettingsSetMutedFromChannelEncoder())
                bind(ClanChannelFullRequestEncoder())
                bind(ClanChannelKickUserEncoder())
                bind(ClanSettingsFullRequestEncoder())

                // Events
                bind(EventAppletFocusEncoder())
                bind(EventCameraPositionEncoder())
                bind(EventKeyboardEncoder())
                bind(EventMouseClickV1Encoder())
                bind(EventMouseClickV2Encoder())
                bind(EventMouseMoveEncoder())
                bind(EventMouseScrollEncoder())
                bind(EventNativeMouseMoveEncoder())

                // Friend Chat
                bind(FriendChatJoinLeaveEncoder())
                bind(FriendChatKickEncoder())
                bind(FriendChatSetRankEncoder())

                // Locs
                bind(OpLoc1Encoder())
                bind(OpLoc2Encoder())
                bind(OpLoc3Encoder())
                bind(OpLoc4Encoder())
                bind(OpLoc5Encoder())
                bind(OpLoc6Encoder())
                bind(OpLocTEncoder())

                // Messaging
                bind(MessagePrivateEncoder(huffmanCodec))
                bind(MessagePublicEncoder(huffmanCodec))

                // Misc Client
                bind(ConnectionTelemetryEncoder())
                bind(DetectModifiedClientEncoder())
                bind(IdleEncoder())
                bind(MapBuildCompleteEncoder())
                bind(MembershipPromotionEligibilityEncoder())
                bind(NoTimeoutEncoder())
                bind(RSevenStatusEncoder())
                bind(ReflectionCheckReplyEncoder())
                bind(SendPingReplyEncoder())
                bind(SoundJingleEndEncoder())
                bind(WindowStatusEncoder())

                // Misc User
                bind(BugReportEncoder())
                bind(ClickWorldMapEncoder())
                bind(ClientCheatEncoder())
                bind(CloseModalEncoder())
                bind(HiscoreRequestEncoder())
                bind(IfCrmViewClickEncoder())
                bind(MoveGameClickEncoder())
                bind(MoveMinimapClickEncoder())
                bind(OculusLeaveEncoder())
                bind(SendSnapshotEncoder())
                bind(SetChatFilterSettingsEncoder())
                bind(SetHeadingEncoder())
                bind(TeleportEncoder())

                // Npcs
                bind(OpNpc1Encoder())
                bind(OpNpc2Encoder())
                bind(OpNpc3Encoder())
                bind(OpNpc4Encoder())
                bind(OpNpc5Encoder())
                bind(OpNpc6Encoder())
                bind(OpNpcTEncoder())

                // Objs
                bind(OpObj1Encoder())
                bind(OpObj2Encoder())
                bind(OpObj3Encoder())
                bind(OpObj4Encoder())
                bind(OpObj5Encoder())
                bind(OpObj6Encoder())
                bind(OpObjTEncoder())

                // Players
                bind(OpPlayer1Encoder())
                bind(OpPlayer2Encoder())
                bind(OpPlayer3Encoder())
                bind(OpPlayer4Encoder())
                bind(OpPlayer5Encoder())
                bind(OpPlayer6Encoder())
                bind(OpPlayer7Encoder())
                bind(OpPlayer8Encoder())
                bind(OpPlayerTEncoder())

                // Resumed
                bind(ResumePCountDialogEncoder())
                bind(ResumePNameDialogEncoder())
                bind(ResumePObjDialogEncoder())
                bind(ResumePStringDialogEncoder())
                bind(ResumePauseButtonEncoder())

                // Social
                bind(FriendListAddEncoder())
                bind(FriendListDelEncoder())
                bind(IgnoreListAddEncoder())
                bind(IgnoreListDelEncoder())

                // World Entities
                bind(OpWorldEntity1Encoder())
                bind(OpWorldEntity2Encoder())
                bind(OpWorldEntity3Encoder())
                bind(OpWorldEntity4Encoder())
                bind(OpWorldEntity5Encoder())
                bind(OpWorldEntity6Encoder())
                bind(OpWorldEntityTEncoder())
            }
        return builder.build()
    }
}
