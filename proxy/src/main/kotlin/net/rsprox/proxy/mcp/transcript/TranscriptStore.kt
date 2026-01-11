package net.rsprox.proxy.mcp.transcript

import net.rsprox.proxy.binary.BinaryHeader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

public class TranscriptStore(
    private val maxEventsPerPort: Int = 5_000,
    private val maxEventsGlobal: Int = 20_000,
) {
    private val seq = AtomicLong(0)

    private val lock = Any()
    private val perPort: MutableMap<Int, ArrayDeque<TranscriptEvent>> = ConcurrentHashMap()
    private val global: ArrayDeque<TranscriptEvent> = ArrayDeque()
    private val sessions: MutableMap<Int, TranscriptSessionInfo> = ConcurrentHashMap()

    public fun updateLogin(
        port: Int,
        header: BinaryHeader,
    ) {
        val now = System.currentTimeMillis()
        sessions[port] =
            TranscriptSessionInfo(
                port = port,
                header = header,
                name = sessions[port]?.name,
                userId = sessions[port]?.userId,
                userHash = sessions[port]?.userHash,
                lastSeenEpochMillis = now,
            )
    }

    public fun updateLogout(port: Int) {
        val now = System.currentTimeMillis()
        val old = sessions[port]
        sessions[port] =
            TranscriptSessionInfo(
                port = port,
                header = old?.header,
                name = old?.name,
                userId = old?.userId,
                userHash = old?.userHash,
                lastSeenEpochMillis = now,
            )
    }

    public fun updateName(
        port: Int,
        name: String,
    ) {
        val now = System.currentTimeMillis()
        val old = sessions[port]
        sessions[port] =
            TranscriptSessionInfo(
                port = port,
                header = old?.header,
                name = name,
                userId = old?.userId,
                userHash = old?.userHash,
                lastSeenEpochMillis = now,
            )
    }

    public fun updateUserInformation(
        port: Int,
        userId: Long,
        userHash: Long,
    ) {
        val now = System.currentTimeMillis()
        val old = sessions[port]
        sessions[port] =
            TranscriptSessionInfo(
                port = port,
                header = old?.header,
                name = old?.name,
                userId = userId,
                userHash = userHash,
                lastSeenEpochMillis = now,
            )
    }

    public fun append(
        port: Int,
        cycle: Int,
        direction: TranscriptDirection,
        prot: String,
        lines: List<String>,
        epochMillis: Long = System.currentTimeMillis(),
    ): TranscriptEvent {
        val event =
            TranscriptEvent(
                seq = seq.incrementAndGet(),
                epochMillis = epochMillis,
                port = port,
                cycle = cycle,
                direction = direction,
                prot = prot,
                lines = lines,
            )

        synchronized(lock) {
            val deque = perPort.getOrPut(port) { ArrayDeque() }
            deque.addLast(event)
            while (deque.size > maxEventsPerPort) {
                deque.removeFirst()
            }

            global.addLast(event)
            while (global.size > maxEventsGlobal) {
                global.removeFirst()
            }
        }

        val now = System.currentTimeMillis()
        val old = sessions[port]
        sessions[port] =
            TranscriptSessionInfo(
                port = port,
                header = old?.header,
                name = old?.name,
                userId = old?.userId,
                userHash = old?.userHash,
                lastSeenEpochMillis = now,
            )

        return event
    }

    public fun listSessions(): List<TranscriptSessionInfo> {
        return sessions.values.sortedBy { it.port }
    }

    public fun snapshot(
        sinceSeqExclusive: Long = 0,
        limit: Int = 200,
        port: Int? = null,
        direction: TranscriptDirection? = null,
        cycleStart: Int? = null,
        cycleEnd: Int? = null,
        contains: String? = null,
    ): List<TranscriptEvent> {
        require(limit in 1..50_000) { "Invalid limit: $limit" }

        val source: List<TranscriptEvent> =
            synchronized(lock) {
                if (port != null) {
                    perPort[port]?.toList() ?: emptyList()
                } else {
                    global.toList()
                }
            }

        val containsLc = contains?.lowercase()

        return source
            .asSequence()
            .filter { it.seq > sinceSeqExclusive }
            .filter { ev -> direction == null || ev.direction == direction }
            .filter { ev -> (cycleStart == null || ev.cycle >= cycleStart) && (cycleEnd == null || ev.cycle <= cycleEnd) }
            .filter { ev ->
                if (containsLc == null) return@filter true
                if (ev.prot.lowercase().contains(containsLc)) return@filter true
                ev.lines.any { it.lowercase().contains(containsLc) }
            }
            .take(limit)
            .toList()
    }

    public fun latestSeq(): Long = seq.get()
}
