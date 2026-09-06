package info.jukov.urenttestassessment.core.logging

/** Single logging entry point for application and feature code. */
object AppLogger : Logger by TimberLogger()
