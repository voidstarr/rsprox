package net.rsprox.proxy.util

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.RootProperty

public class CompositeSessionMonitor<T>(
    private val monitors: List<SessionMonitor<T>>,
) : SessionMonitor<T> {
    override fun onLogin(header: T) {
        for (monitor in monitors) {
            runCatching { monitor.onLogin(header) }
        }
    }

    override fun onLogout(header: T) {
        for (monitor in monitors) {
            runCatching { monitor.onLogout(header) }
        }
    }

    override fun onCacheUpdate(cacheProvider: CacheProvider) {
        for (monitor in monitors) {
            runCatching { monitor.onCacheUpdate(cacheProvider) }
        }
    }

    override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        for (monitor in monitors) {
            runCatching { monitor.onIncomingBytesPerSecondUpdate(bytesPerLastSecond) }
        }
    }

    override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        for (monitor in monitors) {
            runCatching { monitor.onOutgoingBytesPerSecondUpdate(bytesPerLastSecond) }
        }
    }

    override fun onNameUpdate(name: String) {
        for (monitor in monitors) {
            runCatching { monitor.onNameUpdate(name) }
        }
    }

    override fun onUserInformationUpdate(
        userId: Long,
        userHash: Long,
    ) {
        for (monitor in monitors) {
            runCatching { monitor.onUserInformationUpdate(userId, userHash) }
        }
    }

    override fun onTranscribe(
        cycle: Int,
        property: RootProperty,
    ) {
        for (monitor in monitors) {
            runCatching { monitor.onTranscribe(cycle, property) }
        }
    }
}
