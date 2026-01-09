package net.rsprox.proxy.plugin

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import net.rsprox.proxy.scripting.RsproxScriptCompilationConfiguration

public class ScriptingSupport {
    public fun evaluate(script: String, context: ScriptContext, scriptName: String): Any? {
        val compilationConfiguration = RsproxScriptCompilationConfiguration()

        val logger = ProxyScriptLogger(scriptName)

        val host = BasicJvmScriptingHost()
        val evaluationConfiguration = ScriptEvaluationConfiguration {
            providedProperties("context" to context)
            providedProperties("log" to logger)
            implicitReceivers(context)
        }

        val result = host.eval(script.toScriptSource(), compilationConfiguration, evaluationConfiguration)

        return when (result) {
            is ResultWithDiagnostics.Success -> {
                val returnValue = result.value.returnValue
                if (returnValue is ResultValue.Value) {
                    returnValue.value
                } else {
                    null
                }
            }
            is ResultWithDiagnostics.Failure -> {
                val reports = result.reports.joinToString("\n") { report ->
                    val loc = report.location
                    val at =
                        if (loc != null) {
                            "@${loc.start.line}:${loc.start.col} "
                        } else {
                            ""
                        }
                    val sev = report.severity.name
                    "$sev $at${report.message}"
                }
                throw RuntimeException("Script evaluation failed:\n$reports")
            }
        }
    }
}
