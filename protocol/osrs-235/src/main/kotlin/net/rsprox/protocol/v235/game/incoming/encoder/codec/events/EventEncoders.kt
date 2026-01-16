package net.rsprox.protocol.v235.game.incoming.encoder.codec.events

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.game.incoming.model.events.EventCameraPosition
import net.rsprox.protocol.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.game.incoming.model.events.EventMouseClickV1
import net.rsprox.protocol.game.incoming.model.events.EventMouseClickV2
import net.rsprox.protocol.game.incoming.model.events.EventMouseMove
import net.rsprox.protocol.game.incoming.model.events.EventMouseScroll
import net.rsprox.protocol.game.incoming.model.events.EventNativeMouseMove
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.incoming.decoder.prot.GameClientProt

public class EventAppletFocusEncoder : ProxyMessageEncoder<EventAppletFocus> {
    override val prot: ClientProt = GameClientProt.EVENT_APPLET_FOCUS

    override fun encode(
        message: EventAppletFocus,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(if (message.inFocus) 1 else 0)
        return buf.buffer
    }
}

public class EventCameraPositionEncoder : ProxyMessageEncoder<EventCameraPosition> {
    override val prot: ClientProt = GameClientProt.EVENT_CAMERA_POSITION

    override fun encode(
        message: EventCameraPosition,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt1(message.angleX)
        buf.p2(message.angleY)
        return buf.buffer
    }
}

public class EventKeyboardEncoder : ProxyMessageEncoder<EventKeyboard> {
    override val prot: ClientProt = GameClientProt.EVENT_KEYBOARD

    override fun encode(
        message: EventKeyboard,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        if (message.keysPressed.length > 0) {
            val keys = message.keysPressed.asByteArray()
            for (i in keys.indices) {
                // First entry uses lastTransmittedKeyPress, subsequent assume 0 (compressed)
                val delta = if (i == 0) message.lastTransmittedKeyPress else 0
                val key = keys[i].toInt() and 0xFF
                buf.p3Alt1(delta)
                buf.p1Alt3(key)
            }
        }
        return buf.buffer
    }
}

public class EventMouseClickV1Encoder : ProxyMessageEncoder<EventMouseClickV1> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK_V1

    override fun encode(
        message: EventMouseClickV1,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        val packed = (message.lastTransmittedMouseClick shl 1) or (if (message.rightClick) 1 else 0)
        buf.p2(packed)
        buf.p2(message.x)
        buf.p2(message.y)
        return buf.buffer
    }
}

public class EventMouseClickV2Encoder : ProxyMessageEncoder<EventMouseClickV2> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_CLICK_V2

    override fun encode(
        message: EventMouseClickV2,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt3(message.x)
        val packed = (message.lastTransmittedMouseClick shl 1) or (if (message.rightClick) 1 else 0)
        buf.p2(packed)
        buf.p2Alt1(message.y)
        buf.p1Alt2(message.code)
        return buf.buffer
    }
}

public class EventMouseMoveEncoder : ProxyMessageEncoder<EventMouseMove> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_MOVE

    override fun encode(
        message: EventMouseMove,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.stepExcess)
        buf.p1(message.endExcess)
        for (i in 0 until message.movements.length) {
            val change = message.movements.getMousePosChange(i)
            val time = change.timeDelta
            val x = change.x
            val y = change.y
            val delta = change.delta

            if (delta && x >= -32 && x <= 31 && y >= -32 && y <= 31 && time in 0..7) {
                val packed = ((time and 0x7) shl 12) or (((x + 32) and 0x3F) shl 6) or ((y + 32) and 0x3F)
                buf.p2(packed)
            } else if (delta && x >= -128 && x <= 127 && y >= -128 && y <= 127 && time in 0..127) {
                buf.p1(0x80 or (time and 0x7F))
                buf.p1(x + 128)
                buf.p1(y + 128)
            } else if (!delta && time in 0..63) {
                buf.p1(0xC0 or (time and 0x3F))
                if (x == -1 && y == -1) {
                    buf.p4(Int.MIN_VALUE)
                } else {
                    val packedXY = (y shl 16) or (x and 0xFFFF)
                    buf.p4(packedXY)
                }
            } else {
                buf.p1(0xE0 or ((time ushr 8) and 0x1F))
                buf.p1(time and 0xFF)
                if (x == -1 && y == -1) {
                    buf.p4(Int.MIN_VALUE)
                } else {
                    val packedXY = (y shl 16) or (x and 0xFFFF)
                    buf.p4(packedXY)
                }
            }
        }
        return buf.buffer
    }
}

public class EventMouseScrollEncoder : ProxyMessageEncoder<EventMouseScroll> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_SCROLL

    override fun encode(
        message: EventMouseScroll,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2Alt3(message.mouseWheelRotation)
        return buf.buffer
    }
}

public class EventNativeMouseMoveEncoder : ProxyMessageEncoder<EventNativeMouseMove> {
    override val prot: ClientProt = GameClientProt.EVENT_NATIVE_MOUSE_MOVE

    override fun encode(
        message: EventNativeMouseMove,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.stepExcess)
        buf.p1(message.endExcess)
        for (i in 0 until message.movements.length) {
            val change = message.movements.getMousePosChange(i)
            val time = change.timeDelta
            val x = change.x
            val y = change.y
            val delta = change.delta

            if (delta && x >= -32 && x <= 31 && y >= -32 && y <= 31 && time in 0..7) {
                val packed = ((time and 0x7) shl 12) or (((x + 32) and 0x3F) shl 6) or ((y + 32) and 0x3F)
                buf.p2(packed)
            } else if (delta && x >= -128 && x <= 127 && y >= -128 && y <= 127 && time in 0..127) {
                buf.p1(0x80 or (time and 0x7F))
                buf.p1(x + 128)
                buf.p1(y + 128)
            } else if (!delta && time in 0..63) {
                buf.p1(0xC0 or (time and 0x3F))
                if (x == -1 && y == -1) {
                    buf.p2Alt3(0)
                    buf.p2Alt3(-0x8000)
                } else {
                    buf.p2Alt3(y)
                    buf.p2Alt3(x)
                }
            } else {
                buf.p1(0xE0 or ((time ushr 8) and 0x1F))
                buf.p1(time and 0xFF)
                if (x == -1 && y == -1) {
                    buf.p2Alt3(0)
                    buf.p2Alt3(-0x8000)
                } else {
                    buf.p2Alt3(y)
                    buf.p2Alt3(x)
                }
            }
            buf.p1(change.lastMouseButton)
        }
        return buf.buffer
    }
}
