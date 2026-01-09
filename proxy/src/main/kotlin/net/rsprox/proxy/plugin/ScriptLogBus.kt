package net.rsprox.proxy.plugin

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.ArrayDeque
import java.util.concurrent.CopyOnWriteArrayList
import net.rsprox.scripting.api.ScriptLogLevel

public data class ScriptLogEntry(
    val timestampMillis: Long,
    val level: ScriptLogLevel,
    val scriptName: String,
    val message: String,
    val throwableStackTrace: String? = null,
)

public object ScriptLogBus {
    private const val MAX_ENTRIES: Int = 2_000

    private val lock = Any()
    private val entries: ArrayDeque<ScriptLogEntry> = ArrayDeque(MAX_ENTRIES)
    private val listeners: CopyOnWriteArrayList<(ScriptLogEntry) -> Unit> = CopyOnWriteArrayList()

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())

    public fun append(entry: ScriptLogEntry) {
        synchronized(lock) {
            entries.addLast(entry)
            while (entries.size > MAX_ENTRIES) {
                entries.removeFirst()
            }
        }
        for (listener in listeners) {
            runCatching { listener(entry) }
        }
    }

    public fun snapshot(): List<ScriptLogEntry> {
        synchronized(lock) {
            return entries.toList()
        }
    }

    public fun addListener(listener: (ScriptLogEntry) -> Unit) {
        listeners.add(listener)
    }

    public fun removeListener(listener: (ScriptLogEntry) -> Unit) {
        listeners.remove(listener)
    }

    public fun format(entry: ScriptLogEntry): String {
        val time = timeFormatter.format(Instant.ofEpochMilli(entry.timestampMillis))
        val base = "[$time] [${entry.level}] [${entry.scriptName}] ${entry.message}"
        val stack = entry.throwableStackTrace
        return if (stack.isNullOrBlank()) base else base + "\n" + stack
    }
}
