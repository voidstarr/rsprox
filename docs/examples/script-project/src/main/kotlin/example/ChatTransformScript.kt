package example

import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.proxy.plugin.RSProxScript
import net.rsprox.proxy.plugin.ScriptContext
import net.rsprox.scripting.api.ScriptLogger

public class YeetChatScript : RSProxScript {
public class ChatTransformScript : RSProxScript {
    override val name: String = "Chat Transform"

    override fun start(
        context: ScriptContext,
        log: ScriptLogger,
    ) {
        context.onEnable {
            log.info("enabled")
        }

        context.onDisable {
            log.info("disabled")
        }

        // Transform outgoing (client -> server) chat packets.
        // Returning the packet continues forwarding; returning null would drop it.
        context.onClientTransform { packet ->
            when (packet.prot.toString()) {
                "MESSAGE_PUBLIC" -> {
                    val m = packet.decode<MessagePublic>()
                    packet.encode(
                        MessagePublic(
                            type = m.type,
                            colour = m.colour,
                            effect = m.effect,
                            message = "yeet",
                            pattern = m.pattern,
                            clanType = m.clanType,
                        ),
                    )
                    packet
                }

                "MESSAGE_PRIVATE" -> {
                    val m = packet.decode<MessagePrivate>()
                    packet.encode(MessagePrivate(name = m.name, message = "yeet"))
                    packet
                }

                else -> packet
            }
        }
    }
}
