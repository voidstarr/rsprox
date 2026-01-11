package net.rsprox.proxy.mcp.transcript

import net.rsprox.cache.api.CacheProvider
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.shared.symbols.SymbolDictionaryProvider
import net.rsprox.transcriber.prot.GameClientProt
import net.rsprox.transcriber.prot.GameServerProt

public class TranscriptStoreSessionMonitor(
    private val port: Int,
    private val store: TranscriptStore,
    settings: SettingSetStore,
) : SessionMonitor<BinaryHeader> {
    private var lastCacheProvider: CacheProvider? = null

    private val formatter =
        OmitFilteredPropertyTreeFormatter(
            PropertyFormatterCollection.default(
                SymbolDictionaryProvider.get(),
                settings,
            ) {
                this.lastCacheProvider?.get() ?: error("Cache unavailable")
            },
        )

    override fun onLogin(header: BinaryHeader) {
        store.updateLogin(port, header)
    }

    override fun onLogout(header: BinaryHeader) {
        store.updateLogout(port)
    }

    override fun onCacheUpdate(cacheProvider: CacheProvider) {
        this.lastCacheProvider = cacheProvider
    }

    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        // ignored
    }

    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        // ignored
    }

    override fun onNameUpdate(name: String) {
        store.updateName(port, name)
    }

    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
        store.updateUserInformation(port, userId, userHash)
    }

    override fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    ) {
        val direction = inferDirection(property.prot)
        val lines = formatter.format(property)
        store.append(
            port = port,
            cycle = cycle,
            direction = direction,
            prot = property.prot,
            lines = lines,
        )
    }

    private fun inferDirection(prot: String): TranscriptDirection {
        return when (prot) {
            in CLIENT_PROTS -> TranscriptDirection.C2S
            in SERVER_PROTS -> TranscriptDirection.S2C
            else -> TranscriptDirection.UNKNOWN
        }
    }

    private companion object {
        private val CLIENT_PROTS: Set<String> = GameClientProt.entries.mapTo(HashSet()) { it.name }
        private val SERVER_PROTS: Set<String> = GameServerProt.entries.mapTo(HashSet()) { it.name }
    }
}
