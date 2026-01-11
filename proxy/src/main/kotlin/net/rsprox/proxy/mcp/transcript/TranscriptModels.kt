package net.rsprox.proxy.mcp.transcript

import net.rsprox.proxy.binary.BinaryHeader

public enum class TranscriptDirection {
    C2S,
    S2C,
    UNKNOWN,
}

public data class TranscriptEvent(
    public val seq: Long,
    public val epochMillis: Long,
    public val port: Int,
    public val cycle: Int,
    public val direction: TranscriptDirection,
    public val prot: String,
    public val lines: List<String>,
)

public data class TranscriptSessionInfo(
    public val port: Int,
    public val header: BinaryHeader?,
    public val name: String?,
    public val userId: Long?,
    public val userHash: Long?,
    public val lastSeenEpochMillis: Long,
)
