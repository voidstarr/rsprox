package net.rsprox.protocol

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingGameMessage

public class MessageEncoderRepositoryBuilder {
    @PublishedApi
    internal val encoders: MutableMap<ClientProt, ProxyMessageEncoder<*>> = mutableMapOf()

    public inline fun <reified T : IncomingGameMessage> bind(encoder: ProxyMessageEncoder<T>) {
        encoders[encoder.prot] = encoder
    }

    public fun build(): MessageEncoderRepository {
        return MessageEncoderRepository(encoders)
    }
}

