import java.io.File
import java.util.Properties

/** Local release credentials, or null when the debug signing fallback should be used. */
class ReleaseSigning(val storeFile: File, val storePassword: String, val keyAlias: String, val keyPassword: String) {
    companion object {
        fun load(propertiesFile: File): ReleaseSigning? {
            if (!propertiesFile.isFile) return null
            val properties = Properties().apply { propertiesFile.inputStream().use { load(it) } }
            val hasCredentials = listOf("storeFile", "storePassword", "keyAlias", "keyPassword").all {
                !properties.getProperty(it).isNullOrBlank()
            }
            val storeFile = properties.getProperty("storeFile")?.let(propertiesFile.absoluteFile.parentFile::resolve)
            return if (hasCredentials && storeFile?.isFile == true) {
                ReleaseSigning(
                    storeFile,
                    properties.getProperty("storePassword"),
                    properties.getProperty("keyAlias"),
                    properties.getProperty("keyPassword")
                )
            } else {
                null
            }
        }
    }
}
