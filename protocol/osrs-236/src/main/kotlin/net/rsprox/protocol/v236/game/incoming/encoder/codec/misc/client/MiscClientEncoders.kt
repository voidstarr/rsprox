package net.rsprox.protocol.v236.game.incoming.encoder.codec.misc.client

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageEncoder
import net.rsprox.protocol.game.incoming.model.misc.client.ConnectionTelemetry
import net.rsprox.protocol.game.incoming.model.misc.client.DetectModifiedClient
import net.rsprox.protocol.game.incoming.model.misc.client.Idle
import net.rsprox.protocol.game.incoming.model.misc.client.MapBuildComplete
import net.rsprox.protocol.game.incoming.model.misc.client.MembershipPromotionEligibility
import net.rsprox.protocol.game.incoming.model.misc.client.NoTimeout
import net.rsprox.protocol.game.incoming.model.misc.client.RSevenStatus
import net.rsprox.protocol.game.incoming.model.misc.client.ReflectionCheckReply
import net.rsprox.protocol.game.incoming.model.misc.client.SendPingReply
import net.rsprox.protocol.game.incoming.model.misc.client.SoundJingleEnd
import net.rsprox.protocol.game.incoming.model.misc.client.WindowStatus
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.incoming.decoder.prot.GameClientProt
import java.io.IOException
import java.io.InvalidClassException
import java.io.OptionalDataException
import java.io.StreamCorruptedException
import java.lang.reflect.InvocationTargetException
import java.util.zip.CRC32

public class ConnectionTelemetryEncoder : ProxyMessageEncoder<ConnectionTelemetry> {
    override val prot: ClientProt = GameClientProt.CONNECTION_TELEMETRY

    override fun encode(
        message: ConnectionTelemetry,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p2(message.connectionLostDuration)
        buf.p2(message.loginDuration)
        buf.p2(0) // unusedDuration
        buf.p2(message.clientState)
        buf.p2(0) // unused1
        buf.p2(message.loginCount)
        buf.p2(0) // unused2
        return buf.buffer
    }
}

public class DetectModifiedClientEncoder : ProxyMessageEncoder<DetectModifiedClient> {
    override val prot: ClientProt = GameClientProt.DETECT_MODIFIED_CLIENT

    override fun encode(
        message: DetectModifiedClient,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p4(message.code)
        return buf.buffer
    }
}

public class IdleEncoder : ProxyMessageEncoder<Idle> {
    override val prot: ClientProt = GameClientProt.IDLE

    override fun encode(
        message: Idle,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        return buf.buffer
    }
}

public class MapBuildCompleteEncoder : ProxyMessageEncoder<MapBuildComplete> {
    override val prot: ClientProt = GameClientProt.MAP_BUILD_COMPLETE

    override fun encode(
        message: MapBuildComplete,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        return buf.buffer
    }
}

public class MembershipPromotionEligibilityEncoder : ProxyMessageEncoder<MembershipPromotionEligibility> {
    override val prot: ClientProt = GameClientProt.MEMBERSHIP_PROMOTION_ELIGIBILITY

    override fun encode(
        message: MembershipPromotionEligibility,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.eligibleForIntroductoryPrice)
        buf.p1(message.eligibleForTrialPurchase)
        return buf.buffer
    }
}

public class NoTimeoutEncoder : ProxyMessageEncoder<NoTimeout> {
    override val prot: ClientProt = GameClientProt.NO_TIMEOUT

    override fun encode(
        message: NoTimeout,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        return buf.buffer
    }
}

public class RSevenStatusEncoder : ProxyMessageEncoder<RSevenStatus> {
    override val prot: ClientProt = GameClientProt.RSEVEN_STATUS

    override fun encode(
        message: RSevenStatus,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.packedValue)
        return buf.buffer
    }
}

public class ReflectionCheckReplyEncoder : ProxyMessageEncoder<ReflectionCheckReply> {
    override val prot: ClientProt = GameClientProt.REFLECTION_CHECK_REPLY

    override fun encode(
        message: ReflectionCheckReply,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p4(message.id)

        for (result in message.result) {
            if (result is ReflectionCheckReply.ErrorResult<*, *>) {
                val opcode =
                    when (val type = result.throwable) {
                        is ReflectionCheckReply.ErrorResult.ThrowableResultType.ConstructionThrowable<*> -> {
                            getConstructionThrowableOpcode(type.throwableClass)
                        }
                        is ReflectionCheckReply.ErrorResult.ThrowableResultType.ExecutionThrowable<*> -> {
                            getExecutionThrowableOpcode(type.throwableClass)
                        }
                    }
                buf.p1(opcode)
                continue
            }

            when (result) {
                is ReflectionCheckReply.GetFieldValueResult -> {
                    buf.p1(0)
                    buf.p4(result.value)
                }
                is ReflectionCheckReply.SetFieldValueResult -> {
                    buf.p1(0)
                }
                is ReflectionCheckReply.GetFieldModifiersResult -> {
                    buf.p1(0)
                    buf.p4(result.modifiers)
                }
                is ReflectionCheckReply.InvokeMethodResult<*> -> {
                    when (val returnValue = result.result) {
                        is ReflectionCheckReply.NullReturnValue -> buf.p1(0)
                        is ReflectionCheckReply.NumberReturnValue -> {
                            buf.p1(1)
                            buf.p8(returnValue.longValue)
                        }
                        is ReflectionCheckReply.StringReturnValue -> {
                            buf.p1(2)
                            buf.pjstr(returnValue.stringValue)
                        }
                        is ReflectionCheckReply.UnknownReturnValue -> buf.p1(4)
                    }
                }
                is ReflectionCheckReply.GetMethodModifiersResult -> {
                    buf.p1(0)
                    buf.p4(result.modifiers)
                }
                else -> {} // ErrorResult handled above
            }
        }

        // Calculate CRC32 of the buffer content
        val crc = CRC32()
        if (buf.buffer.hasArray()) {
            crc.update(buf.buffer.array(), buf.buffer.arrayOffset(), buf.buffer.readableBytes())
        } else {
            val bytes = ByteArray(buf.buffer.readableBytes())
            buf.buffer.getBytes(buf.buffer.readerIndex(), bytes)
            crc.update(bytes)
        }
        buf.p4(crc.value.toInt())

        return buf.buffer
    }

    private fun getExecutionThrowableOpcode(clazz: Class<out Throwable>): Int =
        when (clazz) {
            ClassNotFoundException::class.java -> -10
            InvalidClassException::class.java -> -11
            StreamCorruptedException::class.java -> -12
            OptionalDataException::class.java -> -13
            IllegalAccessException::class.java -> -14
            IllegalArgumentException::class.java -> -15
            InvocationTargetException::class.java -> -16
            SecurityException::class.java -> -17
            IOException::class.java -> -18
            NullPointerException::class.java -> -19
            Exception::class.java -> -20
            Throwable::class.java -> -21
            else -> throw IllegalArgumentException("Unknown execution throwable class: $clazz")
        }

    private fun getConstructionThrowableOpcode(clazz: Class<out Throwable>): Int =
        when (clazz) {
            ClassNotFoundException::class.java -> -1
            SecurityException::class.java -> -2
            NullPointerException::class.java -> -3
            Exception::class.java -> -4
            Throwable::class.java -> -5
            else -> throw IllegalArgumentException("Unknown construction throwable class: $clazz")
        }
}

public class SendPingReplyEncoder : ProxyMessageEncoder<SendPingReply> {
    override val prot: ClientProt = GameClientProt.SEND_PING_REPLY

    override fun encode(
        message: SendPingReply,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        // value1: g4
        buf.p4(message.value1)
        // value2: g4Alt3
        buf.p4Alt3(message.value2)
        // gcPercentTime: g1Alt1
        buf.p1Alt1(message.gcPercentTime)
        // fps: g1Alt3
        buf.p1Alt3(message.fps)
        return buf.buffer
    }
}

public class SoundJingleEndEncoder : ProxyMessageEncoder<SoundJingleEnd> {
    override val prot: ClientProt = GameClientProt.SOUND_JINGLEEND

    override fun encode(
        message: SoundJingleEnd,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p4(message.jingleId)
        return buf.buffer
    }
}

public class WindowStatusEncoder : ProxyMessageEncoder<WindowStatus> {
    override val prot: ClientProt = GameClientProt.WINDOW_STATUS

    override fun encode(
        message: WindowStatus,
        session: Session,
    ): ByteBuf {
        val buf = Unpooled.buffer().toJagByteBuf()
        buf.p1(message.windowMode)
        buf.p2(message.frameWidth)
        buf.p2(message.frameHeight)
        return buf.buffer
    }
}
