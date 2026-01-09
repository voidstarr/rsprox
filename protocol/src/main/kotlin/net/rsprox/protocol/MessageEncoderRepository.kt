package net.rsprox.protocol

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingGameMessage

public class MessageEncoderRepository(
    private val encoders: Map<ClientProt, ProxyMessageEncoder<*>>,
) {
    public fun getEncoder(prot: ClientProt): ProxyMessageEncoder<IncomingGameMessage> {
        @Suppress("UNCHECKED_CAST")
        return encoders[prot] as? ProxyMessageEncoder<IncomingGameMessage>
            ?: throw IllegalArgumentException("No encoder found for protocol: $prot")
    }
}

