package net.rsprox.scripting.api

public enum class ScriptLogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

public interface ScriptLogger {
    public fun log(level: ScriptLogLevel, message: String, throwable: Throwable? = null)

    public fun debug(message: String) {
        log(ScriptLogLevel.DEBUG, message)
    }

    public fun info(message: String) {
        log(ScriptLogLevel.INFO, message)
    }

    public fun warn(message: String, throwable: Throwable? = null) {
        log(ScriptLogLevel.WARN, message, throwable)
    }

    public fun error(message: String, throwable: Throwable? = null) {
        log(ScriptLogLevel.ERROR, message, throwable)
    }
}
