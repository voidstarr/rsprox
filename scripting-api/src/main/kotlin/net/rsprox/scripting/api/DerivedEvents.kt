package net.rsprox.scripting.api

import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.protocol.game.incoming.model.resumed.ResumePCountDialog
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAnim
import net.rsprox.protocol.game.outgoing.model.misc.client.UrlOpen
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.MapAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV3
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV4
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.game.outgoing.model.varp.VarpReset
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.game.outgoing.model.varp.VarpSync
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapAnim
import net.rsprox.proxy.client.ClientPacket
import net.rsprox.proxy.plugin.ScriptContext
import net.rsprox.proxy.plugin.Subscription
import net.rsprox.proxy.server.ServerPacket
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.util.CombinedId
import net.rsprot.protocol.util.gCombinedId
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.g2Alt1
import net.rsprot.buffer.extensions.g2Alt2
import net.rsprot.buffer.extensions.g2Alt3
import net.rsprot.buffer.extensions.toJagByteBuf

/**
 * Sugar over packet listeners.
 *
 * These are implemented using [ScriptContext] packet hooks + decode, so scripts can stay ergonomic
 * while still retaining access to raw buffers through the original packet object.
 */

public fun ScriptContext.onPublicChat(
    handler: (packet: ClientPacket<*>, message: MessagePublic) -> Unit,
): Subscription {
    return onClientPacket("MESSAGE_PUBLIC") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onResumeCountDialog(
    handler: (packet: ClientPacket<*>, message: ResumePCountDialog) -> Unit,
): Subscription {
    return onClientPacket("RESUME_P_COUNTDIALOG") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Generic sugar: decode an incoming client message of the given packet name.
 */
public inline fun <reified M : IncomingGameMessage> ScriptContext.onClientMessage(
    protName: String,
    crossinline handler: (packet: ClientPacket<*>, message: M) -> Unit,
): Subscription {
    return onClientPacket(protName) { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onPrivateMessage(
    handler: (packet: ClientPacket<*>, message: MessagePrivate) -> Unit,
): Subscription {
    return onClientPacket("MESSAGE_PRIVATE") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Best-effort bank pin helpers.
 *
 * Derives bank-pin UI open/close from IF_OPENSUB/IF_CLOSESUB and filters the bank pin interface id.
 * Derives the entered value from RESUME_P_COUNTDIALOG while the pin UI is open.
 */

public fun ScriptContext.onBankPinOpen(
    handler: (packet: ServerPacket<*>, interfaceId: Int, component: CombinedId) -> Unit,
): Subscription {
    return onServerPacket("IF_OPENSUB") { packet ->
        val (interfaceId, component) = parseIfOpensub(packet) ?: return@onServerPacket
        if (interfaceId == BANK_PIN_INTERFACE_ID) {
            handler(packet, interfaceId, component)
        }
    }
}

public fun ScriptContext.onBankPinClose(
    handler: (packet: ServerPacket<*>) -> Unit,
): Subscription {
    var bankPinComponent: CombinedId? = null
    val open = onServerPacket("IF_OPENSUB") { packet ->
        val (interfaceId, component) = parseIfOpensub(packet) ?: return@onServerPacket
        if (interfaceId == BANK_PIN_INTERFACE_ID) {
            bankPinComponent = component
        }
    }
    val close = onServerPacket("IF_CLOSESUB") { packet ->
        val current = bankPinComponent ?: return@onServerPacket
        val combinedId = packet.payload.toJagByteBuf().gCombinedId()
        if (combinedId == current) {
            bankPinComponent = null
            handler(packet)
        }
    }
    return object : Subscription {
        override fun cancel() {
            open.cancel()
            close.cancel()
        }
    }
}

public fun ScriptContext.onBankPinEntered(
    handler: (packet: ClientPacket<*>, pin: Int) -> Unit,
): Subscription {
    var pinUiOpen = false
    var bankPinComponent: CombinedId? = null
    val open = onServerPacket("IF_OPENSUB") { packet ->
        val (interfaceId, component) = parseIfOpensub(packet) ?: return@onServerPacket
        if (interfaceId == BANK_PIN_INTERFACE_ID) {
            bankPinComponent = component
            pinUiOpen = true
        }
    }
    val close = onServerPacket("IF_CLOSESUB") { packet ->
        val current = bankPinComponent ?: return@onServerPacket
        val combinedId = packet.payload.toJagByteBuf().gCombinedId()
        if (combinedId == current) {
            bankPinComponent = null
            pinUiOpen = false
        }
    }
    val entered = onClientPacket("RESUME_P_COUNTDIALOG") { packet ->
        if (!pinUiOpen) return@onClientPacket
        val message = packet.decode<ResumePCountDialog>()
        handler(packet, message.count)
    }
    return object : Subscription {
        override fun cancel() {
            open.cancel()
            close.cancel()
            entered.cancel()
        }
    }
}

/**
 * Fires when the server sends a private message to the local player.
 */
public fun ScriptContext.onPrivateMessageReceived(
    handler: (packet: ServerPacket<*>, message: net.rsprox.protocol.game.outgoing.model.social.MessagePrivate) -> Unit,
): Subscription {
    return onServerPacket("MESSAGE_PRIVATE") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Fires when the server echoes an outgoing private message ("To name: ...").
 */
public fun ScriptContext.onPrivateMessageSentEcho(
    handler: (packet: ServerPacket<*>, message: MessagePrivateEcho) -> Unit,
): Subscription {
    return onServerPacket("MESSAGE_PRIVATE_ECHO") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Fires when the server instructs the client to open a URL.
 */
public fun ScriptContext.onUrlOpen(
    handler: (packet: ServerPacket<*>, message: UrlOpen) -> Unit,
): Subscription {
    return onServerPacket("URL_OPEN") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Varp helpers.
 */

public fun ScriptContext.onVarpChanged(
    handler: (packet: ServerPacket<*>, id: Int, value: Int) -> Unit,
): Subscription {
    val s1 = onServerPacket("VARP_SMALL") { packet ->
        val m = packet.decode<VarpSmall>()
        handler(packet, m.id, m.value)
    }
    val s2 = onServerPacket("VARP_LARGE") { packet ->
        val m = packet.decode<VarpLarge>()
        handler(packet, m.id, m.value)
    }
    return object : Subscription {
        override fun cancel() {
            s1.cancel()
            s2.cancel()
        }
    }
}

public fun ScriptContext.onVarpReset(
    handler: (packet: ServerPacket<*>, message: VarpReset) -> Unit,
): Subscription {
    return onServerPacket("VARP_RESET") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onVarpSync(
    handler: (packet: ServerPacket<*>, message: VarpSync) -> Unit,
): Subscription {
    return onServerPacket("VARP_SYNC") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Varbit helper without requiring varbit config: specify which varp and bit range to extract.
 *
 * Range is inclusive: [lsb..msb].
 */
public fun ScriptContext.onVarbitChanged(
    varpId: Int,
    lsb: Int,
    msb: Int,
    handler: (packet: ServerPacket<*>, value: Int) -> Unit,
): Subscription {
    require(lsb >= 0) { "lsb must be >= 0" }
    require(msb >= lsb) { "msb must be >= lsb" }
    require(msb <= 31) { "msb must be <= 31" }
    val width = msb - lsb + 1
    val mask = ((1L shl width) - 1L)
    var last: Int? = null
    return onVarpChanged { packet, id, value ->
        if (id != varpId) return@onVarpChanged
        val extracted = ((value.toLong() ushr lsb) and mask).toInt()
        if (last != extracted) {
            last = extracted
            handler(packet, extracted)
        }
    }
}

/**
 * Generic sugar: decode an incoming server message of the given packet name.
 */
public inline fun <reified M : IncomingGameMessage> ScriptContext.onServerMessage(
    protName: String,
    crossinline handler: (packet: ServerPacket<*>, message: M) -> Unit,
): Subscription {
    return onServerPacket(protName) { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Fires for any server-side chatbox message.
 */
public fun ScriptContext.onGameMessage(
    handler: (packet: ServerPacket<*>, message: MessageGame) -> Unit,
): Subscription {
    return onServerPacket("MESSAGE_GAME") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Animation / graphics helpers.
 */

public fun ScriptContext.onResetAnims(
    handler: (packet: ServerPacket<*>, message: ResetAnims) -> Unit,
): Subscription {
    return onServerPacket("RESET_ANIMS") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onIfSetAnim(
    handler: (packet: ServerPacket<*>, message: IfSetAnim) -> Unit,
): Subscription {
    return onServerPacket("IF_SETANIM") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onLocAnim(
    handler: (packet: ServerPacket<*>, message: LocAnim) -> Unit,
): Subscription {
    return onServerPacket("LOC_ANIM") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onMapAnim(
    handler: (packet: ServerPacket<*>, message: MapAnim) -> Unit,
): Subscription {
    return onServerPacket("MAP_ANIM") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onLocAnimSpecific(
    handler: (packet: ServerPacket<*>, message: LocAnimSpecific) -> Unit,
): Subscription {
    return onServerPacket("LOC_ANIM_SPECIFIC") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onMapAnimSpecific(
    handler: (packet: ServerPacket<*>, message: MapAnimSpecific) -> Unit,
): Subscription {
    return onServerPacket("MAP_ANIM_SPECIFIC") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onPlayerAnimSpecific(
    handler: (packet: ServerPacket<*>, message: PlayerAnimSpecific) -> Unit,
): Subscription {
    return onServerPacket("PLAYER_ANIM_SPECIFIC") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onPlayerSpotanimSpecific(
    handler: (packet: ServerPacket<*>, message: PlayerSpotAnimSpecific) -> Unit,
): Subscription {
    return onServerPacket("PLAYER_SPOTANIM_SPECIFIC") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onNpcAnimSpecific(
    handler: (packet: ServerPacket<*>, message: NpcAnimSpecific) -> Unit,
): Subscription {
    return onServerPacket("NPC_ANIM_SPECIFIC") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onNpcSpotanimSpecific(
    handler: (packet: ServerPacket<*>, message: NpcSpotAnimSpecific) -> Unit,
): Subscription {
    return onServerPacket("NPC_SPOTANIM_SPECIFIC") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onProjAnimSpecificV2(
    handler: (packet: ServerPacket<*>, message: ProjAnimSpecificV2) -> Unit,
): Subscription {
    return onServerPacket("PROJANIM_SPECIFIC_V2") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onProjAnimSpecificV3(
    handler: (packet: ServerPacket<*>, message: ProjAnimSpecificV3) -> Unit,
): Subscription {
    return onServerPacket("PROJANIM_SPECIFIC_V3") { packet ->
        handler(packet, packet.decode())
    }
}

public fun ScriptContext.onProjAnimSpecificV4(
    handler: (packet: ServerPacket<*>, message: ProjAnimSpecificV4) -> Unit,
): Subscription {
    return onServerPacket("PROJANIM_SPECIFIC_V4") { packet ->
        handler(packet, packet.decode())
    }
}

/**
 * Fires for a subset of MessageGame types.
 */
public fun ScriptContext.onGameMessageType(
    vararg types: Int,
    handler: (packet: ServerPacket<*>, message: MessageGame) -> Unit,
): Subscription {
    val set = types.toSet()
    return onServerPacket("MESSAGE_GAME") { packet ->
        val message = packet.decode<MessageGame>()
        if (message.type in set) {
            handler(packet, message)
        }
    }
}

/**
 * Fires when the server emits a trade request-style chat message.
 *
 * Uses [MessageGame] message type IDs (see MessageGame docs); currently matches:
 * - 101: trade request
 * - 103: trade challenge request
 */
public fun ScriptContext.onTradeRequest(
    handler: (packet: ServerPacket<*>, message: MessageGame) -> Unit,
): Subscription {
    return onServerPacket("MESSAGE_GAME") { packet ->
        val message = packet.decode<MessageGame>()
        if (message.type == 101 || message.type == 103) {
            handler(packet, message)
        }
    }
}

/**
 * Fires when a trade-status chat message is sent (e.g. "Trade accepted").
 * MessageGame type 102.
 */
public fun ScriptContext.onTradeMessage(
    handler: (packet: ServerPacket<*>, message: MessageGame) -> Unit,
): Subscription {
    return onGameMessageType(102, handler = handler)
}

/**
 * Login/logout notification messages (MessageGame type 5).
 */
public fun ScriptContext.onLoginLogoutNotification(
    handler: (packet: ServerPacket<*>, message: MessageGame) -> Unit,
): Subscription {
    return onGameMessageType(5, handler = handler)
}

public sealed interface InventoryChanged {
    public val interfaceId: Int
    public val componentId: Int
    public val inventoryId: Int

    public data class Full(
        public val message: UpdateInvFull,
    ) : InventoryChanged {
        override val interfaceId: Int get() = message.interfaceId
        override val componentId: Int get() = message.componentId
        override val inventoryId: Int get() = message.inventoryId
    }

    public data class Partial(
        public val message: UpdateInvPartial,
    ) : InventoryChanged {
        override val interfaceId: Int get() = message.interfaceId
        override val componentId: Int get() = message.componentId
        override val inventoryId: Int get() = message.inventoryId
    }
}

public fun ScriptContext.onInventoryChanged(
    handler: (packet: ServerPacket<*>, change: InventoryChanged) -> Unit,
): Subscription {
    val s1 = onServerPacket("UPDATE_INV_FULL") { packet ->
        handler(packet, InventoryChanged.Full(packet.decode()))
    }
    val s2 = onServerPacket("UPDATE_INV_PARTIAL") { packet ->
        handler(packet, InventoryChanged.Partial(packet.decode()))
    }
    return object : Subscription {
        override fun cancel() {
            s1.cancel()
            s2.cancel()
        }
    }
}

/**
 * Fires for each slot update in UPDATE_INV_PARTIAL.
 */
public fun ScriptContext.onInventorySlotChanged(
    handler: (packet: ServerPacket<*>, interfaceId: Int, componentId: Int, inventoryId: Int, slot: Int, id: Int, count: Int) -> Unit,
): Subscription {
    return onServerPacket("UPDATE_INV_PARTIAL") { packet ->
        val message = packet.decode<UpdateInvPartial>()
        for (obj in message.objs) {
            handler(
                packet,
                message.interfaceId,
                message.componentId,
                message.inventoryId,
                obj.slot,
                obj.id,
                obj.count,
            )
        }
    }
}

private const val BANK_PIN_INTERFACE_ID: Int = 213

private fun parseIfOpensub(packet: ServerPacket<*>): Pair<Int, CombinedId>? {
    val revision = packet.revisionDecoder?.revision ?: return null
    val buf = packet.payload.toJagByteBuf()
    // Keep this aligned with the proxy's ServerGameHandler IF_OPENSUB parsing.
    val interfaceId: Int
    val targetComponent: CombinedId
    when (revision) {
        223 -> {
            interfaceId = buf.g2Alt2()
            targetComponent = buf.gCombinedIdAlt2()
            buf.skipRead(1)
        }

        224 -> {
            interfaceId = buf.g2Alt1()
            buf.skipRead(1)
            targetComponent = buf.gCombinedIdAlt3()
        }

        225 -> {
            interfaceId = buf.g2Alt2()
            targetComponent = buf.gCombinedIdAlt2()
            buf.skipRead(1)
        }

        226 -> {
            interfaceId = buf.g2Alt3()
            targetComponent = buf.gCombinedIdAlt2()
            buf.skipRead(1)
        }

        227 -> {
            interfaceId = buf.g2Alt2()
            targetComponent = buf.gCombinedIdAlt1()
            buf.skipRead(1)
        }

        228 -> {
            interfaceId = buf.g2Alt3()
            targetComponent = buf.gCombinedIdAlt3()
            buf.skipRead(1)
        }

        229 -> {
            interfaceId = buf.g2()
            targetComponent = buf.gCombinedIdAlt1()
            buf.skipRead(1)
        }

        230 -> {
            buf.skipRead(1)
            interfaceId = buf.g2Alt2()
            targetComponent = buf.gCombinedIdAlt3()
        }

        231 -> {
            interfaceId = buf.g2Alt2()
            buf.skipRead(1)
            targetComponent = buf.gCombinedIdAlt3()
        }

        232 -> {
            buf.skipRead(1)
            targetComponent = buf.gCombinedIdAlt3()
            interfaceId = buf.g2Alt1()
        }

        233 -> {
            interfaceId = buf.g2Alt3()
            targetComponent = buf.gCombinedIdAlt1()
            buf.skipRead(1)
        }

        234 -> {
            interfaceId = buf.g2()
            buf.skipRead(1)
            targetComponent = buf.gCombinedId()
        }

        235 -> {
            interfaceId = buf.g2()
            buf.skipRead(1)
            targetComponent = buf.gCombinedIdAlt2()
        }

        else -> return null
    }
    return interfaceId to targetComponent
}
