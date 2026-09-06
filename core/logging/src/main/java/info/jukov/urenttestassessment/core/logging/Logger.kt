package info.jukov.urenttestassessment.core.logging

/** Application-wide logging API. Feature code should depend on this instead of platform logging. */
interface Logger {
    fun verbose(message: String, vararg args: Any?)

    fun debug(message: String, vararg args: Any?)

    fun info(message: String, vararg args: Any?)

    fun warn(message: String, vararg args: Any?)

    fun warn(throwable: Throwable, message: String, vararg args: Any?)

    fun error(message: String, vararg args: Any?)

    fun error(throwable: Throwable, message: String, vararg args: Any?)

    fun withTag(tag: String): Logger
}
