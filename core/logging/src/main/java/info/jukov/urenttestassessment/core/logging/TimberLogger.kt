package info.jukov.urenttestassessment.core.logging

import timber.log.Timber

/** Android [Logger] implementation backed by Timber. */
class TimberLogger internal constructor(private val tag: String? = null) : Logger {
    private fun tree(): Timber.Tree = tag?.let(Timber::tag) ?: Timber.asTree()

    override fun verbose(message: String, vararg args: Any?) = tree().v(message, *args)

    override fun debug(message: String, vararg args: Any?) = tree().d(message, *args)

    override fun info(message: String, vararg args: Any?) = tree().i(message, *args)

    override fun warn(message: String, vararg args: Any?) = tree().w(message, *args)

    override fun warn(throwable: Throwable, message: String, vararg args: Any?) = tree().w(throwable, message, *args)

    override fun error(message: String, vararg args: Any?) = tree().e(message, *args)

    override fun error(throwable: Throwable, message: String, vararg args: Any?) = tree().e(throwable, message, *args)

    override fun withTag(tag: String): Logger = TimberLogger(tag)
}
