package info.jukov.urenttestassestment.core.logging

/** Single logging entry point for application and feature code. */
object AppLogger : Logger by TimberLogger()
