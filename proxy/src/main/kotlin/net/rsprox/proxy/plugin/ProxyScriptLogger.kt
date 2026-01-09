package net.rsprox.proxy.plugin

import java.io.PrintWriter
import java.io.StringWriter
import net.rsprox.scripting.api.ScriptLogLevel
import net.rsprox.scripting.api.ScriptLogger

public class ProxyScriptLogger(private val scriptName: String) : ScriptLogger {
    override fun log(level: ScriptLogLevel, message: String, throwable: Throwable?) {
        val stack = throwable?.let { stackTraceToString(it) }
        ScriptLogBus.append(
            ScriptLogEntry(
                timestampMillis = System.currentTimeMillis(),
                level = level,
                scriptName = scriptName,
                message = message,
                throwableStackTrace = stack,
            ),
        )
    }

    private fun stackTraceToString(t: Throwable): String {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
