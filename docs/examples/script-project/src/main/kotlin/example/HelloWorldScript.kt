package example

import net.rsprox.proxy.plugin.RSProxScript
import net.rsprox.proxy.plugin.ScriptContext
import net.rsprox.scripting.api.ScriptLogger

public class HelloWorldScript : RSProxScript {
    override val name: String = "Hello World"

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

        context.onLogin { login ->
            log.info("login: $login")
        }

        context.onClientPacket { packet ->
            // Keep this light; it runs for every packet.
            if (packet.prot.toString() == "MESSAGE_PUBLIC") {
                log.debug("saw MESSAGE_PUBLIC")
            }
        }
    }
}
